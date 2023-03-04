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
package org.netbeans.modules.csl.api;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Action;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.parsing.api.Embedding;

import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.editor.InstantRenamePerformer;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * This file is originally from Retouche, the Java Support
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible.
 *
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 * @deprecated use {@link CslActions#createInstantRenameAction() } instead.
 */
@Deprecated
public class InstantRenameAction extends BaseAction {
    /** Creates a new instance of InstantRenameAction */
    public InstantRenameAction() {
        super("in-place-refactoring", MAGIC_POSITION_RESET | UNDO_MERGE_RESET); //NOI18N
    }

    public @Override void actionPerformed(ActionEvent evt, final JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();
            final String ident = Utilities.getIdentifier(Utilities.getDocument(target), caret);

            if (ident == null) {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenameAction.class, "InstantRenameDenied"));
                return;
            }

            if (IndexingManager.getDefault().isIndexing()) {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenameAction.class, "scanning-in-progress"));
                return;
            }
            Source js = Source.create(target.getDocument());
            if (js == null) {
                return;
            }

            final Set<OffsetRange>[] changePoints = new Set[1];
            final AtomicInteger changed = new AtomicInteger(0);

            DocumentListener dl = new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    changed.compareAndSet(0, 1);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    changed.compareAndSet(0, 1);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    // ignore attr changes
                }
                
            };
            target.getDocument().addDocumentListener(dl);
            
            final InstantRenamer[] renamer = new InstantRenamer[1];
            try {
                do {
                    changed.set(0);
                    ParserManager.parse (
                        Collections.<Source> singleton (js), 
                        new UserTask () {
                            public @Override void run (ResultIterator resultIterator) throws Exception {
                                Map<String, Parser.Result> embeddedResults = new HashMap<String, Parser.Result>();
                                outer:for(;;) {
                                    final Result parserResult = resultIterator.getParserResult();
                                    if (parserResult == null) {
                                        return;
                                    }
                                    embeddedResults.put(parserResult.getSnapshot().getMimeType(),
                                            resultIterator.getParserResult());
                                    String inheritedType = parserResult.getSnapshot().getMimePath().getInheritedType();
                                    if (inheritedType != null) {
                                        embeddedResults.put(inheritedType, resultIterator.getParserResult());
                                    }
                                    Iterable<Embedding> embeddings = resultIterator.getEmbeddings();
                                    for(Embedding e : embeddings) {
                                        if(e.containsOriginalOffset(caret)) {
                                            resultIterator = resultIterator.getResultIterator(e);
                                            continue outer;
                                        }
                                    }
                                    break;
                                }

                                List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(target.getDocument(), caret);
                                for (Language language : list) {
                                    if (language.getInstantRenamer() != null) {
                                        //the parser result matching with the language is just
                                        //mimetype based, it doesn't take mimepath into account,
                                        //which I belive is ok here.
                                        Parser.Result result = embeddedResults.get(language.getMimeType());
                                        if(!(result instanceof ParserResult)) {
                                            return ;
                                        }
                                        ParserResult parserResult = (ParserResult)result;

                                        renamer[0] = language.getInstantRenamer();
                                        assert renamer[0] != null;

                                        String[] descRetValue = new String[1];

                                        if (!renamer[0].isRenameAllowed(parserResult, caret, descRetValue)) {
                                            return;
                                        }

                                        Set<OffsetRange> regions = renamer[0].getRenameRegions(parserResult, caret);

                                        if ((regions != null) && (regions.size() > 0)) {
                                            changePoints[0] = regions;
                                        }

                                        break; //the for break
                                    }
                                }
                            }
                        }
                    );

                    if (changePoints[0] != null) {
                        final BadLocationException[] exc = new BadLocationException[1];
                        final Document baseDoc = target.getDocument();
                        baseDoc.render(new Runnable() {
                            public void run() {
                                try {
                                    // writers are now locked out, check mod flag:
                                    if (changed.get() == 0) {
                                        // sanity check the regions against snaphost size, see #227890; OffsetRange contains document positions.
                                        // if no document change happened, then offsets must be correct and within doc bounds.
                                        int maxLen = baseDoc.getLength();
                                        for (OffsetRange r : changePoints[0]) {
                                            if (r.getStart() >= maxLen || r.getEnd() >= maxLen) {
                                                throw new IllegalArgumentException("Bad OffsetRange provided by " + renamer[0] + ": " + r + ", docLen=" + maxLen);
                                            }
                                        }
                                        doInstantRename(changePoints[0], target, caret, ident);
                                        // don't loop even if there's a modification
                                        changed.set(2);
                                    }
                                } catch (BadLocationException ex) {
                                    exc[0] = ex;
                                }
                            }
                        });
                        if (exc[0] != null) {
                            throw exc[0];
                        }
                    } else {
                        doFullRename((EditorCookie)DataLoadersBridge.getDefault().getCookie(target,EditorCookie.class), DataLoadersBridge.getDefault().getNodeDelegate(target));
                        break;
                    }
                } while (changed.get() == 1);
            } finally {
                target.getDocument().removeDocumentListener(dl);
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } catch (ParseException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    @Override
    protected Class getShortDescriptionBundleClass() {
        return InstantRenameAction.class;
    }

    private void doInstantRename(Set<OffsetRange> changePoints, JTextComponent target, int caret,
        String ident) throws BadLocationException {
        InstantRenamePerformer.performInstantRename(target, changePoints, caret);
    }

    private void doFullRename(EditorCookie ec, Node n) {
        InstanceContent ic = new InstanceContent();
        ic.add(ec);
        ic.add(n);

        Lookup actionContext = new AbstractLookup(ic);

        Action a =
            RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
}
