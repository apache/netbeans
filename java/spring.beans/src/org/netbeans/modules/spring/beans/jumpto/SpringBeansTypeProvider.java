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
package org.netbeans.modules.spring.beans.jumpto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.FileSpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.api.beans.model.SpringModel;
import org.netbeans.modules.spring.beans.ProjectSpringScopeProvider;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Provides Spring bean definitions for the Go To Type dialog
 * 
 * @author Rohan Ranade
 */
public class SpringBeansTypeProvider implements TypeProvider {

    private Set<AbstractBeanTypeDescriptor> cache;
    private String lastRefreshText;
    private SearchType lastRefreshSearchType;
    private volatile boolean isCancelled = false;

    public String name() {
        return "springbeans"; // NOI18N

    }

    public String getDisplayName() {
        return NbBundle.getMessage(SpringBeansTypeProvider.class, "LBL_SpringBeansType"); // NOI18N

    }

    public void computeTypeNames(Context context, Result result) {
        assert context.getProject() == null; // Issue 136025
        
        isCancelled = false;
        boolean cacheRefresh = false;

        final String searchText = context.getText();
        final SearchType searchType = context.getSearchType();
        final NameMatcher matcher = NameMatcherFactory.createNameMatcher(searchText, searchType);
        if (matcher == null) {
            return;
        }

        if (lastRefreshText == null || lastRefreshSearchType == null || !searchText.startsWith(lastRefreshText) || lastRefreshSearchType != searchType || cache == null) {
            // refresh cache
            cacheRefresh = true;
            final Set<AbstractBeanTypeDescriptor> currCache = new HashSet<AbstractBeanTypeDescriptor>();
            Future<Project[]> prjHandle = OpenProjects.getDefault().openProjects();

            Project[] projects = null;
            try {
                projects = prjHandle.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (projects == null) {
                return;
            }

            if (isCancelled) {
                return;
            }
            for (Project project : projects) {
                ProjectSpringScopeProvider scopeProvider = project.getLookup().lookup(ProjectSpringScopeProvider.class);
                if (scopeProvider == null) {
                    continue;
                }

                SpringScope scope = scopeProvider.getSpringScope();
                if (scope == null) {
                    continue;
                }

                if (isCancelled) {
                    return;
                }
                final Set<File> processed = new HashSet<File>();

                // getting entries from configuration files
                List<SpringConfigModel> models = scope.getAllConfigModels();
                for (SpringConfigModel model : models) {
                    try {
                        if (isCancelled) {
                            return;
                        }
                        model.runDocumentAction(new Action<DocumentAccess>() {

                            public void run(DocumentAccess docAccess) {
                                File file = docAccess.getFile();
                                if (processed.contains(file)) {
                                    return;
                                }
                                processed.add(file);

                                if (isCancelled) {
                                    return;
                                }
                                FileObject fo = docAccess.getFileObject();
                                FileSpringBeans fileBeans = docAccess.getSpringBeans().getFileBeans(fo);
                                List<SpringBean> beans = fileBeans.getBeans();

                                for (SpringBean bean : beans) {
                                    String id = bean.getId();
                                    if (id != null && matcher.accept(id)) {
                                        currCache.add(new BeanTypeDescriptor(id, bean));
                                    }

                                    for (String name : bean.getNames()) {
                                        if (matcher.accept(name)) {
                                            currCache.add(new BeanTypeDescriptor(name, bean));
                                        }
                                    }
                                }

                                for (String alias : fileBeans.getAliases()) {
                                    if (matcher.accept(alias)) {
                                        currCache.add(new BeanAliasTypeDescriptor(alias, fo));
                                    }
                                }
                            }
                        });
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                // getting annotated beans
                MetadataModel<SpringModel> springAnnotationModel =
                        scope.getSpringAnnotationModel(project.getProjectDirectory());
                try {
                    springAnnotationModel.runReadAction(new MetadataModelAction<SpringModel, Void>() {

                        @Override
                        public Void run(SpringModel metadata) throws Exception {
                            for (SpringBean springBean : metadata.getBeans()) {
                                for (String name : springBean.getNames()) {
                                    if (matcher.accept(name)) {
                                        currCache.add(new BeanTypeDescriptor(name, springBean));
                                    }
                                }
                            }
                            return null;
                        }
                    });
                } catch (MetadataModelException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (!isCancelled) {
                cache = currCache;
                lastRefreshText = searchText;
                lastRefreshSearchType = searchType;
            }
        }

        if (cache != null) {
            ArrayList<AbstractBeanTypeDescriptor> beans = new ArrayList<AbstractBeanTypeDescriptor>(cache.size());
            for (AbstractBeanTypeDescriptor beanTypeDescriptor : cache) {
                if (cacheRefresh || matcher.accept(beanTypeDescriptor.getSimpleName())) {
                    beans.add(beanTypeDescriptor);
                }
            }

            result.addResult(beans);
        }
    }

    public void cancel() {
        isCancelled = true;

    }

    public void cleanup() {
        isCancelled = false;
        cache = null;
        lastRefreshText = null;
        lastRefreshSearchType = null;
    }
}
