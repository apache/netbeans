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
