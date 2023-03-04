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

package org.netbeans.modules.apisupport.project.ui.wizard.codegenerator;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Wiziard for creating ne Code Generators
 * See <code>org.netbeans.spi.editor.codegen</code>
 * 
 * @author Max Sauer
 */
@TemplateRegistration(folder = UIUtil.TEMPLATE_FOLDER, position = 1200, displayName = "#Templates/NetBeansModuleDevelopment/newCodeGenerator", iconBase = "org/netbeans/modules/apisupport/project/ui/wizard/codegenerator/newCodeGenerator.png", description = "newCodeGenerator.html", category = UIUtil.TEMPLATE_CATEGORY)
public class NewCodeGeneratorIterator extends BasicWizardIterator {

    private static final String lineSep = System.getProperty("line.separator"); // NOI18N
    
    private static final String[] HARDCODED_IMPORTS = new String[] {
        "java.util.Collections", // NOI18N
        "javax.swing.text.JTextComponent", // NOI18N
        "java.util.List", // NOI18N
        "org.openide.util.Lookup", // NOI18N
        "org.netbeans.api.editor.mimelookup.MimeRegistration",
        "org.netbeans.spi.editor.codegen.CodeGenerator" // NOI18N
    };
    
    private static final String[] PROVIDER_HARDCODED_IMPORTS = new String[] {
        "org.netbeans.api.editor.mimelookup.MimeRegistration",
        "org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider", // NOI18N
        "org.openide.util.Lookup", // NOI18N
        "org.openide.util.lookup.Lookups", // NOI18N
        "org.openide.util.lookup.ProxyLookup" // NOI18N
    };

    /**
     * Create CodeGeneartorContextProvider instance and register it inside layer
     * @param model current wizard model
     * @param cmf modified files
     */
    private static void createContextProvider(DataModel model, CreatedModifiedFiles cmf) {
        Map<String, String> cpReplaceTokens = new HashMap<String, String>();
        String providerFileName = model.getProviderFileName();
        cpReplaceTokens.put("CLASS_NAME", providerFileName);
        Set<String> cpImports = new TreeSet<String>(Arrays.asList(PROVIDER_HARDCODED_IMPORTS));
        StringBuffer cpImportsBuffer = new StringBuffer();
        for (String imprt : cpImports) {
            cpImportsBuffer.append("import " + imprt + ';' + lineSep); // NOI18N
        }

        String cpFileName = model.getProviderFileName();
        cpReplaceTokens.put("CLASS_NAME", cpFileName); // NOI18N
        cpReplaceTokens.put("PACKAGE_NAME", model.getPackageName()); // NOI18N
        String cpActionPath = model.getDefaultPackagePath(providerFileName + ".java", false); // NOI18N
        FileObject cpTemplate = CreatedModifiedFiles.getTemplate("contextProvider.java"); // NOI18N
        cpReplaceTokens.put("IMPORTS", cpImportsBuffer.toString()); // NOI18N
        cpReplaceTokens.put("MIME_TYPE", model.getMimeType());
        cmf.add(cmf.createFileWithSubstitutions(cpActionPath, cpTemplate, cpReplaceTokens));
    }
    
    /** datamodel passed through individual panels */
    private NewCodeGeneratorIterator.DataModel data;
    
    static void generateFileChanges(DataModel model) {
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(model.getProject());
        
        //add module dependency
        cmf.add(cmf.addModuleDependency("org.netbeans.modules.editor.lib2")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.netbeans.modules.editor.mimelookup"));
        cmf.add(cmf.addModuleDependency("org.openide.util.lookup")); // NOI18N
        
        //create the java class implementing CodeGenerator
        final String fileName = model.getFileName();
        FileObject template = CreatedModifiedFiles.getTemplate("codeGenerator.java"); // NOI18N
        assert template != null;
        
        String actionPath = model.getDefaultPackagePath(fileName + ".java", false); // NOI18N
        
        Map<String,String> replaceTokens = new HashMap<String,String>();
        replaceTokens.put("CLASS_NAME",fileName); // NOI18N
        replaceTokens.put("PACKAGE_NAME", model.getPackageName()); // NOI18N
        Set<String> imports = new TreeSet<String>(Arrays.asList(HARDCODED_IMPORTS));
        StringBuffer importsBuffer = new StringBuffer();
        
        for (String imprt : imports) {
            importsBuffer.append("import " + imprt + ';' + lineSep); // NOI18N
        }
        replaceTokens.put("IMPORTS", importsBuffer.toString()); // NOI18N
        replaceTokens.put("MIME_TYPE", model.getMimeType());
        cmf.add(cmf.createFileWithSubstitutions(actionPath, template, replaceTokens));
        
        //if checked, create also CodeGeneratorContextProvider instance
        if (model.isContextProviderRequired()) {
            createContextProvider(model, cmf);
        }
        
        //at the end
        model.setCreatedModifiedFiles(cmf);
    }

    @Override
    protected Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewCodeGeneratorIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new CodeGeneratorPanel(wiz, data)
        };
    }

    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }

    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {

        private CreatedModifiedFiles files;
        /** mimetype for which the code generator is registered */
        private String mimeType;
        /** codeGenerator filename */
        private String fileName;
        /** fileName for contex provider */
        private String providerFileName;
        /** checkbox state */
        private boolean contextProviderRequired;
        
        public DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return files;
        }
        
        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Sets mime type taken from wizard panel
         * @param mimeType the mimetype, ie. <code>x-java</code>
         */
        void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public String getMimeType() {
            return mimeType;
        }

        public String getProviderFileName() {
            return providerFileName;
        }

        public boolean isContextProviderRequired() {
            return contextProviderRequired;
        }
        
        public void setContextProviderRequired(boolean req) {
            contextProviderRequired = req;
        }
        
        public void setProviderFileName(String fileName) {
            providerFileName = fileName;
        }
    }
}
