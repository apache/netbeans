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

package org.netbeans.modules.jakarta.web.beans.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.jakarta.web.beans.CdiUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.spi.project.ui.templates.support.Templates.SimpleTargetChooserBuilder;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A template wizard operator for new beans.xml
 */
public class BeansXmlIterator implements TemplateWizard.Iterator {
    
    private enum J2eeProjectType {
        WAR,
        JAR,
        CAR
    }

    private static final String WEB_INF = "WEB-INF";        // NOI18N
    private static final String META_INF = "META-INF";        // NOI18N
    
    private int index;
    private static final String defaultName = "beans";   //NOI18N

    private transient WizardDescriptor.Panel[] panels;
    private transient J2eeProjectType type;

    @Override
    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        String targetName = Templates.getTargetName(wizard);
        FileObject targetDir = Templates.getTargetFolder(wizard);
        Project project = Templates.getProject(wizard);
        Profile profile = null;
        if (project != null) {
            J2eeProjectCapabilities cap = J2eeProjectCapabilities.forProject(project);
            if (cap != null && cap.isCdi41Supported()) {
                profile = Profile.JAKARTA_EE_11_FULL;
            } else if (cap != null && cap.isCdi40Supported()) {
                profile = Profile.JAKARTA_EE_10_FULL;
            } else if (cap != null && cap.isCdi30Supported()) {
                profile = Profile.JAKARTA_EE_9_FULL;
            } else if (cap != null && cap.isCdi20Supported()) {
                profile = Profile.JAVA_EE_8_FULL;
            } else if (cap != null && cap.isCdi11Supported()) {
                profile = Profile.JAVA_EE_7_FULL;
            }
        }
        FileObject fo = DDHelper.createBeansXml(profile != null ? profile : Profile.JAVA_EE_6_FULL, targetDir, targetName);
        if (fo != null) {
            if ( project != null ){
                CdiUtil logger = project.getLookup().lookup( CdiUtil.class );
                if (logger != null){
                    logger.log("USG_CDI_BEANS_WIZARD", BeansXmlIterator.class, 
                            new Object[]{project.getClass().getName()}, true);
                }
            }
            return Collections.singleton(DataObject.find(fo));
        } 
        else {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public void initialize(TemplateWizard wizard) {
        WizardDescriptor.Panel folderPanel;
        Project project = Templates.getProject( wizard );
        
        FileObject  targetFolder = getTargetFolder(project);
        
        Sources sources = project.getLookup().lookup(Sources.class);
        SourceGroup[] sourceGroups; 
        String parentFolder  = null;
        if ( type == J2eeProjectType.WAR){
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if ( targetFolder!=null && targetFolder.getFileObject(defaultName+".xml")!=null){
                parentFolder = WEB_INF;
            }
        }
        else {
            if ( type != null && 
                    targetFolder!=null && targetFolder.getFileObject(defaultName+".xml")!=null )
            {
                parentFolder = targetFolder.getName();
            }
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }
        
        SimpleTargetChooserBuilder builder = Templates.
                buildSimpleTargetChooser(project, sourceGroups);
        
        builder = builder.bottomPanel( new FakePanel(parentFolder));
        folderPanel = builder.create();
        
        panels = new WizardDescriptor.Panel[] { folderPanel };

        // Creating steps.
        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
            jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps); 
        }

        Templates.setTargetName(wizard, defaultName);
        Templates.setTargetFolder(wizard, targetFolder );
    }

    private FileObject getTargetFolder(Project project) {
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject webInf = wm.getWebInf();
            if (webInf == null && wm.getDocumentBase() != null) {
                try {
                    webInf = FileUtil.createFolder(wm.getDocumentBase(), WEB_INF); 
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            type = J2eeProjectType.WAR;
            return webInf;
        } 
        else {
            EjbJar ejbs[] = EjbJar.getEjbJars(project);
            if (ejbs.length > 0) {
                type = J2eeProjectType.JAR;
                return ejbs[0].getMetaInf();
            } else {
                Car cars[] = Car.getCars(project);
                if (cars.length > 0) {
                    type = J2eeProjectType.CAR;
                    return cars[0].getMetaInf();
                } 
            }
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        SourceGroup[] sourceGroups = sources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ( sourceGroups.length >0 ){
            FileObject metaInf = sourceGroups[0].getRootFolder().getFileObject( 
                    META_INF );
            if ( metaInf == null ){
                try {
                    metaInf = FileUtil.createFolder(
                        sourceGroups[0].getRootFolder(), META_INF);
                }
                catch( IOException e ){
                    Exceptions.printStackTrace(e);
                }
            }
            if ( metaInf != null ){
                return metaInf;
            }
        }
        return project.getProjectDirectory();
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
        return NbBundle.getMessage(BeansXmlIterator.class, "TITLE_x_of_y",
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

    public static String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }

    static class FakePanel implements Panel {
        
        private String folder ;
        
        FakePanel(String folder ){
            this.folder = folder;
        }

        @Override
        public Component getComponent() {
            return new JPanel();
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(Object settings) {
            if ( folder!=null ){
                ((WizardDescriptor)settings).putProperty(
                        WizardDescriptor.PROP_ERROR_MESSAGE, 
                        NbBundle.getMessage( BeansXmlIterator.class ,
                                "ERR_BeansAlreadyExists", folder));
            }
        }

        @Override
        public void storeSettings(Object settings) {
        }

        @Override
        public boolean isValid() {
            return folder == null;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }
        
    }

}