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
package org.netbeans.modules.search.matcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.LinkedList;
import java.util.List;
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
 * Multi-line matcher for big files that uses file-mapped memory.
 *
 * @author jhavlin
 */
public class MultiLineMappedMatcherBig extends AbstractMatcher {

    private static int SIZE_LIMIT = 10 * 1024 * 1024;
    private static int LINE_LIMIT = 4 * 1024;
    private SearchPattern searchPattern;
    private Pattern pattern;
    private int fileMatches = 0;
    private int itemMatches = 0;
    private volatile boolean terminated = false;

    public MultiLineMappedMatcherBig(SearchPattern searchPattern) {
        this.searchPattern = searchPattern;
        this.pattern = TextRegexpUtil.makeTextPattern(searchPattern);
    }

    @Override
    protected Def checkMeasuredInternal(FileObject file,
            SearchListener listener) {

        Charset charset = FileEncodingQuery.getEncoding(file);

        LongCharSequence longSequence = null;
        try {
            File f = FileUtil.toFile(file);
            longSequence = new LongCharSequence(f, charset);
            List<TextDetail> textDetails = matchWholeFile(longSequence, file);
            if (textDetails == null) {
                return null;
            } else {
                Def def = new Def(file, charset, textDetails);
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
     * Perform pattern matching inside the whole file.
     *
     * @param cb Character buffer.
     * @param fo File object.
     */
    private List<TextDetail> matchWholeFile(CharSequence cb, FileObject fo)
            throws DataObjectNotFoundException {

        Matcher textMatcher = pattern.matcher(cb);
        DataObject dataObject = null;
        MultiLineMappedMatcherSmall.LineInfoHelper lineInfoHelper =
                new MultiLineMappedMatcherSmall.LineInfoHelper(cb);

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

    @Override
    public void terminate() {
        terminated = true;
    }

    /**
     * Character sequence for matching content of very big files.
     */
    private class LongCharSequence implements CharSequence {

        private long fileSize;
        private FileInputStream fileInputStream;
        private FileChannel fileChannel;
        /**
         * At which character in the file the current buffer starts (counting
         * from 0).
         */
        private int charBufferStartsAt = -1;
        /**
         * At which character in the file the current buffer ends (counting from
         * 0).
         */
        private int charBufferEndsAt = -1;
        /**
         * Current buffer with character data. It shows a part of backing file.
         *
         * The part starts at charBufferStartsAt and ends at charBufferStartsAt
         * + charBuffer.limit().
         */
        private CharBuffer charBuffer = null;
        /**
         * Buffer with bytes from the file. The data between its current
         * position and its limit has not been decoded yet. If it is null, no
         * undecoded bytes are remaining.
         */
        private MappedByteBuffer byteBuffer;
        /**
         * Decoder for file encoding.
         */
        private CharsetDecoder decoder = null;
        /**
         * Total lengh of the character sequence. Value -1 means "unknown".
         */
        private int length = -1;
        /**
         * Number of already decoded bytes;
         */
        private long decodedBytes = 0;
        /*
         * Some variables for internal statistics. Usefull when debugging.
         */
        /**
         * Overflow indicated by the last ending decode or flush command.
         */
        private boolean overflow = false;
        private int shifts = 0;
        private int maps = 0;
        /**
         * Current state of this sequence
         */
        private State state = State.STANDARD;

        public LongCharSequence(File file, Charset charset)
                throws FileNotFoundException {

            decoder = prepareDecoder(charset);
            fileInputStream = new FileInputStream(file);
            fileChannel = fileInputStream.getChannel();
            fileSize = file.length();
            charBuffer = CharBuffer.allocate((int) Math.min(fileSize,
                    SIZE_LIMIT));
        }

        /**
         * Reset the character sequence - to read the file from the beginning.
         */
        public void reset() {
            decoder.reset();
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            decodedBytes = 0;
            charBuffer.clear();
            charBufferStartsAt = -1;
            charBufferEndsAt = -1;
            overflow = false;
            state = State.STANDARD;
            if (byteBuffer != null) {
                MatcherUtils.unmap(byteBuffer);
                byteBuffer = null;
            }
        }

        /**
         * Compute lenght of this sequence - quite expensive operation, indeed.
         */
        @Override
        public synchronized int length() {
            if (length == -1) {
                try {
                    if (charBufferStartsAt == -1) {
                        reset();
                    }
                    while (shiftBuffer()) {
                        if (terminated) {
                            throw new TerminatedException();

                        }
                        // call to decode the whole file
                    }
                    length = charBufferStartsAt + charBuffer.limit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return length;
        }

        /**
         * Get character at an index.
         */
        @Override
        public synchronized char charAt(int index) {

            if (terminated) {
                throw new TerminatedException();
            }

            if (isInBuffer(index)) {
                return getFromBuffer(index);
            } else {
                if (index > length()) {
                    throw new IndexOutOfBoundsException();
                }
                if (index < charBufferStartsAt || charBufferStartsAt == -1) {
                    reset();
                }
                try {
                    while (shiftBuffer()) {
                        if (isInBuffer(index)) {
                            return getFromBuffer(index);
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            throw new IllegalStateException(
                    "Cannot get character.");   //NOI18N
        }

        /**
         * Shift buffer window to the following part of the file.
         *
         * Current buffer, buffer start and read bytes are updated. Current
         * buffer position will be set to 0 and its limit to number of available
         * characters.
         *
         * @return True if the buffer was shifted, false if end of the file was
         * reached (the buffer was been shifted).
         */
        private boolean shiftBuffer() throws IOException {

            shifts++;

            boolean res;

            if (state == State.FLUSHED) {
                assert overflow == false;
                res = false;
            } else if (state == State.STANDARD) {
                assert overflow == false;
                res = shiftBufferStandard();
            } else if (state == State.ENDING) {
                assert overflow == true;
                res = shiftBufferEnding();
            } else if (state == State.FLUSHING) {
                assert overflow == true;
                res = shiftBufferFlushing();
            } else {
                throw new IllegalStateException();
            }
            updateBufferBounds();
            return res;
        }

        private boolean shiftBufferStandard() throws IllegalStateException,
                IOException {
            if (byteBuffer == null || byteBuffer.remaining() == 0) {
                if (byteBuffer != null) {
                    MatcherUtils.unmap(byteBuffer);
                }
                long size = Math.min(SIZE_LIMIT, fileSize - decodedBytes);
                byteBuffer = fileChannel.map(
                        FileChannel.MapMode.READ_ONLY,
                        decodedBytes,
                        size);
                maps++;
            }

            long origByteBufPosition = byteBuffer.position();
            int origCharBufLimit = charBufferStartsAt == -1
                    ? 0
                    : charBuffer.limit();
            charBuffer.clear();
            CoderResult res;
            res = decoder.decode(byteBuffer, charBuffer, false);
            charBufferStartsAt = charBufferStartsAt == -1
                    ? 0
                    : charBufferStartsAt + origCharBufLimit;
            decodedBytes += byteBuffer.position() - origByteBufPosition;

            if (res.isOverflow()) {
                /*
                 * To much bytes for char buffer, will read from the same buffer
                 * the next time again.
                 */
                if (origByteBufPosition == byteBuffer.position()) {
                    throw new IllegalStateException("Neverending loop?");
                }
                charBuffer.flip();
                return true;
            } else if (decodedBytes < fileSize) {
                /*
                 * Not at the end of file, will need new byte buffer.
                 */
                charBuffer.flip();
                MatcherUtils.unmap(byteBuffer);
                byteBuffer = null;
                return true;
            } else {
                /*
                 * All bytes decoded, end and flush decoder.
                 */
                state = State.ENDING;
                return shiftBufferEnding();
            }
        }

        /**
         * Called when state is ENDING. All bytes were read and passed to
         * decoder.
         *
         * If overflow flag is on, the char buffer is cleared before decoding.
         */
        private boolean shiftBufferEnding() {

            assert state == State.ENDING;

            if (overflow) {
                charBufferStartsAt = charBufferStartsAt + charBuffer.limit();
                charBuffer.clear();
            }
            CoderResult res = decoder.decode(byteBuffer, charBuffer, true);
            if (res.isOverflow()) {
                charBuffer.flip();
                overflow = true;
                return true;
            } else {
                overflow = false;
                state = State.FLUSHING;
                return shiftBufferFlushing();
            }
        }

        /**
         * Called when ending decode method was called and the doceder method
         * should be flushed.
         *
         * If overflow flag is on, the char buffer is cleared before decoding.
         */
        private boolean shiftBufferFlushing() {

            assert state == State.FLUSHING;

            if (overflow) {
                charBufferStartsAt = charBufferStartsAt + charBuffer.limit();
                charBuffer.clear();
            }
            CoderResult res = decoder.flush(charBuffer);
            charBuffer.flip();
            if (res.isOverflow()) {
                overflow = true;
            } else {
                overflow = false;
                state = State.FLUSHED;
            }
            return true;
        }

        @Override
        public synchronized CharSequence subSequence(int start, int end) {
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
        public synchronized String toString() {
            return subSequence(0, length()).toString();
        }

        /**
         * Close and release all resources.
         */
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
            if (byteBuffer != null) {
                MatcherUtils.unmap(byteBuffer);
            }
        }

        /**
         * Is character on the specified index in the current buffer?
         */
        private boolean isInBuffer(int index) {
            return index >= charBufferStartsAt
                    && index < charBufferEndsAt;
        }

        /**
         * Get character on the specified index from the current buffer.
         */
        private char getFromBuffer(int index) {
            return charBuffer.charAt(index - charBufferStartsAt);
        }

        /**
         * Update char buffer start and end offset.
         */
        private void updateBufferBounds() {
            if (charBufferStartsAt == -1) {
                charBufferEndsAt = -1;
            } else {
                charBufferEndsAt = charBufferStartsAt + charBuffer.limit();
            }
        }
    }

    /**
     * State of long character sequence.
     */
    private enum State {

        STANDARD, ENDING, FLUSHING, FLUSHED
    }

    class TerminatedException extends RuntimeException {
    }
}
