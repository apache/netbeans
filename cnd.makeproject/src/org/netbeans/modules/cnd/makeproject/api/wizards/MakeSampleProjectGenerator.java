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
package org.netbeans.modules.cnd.makeproject.api.wizards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.api.support.SmartOutputStream;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.cnd.makeproject.api.configurations.Platforms;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Create a sample web project by unzipping a template into some directory
 */
public class MakeSampleProjectGenerator {

    private static final String PROJECT_CONFIGURATION_FILE = MakeConfiguration.NBPROJECT_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML; // NOI18N
    private static final String PROJECT_PRIVATE_CONFIGURATION_FILE = MakeConfiguration.NBPROJECT_PRIVATE_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML; // NOI18N

    private MakeSampleProjectGenerator() {
    }

    public static Set<FileObject> createProjectFromTemplate(final FileObject template, ProjectGenerator.ProjectParameters prjParams) throws IOException {
        String mainProject = (String) template.getAttribute("mainProjectLocation"); // NOI18N
        if (mainProject != null && mainProject.length() > 0) {
            prjParams.setMainProject(mainProject);
        }
        String subProjects = (String) template.getAttribute("subProjectLocations"); // NOI18N
        if (subProjects != null && subProjects.length() > 0) {
            prjParams.setSubProjects(subProjects);
        }
        String postCreationClassName = (String) template.getAttribute("postProjectCreationClassName"); // NOI18N
        if (postCreationClassName != null && postCreationClassName.length() > 0) {
            prjParams.setPostCreationClassName(postCreationClassName);
        }
        if (mainProject != null) {
            final String projectFolderPath = prjParams.getProjectFolderPath();
            FOPath fopath = new FOPath(projectFolderPath);
            FileObject parentFolderLocation;
            if (fopath.root != null) {
                parentFolderLocation = FileUtil.createFolder(fopath.root, fopath.relPath);
            } else {
                parentFolderLocation = FileUtil.createFolder(prjParams.getSourceFileSystem().getRoot(), projectFolderPath);
            }
            FileObject mainProjectLocation;
            if (mainProject.equals(".")) { // NOI18N
                mainProjectLocation = parentFolderLocation;
            }
            else {
                mainProjectLocation = FileUtil.createFolder(parentFolderLocation, mainProject);
            }
            FileObject[] subProjectLocations = null;
            if (subProjects != null) {
                List<FileObject> subProjectsFiles = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(subProjects, ","); // NOI18N
                while (st.hasMoreTokens()) {
                    subProjectsFiles.add(FileUtil.createFolder(parentFolderLocation, st.nextToken()));
                }
                subProjectLocations = subProjectsFiles.toArray(new FileObject[subProjectsFiles.size()]);
            }
            return createProjectWithSubprojectsFromTemplate(template.getInputStream(), parentFolderLocation, mainProjectLocation, subProjectLocations, prjParams);
        } else {
            return createProjectFromTemplate(template.getInputStream(), prjParams);
        }
    }

    private static void addEmptyNode(Document doc, String nodeName) {
        Element data = null;
        NodeList list = doc.getElementsByTagName(MakeProjectType.PROJECT_CONFIGURATION_NAME);
        if (list != null && list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node instanceof Element) {
                    data = (Element) node;
                    break;
                }

            }
        }
        if (data == null) {
            return;
        }
        NodeList nodeList = data.getElementsByTagName(nodeName);
        if (nodeList == null) {
            return;
        }
        // Create new source root node
        Element element = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, nodeName);
        data.appendChild(element);
    }

    private static void postProcessProject(FileObject prjLoc, String name, ProjectGenerator.ProjectParameters prjParams) throws IOException {
        // update project.xml
        try {
            // Change project name in 'project.xml'
            FileObject fo = prjLoc.getFileObject(MakeProjectHelper.PROJECT_XML_PATH);
            if (fo == null) {
                throw new FileNotFoundException("" + prjLoc + '/' + MakeProjectHelper.PROJECT_XML_PATH);
            }
            Document doc = XMLUtil.parse(new InputSource(fo.getInputStream()), false, true, null, null);
            if (name != null) {
                //changeXmlFileByNameNS(doc, PROJECT_CONFIGURATION_NAMESPACE, "name", name, null); // NOI18N
                changeXmlFileByTagName(doc, "name", name, null); // NOI18N
            }
            addEmptyNode(doc, MakeProjectType.SOURCE_ROOT_LIST_ELEMENT);
            saveXml(doc, prjLoc, MakeProjectHelper.PROJECT_XML_PATH);

            // Change working dir and default conf in 'projectDescriptor.xml'
            //String workingDir = projectLocation.getPath();
//            String systemOs = getCurrentSystemOs();
            FileObject pfo = prjLoc.getFileObject(PROJECT_CONFIGURATION_FILE);
            if (pfo != null) {
                doc = XMLUtil.parse(new InputSource(pfo.getInputStream()), false, true, null, null);
                //changeXmlFileByTagName(doc, "buildCommandWorkingDir", workingDir, "X-PROJECTDIR-X"); // NOI18N
                //changeXmlFileByTagName(doc, "cleanCommandWorkingDir", workingDir, "X-PROJECTDIR-X"); // NOI18N
                //changeXmlFileByTagName(doc, "executablePath", workingDir, "X-PROJECTDIR-X"); // NOI18N
                //changeXmlFileByTagName(doc, "folderPath", workingDir, "X-PROJECTDIR-X"); // NOI18N
    //            changeXmlFileByTagName(doc, "defaultConf", systemOs, "X-DEFAULTCONF-X"); // NOI18N
                String hostUID = prjParams.getHostUID();
                ExecutionEnvironment env = null;
                if (hostUID != null) {
                    env = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
                }
                env = (env != null) ? env : ServerList.getDefaultRecord().getExecutionEnvironment();
                String prjHostUID = ExecutionEnvironmentFactory.toUniqueID(env);
                CompilerSetManager compilerSetManager = CompilerSetManager.get(env);
                ExecutionEnvironment projectEE = FileSystemProvider.getExecutionEnvironment(prjLoc);
                int platform;
                if (projectEE.isLocal()) {
                    platform = compilerSetManager.getPlatform();
                } else {
                    platform = CompilerSetManager.get(projectEE).getPlatform();
                }
                CompilerSet compilerSet = prjParams.getToolchain();
                compilerSet = (compilerSet != null) ? compilerSet : compilerSetManager.getDefaultCompilerSet();
                String variant = null;
                String csVariant = "GNU|GNU"; // NOI18N
                if (compilerSet != null) {
                    variant = MakeConfiguration.getVariant(compilerSet, platform);
                    csVariant = compilerSet.getName();
                    if (compilerSet.getCompilerFlavor() != null) {
                        csVariant += "|" + compilerSet.getCompilerFlavor().getToolchainDescriptor().getName(); // NOI18N
                    }
                } else {
                    CndUtils.assertTrue(false, "Expected not null tool collection"); // NOI18N
                }
                CndUtils.assertTrue(platform != PlatformTypes.PLATFORM_NONE, "Expected not Unknown platform"); // NOI18N
                if (prjParams.isDefaultToolchain()) {
                    csVariant = CompilerSet2Configuration.DEFAULT_CS;
                }
                //changeXmlFileByTagName(doc, "developmentServer", prjHostUID, "X-HOST-UID-X"); // NOI18N
                changeXmlFileByTagName(doc, CommonConfigurationXMLCodec.COMPILER_SET_ELEMENT, csVariant, "X-TOOLCHAIN-X"); // NOI18N
                if (platform == PlatformTypes.PLATFORM_WINDOWS) { // Utilities.isWindows()) {
                    changeXmlFileByTagName(doc, CommonConfigurationXMLCodec.OUTPUT_ELEMENT, "lib", "X-LIBPREFIX-X"); // NOI18N
                    changeXmlFileByTagAttrName(doc, CommonConfigurationXMLCodec.MAKE_ARTIFACT_ELEMENT, CommonConfigurationXMLCodec.MAKE_ARTIFACT_OP_ELEMENT, "lib", "X-LIBPREFIX-X"); // NOI18N
                    if (variant != null) {
                        changeXmlFileByTagAttrName(doc, CommonConfigurationXMLCodec.MAKE_ARTIFACT_ELEMENT, CommonConfigurationXMLCodec.MAKE_ARTIFACT_OP_ELEMENT, variant, "X-PLATFORM-X"); // NOI18N
                    }
                } else if (platform == PlatformTypes.PLATFORM_MACOSX) { //Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                    changeXmlFileByTagName(doc, CommonConfigurationXMLCodec.OUTPUT_ELEMENT, "lib", "X-LIBPREFIX-X"); // NOI18N
                    changeXmlFileByTagAttrName(doc, CommonConfigurationXMLCodec.MAKE_ARTIFACT_ELEMENT, CommonConfigurationXMLCodec.MAKE_ARTIFACT_OP_ELEMENT, "lib", "X-LIBPREFIX-X"); // NOI18N
                    if (variant != null) {
                        changeXmlFileByTagAttrName(doc, CommonConfigurationXMLCodec.MAKE_ARTIFACT_ELEMENT, CommonConfigurationXMLCodec.MAKE_ARTIFACT_OP_ELEMENT, variant, "X-PLATFORM-X"); // NOI18N
                    }
                } else {
                    changeXmlFileByTagName(doc, CommonConfigurationXMLCodec.OUTPUT_ELEMENT, "lib", "X-LIBPREFIX-X"); // NOI18N
                    changeXmlFileByTagAttrName(doc, CommonConfigurationXMLCodec.MAKE_ARTIFACT_ELEMENT, CommonConfigurationXMLCodec.MAKE_ARTIFACT_OP_ELEMENT, "lib", "X-LIBPREFIX-X"); // NOI18N
                    if (variant != null) {
                        changeXmlFileByTagAttrName(doc, CommonConfigurationXMLCodec.MAKE_ARTIFACT_ELEMENT, "OP", variant, "X-PLATFORM-X"); // NOI18N
                    }
                }
                saveXml(doc, prjLoc, PROJECT_CONFIGURATION_FILE);
                FileObject privateConfiguration = prjLoc.getFileObject(PROJECT_PRIVATE_CONFIGURATION_FILE);
                if (privateConfiguration != null) {
                    Document secodaryDoc = XMLUtil.parse(new InputSource(privateConfiguration.getInputStream()), false, true, null, null);
                    changeXmlFileByTagName(secodaryDoc, CommonConfigurationXMLCodec.DEVELOPMENT_SERVER_ELEMENT, prjHostUID, "X-HOST-UID-X"); // NOI18N
                    changeXmlFileByTagName(secodaryDoc, CommonConfigurationXMLCodec.PLATFORM_ELEMENT, "" + platform, "X-PLATFORM-INDEX-X"); // NOI18N
                    saveXml(secodaryDoc, prjLoc, PROJECT_PRIVATE_CONFIGURATION_FILE);
                } else {
                    // Create privete configuration with selected host
                    Document secodaryDoc = XMLUtil.createDocument(CommonConfigurationXMLCodec.CONFIGURATION_DESCRIPTOR_ELEMENT, null, null, null);
                    Element masterRoot = doc.getDocumentElement();
                    if (masterRoot != null) {
                        Element secodaryRoot = secodaryDoc.getDocumentElement();
                        secodaryRoot.setAttribute("version", masterRoot.getAttribute("version")); // NOI18N
                        NodeList masterConfsList = masterRoot.getElementsByTagName(CommonConfigurationXMLCodec.CONFS_ELEMENT);
                        if (masterConfsList.getLength() > 0) {
                            Element secondaryConfs = secodaryDoc.createElement(CommonConfigurationXMLCodec.CONFS_ELEMENT);
                            secodaryRoot.appendChild(secondaryConfs);
                            Node masterConfs = masterConfsList.item(0);
                            NodeList masterConfList = masterConfs.getChildNodes();
                            for (int i = 0; i < masterConfList.getLength(); i++) {
                                if ( masterConfList.item(i).getNodeType() != Node.ELEMENT_NODE) {
                                    continue;
                                }
                                Element masterConf = (Element) masterConfList.item(i);
                                Element secondaryConf = secodaryDoc.createElement(CommonConfigurationXMLCodec.CONF_ELEMENT);
                                secondaryConf.setAttribute("name", masterConf.getAttribute("name")); // NOI18N
                                secondaryConf.setAttribute("type", masterConf.getAttribute("type")); // NOI18N
                                secondaryConfs.appendChild(secondaryConf);
                                Element secondaryToolSet = secodaryDoc.createElement(CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT);
                                secondaryConf.appendChild(secondaryToolSet);
                                Element secondaryDevelopmentServer = secodaryDoc.createElement(CommonConfigurationXMLCodec.DEVELOPMENT_SERVER_ELEMENT);
                                secondaryToolSet.appendChild(secondaryDevelopmentServer);
                                secondaryDevelopmentServer.setTextContent(prjHostUID);
                                Element secondaryPlatform = secodaryDoc.createElement(CommonConfigurationXMLCodec.PLATFORM_ELEMENT);
                                secondaryToolSet.appendChild(secondaryPlatform);
                                secondaryPlatform.setTextContent("" + platform); // NOI18N
                            }
                        }
                    }
                    saveXml(secodaryDoc, prjLoc, PROJECT_PRIVATE_CONFIGURATION_FILE);
                }
                recordCreateSampleProject(env);
            }

//            // Custom post-creation process
//            PostProjectCreationProcessor ppcp = null;
//            String postCreationClassName = prjParams.getPostCreationClassName();
//            if (postCreationClassName != null && postCreationClassName.length() > 0) {
//                Collection<? extends PostProjectCreationProcessor> col = Lookup.getDefault().lookupAll(PostProjectCreationProcessor.class);
//                for (PostProjectCreationProcessor instance : col) {
//                    if (postCreationClassName.equals(instance.getClass().getName())) {
//                        ppcp = instance;
//                        break;
//                    }
//                }
//                if (ppcp != null) {
//                    ppcp.postProcess(prjLoc, prjParams);
//                }
//            }

        } catch (Exception e) {
            {
                // get more data for #230463 - sometimes IDE can't create "full remote" project
                final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(prjLoc);
                if (env.isRemote()) {
                    Logger.getLogger("remote.support.logger").log(Level.INFO, //NOI18N
                            "__DIAGNOSTICS__", new Object[]{prjLoc, Boolean.TRUE}); //NOI18N
                    ProcessUtils.ExitStatus rc = ProcessUtils.execute(env, "find", prjLoc.getPath(), " -ls"); //NOI18N
                    System.err.printf("Find in%n%s%nexited with rc=%d;%nerr=%s%nout=%n%s%n", prjLoc.getPath(), rc.exitCode, rc.getErrorString(), rc.getOutputString()); //NOI18N
                }
            }
            IOException ex = new IOException(e);
            throw ex;
        }
    }

    private static void customPostProcessProject(FileObject prjLoc, String name, ProjectGenerator.ProjectParameters prjParams) {
        // Custom post-creation process
        PostProjectCreationProcessor ppcp = null;
        String postCreationClassName = prjParams.getPostCreationClassName();
        if (postCreationClassName != null && postCreationClassName.length() > 0) {
            Collection<? extends PostProjectCreationProcessor> col = Lookup.getDefault().lookupAll(PostProjectCreationProcessor.class);
            for (PostProjectCreationProcessor instance : col) {
                if (postCreationClassName.equals(instance.getClass().getName())) {
                    ppcp = instance;
                    break;
                }
            }
            if (ppcp != null) {
                ppcp.postProcess(prjLoc, prjParams);
            }
        }
    }

    // http://wiki.netbeans.org/UsageLoggingSpecification
    private static void recordCreateSampleProject(ExecutionEnvironment env) {
        CompilerSetManager compilerSetManager = CompilerSetManager.get(env);
        CompilerSet compilerSet = compilerSetManager.getDefaultCompilerSet();
        String host;
        if (env.isLocal()) {
            host = "LOCAL"; // NOI18N
        } else {
            host = "REMOTE"; // NOI18N
        }
        String platform;
        if (compilerSet != null && Platforms.getPlatform(compilerSetManager.getPlatform()) != null) {
            platform = Platforms.getPlatform(compilerSetManager.getPlatform()).getName();
        } else {
            platform = "UNKNOWN_PLATFORM"; // NOI18N
        }
        String flavor;
        String[] families;
        if (compilerSet != null) {
            families = compilerSet.getCompilerFlavor().getToolchainDescriptor().getFamily();
            flavor = compilerSet.getCompilerFlavor().getToolchainDescriptor().getName();
        } else {
            families = new String[0];
            flavor = "UKNOWN"; // NOI18N
        }
        String family;
        if (families.length == 0) {
            family = "UKNOWN"; // NOI18N
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < families.length; i++) {
                buffer.append(families[i]);
                if (i < families.length - 1) {
                    buffer.append(","); // NOI18N
                }
            }
            family = buffer.toString();
        }
        UIGesturesSupport.submit(ConfigurationDescriptorProvider.USG_PROJECT_CREATE_CND,
                "APPLICATION", flavor, family, host, platform, "SAMPLE_PROJECT"); //NOI18N
    }

    public static final class FOPath {
        public FileObject root;
        public String relPath;
        public FOPath(String projectFolderPath) {
            if (projectFolderPath.length()>1 && projectFolderPath.charAt(1) == ':') {
                // This is ugly work around Bug #203507
                try {
                    if (projectFolderPath.length()>2 && projectFolderPath.charAt(2) == '\\') {
                        File driver = new File(projectFolderPath.substring(0,3));
                        URL url = Utilities.toURI(driver).toURL();
                        root = URLMapper.findFileObject(url);
                        relPath = projectFolderPath.substring(3);
                    } else {
                        File driver = new File(projectFolderPath.substring(0,2));
                        URL url = Utilities.toURI(driver).toURL();
                        root = URLMapper.findFileObject(url);
                        relPath = projectFolderPath.substring(2);
                    }
                } catch (Throwable e) {
                }
            } else if (projectFolderPath.startsWith("\\\\")) { //NOI18N
                // This is ugly work around Bug #215790
                int index = projectFolderPath.indexOf('\\', 2); //NOI18N
                if (index > 0) {
                    index = projectFolderPath.indexOf('\\', index+1); //NOI18N
                    if (index > 0) {
                        try {
                            File server = new File(projectFolderPath.substring(0,index));
                            URL url = Utilities.toURI(server).toURL();
                            root = URLMapper.findFileObject(url);
                            relPath = projectFolderPath.substring(index+1);
                        } catch (Throwable e) {
                        }
                    }
                }
            }
        }
    }

    public static Set<FileObject> createProjectFromTemplate(InputStream inputStream, ProjectGenerator.ProjectParameters prjParams) throws IOException {
        String projectFolderPath = prjParams.getProjectFolderPath();
        FOPath fopath = new FOPath(projectFolderPath);
        FileObject prjLoc;
        if (fopath.root != null) {
            prjLoc = FileUtil.createFolder(fopath.root, fopath.relPath);
        } else {
            prjLoc = FileUtil.createFolder(prjParams.getSourceFileSystem().getRoot(), projectFolderPath);
        }
        FileSystemProvider.suspendWritesUpload(prjLoc);
        try {
            unzip(inputStream, prjLoc);
            postProcessProject(prjLoc, prjParams.getProjectName(), prjParams);
            customPostProcessProject(prjLoc, prjParams.getProjectName(), prjParams);
        } finally {
            try {
                FileSystemProvider.resumeWritesUpload(prjLoc);
            } catch (InterruptedException ex) {
                InterruptedIOException iioe = new InterruptedIOException(ex.getMessage());
                iioe.setStackTrace(ex.getStackTrace());
                throw iioe;
            }
        }

        return Collections.singleton(prjLoc);
    }

    private static void addToSet(List<FileObject> set, FileObject projectFile, ProjectGenerator.ProjectParameters prjParams, String projectName) throws IOException {
        try {
            postProcessProject(projectFile, projectName, prjParams);
            projectFile.refresh(false);
            set.add(projectFile);
        } catch (Exception e) {
            IOException ex = new IOException(e);
            throw ex;
        }
    }

    private static Set<FileObject> createProjectWithSubprojectsFromTemplate(InputStream templateResourceStream, FileObject parentFolderLocation, FileObject mainProjectLocation, FileObject[] subProjectLocations, ProjectGenerator.ProjectParameters prjParams) throws IOException {
        List<FileObject> set = new ArrayList<>();
        FileSystemProvider.suspendWritesUpload(parentFolderLocation);
        try {
            unzip(templateResourceStream, parentFolderLocation);
            addToSet(set, mainProjectLocation, prjParams, prjParams.getProjectName());
            if (subProjectLocations != null) {
                for (int i = 0; i < subProjectLocations.length; i++) {
                    addToSet(set, subProjectLocations[i], prjParams, null);
                }
            }
            FileObject prjLoc = CndFileUtils.toFileObject(prjParams.getProjectFolder());
            customPostProcessProject(prjLoc, prjParams.getProjectName(), prjParams);
        } finally {
            try {
                FileSystemProvider.resumeWritesUpload(parentFolderLocation);
            } catch (InterruptedException ex) {
                InterruptedIOException iis = new InterruptedIOException(ex.getMessage());
                iis.setStackTrace(ex.getStackTrace());
                throw iis;
            }
        }
        return new LinkedHashSet<>(set);
    }

    private static void changeXmlFileByNameNS(Document doc, String tagNameNS, String tagName, String newText, String regex) throws IOException {
        NodeList nlist = doc.getElementsByTagNameNS(tagNameNS, tagName); // NOI18N
        changeXmlFileByNodeList(nlist, newText, regex);
    }

    private static void changeXmlFileByTagName(Document doc, String tagName, String newText, String regex) throws IOException {
        NodeList nlist = doc.getElementsByTagName(tagName); // NOI18N
        changeXmlFileByNodeList(nlist, newText, regex);
    }

    private static void changeXmlFileByTagAttrName(Document doc, String tagName, String attrName, String newText, String regex) throws IOException {
        NodeList nlist = doc.getElementsByTagName(tagName); // NOI18N
        changeXmlFileByAttrList(nlist, attrName, newText, regex);
    }

    private static void changeXmlFileByNodeList(NodeList nlist, String newText, String regex) throws IOException {
        if (nlist != null) {
            for (int i = 0; i < nlist.getLength(); i++) {
                Node n = nlist.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element e = (Element) n;
                replaceText(e, newText, regex);
            }
        }
    }

    private static void changeXmlFileByAttrList(NodeList nlist, String attrName, String newText, String regex) throws IOException {
        if (nlist != null) {
            for (int i = 0; i < nlist.getLength(); i++) {
                Node n = nlist.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element e = (Element) n;
                Attr attr = e.getAttributeNode(attrName);
                if (attr != null) {
                    attr.setValue(attr.getValue().replaceAll(regex, newText));
                }
            }
        }
    }

    private static void unzip(InputStream source, FileObject targetFolder) throws IOException {
        ZipInputStream zip = new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                if (ent.isDirectory()) {
                    FileObject f = FileUtil.createFolder(targetFolder, ent.getName());
                } else {
                    FileObject f = FileUtil.createData(targetFolder, ent.getName());
                    OutputStream out = f.getOutputStream();
                    try {
                        String ls;
                        if (ent.getName().toLowerCase(Locale.getDefault()).contains("makefile")) { // NOI18N
                            ls = "\n"; // NOI18N
                        } else {
                            ls = (String) f.getAttribute(FileObject.DEFAULT_LINE_SEPARATOR_ATTR);
                            if (ls == null) {
                                ls = System.getProperty("line.separator"); // NOI18N
                            }
                        }
                        copy(zip, out, ls);
                    } finally {
                        out.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }

    /**
     * Replacement for FileUtil.copy(). The problem with FU.c is that on Windows it terminates lines with
     * <CRLF> rather than <LF>. Now that we do remote development, this means that if a remote project is
     * created on Windows to be built by Sun Studio's dmake, then the <CRLF> breaks the build (this is
     * probably true with Solaris "make" as well).
     *
     * @param is The InputStream
     * @param os The Output Stream
     * @throws java.io.IOException
     */
    private static void copy(InputStream is, OutputStream os, String ls) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8")); //NOI18N
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, FileEncodingQuery.getDefaultEncoding()));
        String line;

        while ((line = br.readLine()) != null) {
            bw.write(line + ls); // NOI18N
        }
        bw.flush();
    }

    private static void replaceText(Element parent, String name, String regex) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text) l.item(i);
                if (regex != null) {
                    String s = text.getNodeValue();
                    text.setNodeValue(s.replaceAll(regex, name));
                } else {
                    text.setNodeValue(name);
                }
                return;
            }
        }
    }

    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
        FileObject xml = FileUtil.createData(dir, path);
        FileLock lock = xml.lock();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
            final byte[] data = SmartOutputStream.convertLineSeparator(baos, xml, xml.getParent());
            OutputStream os = SmartOutputStream.getSmartOutputStream(xml, lock);
            try {
                os.write(data);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
}
