/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.matcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
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
    private SearchPattern searchPattern;
    private Pattern pattern;
    private int count = 0;

    public SingleLineStreamMatcher(SearchPattern searchPattern) {
        this.searchPattern = searchPattern;
        pattern = TextRegexpUtil.makeTextPattern(searchPattern);
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
        } catch (CharacterCodingException e) {
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
        final InputStream stream = fo.getInputStream();
        try {
            LineReader nelr = new LineReader(decoder, stream);
            try {
                LineReader.LineInfo line;
                while ((line = nelr.readNext()) != null && canRun
                        && count < limit) {
                    Matcher m = pattern.matcher(line.getString());
                    while (m.find() && canRun) {
                        if (dets == null) {
                            dets = new LinkedList<TextDetail>();
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
        } finally {
            stream.close();
        }
        return dets;
    }

    @Override
    public void terminate() {
        this.terminated = true;
    }
}
