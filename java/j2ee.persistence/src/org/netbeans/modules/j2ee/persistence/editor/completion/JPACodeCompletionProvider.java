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
package org.netbeans.modules.j2ee.persistence.editor.completion;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.function.Consumer;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.completion.db.DBCompletionContextResolver;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * see NNCompletionProvider and NNCompletionQuery as nb 5.5 precursors for this
 * class
 *
 * @author sp153251
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class, position = 400)///,//NOI18N
public class JPACodeCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE && queryType != CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new JPACodeCompletionQuery(queryType, component.getDocument(), component.getSelectionStart(), true), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;//will not appear automatically
    }

    @MimeRegistration(mimeType = "text/x-java", service = CompletionCollector.class) //NOI18N
    public static class JPACompletionCollector implements CompletionCollector {

        @Override
        public boolean collectCompletions(Document doc, int offset, org.netbeans.api.lsp.Completion.Context context, Consumer<org.netbeans.api.lsp.Completion> consumer) {
            JPACodeCompletionQuery query = new JPACodeCompletionQuery(COMPLETION_QUERY_TYPE, doc, offset, false);
            try {
                query.doQuery(doc, offset);
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            for (JPACompletionItem result : query.results) {
                Builder builder = CompletionCollector.newBuilder(result instanceof JPACompletionItem.DBElementItem ? ((JPACompletionItem.DBElementItem) result).getName() : result.getItemText())
                        .sortText(String.format("%04d%s", result.getSortPriority(), result.getSortText()));
                consumer.accept(builder.build());
            }
            return query.hasAdditionalItems == 0;
        }
    }

    static class JPACodeCompletionQuery extends AsyncCompletionQuery {

        private ArrayList<CompletionContextResolver> resolvers;
        private List<JPACompletionItem> results;
        private byte hasAdditionalItems = 0; //no additional items
        private int anchorOffset;
        private Document doc;
        private int queryType;
        private int caretOffset;
        private boolean hasTask;

        public JPACodeCompletionQuery(int queryType, Document doc, int caretOffset, boolean hasTask) {
            this.queryType = queryType;
            this.caretOffset = caretOffset;
            this.hasTask = hasTask;
            this.doc = doc;
            initResolvers();
        }

        private void initResolvers() {
            //XXX temporary - should be registered somehow better
            resolvers = new ArrayList<CompletionContextResolver>();
            resolvers.add(new DBCompletionContextResolver());
            resolvers.add(new ETCompletionContextResolver());
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                doQuery(doc, caretOffset);
                if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                    if (results != null) {
                        resultSet.addAllItems(results);
                    }
                    resultSet.setHasAdditionalItems(hasAdditionalItems > 0);
                    if (hasAdditionalItems == 1) {
                        resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JPACodeCompletionProvider.class, "JCP-imported-items")); //NOI18N
                    }
                    if (hasAdditionalItems == 2) {
                        resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JPACodeCompletionProvider.class, "JCP-instance-members")); //NOI18N
                    }
                }
                if (anchorOffset > -1) {
                    resultSet.setAnchorOffset(anchorOffset);
                }
            } catch (MissingResourceException | ParseException e) {
                Exceptions.printStackTrace(e);
            } finally {
                resultSet.finish();
            }
        }

        private void doQuery(Document doc, int caretOffset) throws ParseException {
            this.caretOffset = caretOffset;
            results = null;
            anchorOffset = -1;
            Source source = Source.create(doc);
            if (source != null) {
                ParserManager.parse(Collections.singletonList(source), getTask());
            }
        }

        private UserTask getTask() {
            return new Task();
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            return false;//TODO: implement filter
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                    if (results != null) {
                        Completion.get().hideDocumentation();
                        Completion.get().hideCompletion();
                    }
                }
                resultSet.setAnchorOffset(anchorOffset);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            resultSet.finish();
        }

        private Collection getFilteredData(Collection<JPACompletionItem> data, String prefix) {
            if (prefix.length() == 0) {
                return data;
            }
            List<CompletionItem> ret = new ArrayList<>();
            for (Iterator<JPACompletionItem> it = data.iterator(); it.hasNext();) {
                CompletionItem itm = it.next();
                if (itm.getInsertPrefix().toString().startsWith(prefix)) {
                    ret.add(itm);
                }
            }
            return ret;
        }

        private void run(CompilationController controller) {
            if (!hasTask || !isTaskCancelled()){
                int startOffset = caretOffset;
                Iterator<CompletionContextResolver> resolversItr = resolvers.iterator();
                TreePath env = null;
                try {
                    env = getCompletionTreePath(controller, caretOffset, CompletionProvider.COMPLETION_QUERY_TYPE);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (env == null) {
                    return;
                }
                results = new ArrayList<JPACompletionItem>();
                while (resolversItr.hasNext()) {
                    CompletionContextResolver resolver = resolversItr.next();
                    TaskUserAction task = new TaskUserAction(controller, resolver, startOffset);
                    try {
                        EntityClassScope scope = EntityClassScope.getEntityClassScope(URLMapper.findFileObject(controller.getCompilationUnit().getSourceFile().toUri().toURL()));
                        MetadataModel<EntityMappingsMetadata> entityMappingsModel = null;
                        if (scope != null) {
                            entityMappingsModel = scope.getEntityMappingsModel(false); // false since I guess you only want the entity classes defined in the project
                        }
                        if (entityMappingsModel != null) {
                            entityMappingsModel.runReadAction(task);
                        }
                    } catch (IOException ex) {
                    }

                    if (!task.isValid()) {
                        break;
                    }
                }
            }
        }

        private class TaskUserAction implements MetadataModelAction<EntityMappingsMetadata, Boolean> {

            private final CompilationController controller;
            private final CompletionContextResolver resolver;
            private final int startOffset;
            private boolean valid;

            private TaskUserAction(CompilationController controller, CompletionContextResolver resolver, int startOffset) {
                this.controller = controller;
                this.resolver = resolver;
                this.startOffset = startOffset;
                valid = false;
            }

            public boolean isValid() {
                return valid;
            }

            @Override
            public Boolean run(EntityMappingsMetadata metadata) throws Exception {
                Context ctx = new Context(doc, controller, startOffset, false);
                if (ctx.getEntityMappings() == null) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "No EnitityMappings defined.");
                } else {
                    results.addAll(resolver.resolve(ctx));
                    valid = true;
                }
                return valid;
            }
        }

        private class Task extends UserTask {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult(caretOffset);
                CompilationController controller = result != null ? CompilationController.get(result) : null;
                if (controller != null) {
                    JPACodeCompletionQuery.this.run(controller);
                }
            }
        }
    }
    //

    private static TreePath getCompletionTreePath(CompilationController controller, int curOffset, int queryType) throws IOException {
        controller.toPhase(Phase.PARSED);
        int offset = controller.getSnapshot().getEmbeddedOffset(curOffset);
        if (offset < 0) {
            return null;
        }
        boolean complQuery = (queryType & COMPLETION_QUERY_TYPE) != 0;
        if (offset > 0) {
            if (complQuery) {
                TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                // When right at the token end move to previous token; otherwise move to the token that "contains" the offset
                if (ts.move(offset) == 0 || !ts.moveNext()) {
                    ts.movePrevious();
                }
                int len = offset - ts.offset();
                if (len > 0 && (ts.token().id() == JavaTokenId.IDENTIFIER
                        || ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                        ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                        ts.token().id().primaryCategory().equals("literal")) //NOI18N
                        && ts.token().length() >= len) { //TODO: Use isKeyword(...) when available
                    offset = ts.offset();
                }
            } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                // When right at the token start move offset to the position "inside" the token
                ts.move(offset);
                if (!ts.moveNext()) {
                    ts.movePrevious();
                }
                if (ts.offset() == offset && ts.token().length() > 0
                        && (ts.token().id() == JavaTokenId.IDENTIFIER
                        || ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                        ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                        ts.token().id().primaryCategory().equals("literal"))) { //NOI18N
                    offset++;
                }
            }
        }
        TreePath path = controller.getTreeUtilities().pathFor(offset);
        return path;
    }

    public static final class Context {

        /**
         * Document
         */
        private Document doc;
        CompilationController controller;
        /**
         * End position of the scanning - usually the caret position
         */
        private int endOffset;
        private PersistenceUnit[] pus;
        private EntityMappings emaps;
        private String completedMemberName, completedMemberJavaClassName;
        private CCParser CCParser;
        private CCParser.CC parsednn = null;
        private CCParser.MD methodName = null;

        public Context(Document doc, CompilationController controller, int endOffset, boolean autoPopup) {
            this.doc = doc;
            this.controller = controller;
            this.endOffset = endOffset;

            FileObject documentFO = getFileObject();
            if (documentFO != null) {
                try {
                    this.pus = PersistenceUtils.getPersistenceUnits(documentFO);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }

            this.CCParser = new CCParser(controller);
        }

        /**
         * Must be run under MDR transaction!
         */
        public javax.lang.model.element.Element getJavaClass() {
            TreePath path = null;
            try {
                path = getCompletionTreePath(getController(), endOffset, COMPLETION_QUERY_TYPE);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            javax.lang.model.element.Element el = null;
            try {
                getController().toPhase(Phase.ELEMENTS_RESOLVED);
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
            return (BaseDocument) doc;
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
        public PersistenceUnit[] getPersistenceUnits() {
            return this.pus;
        }

        public EntityMappings getEntityMappings() {
            if (emaps == null) {
                FileObject documentFO = getFileObject();
                this.emaps = PersistenceUtils.getEntityMappings(documentFO);
            }
            return this.emaps;
        }

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
                try {
                    el = getController().getTrees().getElement(getCompletionTreePath(getController(), ts.offset() + 1, CompletionProvider.COMPLETION_QUERY_TYPE));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
                try {
                    el = getController().getTrees().getElement(getCompletionTreePath(getController(), ts.offset(), CompletionProvider.COMPLETION_QUERY_TYPE));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
