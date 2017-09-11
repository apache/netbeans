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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnmappableCharacterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.Constants;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.TextDetail;
import org.netbeans.modules.search.TextRegexpUtil;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * Multiline searcher that uses character sequence backed by buffers.
 *
 * The buffers are filled from input stream.
 *
 * @author jhavlin
 */
public class MultiLineStreamMatcher extends AbstractMatcher {

    private static final Logger LOG = Logger.getLogger(
            MultiLineStreamMatcher.class.getName());
    /**
     * Terminated flag.
     */
    private volatile boolean terminated = false;
    /**
     * List of currently processed searches.
     */
    private List<BufferedCharSequence> currentlyProcessedSequences =
            new ArrayList<BufferedCharSequence>(1);
    private Pattern pattern;
    private SearchPattern searchPattern;

    public MultiLineStreamMatcher(SearchPattern searchPattern) {
        this.searchPattern = searchPattern;
        this.pattern = TextRegexpUtil.makeTextPattern(searchPattern);
    }

    @Override
    public Def checkMeasuredInternal(FileObject fo, SearchListener listener) {

        listener.fileContentMatchingStarted(fo.getPath());
        Charset lastCharset = FileEncodingQuery.getEncoding(fo);

        BufferedCharSequence bcs = null;
        List<TextDetail> txtDetails;
        CharsetDecoder decoder = prepareDecoder(lastCharset);
        try {
            bcs = new BufferedCharSequence(fo, decoder, fo.getSize());
            bcs.setSearchListener(listener);
            registerProcessedSequence(bcs);
            txtDetails = getTextDetailsML(bcs, fo, searchPattern);
            unregisterProcessedSequence(bcs);

            if (txtDetails != null && !txtDetails.isEmpty()) {
                return new MatchingObject.Def(fo, lastCharset, txtDetails);
            }
        } catch (BufferedCharSequence.TerminatedException e) {
            LOG.log(Level.INFO, "Search in {0} was terminated.", fo);  // NOI18N
        } catch (DataObjectNotFoundException e) {
            LOG.log(Level.SEVERE,
                    "Unable to get data object for the {0}", fo);      // NOI18N
            LOG.throwing(DefaultMatcher.class.getName(),
                    "checkFileContent", e);                            // NOI18N
            listener.generalError(e);
        } catch (FileNotFoundException e) {
            LOG.log(Level.SEVERE,
                    "Unable to get input stream for the {0}", fo);     // NOI18N
            LOG.throwing(DefaultMatcher.class.getName(),
                    "checkFileContent", e);                            // NOI18N
            listener.generalError(e);
        } catch (BufferedCharSequence.SourceIOException e) {
            if (e.getCause() instanceof CharacterCodingException) {
                handleDecodingError(listener, fo, decoder,
                        (CharacterCodingException) e.getCause());
            } else {
                LOG.log(Level.SEVERE,
                        "IOException during process for the {0}", fo);  //NOI18N
                LOG.log(Level.INFO, "checkFileContent", e);             //NOI18N
                listener.generalError(e);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Unexpected Exception during process for the {0}", // NOI18N
                    fo);
            LOG.log(Level.INFO, "checkFileContent", e);                // NOI18N
            listener.generalError(e);
        } finally {
            if (bcs != null) {
                try {
                    bcs.close();
                } catch (IOException ex) {
                    // do nothing
                }
            }
        }
        return null;
    }

    /**
     * Get text details for multi-line pattern matching.
     *
     * @return MatchingObject instance, or null if this file does not match.
     */
    private ArrayList<TextDetail> getTextDetailsML(BufferedCharSequence bcs,
            FileObject fo,
            SearchPattern sp)
            throws BufferedCharSequence.SourceIOException,
            DataObjectNotFoundException {

        ArrayList<TextDetail> txtDetails = null;
        DataObject dataObject = null;
        FindState fs = new FindState(bcs);

        final int limit = Constants.DETAILS_COUNT_LIMIT;
        Matcher matcher = pattern.matcher(bcs);


        while (matcher.find() && (txtDetails == null
                || txtDetails.size() < limit)) {

            if (txtDetails == null) {
                txtDetails = new ArrayList<TextDetail>();
                dataObject = DataObject.find(fo);
            }

            int matcherStart = matcher.start();
            int column = fs.calcColumn(matcherStart);
            int lineNumber = fs.getLineNumber();
            String lineText = fs.getLineText();

            TextDetail det = MatcherUtils.createTextDetail(true, matcher, dataObject,
                    lineNumber, lineText, column, searchPattern);
            txtDetails.add(det);
        }
        return txtDetails;
    }

    /**
     * Terminate matching.
     */
    @Override
    public void terminate() {
        terminated = true;
        try {
            terminateCurrentSearches();
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        }
    }

    /**
     * Register BufferedCharSequence that is being processed by this object. It
     * is used when user needs to terminate the current search.
     */
    private synchronized void registerProcessedSequence(
            BufferedCharSequence bcs) throws IOException {
        if (terminated) {
            bcs.close();
        } else {
            currentlyProcessedSequences.add(bcs);
        }
    }

    /**
     * Unregister a BufferedCharSequence after it was processed.
     */
    private synchronized void unregisterProcessedSequence(
            BufferedCharSequence bcc) {
        currentlyProcessedSequences.remove(bcc);
    }

    /**
     * Stop all searches that are processed by this instance.
     */
    private synchronized void terminateCurrentSearches() throws IOException {
        for (BufferedCharSequence bcs : currentlyProcessedSequences) {
            bcs.terminate();
        }
        currentlyProcessedSequences.clear();
    }

    /**
     * Utility class providing optimal calculating of the column.
     */
    private class FindState {

        int lineNumber = 1;
        int lineStartOffset = 0;
        int prevCR = 0;
        BufferedCharSequence bcs;

        FindState(BufferedCharSequence bcs) {
            this.bcs = bcs;
        }

        int getLineNumber() {
            return lineNumber;
        }

        String getLineText() {
            return bcs.getLineText(lineStartOffset);
        }

        int calcColumn(int matcherStart) {
            try {
                while (bcs.position() < matcherStart) {
                    char curChar = bcs.nextChar();
                    switch (curChar) {
                        case BufferedCharSequence.UnicodeLineTerminator.LF:
                        case BufferedCharSequence.UnicodeLineTerminator.PS:
                        case BufferedCharSequence.UnicodeLineTerminator.LS:
                        case BufferedCharSequence.UnicodeLineTerminator.NEL:
                            lineNumber++;
                            lineStartOffset = bcs.position();
                            prevCR = 0;
                            break;
                        case BufferedCharSequence.UnicodeLineTerminator.CR:
                            prevCR++;
                            char nextChar = bcs.charAt(bcs.position());
                            if (nextChar
                                    != BufferedCharSequence.UnicodeLineTerminator.LF) {

                                lineNumber++;
                                lineStartOffset = bcs.position();
                                prevCR = 0;
                            }
                            break;
                        default:
                            prevCR = 0;
                    }
                }
            } catch (IndexOutOfBoundsException ioobe) {
                // It is OK. It means that EOF is reached, i.e.
                // bcs.position() >= bcs.length()
            }
            int column = matcherStart - lineStartOffset + 1 - prevCR;
            return column;
        }
    }
}
