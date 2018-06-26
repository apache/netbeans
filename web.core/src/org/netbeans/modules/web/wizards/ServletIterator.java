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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.web.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledEditorKit;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.web.core.Util;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.util.HelpCtx;

/**
 * A template wizard iterator for new servlets, filters and listeners.
 * 
 * @author radim.kubacki@sun.com
 * @author ana.von.klopp@sun.com
 * @author milan.kuchtiak@sun.com
 */

public class ServletIterator implements TemplateWizard.AsynchronousInstantiatingIterator {
    
    private static final long serialVersionUID = -4147344271705652643L;
    private static final Version JAVA_VERSION_17 = Version.fromDottedNotationWithFallback("1.7"); //NOI18N

    private transient FileType fileType; 
    private transient TargetEvaluator evaluator = null; 
    private transient DeployData deployData = null; 
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wizard;
    private transient WizardDescriptor.Panel customPanel;

    private ServletIterator(FileType fileType) {
        this.fileType = fileType;
    }

    public static ServletIterator createServletIterator() {
        return new ServletIterator(FileType.SERVLET);
    }

    public static ServletIterator createFilterIterator() {
        return new ServletIterator(FileType.FILTER);
    }

    public void initialize(WizardDescriptor wiz) {
        this.wizard = (TemplateWizard) wiz;
        index = 0;

        if (fileType.equals(FileType.SERVLET) || fileType.equals(FileType.FILTER)) {
            deployData = new ServletData(fileType);
            if (Utilities.isJavaEE6Plus(wizard)) {
                deployData.setMakeEntry(false);
            }
            evaluator = new TargetEvaluator(fileType, deployData);
        }

        Project project = Templates.getProject(wizard);
        DataFolder targetFolder=null;
        try {
            targetFolder = wizard.getTargetFolder();
        }
        catch (IOException ex) {
            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
        }
        evaluator.setInitialFolder(targetFolder,project); 
        boolean canCreate = ((ServletData)deployData).canCreate(wizard);
        
        if (fileType == FileType.SERVLET) {
            panels = new WizardDescriptor.Panel[]{
                        new FinishableProxyWizardPanel(
                        createPackageChooserPanel(wizard, null),
                        new HelpCtx(ServletIterator.class.getName() + "." + fileType), // #114487
                        canCreate ), 
                        ServletPanel.createServletPanel(evaluator, wizard)
                    };
        } else if (fileType == FileType.FILTER) {
            customPanel = new WrapperSelection(wizard);
            panels = new WizardDescriptor.Panel[]{ canCreate ? 
                        createPackageChooserPanel(wizard, customPanel):
                            new FinishableProxyWizardPanel( 
                                    createPackageChooserPanel(wizard, customPanel), 
                                    new HelpCtx(ServletIterator.class.getName() 
                                            + "." + fileType), false),
                        ServletPanel.createServletPanel(evaluator, wizard),
                        ServletPanel.createFilterPanel(evaluator, wizard)
                    };
        }
        
        // Creating steps.
        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) { 
            JComponent jc = (JComponent)panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = jc.getName();
            }
	    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
	    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
	}	
    }

    private WizardDescriptor.Panel createPackageChooserPanel(TemplateWizard wizard, WizardDescriptor.Panel customPanel) {
        Project project = Templates.getProject(wizard);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        if (customPanel==null) {
            if (sourceGroups.length == 0) {
                Sources sources = ProjectUtils.getSources(project);
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                return Templates.createSimpleTargetChooser(project, sourceGroups);
            }else {
                return JavaTemplates.createPackageChooser(project, sourceGroups);
            }
        } else {
            if (sourceGroups.length == 0) {
                Sources sources = ProjectUtils.getSources(project);
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                return Templates.createSimpleTargetChooser(project, sourceGroups, customPanel);
            } else {
                return JavaTemplates.createPackageChooser(project, sourceGroups, customPanel);
            }
        }
    }
    
    public Set<DataObject> instantiate() throws IOException {
	// Create the target folder. The next piece is independent of
	// the type of file we create, and it should be moved to the
	// evaluator class instead. The exact same process
	// should be used when checking if the directory is valid from
	// the wizard itself. 

	// ------------------------- FROM HERE -------------------------
        
        FileObject dir = Templates.getTargetFolder(wizard);
        DataFolder df = DataFolder.findFolder(dir);

        FileObject template = Templates.getTemplate(wizard);
        if (FileType.FILTER.equals(fileType) && ((WrapperSelection)customPanel).isWrapper()) {
            template = Templates.getTemplate(wizard);
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("AdvancedFilter","java"); //NOI18N
        }
        
        HashMap<String, Object> templateParameters = new HashMap<String, Object>();
        templateParameters.put("servletEditorFold", NbBundle.getMessage(ServletIterator.class, "MSG_ServletEditorFold")); //NOI18N
        templateParameters.put("java17style", isJava17orLater(df.getPrimaryFile())); //NOI18N

        // Fix for IZ171834 - Badly generated servlet template in JavaEE6 Web Application
        initServletEmptyData( );
        
        if (!deployData.makeEntry() && Utilities.isJavaEE6Plus(wizard)) {
            if (fileType == FileType.SERVLET) {
                AnnotationGenerator.webServlet((ServletData)deployData, templateParameters);
            }
            if (fileType == FileType.FILTER) {
                AnnotationGenerator.webFilter((ServletData)deployData, templateParameters);
            }
        }

        DataObject dTemplate = DataObject.find(template);                
        DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wizard), templateParameters);

        //#150274
        Project project = Templates.getProject(wizard);
        ContainerClassPathModifier modifier = project.getLookup().lookup(ContainerClassPathModifier.class);
        if (modifier != null) {
            modifier.extendClasspath(dobj.getPrimaryFile(), new String[] {
                ContainerClassPathModifier.API_SERVLET
            });
        }

	    // If the user does not want to add the file to the
        // deployment descriptor, return
        if (!deployData.makeEntry()) {
            return Collections.singleton(dobj);
        }

        if (!deployData.hasDD()) {
            // Create web.xml
            WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
            if (wm != null && wm.getDocumentBase() != null) {
                FileObject webInf = wm.getWebInf();
                if (webInf == null) {
                    webInf = FileUtil.createFolder(wm.getDocumentBase(), "WEB-INF");    //NOI18N
                }
                FileObject webXml = DDHelper.createWebXml(wm.getJ2eeProfile(), webInf);
                deployData.setWebApp(webXml);
            }
        }

        // needed to be able to finish ServletWizard from the second panel
        if (deployData.getClassName().length()==0) {
            String targetName = wizard.getTargetName();
            FileObject targetFolder = Templates.getTargetFolder(wizard);
            String packageName = null;
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < groups.length && packageName == null; i++) {
                if (WebModule.getWebModule (groups [i].getRootFolder ()) != null) {
                    packageName = FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
                }
            }
            if (packageName!=null)
                packageName = packageName.replace('/','.');
            else packageName="";
            // compute (and set) the servlet-class 
            deployData.setClassName(packageName.length()==0?targetName:packageName+"."+targetName);
            // compute (and set) the servlet-name and url-pattern 
            String servletName = ((ServletData)deployData).createDDServletName(targetName);
            ((ServletData)deployData).createDDServletMapping(servletName);
        } 
        deployData.createDDEntries();
        
        if (fileType == FileType.SERVLET && dobj.getPrimaryFile()!=null) {
            dobj.getPrimaryFile().setAttribute("org.netbeans.modules.web.IsServletFile", 
                    Boolean.TRUE);                      // NOI18N
        }

        return Collections.singleton(dobj);
    }

    /**
     * Says whether the project source level is of 1.7 or above.
     * @param target target project folder
     * @return {@code true} if the project which contains the folder uses java source 1.7 and above
     */
    private static Boolean isJava17orLater(FileObject target) {
        if (target == null) {
            return Boolean.FALSE;
        }

        String sourceLevel = SourceLevelQuery.getSourceLevel(target);
        if (sourceLevel != null) {
            Version currentVersion = Version.fromDottedNotationWithFallback(sourceLevel);
            return currentVersion.isAboveOrEqual(JAVA_VERSION_17);
        }
        return Boolean.FALSE;
    }

    public void uninitialize(WizardDescriptor wizard) {
        this.wizard = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---
    
    public String name() {
        return NbBundle.getMessage (ServletIterator.class, "TITLE_x_of_y",
            index + 1, panels.length);
    }
    
    // If the user has elected to place the file in a regular
    // directory (not a web module) then we don't show the DD info
    // panel. 
    public boolean hasNext() {
        return index < panels.length - 1 && (deployData.hasDD() || Utilities.isJavaEE6Plus(wizard));
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    /*
     * Fix for IZ171834 - Badly generated servlet template in JavaEE6 Web Application
     */
    private void initServletEmptyData(  ) {
        if (fileType != FileType.SERVLET) {
            return;
        }
        if ( !(deployData instanceof ServletData) ){
            return;
        }
        ServletData data = (ServletData)deployData;
        if ( evaluator.getClassName() == null ){
            /*
             *  User skip second panel. So one need to generate
             *  default servlet name and  url mapping.  
             */
            // this call will set file and class name in evaluator.
            panels[1].readSettings( wizard );
            data.createDDServletName( evaluator.getFileName() );
            data.createDDServletMapping( data.getName());
        }
    }
    
    // PENDING - Ann suggests updating the available panels based on
    // changes. Here is what should happen: 
    // 1. If target is directory, disable DD panels
    // 2. If target is web module but the user does not want to make a
    //    DD entry, disable second DD panel for Filters. 

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

}
