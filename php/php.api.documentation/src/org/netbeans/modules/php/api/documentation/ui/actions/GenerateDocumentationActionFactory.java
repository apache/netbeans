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
package org.netbeans.modules.php.api.documentation.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.documentation.PhpDocumentations;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Add 'Generate documentation' menu (for 1 provider) or submenu (for more providers). Do nothing if there are no providers.
 */
@NbBundle.Messages("GenerateDocumentationActionFactory.title=Generate Documentation")
@ActionID(id = "org.netbeans.modules.php.api.documentation.ui.actions.GenerateDocumentationActionFactory", category = "Project")
@ActionRegistration(displayName = "#GenerateDocumentationActionFactory.title", lazy = false)
@ActionReference(position = 800, path = "Projects/org-netbeans-modules-php-project/Actions")
public final class GenerateDocumentationActionFactory extends AbstractAction implements ContextAwareAction {

    private static final long serialVersionUID = 5687856454545L;


    public GenerateDocumentationActionFactory() {
        setEnabled(false);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends Project> projects = actionContext.lookupAll(Project.class);
        if (projects.size() != 1) {
            return this;
        }
        Project project = projects.iterator().next().getLookup().lookup(Project.class);
        if (project == null) {
            return this;
        }
        List<PhpDocumentationProvider> docProviders = PhpDocumentations.getDocumentations();
        if (docProviders.isEmpty()) {
            return this;
        }

        PhpModule phpModule = PhpModule.Factory.lookupPhpModule(project);
        if (phpModule == null) {
            return this;
        }
        List<PhpDocumentationProvider> projectDocProviders = new ArrayList<>(docProviders.size());
        for (PhpDocumentationProvider docProvider : docProviders) {
            if (docProvider.isInPhpModule(phpModule)) {
                projectDocProviders.add(docProvider);
            }
        }
        if (projectDocProviders.isEmpty()) {
            // no provider selected yet -> show all
            projectDocProviders.addAll(docProviders);
        }
        if (projectDocProviders.size() == 1) {
            return new PhpDocAction(phpModule, projectDocProviders.get(0));
        }
        return new DocumentationMenu(phpModule, projectDocProviders);
    }

    //~ Inner classes

    private static final class DocumentationMenu extends AbstractAction implements Presenter.Popup {

        private static final long serialVersionUID = 1587896543546879L;

        private final PhpModule phpModule;
        private final List<PhpDocumentationProvider> docProviders;


        public DocumentationMenu(PhpModule phpModule, List<PhpDocumentationProvider> docProviders) {
            super(Bundle.GenerateDocumentationActionFactory_title(), null);
            assert phpModule != null;
            assert docProviders != null;

            putValue(SHORT_DESCRIPTION, Bundle.GenerateDocumentationActionFactory_title());
            this.phpModule = phpModule;
            this.docProviders = docProviders;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public JMenuItem getPopupPresenter() {
            List<PhpDocAction> docActions = new ArrayList<>(docProviders.size());
            for (PhpDocumentationProvider docProvider : docProviders) {
                docActions.add(new PhpDocAction(docProvider.getDisplayName(), phpModule, docProvider, true));
            }
            return new DocumentationSubMenu(docActions);
        }

    }

    private static class DocumentationSubMenu extends BaseSubMenu {

        private static final long serialVersionUID = -6764324657641L;


        public DocumentationSubMenu(List<PhpDocAction> docActions) {
            super(Bundle.GenerateDocumentationActionFactory_title());

            for (PhpDocAction action : docActions) {
                add(toMenuItem(action));
            }
        }

    }

    private static final class PhpDocAction extends AbstractAction {

        private static final long serialVersionUID = 178423135454L;

        private static final RequestProcessor RP = new RequestProcessor("Generating php documentation", 2); // NOI18N

        private final PhpModule phpModule;
        private final PhpDocumentationProvider docProvider;
        private final boolean remember;


        public PhpDocAction(PhpModule phpModule, PhpDocumentationProvider docProvider) {
            this(Bundle.GenerateDocumentationActionFactory_title(), phpModule, docProvider, false);
        }

        public PhpDocAction(String name, PhpModule phpModule, PhpDocumentationProvider docProvider, boolean remember) {
            this.phpModule = phpModule;
            this.docProvider = docProvider;
            this.remember = remember;

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (phpModule.isBroken()) {
                // broken project
                UiUtils.warnBrokenProject(phpModule);
                return;
            }
            RP.post(new Runnable() {
                @Override
                public void run() {
                    LifecycleManager.getDefault().saveAll();
                    if (remember) {
                        // remember curent provider
                        docProvider.notifyEnabled(phpModule, true);
                    }
                    docProvider.generateDocumentation(phpModule);
                }

            });
        }

    }

}
