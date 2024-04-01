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
package org.netbeans.modules.mercurial.ui.repository;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * We could have used URL with custom protocol handler for ssh 
 * (see http://java.sun.com/developer/onlineTraining/protocolhandlers/index.html)
 * but that is overkill for what we want which is a string to represent the URL.
 *
 * @author Padraig O'Briain
 * @author Marian Petras
 */
public final class HgURL {

    public enum Scheme {
        FILE("file", false),                                            //NOI18N
        HTTP("http", true),                                             //NOI18N
        HTTPS("https", true),                                           //NOI18N
        STATIC_HTTP("static-http", true),                               //NOI18N
        SSH("ssh", true),                                               //NOI18N
        SVN_HTTP("svn+http", true),                                     //NOI18N
        SVN_HTTPS("svn+https", true),                                   //NOI18N
        SVN_SSH("svn+ssh", true);                                       //NOI18N

        private static int longestSchemeName;

        private final String name;
        private final boolean supportsAuthentication;

        Scheme(String name, boolean supportsAuthentication) {
            this.name = name;
            this.supportsAuthentication = supportsAuthentication;
        };

        public static int getMaxSchemeNameLength() {
            if (longestSchemeName == 0) {
                for (Scheme scheme : values()) {
                    longestSchemeName = Math.max(scheme.name.length(),
                                                 longestSchemeName);
                }
            }
            return longestSchemeName;
        }

        /**
         * Returns the system name of this scheme, such as
         * {@code http}, or {@code file}.
         *
         * @return  system name of this scheme
         */
        @Override
        public String toString() {
            return name;
        }

        public boolean supportsAuthentication() {
            return supportsAuthentication;
        }

    }
    
    public static final HgURL NO_URL = new HgURL();

    private static final char SEGMENT_SEPARATOR = '/';
    private static final String PASSWORD_REPLACEMENT = "****";          //NOI18N
    private static final String hexadecimalChars = "0123456789abcdef";  //NOI18N

    private final Scheme scheme;
    private final String host;
    private final String username;
    private final char[] password;
    private final int    port;
    private final String rawPath;
    private final String rawQuery;
    private final String rawFragment;

    private final String path;

    private String hgCommandForm;
    private String publicForm;

    /**
     * Quick validation of an URL string. This validation is quick (such that it
     * can be called after each change of user's input (a character typed) but
     * not proper. It may report an invalid URL report as valid in some cases.
     * To proper validate a URL string, create an instance of {@code HgURL}.
     *
     * @param uriString
     * @see  #HgURL
     */
    public static String validateQuickly(String uriString) {
        if (uriString == null) {
            throw new IllegalArgumentException("URI string is <null>"); //NOI18N
        }

        String schemeName = getSchemeName(uriString);
        if (schemeName == null) {
            return invalidUrlMessage(uriString);
        }

        Scheme scheme = determineScheme(schemeName);
        if (scheme == null) {
            return invalidUrlMessage(uriString);
        }

        String afterScheme = uriString.substring(scheme.name().length());
        if (scheme == Scheme.FILE) {
            if (!afterScheme.startsWith(":/")) {                        //NOI18N
                return invalidUrlMessage(uriString);
            }
        } else {
            if (!afterScheme.startsWith("://")) {                       //NOI18N
                return invalidUrlMessage(uriString);
            }
            if (afterScheme.length() == "://".length()) {               //NOI18N
                return invalidUrlMessage(uriString);
            }
        }
        return null;
    }

    private static String invalidUrlMessage(String uriString) {
        return NbBundle.getMessage(HgURL.class, "MSG_INVALID_URL",      //NOI18N
                                    uriString);
    }

    private static String getSchemeName(String uriString) {
        return getSchemeName(uriString, Scheme.getMaxSchemeNameLength());
    }

    private static String getSchemeName(String uriString, int maxLength) {
        if (uriString.length() < 2) { //at least initial letter and colon needed
            return null;
        }

        final int maxSchemeLength = (maxLength != -1)
                                    ? Math.min(uriString.length() - 1, maxLength)
                                    :          uriString.length() - 1;

        StringBuilder buf = null;

        int c = uriString.charAt(0);
        int lowercase = c | 0x20;

        if (!isLowercaseAsciiAlpha(lowercase)) {
            return null;
        }

        if (c != lowercase) {
            buf = new StringBuilder(maxSchemeLength);
            buf.append(lowercase);
        }

        int firstInvalid = maxSchemeLength;
        for (int i = 1; i < maxSchemeLength; i++) {
            c = uriString.charAt(i);
            lowercase = c | 0x20;                              //lowercase

            if (!isLegalLowercaseSchemeLetter(lowercase)) {
                firstInvalid = i;
                break;
            }

            if ((c != lowercase) && (buf == null)) {
                buf = new StringBuilder(maxSchemeLength);
                appendLowercase(uriString, 1, i, buf);
            }
            if (buf != null) {
                buf.append(lowercase);
            }
        }

        if (uriString.charAt(firstInvalid) != ':') {
            return null;
        }

        return (buf == null) ? uriString.substring(0, firstInvalid)
                             : buf.toString();
    }

    private static boolean isLegalLowercaseSchemeLetter(int c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
               || (c == '+') || (c == '-') || (c == '.');
    }

    private static boolean isAsciiAlpha(char c) {
        return isLowercaseAsciiAlpha(c | 0x20);
    }

    private static boolean isLowercaseAsciiAlpha(int c) {
        return (c >= 'a') && (c <= 'z');
    }

    private static boolean isSlash(char c) {
        return (c == '/') || (c == '\\');
    }

    private static void appendLowercase(String s, int from, int to, StringBuilder buf) {
        for (int i = from; i < to; i++) {
            buf.append(s.charAt(i) | 0x20);
        }
    }

    private HgURL() {
        scheme = null;
        username = null;
        password = null;
        host = null;
        port = -1;
        path = null;
        rawPath = null;
        rawQuery = null;
        rawFragment = null;
    }

    public HgURL(File file) {
        scheme = Scheme.FILE;

        username = null;
        password = null;
        host = null;
        port = -1;
        path = file.toURI().getPath();
        rawPath = makeRawPathInfo(path);
        rawQuery = null;
        rawFragment = null;
    }

    public HgURL(String urlString) throws URISyntaxException {
        this(urlString, null, null);
    }

    /**
     *
     * @param urlString
     * @param username
     * @param password value is cloned, if you want to null the field, call {@link #clearPassword()}
     * @throws URISyntaxException
     */
    public HgURL(String urlString, String username, char[] password) throws URISyntaxException {
        URI originalUri;

        if (urlString == null) {
            throw new IllegalArgumentException("<null> URL string");    //NOI18N
        }

        if (urlString.length() == 0) {
            throw new IllegalArgumentException("empty URL string");     //NOI18N
        }

        if (looksLikePlainFilePath(urlString)) {
            originalUri = new File(urlString).toURI();
            scheme = Scheme.FILE;
        } else {
            originalUri = new URI(urlString).parseServerAuthority();
            String originalScheme = originalUri.getScheme();
            scheme = (originalScheme != null) ? determineScheme(originalScheme)
                                              : null;
        }

        if (scheme == null) {
            throw new URISyntaxException(
                    urlString,
                    NbBundle.getMessage(HgURL.class,
                                        "MSG_UNSUPPORTED_PROTOCOL",     //NOI18N
                                        originalUri.getScheme()));
        }

        verifyUserInfoData(scheme, username, password);

        if (username != null) {
            this.username = username;
            this.password = password == null ? null : (char[])password.clone();
        } else {
            String rawUserInfo = originalUri.getRawUserInfo();
            if (rawUserInfo == null) {
                this.username = null;
                this.password = null;
            } else {
                int colonIndex = rawUserInfo.indexOf(':');
                if (colonIndex == -1) {
                    this.username = rawUserInfo;
                    this.password = null;
                } else {
                    this.username = rawUserInfo.substring(0, colonIndex);
                    this.password = rawUserInfo.substring(colonIndex + 1).toCharArray();
                }
            }
        }

        host = originalUri.getHost();
        port = originalUri.getPort();
        rawPath     = originalUri.getRawPath();
        rawQuery    = originalUri.getRawQuery();
        rawFragment = originalUri.getRawFragment();

        path = originalUri.getPath();
    }

    /**
     * Detects with certain probability whether the given URL string is just
     * a plain path (i.e. without any scheme/protocol specification).
     *
     * @param  urlString  path/URL string to be checked
     * @return  {@code true} if the string seems to be a plain file path,
     *          {@code false} otherwise
     */
    public static boolean looksLikePlainFilePath(String urlString) {
        if (urlString == null) {
            throw new IllegalArgumentException("<null> URL string");    //NOI18N
        }

        if (urlString.length() == 0) {
            return false;
        }

        return (urlString.charAt(0) == '/')
               || Utilities.isWindows() && isWindowsAbsolutePath(urlString);
    }

    public static String stripUserInfo(String urlString) {
        if (urlString == null) {
            throw new IllegalArgumentException("<null> URL string");    //NOI18N
        }

        urlString = urlString.trim();

        if (urlString.length() == 0) {
            return urlString;
        }


        try {
            HgURL hgUrl = new HgURL(urlString);

            /* Keep the form of the Mercurial URL string:
             * If it was specified as plain file path, return plain file path.
             * If it was specified with schema/protocol, return URL string
             * with schema/protocol.
             */
            if (hgUrl.isFile() && looksLikePlainFilePath(urlString)) {
                return hgUrl.toHgCommandUrlStringWithoutUserInfo();
            } else {
                return hgUrl.toUrlStringWithoutUserInfo();
            }

        } catch (URISyntaxException ex) {    //not a valid URL
            return stripUserInfoFromInvalidURI(urlString);
        }
    }

    private static String stripUserInfoFromInvalidURI(String urlString) {
        if (looksLikePlainFilePath(urlString) && !urlString.startsWith("//")) { //NOI18N
            return urlString;
        }

        String schemeName = getSchemeName(urlString, -1);
        String schemeSpecific = (schemeName != null)
                                ? urlString.substring(schemeName.length() + 1).trim()
                                : urlString;

        /*
         * some users use the at-sign in their password, so we must look at
         * the last at-sign (separating user-info from server spec.),
         * not at the first one (which might just be part of the password)
         */
        int atIndex = schemeSpecific.lastIndexOf('@');

        if (atIndex == -1) {
            return urlString;
        } else {
            String schemeSpecNoAuth = schemeSpecific.substring(atIndex + 1);
            if (schemeName == null) {
                if (schemeSpecific.startsWith("//")) {                  //NOI18N
                    return "//" + schemeSpecNoAuth;                     //NOI18N
                } else {
                    return schemeSpecNoAuth;
                }
            } else {
                StringBuilder buf = new StringBuilder(urlString.length());
                buf.append(schemeName).append(':');
                if (schemeSpecific.startsWith("//")) {                  //NOI18N
                    buf.append("//");                                   //NOI18N
                }
                buf.append(schemeSpecNoAuth);
                return buf.toString();
            }
        }
    }

    private static String trimDupliciteInitialSlashes(String schemaSpecificUrlPart) {
        int length = schemaSpecificUrlPart.length();

        if (length == 0) {
            return schemaSpecificUrlPart;
        }

        String str = schemaSpecificUrlPart;

        /* find index of the first non-slash character: */
        int index = 0;
        while ((index < length) && (str.charAt(index) == '/')) {
            index++;
        }

        if ((index == 0) || (index == 1)) {
            /* no duplicite initial slahes */
            return schemaSpecificUrlPart;   //0 .. relative path, 1 .. absolute path
        }

        return schemaSpecificUrlPart.substring(index - 1);
    }

    private static Scheme determineScheme(String schemeString) {
        if (schemeString == null) {
            throw new IllegalArgumentException();
        }

        for (Scheme scheme : Scheme.values()) {
            if (scheme.name.equals(schemeString)) {
                return scheme;
            }
        }
        return null;
    }

    private static void verifyUserInfoData(Scheme scheme, String username, char[] password) {
        boolean authenticationSupported = scheme.supportsAuthentication();
        if (!authenticationSupported && ((username != null) || (password != null))) {
            throw new IllegalArgumentException(
                    "this URI does not support authentication, but username and/or password is specified"); //NOI18N
        }
        if ((username == null) && (password != null)) {
            throw new IllegalArgumentException(
                    "username is unset but password is set");           //NOI18N
        }
    }

    private static boolean isWindowsAbsolutePath(String urlString) {
        final int length = urlString.length();

        if (length == 0) {
            return false;
        }

        int index = 0;

        if (isSlash(urlString.charAt(index))) {
            index++;
        }

        if ((length <= index) || !isAsciiAlpha(urlString.charAt(index++))) {
            return false;
        }

        if ((length <= index) || (urlString.charAt(index++) != ':')) {
            return false;
        }

        if ((length <= index) || !isSlash(urlString.charAt(index++))) {
            return false;
        }

        return true;
    }

    public boolean isFile() {
        return scheme == Scheme.FILE;
    }

    /**
     * get the protocol
     * @return either http, https, file, static-http, ssh
     */ 
    public String getProtocol() {
        return scheme.name();
    }

    public Scheme getScheme() {
        return scheme;
    }

    public String getPath() {
        return path;
    }

    public String getFilePath() {
        if (Utilities.isWindows() && isWindowsAbsolutePath(path)) {
            return getWindowsFilePath(path);
        } else {
            return path;
        }
    }

    private static String getWindowsFilePath(String path) {
        if (isSlash(path.charAt(0))) {
            path = path.substring(1);
        }

        return path.replace('/', '\\');
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    public void clearPassword () {
        if (password != null) {
            Arrays.fill(password, '\0');
            this.hgCommandForm = null;
        }
    }

    public String getUserInfo() {
        return getUserInfo(true);
    }

    String getUserInfo(boolean maskedPassword) {
        if (username == null) {
            return null;
        }

        if (password == null) {
            return username;
        }

        return maskedPassword
               ? username + ':' + PASSWORD_REPLACEMENT
               : username + ':' + password;
    }

    public boolean supportsAuthentication() {
        return scheme.supportsAuthentication();
    }

    public static File getFile(HgURL url) {
        if (!url.isFile()) {
            throw new IllegalArgumentException(
                    "The passed HgURL must represent a file.");         //NOI18N
        }
        return new File(url.getPath());
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        assert getClass() == HgURL.class;
        if (otherObj.getClass() != HgURL.class) {
            return false;
        }
        return ((HgURL) otherObj).getUniqueIdentifier().equals(getUniqueIdentifier());
    }

    @Override
    public int hashCode() {
        return getUniqueIdentifier().hashCode();
    }

    private Object getUniqueIdentifier() {
        return toHgCommandUrlString();
    }

    @Override
    public String toString() {
        if (publicForm == null) {
            publicForm = toUrlString(false, true, true);
        }
        return publicForm;
    }

    public String toHgCommandUrlString() {
        if (hgCommandForm == null) {
            // Workaround for http://www.selenic.com/mercurial/bts/issue776
            // Do not use file:/ or file:/// in local file URIs
            hgCommandForm = isFile() ? getFilePath()
                                     : toUrlString();
        }
        return hgCommandForm;
    }

    public String toHgCommandUrlStringWithoutUserInfo() {
        return isFile() ? getFilePath()
                        : toUrlStringWithoutUserInfo();
    }

    public String toHgCommandStringWithMaskedPassword() {
        return isFile() ? getFilePath()
                        : toUrlString(false, true, true);
    }

    public String toHgCommandStringWithNoPassword () {
        return isFile() ? getFilePath()
                        : toUrlString(false, false, false);
    }

    public URL toURL() {
        String urlSpec = toCompleteUrlString();
        try {
            return new URL(urlSpec);
        } catch (MalformedURLException ex) {
            assert false : ex.getMessage();
            return null;
        }
    }

    public String toCompleteUrlString() {
        return toUrlString();
    }

    public String toUrlString() {
        return toUrlString(false, true, false);
    }

    public String toUrlStringWithoutUserInfo() {
        return toUrlString(true, true, true);
    }

    private String toUrlString(boolean stripUserinfo, boolean displayPasswordSection, boolean maskPassword) {
        if (this == NO_URL) {
            return "";                                                  //NOI18N
        }

        boolean authorityPartSeparationPending;

        StringBuilder buf = new StringBuilder(128);
        buf.append(scheme.name).append(':');
        if (scheme != Scheme.FILE) {
            authorityPartSeparationPending = addAuthoritySpec(stripUserinfo, displayPasswordSection, maskPassword, buf);
        } else {
            authorityPartSeparationPending = false;
        }
        if ((rawPath != null) && (rawPath.length() != 0)) {
            if (authorityPartSeparationPending && (rawPath.charAt(0) != '/')) {
                buf.append('/');
            }
            buf.append(rawPath);
            authorityPartSeparationPending = false;
        }
        if (rawQuery != null) {
            buf.append('?').append(rawQuery);
        }
        if (rawFragment != null) {
            buf.append('#').append(rawFragment);
        }

        return buf.toString();
    }
    
    public boolean addAuthoritySpec(boolean stripUserInfo, boolean displayPasswordSection, boolean maskPassword, StringBuilder buf) {
        if (host == null) {
            return false;
        }

        buf.append("//");                                               //NOI18N
        if (!stripUserInfo && (username != null)) {
            buf.append(makeRawUserInfo(username));
            if (password != null && displayPasswordSection) {
                buf.append(':');
                if (maskPassword) {
                    buf.append(PASSWORD_REPLACEMENT);
                } else {
                    buf.append(makeRawUserInfo(new String(password)));
                }
            }
            buf.append('@');
        }
        buf.append(host);
        if (port != -1) {
            buf.append(':').append(port);
        }
        return true;
    }

    private static String makeRawUserInfo(String userInfo) {
        StringBuilder buf = new StringBuilder(userInfo.length() + 10);

        final int length = userInfo.length();
        for (int i = 0; i < length; i++) {
            char c = userInfo.charAt(i);
            if (isLegalUserInfoChar(c)) {
                buf.append(c);
            } else {
                appendEncoded(c, buf);
            }
        }
        return buf.toString();
    }

    private static String makeRawPathInfo(String path) {
        final int length = path.length();

        if (length == 0) {
            return path;                      //simple case - empty path
        }

        int i;
        for (i = 0; i < length; i++) {
            char c = path.charAt(i);
            if (!isLegalPathChar(c)) {
                break;
            }
        }

        if (i == length) {
            return path;                      //simple case - no encoding needed
        }

        StringBuilder buf = new StringBuilder(path.length() + 20);
        if (i != 0) {
            buf.append(path.substring(0, i));
        }

        /* encode the first illegal character: */
        assert !isLegalPathChar(path.charAt(i));
        appendEncoded(path.charAt(i++), buf);
        for (; i < length; i++) {
            char c = path.charAt(i);
            if (isLegalPathChar(c)) {
                buf.append(c);
            } else {
                appendEncoded(c, buf);
            }
        }
        return buf.toString();
    }

    private static boolean isLegalUserInfoChar(char c) {
        return isAlnumChar(c) || ("-_.!~*'();&=+$,%".indexOf(c) != -1);  //NOI18N
    }

    private static boolean isLegalPathChar(char c) {
        return isAlnumChar(c) || ("/-_.!~*'():@&=+$,%".indexOf(c) != -1);//NOI18N
    }

    private static boolean isAlnumChar(char c) {
        return ((c >= 'a') && (c <= 'z'))
               || ((c >= 'A') && (c <= 'Z'))
               || ((c >= '0') && (c <= '9'));
    }

    private static void appendEncoded(int c, StringBuilder buf) {

        /*
         * Encode by UTF-8 encoding to one, two, three or four bytes, then
         * encode each byte using the common URI syntax (e.g. "%20"):
         */

        if (c < 0x0080) {                                      // 1 byte (ASCII)
            appendEncodedByte(         c & 0x00007f,          buf);

        } else if (c < 0x0800) {                               // 2 bytes
            appendEncodedByte(0xc0 | ((c & 0x0007c0) >>>  6), buf);
            appendEncodedByte(0x80 |  (c & 0x00003f)        , buf);

        } else if (c < 0x10000) {                              // 3 bytes
            appendEncodedByte(0xe0 | ((c & 0x00f000) >>> 12), buf);
            appendEncodedByte(0x80 | ((c & 0x000fc0) >>>  6), buf);
            appendEncodedByte(0x80 |  (c & 0x00003f)        , buf);

        } else {                                               // 4 bytes
            appendEncodedByte(0xf0 | ((c & 0x1c0000) >>> 18), buf);
            appendEncodedByte(0x80 | ((c & 0x03f000) >>> 12), buf);
            appendEncodedByte(0x80 | ((c & 0x000fc0) >>>  6), buf);
            appendEncodedByte(0x80 |  (c & 0x00003f)        , buf);

        }
    }

    private static void appendEncodedByte(int c, StringBuilder buf) {
        assert c < 0x100;

        buf.append('%');
        buf.append(hexadecimalChars.charAt((c & 0xf0) >>> 4));
        buf.append(hexadecimalChars.charAt( c & 0x0f)       );
    }

}
