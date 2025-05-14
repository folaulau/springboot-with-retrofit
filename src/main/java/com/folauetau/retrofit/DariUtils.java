package com.folauetau.retrofit;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class DariUtils {
    private static final Set<String> ABBREVIATIONS = new HashSet(Arrays.asList("api", "cms", "css", "ftp", "ftps", "id", "js", "seo", "ugc", "uri", "url"));
    private static final char[] HEX_CHARACTERS;
    private static final int CASE_DIFF = 32;
    private static final BitSet PCHAR;

    public DariUtils() {
    }

    public static String toLowerCase(String string) {
        return string != null ? string.toLowerCase(Locale.ROOT) : null;
    }

    public static String toUpperCase(String string) {
        return string != null ? string.toUpperCase(Locale.ROOT) : null;
    }

    protected static List<String> splitString(String string) {
        List<String> words = new ArrayList();
        char[] letters = string.toCharArray();
        int length = letters.length;
        int marker = 0;

        for(int i = 0; i < length; ++i) {
            char c = letters[i];
            if (" -_.$".indexOf(c) <= -1) {
                if (Character.isUpperCase(c) && i > 0 && Character.isLowerCase(letters[i - 1])) {
                    words.add(string.substring(marker, i).toLowerCase(Locale.ROOT));
                    marker = i;
                }
            } else {
                words.add(string.substring(marker, i).toLowerCase(Locale.ROOT));
                ++i;

                while(i < length && " -_.$".indexOf(letters[i]) > -1) {
                    ++i;
                }

                marker = i;
            }
        }

        if (marker < length) {
            words.add(string.substring(marker).toLowerCase(Locale.ROOT));
        }

        words.removeIf((w) -> w.length() == 0);
        return words;
    }

    public static String toDelimited(String string, String delimiter) {
        StringBuilder nb = new StringBuilder();

        for(String word : splitString(string)) {
            nb.append(word).append(delimiter);
        }

        if (nb.length() > 0) {
            nb.setLength(nb.length() - delimiter.length());
        }

        return nb.toString();
    }

    public static String toHyphenated(String string) {
        return toDelimited(string, "-");
    }

    public static String toUnderscored(String string) {
        return toDelimited(string, "_");
    }

    public static String toPascalCase(String string) {
        StringBuilder nb = new StringBuilder();

        for(String word : splitString(string)) {
            if (!word.isEmpty()) {
                nb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }

        return nb.toString();
    }

    public static String toCamelCase(String string) {
        string = toPascalCase(string);
        char var10000 = Character.toLowerCase(string.charAt(0));
        return var10000 + string.substring(1);
    }

    public static String toNormalized(CharSequence string) {
        if (string == null) {
            return null;
        } else {
            String normalized = Normalizer.normalize(string, Form.NFD);
            normalized = normalized.replaceAll("[̀-ͯ]+", "");
            normalized = Normalizer.normalize(normalized, Form.NFC);
            normalized = normalized.replaceAll("[\\p{Pe}\\p{Pf}\\p{Pi}\\p{Ps}'\"]+", "");
            normalized = normalized.replaceAll("[^\\p{L}\\p{N}]+", "-");
            normalized = normalized.replaceAll("(?:^-+)|(?:-+$)", "");
            normalized = normalized.replaceAll("-+", "-");
            return normalized.toLowerCase(Locale.ROOT);
        }
    }

    public static String hex(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {
            int bytesLength = bytes.length;
            char[] hex = new char[bytesLength * 2];
            int byteIndex = 0;

            for(int hexIndex = 0; byteIndex < bytesLength; hexIndex += 2) {
                byte currentByte = bytes[byteIndex];
                hex[hexIndex] = HEX_CHARACTERS[(currentByte & 240) >> 4];
                hex[hexIndex + 1] = HEX_CHARACTERS[currentByte & 15];
                ++byteIndex;
            }

            return new String(hex);
        }
    }

    public static String encodePathSegment(String segment) {
        if (segment == null) {
            return null;
        } else {
            StringBuilder encoded = new StringBuilder();
            int i = 0;

            for(int l = segment.length(); i < l; ++i) {
                char c = segment.charAt(i);
                if (PCHAR.get(c)) {
                    encoded.append(c);
                } else if (Character.isHighSurrogate(c) && i + 1 < l && Character.isLowSurrogate(segment.charAt(i + 1))) {
                    appendEncoded(encoded, segment.substring(i, i + 2));
                    ++i;
                } else {
                    appendEncoded(encoded, segment.substring(i, i + 1));
                }
            }

            return encoded.toString();
        }
    }

    private static void appendEncoded(StringBuilder encoded, String s) {
        for(byte b : s.getBytes(StandardCharsets.UTF_8)) {
            encoded.append('%');
            encoded.append(hexCharacter(b >> 4));
            encoded.append(hexCharacter(b));
        }

    }

    private static char hexCharacter(int c) {
        char h = Character.forDigit(c & 15, 16);
        return Character.isLetter(h) ? (char)(h - 32) : h;
    }

    public static String encodeUri(String string) {
        if (string == null) {
            return null;
        } else {
            try {
                return URLEncoder.encode(string, StandardCharsets.UTF_8.name()).replace("+", "%20");
            } catch (UnsupportedEncodingException error) {
                throw new IllegalStateException(error);
            }
        }
    }

    private static String decode(String string, boolean decodePlus) {
        if (string == null) {
            return null;
        } else if ((!decodePlus || string.indexOf(43) < 0) && string.indexOf(37) < 0) {
            return string;
        } else {
            StringBuilder decoded = new StringBuilder();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            int i = 0;

            for(int l = string.length(); i < l; ++i) {
                char c = string.charAt(i);
                if (decodePlus && c == '+') {
                    decoded.append(' ');
                } else {
                    bytes.reset();

                    while(c == '%' && i + 2 < l) {
                        try {
                            bytes.write(Integer.parseInt(string.substring(i + 1, i + 3), 16));
                        } catch (NumberFormatException var8) {
                            break;
                        }

                        i += 3;
                        if (i >= l) {
                            break;
                        }

                        c = string.charAt(i);
                    }

                    if (bytes.size() > 0) {
                        decoded.append(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
                        --i;
                    } else {
                        decoded.append(c);
                    }
                }
            }

            return decoded.toString();
        }
    }

    public static String decodeUrl(String string) {
        return decode(string, false);
    }

    public static String decodeForm(String string) {
        return decode(string, true);
    }

    public static String sanitizeLogArgument(Object argument) {
        return argument != null ? argument.toString().replaceAll("[\r\n]", " ") : null;
    }

    static {
//        EXACT_WORDS_PATTERN = Pattern.compile("(^| )(" + (String)EXACT_WORDS.keySet().stream().map(Pattern::quote).collect(Collectors.joining("|")) + ")( |$)", 2);
        HEX_CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        PCHAR = new BitSet(256);

        for(int i = 65; i <= 90; ++i) {
            PCHAR.set(i);
        }

        for(int i = 97; i <= 122; ++i) {
            PCHAR.set(i);
        }

        for(int i = 48; i <= 57; ++i) {
            PCHAR.set(i);
        }

        PCHAR.set(45);
        PCHAR.set(46);
        PCHAR.set(95);
        PCHAR.set(126);
        PCHAR.set(33);
        PCHAR.set(36);
        PCHAR.set(38);
        PCHAR.set(39);
        PCHAR.set(40);
        PCHAR.set(41);
        PCHAR.set(42);
        PCHAR.set(43);
        PCHAR.set(44);
        PCHAR.set(59);
        PCHAR.set(61);
        PCHAR.set(58);
        PCHAR.set(64);
    }
}
