/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

// @formatter:off
/**
 * Source objects track the origin of JavaScript entities.
 */
public final class Source {
    private static final int BUF_SIZE = 8 * 1024;

    /**
     * Descriptive name of the source as supplied by the user. Used for error
     * reporting to the user. For example, SyntaxError will use this to print message.
     * Used to implement __FILE__. Also used for SourceFile in .class for debugger usage.
     */
    private final String name;

    /**
     * Base directory the File or base part of the URL. Used to implement __DIR__.
     * Used to load scripts relative to the 'directory' or 'base' URL of current script.
     * This will be null when it can't be computed.
     */
    private final String base;

    /** Source content */
    private final Data data;

    /** Cached hash code */
    private int hash;

    /** Base64-encoded SHA1 digest of this source object */
    private volatile byte[] digest;

    /** source URL set via //@ sourceURL or //# sourceURL directive */
    private String explicitURL;

    // Do *not* make this public, ever! Trusts the URL and content.
    private Source(final String name, final String base, final Data data) {
        this.name = name;
        this.base = base;
        this.data = data;
    }

    private static Source sourceFor(final String name, final String base, final URLData data) throws IOException {
        try {
            final Source newSource = new Source(name, base, data);

            // All sources in cache must be fully loaded
            data.load();

            return newSource;
        } catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw e;
        }
    }

    // Wrapper to manage lazy loading
    private interface Data {

        URL url();

        int length();

        long lastModified();

        String data();

        boolean isEvalCode();
    }

    private static final class RawData implements Data {
        private final String source;
        private final boolean evalCode;
        private int hash;

        private RawData(final String source, final boolean evalCode) {
            this.source = Objects.requireNonNull(source);
            this.evalCode = evalCode;
        }

        private RawData(final Reader reader) throws IOException {
            this(readFully(reader), false);
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                h = hash = source.hashCode() ^ (evalCode ? 1 : 0);
            }
            return h;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof RawData) {
                final RawData other = (RawData)obj;
                return source.equals(other.source) && evalCode == other.evalCode;
            }
            return false;
        }

        @Override
        public String toString() {
            return data();
        }

        @Override
        public URL url() {
            return null;
        }

        @Override
        public int length() {
            return source.length();
        }

        @Override
        public long lastModified() {
            return 0;
        }

        public String data() {
            return source;
        }

        @Override
        public boolean isEvalCode() {
            return evalCode;
        }
    }

    private static class URLData implements Data {
        private final URL url;
        protected final Charset cs;
        private int hash;
        protected String source;
        protected int length;
        protected long lastModified;

        private URLData(final URL url, final Charset cs) {
            this.url = Objects.requireNonNull(url);
            this.cs = cs;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                h = hash = url.hashCode();
            }
            return h;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof URLData)) {
                return false;
            }

            final URLData otherData = (URLData) other;

            if (url.equals(otherData.url)) {
                // Make sure both have meta data loaded
                try {
                    if (isDeferred()) {
                        // Data in cache is always loaded, and we only compare to cached data.
                        assert !otherData.isDeferred();
                        loadMeta();
                    } else if (otherData.isDeferred()) {
                        otherData.loadMeta();
                    }
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }

                // Compare meta data
                return this.length == otherData.length && this.lastModified == otherData.lastModified;
            }
            return false;
        }

        @Override
        public String toString() {
            return data();
        }

        @Override
        public URL url() {
            return url;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public long lastModified() {
            return lastModified;
        }

        public String data() {
            assert !isDeferred();
            return source;
        }

        @Override
        public boolean isEvalCode() {
            return false;
        }

        boolean isDeferred() {
            return source == null;
        }

        protected void load() throws IOException {
            if (source == null) {
                final URLConnection c = url.openConnection();
                try (InputStream in = c.getInputStream()) {
                    source = cs == null ? readFully(in) : readFully(in, cs);
                    length = source.length();
                    lastModified = c.getLastModified();
                }
            }
        }

        protected void loadMeta() throws IOException {
            if (length == 0 && lastModified == 0) {
                final URLConnection c = url.openConnection();
                length = c.getContentLength();
                lastModified = c.getLastModified();
            }
        }
    }

    private static class FileData extends URLData {
        private final File file;

        private FileData(final File file, final Charset cs) {
            super(getURLFromFile(file), cs);
            this.file = file;

        }

        @Override
        protected void loadMeta() {
            if (length == 0 && lastModified == 0) {
                length = (int) file.length();
                lastModified = file.lastModified();
            }
        }

        @Override
        protected void load() throws IOException {
            if (source == null) {
                source = cs == null ? readFully(file) : readFully(file, cs);
                length = source.length();
                lastModified = file.lastModified();
            }
        }
    }

    private String data() {
        return data.data();
    }

    /**
     * Returns a Source instance
     *
     * @param name    source name
     * @param content contents as string
     * @param isEval does this represent code from 'eval' call?
     * @return source instance
     */
    public static Source sourceFor(final String name, final String content, final boolean isEval) {
        return new Source(name, baseName(name), new RawData(content, isEval));
    }

    /**
     * Returns a Source instance
     *
     * @param name    source name
     * @param content contents as string
     * @return source instance
     */
    public static Source sourceFor(final String name, final String content) {
        return sourceFor(name, content, false);
    }

    /**
     * Returns a Source instance
     *
     * @param name  source name
     * @param url   url from which source can be loaded
     *
     * @return source instance
     *
     * @throws IOException if source cannot be loaded
     */
    public static Source sourceFor(final String name, final URL url) throws IOException {
        return sourceFor(name, url, null);
    }

    /**
     * Returns a Source instance
     *
     * @param name  source name
     * @param url   url from which source can be loaded
     * @param cs    Charset used to convert bytes to chars
     *
     * @return source instance
     *
     * @throws IOException if source cannot be loaded
     */
    public static Source sourceFor(final String name, final URL url, final Charset cs) throws IOException {
        return sourceFor(name, baseURL(url), new URLData(url, cs));
    }

    /**
     * Returns a Source instance
     *
     * @param name  source name
     * @param file  file from which source can be loaded
     *
     * @return source instance
     *
     * @throws IOException if source cannot be loaded
     */
    public static Source sourceFor(final String name, final File file) throws IOException {
        return sourceFor(name, file, null);
    }

    /**
     * Returns a Source instance
     *
     * @param name  source name
     * @param path  path from which source can be loaded
     *
     * @return source instance
     *
     * @throws IOException if source cannot be loaded
     */
    public static Source sourceFor(final String name, final Path path) throws IOException {
        File file = null;
        try {
            file = path.toFile();
        } catch (final UnsupportedOperationException uoe) {
        }

        if (file != null) {
            return sourceFor(name, file);
        } else {
            return sourceFor(name, Files.newBufferedReader(path));
        }
    }

    /**
     * Returns a Source instance
     *
     * @param name  source name
     * @param file  file from which source can be loaded
     * @param cs    Charset used to convert bytes to chars
     *
     * @return source instance
     *
     * @throws IOException if source cannot be loaded
     */
    public static Source sourceFor(final String name, final File file, final Charset cs) throws IOException {
        final File absFile = file.getAbsoluteFile();
        return sourceFor(name, dirName(absFile, null), new FileData(file, cs));
    }

    /**
     * Returns a Source instance.
     *
     * @param name source name
     * @param reader reader from which source can be loaded
     *
     * @return source instance
     *
     * @throws IOException if source cannot be loaded
     */
    public static Source sourceFor(final String name, final Reader reader) throws IOException {
        return new Source(name, baseName(name), new RawData(reader));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Source)) {
            return false;
        }
        final Source other = (Source) obj;
        return Objects.equals(name, other.name) && data.equals(other.data);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            h = hash = data.hashCode() ^ Objects.hashCode(name);
        }
        return h;
    }

    /**
     * Fetch source content.
     * @return Source content.
     */
    public String getString() {
        return data.toString();
    }

    /**
     * Get the user supplied name of this script.
     * @return User supplied source name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the last modified time of this script.
     * @return Last modified time.
     */
    public long getLastModified() {
        return data.lastModified();
    }

    /**
     * Get the "directory" part of the file or "base" of the URL.
     * @return base of file or URL.
     */
    public String getBase() {
        return base;
    }

    /**
     * Fetch a portion of source content.
     * @param start start index in source
     * @param len length of portion
     * @return Source content portion.
     */
    public String getString(final int start, final int len) {
        return data().substring(start, start + len);
    }

    /**
     * Fetch a portion of source content associated with a token.
     * @param token Token descriptor.
     * @return Source content portion.
     */
    public String getString(final long token) {
        final int start = Token.descPosition(token);
        final int len = Token.descLength(token);
        return getString(start, len);
    }

    /**
     * Returns the source URL of this script Source. Can be null if Source
     * was created from a String or a char[].
     *
     * @return URL source or null
     */
    public URL getURL() {
        return data.url();
    }

    /**
     * Get explicit source URL.
     * @return URL set via sourceURL directive
     */
    public String getExplicitURL() {
        return explicitURL;
    }

    /**
     * Set explicit source URL.
     * @param explicitURL URL set via sourceURL directive
     */
    public void setExplicitURL(String explicitURL) {
        this.explicitURL = explicitURL;
    }

    /**
     * Returns whether this source was submitted via 'eval' call or not.
     *
     * @return true if this source represents code submitted via 'eval'
     */
    public boolean isEvalCode() {
        return data.isEvalCode();
    }

    /**
     * Find the beginning of the line containing position.
     * @param position Index to offending token.
     * @return Index of first character of line.
     */
    private int findBOLN(final int position) {
        final String d = data();
        for (int i = position - 1; i > 0; i--) {
            final char ch = d.charAt(i);

            if (ch == '\n' || ch == '\r') {
                return i + 1;
            }
        }

        return 0;
    }

    /**
     * Find the end of the line containing position.
     * @param position Index to offending token.
     * @return Index of last character of line.
     */
    private int findEOLN(final int position) {
        final String d = data();
        final int length = d.length();
        for (int i = position; i < length; i++) {
            final char ch = d.charAt(i);

            if (ch == '\n' || ch == '\r') {
                return i - 1;
            }
        }

        return length - 1;
    }

    /**
     * Return line number of character position.
     *
     * <p>This method can be expensive for large sources as it iterates through
     * all characters up to {@code position}.</p>
     *
     * @param position Position of character in source content.
     * @return Line number.
     */
    public int getLine(final int position) {
        final String d = data();
        // Line count starts at 1.
        int line = 1;

        for (int i = 0; i < position; i++) {
            final char ch = d.charAt(i);
            // Works for both \n and \r\n.
            if (ch == '\n') {
                line++;
            }
        }

        return line;
    }

    /**
     * Return column number of character position.
     * @param position Position of character in source content.
     * @return Column number.
     */
    public int getColumn(final int position) {
        // TODO - column needs to account for tabs.
        return position - findBOLN(position);
    }

    /**
     * Return line text including character position.
     * @param position Position of character in source content.
     * @return Line text.
     */
    public String getSourceLine(final int position) {
        // Find end of previous line.
        final int first = findBOLN(position);
        // Find end of this line.
        final int last = findEOLN(position);

        return data().substring(first, last + 1);
    }

    /**
     * Get the content of this source as a {@link String}.
     */
    public String getContent() {
        return data();
    }

    /**
     * Get the length in chars for this source
     * @return length
     */
    public int getLength() {
        return data.length();
    }

    /**
     * Read all of the source until end of file.
     *
     * @param reader reader opened to source stream
     * @return source as content
     * @throws IOException if source could not be read
     */
    public static String readFully(final Reader reader) throws IOException {
        final char[]        arr = new char[BUF_SIZE];
        final StringBuilder sb  = new StringBuilder();

        try {
            int numChars;
            while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
                sb.append(arr, 0, numChars);
            }
        } finally {
            reader.close();
        }

        return sb.toString();
    }

    /**
     * Read all of the source until end of file.
     *
     * @param file source file
     * @return source as content
     * @throws IOException if source could not be read
     */
    public static String readFully(final File file) throws IOException {
        if (!file.isFile()) {
            throw new IOException(file + " is not a file");
        }
        return byteArrayToString(Files.readAllBytes(file.toPath()));
    }

    /**
     * Read all of the source until end of file.
     *
     * @param file source file
     * @param cs Charset used to convert bytes to chars
     * @return source as content
     * @throws IOException if source could not be read
     */
    public static String readFully(final File file, final Charset cs) throws IOException {
        if (!file.isFile()) {
            throw new IOException(file + " is not a file");
        }

        final byte[] buf = Files.readAllBytes(file.toPath());
        return (cs != null) ? new String(buf, cs) : byteArrayToString(buf);
    }

    /**
     * Read all of the source until end of stream from the given URL.
     *
     * @param url URL to read content from
     * @return source as content
     * @throws IOException if source could not be read
     */
    public static String readFully(final URL url) throws IOException {
        return readFully(url.openStream());
    }

    /**
     * Read all of the source until end of file.
     *
     * @param url URL to read content from
     * @param cs Charset used to convert bytes to chars
     * @return source as content
     * @throws IOException if source could not be read
     */
    public static String readFully(final URL url, final Charset cs) throws IOException {
        return readFully(url.openStream(), cs);
    }

    /**
     * Get a Base64-encoded SHA1 digest for this source.
     *
     * @return a Base64-encoded SHA1 digest for this source
     */
    public String getDigest() {
        return new String(getDigestBytes(), StandardCharsets.US_ASCII);
    }

    private byte[] getDigestBytes() {
        byte[] ldigest = digest;
        if (ldigest == null) {
            final String content = data();
            final byte[] bytes = new byte[content.length() * 2];

            for (int i = 0; i < content.length(); i++) {
                bytes[i * 2]     = (byte)  (content.charAt(i) & 0x00ff);
                bytes[i * 2 + 1] = (byte) ((content.charAt(i) & 0xff00) >> 8);
            }

            try {
                final MessageDigest md = MessageDigest.getInstance("SHA-1");
                if (name != null) {
                    md.update(name.getBytes(StandardCharsets.UTF_8));
                }
                if (base != null) {
                    md.update(base.getBytes(StandardCharsets.UTF_8));
                }
                if (getURL() != null) {
                    md.update(getURL().toString().getBytes(StandardCharsets.UTF_8));
                }
                // Message digest to file name encoder
                Base64.Encoder base64 = Base64.getUrlEncoder().withoutPadding();
                digest = ldigest = base64.encode(md.digest(bytes));
            } catch (final NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return ldigest;
    }

    /**
     * Get the base url. This is currently used for testing only
     * @param url a URL
     * @return base URL for url
     */
    public static String baseURL(final URL url) {
        if (url.getProtocol().equals("file")) {
            try {
                final Path path = Paths.get(url.toURI());
                final Path parent = path.getParent();
                return (parent != null) ? (parent + File.separator) : null;
            } catch (final SecurityException | URISyntaxException | IOError e) {
                return null;
            }
        }

        // FIXME: is there a better way to find 'base' URL of a given URL?
        String path = url.getPath();
        if (path.isEmpty()) {
            return null;
        }
        path = path.substring(0, path.lastIndexOf('/') + 1);
        final int port = url.getPort();
        try {
            return new URL(url.getProtocol(), url.getHost(), port, path).toString();
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    private static String dirName(final File file, final String defaultBaseName) {
        final String res = file.getParent();
        return (res != null) ? (res + File.separator) : defaultBaseName;
    }

    // fake directory like name
    private static String baseName(final String name) {
        int idx = name.lastIndexOf('/');
        if (idx == -1) {
            idx = name.lastIndexOf('\\');
        }
        return (idx != -1) ? name.substring(0, idx + 1) : null;
    }

    public static String readFully(final InputStream is, final Charset cs) throws IOException {
        return (cs != null) ? new String(readBytes(is), cs) : readFully(is);
    }

    public static String readFully(final InputStream is) throws IOException {
        return byteArrayToString(readBytes(is));
    }

    private static String byteArrayToString(final byte[] bytes) {
        Charset cs = StandardCharsets.UTF_8;
        int start = 0;
        // BOM detection.
        if (bytes.length > 1 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
            start = 2;
            cs = StandardCharsets.UTF_16BE;
        } else if (bytes.length > 1 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
            if (bytes.length > 3 && bytes[2] == 0 && bytes[3] == 0) {
                start = 4;
                cs = Charset.forName("UTF-32LE");
            } else {
                start = 2;
                cs = StandardCharsets.UTF_16LE;
            }
        } else if (bytes.length > 2 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            start = 3;
            cs = StandardCharsets.UTF_8;
        } else if (bytes.length > 3 && bytes[0] == 0 && bytes[1] == 0 && bytes[2] == (byte) 0xFE && bytes[3] == (byte) 0xFF) {
            start = 4;
            cs = Charset.forName("UTF-32BE");
        }

        return new String(bytes, start, bytes.length - start, cs);
    }

    static byte[] readBytes(final InputStream is) throws IOException {
        final byte[] arr = new byte[BUF_SIZE];
        try {
            try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                int numBytes;
                while ((numBytes = is.read(arr, 0, arr.length)) > 0) {
                    buf.write(arr, 0, numBytes);
                }
                return buf.toByteArray();
            }
        } finally {
            is.close();
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    private static URL getURLFromFile(final File file) {
        try {
            return file.toURI().toURL();
        } catch (final SecurityException | MalformedURLException ignored) {
            return null;
        }
    }
}
