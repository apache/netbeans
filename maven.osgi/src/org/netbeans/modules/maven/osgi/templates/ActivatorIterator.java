/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.maven.osgi.templates;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.osgi.OSGiConstants;
import static org.netbeans.modules.maven.osgi.templates.Bundle.*;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle.Messages;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author mkleint
 */
@TemplateRegistration(folder="OSGi", position=100, content="Activator.java.template", scriptEngine="freemarker", displayName="#template.activator", iconBase="org/netbeans/modules/maven/osgi/templates/new_OSGi_file_16.png", description="new_OSGi_activator.html", category="osgi")
@Messages("template.activator=Bundle Activator")
public class ActivatorIterator implements TemplateWizard.AsynchronousInstantiatingIterator<WizardDescriptor> {
    private static final Logger LOG = Logger.getLogger(ActivatorIterator.class.getName());
    
    // You should define what panels you want to use here:
    protected List<WizardDescriptor.Panel<WizardDescriptor>> createPanels (Project project, TemplateWizard wiz) {
        Sources sources = ProjectUtils.getSources(project);
        DataFolder targetFolder=null;
        try {
            targetFolder = wiz.getTargetFolder();
        }
        catch (IOException ex) {
            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
        }
        return Collections.<WizardDescriptor.Panel<WizardDescriptor>>singletonList(
            JavaTemplates.createPackageChooser(project,
                          sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA))
            );
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
       
        org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
        DataFolder df = DataFolder.findFolder( dir );
        
        FileObject template = Templates.getTemplate( wiz );
        
        DataObject dTemplate = DataObject.find( template );                
        final DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );

        //this part might be turned pluggable once we have also ant based osgi projects. if..
        Project project = Templates.getProject( wiz );
        ClassPath cp = ClassPath.getClassPath(dobj.getPrimaryFile(), ClassPath.SOURCE);
        final String path = cp.getResourceName(dobj.getPrimaryFile(), '.', false);

        final NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        if (prj != null) {
            Utilities.performPOMModelOperations(project.getProjectDirectory().getFileObject("pom.xml"),
                    Collections.<ModelOperation<POMModel>>singletonList(
                        new ModelOperation<POMModel>() {
                @Override
                           public void performOperation(POMModel model) {
                               addActivator(prj, model, path);
                           }
                    }
            ));
        }

        return Collections.singleton(dobj);
    }

    private void addActivator(NbMavenProject prj, POMModel mdl, String path) {
        //TODO check if present already..

        Plugin old = null;
        Plugin plugin;
        Build bld = mdl.getProject().getBuild();
        if (bld != null) {
            old = bld.findPluginById(OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN);
        } else {
            mdl.getProject().setBuild(mdl.getFactory().createBuild());
        }
        if (old != null) {
            plugin = old;
        } else {
            plugin = mdl.getFactory().createPlugin();
            plugin.setGroupId(OSGiConstants.GROUPID_FELIX);
            plugin.setArtifactId(OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN);
            String ver = PluginPropertyUtils.getPluginVersion(prj.getMavenProject(), 
                    OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN);
            if (ver == null) {
                //not defined in resolved project, set version.
//                plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_FELIX));
                plugin.setVersion("2.3.7"); //TODO get from some preferences file.
            }
            mdl.getProject().getBuild().addPlugin(plugin);
        }
        Configuration conf = plugin.getConfiguration();
        if (conf == null) {
            conf = mdl.getFactory().createConfiguration();
            plugin.setConfiguration(conf);
        }
        List<POMExtensibilityElement> elems = conf.getConfigurationElements();
        POMExtensibilityElement instructions = null;
        for (POMExtensibilityElement el : elems) {
            if (OSGiConstants.PARAM_INSTRUCTIONS.equals(el.getQName().getLocalPart())) {
                instructions = el;
                break;
            }
        }
        if (instructions == null) {
            instructions = mdl.getFactory().createPOMExtensibilityElement(POMQName.createQName(OSGiConstants.PARAM_INSTRUCTIONS, mdl.getPOMQNames().isNSAware()));
            conf.addExtensibilityElement(instructions);
        }
        elems = instructions.getExtensibilityElements();
        POMExtensibilityElement activator = null;
        for (POMExtensibilityElement el : elems) {
            if (OSGiConstants.BUNDLE_ACTIVATOR.equals(el.getQName().getLocalPart())) {
                activator = el;
                break;
            }
        }
        if (activator == null) {
            activator = mdl.getFactory().createPOMExtensibilityElement(POMQName.createQName(OSGiConstants.BUNDLE_ACTIVATOR, mdl.getPOMQNames().isNSAware()));
            instructions.addExtensibilityElement(activator);
        }
        activator.setElementText(path);
    }

    // --- The rest probably does not need to be touched. ---
    
    private transient int index;
    private transient List<WizardDescriptor.Panel<WizardDescriptor>> panels;
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
        Project project = Templates.getProject( wiz );
        panels = createPanels (project,this.wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent ();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName ();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
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

    @Messages("TITLE_x_of_y={0} of {1}")
    @Override
    public String name () {
        return TITLE_x_of_y(index + 1, panels.size());
    }
    
    @Override
    public boolean hasNext () {
        return index < panels.size() - 1;
    }
    @Override
    public boolean hasPrevious () {
        return index > 0;
    }
    @Override
    public void nextPanel () {
        if (! hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }
    @Override
    public void previousPanel () {
        if (! hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        return panels.get(index);
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

    public static String[] createSteps(String[] before, List<WizardDescriptor.Panel<WizardDescriptor>> panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.size()];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels.get(i - before.length + diff).getComponent ().getName ();
            }
        }
        return res;
    }

}
