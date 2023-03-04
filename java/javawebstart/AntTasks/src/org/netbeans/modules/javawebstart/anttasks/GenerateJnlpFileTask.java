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
package org.netbeans.modules.javawebstart.anttasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PathTokenizer;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Milan Kubec
 * @author Petr Somol
 */
public class GenerateJnlpFileTask extends Task {
    
    private File destFile;
    private File destDir;
    private File template;
    private File properties;
    private Path lazyJars;
    
    // jars in /lib signed with different signer/keystore
    // must be included in jnlp resources through jnlp extensions
    public static final String EXTERNAL_JARS_PROP = "jar.files.to.include.through.external.jnlp"; //NOI18N
    public static final String EXTERNAL_JNLPS_PROP = "external.jnlp.component.names"; //NOI18N
    public static final String EXTERNAL_PROP_DELIMITER = ";"; //NOI18N

    private static final String EXT_RESOURCE_PROPNAME_PREFIX = "jnlp.ext.resource."; //NOI18N
    private static String[] EXT_RESOURCE_SUFFIXES = new String[] { "href", "name", "version" }; //NOI18N
    private static String[] EXT_RESOURCE_SUFFIXES_REQUIRED = new String[] { "href" }; //NOI18N
    
    private static final String APPLET_PARAM_PROPNAME_PREFIX = "jnlp.applet.param."; //NOI18N
    private static String[] APPLET_PARAM_SUFFIXES = new String[] { "name", "value" }; //NOI18N
    
    private static final String DEFAULT_JNLP_CODEBASE = "${jnlp.codebase}"; //NOI18N
    private static final String DEFAULT_JNLP_FILENAME = "launch.jnlp"; //NOI18N
    private static final String DEFAULT_APPLICATION_TITLE = "${APPLICATION.TITLE}"; //NOI18N
    private static final String DEFAULT_APPLICATION_VENDOR = "${APPLICATION.VENDOR}"; //NOI18N
    private static final String DEFAULT_APPLICATION_HOMEPAGE = "${APPLICATION.HOMEPAGE}"; //NOI18N
    private static final String DEFAULT_APPLICATION_DESC = "${APPLICATION.DESC}"; //NOI18N
    private static final String DEFAULT_APPLICATION_DESC_SHORT = "${APPLICATION.DESC.SHORT}"; //NOI18N
    private static final String DEFAULT_JNLP_ICON = "${JNLP.ICONS}"; //NOI18N
    private static final String DEFAULT_JNLP_OFFLINE = "${JNLP.OFFLINE.ALLOWED}"; //NOI18N
    private static final String JNLP_UPDATE = "${JNLP.UPDATE}"; //NOI18N
    private static final String DEFAULT_JNLP_SECURITY = "${JNLP.SECURITY}"; //NOI18N
    private static final String DEFAULT_JNLP_RESOURCES_RUNTIME = "${JNLP.RESOURCES.RUNTIME}"; //NOI18N
    private static final String DEFAULT_JNLP_RESOURCES_MAIN_JAR = "${JNLP.RESOURCES.MAIN.JAR}"; //NOI18N
    private static final String DEFAULT_JNLP_RESOURCES_JARS = "${JNLP.RESOURCES.JARS}"; //NOI18N
    private static final String DEFAULT_JNLP_RESOURCES_EXTENSIONS = "${JNLP.RESOURCES.EXTENSIONS}"; //NOI18N
    private static final String DEFAULT_JNLP_MAIN_CLASS = "${jnlp.main.class}"; //NOI18N
    private static final String DEFAULT_JNLP_APPLICATION_ARGS = "${JNLP.APPLICATION.ARGS}"; //NOI18N
    private static final String DEFAULT_JNLP_APPLET_PARAMS = "${JNLP.APPLET.PARAMS}"; //NOI18N
    private static final String DEFAULT_JNLP_APPLET_WIDTH = "${jnlp.applet.width}"; //NOI18N
    private static final String DEFAULT_JNLP_APPLET_HEIGHT = "${jnlp.applet.height}"; //NOI18N
    private static final String JNLP_LAZY_FORMAT = "jnlp.lazy.jar.%s"; //NOI18N
    
    private static final String DESC_APPLICATION = "application-desc"; //NOI18N
    private static final String DESC_APPLET = "applet-desc"; //NOI18N
    private static final String DESC_COMPONENT = "component-desc"; //NOI18N
    private static final String DESC_INSTALLER = "installer-desc"; //NOI18N
    
    public void setDestfile(File file) {
        this.destFile = file;
    }
    
    public void setDestDir(File dir) {
        this.destDir = dir;
    }
    
    public void setTemplate(File file) {
        this.template = file;
    }

    public void setLazyJars(final Path lazyJars) {
        this.lazyJars = lazyJars;
    }

    // XXX ??? properties that will override those 
    // available via getProject().getProperty()
    public void setProperties(File file) {
        this.properties = file;
    }
    
    private Document loadTemplate(File tempFile) throws IOException {
        Document docDom = null;
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            docDom = docBuilder.parse(tempFile);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GenerateJnlpFileTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(GenerateJnlpFileTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return docDom;
    }
    
    @Override
    public void execute() throws BuildException {
        
        checkParameters();
        
        Document docDom = null;
        if (template != null) {
            try {
                docDom = loadTemplate(template);
            } catch (IOException ex) {
                throw new BuildException(ex, getLocation());
            }
        }
        
        if (docDom == null) {
            throw new BuildException("Template file is either missing or broken XML document, cannot generate JNLP file.", getLocation()); //NOI18N
        }
        
        // LoadProperties ??
        processDocument(docDom);
        
        Transformer tr;
        try {
            tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
            tr.setOutputProperty(OutputKeys.METHOD,"xml"); //NOI18N
            tr.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml"); //NOI18N
            tr.transform(new DOMSource(docDom), new StreamResult(new FileOutputStream(destFile)));
        } catch (TransformerConfigurationException ex) {
            throw new BuildException(ex, getLocation());
        } catch (TransformerException ex) {
            throw new BuildException(ex, getLocation());
        } catch (FileNotFoundException ex) {
            throw new BuildException(ex, getLocation());
        }
        
    }
    
    private void checkParameters() {
        if (destFile == null) {
            throw new BuildException("Destination file is not set, jnlp file cannot be created."); //NOI18N
        }
        if (destDir == null) {
            throw new BuildException("Destination directory is not set, jnlp file cannot be created."); //NOI18N
        }
        if (template == null) {
            throw new BuildException("Template file is not set, jnlp file cannot be created."); //NOI18N
        }
    }
    
    private void processDocument(Document docDom) {
        processJnlpElem(docDom);
    }
    
    private void processJnlpElem(Document docDom) {
        
        Node jnlpElem = docDom.getElementsByTagName("jnlp").item(0); //NOI18N
        assert jnlpElem != null;
        
        String specAttr = ((Element) jnlpElem).getAttribute("spec"); //NOI18N
        String specProp = getProject().getProperty("jnlp.spec"); //NOI18N // property in project.properties
        log("jnlp.spec = " + specProp, Project.MSG_VERBOSE); //NOI18N
        if (specProp!= null && !specAttr.equals(specProp)) {
            ((Element) jnlpElem).setAttribute("spec", specProp); //NOI18N
        }
        
        String codebaseAttr = ((Element) jnlpElem).getAttribute("codebase"); //NOI18N
        String codebaseTypeProp = getProject().getProperty("jnlp.codebase.type"); //NOI18N // property in project.properties
        String codebaseProp = null;
        if (codebaseTypeProp.equals("local")) { //NOI18N
            codebaseProp = getProject().getProperty("jnlp.local.codebase.url"); //NOI18N
        } else if (codebaseTypeProp.equals("web")) { //NOI18N
            codebaseProp = getProject().getProperty("jnlp.codebase.url"); //NOI18N // property in project.properties
        } else if (codebaseTypeProp.equals("user")) { //NOI18N
            codebaseProp = getProject().getProperty("jnlp.codebase.user"); //NOI18N // property in project.properties
        }
        log("jnlp.codebase.url = " + codebaseProp, Project.MSG_VERBOSE); //NOI18N
        if (codebaseAttr.equals(DEFAULT_JNLP_CODEBASE)) {   // default value => replace
            if (codebaseTypeProp.equals("no.codebase")) {   //NOI18N
                ((Element)jnlpElem).removeAttribute("codebase");    //NOI18N
            } else if (codebaseProp != null) {
                ((Element) jnlpElem).setAttribute("codebase", codebaseProp); //NOI18N
            }
        }
        
        String hrefAttr = ((Element) jnlpElem).getAttribute("href"); //NOI18N
        String jnlpFileNameProp = getProject().getProperty("jnlp.file.name"); //NOI18N // property in project.properties
        log("jnlp.file.name = " + jnlpFileNameProp, Project.MSG_VERBOSE); //NOI18N
        if (jnlpFileNameProp != null && (hrefAttr.equals(DEFAULT_JNLP_FILENAME))) { //NOI18N // default value => replace
            ((Element) jnlpElem).setAttribute("href", jnlpFileNameProp); //NOI18N
        }
        
        processInformationElem(docDom);
        processBackgroundElem(docDom, jnlpElem);
        processSecurityElem(docDom, jnlpElem);
        processResourcesElem(docDom);
        processDescriptorElem(docDom);
        
    }

    private void processInformationElem(Document docDom) {
        
        NodeList nodeList = docDom.getElementsByTagName("information"); //NOI18N
        int listLen = nodeList.getLength();
        for (int j = 0; j < listLen; j++) {
            Node informationElem = nodeList.item(j);
            assert informationElem != null;
            
            NodeList childNodes = informationElem.getChildNodes();
            int len = childNodes.getLength();
            for (int i = 0; i < len; i++) {
                Node node = childNodes.item(i);
                if (node != null) { // node might be null (don't know why)
                    String elemName = node.getNodeName();
                    String elemText = node.getTextContent();
                    switch (node.getNodeType()) {
                        case Node.ELEMENT_NODE:
                            if (elemName.equals("title")) { //NOI18N
                                String titleProp = getProperty("application.title", "Application Title"); //NOI18N // property in project.properties
                                log("application.title = " + titleProp, Project.MSG_VERBOSE); // NOI18N
                                if (elemText.equals(DEFAULT_APPLICATION_TITLE)) {
                                    node.setTextContent(titleProp);
                                }
                            } else if (elemName.equals("vendor")) { //NOI18N
                                String vendorProp = getProperty("application.vendor", "Application Vendor"); //NOI18N // property in project.properties
                                log("application.vendor = " + vendorProp, Project.MSG_VERBOSE); // NOI18N
                                if (elemText.equals(DEFAULT_APPLICATION_VENDOR)) {
                                    node.setTextContent(vendorProp);
                                }
                            } else if (elemName.equals("homepage")) { //NOI18N
                                // process attribute 'href'
                                String hrefAttr = ((Element) node).getAttribute("href"); //NOI18N
                                String hrefProp = getProperty("application.homepage", null); //NOI18N // property in project.properties
                                log("application.homepage = " + hrefProp, Project.MSG_VERBOSE); // NOI18N
                                if (hrefAttr.equals(DEFAULT_APPLICATION_HOMEPAGE)) {
                                    if (hrefProp != null) {
                                        ((Element) node).setAttribute("href", hrefProp); //NOI18N
                                    } else {
                                        ((Element) node).setAttribute("href", ""); //NOI18N
                                    }
                                }
                            } else if (elemName.equals("description")) { //NOI18N
                                // title will be used as default if no desc or desc == ""
                                String titleProp = getProperty("application.title", null); //NOI18N // property in project.properties
                                // two possible texts: description and short description
                                String descProp = getProperty("application.desc", null); //NOI18N // property in project.properties
                                String descShortProp = getProperty("application.desc.short", null); //NOI18N // property in project.properties
                                String descPropVal = descProp != null && !descProp.equals("") ? descProp : titleProp; //NOI18N
                                String descShortPropVal = descShortProp != null && !descShortProp.equals("") ? descShortProp : titleProp; //NOI18N
                                if (elemText.equals(DEFAULT_APPLICATION_DESC)) {
                                    node.setTextContent(descPropVal);
                                } else if (elemText.equals(DEFAULT_APPLICATION_DESC_SHORT)) {
                                    node.setTextContent(descShortPropVal);
                                }
                            }
                            break;
                        case Node.COMMENT_NODE:
                            String nodeValue = node.getNodeValue();
                            if (nodeValue.equals(DEFAULT_JNLP_ICON)) {
                                informationElem.removeChild(node);
                                String splashProp = getProperty("application.splash", null); //NOI18N // property in project.properties
                                if (splashProp != null && fileExists(splashProp)) {
                                    copyFile(new File(splashProp), destDir);
                                    String fileName = stripFilename(splashProp);
                                    informationElem.appendChild(createIconElement(docDom, fileName, "splash")); //NOI18N
                                }
                                String iconProp = getProperty("jnlp.icon", null); //NOI18N // property in project.properties
                                if (iconProp != null && fileExists(iconProp)) {
                                    copyFile(new File(iconProp), destDir);
                                    String fileName = stripFilename(iconProp);
                                    informationElem.appendChild(createIconElement(docDom, fileName, "default")); //NOI18N
                                }
                            } else if (nodeValue.equals(DEFAULT_JNLP_OFFLINE)) {
                                informationElem.removeChild(node);
                                String offlineProp = getProperty("jnlp.offline-allowed", null); //NOI18N // property in project.properties
                                if (offlineProp.equalsIgnoreCase("true")) { //NOI18N
                                    informationElem.appendChild(docDom.createElement("offline-allowed")); //NOI18N
                                }
                            }
                            break;
                        default:
                    }
                    
                }
                
            }
        }   
    }

    private void processBackgroundElem(final Document docDom, final Node parent) {
        assert docDom != null;
        assert parent != null;
        NodeList childNodes = parent.getChildNodes();
        int len = childNodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = childNodes.item(i);
            if (node != null && node.getNodeType() == Node.COMMENT_NODE) { // node might be null (don't know why)
                if (node.getNodeValue().equals(JNLP_UPDATE)) {
                    String offlineProp = getProperty("jnlp.offline-allowed", null); //NOI18N // property in project.properties
                    final Element updateElm = docDom.createElement("update"); //NOI18N
                    final String updateVal = offlineProp.equalsIgnoreCase("true") ? //NOI18N
                        "background" :  //NOI18N
                        "always";       //NOI18N
                    updateElm.setAttribute("check", updateVal); //NOI18N
                    parent.replaceChild(updateElm, node);
                }
            }
        }
    }

    private Element createIconElement(Document doc, String href, String kind) {
        Element iconElem = doc.createElement("icon"); //NOI18N
        iconElem.setAttribute("href", href); //NOI18N
        iconElem.setAttribute("kind", kind); //NOI18N
        return iconElem;
    }
    
    private boolean fileExists(String path) {
        assert path != null;
        return new File(path).exists();
    }
    
    private String getProperty(String propName, String defaultVal) {
        String propVal = getProject().getProperty(propName);
        if (propVal == null) {
            log("Property " + propName + " is not defined, using default value: " + defaultVal, Project.MSG_VERBOSE); //NOI18N
            return defaultVal;
        }
        return propVal.trim();
    }
    
    private void copyFile(File src, File dest) {
        Copy copyTask = (Copy) getProject().createTask("copy"); //NOI18N
        copyTask.setFile(src);
        copyTask.setTodir(dest);
        copyTask.setFailOnError(false);
        copyTask.init();
        copyTask.setLocation(getLocation());
        copyTask.execute();
    }
    
    private void processSecurityElem(Document docDom, Node parent) {
        NodeList childNodes = parent.getChildNodes();
        int len = childNodes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = childNodes.item(i);
            if (node != null && node.getNodeType() == Node.COMMENT_NODE) { // node might be null (don't know why)
                if (node.getNodeValue().equals(DEFAULT_JNLP_SECURITY)) {
                    String securityProp = getProperty("jnlp.signed", null); //NOI18N // property in project.properties
                    if (securityProp != null && securityProp.equalsIgnoreCase("true")) { //NOI18N
                        parent.replaceChild(createSecurityElement(docDom), node);
                    } else {
                        parent.removeChild(node);
                    }
                }
            }
        }
    }
    
    // should be extended to support all security types
    private Element createSecurityElement(Document doc) {
        Element secElem = doc.createElement("security"); //NOI18N
        Element allPermElem = doc.createElement("all-permissions"); //NOI18N
        secElem.appendChild(allPermElem);
        return secElem;
    }
    
    private void processResourcesElem(Document docDom) {
        NodeList nodeList = docDom.getElementsByTagName("resources"); // NOI18N
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
            Node resourceElem = nodeList.item(i);
            NodeList childNodes = resourceElem.getChildNodes();
            int lenChild = childNodes.getLength();
            for (int j = 0; j < lenChild; j++) {
                Node node = childNodes.item(j);
                if (node != null && node.getNodeType() == Node.COMMENT_NODE) { // node might be null (don't know why)
                    String nodeValue = node.getNodeValue();
                    if (nodeValue.equals(DEFAULT_JNLP_RESOURCES_RUNTIME)) {
                        resourceElem.replaceChild(createJ2seElement(docDom), node);
                    } else if (nodeValue.equals(DEFAULT_JNLP_RESOURCES_MAIN_JAR)) {
                        String fileName = stripFilename(getProject().getProperty("dist.jar")); // NOI18N
                        resourceElem.replaceChild(createJarElement(docDom, fileName, true, true), node);
                    } else if (nodeValue.equals(DEFAULT_JNLP_RESOURCES_JARS)) {
                        resourceElem.removeChild(node);
                        String cpProp = getProperty("run.classpath", null); //NOI18N // property in project.properties
                        log("run.classpath = " + cpProp, Project.MSG_VERBOSE); //NOI18N
                        final List<? extends File> runCpResolved = resolveCp(getProject(), cpProp);
                        final Set<? extends File> lazyJarsSet = getLazyJarsSet(getProject(), runCpResolved, lazyJars);
                        final Set<? extends String> extJarsSet = getExternalJarsProp(getProject());
                        for (File re : runCpResolved) {
                            if(!isExternalJar(re, extJarsSet)) {
                                final String fileName = re.getName();
                                if (fileName.endsWith("jar") && !fileName.equals("javaws.jar")) { //NOI18N
                                    // TODO: lib/ should be probably taken from some properties file ?
                                    final boolean eager = !lazyJarsSet.contains(re);
                                    resourceElem.appendChild(createJarElement(docDom, "lib/" + fileName, false, eager)); //NOI18N
                                }
                            }
                        }
                    } else if (nodeValue.equals(DEFAULT_JNLP_RESOURCES_EXTENSIONS)) {
                        resourceElem.removeChild(node);
                        List<Map<String,String>> extResProps = readMultiProperties(EXT_RESOURCE_PROPNAME_PREFIX, EXT_RESOURCE_SUFFIXES);
                        extResProps.addAll(getExternalJnlpsProp(getProject()));
                        for (Map<String,String> map : extResProps) {
                            List<String> requiredKeys = Arrays.asList(EXT_RESOURCE_SUFFIXES_REQUIRED);
                            Set<String> keys = map.keySet();
                            if (keys.containsAll(requiredKeys)) {
                                resourceElem.appendChild(createPropElement(docDom, "extension", map)); //NOI18N
                            }
                        }
                    }
                }
            }
        }
    }
    
    private Element createJ2seElement(Document doc) {
        // element should be <java ...> but we want to support version JNLP 1.0+
        Element j2seElem = doc.createElement("j2se"); // NOI18N
        String javacTargetProp = getProperty("javac.target", null); //NOI18N // property in project.properties
        j2seElem.setAttribute("version", javacTargetProp + "+"); // NOI18N
        String runArgsProp = getProperty("run.jvmargs", null); //NOI18N // property in project.properties
        if (runArgsProp != null && !runArgsProp.equals("")) { //NOI18N
            j2seElem.setAttribute("java-vm-args", runArgsProp); // NOI18N
        }
        String initHeapProp = getProperty("jnlp.initial-heap-size", null); //NOI18N // property in project.properties
        if (initHeapProp != null && !initHeapProp.equals("")) { //NOI18N
            j2seElem.setAttribute("initial-heap-size", initHeapProp); // NOI18N
        }
        String maxHeapProp = getProperty("jnlp.max-heap-size", null); //NOI18N // property in project.properties
        if (maxHeapProp != null && !maxHeapProp.equals("")) { //NOI18N
            j2seElem.setAttribute("max-heap-size", maxHeapProp); // NOI18N
        }
        return j2seElem;
    }
    
    private Element createJarElement(Document doc, String href, boolean main, boolean eager) {
        assert href != null;
        Element jarElem = doc.createElement("jar"); // NOI18N
        jarElem.setAttribute("href", href); // NOI18N
        if (main) {
            jarElem.setAttribute("main", "true"); // NOI18N
        }
        if (!eager) {
            jarElem.setAttribute("download", "lazy"); // NOI18N
        }
        return jarElem;
    }
    
    private void processDescriptorElem(Document docDom) {
        
        String elemNames[] = new String[] { DESC_APPLICATION, DESC_APPLET, DESC_COMPONENT, DESC_INSTALLER };
        String descName = null;
        Element descElem = null;
        for (String elemName : elemNames) {
            Node node = docDom.getElementsByTagName(elemName).item(0);
            if (node != null) {
                descName = elemName;
                descElem = (Element) node;
                break;
            }
        }
        if (DESC_APPLICATION.equals(descName)) { // APPLICATION
            if (DEFAULT_JNLP_MAIN_CLASS.equals(descElem.getAttribute("main-class"))) { //NOI18N
                descElem.setAttribute("main-class", getProject().getProperty("main.class")); // NOI18N
            }
            // process subelements - arguments
            // only if there is ${JNLP.APPLICATION.ARGS} comment element
            NodeList childNodes = descElem.getChildNodes();
            int len = childNodes.getLength();
            for (int i = 0; i < len; i++) {
                Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getNodeType() == Node.COMMENT_NODE && 
                        childNode.getNodeValue().equals(DEFAULT_JNLP_APPLICATION_ARGS)) {
                    descElem.removeChild(childNode);
                    // create new elements
                    String appArgsProp = getProject().getProperty("application.args"); //NOI18N
                    if (appArgsProp != null) {
                        for (String arg : Commandline.translateCommandline(appArgsProp)) {
                            Element argElem = docDom.createElement("argument"); // NOI18N
                            argElem.setTextContent(arg);
                            descElem.appendChild(argElem);
                        }
                    }
                }
            }
        } else if (DESC_APPLET.equals(descName)) { // APPLET
            if (DEFAULT_JNLP_MAIN_CLASS.equals(descElem.getAttribute("main-class"))) { // NOI18N
                descElem.setAttribute("main-class", getProject().getProperty("jnlp.applet.class")); // NOI18N
            }
            if (DEFAULT_APPLICATION_TITLE.equals(descElem.getAttribute("name"))) { // NOI18N
                descElem.setAttribute("name", getProperty("application.title", "Application Title")); // NOI18N
            }
            if (DEFAULT_JNLP_APPLET_WIDTH.equals(descElem.getAttribute("width"))) { // NOI18N
                descElem.setAttribute("width", getProperty("jnlp.applet.width", "300")); // NOI18N
            }
            if (DEFAULT_JNLP_APPLET_HEIGHT.equals(descElem.getAttribute("height"))) { // NOI18N
                descElem.setAttribute("height", getProperty("jnlp.applet.height", "300")); // NOI18N
            }
            // process subelements - params
            // only if there is ${JNLP.APPLET.PARAMS} comment element
            NodeList childNodes = descElem.getChildNodes();
            int len = childNodes.getLength();
            for (int i = 0; i < len; i++) {
                Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getNodeType() == Node.COMMENT_NODE && 
                        childNode.getNodeValue().equals(DEFAULT_JNLP_APPLET_PARAMS)) {
                    descElem.removeChild(childNode);
                    // create new elements
                    List<Map<String,String>> appletParamProps = readMultiProperties(APPLET_PARAM_PROPNAME_PREFIX, APPLET_PARAM_SUFFIXES);
                    for (Map<String,String> map : appletParamProps) {
                        if (map.size() == APPLET_PARAM_SUFFIXES.length) {
                            descElem.appendChild(createPropElement(docDom, "param", map)); // NOI18N
                        }
                    }
                }
            }
        } else if (DESC_COMPONENT.equals(descName)) {
            // do nothing - there is nothing to change
        } else if (DESC_INSTALLER.equals(descName)) {
            // XXX TBD
        }
    }
    
    private Element createPropElement(Document doc, String elemName, Map<String,String> props) {
        Element propElem = doc.createElement(elemName);
        for (String propName : props.keySet()) {
            String propValue = props.get(propName);
            propElem.setAttribute(propName, propValue);
        }
        return propElem;
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * Loads properties in form of ${propPrefix}.{0..n}.${propSuffixes[i]}
     * 
     * @param propPrefix prefix of the property
     * @param propSuffixes array of all suffixes to load for each prefix
     * @return list of maps of propSuffix to value of the property
     */
    private List<Map<String,String>> readMultiProperties(String propPrefix, String[] propSuffixes) {
        
        ArrayList<Map<String,String>> listToReturn = new ArrayList<Map<String,String>>();
        int index = 0;
        while (true) {
            HashMap<String,String> map = new HashMap<String,String>();
            int numProps = 0;
            for (String propSuffix : propSuffixes) {
                String propValue = getProject().getProperty(propPrefix + index + "." + propSuffix); //NOI18N
                if (propValue != null) {
                    map.put(propSuffix, propValue);
                    numProps++;
                }
            }
            if (numProps == 0) {
                break;
            }
            listToReturn.add(map);
            index++;
        }
        return listToReturn;
        
    }

    private String stripFilename(String path) {
        int sepIndex = path.lastIndexOf('/') == -1 ? path.lastIndexOf('\\') : path.lastIndexOf('/'); //NOI18N
        return  path.substring(sepIndex + 1);
    }

    private static Set<? extends File> getLazyJarsSet(final Project prj, final List<? extends File> runCp, final Path value) {
        final Set<File> result = new HashSet<File>();
        if (value != null) {
            for (String pathElement : value.list()) {
                result.add(prj.resolveFile(pathElement));
            }
        }
        for (File re : runCp) {
            if (Project.toBoolean(prj.getProperty(String.format(JNLP_LAZY_FORMAT, re.getName())))) {
                result.add(re);
            }
        }
        return result;
    }
    
    private static List<? extends File> resolveCp(final Project prj, final String path) {
        final PathTokenizer ptok = new PathTokenizer(path);
        final List<File> result = new ArrayList<File>();
        while (ptok.hasMoreTokens()) {
            result.add(prj.resolveFile(ptok.nextToken()));
        }
        return result;
    }

    /**
     * SignJarsTask stores a project property with list of jars that have been signed
     * by a different keystore. These must not be referenced directly in jnlp but through
     * dedicated included jnlps. This method returns the list of such jars transferred from
     * SignJarsTask
     * @param prj
     * @return set of JAR file names
     */
    private static Set<? extends String> getExternalJarsProp(final Project prj) {
        final Set<String> result = new HashSet<String>();
        final String extJarsProp = prj.getProperty(EXTERNAL_JARS_PROP);
        if(extJarsProp != null) {
            for(String extJar : extJarsProp.split(EXTERNAL_PROP_DELIMITER)) {
                if(!extJar.isEmpty()) {
                    File f = new File(extJar);
                    result.add(f.toString());
                }
            }
        }
        return result;
    }

    /**
     * SignJarsTask stores a project property with list of jnlp component files that 
     * represent JARs signed by a different keystore. These need to be referenced 
     * from the main jnlp as external components.
     * @param prj
     * @return set of maps, each containing href and possibly other attributes representing a jnlp component
     */
    private static Set<Map<String,String>> getExternalJnlpsProp(final Project prj) {
        final Set<Map<String,String>> result = new HashSet<Map<String,String>>();
        final String extJnlpsProp = prj.getProperty(EXTERNAL_JNLPS_PROP);
        if(extJnlpsProp != null) {
            for(String extJnlp : extJnlpsProp.split(EXTERNAL_PROP_DELIMITER)) {
                if(!extJnlp.isEmpty()) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put(EXT_RESOURCE_SUFFIXES[0], extJnlp);
                    result.add(m);
                }
            }
        }
        return result;
    }
    
    /** 
     * Returns true is file has the same name as one of the JARs marked for
     * indirect inclusion through separate jnlp component instead of directly.
     * @param file
     * @param extJars
     * @return true if file is not to be included in main jnlp directly
     */
    private static boolean isExternalJar(File file, Set<? extends String> extJars) {
        if(file != null && extJars != null) {
            String fileStr = file.toString();
            for(String extJar : extJars) {
                if(fileStr.contains(extJar)) {
                    return true;
                }
            }
        }
        return false;
    }
}
