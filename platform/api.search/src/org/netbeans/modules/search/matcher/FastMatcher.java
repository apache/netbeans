/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.search.matcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.Constants;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.TextDetail;
import org.netbeans.modules.search.TextRegexpUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;


/**
 * Fast Matcher - use file mapped memory.
 *
 * Matches only small files, large files are matched by default matcher.
 *
 * @author jhavlin
 */
public class FastMatcher extends AbstractMatcher {

    private static final int SIZE_LIMIT = 1 << 20; // 1 MiB
    private static final int LINE_LIMIT = 1 << 12; // 4 KiB
    private static final Logger LOG = Logger.getLogger(
            FastMatcher.class.getName());
    private SearchPattern searchPattern;
    private Pattern pattern;
    private DefaultMatcher defaultMatcher = null;
    private static final Pattern linePattern =
            Pattern.compile("(.*)(\\r\\n|\\n|\\r)");                    //NOI18N
    private boolean trivial;
    private boolean asciiPattern;
    private long totalTime;
    private int fileMatches = 0;
    private int itemMatches = 0;
    private boolean multiline = false;

    public FastMatcher(SearchPattern searchPattern) {
        this.trivial = searchPattern.getSearchExpression() == null
                || searchPattern.getSearchExpression().isEmpty();
        if (!trivial) {
            this.searchPattern = searchPattern;
            String expr = searchPattern.getSearchExpression();
            this.pattern = TextRegexpUtil.makeTextPattern(searchPattern);
            this.multiline = TextRegexpUtil.isMultilineOrMatchesMultiline(expr);
            asciiPattern = expr.matches("\\p{ASCII}+") //NOI18N
                    && !expr.contains(".") //NOI18N
                    && !expr.matches(".*\\\\[0xXuU].*");                //NOI18N
            if (asciiPattern) {
                LOG.info("Using ASCII pattern");                        //NOI18N
            }
        }
    }

    @Override
    public Def checkMeasuredInternal(FileObject file,
            SearchListener listener) {

        if (trivial) {
            return new Def(file, null, null);
        }

        listener.fileContentMatchingStarted(file.getPath());

        long start = System.currentTimeMillis();
        Def def;
        File f = FileUtil.toFile(file);
        if (file.getSize() > SIZE_LIMIT || f == null) {
//            LOG.log(Level.INFO,
//                    "Falling back to default matcher: {0}, {1} kB", //NOI18N
//                    new Object[]{file.getPath(), file.getSize() / 1024});
//            def = getDefaultMatcher().check(file, listener);
            def = checkBig(file, f, listener);
        } else {
            def = checkSmall(file, f, listener);
        }
        totalTime += System.currentTimeMillis() - start;
        return def;
    }

    @Override
    public void terminate() {
        if (defaultMatcher != null) {
            defaultMatcher.terminate();
        }
    }

    /**
     * Lazy initialization of fallback default matcher (for files that are too
     * big for file-mapped-memory).
     */
    private DefaultMatcher getDefaultMatcher() {
        if (defaultMatcher == null) {
            defaultMatcher = new DefaultMatcher(searchPattern);
        }
        return defaultMatcher;
    }

    /**
     * Perform pattern matching inside the whole file.
     *
     * @param cb Character buffer.
     * @param fo File object.
     */
    private List<TextDetail> matchWholeFile(CharSequence cb, FileObject fo)
            throws DataObjectNotFoundException {

        Matcher textMatcher = pattern.matcher(cb);
        DataObject dataObject = null;
        LineInfoHelper lineInfoHelper = new LineInfoHelper(cb);

        List<TextDetail> textDetails = null;

        while (textMatcher.find()) {
            if (textDetails == null) {
                textDetails = new LinkedList<>();
                dataObject = DataObject.find(fo);
                fileMatches++;
            }
            itemMatches++;
            TextDetail ntd = new TextDetail(dataObject, searchPattern);
            lineInfoHelper.findAndSetPositionInfo(ntd, textMatcher.start(),
                    textMatcher.end(), textMatcher.group());
            textDetails.add(ntd);
            if (fileMatches >= Constants.COUNT_LIMIT
                    || itemMatches
                    >= Constants.DETAILS_COUNT_LIMIT) {
                break;
            }
        }
        return textDetails;
    }

    /**
     * Perform pattern matching in individual lines of the file.
     */
    private List<TextDetail> matchLines(CharSequence cs, FileObject fo)
            throws DataObjectNotFoundException, IOException {

        List<TextDetail> dets = null;
        DataObject dataObject = null;
        int count = 0;
        int limit = Constants.DETAILS_COUNT_LIMIT;
        boolean canRun = true;

        LineReader nelr = new LineReader(cs);
        LineInfo line;
        while ((line = nelr.readNext()) != null && canRun
                && count < limit) {
            Matcher m = pattern.matcher(line.getString());
            while (m.find() && canRun) {
                if (dets == null) {
                    dets = new ArrayList<>();
                    dataObject = DataObject.find(fo);

                }
                TextDetail det = createLineMatchTextDetail(dataObject,
                        line.getNumber(), m, line.getString(), line.start);
                dets.add(det);
                count++;
            }
            if ((line.getNumber() % 50) == 0) {
                synchronized (this) {
                    // TODO terminate
                    canRun = true;
                }
            }
        }
        return dets;
    }
//        Matcher lineMatcher = linePattern.matcher(cs);
//        int lineNumber = 0;
//        int lineStart;
//        int lineEnd = 0;
//        List<TextDetail> textDetails = null;
//        DataObject dataObject = null;
//        while (lineMatcher.find()) {
//            lineNumber++;
//            lineStart = lineMatcher.start();
//            lineEnd = lineMatcher.end();
//            String text = lineMatcher.group(1);
//            Matcher textMatcher = pattern.matcher(text);
//            while (textMatcher.find()) {
//                if (textDetails == null) {
//                    textDetails = new LinkedList<TextDetail>();
//                    dataObject = DataObject.find(fo);
//                }
//                TextDetail textDetail = createLineMatchTextDetail(dataObject,
//                        lineNumber, textMatcher, text, lineStart);
//                textDetails.add(textDetail);
//            }
//        }
//        lineNumber++;
//        String text = cs.subSequence(lineEnd, cs.length()).toString();
//        lineStart = lineEnd;
//        Matcher textMatcher = pattern.matcher(text);
//        while (textMatcher.find()) {
//            if (textDetails == null) {
//                textDetails = new LinkedList<TextDetail>();
//                dataObject = DataObject.find(fo);
//            }
//            TextDetail textDetail = createLineMatchTextDetail(dataObject,
//                    lineNumber, textMatcher, text, lineStart);
//            textDetails.add(textDetail);
//        }
//        return textDetails;

    /**
     * Create a TextDetail instance for match found in matchLines.
     */
    private TextDetail createLineMatchTextDetail(DataObject dataObject,
            int lineNumber, Matcher textMatcher, String text, int lineStart) {
        TextDetail textDetail = new TextDetail(dataObject,
                searchPattern);
        textDetail.setLine(lineNumber);
        textDetail.setColumn(textMatcher.start() + 1);
        textDetail.setMatchedText(textMatcher.group());
        textDetail.setStartOffset(lineStart + textMatcher.start());
        textDetail.setEndOffset(lineStart + textMatcher.end());
        textDetail.setMarkLength(textMatcher.end()
                - textMatcher.start());
        textDetail.setLineText(text);
        return textDetail;
    }

    /**
     * Check file content using Java NIO API.
     */
    private Def checkSmall(FileObject fo, File file,
            SearchListener listener) {

        MappedByteBuffer bb = null;
        FileChannel fc = null;
        try {
            // Open the file and then get a channel from the stream
            FileInputStream fis = new FileInputStream(file);
            fc = fis.getChannel();

            // Get the file's size and then map it into memory
            int sz = (int) fc.size();
            bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

            if (asciiPattern && !matchesIgnoringEncoding(bb)) {
                return null;
            }
            // Decode the file into a char buffer
            Charset charset = FileEncodingQuery.getEncoding(fo);
            CharsetDecoder decoder = prepareDecoder(charset);
            decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            CharBuffer cb = decoder.decode(bb);

            List<TextDetail> textDetails = multiline
                    ? matchWholeFile(cb, fo)
                    : matchLines(cb, fo);
            if (textDetails == null) {
                return null;
            } else {
                Def def = new Def(fo, decoder.charset(), textDetails);
                return def;
            }
        } catch (Exception e) {
            listener.generalError(e);
            return null;
        } finally {
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException ex) {
                    listener.generalError(ex);
                }
            }
            unmap(bb);
        }
    }

    private Def checkBig(FileObject fileObject, File file,
            SearchListener listener) {

        Charset charset = FileEncodingQuery.getEncoding(fileObject);
        CharsetDecoder decoder = prepareDecoder(charset);

        LongCharSequence longSequence = null;
        try {
            longSequence = new LongCharSequence(file, charset);
            List<TextDetail> textDetails = multiline
                    ? matchWholeFile(longSequence, fileObject)
                    : matchLines(longSequence, fileObject);
            if (textDetails == null) {
                return null;
            } else {
                Def def = new Def(fileObject, decoder.charset(), textDetails);
                return def;
            }
        } catch (Exception ex) {
            listener.generalError(ex);
            return null;
        } finally {
            if (longSequence != null) {
                longSequence.close();
            }
        }
    }

    /**
     * Match content of byte buffer, without character encoding. Can be used if
     * the pattern contains only ASCII characters.
     *
     * @param byteBuffer Byte buffer with file contents.
     */
    private boolean matchesIgnoringEncoding(ByteBuffer byteBuffer) {
        Matcher m = pattern.matcher(new FastCharSequence(byteBuffer, 0));
        boolean found = m.find();
        return found;
    }

    /**
     * Unmap mapped buffer.
     */
    private void unmap(MappedByteBuffer buffer) {
        try {
            Method getCleanerMethod = buffer.getClass().getMethod(
                    "cleaner");                                         //NOI18N
            getCleanerMethod.setAccessible(true);
            // sun.misc.Cleaner
            Object cleaner = getCleanerMethod.invoke(buffer);
            cleaner.getClass().getMethod("clean").invoke(cleaner);
        } catch (Exception e) {
        }
    }

    /*
     * Character sequence that gets bytes from a byte buffer and casts them to
     * characters - without any encoding.
     */
    private class FastCharSequence implements CharSequence {

        private ByteBuffer bb;
        private int start;

        /**
         * @param bb Byte buffer with file content.
         * @param start Position in the buffer where then new sequence starts.
         */
        public FastCharSequence(ByteBuffer bb, int start) {
            this.bb = bb;
            this.start = start;
        }

        @Override
        public int length() {
            return bb.limit();
        }

        @Override
        public char charAt(int index) {
            return (char) bb.get(start + index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new FastCharSequence(bb, start);
        }
    }

    /**
     * Character sequence for matching content of very big files.
     */
    private class LongCharSequence implements CharSequence {

        private long fileSize;
        private FileInputStream fileInputStream;
        private FileChannel fileChannel;
        private Charset charset;
        /*
         * At which character in the file the current buffer starts (counting
         * from 0).
         */
        private int currentStart = -1;
        /**
         * Current buffer with character data.
         */
        private CharBuffer currentBuffer = CharBuffer.allocate(SIZE_LIMIT);
        private CharsetDecoder currentDecoder = null;
        private int length = -1; // Length of sequence, in characters.
        private long readBytes = 0;
        private int lastIndex = 0;
        private int returns = 0;
        private int retrieves = 0;
        private int maps = 0;

        public LongCharSequence(File file, Charset charset)
                throws FileNotFoundException {

            this.charset = charset;
            fileInputStream = new FileInputStream(file);
            fileChannel = fileInputStream.getChannel();
            fileSize = file.length();
        }

        /**
         * Reset the character sequence - to read the file from the beginning.
         */
        public void reset() {
            currentDecoder = prepareDecoder(charset);
            currentDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            readBytes = 0;
            currentBuffer.clear();
            currentStart = -1;
        }

        /**
         * Compute lenght of this sequence - quite expensive operation, indeed.
         */
        @Override
        public int length() {
            if (length != -1) {
                return length;
            }
            long start = System.currentTimeMillis();
            int charactersRead = 0;
            long bytesRead = 0;
            MappedByteBuffer mappedByteBuffer = null;
            CharBuffer charBuffer = CharBuffer.allocate(SIZE_LIMIT);
            CharsetDecoder decoder = prepareDecoder(charset);
            decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);

            try {
                while (bytesRead < fileSize) {
                    mappedByteBuffer = fileChannel.map(
                            FileChannel.MapMode.READ_ONLY, bytesRead,
                            Math.min(SIZE_LIMIT, fileSize - bytesRead));
                    CoderResult result;
                    do {
                        charBuffer.clear();
                        result = decoder.decode(
                                mappedByteBuffer, charBuffer,
                                bytesRead + SIZE_LIMIT >= fileSize);
                        if (result.isUnmappable() || result.isMalformed()
                                || result.isError()) {
                            throw new IOException("Error decoding file: "
                                    + result.toString() + " ");
                        }
                        if (bytesRead + SIZE_LIMIT >= fileSize) {
                            LOG.info("Coding end");
                        }
                        charactersRead += charBuffer.position();
                    } while (result.isOverflow());

                    int readNow = mappedByteBuffer.position();
                    bytesRead += readNow;
                    unmap(mappedByteBuffer);
                }
                charBuffer.clear();
                boolean repeat;
                do {
                    repeat = decoder.flush(charBuffer).isOverflow();
                    charactersRead += charBuffer.position();
                    charBuffer.clear();
                } while (repeat);
            } catch (IOException ex) {
                if (mappedByteBuffer != null) {
                    unmap(mappedByteBuffer);
                }
                Exceptions.printStackTrace(ex);
            }
            length = charactersRead;
            LOG.log(Level.INFO, "Length computed in {0} ms.", //NOI18N
                    System.currentTimeMillis() - start);
            return length;
        }

        @Override
        public char charAt(int index) {

            if (index < lastIndex) {
                returns++;
            }
            lastIndex = index;
            if (index > length()) {
                throw new IndexOutOfBoundsException();
            }
            if (isInBuffer(index)) {
                return getFromBuffer(index);
            } else {
                if (index < currentStart || currentStart == -1) {
                    reset();
                }
                retrieves++;
                MappedByteBuffer mappedByteBuffer = null;
                try {
                    while (readBytes < fileSize) {
                        try {
                            mappedByteBuffer = fileChannel.map(
                                    FileChannel.MapMode.READ_ONLY,
                                    readBytes,
                                    Math.min(SIZE_LIMIT, fileSize - readBytes));
                            maps++;
                            CoderResult result;
                            do {
                                currentStart = currentStart == -1 ? 0
                                        : currentStart + currentBuffer.limit();
                                currentBuffer.clear();
                                result = currentDecoder.decode(mappedByteBuffer,
                                        currentBuffer,
                                        readBytes + SIZE_LIMIT >= fileSize);
                                currentBuffer.flip();
                                int readChars = currentBuffer.limit();
                                if (currentStart + readChars > index) {
                                    return getFromBuffer(index);
                                }
                                if (result.isUnmappable() || result.isMalformed()
                                        || result.isError()) {
                                    throw new IOException("Error decoding file: "
                                            + result.toString() + " ");
                                }
                            } while (result.isOverflow());
                        } finally {
                            if (mappedByteBuffer != null) {
                                int readNow = mappedByteBuffer.position();
                                readBytes += readNow;
                                unmap(mappedByteBuffer);
                            }
                        }
                    }
                    boolean repeat;
                    do {
                        repeat = currentDecoder.flush(currentBuffer).isOverflow();
                        int size = currentBuffer.position();
                        if (size + currentStart > index) {
                            currentBuffer.flip();
                            return currentBuffer.get(index - currentStart);
                        }
                        currentBuffer.clear();
                        currentStart += size;
                    } while (repeat);
                } catch (IOException ex) {
                    if (mappedByteBuffer != null) {
                        unmap(mappedByteBuffer);
                    }
                    Exceptions.printStackTrace(ex);
                }
            }

            throw new IllegalStateException(
                    "Cannot get character.");   //NOI18N
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            if (end - start < LINE_LIMIT) {
                StringBuilder sb = new StringBuilder();
                for (int i = start; i < end; i++) {
                    sb.append(charAt(i));
                }
                return sb.toString();
            } else {
                throw new IllegalArgumentException(
                        "Long subSequences are not supported.");        //NOI18N
            }
        }

        @Override
        public String toString() {
            return subSequence(0, length()).toString();
        }

        public void close() {
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        public void terminate() {
        }

        /**
         * Is character on the specified index in the current buffer?
         */
        private boolean isInBuffer(int index) {
            return currentStart != -1 && index >= currentStart
                    && index < currentStart + currentBuffer.limit();
        }

        /**
         * Get character on the specified index from the current buffer.
         */
        private char getFromBuffer(int index) {
            char c = currentBuffer.charAt(index - currentStart);
            return c;
        }
    }

    /**
     * Helper for associating line and position info to TextDetail objects.
     */
    private class LineInfoHelper {

        private CharSequence charSequence;
        private Matcher lineMatcher;
        private int lastStartPos = 0;
        private int currentLineNumber = 0;
        private int currentLineStart = -1;
        private int currentLineEnd = -1;
        private String lastLine = null;

        public LineInfoHelper(CharSequence charSequence) {
            this.charSequence = charSequence;
            this.lineMatcher = linePattern.matcher(charSequence);
        }

        /**
         * Find line number and text for passed positions.
         *
         * State of line matcher is defined by previous invocations of this
         * method with the same line matcher.
         *
         * Start position must be bigger than it was in the previous invocation.
         *
         * @param textDetail Text details to set.
         * @param startPos Start position of found text, for which we are
         * looking for the correct line number
         * @param endPos End position of found text.
         */
        public void findAndSetPositionInfo(TextDetail textDetail,
                int startPos, int endPos, String text) {
            if (startPos < lastStartPos) {
                throw new IllegalStateException(
                        "Start offset lower than the previous one.");   //NOI18N
            }
            updateStateForPosition(startPos);
            setTextDetailInfo(textDetail, startPos, endPos, text);
        }

        /**
         * Update internal state for a position of a character in the file.
         */
        private void updateStateForPosition(int pos) {
            if (pos > currentLineEnd) {
                boolean found = false;
                while (lineMatcher.find()) {
                    currentLineNumber++;
                    currentLineEnd = lineMatcher.end() - 1;
                    if (lineMatcher.end() > pos) {
                        currentLineStart = lineMatcher.start();
                        lastLine = lineMatcher.group().trim();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (currentLineNumber == 0) {
                        setupOnlyLine();
                    } else {
                        setupLastLine();
                    }
                }
            }
        }

        /**
         * Set properties of a TextDetail instance.
         */
        private void setTextDetailInfo(TextDetail textDetail, int startPos,
                int endPos, String text) {

            textDetail.setLine(currentLineNumber);
            textDetail.setStartOffset(startPos);
            textDetail.setEndOffset(endPos);
            textDetail.setMarkLength(endPos - startPos);
            textDetail.setMatchedText(text);
            textDetail.setColumn(startPos - currentLineStart + 1);
            textDetail.setLineText(lastLine);
        }

        /**
         * Set internal state if the last line in a multi-line file has been
         * reached
         */
        private void setupLastLine() {
            currentLineNumber++;
            currentLineStart = currentLineEnd + 1;
            currentLineEnd = charSequence.length();
            lastLine = charSequence.subSequence(
                    currentLineStart,
                    currentLineEnd).toString().trim();
        }

        /**
         * Set internal state if there is only single line in the file.
         */
        private void setupOnlyLine() {
            currentLineNumber = 1;
            String s = charSequence.toString();
            currentLineStart = 0;
            currentLineEnd = s.length();
            lastLine = s.trim();
        }
    }

    /**
     * Reader that reads lines (and some info about them) from a file.
     *
     * @author jhavlin
     */
    class LineReader {

        int lastChar = 0;
        int pos = 0;
        int line = 1;
        int length = 0;
        CharSequence charSequence;

        LineReader(CharSequence charSequence)
                throws IOException {

            this.charSequence = charSequence;
            this.length = charSequence.length();
        }

        /**
         * Read next line from the file.
         *
         * @return Object with line info, or null if no more lines exit.
         * @throws IOException
         */
        LineInfo readNext() throws IOException {

            int ch;
            LineInfo li = new LineInfo(pos, line);

            if (pos >= length) {
                return null;
            }

            while (pos < length) {
                ch = charSequence.charAt(pos);
                pos++;
                if (ch == '\n' && lastChar == '\r') {                   //NOI18N
                    li = new LineInfo(pos, line);
                } else if (isLineTerminator(ch)) {
                    line++;
                    lastChar = ch;
                    li.close();
                    return li;
                } else {
                    li.appendCharacter(ch);
                }
                lastChar = ch;
            }
            li.close();
            return li;
        }

        private boolean isLineTerminator(int ch) {
            return ch == BufferedCharSequence.UnicodeLineTerminator.LF
                    || ch == BufferedCharSequence.UnicodeLineTerminator.CR
                    || ch == BufferedCharSequence.UnicodeLineTerminator.LS
                    || ch == BufferedCharSequence.UnicodeLineTerminator.NEL
                    || ch == BufferedCharSequence.UnicodeLineTerminator.PS;
        }
    }

    /**
     * Info about a line in a file.
     *
     * It contains its number in the file, file offsets of its first and last
     * characters, length and value.
     */
    class LineInfo {

        private int start;
        private int length = 0;
        private int number;
        private StringBuilder sb = new StringBuilder();
        private String string = null;

        LineInfo(int start, int number) {
            this.start = start;
            this.number = number;
        }

        private void appendCharacter(int c) throws IOException {
            sb.append((char) c);
            length++;
            if (length > LINE_LIMIT) {
                throw new IOException("Line is too long: " + number);
            }
        }

        String getString() {
            return this.string;
        }

        /**
         * Line number in the file.
         */
        int getNumber() {
            return number;
        }

        /**
         * File offset of the first character.
         */
        int getFileStart() {
            return start;
        }

        /**
         * File offset of the last character.
         */
        int getFileEnd() {
            return start + length;
        }

        /**
         * Test if the line is non-empty.
         */
        private boolean isNotEmpty() {
            return length > 0;
        }

        /**
         * Get lenght of the line.
         */
        int getLength() {
            return length;
        }

        /**
         * Close this line for modifications.
         */
        void close() {
            this.string = this.sb.toString();
            this.sb = null;
        }
    }
}
