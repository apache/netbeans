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
package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.Description;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelUtilities;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * A template wizard iterator for new ManagedBean.
 *
 * @author Petr Pisl, Alexey Butenko, Martin Fousek
 */
@SuppressWarnings("serial") // not going to be serialized
public class ManagedBeanIterator implements TemplateWizard.Iterator {

    private int index;
    private ManagedBeanPanel managedBeanPanel;
    private transient WizardDescriptor.Panel[] panels;
    /**
     * List of ManagedBean scopes.
     */
    private static final Map<ManagedBean.Scope, String> FACES_SCOPE = new EnumMap<>(Scope.class);
    static {
        FACES_SCOPE.put(ManagedBean.Scope.APPLICATION, "ApplicationScoped"); // NOI18N
        FACES_SCOPE.put(ManagedBean.Scope.NONE, "NoneScoped");               // NOI18N
        FACES_SCOPE.put(ManagedBean.Scope.REQUEST, "RequestScoped");         // NOI18N
        FACES_SCOPE.put(ManagedBean.Scope.SESSION, "SessionScoped");         // NOI18N
        FACES_SCOPE.put(ManagedBean.Scope.VIEW, "ViewScoped");               // NOI18N
    }

    /**
     * List of ContextDependencyInjection scopes.
     */
    private static final Map<NamedScope, String> NAMED_SCOPE = new EnumMap<>(NamedScope.class);
    static {
        NAMED_SCOPE.put(NamedScope.DEPENDENT, "Dependent");             //NOI18N
        NAMED_SCOPE.put(NamedScope.APPLICATION, "ApplicationScoped");   //NOI18N
        NAMED_SCOPE.put(NamedScope.REQUEST, "RequestScoped");           //NOI18N
        NAMED_SCOPE.put(NamedScope.SESSION, "SessionScoped");           //NOI18N
        NAMED_SCOPE.put(NamedScope.CONVERSATION, "ConversationScoped"); //NOI18N
        NAMED_SCOPE.put(NamedScope.FLOW, "FlowScoped");                 //NOI18N
        NAMED_SCOPE.put(NamedScope.VIEW, "ViewScoped");                 //NOI18N

    }

    @Override
    public void initialize(TemplateWizard wizard) {
        index = 0;
        // obtaining target folder
        Project project = Templates.getProject(wizard);

        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        managedBeanPanel = new ManagedBeanPanel(project, wizard);
        WizardDescriptor.Panel javaPanel;
        if (sourceGroups.length == 0) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(ManagedBeanIterator.class, "MSG_No_Sources_found"));
            javaPanel = managedBeanPanel;
        } else {
            javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, managedBeanPanel);

            javaPanel.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    managedBeanPanel.updateManagedBeanName((WizardDescriptor.Panel) e.getSource());
                }
            });
        }
        panels = new WizardDescriptor.Panel[]{javaPanel};

        // Creating steps.
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent) panels[i].getComponent();
            if (steps[i] == null) {
                steps[i] = jc.getName();
            }
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
        }
    }

    @Override
    public void uninitialize(TemplateWizard wizard) {
        panels = null;
    }

    @Override
    public Set instantiate(TemplateWizard wizard) throws IOException {
        FileObject dir = Templates.getTargetFolder(wizard);
        DataFolder df = DataFolder.findFolder(dir);
        FileObject template = Templates.getTemplate(wizard);

        DataObject dTemplate = DataObject.find(template);

        String configFile = (String) wizard.getProperty(WizardProperties.CONFIG_FILE);
        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        dir = wm.getDocumentBase();
        if (configFile == null) {
            if (!JSFConfigUtilities.hasJsfFramework(dir)) {
                JSFConfigUtilities.extendJsfFramework(dir, false);
            }
        }
        String beanName = getUniqueName((String) wizard.getProperty(WizardProperties.NAME), project);
        Object scope = wizard.getProperty(WizardProperties.SCOPE);
        boolean isAnnotate = !managedBeanPanel.isAddBeanToConfig();
        DataObject dobj = null;

        if (isAnnotate && (Utilities.isJavaEE6Plus(wizard) || (JSFUtils.isJSF20Plus(wm, true) && JSFUtils.isJavaEE5(wizard)))) {
            Map<String, Object> templateProperties = new HashMap<String, Object>();
            String targetName = Templates.getTargetName(wizard);
            boolean isCdiEnabled = false;
            boolean jakartaJsfPackages;
            if(JSFUtils.isJakartaEE9Plus(wizard)) {
                org.netbeans.modules.jakarta.web.beans.CdiUtil cdiUtil = project.getLookup().lookup(org.netbeans.modules.jakarta.web.beans.CdiUtil.class);
                if(cdiUtil != null && cdiUtil.isCdiEnabled()){
                    isCdiEnabled = true;
                }
                templateProperties.put("jakartaJsfPackages", true);
                jakartaJsfPackages = true;
            } else {
                org.netbeans.modules.web.beans.CdiUtil cdiUtil = project.getLookup().lookup(org.netbeans.modules.web.beans.CdiUtil.class);
                if(cdiUtil != null && cdiUtil.isCdiEnabled()){
                    isCdiEnabled = true;
                }
                templateProperties.put("jakartaJsfPackages", false);
                jakartaJsfPackages = false;
            }
            if (isCdiEnabled) {
                templateProperties.put("cdiEnabled", true);
                templateProperties.put("classAnnotation", "@Named(value=\"" + beanName + "\")");   //NOI18N
                templateProperties.put("scope", ScopeEntry.getFor(scope, jakartaJsfPackages));    //NOI18N
                NamedScope namedScope = (NamedScope) scope;
                switch (namedScope) {
                    case SESSION:
                    case CONVERSATION:
                    case VIEW:
                        templateProperties.put("passivationCapable", "true");    //NOI18N
                        break;
                    default:
                        break;
                }
            } else {
                if (targetName.equalsIgnoreCase(beanName) && targetName.substring(0, 1).equalsIgnoreCase(beanName.substring(0, 1))) {
                    templateProperties.put("classAnnotation", "@ManagedBean");   //NOI18N
                } else {
                    templateProperties.put("classAnnotation", "@ManagedBean(name=\"" + beanName + "\")");   //NOI18N
                }
                templateProperties.put("scope", ScopeEntry.getFor(scope, jakartaJsfPackages));    //NOI18N
            }
            dobj = dTemplate.createFromTemplate(df, targetName, templateProperties);
        } else {
            FileObject fo = dir.getFileObject(configFile); //NOI18N
            JSFConfigModel configModel = ConfigurationUtils.getConfigModel(fo, true);
            final FacesConfig facesConfig = configModel.getRootComponent();
            dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wizard));

            final ManagedBean bean = configModel.getFactory().createManagedBean();
            String targetName = Templates.getTargetName(wizard);
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String packageName = null;
            org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(wizard);
            for (int i = 0; i < groups.length && packageName == null; i++) {
                packageName = org.openide.filesystems.FileUtil.getRelativePath(groups[i].getRootFolder(), targetFolder);
                if (packageName != null) {
                    break;
                }
            }
            if (packageName != null) {
                packageName = packageName.replace('/', '.');
            } else {
                packageName = "";
            }
            String className = null;
            if (packageName.length() > 0) {
                className = packageName + "." + targetName; //NOI18N
            } else {
                className = targetName;
            }

            bean.setManagedBeanName(beanName);
            bean.setManagedBeanClass(className);

            //#172446: Make sure that scope is not null
            if (scope == null) {
                scope = Scope.REQUEST;
            }
            bean.setManagedBeanScope((Scope) scope);

            String description = (String) wizard.getProperty(WizardProperties.DESCRIPTION);
            if (description != null && description.length() > 0) {
                Description beanDescription = bean.getModel().getFactory().createDescription();
                beanDescription.setValue(description);
                bean.addDescription(beanDescription);
            }

            JSFConfigModelUtilities.doInTransaction(configModel, new Runnable() {
                @Override
                public void run() {
                    facesConfig.addManagedBean(bean);
                }
            });
            JSFConfigModelUtilities.saveChanges(configModel);
        }
        return Collections.singleton(dobj);
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public String name() {
        return NbBundle.getMessage(ManagedBeanIterator.class, "TITLE_x_of_y", index + 1, panels.length);
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; //NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }

    private String getUniqueName(String original, Project project) {
        String value = original;
        int count = 0;
        for (FacesManagedBean managedBean : JSFBeanCache.getBeans(project)) {
            if (value.equals(managedBean.getManagedBeanName())) {
                count++;
                value = original + count;
            }
        }
        return value;
    }

    protected enum NamedScope {
        DEPENDENT("dependent"),         //NOI18N
        APPLICATION("application"),     //NOI18N
        REQUEST("request"),             //NOI18N
        SESSION("session"),             //NOI18N
        CONVERSATION("conversation"),   //NOI18N
        FLOW("flow"),                   //NOI18N
        VIEW("view");                   //NOI18N

        private String scope;

        NamedScope(String scope) {
            this.scope = scope;
        }

        @Override
        public String toString() {
            return scope;
        }
    }

    public static class ScopeEntry {

        private final String className;
        private final String importEntry;
        private String parameters;

        public ScopeEntry(String className, String importEntry) {
            this.className = className;
            this.importEntry = importEntry;
        }

        public String getClassName() {
            return className;
        }

        public String getImportEntry() {
            return importEntry;
        }

        public String getParameters() {
            return parameters;
        }

        private static ScopeEntry getFor(Object scope, boolean jakartaJsfPackages) {
            if (scope instanceof Scope) {
                Scope typedScope = (Scope) scope;
                return new ScopeEntry(FACES_SCOPE.get(typedScope), getScopeImport(typedScope, jakartaJsfPackages));
            } else {
                NamedScope typedScope = (NamedScope) scope;
                ScopeEntry se = new ScopeEntry(NAMED_SCOPE.get(typedScope), getScopeImport(typedScope, jakartaJsfPackages));
                if (typedScope == NamedScope.FLOW) {
                    se.parameters = "\"\""; //NOI18N
                }
                return se;
            }
        }

        private static String getScopeImport(Scope scope, boolean jakartaJsfPackages) {
            if(jakartaJsfPackages) {
                return "jakarta.faces.bean." + FACES_SCOPE.get(scope); //NOI18N
            } else {
                return "javax.faces.bean." + FACES_SCOPE.get(scope); //NOI18N
            }
        }

        private static String getScopeImport(NamedScope scope, boolean jakartaJsfPackages) {
            String scopeSimpleName = NAMED_SCOPE.get(scope);
            if (jakartaJsfPackages) {
                if (scope == NamedScope.FLOW) {
                    return "jakarta.faces.flow." + scopeSimpleName; //NOI18N
                } else if (scope == NamedScope.VIEW) {
                    return "jakarta.faces.view." + scopeSimpleName; //NOI18N
                } else {
                    return "jakarta.enterprise.context." + scopeSimpleName; //NOI18N
                }
            } else {
                if (scope == NamedScope.FLOW) {
                    return "javax.faces.flow." + scopeSimpleName; //NOI18N
                } else if (scope == NamedScope.VIEW) {
                    return "javax.faces.view." + scopeSimpleName; //NOI18N
                } else {
                    return "javax.enterprise.context." + scopeSimpleName; //NOI18N
                }
            }
        }
    }
}
