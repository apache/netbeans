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
import org.netbeans.api.java.classpath.ClassPath;
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
    private static final String JAXB_PLUGIN_GROUP_ID = "org.jvnet.jaxb"; //NOI18N
    private static final String JAXB_PLUGIN_ARTIFACT_ID = "jaxb-maven-plugin"; //NOI18N
    private static final String JAXB_PLUGIN_VERSION_JAVAX = "2.0.9"; //NOI18N
    private static final String JAXB_PLUGIN_VERSION_JAKARTA = "4.0.0"; //NOI18N
    private static final String JAXB_GENERATE_PREFIX = "jaxb-generate-"; //NOI18N

    private final Project project;

    MavenSchemaCompiler(Project project) {
        this.project = project;
    }

    @Override
    public void compileSchema(final WizardDescriptor wiz) {
        final String schemaName = (String) wiz.getProperty(JAXBWizModuleConstants.SCHEMA_NAME);

        String catalogFilePrep = (String) wiz.getProperty(
                    JAXBWizModuleConstants.CATALOG_FILE);
        if (catalogFilePrep != null && catalogFilePrep.trim().isEmpty()) {
            catalogFilePrep = null;
        }
        String catalogFile = catalogFilePrep;

        List<String> bindingFileList = (List<String>) wiz.getProperty(
                    JAXBWizModuleConstants.JAXB_BINDING_FILES);

        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            @Override
            public void performOperation(POMModel model) {
                org.netbeans.modules.maven.model.pom.Plugin plugin = addJaxb2Plugin(model); //NOI18N
                String packageName =
                        (String)wiz.getProperty(JAXBWizModuleConstants.PACKAGE_NAME);
                if (packageName != null && packageName.trim().length() == 0) {
                    packageName = null;
                }
                addJaxb2Execution(plugin, schemaName, packageName, catalogFile, bindingFileList);
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
                        retrieveResource(
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
                    retrieveResource(
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

        ClassPath cp = ClassPath.getClassPath(project.getProjectDirectory().getFileObject("src/main/java"), ClassPath.COMPILE);

        boolean javaxXmlBindingPresent = cp.findResource("javax/xml/bind/JAXBContext.class") != null; //NOI18N
        boolean jakartaXmlBindingPresent = cp.findResource("jakarta/xml/bind/JAXBContext.class") != null; //NOI18N
        boolean jakartaNamespace = jakartaXmlBindingPresent || (! javaxXmlBindingPresent);

        plugin = model.getFactory().createPlugin();
        plugin.setGroupId(JAXB_PLUGIN_GROUP_ID);
        plugin.setArtifactId(JAXB_PLUGIN_ARTIFACT_ID);
        if(jakartaNamespace) {
            plugin.setVersion(JAXB_PLUGIN_VERSION_JAKARTA);
        } else {
            plugin.setVersion(JAXB_PLUGIN_VERSION_JAVAX);
        }
        bld.addPlugin(plugin);

        // setup global configuration
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            config.setSimpleParameter("verbose", "true"); //NOI18N
            plugin.setConfiguration(config);
        }
        return plugin;
    }

    public static void addJaxb2Execution(Plugin plugin, String id, String packageName, String catalogFile, List<String> bindingFiles) {
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

        POMExtensibilityElement schemaIncludes = createPOMExtensibilityElement("schemaIncludes", model);  //NOI18N
        config.addExtensibilityElement(schemaIncludes);

        POMExtensibilityElement include = createPOMExtensibilityElement("include", model); //NOI18N
        include.setElementText("jaxb/"+id+"/*.xsd"); //NOI18N
        schemaIncludes.addExtensibilityElement(include);

        POMExtensibilityElement episodeFile = createPOMExtensibilityElement("episodeFile", model); //NOI18N
        episodeFile.setElementText("${project.build.directory}/generated-sources/xjc-" + id + "/META-INF/jaxb-" + id + ".episode"); //NOI18N
        config.addExtensibilityElement(episodeFile);

        if (catalogFile != null) {
            POMExtensibilityElement catalog = createPOMExtensibilityElement("catalog", model); //NOI18N
            catalog.setElementText(catalogFile); //NOI18N
            config.addExtensibilityElement(catalog);
        }

        if (bindingFiles != null && !bindingFiles.isEmpty()) {
            POMExtensibilityElement bindings = createPOMExtensibilityElement("bindings", model); //NOI18N
            config.addExtensibilityElement(bindings);

            for (String bindingFile : bindingFiles) {
                File bindingFileObject = new File(bindingFile);

                POMExtensibilityElement binding = createPOMExtensibilityElement("binding", model); //NOI18N
                bindings.addExtensibilityElement(binding);

                POMExtensibilityElement fileset = createPOMExtensibilityElement("fileset", model); //NOI18N
                binding.addExtensibilityElement(fileset);

                POMExtensibilityElement directory = createPOMExtensibilityElement("directory", model); //NOI18N
                fileset.addExtensibilityElement(directory);
                directory.setElementText(bindingFileObject.getParent());

                POMExtensibilityElement includes = createPOMExtensibilityElement("includes", model); //NOI18N
                fileset.addExtensibilityElement(includes);

                POMExtensibilityElement include2 = createPOMExtensibilityElement("include", model); //NOI18N
                includes.addExtensibilityElement(include2);
                include2.setElementText(bindingFileObject.getName());
            }
        }

        POMExtensibilityElement outputDirectory = createPOMExtensibilityElement("generateDirectory", model); //NOI18N
        outputDirectory.setElementText("${project.build.directory}/generated-sources/xjc-" + id); //NOI18N
        config.addExtensibilityElement(outputDirectory);

        if (packageName != null) {
            POMExtensibilityElement generatePackage = createPOMExtensibilityElement("generatePackage", model); //NOI18N
            generatePackage.setElementText(packageName); //NOI18N
            config.addExtensibilityElement(generatePackage);
        }
    }

    private static POMExtensibilityElement createPOMExtensibilityElement(String name, POMModel model) {
        QName qname = POMQName.createQName(name, model.getPOMQNames().isNSAware());
        return model.getFactory().createPOMExtensibilityElement(qname);
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
