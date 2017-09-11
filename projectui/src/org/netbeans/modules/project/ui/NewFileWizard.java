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
package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class NewFileWizard extends TemplateWizard {
    public static final String INCLUDES_TEMPLATES_WITH_PROJECTS = "INCLUDES_TEMPLATES_WITH_PROJECTS";
   
    @NullAllowed
    private Project currP;
    private MessageFormat format;
    
    // private String[] recommendedTypes;
    private final boolean includeTemplatesWithProject;

    @CheckForNull
    private Project getCurrentProject() {
        return currP;
    }

    private void setCurrentProject(@NullAllowed Project p) {
        this.currP = p;
    }

    public NewFileWizard(@NullAllowed Project project /*, String recommendedTypes[] */, boolean includeTemplatesWithProjects) {
        setCurrentProject(project);
        this.includeTemplatesWithProject = includeTemplatesWithProjects;
        putProperty(INCLUDES_TEMPLATES_WITH_PROJECTS, Boolean.valueOf(includeTemplatesWithProject));
        putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, getCurrentProject());
        format = new MessageFormat(NbBundle.getBundle(NewFileWizard.class).getString("LBL_NewFileWizard_MessageFormat"));
        // this.recommendedTypes = recommendedTypes;        
        //setTitleFormat( new MessageFormat( "{0}") );
        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // check ProjectChooserFactory.WIZARD_KEY_PROJECT property
                if (ProjectChooserFactory.WIZARD_KEY_PROJECT.equals(evt.getPropertyName())) {
                    Project newProject = (Project) evt.getNewValue();
                    if (!Utilities.compareObjects(getCurrentProject(), newProject)) {
                        // set the new project and force reload panels in wizard
                        setCurrentProject(newProject);
                        try {
                            //reload (DataObject.find (Templates.getTemplate (NewFileWizard.this)));
                            // bugfix #44481, check if the template is null
                            if (Templates.getTemplate(NewFileWizard.this) != null) {
                                DataObject obj = DataObject.find(Templates.getTemplate(NewFileWizard.this));

                                // read the attributes declared in module's layer
                                Object unknownIterator = obj.getPrimaryFile().getAttribute("instantiatingIterator"); //NOI18N
                                if (unknownIterator == null) {
                                    unknownIterator = obj.getPrimaryFile().getAttribute("templateWizardIterator"); //NOI18N
                                }
                                // set default NewFileIterator if no attribute is set
                                if (unknownIterator == null) {
                                    try {
                                        obj.getPrimaryFile().setAttribute("instantiatingIterator", NewFileIterator.genericFileIterator()); //NOI18N
                                    } catch (java.io.IOException e) {
                                        // can ignore it because a iterator will created though
                                    }
                                }
                                Hacks.reloadPanelsInWizard(NewFileWizard.this, obj);
                            }
                        } catch (DataObjectNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void updateState() {
        super.updateState();
        String substitute = (String) getProperty("NewFileWizard_Title"); // NOI18N
        String title;
        if (substitute == null) {
            title = NbBundle.getBundle(NewFileWizard.class).getString("LBL_NewFileWizard_Title"); // NOI18N
        } else {
            Object[] args = new Object[]{
                NbBundle.getBundle(NewFileWizard.class).getString("LBL_NewFileWizard_Subtitle"), // NOI18N
                substitute
            };
            title = format.format(args);
        }
        super.setTitle(title);
    }

    @Override
    public void setTitle(String ignore) {
    }

    @Override
    protected WizardDescriptor.Panel<WizardDescriptor> createTemplateChooser() {
        WizardDescriptor.Panel<WizardDescriptor> panel = new TemplateChooserPanel(getCurrentProject() /*, recommendedTypes */, includeTemplatesWithProject);
        JComponent jc = (JComponent) panel.getComponent();
        jc.getAccessibleContext().setAccessibleName(NbBundle.getBundle(NewProjectWizard.class).getString("ACSN_NewFileWizard")); // NOI18N
        jc.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(NewProjectWizard.class).getString("ACSD_NewFileWizard")); // NOI18N
        return panel;
    }

    @Override
    protected WizardDescriptor.Panel<WizardDescriptor> createTargetChooser() {
        Project project = getCurrentProject();
        SourceGroup[] sourceGroups;
        if (project != null) {
            Sources c = ProjectUtils.getSources(project);
            sourceGroups = c.getSourceGroups(Sources.TYPE_GENERIC);
        }
        else {
            sourceGroups = new SourceGroup[0];
        }

        return Templates.buildSimpleTargetChooser(project, sourceGroups).create();
    }
}
 
/** Old impl might be usefull later in Wizards API

///** Wizard for creating new files in a project.
// *
// * @author  Jesse Glick, Petr Hrebejk
// */
//public class NewFileWizard implements TemplateWizard.Iterator, ChangeListener {
//        
//    /** Currently selected project */
//    private Project p;
//    /** Recommended template types, or null for any. */
//    private final String[] recommendedTypes;
//    /** Currently selected template to delegate subsequent panels to, or null. */
//    private InstantiatingIterator delegate = null;
//    /** True if currently on a panel created by the delegate. */
//    private boolean insideDelegate = false;
//    /** The template chooser panel (initially null). */
//    private WizardDescriptor.Panel templateChooser = null;
//    /** Change listeners. */
//    private final ChangeSupport changeSupport = new ChangeSupport(this);
//    /** Currently used wizard. */
//    private TemplateWizard wiz = null;
//    
//    /** Creates a new instance of NewFileWizard */
//    public NewFileWizard(Project p, String[] recommendedTypes) {
//        this.p = p;
//        this.recommendedTypes = recommendedTypes;
//    }
//    
//    public void initialize(TemplateWizard wiz) {
//        this.wiz = wiz;
//        wiz.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, p);
//    }
//
//    public void uninitialize(TemplateWizard wiz) {
//        this.wiz = null;
//        insideDelegate = false;
//        setDelegate(null);
//        templateChooser = null;
//    }
//
//    public Set instantiate(TemplateWizard wiz) throws IOException {
//        assert insideDelegate;
//        return delegate.instantiate(wiz);
//    }
//
//    public String name() {
//        if (insideDelegate) {
//            return delegate.name();
//        } else {
//            return "Choose Template"; // XXX I18N
//        }
//    }
//
//    /*
//    public WizardDescriptor.Panel current() {
//        if (insideDelegate) {
//            return delegate.current();
//        } else {
//            if (templateChooser == null) {
//                templateChooser = new TemplateChooserPanel();
//            }
//            return templateChooser;
//        }
//    }
//     */
//
//    public boolean hasNext() {
//        if (insideDelegate) {
//            return delegate.hasNext();
//        } else {
//            return delegate != null;
//        }
//    }
//
//    public boolean hasPrevious() {
//        return insideDelegate;
//    }
//
//    public void nextPanel() {
//        if (insideDelegate) {
//            delegate.nextPanel();
//        } else {
//            assert delegate != null;
//            insideDelegate = true;
//        }
//    }
//
//    public void previousPanel() {
//        assert insideDelegate;
//        if (delegate.hasPrevious()) {
//            delegate.previousPanel();
//        } else {
//            insideDelegate = false;
//        }
//    }
//
//    public void addChangeListener(ChangeListener l) {
//        changeSupport.addChangeListener(l);
//    }
//
//    public void removeChangeListener(ChangeListener l) {
//        changeSupport.removeChangeListener(l);
//    }
//
//    public void setDelegate(InstantiatingIterator nue) {
//        assert !insideDelegate;
//        if (delegate == nue) {
//            return;
//        }
//        if (delegate != null) {
//            delegate.removeChangeListener(this);
//            delegate.uninitialize(wiz);
//        }
//        if (nue != null) {
//            nue.initialize(wiz);
//            nue.addChangeListener(this);
//        }
//        delegate = nue;
//        changeSupport.fireChange();
//    }
//
//    public void stateChanged(ChangeEvent e) {
//        changeSupport.fireChange();
//    }
//    
//    private static InstantiatingIterator findTemplateWizardIterator(FileObject template, Project p) {
//        TemplateWizard.Iterator iter = (TemplateWizard.Iterator)template.getAttribute("templateWizardIterator"); // NOI18N
//        if (iter != null) {
//            return WizardIterators.templateIteratotBridge( iter );
//        } 
//        else {            
//            Sources c = ProjectUtils.getSources(p);
//            WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[1];            
//            panels[0] = Templates.createSimpleTargetChooser(p, template, c.getSourceGroups(Sources.TYPE_GENERIC));
//            return new WizardIterators.InstantiatingArrayIterator( panels, template );
//        }
//    }
//
//    }            
