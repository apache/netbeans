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

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileSystem;

/**
 * Wizard <em>J2SE Library Descriptor</em> for registering
 * libraries for end users.
 *
 * @author Radek Matous
 */
@TemplateRegistration(folder = UIUtil.TEMPLATE_FOLDER, position = 1100, displayName = "#Templates/NetBeansModuleDevelopment/emptyLibraryDescriptor", iconBase = UIUtil.LIBRARIES_ICON, description = "emptyLibraryDescriptor.html", category = UIUtil.TEMPLATE_CATEGORY)
public final class NewLibraryDescriptor extends BasicWizardIterator {
    
    NewLibraryDescriptor.DataModel data;
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewLibraryDescriptor.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new SelectLibraryPanel(wiz,data ),
                    new NameAndLocationPanel(wiz,data )
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        private Library library;
        private String libraryName;
        private String libraryDisplayName;
        
        private CreatedModifiedFiles files;
        
        /** Creates a new instance of NewLibraryDescriptorData */
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public Library getLibrary() {
            return library;
        }
        
        public void setLibrary(Library library) {
            this.library = library;
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return files;
        }
        
        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.files = files;
        }
                        
        public String getLibraryName() {
            return libraryName;
        }
        
        public void setLibraryName(String libraryName) {
            this.libraryName = libraryName;
        }

        public boolean isValidLibraryName() {
            if (getLibraryName() == null 
                    || getLibraryName().trim().length() == 0 
                    || getLibraryName().indexOf('/') != -1) {
                return false;
            }
            try {
                // additional conditions based on what is done with library name
                String path = CreatedModifiedFilesProvider.getLibraryDescriptorEntryPath(getLibraryName());
                // would throw IAE
                new URI(path);
                LayerUtil.findGeneratedName(null, path);
            } catch (URISyntaxException e) {
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
            return true;
        }
        
        public String getLibraryDisplayName() {
            return libraryDisplayName;
        }
        
        public void setLibraryDisplayName(String libraryDisplayName) {
            this.libraryDisplayName = libraryDisplayName;
        }
        
        public boolean isValidLibraryDisplayName() {
            return getLibraryDisplayName() != null && 
                    getLibraryDisplayName().trim().length() != 0;
        }
        
        boolean libraryAlreadyExists() {
            FileSystem layerFs = null;
            LayerHandle handle  = LayerHandle.forProject(getProject());
            layerFs = handle.layer(false);
            return (layerFs != null) ? (layerFs.findResource(CreatedModifiedFilesProvider.getLibraryDescriptorEntryPath(getLibraryName())) != null) : false;
        }
                        
        public NewLibraryDescriptor.DataModel cloneMe(WizardDescriptor wiz) {
            NewLibraryDescriptor.DataModel d = new NewLibraryDescriptor.DataModel(wiz);
            d.setLibrary(this.getLibrary());
            d.setPackageName(this.getPackageName());
            d.setCreatedModifiedFiles(this.getCreatedModifiedFiles());
            d.setLibraryDisplayName(this.getLibraryDisplayName());
            d.setLibraryName(this.getLibraryName());
            return d;
        }        
    }
    
}
