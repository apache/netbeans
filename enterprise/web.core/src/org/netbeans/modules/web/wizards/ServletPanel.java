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
package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.model.FilterInfo;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Wizard panel that collects data for the Servlet and Filter
 * wizards. 
 *
 * @author Ana von Klopp, Milan Kuchtiak
 */
public class ServletPanel implements WizardDescriptor.FinishablePanel {

    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private transient BaseWizardPanel wizardPanel;
    private transient TemplateWizard wizard;
    /** listener to changes in the wizard */
    private ChangeListener listener;
    private DeployData deployData;
    private transient TargetEvaluator evaluator;

    /** Create the wizard panel descriptor. */
    private ServletPanel(TargetEvaluator evaluator, TemplateWizard wizard,
            boolean first) {
        this.evaluator = evaluator;
        this.wizard = wizard;
        this.deployData = evaluator.getDeployData();
        if (first) {
            this.wizardPanel = new DeployDataPanel(evaluator, wizard);
        } else {
            this.wizardPanel = new DeployDataExtraPanel(evaluator, wizard);
        }
    }

    public boolean isFinishPanel() {
        return true;
    }

    /** Create the wizard panel descriptor. */
    public static ServletPanel createServletPanel(TargetEvaluator evaluator,
            TemplateWizard wizard) {
        return new ServletPanel(evaluator, wizard, true);
    }

    /** Create the wizard panel descriptor. */
    public static ServletPanel createFilterPanel(TargetEvaluator evaluator,
            TemplateWizard wizard) {
        return new ServletPanel(evaluator, wizard, false);
    }

    public Component getComponent() {
        return wizardPanel;
    }

    public boolean isValid() {
        if (!deployData.isValid()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, deployData.getErrorMessage());
            return false;
        }
        if (Utilities.isJavaEE6Plus(wizard)) {
            // check name and mapping uniqness again with metadata model
            WebModule wm = Utilities.findWebModule(wizard);
            if (wm != null && deployData instanceof ServletData) {
                ServletData servletData = (ServletData)deployData;
                String name = servletData.getName();
                if (servletData.fileType == FileType.SERVLET) {
                    // find name clashings
                    for (ServletInfo si : getServlets(wm)) {
                        if (si.getName().equals(name)) {
                            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                    NbBundle.getMessage(ServletData.class, "MSG_servlet_name_defined", name, si.getServletClass())); //NOI18N
                            return false;
                        }
                    }
                    // find URL mapping clashings
                    for (ServletInfo si : getServlets(wm)) {
                        List<String> patterns = si.getUrlPatterns();
                        for (String pattern : patterns) {
                            for (String m : servletData.getUrlMappings()) {
                                if (pattern.equals(m)) {
                                    wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                                            NbBundle.getMessage(ServletData.class, "MSG_servlet_mapping_defined", m, si.getServletClass())); //NOI18N
                                    return true;
                                }
                            }
                        }
                    }
                }
                else if (servletData.fileType == FileType.FILTER) {
                    // find name clashings
                    for (FilterInfo fi : getFilters(wm)) {
                        if (fi.getName().equals(name)) {
                            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                    NbBundle.getMessage(ServletData.class, "MSG_filter_name_defined", name, fi.getFilterClass())); //NOI18N
                            return false;
                        }
                    }
                    // find URL mapping clashings
                    for (FilterInfo fi : getFilters(wm)) {
                        List<String> patterns = fi.getUrlPatterns();
                        for (String pattern : patterns) {
                            for (FilterMappingData m : servletData.getFilterMappings()) {
                                if (pattern.equals(m.getPattern())) {
                                    wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                                            NbBundle.getMessage(ServletData.class, "MSG_filter_mapping_defined", m.getPattern(), fi.getFilterClass())); //NOI18N
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
        return true;
    }

    private List<ServletInfo> getServlets(WebModule wm) {
        try {
            List<ServletInfo> servlets = wm.getMetadataModel().runReadAction(new MetadataModelAction<WebAppMetadata, List<ServletInfo>>() {
                public List<ServletInfo> run(WebAppMetadata metadata) throws Exception {
                    return metadata.getServlets();
                }
            });
            return servlets;
        }
        catch (MetadataModelException e) {
            Logger.getGlobal().log(Level.WARNING, "getServlets failed", e);
        }
        catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, "getServlets failed", e);
        }
        return Collections.emptyList();
    }

    private List<FilterInfo> getFilters(WebModule wm) {
        try {
            List<FilterInfo> filters = wm.getMetadataModel().runReadAction(new MetadataModelAction<WebAppMetadata, List<FilterInfo>>() {
                public List<FilterInfo> run(WebAppMetadata metadata) throws Exception {
                    return metadata.getFilters();
                }
            });
            return filters;
        }
        catch (MetadataModelException e) {
            Logger.getGlobal().log(Level.WARNING, "getFilters failed", e);
        }
        catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, "getFilters failed", e);
        }
        return Collections.emptyList();
    }

    public HelpCtx getHelp() {
        // #114487
        if (evaluator.getFileType() == FileType.SERVLET) {
            return wizardPanel.getHelp();
        }
        return null;
    }

    /** Add a listener to changes of the panel's validity.
     * @param l the listener to add
     * @see #isValid
     */
    public void addChangeListener(ChangeListener l) {
        if (listener != null) {
            throw new IllegalStateException();
        }
        if (wizardPanel != null) {
            wizardPanel.addChangeListener(l);
        }
        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
     * @param l the listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        listener = null;
        if (wizardPanel != null) {
            wizardPanel.removeChangeListener(l);
        }
    }

    public void readSettings(Object settings) {
        if (settings instanceof TemplateWizard) {
            TemplateWizard w = (TemplateWizard) settings;
            //Project project = Templates.getProject(w);
            String targetName = w.getTargetName();
            org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(w);
            Project project = Templates.getProject(w);
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String packageName = null;
            for (int i = 0; i < groups.length && packageName == null; i++) {
                if (WebModule.getWebModule(groups[i].getRootFolder()) != null) {
                    packageName = org.openide.filesystems.FileUtil.getRelativePath(groups[i].getRootFolder(), targetFolder);
                }
            }
            if (packageName != null) {
                packageName = packageName.replace('/', '.');
            } else {
                packageName = "";
            }
            if (targetName == null) {
                evaluator.setClassName(w.getTemplate().getName(), packageName);
            } else {
                evaluator.setClassName(targetName, packageName);
            }
        }
        wizardPanel.setData();
    }

    public void storeSettings(Object settings) {
        // do nothing
    }
}
