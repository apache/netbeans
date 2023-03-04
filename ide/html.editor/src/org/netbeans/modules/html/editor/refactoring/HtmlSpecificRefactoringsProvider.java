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

package org.netbeans.modules.html.editor.refactoring;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;


/**
 *
 * @author marekfukala
 */
@org.openide.util.lookup.ServiceProvider(service=HtmlSpecificActionsImplementationProvider.class, position=150)
public class HtmlSpecificRefactoringsProvider extends HtmlSpecificActionsImplementationProvider {

    @Override
    public boolean canExtractInlineStyle(Lookup lookup) {
        //the editor cookie is in the lookup only if the file is opened in the editor and is active
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if(ec == null) {
            return false;
        }

        //non-blocking call, return null if no document pane initialized yet
        JEditorPane pane = NbDocument.findRecentEditorPane(ec);
        if(pane == null) {
            return false;
        }
        return canExtractInlineStyle(ec.getDocument(), new OffsetRange(pane.getSelectionStart(), pane.getSelectionEnd()));
    }
    
    public boolean canExtractInlineStyle(Document doc, OffsetRange range) {
        OffsetRange adjusted = adjustContextRange(doc, range.getStart(), range.getEnd());

        //enable the action if there are some inlined styles in the selection or at the caret position
        return !RefactoringContext.findInlinedStyles(doc, adjusted.getStart(), adjusted.getEnd()).isEmpty();
    }

    @Override
    public void doExtractInlineStyle(Lookup lookup) {
        //the editor cookie is in the lookup only if the file is opened in the editor and is active
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if(ec == null) {
            return;
        }
        JEditorPane[] panes = ec.getOpenedPanes();
        if(panes == null || panes.length == 0) {
            return;
        }
        JEditorPane pane = panes[0]; //get first pane, I hope the activated one is first
        if(pane == null) {
            return ;
        }
        final Document doc = ec.getDocument();
        final FileObject file = DataLoadersBridge.getDefault().getFileObject(doc);
        assert file != null;

        //widen the context range to tags' start/end if only partially selected
        final OffsetRange adjusted = adjustContextRange(doc, pane.getSelectionStart(), pane.getSelectionEnd());

        //RefactoringContext.create() may take a long time, should run off the AWT thread
        //show wait cursor
        final Frame main = WindowManager.getDefault().getMainWindow();
        final Runnable setDefaultCursorTask = new Runnable() {

            @Override
            public void run() {
                main.setCursor(null);
            }

        };

        main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                try {
                    //create the refactoring context - time consuming task
                    final RefactoringContext context = RefactoringContext.create(file, doc, adjusted.getStart(), adjusted.getEnd());

                    //open the UI in AWT
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                UI.openRefactoringUI(new ExtractInlinedStyleRefactoringUI(context));
                            } finally {
                                //set the original cursor
                                setDefaultCursorTask.run();
                            }
                        }
                        
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (Throwable t) {
                    //set the original cursor if the task fails so the subsequent
                    //swing task doesn't run
                    SwingUtilities.invokeLater(setDefaultCursorTask);
                }
            }

        });
        
    }

    private static OffsetRange adjustContextRange(final Document doc, final int from, final int to) {
        final AtomicReference<OffsetRange> ret = new AtomicReference<>();
        ret.set(new OffsetRange(from, to)); //return the same pair by default
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, from);
                if(ts == null) {
                    //no html token sequence at the offset, try to 
                    //TODO possibly try to travese the top level sequence backward
                    //and try to find an html embedding.
                    return ;
                }
                Token<HTMLTokenId> openTag = Utils.findTagOpenToken(ts);
                if(openTag == null) {
                    return ;
                }
                int adjustedFrom = ts.offset();

                //now try to find the tag's end
                ts.move(to);
                int adjustedTo = -1;
                while(ts.moveNext()) {
                    Token<HTMLTokenId> t = ts.token();
                    if(t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                        adjustedTo = ts.offset() + t.length();
                        break;
                    } else if(t.id() == HTMLTokenId.TEXT) {
                        //do not go too far - out of the tag
                        break;
                    }
                }

                if(adjustedTo == -1) {
                    return ;
                }

                //we found the adjusted range
                ret.set(new OffsetRange(adjustedFrom, adjustedTo));

            }

        });
        return ret.get();
    }

    
}
