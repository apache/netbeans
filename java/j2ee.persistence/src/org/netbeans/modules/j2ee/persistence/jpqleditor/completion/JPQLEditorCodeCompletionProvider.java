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
package org.netbeans.modules.j2ee.persistence.jpqleditor.completion;

import org.netbeans.modules.j2ee.persistence.editor.completion.*;
import java.io.IOException;
import java.util.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.eclipse.persistence.jpa.jpql.parser.DefaultJPQLGrammar;
import org.eclipse.persistence.jpa.jpql.tools.ContentAssistProposals;
import org.eclipse.persistence.jpa.jpql.tools.DefaultJPQLQueryHelper;
import org.eclipse.persistence.jpa.jpql.tools.spi.IEntity;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMapping;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.jpqleditor.ui.JPQLEditorTopComponent;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.Query;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * see NNCompletionProvider and NNCompletionQuery as nb 5.5 precursors for this
 * class
 *
 * @author sp153251
 */
@MimeRegistration(mimeType = JPQLEditorCodeCompletionProvider.MIME_JPQL, service = CompletionProvider.class)//NOI18N
public class JPQLEditorCodeCompletionProvider implements CompletionProvider {

    public static final String MIME_JPQL = "text/x-jpql";//NOI18N

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE && queryType != CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new JPACodeCompletionQuery(queryType, component, component.getSelectionStart(), true), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;//will not appear automatically
    }

    class JPACodeCompletionQuery extends AsyncCompletionQuery {

        private List<JPACompletionItem> results;
        private byte hasAdditionalItems = 0; //no additional items
        private int anchorOffset;
        private JTextComponent component;
        private int queryType;
        private int caretOffset;
        private String filterPrefix;
        private boolean hasTask;

        public JPACodeCompletionQuery(int queryType, JTextComponent component, int caretOffset, boolean hasTask) {
            this.queryType = queryType;
            this.caretOffset = caretOffset;
            this.hasTask = hasTask;
            this.component = component;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            {
                try {
                    this.caretOffset = caretOffset;
                    {
                        results = null;
                        anchorOffset = -1;
                        Source source = Source.create(doc);
                        PUDataObject puObject;
                        JavaSource js;
                        if (source != null) {
                            JPQLEditorTopComponent tc = (JPQLEditorTopComponent) NbEditorUtilities.getTopComponent(component);
                            puObject = tc.getDataObject();
                            final FileObject pXml = puObject.getPrimaryFile();
                            final Project project = FileOwnerQuery.getOwner(pXml);
                            if (project == null) {
                                return;
                            }
                            // XXX this only works correctly with projects with a single sourcepath,
                            // but we don't plan to support another kind of projects anyway (what about Maven?).
                            // mkleint: Maven has just one sourceroot for java sources, the config files are placed under
                            // different source root though. JavaProjectConstants.SOURCES_TYPE_RESOURCES
                            SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                            js = JavaSource.create(ClasspathInfo.create(sourceGroups[0].getRootFolder()));

                            js.runUserActionTask( (CompilationController parameter) -> {
                                JPACodeCompletionQuery.this.run(parameter, project, pXml);
                            }, false);
                            if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                                if (results != null) {
                                    resultSet.addAllItems(results);
                                }
                                resultSet.setHasAdditionalItems(hasAdditionalItems > 0);
                                if (hasAdditionalItems == 1) {
                                    resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JPQLEditorCodeCompletionProvider.class, "JCP-imported-items")); //NOI18N
                                }
                                if (hasAdditionalItems == 2) {
                                    resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JPQLEditorCodeCompletionProvider.class, "JCP-instance-members")); //NOI18N
                                }
                            }
                            if (anchorOffset > -1) {
                                resultSet.setAnchorOffset(anchorOffset);
                            }
                        }
                    }
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    resultSet.finish();
                }
            }

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
                        if (filterPrefix != null) {
                            resultSet.addAllItems(getFilteredData(results, filterPrefix));
                            resultSet.setHasAdditionalItems(hasAdditionalItems > 0);
                        } else {
                            Completion.get().hideDocumentation();
                            Completion.get().hideCompletion();
                        }
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
            List ret = new ArrayList();
            for (Iterator<JPACompletionItem> it = data.iterator(); it.hasNext();) {
                CompletionItem itm = it.next();
                if (itm.getInsertPrefix().toString().startsWith(prefix)) {
                    ret.add(itm);
                }
            }
            return ret;
        }

        private void run(CompilationController controller, final Project project, final FileObject fo) throws MetadataModelException, IOException {
            if (hasTask && !isTaskCancelled()) {
                int startOffset = caretOffset;
                results = new ArrayList<JPACompletionItem>();
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                EntityClassScopeProvider provider = (EntityClassScopeProvider) project.getLookup().lookup(EntityClassScopeProvider.class);
                EntityClassScope ecs = null;
                if (provider != null) {
                    ecs = provider.findEntityClassScope(fo);
                }
                TaskUserAction task = new TaskUserAction(controller, component.getText(), fo, startOffset);
                EntityClassScope scope = ecs;
                MetadataModel<EntityMappingsMetadata> entityMappingsModel = null;
                if (scope != null) {
                    entityMappingsModel = scope.getEntityMappingsModel(false); // false since I guess you only want the entity classes defined in the project
                }
                if (entityMappingsModel != null) {
                    entityMappingsModel.runReadAction(task);
                }
            }
        }

        private class TaskUserAction implements MetadataModelAction<EntityMappingsMetadata, Boolean> {

            private final CompilationController controller;
            private final int startOffset;
            private boolean valid;
            private final FileObject fo;
            private final String completedValue;

            private TaskUserAction(CompilationController controller, String value, FileObject fo, int startOffset) {
                this.controller = controller;
                this.fo = fo;
                this.startOffset = startOffset;
                this.completedValue = value;
                valid = false;
            }

            public boolean isValid() {
                return valid;
            }

            @Override
            public Boolean run(EntityMappingsMetadata metadata) throws Exception {
                if (metadata.getRoot() == null) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "No EnitityMappings defined.");
                } else {
                    completeJPQLContext(metadata.getRoot(), completedValue, results);
                    valid = true;
                }
                return valid;
            }

            private List completeJPQLContext(EntityMappings mappings, String completedValue, List<JPACompletionItem> results) {
                DefaultJPQLQueryHelper helper = new DefaultJPQLQueryHelper(DefaultJPQLGrammar.instance());

                Project project = FileOwnerQuery.getOwner(fo);
                helper.setQuery(new Query(null, completedValue, new ManagedTypeProvider(project, mappings, controller.getElements())));
                int offset = startOffset;
                ContentAssistProposals buildContentAssistProposals = helper.buildContentAssistProposals(offset);

                if (buildContentAssistProposals != null && buildContentAssistProposals.hasProposals()) {
                    for (String var : buildContentAssistProposals.identificationVariables()) {
                        results.add(new JPACompletionItem.JPQLElementItem(var, false, false, 0, offset, completedValue, buildContentAssistProposals));
                    }
                    for (IMapping mapping : buildContentAssistProposals.mappings()) {
                        results.add(new JPACompletionItem.JPQLElementItem(mapping.getName(), false, false, 0, offset, completedValue, buildContentAssistProposals));
                    }
                    for (IEntity entity : buildContentAssistProposals.abstractSchemaTypes()) {
                        results.add(new JPACompletionItem.JPQLElementItem(entity.getName(), false, false, 0, offset, completedValue, buildContentAssistProposals));
                    }
                    for (String ids : buildContentAssistProposals.identifiers()) {
                        results.add(new JPACompletionItem.JPQLElementItem(ids, false, false, 0, offset, completedValue, buildContentAssistProposals));
                    }
                }

                return results;
            }
        }
    }
}