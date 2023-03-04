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

package org.netbeans.modules.maven.jaxws.wizards;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.spi.SchemaCompiler;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkuchtiak
 */
public class MavenSchemaCompiler implements SchemaCompiler {
    private static final String JAVA_SE_CONFIG_DIR = "resources/jaxb"; //NOI18N
    private static final String JAXB_PLUGIN_GROUP_ID = "org.jvnet.jaxb2.maven2"; //NOI18N
    private static final String JAXB_PLUGIN_ARTIFACT_ID = "maven-jaxb2-plugin"; //NOI18N
    private static final String JAXB_GENERATE_PREFIX = "jaxb-generate-"; //NOI18N

    private Project project;
    MavenSchemaCompiler(Project project) {
        this.project = project;
    }

    @Override
    public void compileSchema(final WizardDescriptor wiz) {
        final String schemaName = (String) wiz.getProperty(JAXBWizModuleConstants.SCHEMA_NAME);
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            @Override
            public void performOperation(POMModel model) {
                org.netbeans.modules.maven.model.pom.Plugin plugin = addJaxb2Plugin(model); //NOI18N
                String packageName =
                        (String)wiz.getProperty(JAXBWizModuleConstants.PACKAGE_NAME);
                if (packageName != null && packageName.trim().length() == 0) {
                    packageName = null;
                }
                addJaxb2Execution(plugin, schemaName, packageName);
            }
        };
        Utilities.performPOMModelOperations(project.getProjectDirectory().getFileObject("pom.xml"),
                Collections.singletonList(operation));
    }

    @Override
    public void importResources(WizardDescriptor wiz) throws IOException {
        List<String> xsdFileList = (List<String>) wiz.getProperty(
                JAXBWizModuleConstants.XSD_FILE_LIST );

        if (xsdFileList != null) {
            String schemaName = (String) wiz.getProperty(JAXBWizModuleConstants.SCHEMA_NAME);

            /*List<String> bindingFileList = (List<String>) wiz.getProperty(
                    JAXBWizModuleConstants.JAXB_BINDING_FILES);

            String catlogFile = (String) wiz.getProperty(
                    JAXBWizModuleConstants.CATALOG_FILE);*/

            boolean srcLocTypeUrl = JAXBWizModuleConstants.SRC_LOC_TYPE_URL.equals(
                    (String) wiz.getProperty(
                    JAXBWizModuleConstants.SOURCE_LOCATION_TYPE));

            // import schemas
            if (srcLocTypeUrl) {
                // URL
                for (int i = 0; i < xsdFileList.size(); i++) {
                    String url = xsdFileList.get(i);
                    URL schemaURL = new URL(url);
                    try {
                        FileObject newFileFO = retrieveResource(
                               getSchemaFolder(schemaName),
                               schemaURL.toURI());
                    } catch (URISyntaxException ex) {
                        throw new IOException(ex.getMessage());
                    }
                }
            } else {
                // Local File
                FileObject projFO = project.getProjectDirectory();
                File projDir = FileUtil.toFile(projFO);
                for (int i = 0; i < xsdFileList.size(); i++) {
                    File srcFile = Relative2AbsolutePath(projDir,
                                xsdFileList.get(i));
                    FileObject newFileFO = retrieveResource(
                           getSchemaFolder(schemaName),
                           srcFile.toURI());
                }
            }
        }
    }

    private static FileObject retrieveResource(FileObject targetFolder,
            URI source){
        Retriever retriever = Retriever.getDefault();
        FileObject result = null;
        try {
            result = retriever.retrieveResource(targetFolder, source);
        } catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (result == null) {
            // XXX TODO Handle or log exception.
            // Map map = retriever.getRetrievedResourceExceptionMap();
        }
        return result;
    }

    public FileObject getSchemaFolder(String schemaName) throws IOException {
        FileObject mainFolder = project.getProjectDirectory().getFileObject("src/main"); //NOI18N
        if (mainFolder != null) {
            FileObject resourcesFolder = mainFolder.getFileObject("resources"); //NOI18N
            if (resourcesFolder == null) {
                resourcesFolder = mainFolder.createFolder("resources"); //NOI18N
            }
            if (resourcesFolder != null) {
                FileObject jaxbFolder = resourcesFolder.getFileObject("jaxb"); //NOI18N
                if (jaxbFolder == null) {
                    jaxbFolder = resourcesFolder.createFolder("jaxb"); //NOI18N
                }
                if (jaxbFolder != null) {
                    FileObject schemaFolder = jaxbFolder.getFileObject(schemaName); //NOI18N
                    if (schemaFolder == null) {
                        schemaFolder = jaxbFolder.createFolder(schemaName); //NOI18N
                    }
                    return schemaFolder;
                }
            }
        }
        return null;
    }

    private static File Relative2AbsolutePath(File base, String relPath){
        File relPathFile = new File(relPath);
        File absPath = null;
        if (!relPathFile.isAbsolute()){
            absPath = new File(base, relPath);
        } else {
            absPath = relPathFile;
        }

        return absPath;
    }

    private Plugin addJaxb2Plugin(POMModel model) {
        assert model.isIntransaction() : "need to call model modifications under transaction."; //NOI18N
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            model.getProject().setBuild(bld);
        }
        Plugin plugin = bld.findPluginById(JAXB_PLUGIN_GROUP_ID, JAXB_PLUGIN_ARTIFACT_ID);
        if (plugin != null) {
            //TODO CHECK THE ACTUAL PARAMETER VALUES..
            return plugin;
        }
        plugin = model.getFactory().createPlugin();
        plugin.setGroupId(JAXB_PLUGIN_GROUP_ID);
        plugin.setArtifactId(JAXB_PLUGIN_ARTIFACT_ID);
        plugin.setVersion("0.12.0"); //NOI18N
        bld.addPlugin(plugin);

        // setup global configuration
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            config.setSimpleParameter("catalog", "src/main/resources/jaxb/catalog.xml"); //NOI18N
            config.setSimpleParameter("catalogResolver", "org.jvnet.jaxb2.maven2.resolver.tools.ClasspathCatalogResolver"); //NOI18N
            config.setSimpleParameter("forceRegenerate", "true"); //NOI18N
            config.setSimpleParameter("generateDirectory", "${project.build.directory}/generated-sources/xjc"); //NOI18N
            config.setSimpleParameter("verbose", "true"); //NOI18N
            plugin.setConfiguration(config);
        }
        return plugin;
    }

   public static void addJaxb2Execution(Plugin plugin, String id, String packageName) {
        POMModel model = plugin.getModel();
        assert model.isIntransaction();

        PluginExecution exec = model.getFactory().createExecution();
        String uniqueId = getUniqueId(plugin, id);
        exec.setId(JAXB_GENERATE_PREFIX+uniqueId);
        //exec.setPhase("generate-sources"); //NOI18N
        exec.addGoal("generate"); //NOI18N
        plugin.addExecution(exec);

        Configuration config = model.getFactory().createConfiguration();
        exec.setConfiguration(config);

        QName qname = POMQName.createQName("schemaIncludes", model.getPOMQNames().isNSAware()); //NOI18N
        POMExtensibilityElement schemaIncludes = model.getFactory().createPOMExtensibilityElement(qname);
        config.addExtensibilityElement(schemaIncludes);

        qname = POMQName.createQName("include", model.getPOMQNames().isNSAware()); //NOI18N
        POMExtensibilityElement include = model.getFactory().createPOMExtensibilityElement(qname);
        include.setElementText("jaxb/"+id+"/*.xsd"); //NOI18N
        schemaIncludes.addExtensibilityElement(include);

        qname = POMQName.createQName("episodeFile", model.getPOMQNames().isNSAware()); //NOI18N
        POMExtensibilityElement episodeFile = model.getFactory().createPOMExtensibilityElement(qname);
        episodeFile.setElementText("${project.build.directory}/generated-sources/xjc/META-INF/jaxb-"+id+".episode"); //NOI18N
        config.addExtensibilityElement(episodeFile);

        if (packageName != null) {
            qname = POMQName.createQName("generatePackage", model.getPOMQNames().isNSAware()); //NOI18N
            POMExtensibilityElement generatePackage = model.getFactory().createPOMExtensibilityElement(qname);
            generatePackage.setElementText(packageName); //NOI18N
            config.addExtensibilityElement(generatePackage);
        }
    }

    private static String getUniqueId(Plugin plugin, String id) {
        String result = id;
        List<PluginExecution> executions = plugin.getExecutions();
        if (executions != null) {
            Set<String> execIdSet = new HashSet<String>();
            for (PluginExecution ex : executions) {
                String execId = ex.getId();
                if (execId != null) {
                    if (execId.startsWith(JAXB_GENERATE_PREFIX)) {
                        execIdSet.add(execId.substring(JAXB_GENERATE_PREFIX.length()));
                    } else {
                        execIdSet.add(execId);
                    }
                }
            }

            int i=1;
            while (execIdSet.contains(result)) {
                result = id+"_"+String.valueOf(i++); //NOI18N
            }
        }
        return result;
    }

}
