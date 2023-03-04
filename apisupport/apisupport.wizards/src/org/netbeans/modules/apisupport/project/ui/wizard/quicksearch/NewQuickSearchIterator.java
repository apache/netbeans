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
package org.netbeans.modules.apisupport.project.ui.wizard.quicksearch;

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
 * Wizard for creating new providers for QuickSearch SPI
 *
 * @author Max Sauer
 */
@TemplateRegistration(folder = UIUtil.TEMPLATE_FOLDER, position = 800, displayName = "#Templates/NetBeansModuleDevelopment/newQuickSearch", iconBase = "org/netbeans/modules/apisupport/project/ui/wizard/quicksearch/newQuickSearch.png", description = "newQuickSearch.html", category = UIUtil.TEMPLATE_CATEGORY)
public class NewQuickSearchIterator extends BasicWizardIterator {

    private static final String[] HARDCODED_IMPORTS = new String[]{
        "org.netbeans.spi.quicksearch.SearchProvider", //NOI18N
        "org.netbeans.spi.quicksearch.SearchRequest", //NOI18N
        "org.netbeans.spi.quicksearch.SearchResponse" //NOI18N
    };
    private static final String lineSep = System.getProperty("line.separator"); // NOI18N
    /** datamodel passed through individual panels */
    private NewQuickSearchIterator.DataModel data;

    @Override
    protected Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewQuickSearchIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[]{
                    new QuickSearchPanel(wiz, data)
                };
    }

    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }

    static void generateFileChanges(DataModel model) {
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(model.getProject());

        //add module dependency
        cmf.add(cmf.addModuleDependency("org.netbeans.spi.quicksearch")); // NOI18N

        //create the java class implementing SearchProvider
        final String fileName = model.getClassName();
        FileObject template = CreatedModifiedFiles.getTemplate("quickSearch.java"); // NOI18N
        assert template != null;

        String actionPath = model.getDefaultPackagePath(fileName + ".java", false); // NOI18N

        Map<String, String> replaceTokens = new HashMap<String, String>();
        replaceTokens.put("CLASS_NAME", fileName); // NOI18N
        replaceTokens.put("PACKAGE_NAME", model.getPackageName()); // NOI18N
        Set<String> imports = new TreeSet<String>(Arrays.asList(HARDCODED_IMPORTS));
        StringBuffer importsBuffer = new StringBuffer();

        for (String imprt : imports) {
            importsBuffer.append("import " + imprt + ';' + lineSep); // NOI18N
        }
        replaceTokens.put("IMPORTS", importsBuffer.toString()); // NOI18N
        cmf.add(cmf.createFileWithSubstitutions(actionPath, template, replaceTokens));

        // add layer entry about the provider
        String category = "QuickSearch/" + model.getCategoryName().replace(" ", ""); // NOI18N
        String dashedPkgName = model.getPackageName().replace('.', '-');
        String dashedFqClassName = dashedPkgName + '-' + fileName;
        String instanceFullPath = category + "/" // NOI18N
                + dashedFqClassName + ".instance"; // NOI18N
        cmf.add(cmf.createLayerEntry(instanceFullPath, null, null, model.getCategoryName(), null));

        //<!--Attribute for command prefix - used to narrow search to this category only!-->
        //<attr name="command" stringvalue="p"/>
        cmf.add(cmf.createLayerAttribute(category, "command", model.commandPrefix)); // NOI18N

        //<!--Attribute for category ordering!-->
        //<attr name="position" intvalue="200"/>
        cmf.add(cmf.createLayerAttribute(category, "position", model.getPosition()));

        //at the end
        model.setCreatedModifiedFiles(cmf);
    }

    static final class DataModel extends BasicWizardIterator.BasicDataModel {

        private CreatedModifiedFiles files;
        private String categoryName;
        private String commandPrefix;
        private String className;
        private int position;

        public DataModel(WizardDescriptor wiz) {
            super(wiz);
        }

        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return files;
        }

        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        String getCategoryName() {
            return categoryName;
        }

        String getClassName() {
            return className;
        }

        String getCommandPrefix() {
            return commandPrefix;
        }

        Integer getPosition() {
            return position;
        }

        void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        void setClassName(String className) {
            this.className = className;
        }

        void setCommandPrefix(String commandPrefix) {
            this.commandPrefix = commandPrefix;
        }

        void setPosition(int parseInt) {
            this.position = parseInt;
        }
    }
}
