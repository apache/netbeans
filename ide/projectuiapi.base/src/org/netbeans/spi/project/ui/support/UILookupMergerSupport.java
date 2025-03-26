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


package org.netbeans.spi.project.ui.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Factory class for creation of {@link org.netbeans.spi.project.LookupMerger} instances.
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi 1.19
 */
public final class UILookupMergerSupport {
    
    /** Creates a new instance of LookupMergerSupport */
    private UILookupMergerSupport() {
    }
    
    /**
     * Create a {@link org.netbeans.spi.project.LookupMerger} instance 
     * for {@link org.netbeans.spi.project.ui.RecommendedTemplates}. Allows to merge 
     * templates from multiple sources. 
     * @return instance to include in project lookup
     */
    public static LookupMerger<RecommendedTemplates> createRecommendedTemplatesMerger() {
        return new RecommendedMerger();
    }
    
    /**
     * Create a {@link org.netbeans.spi.project.LookupMerger} instance 
     * for {@link org.netbeans.spi.project.ui.PrivilegedTemplates}. Allows to merge 
     * templates from multiple sources. 
     * @return instance to include in project lookup
     */
    public static LookupMerger<PrivilegedTemplates> createPrivilegedTemplatesMerger() {
        return new PrivilegedMerger();
    }
    
    /**
     * Create a {@link org.netbeans.spi.project.LookupMerger} instance 
     * for {@link org.netbeans.spi.project.ui.ProjectOpenedHook}. The merger makes sure all registered
     * <code>ProjectOpenedHook</code> instances are called and that the default instance is called first.
     * @param defaultInstance - the default {@link org.netbeans.spi.project.ui.ProjectOpenedHook} instance or null if
     * a default privileged instance is not required.
     * @return instance to include in project lookup
     * @since org.netbeans.modules.projectuiapi 1.24
     */
    public static LookupMerger<ProjectOpenedHook> createProjectOpenHookMerger(ProjectOpenedHook defaultInstance) {
        return new OpenMerger(defaultInstance);
    }


    /**
     * Create a {@link org.netbeans.spi.project.LookupMerger} instance
     * for {@link org.netbeans.spi.project.ui.ProjectProblemsProvider}. The merger
     * collects all {@link org.netbeans.spi.project.ui.ProjectProblemsProvider.ProjectProblem}s
     * from {@link org.netbeans.spi.project.ui.ProjectProblemsProvider}s registered in the
     * project lookup.
     * @return instance to include in project lookup
     * @since  1.60
     */
    public static LookupMerger<ProjectProblemsProvider> createProjectProblemsProviderMerger() {
        return new ProjectProblemsProviderMerger();
    }
    
    private static class PrivilegedMerger implements LookupMerger<PrivilegedTemplates> {
        @Override
        public Class<PrivilegedTemplates> getMergeableClass() {
            return PrivilegedTemplates.class;
        }

        @Override
        public PrivilegedTemplates merge(Lookup lookup) {
            return new PrivilegedTemplatesImpl(lookup);
        }
    }
    
    private static class RecommendedMerger implements LookupMerger<RecommendedTemplates> {
        
        @Override
        public Class<RecommendedTemplates> getMergeableClass() {
            return RecommendedTemplates.class;
        }

        @Override
        public RecommendedTemplates merge(Lookup lookup) {
            return new RecommendedTemplatesImpl(lookup);
        }
    }
    
    private static class OpenMerger implements LookupMerger<ProjectOpenedHook> {
        private final ProjectOpenedHook defaultInstance;

        OpenMerger(ProjectOpenedHook def) {
            defaultInstance = def;
        }
        @Override
        public Class<ProjectOpenedHook> getMergeableClass() {
            return ProjectOpenedHook.class;
        }

        @Override
        public ProjectOpenedHook merge(Lookup lookup) {
            return new OpenHookImpl(defaultInstance, lookup);
        }
        
    }

    private static class ProjectProblemsProviderMerger implements LookupMerger<ProjectProblemsProvider> {

        @Override
        public Class<ProjectProblemsProvider> getMergeableClass() {
            return ProjectProblemsProvider.class;
        }

        @Override
        public ProjectProblemsProvider merge(Lookup lookup) {
            return new ProjectProblemsProviderImpl(lookup);
        }

    }
    
    private static class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private final Lookup lkp;
        
        public PrivilegedTemplatesImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        @Override
        public String[] getPrivilegedTemplates() {
            Set<String> templates = new LinkedHashSet<String>();
            for (PrivilegedTemplates pt : lkp.lookupAll(PrivilegedTemplates.class)) {
                String[] temp = pt.getPrivilegedTemplates();
                if (temp == null) {
                    throw new IllegalStateException(pt.getClass().getName() + " returns null from getPrivilegedTemplates() method."); //NOI18N
                }
                templates.addAll(Arrays.asList(temp));
            }
            return templates.toArray(new String[0]);
        }
    }
    
    private static class RecommendedTemplatesImpl implements RecommendedTemplates {
        
        private final Lookup lkp;
        
        public RecommendedTemplatesImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        @Override
        public String[] getRecommendedTypes() {
            Set<String> templates = new LinkedHashSet<String>();
            for (RecommendedTemplates pt : lkp.lookupAll(RecommendedTemplates.class)) {
                String[] temp = pt.getRecommendedTypes();
                if (temp == null) {
                    throw new IllegalStateException(pt.getClass().getName() + " returns null from getRecommendedTemplates() method."); //NOI18N
                }
                templates.addAll(Arrays.asList(temp));
            }
            return templates.toArray(new String[0]);
        }
        
    }
    
    private static class OpenHookImpl extends ProjectOpenedHook {

        private final ProjectOpenedHook defaultInstance;
        private final Lookup lkp;        
        
        OpenHookImpl(ProjectOpenedHook def, Lookup lkp) {
            defaultInstance = def;
            this.lkp = lkp; 
            //shall we listen on ProjectOpenedHook instance changes in lookup and 
            // call close on the disappearing ones?
        }
        
        @Override
        protected void projectOpened() {
            if (defaultInstance != null) {
                ProjectOpenedTrampoline.DEFAULT.projectOpened(defaultInstance);
            }
            for (ProjectOpenedHook poh : lkp.lookupAll(ProjectOpenedHook.class)) {
                if (poh != defaultInstance && poh != this) {
                    ProjectOpenedTrampoline.DEFAULT.projectOpened(poh);
                }
            }
        }

        @Override
        protected void projectClosed() {
            if (defaultInstance != null) {
                ProjectOpenedTrampoline.DEFAULT.projectClosed(defaultInstance);
            }
            for (ProjectOpenedHook poh : lkp.lookupAll(ProjectOpenedHook.class)) {
                if (poh != defaultInstance && poh != this) {
                    ProjectOpenedTrampoline.DEFAULT.projectClosed(poh);
                }
            }
        }
        
    }
    
    private static class ProjectProblemsProviderImpl implements ProjectProblemsProvider, PropertyChangeListener {

        private final Lookup lkp;
        private final PropertyChangeSupport support;
        //@GuardedBy("this")
        private Iterable<? extends Reference<ProjectProblemsProvider>> providers;


        ProjectProblemsProviderImpl(@NonNull final Lookup lkp) {
            Parameters.notNull("lkp", lkp); //NOI18N
            this.lkp = lkp;
            this.support = new PropertyChangeSupport(this);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            support.removePropertyChangeListener(listener);
        }

        @Override
        public Collection<? extends ProjectProblem> getProblems() {
            final Collection<ProjectProblem> problems = new LinkedHashSet<ProjectProblem>();
            for (Reference<ProjectProblemsProvider> providerRef : getProviders()) {
                final ProjectProblemsProvider provider = providerRef.get();
                if (provider != null) {
                    problems.addAll(provider.getProblems());
                }
            }
            return Collections.unmodifiableCollection(problems);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PROP_PROBLEMS.equals(evt.getPropertyName())) {
                support.firePropertyChange(PROP_PROBLEMS,null,null);
            }
        }

        private synchronized Iterable<? extends Reference<ProjectProblemsProvider>> getProviders() {
            if (providers == null) {
                final Collection<Reference<ProjectProblemsProvider>> newProviders = new LinkedHashSet<Reference<ProjectProblemsProvider>>();
                for (ProjectProblemsProvider provider : lkp.lookupAll(ProjectProblemsProvider.class)) {
                    provider.addPropertyChangeListener(WeakListeners.propertyChange(this, provider));
                    newProviders.add(new WeakReference<ProjectProblemsProvider>(provider));
                }
                providers = newProviders;
            }
            return providers;
        }
    }
}
