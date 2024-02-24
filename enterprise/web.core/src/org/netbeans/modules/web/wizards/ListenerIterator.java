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

package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.web.core.Util;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.DialogDisplayer;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author  mk115033
 */
public class ListenerIterator implements TemplateWizard.AsynchronousInstantiatingIterator {

    private static final Logger LOG = Logger.getLogger(ListenerIterator.class.getName());

    //                                    CHANGEME vvv
    //private static final long serialVersionUID = ...L;

    // You should define what panels you want to use here:
    private ListenerPanel panel;
    protected WizardDescriptor.Panel[] createPanels(TemplateWizard wizard) {
        Project project = Templates.getProject( wiz );
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        panel = new ListenerPanel(wizard);

        WizardDescriptor.Panel packageChooserPanel;
        if (sourceGroups.length == 0) {
            Sources sources = (Sources) ProjectUtils.getSources(project);
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            packageChooserPanel = Templates
                    .buildSimpleTargetChooser(project, sourceGroups)
                    .bottomPanel(panel)
                    .create();
        }
        else
            packageChooserPanel = JavaTemplates.createPackageChooser(project, sourceGroups, panel);

        return new WizardDescriptor.Panel[] {
            // Assuming you want to keep the default 2nd panel:
            packageChooserPanel
        };
    }

    @Override
    public Set<DataObject> instantiate () throws IOException/*, IllegalStateException*/ {
        // Here is the default plain behavior. Simply takes the selected
        // template (you need to have included the standard second panel
        // in createPanels(), or at least set the properties targetName and
        // targetFolder correctly), instantiates it in the provided
        // position, and returns the result.
        // More advanced wizards can create multiple objects from template
        // (return them all in the result of this method), populate file
        // contents on the fly, etc.

        FileObject folder = Templates.getTargetFolder(wiz);
        DataFolder targetFolder = DataFolder.findFolder(folder);


        ClassPath classPath = ClassPath.getClassPath(folder,ClassPath.SOURCE);
        String listenerName = wiz.getTargetName();
        DataObject result;

        if (classPath != null) {
            Map<String, Object> templateParameters = new HashMap<>();
            ClassPath cp = ClassPath.getClassPath(folder, ClassPath.COMPILE);
            if (cp != null && cp.findResource("jakarta/servlet/http/HttpServlet.class") != null) {
                templateParameters.put("jakartaPackages", true);
            } else {
                templateParameters.put("jakartaPackages", false);
            }
            if (!panel.createElementInDD() && Utilities.isJavaEE6Plus(wiz)) {
                templateParameters.put("classAnnotation", AnnotationGenerator.webListener());
            }

            DataObject template = wiz.getTemplate();
            result = template.createFromTemplate(targetFolder, listenerName, templateParameters);
            if (result!=null && panel.createElementInDD()){
                String className = classPath.getResourceName(result.getPrimaryFile(),'.',false);
                FileObject webAppFo=DeployData.getWebAppFor(folder);
                if (webAppFo == null) {
                    WebModule wm = WebModule.getWebModule(folder);
                    if (wm != null) {
                        FileObject webInfFolder = wm.getWebInf();
                        if (webInfFolder == null) {
                            webInfFolder = FileUtil.createFolder(folder, "WEB-INF"); //NOI18N
                        }
                        webAppFo = DDHelper.createWebXml(wm.getJ2eeProfile(), webInfFolder);
                    }
                }
                WebApp webApp=null;
                if (webAppFo!=null) {
                    webApp = DDProvider.getDefault().getDDRoot(webAppFo);
                }
                if (webApp!=null) {
                    Listener[] oldListeners = webApp.getListener();
                    boolean found=false;
                    for (int i=0;i<oldListeners.length;i++) {
                        if (className.equals(oldListeners[i].getListenerClass())) {
                            found=true;
                            break;
                        }
                    }
                    if (!found) {
                        try {
                            Listener listener = (Listener)webApp.createBean("Listener");//NOI18N
                            listener.setListenerClass(className);
                            StringBuilder desc= new StringBuilder();
                            int i=0;
                            if (panel.isContextListener()) {
                                desc.append("ServletContextListener"); //NOI18N
                                i++;
                            }
                            if (panel.isContextAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("ServletContextAttributeListener"); //NOI18N
                                i++;
                            }
                            if (panel.isSessionListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("HttpSessionListener"); //NOI18N
                                i++;
                            }
                            if (panel.isSessionAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("HttpSessionAttributeListener"); //NOI18N
                            }
                            if (panel.isRequestListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("RequestListener"); //NOI18N
                                i++;
                            }
                            if (panel.isRequestAttrListener()) {
                                if (i>0) desc.append(", ");
                                desc.append("RequestAttributeListener"); //NOI18N
                            }
                            listener.setDescription(desc.toString());
                            webApp.addListener(listener);
                            webApp.write(webAppFo);
                        }
                        catch (ClassNotFoundException ex) {
                            LOG.log(Level.FINE, "error", ex);
                            //Shouldn happen since
                        }
                    }
                }
            }
            if (result!=null) {
                //#150274, #153790
                Project project = Templates.getProject(wiz);
                ContainerClassPathModifier modifier = project.getLookup().lookup(ContainerClassPathModifier.class);
                if (modifier != null) {
                    modifier.extendClasspath(result.getPrimaryFile(), new String[]{
                                ContainerClassPathModifier.API_SERVLET
                            });
                }
                JavaSource clazz = JavaSource.forFileObject(result.getPrimaryFile());
                if (clazz!=null) {
                    ListenerGenerator gen = new ListenerGenerator(
                        panel.isContextListener(),
                        panel.isContextAttrListener(),
                        panel.isSessionListener(),
                        panel.isSessionAttrListener(),
                        panel.isRequestListener(),
                        panel.isRequestAttrListener());
                    try {
                        gen.generate(clazz);
                    } catch (IOException ex){
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        } else {
            String mes = NbBundle.getMessage (ListenerIterator.class, "TXT_wrongFolderForClass", "Servlet Listener"); //NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return null;
        }
        return Collections.singleton (result);
    }

    // --- The rest probably does not need to be touched. ---

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;

    private static final long serialVersionUID = -7586964579556513549L;

    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    @Override
    public void initialize (WizardDescriptor wiz) {
        this.wiz = (TemplateWizard) wiz;
        index = 0;
        panels = createPanels (this.wiz);

        // Creating steps.
        Object prop = wiz.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps (beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent ();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName ();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }
    @Override
    public void uninitialize (WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---
    // Note that this is very similar to WizardDescriptor.Iterator, but with a
    // few more options for customization. If you e.g. want to make panels appear
    // or disappear dynamically, go ahead.

    @Override
    public String name () {
        return NbBundle.getMessage(ListenerIterator.class, "TITLE_x_of_y",
            index + 1, panels.length);
    }

    @Override
    public boolean hasNext () {
        return index < panels.length - 1;
    }
    @Override
    public boolean hasPrevious () {
        return index > 0;
    }
    @Override
    public void nextPanel () {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }
    @Override
    public void previousPanel () {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }
    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener (ChangeListener l) {}
    @Override
    public final void removeChangeListener (ChangeListener l) {}
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent ();
}
