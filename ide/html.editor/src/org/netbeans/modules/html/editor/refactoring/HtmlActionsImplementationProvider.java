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
package org.netbeans.modules.html.editor.refactoring;

import org.netbeans.modules.web.common.ui.refactoring.RenameRefactoringUI;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo;
import org.netbeans.modules.css.refactoring.api.CssRefactoringInfo.Type;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Generic rename refactoring UI for all kinds of file possibly refered from an html code.
 * The main purpose is to allow refactoring of html references to such files.
 *
 * Please look at REFACTORABLE_TYPES field to find out what mimetypes this refactoring
 * plugin registeres an UI for.
 *
 * Anyone who want to provide its own rename refactoring has to register his 
 * ActionsImplementationProvider to a lower position.
 *
 * @author marekfukala
 */
//default position=Integet.MAX_VALUE; all who wants to provide its own refactgoring UI
//for one of the registered mimetypes has to use a lower position
@ServiceProvider(service = ActionsImplementationProvider.class)
public class HtmlActionsImplementationProvider extends ActionsImplementationProvider {
    //all mimetypes which we want to register the rename refactoring ui to
    //basically the list should contain all mimetypes which can be referenced from an html file
    //since this service provider has a very high position, if one of the mimetypes has
    //its own refactoring UI registered that one will be prefered.
    private static final Collection<String> REFACTORABLE_TYPES =
            Arrays.asList(new String[]{"text/html", "text/xhtml", "text/css", "text/javascript", "text/x-json",
            "image/gif", "image/jpeg", "image/png", "image/bmp"}); //NOI18N

    @Override
    public boolean canFindUsages(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        //we are able to rename only one node selection [at least for now ;-) ]
        if (nodes.size() != 1) {
            return false;
        }

        //check if the file is a file with .css extension or represents
        //an opened file which code embeds a css content on the caret position
        Node node = nodes.iterator().next();
        return isRefactorableEditorElement(node);
    }

    @Override
    //file rename
    public boolean canRename(Lookup lookup) {
	Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
	//we are able to rename only one node selection [at least for now ;-) ]
	if (nodes.size() != 1) {
	    return false;
	}

        //apply only on supported mimetypes and if not invoked in editor context
	Node node = nodes.iterator().next();
        EditorCookie ec = getEditorCookie(node);
        if(ec == null || !isFromEditor(ec)) {
            FileObject fo = getFileObjectFromNode(node);
            return fo != null && REFACTORABLE_TYPES.contains(fo.getMIMEType());
        } else {
            return isRefactorableEditorElement(node);
        }
    }

    @Override
    public void doRename(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            openCssRefactoringUI(ec, lookup, (entryName, refactoringInfo) -> new CssRenameRefactoringUI(entryName, refactoringInfo));
        } else {
            Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
            assert nodes.size() == 1;
            Node node = nodes.iterator().next();
            FileObject file = getFileObjectFromNode(node);
            UI.openRefactoringUI(new RenameRefactoringUI(file));
        }
    }

    private void openCssRefactoringUI(EditorCookie ec, Lookup lookup, BiFunction<String, CssRefactoringInfo, RefactoringUI> uiConstructor) {
        JTextComponent textC = ec.getOpenedPanes()[0];
        StyledDocument document = (StyledDocument) textC.getDocument();
        FileObject fileObject = getFileObjectFromNode(lookup.lookupAll(Node.class).iterator().next());
        document.render(() -> {
            int caretOffset = textC.getCaretPosition();
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence<HTMLTokenId> htmlTokens = Utils.getJoinedHtmlSequence(th, caretOffset);
            htmlTokens.move(caretOffset);
            if (htmlTokens.moveNext()) {
                Token<HTMLTokenId> t = htmlTokens.token();
                if (t.id() == HTMLTokenId.VALUE_CSS) {
                    String valueType = (String) t.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
                    Type type;
                    if("id".equals(valueType)) {
                        type = Type.ID;
                    } else if ("class".equals(valueType)) {
                        type = Type.CLASS;
                    } else {
                        return;
                    }

                    CharSequence text = t.text();
                    int tokenOffset = t.offset(th);
                    int valueOffset = caretOffset - tokenOffset;
                    int minStart = 0;
                    int maxEnd = t.length();
                    if ((text.charAt(0) == '"' && text.charAt(maxEnd - 1) == '"') || (text.charAt(0) == '\'' && text.charAt(maxEnd - 1) == '\'')) {
                        minStart = 1;
                        maxEnd = t.length() - 1;
                    }

                    int start = valueOffset;
                    int end = valueOffset;
                    while (start > minStart && (!Character.isWhitespace(text.charAt(start - 1)))) {
                        start--;
                    }
                    while (end < maxEnd && (!Character.isWhitespace(text.charAt(end)))) {
                        end++;
                    }

                    String className = text.subSequence(start, end).toString();
                    UI.openRefactoringUI(uiConstructor.apply(className, new CssRefactoringInfo(fileObject, className, type)));
                }
            }
        });
    }


    @Override
    public void doFindUsages(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            openCssRefactoringUI(ec, lookup, (entryName, refactoringInfo) -> new WhereUsedUI(refactoringInfo));
        }
    }

    private static FileObject getFileObjectFromNode(Node node) {
	DataObject dobj = node.getLookup().lookup(DataObject.class);
	return dobj != null ? dobj.getPrimaryFile() : null;
    }

    private static boolean isFromEditor(final EditorCookie ec) {
        return Mutex.EVENT.readAccess((Mutex.Action<Boolean>) () -> {
            if (ec != null && ec.getOpenedPanes() != null) {
                TopComponent activetc = TopComponent.getRegistry().getActivated();
                if (activetc instanceof CloneableEditorSupport.Pane) {
                    return true;
                }
            }
            return false;
        });
    }

    private static EditorCookie getEditorCookie(Node node) {
	return node.getLookup().lookup(EditorCookie.class);
    }

    /*
     * We can't access the parser here as we may (or always are?) be called
     * in the EDT.
     */
    private static boolean isRefactorableEditorElement(final Node node) {
        final AtomicBoolean result = new AtomicBoolean(false);
        Mutex.EVENT.readAccess(() -> {
            EditorCookie ec = getEditorCookie(node);
            if (isFromEditor(ec)) {
                //check if there's css code at the offset
                final StyledDocument document = ec.getDocument();
                JEditorPane pane = ec.getOpenedPanes()[0];
                final int caret = pane.getCaretPosition();
                document.render(() -> {
                    TokenSequence<?> ts = LexerUtils.getTokenSequence(document, caret, HTMLTokenId.language(), false);
                    result.set(ts != null);
                });
            }
        });

        return result.get();
    }
}
