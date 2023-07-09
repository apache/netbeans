/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.installer.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.Platform;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class StringUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Static
    public static String format(
            final String message,
            final Object... arguments) {
        return MessageFormat.format(message, arguments);
    }
    
    public static String leftTrim(
            final String string) {
        return string.replaceFirst(LEFT_WHITESPACE, EMPTY_STRING);
    }
    
    public static String rightTrim(
            final String string) {
        return string.replaceFirst(RIGHT_WHITESPACE, EMPTY_STRING);
    }
    
    public static char fetchMnemonic(
            final String string) {
        // source is org.openide.awt.Mnemonics
        int i = findMnemonicAmpersand(string);
        return (i >= 0) ? string.charAt(i+1) : NO_MNEMONIC;
    }
    
    public static String stripMnemonic(
            final String string) {
        int i = findMnemonicAmpersand(string);
        String s = string;
        if( i>=0 ) {
            if (string.startsWith("<html>")) { // NOI18N
                // Workaround for JDK bug #6510775                
                s = string.substring(0, i) + 
                        "<u>" + string.charAt(i + 1) + "</u>" + // NOI18N
                        string.substring(i + 2);                 
            } else {
                s = string.substring(0, i) + string.substring(i + 1);
            }
        } 
        return s;
    }
    
    // source - org.openide.awt.Mnenonics;
    public static int findMnemonicAmpersand(String text) {
        int i = -1;
        boolean isHTML = text.startsWith("<html>");
        
        do {
            // searching for the next ampersand
            i = text.indexOf(MNEMONIC_CHAR, i + 1);
            
            if ((i >= 0) && ((i + 1) < text.length())) {
                if (isHTML) {
                    boolean startsEntity = false;
                    for (int j = i + 1; j < text.length(); j++) {
                        char c = text.charAt(j);
                        if (c == ';') {
                            startsEntity = true;
                            break;
                        }
                        if (!Character.isLetterOrDigit(c)) {
                            break;
                        }
                    }
                    if (!startsEntity) {
                        return i;
                    }
                } else {
                    // before ' '
                    if (text.charAt(i + 1) == ' ') {
                        continue;
                        
                        // before ', and after '
                    } else if ((text.charAt(i + 1) == '\'') && (i > 0) && (text.charAt(i - 1) == '\'')) {
                        continue;
                    }
                    
                    // ampersand is marking mnemonics
                    return i;
                }
            }
        } while (i >= 0);
        
        return -1;
    }
    
    public static String capitalizeFirst(
            final String string) {
        return EMPTY_STRING + Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
    
    public static String getGetterName(
            final String propertyName) {
        return "get" + capitalizeFirst(propertyName);
    }
    
    public static String getBooleanGetterName(
            final String propertyName) {
        return "is" + capitalizeFirst(propertyName);
    }
    
    public static String getSetterName(
            final String propertyName) {
        return "set" + capitalizeFirst(propertyName);
    }
    
    public static String getFilenameFromUrl(
            final String string) {
        String url = string.trim();
        
        int index = Math.max(
                url.lastIndexOf(FORWARD_SLASH),
                url.lastIndexOf(BACK_SLASH));
        int length = url.length();
        return (index > 0 && (index < length - 1)) ?
            url.substring(index + 1,  length) : null;
    }
    
    public static String formatSize(
            final long longBytes) {
        double bytes = (double) longBytes;
        
        // try as GB
        double gigabytes = bytes / 1024. / 1024. / 1024.;
        if (gigabytes > 1.) {
            return String.format("%.1f GB", gigabytes);
        }
        
        // try as MB
        double megabytes = bytes / 1024. / 1024.;
        if (megabytes > 1.) {
            return String.format("%.1f MB", megabytes);
        }
        
        // try as KB
        double kilobytes = bytes / 1024.;
        if (kilobytes > .5) {
            return String.format("%.1f KB", kilobytes);
        }
        
        // return as bytes
        return EMPTY_STRING + longBytes + " B";
    }
    
    public static String asHexString(
            final byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            
            String byteHex = Integer.toHexString(b);
            if (byteHex.length() == 1) {
                byteHex = "0" + byteHex;
            }
            if (byteHex.length() > 2) {
                byteHex = byteHex.substring(byteHex.length() - 2);
            }
            
            builder.append(byteHex);
        }
        
        return builder.toString();
    }
    
    public static String pad(
            final String string,
            final int number) {
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < number; i++) {
            builder.append(string);
        }
        
        return builder.toString();
    }
    
    public static String escapeRegExp(
            final String string) {
        return string.replace(BACK_SLASH, BACK_SLASH + BACK_SLASH).replace("$", "\\$");
    }
    public static String [] splitByLines(CharSequence cs) {
        return splitByLines(cs.toString());
    }
    public static String [] splitByLines(String s) {
        return s.split(NEW_LINE_PATTERN, -1);
    }
    public static String readStream(
            final InputStream stream) throws IOException {
        return readStream(stream, null);
    }
    public static String readStream(
            final InputStream stream, String charset) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        byte[] buffer = new byte[1024];
        while (stream.available() > 0) {
            int read = stream.read(buffer);
            
            String readString =  (charset==null) ? 
                new String(buffer, 0, read) : 
                new String(buffer, 0, read, charset);
            String[] strings = splitByLines(readString);
            for(int i=0;i<strings.length;i++) {
                builder.append(strings[i]);
                if ( i != strings.length - 1 ) {
                    builder.append(SystemUtils.getLineSeparator());
                }
            }
        }
        
        return builder.toString();
    }
    
    public static String httpFormat(
            final Date date) {
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(date);
    }
    
    public static String asPath(
            final Class clazz) {
        return clazz.getPackage().getName().replace('.', '/');
    }
    
    public static String replace(
            final String string,
            final String replacement,
            final int begin,
            final int end) {
        return string.substring(0, begin) + replacement + string.substring(end);
    }
    
    /**
     * Escapes the path using the platform-specific escape rules.
     *
     * @param path Path to escape.
     * @return Escaped path.
     */
    public static String escapePath(
            final String path) {
        String localPath = path;
        
        if (localPath.indexOf(' ') > -1) {
            if (SystemUtils.isWindows()) {
                localPath = QUOTE + localPath + QUOTE;
            } else {
                localPath = localPath.replace(SPACE,
                        BACK_SLASH + SPACE); //NOI18N
            }
        }
        
        return localPath;
    }
    
    /**
     * Joins a command string and its arguments into a single string using the
     * platform-specific rules.
     *
     * @param commandArray The command and its arguments.
     * @return The joined string.
     */
    public static String joinCommand(
            final String... commandArray) {
        StringBuffer command = new StringBuffer();
        
        for (int i = 0; i < commandArray.length; i++) {
            command.append(escapePath(commandArray[i]));
            if (i != commandArray.length - 1) {
                command.append(SPACE); //NOI18N
            }
        }
        
        return command.toString();
    }
    
    // object -> string .////////////////////////////////////////////////////////////
    public static String asString(
            final Throwable throwable) {
        final StringWriter writer = new StringWriter();
        
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
    
    public static String asString(
            final List<? extends Object> objects) {
        return asString(objects.toArray(), 0, objects.size(), ", ");
    }
    
    public static String asString(
            final List<? extends Object> objects,
            final String separator) {
        return asString(objects.toArray(), 0, objects.size(), separator);
    }
    
    public static String asString(
            final List<? extends Object> objects,
            final int offset,
            final int length,
            final String separator) {
        return asString(objects.toArray(), offset, length, separator);
    }
    
    public static String asString(
            final Object[] objects) {
        return asString(objects, 0, objects.length, ", ");
    }
    
    public static String asString(
            final Object[] objects,
            final String separator) {
        return asString(objects, 0, objects.length, separator);
    }
    
    public static String asString(
            final Object[] objects,
            final int offset,
            final int length,
            final String separator) {
        final StringBuilder result = new StringBuilder();
        
        for (int i = offset; i < offset + length; i++) {
            result.append(EMPTY_STRING + objects[i]);
            
            if (i != offset + length - 1) {
                result.append(separator);
            }
        }
        
        return result.toString();
    }
    
    // base64 ///////////////////////////////////////////////////////////////////////
    public static String base64Encode(
            final String string) throws UnsupportedEncodingException {
        return base64Encode(string, ENCODING_UTF8);
    }
    
    public static String base64Encode(
            final String string,
            final String charset) throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();
        final byte[] bytes = string.getBytes(charset);
        
        int i;
        for (i = 0; i < bytes.length - 2; i += 3) {
            int byte1 = bytes[i] & BIN_11111111;
            int byte2 = bytes[i + 1] & BIN_11111111;
            int byte3 = bytes[i + 2] & BIN_11111111;
            
            builder.append(
                    BASE64_TABLE[byte1 >> 2]);
            builder.append(
                    BASE64_TABLE[((byte1 << 4) & BIN_00110000) | (byte2 >> 4)]);
            builder.append(
                    BASE64_TABLE[((byte2 << 2) & BIN_00111100) | (byte3 >> 6)]);
            builder.append(
                    BASE64_TABLE[byte3 & BIN_00111111]);
        }
        
        if (i == bytes.length - 2) {
            int byte1 = bytes[i] & BIN_11111111;
            int byte2 = bytes[i + 1] & BIN_11111111;
            
            builder.append(
                    BASE64_TABLE[byte1 >> 2]);
            builder.append(
                    BASE64_TABLE[((byte1 << 4) & BIN_00110000) | (byte2 >> 4)]);
            builder.append(
                    BASE64_TABLE[(byte2 << 2) & BIN_00111100]);
            builder.append(
                    BASE64_PAD);
        }
        
        if (i == bytes.length - 1) {
            int byte1 = bytes[i] & BIN_11111111;
            
            builder.append(
                    BASE64_TABLE[byte1 >> 2]);
            builder.append(
                    BASE64_TABLE[(byte1 << 4) & BIN_00110000]);
            builder.append(
                    BASE64_PAD);
            builder.append(
                    BASE64_PAD);
        }
        
        return builder.toString();
    }
    
    public static String base64Decode(
            final String string) throws UnsupportedEncodingException {
        return base64Decode(string, ENCODING_UTF8);
    }
    
    public static String base64Decode(
            final String string,
            final String charset) throws UnsupportedEncodingException {
        int completeBlocksNumber = string.length() / 4;
        int missingBytesNumber = 0;
        
        if (string.endsWith("=")) {
            completeBlocksNumber--;
            missingBytesNumber++;
        }
        if (string.endsWith("==")) {
            missingBytesNumber++;
        }
        
        int decodedLength = (completeBlocksNumber * 3) + (3 - missingBytesNumber) % 3;
        byte[] decodedBytes = new byte[decodedLength];
        
        int encodedCounter = 0;
        int decodedCounter = 0;
        for (int i = 0; i < completeBlocksNumber; i++) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte3 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte4 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
            decodedBytes[decodedCounter++] = (byte) ((byte2 << 4) | (byte3 >> 2));
            decodedBytes[decodedCounter++] = (byte) ((byte3 << 6) | byte4);
        }
        
        if (missingBytesNumber == 1) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte3 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
            decodedBytes[decodedCounter++] = (byte) ((byte2 << 4) | (byte3 >> 2));
        }
        
        if (missingBytesNumber == 2) {
            int byte1 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            int byte2 = BASE64_REVERSE_TABLE[string.charAt(encodedCounter++)];
            
            decodedBytes[decodedCounter++] = (byte) ((byte1 << 2) | (byte2 >> 4));
        }
        
        return new String(decodedBytes, charset);
    }
    
    // normal <-> ascii only ////////////////////////////////////////////////////////
    public static String parseAscii(final String string) {
        final Properties properties = new Properties();
        
        // we don't really care about enconding here, as the input string is
        // expected to be ASCII-only, which means it's the same for any encoding
        try {
            properties.load(new ByteArrayInputStream(("key=" + string).getBytes()));
        } catch (IOException e) {
            ErrorManager.notifyWarning(
                    "Cannot parse string",
                    e);
            return string;
        }
        
        return (String) properties.get("key");
    }
    
    public static String convertToAscii(final String string) {
        final Properties properties = new Properties();
        
        properties.put("uberkey", string);
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            properties.store(baos, EMPTY_STRING);
        } catch (IOException e) {
            ErrorManager.notifyWarning(
                    "Cannot convert string",
                    e);
            return string;
        }
        
        final Matcher matcher = Pattern.
                compile("uberkey=(.*)$", Pattern.MULTILINE).
                matcher(baos.toString());
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return string;
        }
    }
    
    // string -> object /////////////////////////////////////////////////////////////
    public static List<String> asList(
            final String string) {
        return asList(string, ", ");
    }
    
    public static List<String> asList(
            final String string, final String separator) {
        return Arrays.asList(string.split(separator));
    }
    
    public static Locale parseLocale(
            final String string) {
        final String[] parts = string.split("_");
        
        switch (parts.length) {
            case 1:
                return new Locale(parts[0]);
            case 2:
                return new Locale(parts[0], parts[1]);
            default:
                return new Locale(parts[0], parts[1], parts[2]);
        }
    }
    public static String getLocalizedString(final Map <Locale, String> stringsMap, final Locale inLocale) {
        final String message = stringsMap.get(inLocale);
        if(message==null && !inLocale.equals(new Locale(EMPTY_STRING))) {
            final Locale upLocale;
            if(!inLocale.getVariant().equals(EMPTY_STRING)) {
                upLocale = new Locale(inLocale.getLanguage(), inLocale.getCountry());
            } else if(!inLocale.getCountry().equals(EMPTY_STRING)) {
                upLocale = new Locale(inLocale.getLanguage());
            } else {
                upLocale = new Locale(EMPTY_STRING);
            }
            return getLocalizedString(stringsMap, upLocale);
       } else {
            return message;
       }
    }
    public static URL parseUrl(
            final String string) throws ParseException {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            throw new ParseException("Cannot parse URL", e);
        }
    }
    
    public static Platform parsePlatform(
            final String string) throws ParseException {
        for (Platform platform: Platform.values()) {
            if (platform.getCodeName().equals(string)) {
                return platform;
            }
        }
        
        throw new ParseException(ResourceUtils.getString(StringUtils.class,
                StringUtils.ERROR_UNKNOWN_PLATFORM, string));
    }
    
    public static List<Platform> parsePlatforms(
            final String string) throws ParseException {
        final List<Platform> platforms = new ArrayList<Platform>();
        
        for (String name: asList(string, " ")) {
            final Platform platform = parsePlatform(name);
            
            if (!platforms.contains(platform)) {
                platforms.add(platform);
            }
        }
        
        return platforms;
    }
    
    public static Status parseStatus(
            final String string) throws ParseException {
        for (Status status: Status.values()) {
            if (status.getName().equals(string)) {
                return status;
            }
        }
        
        throw new ParseException(ResourceUtils.getString(StringUtils.class,
                StringUtils.ERROR_CANNOT_PARSE_STATUS, string));
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String BACK_SLASH =
            "\\"; // NOI18N
    public static final String FORWARD_SLASH =
            "/"; // NOI18N
    public static final String DOUBLE_BACK_SLASH =
            "\\\\"; // NOI18N
    
    public static final String ENCODING_UTF8 =
            "UTF-8"; // NOI18N
    
    public static final String CR = "\r"; // NOI18N
    public static final String LF = "\n"; // NOI18N
    public static final String DOT = "."; // NOI18N
    public static final String EMPTY_STRING = ""; // NOI18N
    public static final String CRLF = CR + LF;
    public static final String CRLFCRLF = CRLF + CRLF;
    public static final String SPACE = " "; // NOI18N
    public static final String QUOTE = "\""; // NOI18N
    public static final String EQUAL = "="; // NOI18N
    public static final String UNDERSCORE = "_"; // NOI18N
    
    public static final String NEW_LINE_PATTERN = "(?:\r\n|\n|\r)"; // NOI18N
    
    private static final String LEFT_WHITESPACE = "^\\s+"; // NOI18N
    private static final String RIGHT_WHITESPACE = "\\s+$"; // NOI18N
    
    
    private static final char MNEMONIC_CHAR = '&';
    private static final String MNEMONIC = "&"; // NOI18N
    private static final char NO_MNEMONIC = '\u0000';
    
    private static final char[] BASE64_TABLE = new char[] {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', '+', '/'
    };
    
    private static final byte[] BASE64_REVERSE_TABLE = new byte[] {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, 62, -1, -1, -1, 63, 52, 53,
        54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
        -1, -1, -1, -1, -1,  0,  1,  2,  3,  4,
        5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, -1, -1, -1, -1, -1, -1, 26, 27, 28,
        29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
        49, 50, 51
    };
    
    private static final char BASE64_PAD = '=';
    
    private static final int BIN_11111111 = 0xff;
    private static final int BIN_00110000 = 0x30;
    private static final int BIN_00111100 = 0x3c;
    private static final int BIN_00111111 = 0x3f;
    
    public static final String ERROR_CANNOT_PARSE_STATUS = 
            "StrU.error.cannot.parse.status";//NOI18N
    public static final String ERROR_UNKNOWN_PLATFORM = 
            "StrU.error.unknown.platform";//NOI18N
}
