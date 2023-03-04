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

package org.netbeans.modules.profiler.j2ee;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "WebProjectUtils_CannotFindServletMsg=Cannot resolve servlet class generated from {0}. It will not be included into root methods.",
    "WebProjectUtils_CannotFindServletClassMsg=Cannot find servlet class {0} defined in deployment descriptor. Its methods will not be included into root methods.",
    "WebProjectUtils_CannotFindFilterClassMsg=Cannot find filter class {0} defined in deployment descriptor. Its methods will not be included into root methods.",
    "WebProjectUtils_CannotFindListenerClassMsg=Cannot find listener class {0} defined in deployment descriptor. Its methods will not be included into root methods."
})
public class WebProjectUtils {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static final Logger LOGGER = Logger.getLogger(WebProjectUtils.class.getName());
    
    private static final Map<ClientUtils.SourceCodeSelection, String> jspClass2NameMap = new HashMap<ClientUtils.SourceCodeSelection, String>();
    private static final String[][] jspServletMethods = new String[][] {
                                                            {
                                                                "_jspService",
                                                                "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V"
                                                            }
                                                        };

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static Document getDeploymentDescriptorDocument(FileObject deploymentDescriptorFile) {
        Document deploymentDescriptorDocument = null;

        try {
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            dbfactory.setValidating(false);

            DocumentBuilder builder = dbfactory.newDocumentBuilder();

            builder.setEntityResolver(new EntityResolver() {
                    public InputSource resolveEntity(String publicId, String systemId)
                                              throws SAXException, IOException {
                        StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
                        InputSource source = new InputSource(reader);
                        source.setPublicId(publicId);
                        source.setSystemId(systemId);

                        return source;
                    }
                });

            deploymentDescriptorDocument = builder.parse(FileUtil.toFile(deploymentDescriptorFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deploymentDescriptorDocument;
    }

    public static Collection<Document> getDeploymentDescriptorDocuments(Project project, boolean subprojects) {
        Collection<Document> documents = new ArrayList<Document>();

        for (FileObject ddFile : getDeploymentDescriptorFileObjects(project, subprojects)) {
            Document d = getDeploymentDescriptorDocument(ddFile);
            if (d != null) documents.add(d);
        }

        return documents;
    }

    public static Collection<FileObject> getDeploymentDescriptorFileObjects(Project project, boolean subprojects) {
        Collection<FileObject> descriptors = new ArrayList<FileObject>();

        for (WebModule wm : getWebModules(project, subprojects)) {
            FileObject d = wm.getDeploymentDescriptor();
            if (d != null) descriptors.add(d);
        }

        return descriptors;
    }

    public static Collection<FileObject> getDocumentBaseFileObjects(Project project, boolean subprojects) {
        Collection<FileObject> basefos = new ArrayList<FileObject>();

        for (WebModule wm : getWebModules(project, subprojects)) {
            FileObject d = wm.getDocumentBase();
            if (d != null) basefos.add(d);
        }

        return basefos;
    }

    public static Collection<File> getDocumentBaseFiles(Project project, boolean subprojects) {
        Collection<File> basefiles = new ArrayList<File>();

        for (FileObject basefo : getDocumentBaseFileObjects(project, subprojects)) {
            basefiles.add(FileUtil.toFile(basefo));
        }

        return basefiles;
    }

    public static boolean isWebProject(Lookup.Provider p) {
        assert p != null;
        return p.getLookup().lookup(WebModule.class) != null;
    }
    
    public static ArrayList[] getFilterClasses(Document deploymentDescriptorDocument) {
        ArrayList mappedFilters = new ArrayList();
        ArrayList notMappedFilters = new ArrayList();

        NodeList filtersList = getFilters(deploymentDescriptorDocument);
        NodeList filterMappingsList = getFilterMappings(deploymentDescriptorDocument);

        Collection<String> mappedFilterNames = new HashSet<String>();

        for (int i = 0; i < filterMappingsList.getLength(); i++) {
            String mappedFilterName = getElementContent((Element) filterMappingsList.item(i), "filter-name"); // NOI18N

            if ((mappedFilterName != null) && !mappedFilterNames.contains(mappedFilterName)) {
                mappedFilterNames.add(mappedFilterName);
            }
        }

        for (int i = 0; i < filtersList.getLength(); i++) {
            String filterName = getElementContent((Element) filtersList.item(i), "filter-name"); // NOI18N
            String filterClassName = getElementContent((Element) filtersList.item(i), "filter-class"); // NOI18N

            if ((filterName != null) && (filterClassName != null) && mappedFilterNames.contains(filterName)) {
                if (!mappedFilters.contains(filterClassName)) {
                    mappedFilters.add(filterClassName);
                }
            } else {
                if (!notMappedFilters.contains(filterClassName)) {
                    notMappedFilters.add(filterClassName);
                }
            }
        }

        return new ArrayList[] { mappedFilters, notMappedFilters };
    }

    public static NodeList getFilterMappings(Document deploymentDescriptorDocument) {
        return deploymentDescriptorDocument.getElementsByTagName("filter-mapping"); // NOI18N
    }

    public static NodeList getFilters(Document deploymentDescriptorDocument) {
        return deploymentDescriptorDocument.getElementsByTagName("filter"); // NOI18N
    }

    public static boolean isHttpServlet(FileObject fo) {
        // FIXME pass in the JavaProfilerSource instead
        JavaProfilerSource src = JavaProfilerSource.createFrom(fo);
        return src != null && src.isInstanceOf("javax.servlet.http.HttpServlet"); // NOI18N
    }

    public static boolean isJSP(FileObject fo) {
        return "jsp".equals(fo.getExt()); // NOI18N
    }

    public static String getJSPFileContext(Project project, FileObject jspFile, boolean subprojects) {
        Collection<FileObject> docBases = getDocumentBaseFileObjects(project, subprojects);
        String relPathUsed = null;

        for (FileObject docBase : docBases) {
            if (docBase == null) {
                continue;
            }

            String relativePath = FileUtil.getRelativePath(docBase, jspFile);

            if (relativePath == null) {
                continue;
            }

            if ((relPathUsed == null) || (relPathUsed.length() > relativePath.length())) {
                relPathUsed = relativePath;
            }
        }

        return relPathUsed;
    }

    public static ClientUtils.SourceCodeSelection getJSPFileRootMethod(Project project, FileObject jspFile) {
        return getJSPMethodSignature(project, jspFile);
    }

    public static ClientUtils.SourceCodeSelection getJSPMethodSignature(Project project, FileObject jspFile) {
        String jspPseudoServletClass = getJSPPseudoServletClass(project, jspFile);

        if (jspPseudoServletClass == null) {
            LOGGER.log(Level.WARNING, Bundle.WebProjectUtils_CannotFindServletMsg(FileUtil.toFile(jspFile).getPath()));
            return null; // According to Issue 62519, jsp file is not resolved/found due odd project layout
        }

        return getJSPMethodSignature(jspPseudoServletClass);
    }

    public static ClientUtils.SourceCodeSelection getJSPMethodSignature(String jspPseudoServletClass) {
        return new ClientUtils.SourceCodeSelection(jspPseudoServletClass, jspServletMethods[0][0], jspServletMethods[0][1]);
    }

    public static Set<ClientUtils.SourceCodeSelection> getJSPMethodSignatures(Project project, boolean subprojects) {
        Set<ClientUtils.SourceCodeSelection> methodSignatures = new HashSet<ClientUtils.SourceCodeSelection>();
        Collection<FileObject> baseFOs = getDocumentBaseFileObjects(project, subprojects);

        if ((baseFOs == null) || baseFOs.isEmpty()) {
            return methodSignatures;
        }

        for (FileObject baseFO : baseFOs) {
            int jspStringStartIndex = baseFO.getPath().length();
            List<FileObject> jspFileObjects = getJSPs(baseFO);
            ClientUtils.SourceCodeSelection methodDescription;

            for (FileObject jsp : jspFileObjects) {
                methodDescription = getJSPMethodSignature(project, jsp);

                if (methodDescription != null) {
                    // TODO: Get rid of this ridiculous formatter and enhance FlatProfileContainer to return a SourceCodeSelection of a certain row
                    jspClass2NameMap.put(methodDescription, jsp.getPath().substring(jspStringStartIndex));
                    // ****
                    methodSignatures.add(methodDescription);
                }
            }
        }

        return methodSignatures;
    }

    public static String getJSPPath(ClientUtils.SourceCodeSelection jspMethod) {
        return jspClass2NameMap.get(jspMethod);
    }

    public static String getJSPPseudoServletClass(Project project, FileObject jspFile) {
        String jspPseudoServletJavaFile = JSPServletFinder.findJSPServletFinder(jspFile)
                                                          .getServletResourcePath(getJSPFileContext(project, jspFile, true));

        if (jspPseudoServletJavaFile == null) {
            return null; // According to Issue 62519, jsp file is not resolved/found due odd project layout
        }

        String jspPseudoServletJavaClass = jspPseudoServletJavaFile.substring(0,
                                                                              jspPseudoServletJavaFile.length()
                                                                              - ".java".length()).replace('/', '.'); // NOI18N

        return jspPseudoServletJavaClass;
    }

    public static ClientUtils.SourceCodeSelection[] getJSPRootMethods(Project project, boolean subprojects) {
        Set<ClientUtils.SourceCodeSelection> jspRootMethodDescriptions = getJSPMethodSignatures(project, subprojects);

        if (jspRootMethodDescriptions == null) {
            return new ClientUtils.SourceCodeSelection[0];
        }

        return jspRootMethodDescriptions.toArray(new ClientUtils.SourceCodeSelection[0]);
    }

    public static ArrayList getJSPs(FileObject documentBase) {
        ArrayList jspFileObjects = new ArrayList();

        if (documentBase.isFolder()) {
            searchForJSPs(documentBase, jspFileObjects);
        }

        return jspFileObjects;
    }

    public static ArrayList getListenerClasses(Document deploymentDescriptorDocument) {
        ArrayList listeners = new ArrayList();
        NodeList listenersList = getListeners(deploymentDescriptorDocument);

        for (int i = 0; i < listenersList.getLength(); i++) {
            String listenerClass = getElementContent((Element) listenersList.item(i), "listener-class"); // NOI18N

            if ((listenerClass != null) && !listeners.contains(listenerClass)) {
                listeners.add(listenerClass);
            }
        }

        return listeners;
    }

    public static NodeList getListeners(Document deploymentDescriptorDocument) {
        return deploymentDescriptorDocument.getElementsByTagName("listener"); // NOI18N
    }

    public static boolean isMappedServlet(FileObject servlet, Project project, boolean subprojects) {
        Collection<Document> dds = getDeploymentDescriptorDocuments(project, subprojects);

        for (Document dd : dds) {
            if (getServletMapping(servlet, dd) != null) {
                return true;
            }
        }

        return false;
    }

    public static ArrayList[] getServletClasses(Document deploymentDescriptorDocument) {
        ArrayList mappedServlets = new ArrayList();
        ArrayList notMappedServlets = new ArrayList();

        NodeList servletsList = getServlets(deploymentDescriptorDocument);
        NodeList servletMappingsList = getServletMappings(deploymentDescriptorDocument);

        Collection<String> mappedServletNames = new HashSet<String>();

        for (int i = 0; i < servletMappingsList.getLength(); i++) {
            String mappedServletName = getElementContent((Element) servletMappingsList.item(i), "servlet-name"); // NOI18N

            if ((mappedServletName != null) && !mappedServletNames.contains(mappedServletName)) {
                mappedServletNames.add(mappedServletName);
            }
        }

        for (int i = 0; i < servletsList.getLength(); i++) {
            String servletName = getElementContent((Element) servletsList.item(i), "servlet-name"); // NOI18N
            String servletClassName = getElementContent((Element) servletsList.item(i), "servlet-class"); // NOI18N

            if ((servletName != null) && (servletClassName != null) && mappedServletNames.contains(servletName)) {
                if (!mappedServlets.contains(servletClassName)) {
                    mappedServlets.add(servletClassName);
                }
            } else {
                if (!notMappedServlets.contains(servletClassName)) {
                    notMappedServlets.add(servletClassName);
                }
            }
        }

        return new ArrayList[] { mappedServlets, notMappedServlets };
    }

    public static String getServletMapping(FileObject servletFO, Document deploymentDescriptorDocument) {
        // FIXME - pass in JavaProfilerSource param
        JavaProfilerSource src = JavaProfilerSource.createFrom(servletFO);
        if (src == null) {
            return null;
        }
        String servletClassName = src.getTopLevelClass().getVMName();

        if ((servletClassName == null) || (deploymentDescriptorDocument == null)) {
            return null;
        }

        NodeList servletsList = getServlets(deploymentDescriptorDocument);

        for (int i = 0; i < servletsList.getLength(); i++) {
            String servletName = getElementContent((Element) servletsList.item(i), "servlet-name"); // NOI18N
            String className = getElementContent((Element) servletsList.item(i), "servlet-class"); // NOI18N

            if ((servletName != null) && (className != null) && servletClassName.equals(className)) {
                NodeList servletMappingsList = getServletMappings(deploymentDescriptorDocument);

                for (int j = 0; j < servletMappingsList.getLength(); j++) {
                    if (servletName.equals(getElementContent((Element) servletMappingsList.item(j), "servlet-name"))) {
                        // NOI18N
                        return getElementContent((Element) servletMappingsList.item(j), "url-pattern"); // NOI18N
                    }
                }

                return null;
            }
        }

        return null;
    }

    public static NodeList getServletMappings(Document deploymentDescriptorDocument) {
        return deploymentDescriptorDocument.getElementsByTagName("servlet-mapping"); // NOI18N
    }

    public static NodeList getServlets(Document deploymentDescriptorDocument) {
        return deploymentDescriptorDocument.getElementsByTagName("servlet"); // NOI18N
    }

    // returns true if passed fo lives in /web directory
    public static boolean isWebDocumentSource(FileObject fo, Project project) {
        SourceGroup[] sg = ProjectUtils.getSources(project).getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);

        for (int i = 0; i < sg.length; i++) {
            if (FileUtil.isParentOf(sg[i].getRootFolder(), fo)) {
                return true;
            }
        }

        return false;
    }

    // returns true if passed fo lives in /src directory
    public static boolean isWebJavaSource(FileObject fo, Project project) {
        SourceGroup[] sg = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        for (int i = 0; i < sg.length; i++) {
            if (FileUtil.isParentOf(sg[i].getRootFolder(), fo)) {
                return true;
            }
        }

        return false;
    }

    public static Collection<WebModule> getWebModules(Project project, boolean subprojects) {
        Collection<WebModule> wms = new ArrayList<WebModule>();
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());

        if (wm != null) {
            wms.add(wm);
        }

        if (subprojects) {
            EarProvider earprovider = project.getLookup().lookup(EarProvider.class);

            if ((wm == null) && (earprovider != null)) {
                Set<Project> projects = new HashSet<Project>();
                ProjectUtilities.fetchSubprojects(project, projects);

                for (Project subp : projects) {
                    wms.addAll(getWebModules(subp, subprojects));
                }
            }
        }

        return wms;
    }

    public static void resetJSPNameCache() {
        jspClass2NameMap.clear();
    }

    private static String getElementContent(Element rootElement, String tagName) {
        if (rootElement == null) {
            return null;
        }

        NodeList elementsList = rootElement.getElementsByTagName(tagName);

        if ((elementsList == null) || (elementsList.getLength() == 0)) {
            return null;
        }

        Node element = elementsList.item(0);

        if (element == null) {
            return null;
        }

        String elementContents = element.getTextContent();

        if (elementContents == null) {
            return null;
        }

        return elementContents.trim();
    }

    private static void searchForJSPs(FileObject root, ArrayList jspFileObjects) {
        FileObject[] childs = root.getChildren();
        FileObject child;

        for (int i = 0; i < childs.length; i++) {
            child = childs[i];

            if (child.isFolder()) {
                searchForJSPs(child, jspFileObjects);
            } else if (child.isData() && "jsp".equals(child.getExt())) {
                jspFileObjects.add(child); // NOI18N
            }
        }
    }
}
