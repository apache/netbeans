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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.Constants;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.TextDetail;
import org.netbeans.modules.search.TextRegexpUtil;
import org.netbeans.modules.search.matcher.MultiLineMappedMatcherSmall.LineInfoHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Multi-line matcher for small files that uses file-mapped memory.
 *
 * @author jhavlin
 */
public class AsciiMultiLineMappedMatcher extends AbstractMatcher {

    private SearchPattern searchPattern;
    private Pattern pattern;
    private int fileMatches = 0;
    private int itemMatches = 0;

    public AsciiMultiLineMappedMatcher(SearchPattern searchPattern) {
        this.searchPattern = searchPattern;
        this.pattern = TextRegexpUtil.makeTextPattern(searchPattern);
    }

    @Override
    protected Def checkMeasuredInternal(FileObject fo,
            SearchListener listener) {

        MappedByteBuffer bb = null;
        FileChannel fc = null;
        try {

            listener.fileContentMatchingStarted(fo.getPath());
            File file = FileUtil.toFile(fo);

            // Open the file and then get a channel from the stream
            FileInputStream fis = new FileInputStream(file);
            fc = fis.getChannel();

            // Get the file's size and then map it into memory
            int sz = (int) fc.size();
            bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

            List<TextDetail> list = matchWholeFile(new FastCharSequence(bb, 0),
                    fo);

            if (list != null && !list.isEmpty()) {
                return new Def(fo, Charset.forName("ASCII"), list);
            } else {
                return null;
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
            MatcherUtils.unmap(bb);
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

    @Override
    public void terminate() {
        // no need to terminate searching in small files
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
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i ++) {
                sb.append(charAt(i));
            }
            return sb.toString();
        }
    }
}
