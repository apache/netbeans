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
package org.netbeans.modules.jakarta.web.beans.completion;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.BLOCK_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.JAVADOC_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.LINE_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.WHITESPACE;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
//import org.netbeans.modules.jakarta.web.beans.BeansDataLoader;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author sp153251
 */
@MimeRegistration(mimeType = "text/x-beans-jakarta+xml", service = CompletionProvider.class)//NOI18N
public class BeansCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE && queryType !=CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new BeansCompletionQuery(queryType, component, component.getSelectionStart(), true), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;//will not appear automatically
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset)
            throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1)
                        + ") on doc of length: " + doc.getLength(), start).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

    static class BeansCompletionQuery extends AsyncCompletionQuery {

        private ArrayList<CompletionContextResolver> resolvers;
        private byte hasAdditionalItems = 0; //no additional items
        private int anchorOffset;
        private int queryType;

        public BeansCompletionQuery(int queryType, JTextComponent component, int caretOffset, boolean hasTask) {
            this.queryType = queryType;
            initResolvers();
        }

        private void initResolvers() {
            //XXX temporary - should be registered somehow better
            resolvers = new ArrayList<CompletionContextResolver>();
            //resolvers.add(new DBCompletionContextResolver());
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            List<BeansCompletionItem> completionItems = new ArrayList<BeansCompletionItem>();

            int anchorOffset = getCompletionItems(doc, caretOffset, completionItems);
            resultSet.addAllItems(completionItems);
            if (anchorOffset != -1) {
                resultSet.setAnchorOffset(anchorOffset);
            }

            resultSet.finish();
        }

        // This method is here for Unit testing purpose
        int getCompletionItems(Document doc, int caretOffset, List<BeansCompletionItem> completionItems) {

            int anchorOffset = -1;
            CompletionContext context = new CompletionContext(doc, caretOffset);

            if (context.getCompletionType() == CompletionContext.CompletionType.NONE) {
                return anchorOffset;
            }

            switch (context.getCompletionType()) {
//                case ATTRIBUTE_VALUE:
//                    anchorOffset = BeansCompletionManager.getDefault().completeAttributeValues(context, completionItems);
//                    break;
//                case ATTRIBUTE:
//                    anchorOffset = BeansCompletionManager.getDefault().completeAttributes(context, completionItems);
//                    break;
//                case TAG:
//                    anchorOffset = BeansCompletionManager.getDefault().completeElements(context, completionItems);
//                    break;
                case VALUE:
                    anchorOffset = BeansCompletionManager.getDefault().completeValues(context, completionItems);
                    break;
            }

            return anchorOffset;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            return false;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                resultSet.setAnchorOffset(anchorOffset);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            resultSet.finish();
        }
    }
    public final class Context {

        /**
         * Text component
         */
        private JTextComponent component;
        CompilationController controller;
        /**
         * End position of the scanning - usually the caret position
         */
        private int endOffset;
        //private PersistenceUnit[] pus;
        //private EntityMappings emaps;
        private String completedMemberName, completedMemberJavaClassName;
        private CCParser CCParser;
        private CCParser.CC parsednn = null;
        private CCParser.MD methodName = null;

        public Context(JTextComponent component, CompilationController controller, int endOffset, boolean autoPopup) {
            this.component = component;
            this.controller = controller;
            this.endOffset = endOffset;

            FileObject documentFO = getFileObject();
            if (documentFO != null) {
//                try {
//                    this.pus = PersistenceUtils.getPersistenceUnits(documentFO);
//                } catch (IOException e) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//                }
            }

            this.CCParser = new CCParser(controller);
        }

        /**
         * Must be run under MDR transaction!
         */
        public javax.lang.model.element.Element getJavaClass() {
            TreePath path = null;
//            try {
//                path = getCompletionTreePath(getController(), endOffset, COMPLETION_QUERY_TYPE);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
            javax.lang.model.element.Element el = null;
            try {
                getController().toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            while ((el == null || !(ElementKind.CLASS == el.getKind() || ElementKind.INTERFACE == el.getKind())) && path != null) {
                path.getCompilationUnit().getTypeDecls();
                el = getController().getTrees().getElement(path);
                path = path.getParentPath();
            }
            return el;
        }

        public BaseDocument getBaseDocument() {
            BaseDocument doc = (BaseDocument) component.getDocument();
            return doc;
        }

        public FileObject getFileObject() {
            try {
                return URLMapper.findFileObject(getController().getCompilationUnit().getSourceFile().toUri().toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        /**
         * @return an arrat of PUs which this sourcefile belongs to.
         */
//        public PersistenceUnit[] getPersistenceUnits() {
//            return this.pus;
//        }
//
//        public EntityMappings getEntityMappings() {
//            if (emaps == null) {
//                FileObject documentFO = getFileObject();
//                this.emaps = PersistenceUtils.getEntityMappings(documentFO);
//            }
//            return this.emaps;
//        }

        public int getCompletionOffset() {
            return endOffset;
        }

        public CCParser.CC getParsedAnnotation() {
            synchronized (CCParser) {
                if (parsednn == null) {
                    parsednn = CCParser.parseAnnotation(getCompletionOffset());
                }
                return parsednn;
            }
        }

        public String getCompletedMemberClassName() {
            if (completedMemberJavaClassName == null) {
                initCompletedMemberContext();
            }
            return completedMemberJavaClassName;
        }

        public String getCompletedMemberName() {
            if (completedMemberName == null) {
                initCompletedMemberContext();
            }
            return completedMemberName;
        }

        private void initCompletedMemberContext() {
            //parse the text behind the cursor and try to find identifiers.
            //it seems to be impossible to use JMI model for this since it havily
            //relies on the state of the source (whether it contains errors, which types etc.)
            String type = null;
            String genericType = null;
            String propertyName = null;
            CCParser nnp = new CCParser(getController()); //helper parser

            TokenSequence<JavaTokenId> ts = getController().getTokenHierarchy().tokenSequence(JavaTokenId.language());
            ts.move(getCompletionOffset() + 1);
            nextNonWhitespaceToken(ts);
            Token<JavaTokenId> ti = ts.token();
            while (ti != null && propertyName == null) {
                javax.lang.model.element.Element el = null;
//                try {
//                    el = getController().getTrees().getElement(getCompletionTreePath(getController(), ts.offset() + 1, CompletionProvider.COMPLETION_QUERY_TYPE));
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
                //skip all annotations between the CC offset and the completed member
                if (el!=null && el.getKind() == ElementKind.ANNOTATION_TYPE) {
                    //parse to find NN end
                    CCParser.CC parsed = nnp.parseAnnotation(ts.offset() + 1);
                    if (parsed != null) {
                        //parse after the NN end (skip)
                        ts.move(parsed.getEndOffset());
                        ti = ts.token();
                        continue;
                    }
                }

                //test whether we have just found a type and '<' character after
                if (genericType != null && ti.id() == JavaTokenId.LT) {
                    //maybe a start of generic
                    ts.moveNext();
                    Token<JavaTokenId> ti2 = ts.token();
                    if (ti2.id() == JavaTokenId.IDENTIFIER) {
                        //found generic
                        //genericType = ti2.getImage();
                        //ti = ti.getNext(); //skip the next IDENTIFIER token so it is not considered as property name
                    } else {
                        //false alarm
                        genericType = null;
                    }
                } else if (ti.id() == JavaTokenId.IDENTIFIER) {
                    if (type == null) {
                        //type = ti.getImage();
                        genericType = type;
                    } else {
                        //propertyName = ti.getImage();
                    }
                }
                ts.moveNext();
                ti = ts.token();
            }

            completedMemberName = propertyName;
            completedMemberJavaClassName = genericType == null ? type : genericType;
        }

        private void initMethodContext() {
            TokenSequence<JavaTokenId> ts = getController().getTokenHierarchy().tokenSequence(JavaTokenId.language());
            ts.move(getCompletionOffset());
            previousNonWhitespaceToken(ts);
            Token<JavaTokenId> ti = ts.token();
            int lparpassed = 0;
            String mname = null;
            while (ti != null) {
                javax.lang.model.element.Element el = null;
                if (ti.id() == JavaTokenId.LPAREN) {
                    lparpassed++;
                } else if (ti.id() == JavaTokenId.IDENTIFIER) {
                    break;//so far we have only simple model for method parameters without identifier checks
                } else if (ti.id() == JavaTokenId.RPAREN) {
                    lparpassed--;
                }
//                try {
//                    el = getController().getTrees().getElement(getCompletionTreePath(getController(), ts.offset(), CompletionProvider.COMPLETION_QUERY_TYPE));
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
                //
                if (lparpassed > 0) {
                    if (el != null && el.getKind() == ElementKind.METHOD) {//we insde parameters section
                        //parse to find NN end
                        mname = el.getSimpleName().toString();
                        break;
                    } else if (el != null && el.getKind() == ElementKind.CLASS && el.asType().getKind() == TypeKind.ERROR && (el.asType().toString().indexOf('.') > 0 && el.asType().toString().indexOf('.') < (el.asType().toString().length() - 1))) {//NOI18N
                        mname = el.getSimpleName().toString();//supposed method name in case of error
                        break;
                    } else {
                        break;
                    }
                }

                //

                if (!ts.movePrevious()) {
                    break;
                }
                ti = ts.token();
            }
            if (mname != null) {
                Token<JavaTokenId> literalToComplete = null;
                Token<JavaTokenId> titk = ts.token();
                JavaTokenId id;
                do {
                    id = titk.id();
                    //ignore whitespaces
                    if (id == JavaTokenId.WHITESPACE || id == JavaTokenId.LINE_COMMENT || id == JavaTokenId.BLOCK_COMMENT || id == JavaTokenId.JAVADOC_COMMENT) {
                        if (!ts.moveNext()) {
                            break;
                        }
                        titk = ts.token();
                        continue;
                    }
                    int tokenOffset = titk.offset(getController().getTokenHierarchy());
                    if(tokenOffset>getCompletionOffset()){

                        break;
                    }

                    if(id == JavaTokenId.STRING_LITERAL){
                        if((tokenOffset + titk.length())>getCompletionOffset()){
                            //we complete this literal
                            literalToComplete = titk;
                            break;
                        }
                    }

                    if (!ts.moveNext()) {
                        break;
                    }
                    titk = ts.token();//get next token

                } while (titk != null);
                methodName = new CCParser.MD(mname, literalToComplete != null ? literalToComplete.text().toString() : null, literalToComplete != null ? literalToComplete.offset(getController().getTokenHierarchy()) : getCompletionOffset(), true, true);
            }
        }

        private TokenSequence<JavaTokenId> nextNonWhitespaceToken(TokenSequence<JavaTokenId> ts) {
            while (ts.moveNext()) {
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case JAVADOC_COMMENT:
                        break;
                    default:
                        return ts;
                }
            }
            return null;
        }

        private TokenSequence<JavaTokenId> previousNonWhitespaceToken(TokenSequence<JavaTokenId> ts) {
            do {
                if (ts.token() != null) {
                    switch (ts.token().id()) {
                        case WHITESPACE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case JAVADOC_COMMENT:
                            break;
                        default:
                            return ts;
                    }
                }
            } while (ts.movePrevious());
            return null;
        }

        /**
         * @return the controller
         */
        public CompilationController getController() {
            return controller;
        }

        public CCParser.MD getMethod() {
            if (methodName == null) {
                initMethodContext();
            }
            return methodName;
        }
    }
    private static final String EMPTY = ""; //NOI18N
}
