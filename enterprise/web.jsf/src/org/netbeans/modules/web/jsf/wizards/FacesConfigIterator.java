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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFCatalog;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_1_0;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_1_1;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_1_2;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_2_0;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_2_1;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_2_2;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_2_3;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_3_0;
import static org.netbeans.modules.web.jsfapi.api.JsfVersion.JSF_4_0;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * A template wizard operator for new faces-config.xml
 *
 * @author Alexey Butenko
 */
public class FacesConfigIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(FacesConfigIterator.class.getName());
    private static final String DEFAULT_NAME = "faces-config";   //NOI18N
    private static final String FACES_CONFIG_PARAM = "javax.faces.CONFIG_FILES";    //NOI18N
    private static final String JAKARTAEE_FACES_CONFIG_PARAM = "jakarta.faces.CONFIG_FILES";    //NOI18N
    private static final String INIT_PARAM = "InitParam";  //NOI18N
    private static final String RESOURCE_FOLDER = "/org/netbeans/modules/web/jsf/resources/"; //NOI18N

    private int index;
    private transient WizardDescriptor.Panel[] panels;

    @Override
    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        Project project = Templates.getProject( wizard );
        String targetName = Templates.getTargetName(wizard);
        FileObject targetDir = Templates.getTargetFolder(wizard);

        FileObject fo = createFacesConfig(project, targetDir, targetName, true);
        if (fo != null) {
            return Collections.singleton(DataObject.find(fo));
        } else {
            return Collections.EMPTY_SET;
        }
    }

    public static FileObject createFacesConfig(Project project, FileObject targetDir, String targetName, boolean addJSFFrameworkIfNecessary) throws IOException {
        FileObject result = null;
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject docBase = wm.getDocumentBase();
            if (addJSFFrameworkIfNecessary) {
                if (!JSFConfigUtilities.hasJsfFramework(docBase)) {
                    JSFConfigUtilities.extendJsfFramework(docBase, false);
                }

                final ContainerClassPathModifier modifier = project.getLookup().lookup(ContainerClassPathModifier.class);
                if (modifier != null) {
                    modifier.extendClasspath(targetDir, new String[] {ContainerClassPathModifier.API_JSF});
                }
            }

            final String facesConfigTemplate = findFacesConfigTemplate(wm);
            FileObject fcTemplate = URLMapper.findFileObject(FacesConfigIterator.class.getResource(RESOURCE_FOLDER + facesConfigTemplate));
            DataObject fc = DataObject.find(fcTemplate);
            result = fc.createFromTemplate(DataFolder.findFolder(targetDir), targetName).getPrimaryFile(); //NOI18N

            FileObject dd = wm.getDeploymentDescriptor();
//            assert dd != null;
            FileObject webInf = wm.getWebInf();
            WebApp ddRoot = (dd == null) ? null : DDProvider.getDefault().getDDRoot(dd);

            boolean isDefaultLocation = DEFAULT_NAME.equals(targetName) && targetDir == webInf;
            if (!isDefaultLocation && ddRoot != null) {
                try {
                    //Need to specify config file in javax.faces.FACES_CONFIG property
                    //First search existing param
                    InitParam[] parameters = ddRoot.getContextParam();
                    boolean found = false;
                    int i = 0;
                    for (InitParam param : parameters) {
                        if (param.getParamName().equals(FACES_CONFIG_PARAM) || param.getParamName().equals(JAKARTAEE_FACES_CONFIG_PARAM)) {
                            found = true;
                            String value = param.getParamValue() + ",\n            /" + FileUtil.getRelativePath(wm.getDocumentBase(), targetDir) + "/" + targetName + ".xml";  //NOI18N
                            ddRoot.removeContextParam(param);
                            InitParam newParameter = (InitParam) ddRoot.createBean(INIT_PARAM);
                            newParameter.setParamName(param.getParamName());
                            newParameter.setParamValue(value);  //NOI18N
                            ddRoot.addContextParam(newParameter);
                            break;
                        }
                        i++;
                    }
                    if (!found) {
                        InitParam contextParam = (InitParam) ddRoot.createBean(INIT_PARAM);
                        if(WebApp.VERSION_6_1.equals(ddRoot.getVersion()) || WebApp.VERSION_6_0.equals(ddRoot.getVersion()) || 
                                WebApp.VERSION_5_0.equals(ddRoot.getVersion())) {
                            contextParam.setParamName(JAKARTAEE_FACES_CONFIG_PARAM);
                        } else {
                            contextParam.setParamName(FACES_CONFIG_PARAM);
                        }
                        contextParam.setParamValue("/" + FileUtil.getRelativePath(wm.getDocumentBase(), targetDir) + "/" + targetName + ".xml");  //NOI18N
                        ddRoot.addContextParam(contextParam);
                    }
                    ddRoot.write(dd);

                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return result;
    }

    private static ClassPath getCompileClasspath(Project project) {
        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        Sources sources = ProjectUtils.getSources(project);
        if (sources == null) {
            return null;
        }

        SourceGroup[] sourceGroups = sources.getSourceGroups("java"); //NOII18N
        if (sourceGroups.length > 0) {
            return cpp.findClassPath(sourceGroups[0].getRootFolder(), ClassPath.COMPILE);
        }
        return null;
    }

    private static String findFacesConfigTemplate(WebModule wm) {
        JsfVersion jsfVersion = JsfVersionUtils.get(wm, false);
        // not found on project classpath (case of Maven project with JSF in deps)
        if (jsfVersion == null) {
            Profile profile = wm.getJ2eeProfile();
            if (profile.isAtLeast(Profile.JAKARTA_EE_11_WEB)) {
                return JSFCatalog.RES_FACES_CONFIG_4_1;
            } else if (profile.isAtLeast(Profile.JAKARTA_EE_10_WEB)) {
                return JSFCatalog.RES_FACES_CONFIG_4_0;
            } else if (profile.isAtLeast(Profile.JAKARTA_EE_9_WEB)) {
                return JSFCatalog.RES_FACES_CONFIG_3_0;
            } else if (profile.isAtLeast(Profile.JAVA_EE_8_WEB)) {
                return JSFCatalog.RES_FACES_CONFIG_2_3;
            } else if (profile.isAtLeast(Profile.JAVA_EE_7_WEB)) {
                return JSFCatalog.RES_FACES_CONFIG_2_2;
            } else if (profile.isAtLeast(Profile.JAVA_EE_6_WEB)) {
                return JSFCatalog.RES_FACES_CONFIG_2_1;
            } else if (profile.isAtLeast(Profile.JAVA_EE_5)) {
                return JSFCatalog.RES_FACES_CONFIG_1_2;
            }
            
            Project project = FileOwnerQuery.getOwner(JSFUtils.getFileObject(wm));
            if (project != null ) {
                ClassPath compileClasspath = getCompileClasspath(project);
                if (compileClasspath != null) {
                    List<URL> cpUrls = new ArrayList<>();
                    for (ClassPath.Entry entry : compileClasspath.entries()) {
                        cpUrls.add(entry.getURL());
                    }
                    jsfVersion = JsfVersionUtils.forClasspath(cpUrls);
                    jsfVersion = jsfVersion == null ? JsfVersion.JSF_2_3 : jsfVersion;
                    return facesConfigForVersion(jsfVersion);
                }
            }
            return JSFCatalog.RES_FACES_CONFIG_DEFAULT;
        }
        return facesConfigForVersion(jsfVersion);
    }

    private static String facesConfigForVersion(JsfVersion jsfVersion) {
        switch (jsfVersion) {
            case JSF_4_1:
                return JSFCatalog.RES_FACES_CONFIG_4_1;
            case JSF_4_0:
                return JSFCatalog.RES_FACES_CONFIG_4_0;
            case JSF_3_0:
                return JSFCatalog.RES_FACES_CONFIG_3_0;
            case JSF_2_3:
                return JSFCatalog.RES_FACES_CONFIG_2_3;
            case JSF_2_2:
                return JSFCatalog.RES_FACES_CONFIG_2_2;
            case JSF_2_1:
                return JSFCatalog.RES_FACES_CONFIG_2_1;
            case JSF_2_0:
                return JSFCatalog.RES_FACES_CONFIG_2_0;
            case JSF_1_2:
                return JSFCatalog.RES_FACES_CONFIG_1_2;
            case JSF_1_1:
            case JSF_1_0:
            default:
                return JSFCatalog.RES_FACES_CONFIG_DEFAULT;
        }
    }

    @Override
    public void initialize(TemplateWizard wizard) {
        // obtaining target folder
        Project project = Templates.getProject( wizard );
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_WEB_INF);

        if (sourceGroups == null || sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        }

        WizardDescriptor.Panel folderPanel;
        if (sourceGroups == null || sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }

        folderPanel = new FacesConfigValidationPanel(Templates.buildSimpleTargetChooser(project, sourceGroups).create());
        panels = new WizardDescriptor.Panel[] { folderPanel };

        // Creating steps.
        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
	}

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject webInf = wm.getWebInf();
            if (webInf == null) {
                try {
                    FileObject documentBase = wm.getDocumentBase();
                    if (documentBase == null) {
                        LOG.log(Level.INFO, "WebModule does not have valid documentBase");
                        return;
                    }
                    webInf = FileUtil.createFolder(documentBase, "WEB-INF"); //NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            FileObject targetFolder = Templates.getTargetFolder(wizard);
            String relativePath = (targetFolder == null) ? null : FileUtil.getRelativePath(webInf, targetFolder);
            if (relativePath == null) {
                Templates.setTargetFolder(wizard, webInf);
            }
        }
        Templates.setTargetName(wizard, DEFAULT_NAME);
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    @Override
    public Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(FacesConfigIterator.class, "TITLE_x_of_y",
                index + 1, panels.length);
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }

    @Override
    public void previousPanel() {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private static class FacesConfigValidationPanel extends JSFValidationPanel {

        public FacesConfigValidationPanel(Panel delegate) {
            super(delegate);
        }

        @Messages({
            "FacesConfigIterator.err.no.document.base=Project hasn't defined document root. See project properties."
        })
        @Override
        public boolean isValid() {
            if (!super.isValid()) {
                return false;
            }

            Project project = getProject();
            WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
            if (webModule != null && webModule.getDocumentBase() == null) {
                getWizardDescriptor().putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, Bundle.FacesConfigIterator_err_no_document_base());
                return true;
            }
            return true;
        }

    }

}
