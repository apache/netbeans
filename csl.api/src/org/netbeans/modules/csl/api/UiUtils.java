/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.csl.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.navigation.Icons;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;


/** 
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * This class contains various methods bound to visualization of Java model
 * elements. It was formerly included under SourceUtils
 *
 * XXX - needs cleanup
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 */
public final class UiUtils {

    private static final int AWT_TIMEOUT = 1000;
    private static final int NON_AWT_TIMEOUT = 2000;

    public static boolean open(Source source, ElementHandle handle) {
        assert source != null;
        assert handle != null; // Only one should be set

        DeclarationLocation location = getElementLocation(source, handle);

        if (location != DeclarationLocation.NONE) {
            return doOpen(location.getFileObject(), location.getOffset());
        }

        return false;
    }

    public static boolean open(final FileObject fo, final int offset) {
        assert fo != null;
        
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public @Override void run() {
                    doOpen(fo, offset);
                }
            });
            return true; // not exactly accurate, but....
        }
        
        return doOpen(fo, offset);
    }

    public static ImageIcon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
        return Icons.getElementIcon(elementKind, modifiers);
    }



    public static KeystrokeHandler getBracketCompletion(final Document doc, final int offset) {
        final AtomicReference<KeystrokeHandler> ref = new AtomicReference<KeystrokeHandler>();
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenHierarchy hi = TokenHierarchy.get(doc);

                //# Bug 184156 -  [69cat][editor][HTML] Typing quote after code completion of CSS definition
                //If the offset falls to a position between two tokens with different embeddings,
                //we should try to use KeystrokeHandler-s for both languages.
                List<TokenSequence<?>> forward = hi.embeddedTokenSequences(offset, false);
                List<TokenSequence<?>> backward = hi.embeddedTokenSequences(offset, true);

                final KeystrokeHandler bwHandler = getFirstHandler(backward);
                final KeystrokeHandler fwHandler = getFirstHandler(forward);

                if(fwHandler == null && bwHandler == null) {
                    return ;
                }

                //forward bias handler has a precedence to make it compatible
                //with the former implementation as much as possible.
                final KeystrokeHandler defaultt = fwHandler == null ? bwHandler : fwHandler;

                if(fwHandler != null && bwHandler != null && fwHandler != bwHandler) {
                    //we are on a border of two embeddings, there's a need to use both
                    //keystroke handlers, create a delegating handler
                    ref.set(new KeystrokeHandler() {

                        @Override
                        public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
                            if(!fwHandler.beforeCharInserted(doc, caretOffset, target, ch)) {
                                return bwHandler.beforeCharInserted(doc, caretOffset, target, ch);
                            } else {
                                return true;
                            }
                        }

                        @Override
                        public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
                            if (!fwHandler.afterCharInserted(doc, caretOffset, target, ch)) {
                                return bwHandler.afterCharInserted(doc, caretOffset, target, ch);
                            } else {
                                return true;
                            }
                        }

                        @Override
                        public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
                            if (!fwHandler.charBackspaced(doc, caretOffset, target, ch)) {
                                return bwHandler.charBackspaced(doc, caretOffset, target, ch);
                            } else {
                                return true;
                            }
                        }

                        @Override
                        public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
                            return defaultt.beforeBreak(doc, caretOffset, target);
                        }

                        @Override
                        public OffsetRange findMatching(Document doc, int caretOffset) {
                            return defaultt.findMatching(doc, caretOffset);
                        }

                        @Override
                        public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
                            return defaultt.findLogicalRanges(info, caretOffset);
                        }

                        @Override
                        public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
                            return defaultt.getNextWordOffset(doc, caretOffset, reverse);
                        }

                    });

                } else {
                    //common situation
                    ref.set(defaultt);
                }

            }

        });

        return ref.get();
    }


    // Private methods ---------------------------------------------------------
    private static KeystrokeHandler getFirstHandler(List<TokenSequence<?>> embeddedTS) {
        for (int i = embeddedTS.size() - 1; i >= 0; i--) {
            TokenSequence<?> ts = embeddedTS.get(i);
            Language lang = LanguageRegistry.getInstance().getLanguageByMimeType(ts.language().mimeType());
            KeystrokeHandler handler = lang != null ? lang.getBracketCompletion() : null;
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    private static final Logger LOG = Logger.getLogger(UiUtils.class.getName());

    private UiUtils() {
    }

    private static boolean doOpen(FileObject fo, int offset) {
        try {
            DataObject od = DataObject.find(fo);
            return NbDocument.openDocument(od, offset, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
        } catch (DataObjectNotFoundException e) {
            LOG.log(Level.FINE, null, e);
        }
        return false;
    }

    private static DeclarationLocation getElementLocation(final Source source, final ElementHandle handle) {
        if (source.getFileObject() == null) {
            return DeclarationLocation.NONE;
        }

        FileObject fileObject = handle.getFileObject();
        if (fileObject != null && fileObject != source.getFileObject()) {
            // The element is not in the parse tree for this parse job; it is
            // probably something like an indexed element
            return new DeclarationLocation(fileObject, -1);
        }

        final DeclarationLocation[] result = new DeclarationLocation[] { null };
        final AtomicBoolean cancel = new AtomicBoolean();
        final UserTask t = new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws ParseException {
                    if (cancel.get()) {
                        return;
                    }
                    if (resultIterator.getSnapshot().getMimeType().equals(handle.getMimeType())) {
                        Parser.Result r = resultIterator.getParserResult();
                        if (r instanceof ParserResult) {
                            ParserResult info = (ParserResult) r;
                            OffsetRange range = handle.getOffsetRange(info);
                            if (range != OffsetRange.NONE && range != null) {
                                result[0] = new DeclarationLocation(info.getSnapshot().getSource().getFileObject(), range.getStart());
                                return;
                            }
                        }
                    }

                    for(Embedding e : resultIterator.getEmbeddings()) {
                        run(resultIterator.getResultIterator(e));
                        if (result[0] != null) {
                            break;
                        }
                    }
                }
            };

            if (IndexingManager.getDefault().isIndexing()) {
                int timeout = SwingUtilities.isEventDispatchThread() ? AWT_TIMEOUT : NON_AWT_TIMEOUT;
                Future<Void> f;
                try {
                    f = ParserManager.parseWhenScanFinished(Collections.singleton(source), t);
                } catch (ParseException ex) {
                    LOG.log(Level.WARNING, null, ex);
                    return DeclarationLocation.NONE;
                }

                try {
                    f.get(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, null, ex);
                    return DeclarationLocation.NONE;
                } catch (ExecutionException ex) {
                    LOG.log(Level.INFO, null, ex);
                    return DeclarationLocation.NONE;
                } catch (TimeoutException ex) {
                    f.cancel(true);
                    LOG.info("Skipping location of element offset within file, Scannig in progress"); // NOI18N
                    return DeclarationLocation.NONE; //we are opening @ 0 position. Fix #160478
                }

                if (!f.isDone()) {
                    f.cancel(true);
                    LOG.info("Skipping location of element offset within file, Scannig in progress"); // NOI18N
                    return DeclarationLocation.NONE; //we are opening @ 0 position. Fix #160478
                }
            } else if (SwingUtilities.isEventDispatchThread()) {
                ProgressUtils.runOffEventDispatchThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ParserManager.parse(Collections.singleton(source), t);
                            } catch (ParseException ex) {
                                LOG.log(Level.WARNING, null, ex);
                            }
                        }
                    },
                    NbBundle.getMessage(UiUtils.class, "TXT_CalculatingDeclPos"),
                    cancel,
                    false);
            } else {
                try {
                    ParserManager.parse(Collections.singleton(source), t);
                } catch (ParseException ex) {
                    LOG.log(Level.WARNING, null, ex);
                    return DeclarationLocation.NONE;
                }
            }
            return result[0] != null ? result[0] : DeclarationLocation.NONE;
        }
    }
