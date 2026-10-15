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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.web.core.Util;
import org.openide.filesystems.FileObject;
import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.taglib.TaglibCatalog;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.web.taglib.model.TagType;
import org.netbeans.modules.web.taglib.model.TldAttributeType;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author  mk115033
 */
public class TagHandlerIterator implements TemplateWizard.AsynchronousInstantiatingIterator {
    private static final Logger LOG = Logger.getLogger(TagHandlerIterator.class.getName());
    private WizardDescriptor.Panel<WizardDescriptor> packageChooserPanel,tagHandlerSelectionPanel,tagInfoPanel;

    // You should define what panels you want to use here:
    protected WizardDescriptor.Panel<WizardDescriptor>[] createPanels (Project project,TemplateWizard wiz) {
        Sources sources = (Sources) ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        tagHandlerSelectionPanel = new TagHandlerSelection(wiz);

        if (sourceGroups.length == 0)
            packageChooserPanel = Templates
                    .buildSimpleTargetChooser(project, sourceGroups)
                    .bottomPanel(tagHandlerSelectionPanel)
                    .create();
        else
            packageChooserPanel = JavaTemplates.createPackageChooser(project,sourceGroups,tagHandlerSelectionPanel);

        sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        if (sourceGroups==null || sourceGroups.length==0)
            sourceGroups = Util.getJavaSourceGroups(project);
        if (sourceGroups==null || sourceGroups.length==0)
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        tagInfoPanel = new TagInfoPanel(wiz, project, sourceGroups);
        return new WizardDescriptor.Panel[] {
            packageChooserPanel,
            tagInfoPanel
        };
    }

    @Override
    public Set instantiate () throws IOException/*, IllegalStateException*/ {
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

        HashMap<String, Object> templateParameters = new HashMap<>();
        ClassPath cp = ClassPath.getClassPath(dir, ClassPath.COMPILE);
        if(cp != null && cp.findResource("jakarta/servlet/http/HttpServlet.class") != null) {
            templateParameters.put("jakartaPackages", true);
        } else {
            templateParameters.put("jakartaPackages", false);
        }

        FileObject template = Templates.getTemplate( wiz );

        if (((TagHandlerSelection)tagHandlerSelectionPanel).isBodyTagSupport()) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("BodyTagHandler","java"); //NOI18N
        }
        DataObject dTemplate = DataObject.find( template );
        DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wiz), templateParameters);
        // writing to TLD File
        TagInfoPanel tldPanel = (TagInfoPanel)tagInfoPanel;
        Object[][] attrs = tldPanel.getAttributes();
        boolean isBodyTag = ((TagHandlerSelection)tagHandlerSelectionPanel).isBodyTagSupport();

        // writing setters to tag handler
        if (attrs.length>0 || isBodyTag) {
            JavaSource clazz = JavaSource.forFileObject(dobj.getPrimaryFile());
            boolean evaluateBody = !((TagInfoPanel)tagInfoPanel).isEmpty();
            TagHandlerGenerator generator = new TagHandlerGenerator(clazz,attrs,isBodyTag, evaluateBody);
            try {
                generator.generate();
            } catch (IOException ex){
                LOG.log(Level.INFO, null, ex);
            }
        }

        //#150274
        Project project = Templates.getProject( wiz );
        ContainerClassPathModifier modifier = project.getLookup().lookup(ContainerClassPathModifier.class);
        if (modifier != null) {
            modifier.extendClasspath(dobj.getPrimaryFile(), new String[] {
                ContainerClassPathModifier.API_JSP
            });
        }



        // writing to TLD file
        if (tldPanel.writeToTLD()) {
            FileObject tldFo = tldPanel.getTLDFile();
            if (tldFo!=null) {
                if (!tldFo.canWrite()) {
                    String mes = NbBundle.getMessage (TagHandlerIterator.class, "MSG_tldRO",tldFo.getNameExt());
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                } else {
                    TLDDataObject tldDO = (TLDDataObject)DataObject.find(tldFo);
                    Taglib taglib=null;
                    try {
                        taglib = tldDO.getTaglib();
                    } catch (IOException ex) {
                        String mes = NbBundle.getMessage (TagHandlerIterator.class, "MSG_tldCorrupted",tldFo.getNameExt());
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes,NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    }
                    if (taglib!=null) {
                        WebModule wm = WebModule.getWebModule(dir);
                        if (wm != null) {
                            Profile j2eeVersion = wm.getJ2eeProfile();
                            if (Profile.J2EE_13.equals(j2eeVersion) || Profile.J2EE_14.equals(j2eeVersion)) {
                                taglib.setDefaultNamespace(TaglibCatalog.J2EE_NS);  //NOI18N
                            }
                        }
                        TagType tag = new TagType();
                        tag.setName(tldPanel.getTagName());
                        tag.setTagClass(tldPanel.getClassName());
                        if (tldPanel.isEmpty()) {
                            tag.setBodyContent("empty"); //NOI18N
                        } else if (tldPanel.isScriptless()) {
                            tag.setBodyContent(isBodyTag?"JSP":"scriptless"); //NOI18N
                        } else if (tldPanel.isTegdependent()) {
                            tag.setBodyContent("tagdependent"); //NOI18N
                        }
                        //Object[][] attrs = tldPanel.getAttributes();
                        for (int i=0;i<attrs.length;i++) {
                            TldAttributeType attr = new TldAttributeType();
                            attr.setName((String)attrs[i][0]);
                            attr.setType((String)attrs[i][1]);
                            boolean required = ((Boolean)attrs[i][2]);
                            if (required) attr.setRequired("true"); //NOI18N
                            boolean rtexpr = ((Boolean)attrs[i][3]);
                            //if (rtexpr) attr.setRtexprvalue("true"); //NOI18N
                            // #252857 there is likely a bug in 2.1 xsd where rtexprvalue is mandatory
                            // while the docs says opposite, we chose not to fix the xsd as it
                            // is part of the JSP 2.1 spec
                            attr.setRtexprvalue(Boolean.toString(rtexpr));
                            tag.addAttribute(attr);
                        }
                        taglib.addTag(tag);
                        SaveCookie save = (SaveCookie)tldDO.getCookie(SaveCookie.class);
                        if (save!=null) save.save();
                        try {
                            tldDO.write(taglib);
                        } catch (IOException ex) {
                            LOG.log(Level.WARNING, null, ex);
                        }
                    }
                }
            }
        }

        return Collections.singleton(dobj);
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
        Project project = Templates.getProject( wiz );
        panels = createPanels (project,this.wiz);

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
        return NbBundle.getMessage(TagHandlerIterator.class, "TITLE_x_of_y",
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
    public WizardDescriptor.Panel<WizardDescriptor> current () {
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
