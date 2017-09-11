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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */

public class PersistenceUnitWizard implements WizardDescriptor.ProgressInstantiatingIterator {

    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private Project project;
    private PersistenceUnitWizardDescriptor descriptor;
    private static final Logger LOG = Logger.getLogger(PersistenceUnitWizard.class.getName());
    
    public static PersistenceUnitWizard create() {
        return new PersistenceUnitWizard();
    }
    
    @Override
    public String name() {
        return NbBundle.getMessage(PersistenceUnitWizard.class, "LBL_WizardTitle");
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
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    @Override
    public void previousPanel() {
        if (! hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
    }
    
    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    @Override
    public void initialize(WizardDescriptor wizard) {
        project = Templates.getProject(wizard);
        descriptor = new PersistenceUnitWizardDescriptor(project);
        panels = new WizardDescriptor.Panel[] {descriptor};
        wizard.putProperty("NewFileWizard_Title",
                NbBundle.getMessage(PersistenceUnitWizard.class, "Templates/Persistence/PersistenceUnit"));
        Wizards.mergeSteps(wizard, panels, null);
    }
    
    @Override
    public Set instantiate() throws java.io.IOException {
        assert true : "should never be called, instantiate(ProgressHandle) should be called instead";
        return null;
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start();
            return instantiateWProgress(handle);
        } finally {
            handle.finish();
        }
    }

    private Set instantiateWProgress(ProgressHandle handle) throws IOException {
        PersistenceUnit punit = null;
        PUDataObject pud = null;
        LOG.fine("Instantiating...");
        //first add libraries if necessary
        Library lib = null;
        boolean useModelgen = false;
        String modelGenLib = null;
        Provider selectedProvider = null;
        boolean libIsAdded = false;//used to check if lib was added to compile classpath
        if (descriptor.isContainerManaged()) {
            selectedProvider=descriptor.getSelectedProvider();
            if (descriptor.isNonDefaultProviderEnabled()) {
                lib = PersistenceLibrarySupport.getLibrary(selectedProvider);
                if (lib != null && !Util.isDefaultProvider(project, selectedProvider)) {
                    handle.progress(NbBundle.getMessage(PersistenceUnitWizard.class, "MSG_LoadLibs"));
                    Util.addLibraryToProject(project, lib);
                    modelGenLib = lib.getName()+"modelgen";//NOI18N
                    selectedProvider = null;//to avoid one more library addition
                    libIsAdded = true;
                }
            }
            if(selectedProvider != null && selectedProvider.getAnnotationProcessor() != null){
                if(lib == null)lib = PersistenceLibrarySupport.getLibrary(selectedProvider);
                if (lib != null){
                    Util.addLibraryToProject(project, lib, JavaClassPathConstants.PROCESSOR_PATH);
                    modelGenLib = lib.getName()+"modelgen";//NOI18N
                }
            }
        } else {
            lib = PersistenceLibrarySupport.getLibrary(descriptor.getSelectedProvider());
            if (lib != null){
                handle.progress(NbBundle.getMessage(PersistenceUnitWizard.class, "MSG_LoadLibs"));
                Util.addLibraryToProject(project, lib);
                modelGenLib = lib.getName()+"modelgen";//NOI18N
                libIsAdded = true;
            }
            JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(descriptor.getPersistenceConnection().getDriverClass());
            PersistenceLibrarySupport.addDriver(project, driver[0]);
        }
        handle.progress(NbBundle.getMessage(PersistenceUnitWizard.class, "MSG_CreatePU"));
        String version = (lib!=null && libIsAdded) ? PersistenceUtils.getJPAVersion(lib) : PersistenceUtils.getJPAVersion(project);//use project provided api and avoid use of unsupported features this way
        //
        if (selectedProvider != null && version != null) {
            String provVersion = ProviderUtil.getVersion(selectedProvider);
            if (provVersion != null) {
                //even if project support jpa 2.x etc, but selected provider is reported as jpa1.0 use jpa1.0
                if (Double.parseDouble(version) > Double.parseDouble(provVersion)) {
                    version = provVersion;
                }
            }
        }
        if(version != null && descriptor.isContainerManaged()){
            //version may be limited by server
            version = Util.getJPAVersionSupported(project, version);
        }
        try{
            LOG.fine("Retrieving PUDataObject");
            pud = ProviderUtil.getPUDataObject(project, version);
        } catch (InvalidPersistenceXmlException ipx){
            // just log for debugging purposes, at this point the user has
            // already been warned about an invalid persistence.xml
            LOG.log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N
            return Collections.emptySet();
       }
        version=pud.getPersistence().getVersion();
        //
        if (descriptor.isContainerManaged()) {
            LOG.fine("Creating a container managed PU");
            if(Persistence.VERSION_2_1.equals(version)) {
                punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
            } else if(Persistence.VERSION_2_0.equals(version)) {
                punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
            } else {//currently default 1.0
                punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
            }
            if (descriptor.getDatasource() != null && !"".equals(descriptor.getDatasource())){
                if (descriptor.isJTA()) {
                    punit.setJtaDataSource(descriptor.getDatasource());
                } else {
                    punit.setNonJtaDataSource(descriptor.getDatasource());
                    punit.setTransactionType("RESOURCE_LOCAL");
                }
            }
            
            if (descriptor.isNonDefaultProviderEnabled()) {
                String providerClass = descriptor.getNonDefaultProvider();
                punit.setProvider(providerClass);
            }
        } else {
            LOG.fine("Creating an application managed PU");
            punit = ProviderUtil.buildPersistenceUnit(descriptor.getPersistenceUnitName(),
                    descriptor.getSelectedProvider(), descriptor.getPersistenceConnection(), version);
            punit.setTransactionType("RESOURCE_LOCAL");
            // Explicitly add <exclude-unlisted-classes>false</exclude-unlisted-classes>
            // See issue 142575 - desc 10, and issue 180810
            if (!Util.isJavaSE(project)) {
                punit.setExcludeUnlistedClasses(false);
            }
        }
        useModelgen = !(punit instanceof org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit);//above 1.0
        // Explicitly add <exclude-unlisted-classes>false</exclude-unlisted-classes>
        // See issue 142575 - desc 10
        if (!Util.isJavaSE(project)) {
            punit.setExcludeUnlistedClasses(false);
        }
        
        punit.setName(descriptor.getPersistenceUnitName());
        ProviderUtil.setTableGeneration(punit, descriptor.getTableGeneration(), project);
        pud.addPersistenceUnit(punit);
        LOG.fine("Saving PUDataObject");
        pud.save();
        LOG.fine("Saved");
        //modelgen
        if(useModelgen && modelGenLib!=null){
            Library mLib = LibraryManager.getDefault().getLibrary(modelGenLib);
            if(mLib!=null) Util.addLibraryToProject(project, mLib, JavaClassPathConstants.PROCESSOR_PATH);//no real need to add modelgen to compile classpath
        }
        return Collections.singleton(pud.getPrimaryFile());
    }
    
}
