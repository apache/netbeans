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
package org.netbeans.modules.csl.editor.hyperlink;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lsp.HyperlinkLocation;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.core.GsfHtmlFormatter;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.lsp.HyperlinkLocationProvider;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 *
 */
public class GoToSupport {
    private static final Logger LOG = Logger.getLogger(GoToSupport.class.getName());

    /** Jump straight to declarations */
    static final boolean IM_FEELING_LUCKY = Boolean.getBoolean("gsf.im_feeling_lucky");

    private GoToSupport() {
    }

    public static String getGoToElementTooltip(final Document doc, final int offset) {
        return perform(doc, offset, true, null, new AtomicBoolean());
    }

    public static void performGoTo(final Document doc, final int offset) {
        final AtomicBoolean cancel = new AtomicBoolean();
        String name = NbBundle.getMessage(GoToSupport.class, "NM_GoToDeclaration");

        BaseProgressUtils.runOffEventDispatchThread(new Runnable() {

            public void run() {
                perform(doc, offset, false, null, cancel);
            }
        }, name, cancel, false);
    }

    public static CompletableFuture<HyperlinkLocation> getGoToLocation(final Document doc, final int offset) {
        DeclarationLocation[] location = new DeclarationLocation[1];
        perform(doc, offset, false, location, new AtomicBoolean());
        return CompletableFuture.completedFuture(location[0] == null || location[0] == DeclarationLocation.NONE ? 
                null : HyperlinkLocationProvider.createHyperlinkLocation(location[0].getFileObject(), location[0].getOffset(), location[0].getOffset()));
    }

    private static String perform(final Document doc, final int offset, final boolean tooltip, final DeclarationLocation[] location, final AtomicBoolean cancel) {
        if (tooltip && PopupUtil.isPopupShowing()) {
            return null;
        }

        final FileObject fo = getFileObject(doc);
        if (fo == null) {
            return null;
        }

        Source js = Source.create(fo);
        if (js == null) {
            return null;
        }

        final Language language = identifyActiveFindersLanguage(doc, offset);
        if (language == null) {
            return null; // #181565
        }
        //I tend to put assert language != null, sincen the perform() method
        //should be only called when identifyActiveFindersLanguage() before
        //returned non-null value. But probably can become false?!?!?!

        final String[] result = new String[] { null };

        try {
            ParserManager.parse(Collections.singleton(js), new UserTask() {
                public void run(ResultIterator controller) throws Exception {
                    if(cancel.get()) return ;

                    //find the proper parser result
                    ResultIterator ri = getResultIterator(controller, language.getMimeType());
                    if(ri == null) {
                        return ;
                    }

                    Parser.Result embeddedResult = ri.getParserResult();
                    if (!(embeddedResult instanceof ParserResult)) {
                        return;
                    }

                    ParserResult info = (ParserResult) embeddedResult;
                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(info.getSnapshot().getMimeType());
                    if (language == null) {
                        return;
                    }

                    DeclarationFinder finder = language.getDeclarationFinder();
                    if (finder == null) {
                        return;
                    }

                    DeclarationLocation decl = finder.findDeclaration(info, offset);

                    if (location != null) {
                        location[0] = decl;
                        return;
                    }

                    if (cancel.get()) return ;

                    if (tooltip) {
                        CodeCompletionHandler completer = language.getCompletionProvider();
                        if (decl != DeclarationLocation.NONE && completer != null) {
                            ElementHandle element = decl.getElement();
                            if (element != null) {
                                String documentationContent;
                                if (completer instanceof CodeCompletionHandler2) {
                                    Documentation documentation = ((CodeCompletionHandler2) completer).documentElement(info, element, new Callable<Boolean>(){
                                        @Override
                                        public Boolean call() throws Exception {
                                            return cancel.get();
                                        }
                                    });
                                    if (documentation != null) {
                                        documentationContent = documentation.getContent();
                                    } else {
                                        documentationContent = completer.document(info, element);
                                    }
                                } else {
                                    documentationContent = completer.document(info, element);
                                }
                                if (documentationContent != null) {
                                    result[0] = "<html><body>" + documentationContent; // NOI18N
                                }
                            }
                        }

                    } else if (decl != DeclarationLocation.NONE && decl != null) {
                        final URL url = decl.getUrl();
                        final String invalid = decl.getInvalidMessage();
                        if (url != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                                }
                            });
                        } else if (invalid != null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    // TODO - show in the editor as an error instead?
                                    StatusDisplayer.getDefault().setStatusText(invalid);
                                    Toolkit.getDefaultToolkit().beep();
                                }

                            });
                        } else {
                            if (!IM_FEELING_LUCKY && decl.getAlternativeLocations().size() > 0 &&
                                    !PopupUtil.isPopupShowing()) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        // Many alternatives - pop up a dialog and make the user choose
                                        if (!chooseAlternatives(doc, offset, decl.getAlternativeLocations())) {
                                            openLocation(decl);
                                        }
                                    }
                                });
                            } else {
                                openLocation(decl);
                            }
                        }

                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                Toolkit.getDefaultToolkit().beep();
                            }
                        });
                    }
                }
            });
        } catch (ParseException pe) {
            LOG.log(Level.WARNING, null, pe);
        }

        return result[0];
    }

    /** finds first ResultIterator of the given mimetype
     *
     * @todo refactor this to some CSL's api utility class. It's a copy of
     * WebUtils class in web.common module. The location there is not so much fitting though...
     */
    private static ResultIterator getResultIterator(ResultIterator ri, String mimetype) {
        if (ri.getSnapshot().getMimeType().equals(mimetype)) {
            return ri;
        } else if (ri.getSnapshot().getMimePath().getInheritedType().equals(mimetype)) {
            return ri;
        }
        for (Embedding e : ri.getEmbeddings()) {
            ResultIterator eri = ri.getResultIterator(e);
            if (e.getMimeType().equals(mimetype)) {
                return eri;
            } else {
                ResultIterator eeri = getResultIterator(eri, mimetype);
                if (eeri != null) {
                    return eeri;
                }
            }
        }
        return null;
    }

    private static void openLocation(DeclarationLocation location) {
        FileObject f = location.getFileObject();
        int offset = location.getOffset();

        if (f != null && f.isValid()) {
            UiUtils.open(f, offset);
        }
    }

    /** TODO - MOVE TO UTILITTY LIBRARY */
    private static JTextComponent findEditor(Document doc) {
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        if (comp.getDocument() == doc) {
            return comp;
        }
        List<? extends JTextComponent> componentList = EditorRegistry.componentList();
        for (JTextComponent component : componentList) {
            if (comp.getDocument() == doc) {
                return comp;
            }
        }

        return null;
    }

    private static boolean chooseAlternatives(Document doc, int offset, List<AlternativeLocation> alternatives) {
        String caption = NbBundle.getMessage(GoToSupport.class, "ChooseDecl");

        return chooseAlternatives(doc, offset, caption, alternatives);
    }

    public static boolean chooseAlternatives(Document doc, int offset, String caption, List<AlternativeLocation> alternatives) {
        Collections.sort(alternatives);

        // Prune results a bit
        int MAX_COUNT = 30; // Don't show more items than this
        String previous = "";
        GsfHtmlFormatter formatter = new GsfHtmlFormatter();
        int count = 0;
        List<AlternativeLocation> pruned = new ArrayList<AlternativeLocation>(alternatives.size());
        for (AlternativeLocation alt : alternatives) {
            String s = alt.getDisplayHtml(formatter);
            if (!s.equals(previous)) {
                pruned.add(alt);
                previous = s;
                count++;
                if (count == MAX_COUNT) {
                    break;
                }
            }
        }
        alternatives = pruned;
        if (alternatives.size() <= 1) {
            return false;
        }

        JTextComponent target = findEditor(doc);
        if (target != null) {
            try {
                Rectangle rectangle = target.modelToView(offset);
                Point point = new Point(rectangle.x, rectangle.y+rectangle.height);
                SwingUtilities.convertPointToScreen(point, target);

                PopupUtil.showPopup(new DeclarationPopup(caption, alternatives), caption, point.x, point.y, true, 0);

                return true;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return false;
    }

    private static FileObject getFileObject(Document doc) {
        return DataLoadersBridge.getDefault().getFileObject(doc);
    }

    public static int[] getIdentifierSpan(Document doc, int offset) {
        Language lang = identifyActiveFindersLanguage(doc, offset);
        if(lang == null) {
            return null;
        }

        DeclarationFinder finder = lang.getDeclarationFinder();
        assert finder != null;
        OffsetRange range = finder.getReferenceSpan(doc, offset);
        // rather check for consistency, we are calling to client code:
        if (range == null || range == OffsetRange.NONE) {
            LOG.log(Level.WARNING, "Inconsistent DeclarationFinder {0} for offset {1}",
                new Object[] {
                    finder,
                    offset
            });
            return null;
        }
        return new int[]{range.getStart(), range.getEnd()};
    }

    private static Language identifyActiveFindersLanguage(Document doc, int offset) {
        FileObject fo = getFileObject(doc);

        if (fo == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }

        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages((BaseDocument) doc, offset);
        //according to the lexical embedding iterate over the
        //languages from the leaf to the root and try to find
        //their declrations finders. If there is and indicates
        //it is interested in the given offset return its result
        //otherwise continue to the root.
        for (Language l : list) {
            DeclarationFinder finder = l.getDeclarationFinder();
            if (finder != null) {
                OffsetRange range = finder.getReferenceSpan(doc, offset);
                if (range == null) {
                    throw new NullPointerException(finder + " violates its contract; should not return null from getReferenceSpan."); //NOI18N
                } else if (range != OffsetRange.NONE) {
                    return l;
                }
            }
        }

        return null;
    }
}
