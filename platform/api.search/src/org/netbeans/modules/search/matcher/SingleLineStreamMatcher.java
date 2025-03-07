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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderMalfunctionError;
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
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * Matcher that checks individual lines, that are decoded with correct decoding.
 *
 * @author jhavlin
 */
public class SingleLineStreamMatcher extends AbstractMatcher {

    private static final int limit = Constants.DETAILS_COUNT_LIMIT;
    private volatile boolean terminated = false;
    private final SearchPattern searchPattern;
    private final Pattern pattern;
    private int count = 0;

    public SingleLineStreamMatcher(SearchPattern searchPattern) {
        this.searchPattern = searchPattern;
        this.pattern = TextRegexpUtil.makeTextPattern(searchPattern);
    }

    @Override
    public Def checkMeasuredInternal(FileObject file, SearchListener listener) {

        Charset charset = FileEncodingQuery.getEncoding(file);
        CharsetDecoder decoder = prepareDecoder(charset);
        try {
            listener.fileContentMatchingStarted(file.getPath());
            List<TextDetail> textDetails = getTextDetailsSL(file, decoder,
                    listener);
            if (textDetails == null) {
                return null;
            } else {
                return new Def(file, charset, textDetails);
            }
        } catch (CharacterCodingException | CoderMalfunctionError e) {
            handleDecodingError(listener, file, decoder, e);
            return null;
        } catch (Exception e) {
            listener.fileContentMatchingError(file.getPath(), e);
            return null;
        }
    }

    /**
     * Get text details for single-line pattern matching.
     */
    private List<TextDetail> getTextDetailsSL(final FileObject fo,
            CharsetDecoder decoder, SearchListener listener)
            throws FileNotFoundException, DataObjectNotFoundException,
            IOException {

        List<TextDetail> dets = null;
        DataObject dataObject = null;
        ReadLineBuffer lineBuffer = new ReadLineBuffer(3);
        FinishingTextDetailList finishList = new FinishingTextDetailList(3);

        boolean canRun = true;
        try (final InputStream stream = fo.getInputStream()) {
            LineReader nelr = new LineReader(decoder, stream);
            try {
                LineReader.LineInfo line;
                while ((line = nelr.readNext()) != null && canRun
                        && count < limit) {
                    Matcher m = pattern.matcher(line.getString());
                    while (m.find() && canRun) {
                        if (dets == null) {
                            dets = new LinkedList<>();
                            dataObject = DataObject.find(fo);
                        }
                        TextDetail det = MatcherUtils.createTextDetail(false, m,
                                dataObject, line.getNumber(), line.getString(),
                                line.getFileStart(), searchPattern);
                        dets.add(det);
                        for (ReadLineBuffer.Line bl : lineBuffer.getLines()) {
                            det.addSurroundingLine(bl.getNumber(), bl.getText());
                        }
                        finishList.addTextDetail(det);
                        count++;
                    }
                    if ((line.getNumber() % 50) == 0) {
                        synchronized (this) {
                            canRun = !terminated;
                        }
                        listener.fileContentMatchingProgress(fo.getPath(),
                                line.getFileEnd());
                    }
                    lineBuffer.addLine(line.getNumber(), line.getString());
                    finishList.nextLineRead(line.getNumber(), line.getString());
                }
            } finally {
                nelr.close();
            }
        }
        return dets;
    }

    @Override
    public void terminate() {
        this.terminated = true;
    }
}
