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
package org.netbeans.modules.web.core.syntax;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.web.jsps.parserapi.TagFileInfo;
import org.netbeans.modules.web.jsps.parserapi.TagInfo;
import org.netbeans.modules.web.jsps.parserapi.TagLibraryInfo;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 * @author Marek.Fukala@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class JspHyperlinkProvider implements HyperlinkProvider {

    /**
     * Should determine whether there should be a hyperlink on the given offset
     * in the given document. May be called any number of times for given
     * parameters.
     * <br>
     * This method is called from event dispatch thread. It should run very fast
     * as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt;
     * doc.getLength(), but the implementations should not depend on it)
     * @return true if the provided offset should be in a hyperlink false
     * otherwise
     */
    @Override
    public boolean isHyperlinkPoint(Document doc, final int offset) {
        if (!(doc instanceof BaseDocument)) {
            return false;
        }

        final BaseDocument bdoc = (BaseDocument) doc;
        final AtomicBoolean result = new AtomicBoolean();
        bdoc.render(new Runnable() {

            @Override
            public void run() {
                try {
                    JTextComponent target = Utilities.getFocusedComponent();

                    if (target == null || target.getDocument() != bdoc) {
                        return;
                    }

                    JspSyntaxSupport jspSup = JspSyntaxSupport.get(bdoc);

                    TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(bdoc);
                    TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
                    if (tokenSequence == null) {
                        return;
                    }
                    tokenSequence.move(offset);
                    if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                        return; //no token found
                    }
                    Token<?> token = tokenSequence.token();

                    if (token.id() == JspTokenId.ATTR_VALUE) {
                        SyntaxElement syntaxElement = jspSup.getElementChain(offset);
                        if (syntaxElement != null) {
                            if (syntaxElement.getCompletionContext()
                                    == JspSyntaxSupport.DIRECTIVE_COMPLETION_CONTEXT) {
                                // <%@include file="xxx"%> hyperlink usecase
                                SyntaxElement.Directive sed = (SyntaxElement.Directive) syntaxElement;
                                if ("include".equals(sed.getName())) {
                                    result.set(containsAttribute(tokenSequence, "file"));
                                } else if ("page".equals(sed.getName())) {
                                    result.set(containsAttribute(tokenSequence, "errorPage"));
                                }
                                return;
                            }
                            if (syntaxElement.getCompletionContext()
                                    == JspSyntaxSupport.TAG_COMPLETION_CONTEXT) {
                                //find attribute name
                                while (tokenSequence.movePrevious()
                                        && tokenSequence.token().id() != JspTokenId.TAG) {
                                    if (tokenSequence.token().id() == JspTokenId.ATTRIBUTE) {
                                        String attributeName = tokenSequence.token().
                                                text().toString();
                                        String tagName = ((SyntaxElement.Tag) syntaxElement).
                                                getName();

                                        if ("jsp:include".equals(tagName)
                                                && "page".equals(attributeName)) {
                                            //<jsp:include page="xxx"/> usecase
                                            result.set(true);
                                            return;
                                        }
                                        if ("jsp:forward".equals(tagName)
                                                && "page".equals(attributeName)) {
                                            //<jsp:forward page="xxx"/> usecase
                                            result.set(true);
                                            return;
                                        }
                                        if ("jsp:useBean".equals(tagName)
                                                && ("type".equals(attributeName)
                                                || "class".equals(attributeName))) {
                                            //<jsp:useBean class="xxx" type="yyy"/> usecase
                                            result.set(true);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }

//            // is it a bean in EL?
//            tokenSequence.move(offset); //reset tokenSequence
//            if(!tokenSequence.moveNext()) {
//                return false; //no token
//            }
//            TokenSequence<ELTokenId> elTokenSequence =
//                tokenSequence.embedded(ELTokenId.language());
//            if (elTokenSequence != null){
//                //check expression language
// 		elTokenSequence.move(offset);
//		if (!elTokenSequence.moveNext()) {
//		    return false;
//		}
//
//		if(elTokenSequence.token().id() == ELTokenId.IDENTIFIER) {
//                    return true;
//                }
//            }
                    // is the a reachable tag file?
                    result.set(canBeTagFile(tokenSequence, jspSup));

                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }

            }

        });

        return result.get();
    }

    private boolean containsAttribute(TokenSequence<?> tokenSequence,
            String attributeName) {
        //find attribute name
        while (tokenSequence.movePrevious() && tokenSequence.token().id() != JspTokenId.TAG) {
            if (tokenSequence.token().id() == JspTokenId.ATTRIBUTE) {
                return tokenSequence.token().text().toString().equals(attributeName);
            }
        }
        return false;
    }

    /**
     * Should determine the span of hyperlink on given offset. Generally, if
     * isHyperlinkPoint returns true for a given parameters, this class should
     * return a valid span, but it is not strictly required.
     * <br>
     * This method is called from event dispatch thread. This method should run
     * very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt;
     * doc.getLength(), but the implementations should not depend on it)
     * @return a two member array which contains starting and ending offset of a
     * hyperlink that should be on a given offset
     */
    @Override
    public int[] getHyperlinkSpan(final Document doc, final int offset) {
        final AtomicReference<Callable<int[]>> returnTaskRef = new AtomicReference<Callable<int[]>>();
        doc.render(new Runnable() {

            @Override
            public void run() {
                BaseDocument bdoc = (BaseDocument) doc;
                JTextComponent target = Utilities.getFocusedComponent();

                if (target == null || target.getDocument() != bdoc) {
                    return;
                }

                final JspSyntaxSupport jspSup = JspSyntaxSupport.get(bdoc);

                TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(bdoc);
                TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
                tokenSequence.move(offset);
                if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                    return; //no token found
                }
                Token<?> token = tokenSequence.token();

                if (canBeTagFile(tokenSequence, jspSup)) {
                    // a reachable tag file.
                    int start = token.offset(tokenHierarchy);
                    final int end = token.offset(tokenHierarchy) + token.length();
                    String text = token.text().toString().trim();
                    if (text.startsWith("<")) { //NOI18N
                        start = start + 1;
                    }
                    final int fstart = start;
                    returnTaskRef.set(new Callable<int[]>() {

                        @Override
                        public int[] call() throws Exception {
                            return new int[]{fstart, end};
                        }
                    });
                } else {
//                    // is it a bean in EL ?
//                    final TokenSequence<ELTokenId> elTokenSequence = tokenSequence.embedded(
//                            ELTokenId.language());
//
//                    if (elTokenSequence != null){
//                            elTokenSequence.move(offset);
//                            if (!elTokenSequence.moveNext()) {
//                                return; //no token
//                            }
//                            try {
//                                final int elEnd = elTokenSequence.offset() + elTokenSequence.token().length();
//                                final JspELExpression exp = new JspELExpression(jspSup, elEnd);
//
//                                returnTaskRef.set(new Callable<int[]>() {
//                                    @Override
//                                    public int[] call() throws Exception {
//                                        int res = exp.parse();
//                                        if (res == ELExpression.EL_BEAN || res == ELExpression.EL_START) {
//                                            return new int[]{elTokenSequence.offset(), elEnd};
//                                        }
//                                        return null;
//                                    }
//                                });
//
//                            } catch (BadLocationException ex) {
//                                Exceptions.printStackTrace(ex);
//                            }
//
//                    }

                    //the token image always contains the quotation marks e.g. "test.css"
                    if (token.length() > 2) {
                        //there is somethin between the qutation marks
                        final int from = token.offset(tokenHierarchy) + 1;
                        final int to = token.offset(tokenHierarchy) + token.length() - 1;
                        returnTaskRef.set(new Callable<int[]>() {
                            @Override
                            public int[] call() throws Exception {
                                return new int[]{from, to};
                            }
                        });
                    } else {
                        //empty value
                    }
                }

            }

        });

        try {
            Callable<int[]> returnTask = returnTaskRef.get();
            return returnTask == null ? null : returnTask.call();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

    }

    /**
     * The implementor should perform an action corresponding to clicking on the
     * hyperlink on the given offset. The nature of the action is given by the
     * nature of given hyperlink, but generally should open some resource or
     * move cursor to certain place in the current document.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt;
     * doc.getLength(), but the implementations should not depend on it)
     */
    @Override
    public void performClickAction(final Document doc, final int offset) {
        final JTextComponent target = Utilities.getFocusedComponent();
        if (target == null || target.getDocument() != doc) {
            return;
        }

        final AtomicReference<Runnable> outOfDocumentLockTaskRef = new AtomicReference<Runnable>();
        doc.render(new Runnable() {

            @Override
            public void run() {
                final JspSyntaxSupport jspSup = JspSyntaxSupport.get(doc);

                TokenHierarchy<Document> tokenHierarchy
                        = TokenHierarchy.get(doc);
                TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
                tokenSequence.move(offset);
                if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                    return; //no token found
                }
                Token<?> token = tokenSequence.token();

//                // is it a bean in EL
//                TokenSequence<ELTokenId> elTokenSequence =
//                        tokenSequence.embedded(ELTokenId.language());
//                if (elTokenSequence != null) {
//
//                    elTokenSequence.move(offset);
//                    if (!elTokenSequence.moveNext()) {
//                        return;//not token
//                    }
//
//                    final int parseOffset = elTokenSequence.offset()
//                            + elTokenSequence.token().length();
//                    final String beanName = elTokenSequence.token().text().toString();
//                    try {
//                        final JspELExpression exp = new JspELExpression(jspSup, parseOffset);
//
//                        outOfDocumentLockTaskRef.set(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                try {
//                                    int res = exp.parse();
//                                    if (res == ELExpression.EL_START) {
//                                        navigateToUserBeanDef(doc, jspSup, target, beanName);
//                                    }
//                                    if (res == ELExpression.EL_BEAN) {
//                                        if (!exp.gotoPropertyDeclaration(exp.getObjectClass())) {
//                                            gotoSourceFailed();
//                                        }
//                                    }
//                                } catch (BadLocationException ex) {
//                                    Exceptions.printStackTrace(ex);
//                                }
//                            }
//                        });
//
//                    } catch (BadLocationException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                    return;
//                }
                // is ti declaration of userBean?
                while (tokenSequence.token().id() != JspTokenId.TAG
                        && !"jsp:useBean".equals(tokenSequence.token().text().toString())
                        && tokenSequence.movePrevious()) {
                    //do nothing, just skip the tokens
                }

                if (tokenSequence.index() != -1 && tokenSequence.token().id()
                        == JspTokenId.TAG) {
                    //we are in useBean
                    final String className = token.text().toString().substring(1,
                            token.length() - 1).trim();

                    //compute the type off awt
                    ClasspathInfo cpInfo = ClasspathInfo.create(jspSup.getFileObject());
                    JavaSource source = JavaSource.create(cpInfo, Collections.<FileObject>emptyList());

                    AtomicBoolean cancel = new AtomicBoolean();
                    Compute<TypeElement> compute = new Compute<TypeElement>(cancel,
                            source,
                            Phase.ELEMENTS_RESOLVED,
                            new Worker<TypeElement>() {

                                @Override
                                public TypeElement process(CompilationInfo info) {
                                    return info.getElements().getTypeElement(className);
                                }
                            });

                    BaseProgressUtils.runOffEventDispatchThread(compute,
                            NbBundle.getMessage(JspHyperlinkProvider.class, "MSG_goto-source"),
                            cancel,
                            false);

                    if (cancel.get()) {
                        return;
                    }

                    TypeElement typeElement = compute.result();

                    //if resolved in time limit, open it
                    if (typeElement != null) {
                        if (!UiUtils.open(cpInfo, typeElement)) {
                            gotoSourceFailed();
                        }
                    }

                }

                tokenSequence.move(offset);//reset tokenSequence
                if (!tokenSequence.moveNext()) {
                    return; //no token
                }

                FileObject fObj = getTagFile(tokenSequence, jspSup);
                if (fObj != null) {
                    openInEditor(fObj);
                } else {
                    String path = token.text().toString();
                    int openingQuotePos = path.indexOf('"');

                    if (openingQuotePos > -1) {
                        path = path.substring(openingQuotePos + 1);

                        int closingQuotePos = path.indexOf('"');

                        if (closingQuotePos > -1) {
                            path = path.substring(0, closingQuotePos);
                            fObj = JspUtils.getFileObject(doc, path);
                        }
                    }
                    if (fObj != null) {
                        openInEditor(fObj);
                    } else {
                        // when the file was not found.
                        String msg = NbBundle.getMessage(JspHyperlinkProvider.class,
                                "LBL_file_not_found", path); //NOI18N
                        StatusDisplayer.getDefault().setStatusText(msg);
                    }

                }
            }
        });

        Runnable outOfDocumentLockTask = outOfDocumentLockTaskRef.get();
        if (outOfDocumentLockTask != null) {
            outOfDocumentLockTask.run();
        }

    }

    private String getTagName(String tagwithprefix) {
        int index = tagwithprefix.indexOf(':');
        if (index > 0) {
            return tagwithprefix.substring(index + 1);
        } else {
            return tagwithprefix;
        }
    }

    private void openInEditor(FileObject fObj) {
        if (fObj != null) {
            DataObject dobj;
            try {
                dobj = DataObject.find(fObj);
            } catch (DataObjectNotFoundException e) {
                Exceptions.printStackTrace(e);
                return;
            }
            if (dobj != null) {
                Node.Cookie cookie = dobj.getLookup().lookup(EditCookie.class);
                if (cookie != null) {
                    ((EditCookie) cookie).edit();
                }
            }
        }
    }

    private boolean canBeTagFile(TokenSequence<?> tokenSequence, JspSyntaxSupport jspSup) {
        Token token = tokenSequence.token();
        if (token.id() == JspTokenId.TAG) {
            String image = token.text().toString().trim();
            if (image.startsWith("<")) {                                 // NOI18N
                image = image.substring(1).trim();
            }
            if (!image.startsWith("jsp:") && image.indexOf(':') != -1) {  // NOI18N
                return true;
            }

        }
        return false;
    }

    private FileObject getTagFile(TokenSequence<?> tokenSequence, JspSyntaxSupport jspSup) {
        Token token = tokenSequence.token();
        if (token.id() == JspTokenId.TAG) {
            String image = token.text().toString().trim();
            if (image.startsWith("<")) {                                 // NOI18N
                image = image.substring(1).trim();
            }
            if (!image.startsWith("jsp:") && image.indexOf(':') != -1) {  // NOI18N
                List l = jspSup.getTags(image);
                if (l.size() == 1) {
                    TagLibraryInfo libInfo = ((TagInfo) l.get(0)).getTagLibrary();
                    if (libInfo != null) {
                        TagFileInfo fileInfo = libInfo.getTagFile(getTagName(image));
                        if (fileInfo != null) {
                            return JspUtils.getFileObject(jspSup.getDocument(),
                                    fileInfo.getPath());
                        }
                    }
                }
            }
        }
        return null;
    }

    /* Move the cursor to the user bean definition.
     */
    private void navigateToUserBeanDef(Document doc, JspSyntaxSupport jspSup,
            JTextComponent target, String bean)
            throws BadLocationException {
        String text = doc.getText(0, doc.getLength());
        int index = text.indexOf(bean);
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();

        while (index > 0) {
            tokenSequence.move(index);
            if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                return; //no token found
            }
            Token token = tokenSequence.token();

            if (token.id() == JspTokenId.ATTR_VALUE) {

                while (!(token.id() == JspTokenId.ATTRIBUTE
                        && (token.text().toString().equals("class")
                        || token.text().toString().equals("type")))
                        && !(token.id() == JspTokenId.SYMBOL
                        && token.text().toString().equals("/>")) && tokenSequence.moveNext()) {
                    token = tokenSequence.token();
                }

                if (tokenSequence.index() != -1 && token.id() == JspTokenId.SYMBOL) {
                    while (!(token.id() == JspTokenId.ATTRIBUTE
                            && (token.text().toString().equals("class")
                            || token.text().toString().equals("type")))
                            && !(token.id() != JspTokenId.SYMBOL
                            && token.text().toString().equals("<")) && tokenSequence.movePrevious()) {
                        token = tokenSequence.token();
                    }
                }

                if (tokenSequence.index() != -1 && token.id() == JspTokenId.ATTRIBUTE) {
                    while (token.id() != JspTokenId.ATTR_VALUE && tokenSequence.moveNext()) {
                        token = tokenSequence.token();
                    }
                }

                if (tokenSequence.index() != -1 && token.id() == JspTokenId.ATTR_VALUE) {
                    target.setCaretPosition(token.offset(tokenHierarchy) + 1);
                    break;
                }
            }
            index = text.indexOf(bean, index + bean.length());
        }
    }

    @NbBundle.Messages("MSG_source_not_found=The source file was not found.")
    private void gotoSourceFailed() {
        String msg = Bundle.MSG_source_not_found();
        StatusDisplayer.getDefault().setStatusText(msg);
        Toolkit.getDefaultToolkit().beep();
    }

    private static final class Compute<T> implements Runnable, Task<CompilationController> {

        private final AtomicBoolean cancel;
        private final JavaSource source;
        private final Phase phase;
        private final Worker<T> worker;
        private T result;

        public Compute(AtomicBoolean cancel, JavaSource source, Phase phase, Worker<T> worker) {
            this.cancel = cancel;
            this.source = source;
            this.phase = phase;
            this.worker = worker;
        }

        @Override
        public void run() {
            try {
                source.runUserActionTask(this, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                result = null;
            }
        }

        @Override
        public void run(CompilationController parameter) throws Exception {
            if (cancel.get()) {
                return;
            }

            parameter.toPhase(phase);

            if (cancel.get()) {
                return;
            }

            T t = worker.process(parameter);

            if (cancel.get()) {
                return;
            }

            result = t;
        }

        public T result() {
            return result;
        }

    }

    public static interface Worker<T> {

        T process(CompilationInfo info);
    }
}
