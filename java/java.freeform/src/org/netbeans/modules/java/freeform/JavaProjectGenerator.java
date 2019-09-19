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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Reads/writes project.xml.
 * Handling of /1 vs. /2 namespace: either namespace can be read;
 * when writing, attempts to keep existing namespace when possible, but
 * will always write a /2 namespace when it is necessary (for isTests or javadoc).
 * @author  Jesse Glick, David Konecny, Pavel Buzek
 */
public class JavaProjectGenerator {

    /** Keep root elements in the order specified by project's XML schema. */
    private static final String[] rootElementsOrder = new String[]{"name", "properties", "folders", "ide-actions", "export", "view", "subprojects", "project-license"}; // NOI18N
    private static final String[] viewElementsOrder = new String[]{"items", "context-menu"}; // NOI18N
    
    // this order is not required by schema, but follow it to minimize randomness a bit
    private static final String[] folderElementsOrder = new String[]{"source-folder", "build-folder", "build-file"}; // NOI18N
    private static final String[] viewItemElementsOrder = new String[]{"source-folder", "source-file"}; // NOI18N
    
    /**
     * Structure describing source folder.
     * Data in the struct are in the same format as they are stored in XML.
     * Beware that when used in <folders> you must specify label, location, and optional type;
     * in <view><items>, you must specify label, location, and style. So if you are switching
     * from the latter to the former you must add style; if vice-versa, you may need to add type.
     */
    public static final class SourceFolder {
        public SourceFolder() {}
        public String label;
        public String type;
        public String location;
        public String style;
        public String includes;
        public String excludes;
        public String encoding;
        public String toString() {
            return "FPG.SF[label=" + label + ",type=" + type + ",location=" + location + ",style=" + style + ",includes=" + includes + ",excludes=" + excludes + ",encoding=" + encoding + "]"; // NOI18N
        }
    }

    /**
     * Read source folders from the project.
     * @param helper AntProjectHelper instance
     * @param type type of source folders to be read. Can be null in which case
     *    all types will be read. Useful for reading one type of source folders.
     *    Source folders without type are read only when type == null.
     * @return list of SourceFolder instances; style value will be always null
     */
    public static List<SourceFolder> getSourceFolders(AntProjectHelper helper, String type) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<SourceFolder> list = new ArrayList<SourceFolder>();
        Element data = Util.getPrimaryConfigurationData(helper);
        Element foldersEl = XMLUtil.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            return list;
        }
        for (Element sourceFolderEl : XMLUtil.findSubElements(foldersEl)) {
            if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            SourceFolder sf = new SourceFolder();
            Element el = XMLUtil.findElement(sourceFolderEl, "label", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.label = XMLUtil.findText(el);
            }
            el = XMLUtil.findElement(sourceFolderEl, "type", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.type = XMLUtil.findText(el);
            }
            el = XMLUtil.findElement(sourceFolderEl, "location", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.location = XMLUtil.findText(el);
            }
            el = XMLUtil.findElement(sourceFolderEl, "includes", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.includes = XMLUtil.findText(el);
            }
            el = XMLUtil.findElement(sourceFolderEl, "excludes", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.excludes = XMLUtil.findText(el);
            }
            el = XMLUtil.findElement(sourceFolderEl, "encoding", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.encoding = XMLUtil.findText(el);
            }
            if (type == null || type.equals(sf.type)) {
                if (sf.label == null || sf.label.length() == 0) {
                    throw new IllegalArgumentException("label element is empty or not specified. "+helper.getProjectDirectory()); // NOI18N
                }
                if (sf.location == null || sf.location.length() == 0) {
                    throw new IllegalArgumentException("location element is empty or not specified. "+helper.getProjectDirectory()); // NOI18N
                }
                list.add(sf);
            }
        }
        return list;
    }

    /**
     * Update source folders of the project. Project is left modified and you 
     * must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param sources list of SourceFolder instances
     * @param type type of source folders to update. 
     *    Can be null in which case all types will be overriden.
     *    Useful for overriding just one type of source folders. Source folders
     *    without type are overriden only when type == null.
     */
    public static void putSourceFolders(AntProjectHelper helper, List<SourceFolder> sources, String type) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element foldersEl = XMLUtil.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            foldersEl = doc.createElementNS(Util.NAMESPACE, "folders"); // NOI18N
            XMLUtil.appendChildElement(data, foldersEl, rootElementsOrder);
        } else {
            for (Element sourceFolderEl : XMLUtil.findSubElements(foldersEl)) {
                if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                    continue;
                }
                if (type == null) {
                    foldersEl.removeChild(sourceFolderEl);
                } else {
                    Element typeEl = XMLUtil.findElement(sourceFolderEl, "type", Util.NAMESPACE); // NOI18N
                    if (typeEl != null) {
                        String typeElValue = XMLUtil.findText(typeEl);
                        if (type.equals(typeElValue)) {
                            foldersEl.removeChild(sourceFolderEl);
                        }
                    }
                }
            }
        }
        for (SourceFolder sf : sources) {
            Element sourceFolderEl = doc.createElementNS(Util.NAMESPACE, "source-folder"); // NOI18N
            Element el;
            if (sf.label != null && sf.label.length() > 0) {
                el = doc.createElementNS(Util.NAMESPACE, "label"); // NOI18N
                el.appendChild(doc.createTextNode(sf.label)); // NOI18N
                sourceFolderEl.appendChild(el);
            } else {
                throw new IllegalArgumentException("label cannot be empty. "+helper.getProjectDirectory()); // NOI18N
            }
            if (sf.type != null) {
                el = doc.createElementNS(Util.NAMESPACE, "type"); // NOI18N
                el.appendChild(doc.createTextNode(sf.type)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.location != null && sf.location.length() > 0) {
                el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
                el.appendChild(doc.createTextNode(sf.location)); // NOI18N
                sourceFolderEl.appendChild(el);
            } else {
                throw new IllegalArgumentException("location cannot be empty. "+helper.getProjectDirectory()); // NOI18N
            }
            if (sf.includes != null) {
                el = doc.createElementNS(Util.NAMESPACE, "includes"); // NOI18N
                el.appendChild(doc.createTextNode(sf.includes)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.excludes != null) {
                el = doc.createElementNS(Util.NAMESPACE, "excludes"); // NOI18N
                el.appendChild(doc.createTextNode(sf.excludes)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.encoding != null) {
                el = doc.createElementNS(Util.NAMESPACE, "encoding"); // NOI18N
                el.appendChild(doc.createTextNode(sf.encoding)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            XMLUtil.appendChildElement(foldersEl, sourceFolderEl, folderElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    /**
     * Read source views from the project. At the moment only source-folder
     * elements are read and source-file ones are ignored.
     * @param helper AntProjectHelper instance
     * @param style style of source folders to be read. Can be null in which case
     *    all styles will be read. Useful for reading one style of source folders.
     * @return list of SourceFolder instances; type value will be always null
     */
    public static List getSourceViews(AntProjectHelper helper, String style) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<SourceFolder> list = new ArrayList<SourceFolder>();
        Element data = Util.getPrimaryConfigurationData(helper);
        Element viewEl = XMLUtil.findElement(data, "view", Util.NAMESPACE); // NOI18N
        if (viewEl == null) {
            return list;
        }
        Element itemsEl = XMLUtil.findElement(viewEl, "items", Util.NAMESPACE); // NOI18N
        if (itemsEl == null) {
            return list;
        }
        for (Element sourceFolderEl : XMLUtil.findSubElements(itemsEl)) {
            if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            SourceFolder sf = new SourceFolder();
            sf.style = sourceFolderEl.getAttribute("style"); // NOI18N
            assert sf.style != null && sf.style.length() > 0 : "Bad style attr on <source-folder> in " + helper; // NOI18N
            Element el = XMLUtil.findElement(sourceFolderEl, "label", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.label = XMLUtil.findText(el);
            }
            el = XMLUtil.findElement(sourceFolderEl, "location", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.location = XMLUtil.findText(el);
            }
            el = XMLUtil.findElement(sourceFolderEl, "includes", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.includes = XMLUtil.findText(el);
            }
            el = XMLUtil.findElement(sourceFolderEl, "excludes", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.excludes = XMLUtil.findText(el);
            }
            if (style == null || style.equals(sf.style)) {
                list.add(sf);
            }
        }
        return list;
    }
    
    /**
     * Update source views of the project. 
     * This method should be called always after the putSourceFolders method
     * to keep views and folders in sync.
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param sources list of SourceFolder instances
     * @param style style of source views to update. 
     *    Can be null in which case all styles will be overriden.
     *    Useful for overriding just one style of source view.
     */
    public static void putSourceViews(AntProjectHelper helper, List<SourceFolder> sources, String style) {
        //assert ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element viewEl = XMLUtil.findElement(data, "view", Util.NAMESPACE); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(Util.NAMESPACE, "view"); // NOI18N
            XMLUtil.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element itemsEl = XMLUtil.findElement(viewEl, "items", Util.NAMESPACE); // NOI18N
        if (itemsEl == null) {
            itemsEl = doc.createElementNS(Util.NAMESPACE, "items"); // NOI18N
            XMLUtil.appendChildElement(viewEl, itemsEl, viewElementsOrder);
        }
        List<Element> sourceViews = XMLUtil.findSubElements(itemsEl);
        for (Element sourceViewEl : sourceViews) {
            if (!sourceViewEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            String sourceStyle = sourceViewEl.getAttribute("style"); // NOI18N
            if (style == null || style.equals(sourceStyle)) {
                itemsEl.removeChild(sourceViewEl);
            }
        }
        
        for (SourceFolder sf : sources) {
            if (sf.style == null || sf.style.length() == 0) {
                // perhaps this is principal source folder?
                continue;
            }
            Element sourceFolderEl = doc.createElementNS(Util.NAMESPACE, "source-folder"); // NOI18N
            sourceFolderEl.setAttribute("style", sf.style); // NOI18N
            Element el;
            if (sf.label != null) {
                el = doc.createElementNS(Util.NAMESPACE, "label"); // NOI18N
                el.appendChild(doc.createTextNode(sf.label)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.location != null) {
                el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
                el.appendChild(doc.createTextNode(sf.location)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.includes != null) {
                el = doc.createElementNS(Util.NAMESPACE, "includes"); // NOI18N
                el.appendChild(doc.createTextNode(sf.includes)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.excludes != null) {
                el = doc.createElementNS(Util.NAMESPACE, "excludes"); // NOI18N
                el.appendChild(doc.createTextNode(sf.excludes)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            XMLUtil.appendChildElement(itemsEl, sourceFolderEl, viewItemElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }

    /**
     * Returns {@link Element} for {@link JavaCompilationUnit}.
     * @param aux AuxiliaryConfiguration instance
     * @return {@link Element} representing JavaCompilationUnit instances or null
     */
    public static Element getJavaCompilationUnits (final AuxiliaryConfiguration aux) {
        for (String ns : JavaProjectNature.JAVA_NAMESPACES) {
            Element data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, ns, true);
            if (data != null) return data;
        }
        return null;
    }

    /**
     * Read Java compilation units from the project.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @return list of JavaCompilationUnit instances; never null;
     */
    public static List<JavaCompilationUnit> getJavaCompilationUnits(
            AntProjectHelper helper, AuxiliaryConfiguration aux) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<JavaCompilationUnit> list = new ArrayList<JavaCompilationUnit>();
        final Element data = getJavaCompilationUnits(aux);
        if (data == null) {
            return list;
        }
        for (Element cuEl : XMLUtil.findSubElements(data)) {
            JavaCompilationUnit cu = new JavaCompilationUnit();
            List<String> outputs = new ArrayList<String>();
            List<String> javadoc = new ArrayList<String>();
            List<JavaCompilationUnit.CP> cps = new ArrayList<JavaCompilationUnit.CP>();
            List<String> packageRoots = new ArrayList<String>();
            for (Element el : XMLUtil.findSubElements(cuEl)) {
                if (el.getLocalName().equals("package-root")) { // NOI18N
                    packageRoots.add(XMLUtil.findText(el));
                    continue;
                }
                if (el.getLocalName().equals("classpath")) { // NOI18N
                    JavaCompilationUnit.CP cp = new JavaCompilationUnit.CP();
                    cp.classpath = XMLUtil.findText(el);
                    cp.mode = el.getAttribute("mode"); // NOI18N
                    if (cp.mode != null && cp.classpath != null) {
                        cps.add(cp);
                    }
                    continue;
                }
                if (el.getLocalName().equals("built-to")) { // NOI18N
                    outputs.add(XMLUtil.findText(el));
                    continue;
                }
                if (el.getLocalName().equals("javadoc-built-to")) { // NOI18N
                    javadoc.add(XMLUtil.findText(el));
                    continue;
                }
                if (el.getLocalName().equals("source-level")) { // NOI18N
                    cu.sourceLevel = XMLUtil.findText(el);
                    continue;
                }
                if (el.getLocalName().equals("unit-tests")) { // NOI18N
                    cu.isTests = true;
                    continue;
                }
                if ("annotation-processing".equals(el.getLocalName())&&         //NOI18N
                    JavaProjectNature.namespaceAtLeast(el.getNamespaceURI(), JavaProjectNature.NS_JAVA_3)) {
                    cu.annotationPorocessing = new JavaCompilationUnit.AnnotationProcessing();
                    cu.annotationPorocessing.trigger = EnumSet.<AnnotationProcessingQuery.Trigger>noneOf(AnnotationProcessingQuery.Trigger.class);
                    cu.annotationPorocessing.processors = new ArrayList<String>();
                    cu.annotationPorocessing.processorParams = new LinkedHashMap<String, String>();
                    for (Element apEl : XMLUtil.findSubElements(el)) {
                        final String localName = apEl.getLocalName();
                        if ("scan-trigger".equals(localName)) { //NOI18N
                            cu.annotationPorocessing.trigger.add(AnnotationProcessingQuery.Trigger.ON_SCAN);
                        } else if ("editor-trigger".equals(localName)) {   //NOI18N
                            cu.annotationPorocessing.trigger.add(AnnotationProcessingQuery.Trigger.IN_EDITOR);
                        } else if ("source-output".equals(localName)) {
                            cu.annotationPorocessing.sourceOutput = XMLUtil.findText(apEl);
                        } else if ("processor-path".equals(localName)) {    //NOI18N
                            cu.annotationPorocessing.processorPath = XMLUtil.findText(apEl);
                        } else if ("processor".equals(localName)) { //NOI18N
                            cu.annotationPorocessing.processors.add(XMLUtil.findText(apEl));
                        } else if ("processor-option".equals(localName)) {  //NOI18N
                            final Element keyEl = XMLUtil.findElement(apEl, "key", el.getNamespaceURI());    //NOI18N
                            final Element valueEl = XMLUtil.findElement(apEl, "value", el.getNamespaceURI());     //NOI18N
                            if (keyEl != null && valueEl != null) {
                                final String key = XMLUtil.findText(keyEl);
                                final String value = XMLUtil.findText(valueEl);
                                if (key != null) {
                                    cu.annotationPorocessing.processorParams.put(key, value);
                                }
                            }
                        }
                    }
                }
            }
            cu.output = outputs.size() > 0 ? outputs : null;
            cu.javadoc = javadoc.size() > 0 ? javadoc : null;
            cu.classpath = cps.size() > 0 ? cps: null;
            cu.packageRoots = packageRoots.size() > 0 ? packageRoots: null;
            list.add(cu);
        }
        return list;
    }

    /**
     * Update Java compilation units of the project. Project is left modified
     * and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @param compUnits list of JavaCompilationUnit instances
     */
    public static void putJavaCompilationUnits(AntProjectHelper helper, 
            AuxiliaryConfiguration aux, List<JavaCompilationUnit> compUnits) {
        //assert ProjectManager.mutex().isWriteAccess();
        int requiredVersion = 1;
        
        // detect minimal required namespace:
        for (JavaCompilationUnit unit : compUnits) {
            requiredVersion = Math.max(requiredVersion, minimalNS(unit));
        }
        
        String namespace;
        
        switch (requiredVersion) {
            case 4: namespace = JavaProjectNature.NS_JAVA_4; break;
            case 3: namespace = JavaProjectNature.NS_JAVA_3; break;
            case 2: namespace = JavaProjectNature.NS_JAVA_2; break;
            default: namespace = JavaProjectNature.NS_JAVA_1; break;
        }
        
        Element data = getJavaCompilationUnits(aux);
        
        if (data == null || !JavaProjectNature.namespaceAtLeast(data.getNamespaceURI(), namespace)) {
            if (data != null) {
                aux.removeConfigurationFragment(JavaProjectNature.EL_JAVA, data.getNamespaceURI(), true);
            }
            data = Util.getPrimaryConfigurationData(helper).getOwnerDocument().
                createElementNS(namespace, JavaProjectNature.EL_JAVA);
        }
        Document doc = data.getOwnerDocument();
        for (Element cuEl : XMLUtil.findSubElements(data)) {
            data.removeChild(cuEl);
        }
        for (JavaCompilationUnit cu : compUnits) {
            Element cuEl = doc.createElementNS(data.getNamespaceURI(), "compilation-unit"); // NOI18N
            data.appendChild(cuEl);
            Element el;
            if (cu.packageRoots != null) {
                for (String packageRoot : cu.packageRoots) {
                    el = doc.createElementNS(data.getNamespaceURI(), "package-root"); // NOI18N
                    el.appendChild(doc.createTextNode(packageRoot));
                    cuEl.appendChild(el);
                }
            }
            if (cu.isTests) {
                assert JavaProjectNature.namespaceAtLeast(namespace, JavaProjectNature.NS_JAVA_2);
                cuEl.appendChild(doc.createElementNS(data.getNamespaceURI(), "unit-tests")); // NOI18N
            }
            if (cu.classpath != null) {
                for (JavaCompilationUnit.CP cp : cu.classpath) {
                    el = doc.createElementNS(data.getNamespaceURI(), "classpath"); // NOI18N
                    el.appendChild(doc.createTextNode(cp.classpath));
                    el.setAttribute("mode", cp.mode); // NOI18N
                    cuEl.appendChild(el);
                }
            }
            if (cu.output != null) {
                for (String output : cu.output) {
                    el = doc.createElementNS(data.getNamespaceURI(), "built-to"); // NOI18N
                    el.appendChild(doc.createTextNode(output));
                    cuEl.appendChild(el);
                }
            }
            if (cu.javadoc != null) {
                for (String javadoc : cu.javadoc) {
                    assert JavaProjectNature.namespaceAtLeast(namespace, JavaProjectNature.NS_JAVA_2);
                    el = doc.createElementNS(data.getNamespaceURI(), "javadoc-built-to"); // NOI18N
                    el.appendChild(doc.createTextNode(javadoc));
                    cuEl.appendChild(el);
                }
            }
            if (cu.sourceLevel != null) {
                el = doc.createElementNS(data.getNamespaceURI(), "source-level"); // NOI18N
                el.appendChild(doc.createTextNode(cu.sourceLevel));
                cuEl.appendChild(el);
            }
            if (cu.annotationPorocessing != null) {
                el = doc.createElementNS(data.getNamespaceURI(), "annotation-processing"); // NOI18N
                if (cu.annotationPorocessing.trigger.contains(AnnotationProcessingQuery.Trigger.ON_SCAN)) {                    
                    el.appendChild(doc.createElementNS(data.getNamespaceURI(), "scan-trigger")); //NOI18N
                }
                if (cu.annotationPorocessing.trigger.contains(AnnotationProcessingQuery.Trigger.IN_EDITOR)) {
                    el.appendChild(doc.createElementNS(data.getNamespaceURI(), "editor-trigger")); //NOI18N
                }
                if (cu.annotationPorocessing.sourceOutput != null) {
                    final Element soElm = doc.createElementNS(data.getNamespaceURI(), "source-output");  //NOI18N
                    soElm.appendChild(doc.createTextNode(cu.annotationPorocessing.sourceOutput));
                    el.appendChild(soElm);
                }
                if (cu.annotationPorocessing.processorPath != null) {
                    final Element ppElm = doc.createElementNS(data.getNamespaceURI(), "processor-path"); //NOI18N
                    ppElm.appendChild(doc.createTextNode(cu.annotationPorocessing.processorPath));
                    el.appendChild(ppElm);
                }
                for (String processor : cu.annotationPorocessing.processors) {
                    final Element pElm = doc.createElementNS(data.getNamespaceURI(), "processor");   //NOI18N
                    pElm.appendChild(doc.createTextNode(processor));
                    el.appendChild(pElm);
                }
                for (Map.Entry<String,String> option : cu.annotationPorocessing.processorParams.entrySet()) {
                    final Element poElm = doc.createElementNS(data.getNamespaceURI(), "processor-option");   //NOI18N
                    final Element keyElm = doc.createElementNS(data.getNamespaceURI(),"key");  //NOI18N
                    final Element valueElm = doc.createElementNS(data.getNamespaceURI(), "value");   //NOI18N
                    keyElm.appendChild(doc.createTextNode(option.getKey()));
                    if (option.getValue() != null) {
                        valueElm.appendChild(doc.createTextNode(option.getValue()));
                    }
                    poElm.appendChild(keyElm);
                    poElm.appendChild(valueElm);
                    el.appendChild(poElm);
                }
                cuEl.appendChild(el);
            }
        }
        aux.putConfigurationFragment(data, true);
    }

    /**
     * Structure describing compilation unit.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class JavaCompilationUnit {
        public List<String> packageRoots;
        public List<CP> classpath;
        public List<String> output;
        public List<String> javadoc;
        public String sourceLevel;
        public boolean isTests;
        public AnnotationProcessing annotationPorocessing;
        
        public String toString() {
            return "FPG.JCU[packageRoots=" + packageRoots + ", classpath=" + classpath + ", output=" + output + ", javadoc=" + javadoc + ", sourceLevel=" + sourceLevel + ",isTests=" + isTests + "]"; // NOI18N
        }
        
        public static final class CP {
            public String classpath;
            public String mode;
            
            public String toString() {
                return "FPG.JCU.CP:[classpath="+classpath+", mode="+mode+", this="+super.toString()+"]"; // NOI18N
            }
            
        }
        
        //@NotThreadSafe
        public static final class AnnotationProcessing {
            public Set<AnnotationProcessingQuery.Trigger> trigger;
            public String sourceOutput;
            public String processorPath;
            public List<String> processors;
            public Map<String,String> processorParams;

            @Override
            public String toString() {
                return String.format(
                    "Processors run: %s, source output %s, processor path: %s, processors: %s, processor options: %s",  //NOI18N
                    trigger,
                    sourceOutput,
                    processorPath,
                    processors,
                    processorParams);
            }
            
            
        }
    }
    
    /**
     * Structure describing one export record.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class Export {
        public String type;
        public String location;
        public String script; // optional
        public String buildTarget;
        public String cleanTarget; // optional
    }

    /**
     * Try to guess project's exports. See issue #49221 for more details.
     */
    public static List<Export> guessExports(PropertyEvaluator evaluator, File baseFolder,
            List<TargetMapping> targetMappings, List<JavaCompilationUnit> javaCompilationUnits) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<Export> exports = new ArrayList<Export>();
        String targetName = null;
        String scriptName = null;
        for (TargetMapping tm : targetMappings) {
            if (tm.name.equals("build")) { // NOI18N
                if (tm.targets.size() == 1) {
                    targetName = tm.targets.get(0);
                    scriptName = tm.script;
                } else {
                    return new ArrayList<Export>();
                }
            }
        }
        if (targetName == null) {
            return new ArrayList<Export>();
        }
        for (JavaCompilationUnit cu : javaCompilationUnits) {
            if (cu.output != null) {
                for (String output : cu.output) {
                    String output2 = evaluator.evaluate(output);
                    if (output2.endsWith(".jar")) { // NOI18N
                        Export e = new Export();
                        e.type = JavaProjectConstants.ARTIFACT_TYPE_JAR;
                        e.location = output;
                        e.script = scriptName;
                        e.buildTarget = targetName;
                        exports.add(e);
                    }
                    else if (isFolder(evaluator, baseFolder, output2)) {
                        Export e = new Export();
                        e.type = JavaProjectConstants.ARTIFACT_TYPE_FOLDER;
                        e.location = output;
                        e.script = scriptName;
                        e.buildTarget = targetName;
                        exports.add(e);
                    }
                }
            }
        }
        return exports;
    }
    
    /**
     * Update exports of the project. 
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param exports list of Export instances
     */
    public static void putExports(AntProjectHelper helper, List<Export> exports) {
        //assert ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        
        for (Element exportEl : XMLUtil.findSubElements(data)) {
            if (!exportEl.getLocalName().equals("export")) { // NOI18N
                continue;
            }
            data.removeChild(exportEl);
        }
        
        for (Export export : exports) {
            Element exportEl = doc.createElementNS(Util.NAMESPACE, "export"); // NOI18N
            Element el;
            el = doc.createElementNS(Util.NAMESPACE, "type"); // NOI18N
            el.appendChild(doc.createTextNode(export.type)); // NOI18N
            exportEl.appendChild(el);
            el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
            el.appendChild(doc.createTextNode(export.location)); // NOI18N
            exportEl.appendChild(el);
            if (export.script != null) {
                el = doc.createElementNS(Util.NAMESPACE, "script"); // NOI18N
                el.appendChild(doc.createTextNode(export.script)); // NOI18N
                exportEl.appendChild(el);
            }
            el = doc.createElementNS(Util.NAMESPACE, "build-target"); // NOI18N
            el.appendChild(doc.createTextNode(export.buildTarget)); // NOI18N
            exportEl.appendChild(el);
            if (export.cleanTarget != null) {
                el = doc.createElementNS(Util.NAMESPACE, "clean-target"); // NOI18N
                el.appendChild(doc.createTextNode(export.cleanTarget)); // NOI18N
                exportEl.appendChild(el);
            }
            XMLUtil.appendChildElement(data, exportEl, rootElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    /**
     * Try to guess project's subprojects. See issue #49640 for more details.
     */
    public static List<String> guessSubprojects(PropertyEvaluator evaluator,
            List<JavaCompilationUnit> javaCompilationUnits, File projectBase, File freeformBase) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        Set<String> subprojs = new HashSet<String>();
        for (JavaCompilationUnit cu : javaCompilationUnits) {
            if (cu.classpath != null) {
                for (JavaCompilationUnit.CP cp : cu.classpath) {
                    if (!"compile".equals(cp.mode))  { // NOI18N
                        continue;
                    }
                    String classpath = evaluator.evaluate(cp.classpath);
                    if (classpath == null) {
                        continue;
                    }
                    for (String s : PropertyUtils.tokenizePath(classpath)) {
                        File file = FileUtil.normalizeFile(new File(s));
                        AntArtifact aa = AntArtifactQuery.findArtifactFromFile(file);
                        if (aa != null) {
                            File proj = FileUtil.toFile(aa.getProject().getProjectDirectory());
                            String p = Util.relativizeLocation(projectBase, freeformBase, proj);
                            subprojs.add(p);
                        }
                    }
                }
            }
        }
        return new ArrayList<String>(subprojs);
    }
    
    /**
     * Update subprojects of the project. 
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param subprojects list of paths to subprojects
     */
    public static void putSubprojects(AntProjectHelper helper, List<String> subprojects) {
        //assert ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element subproject = XMLUtil.findElement(data, "subprojects", Util.NAMESPACE); // NOI18N
        if (subproject != null) {
            data.removeChild(subproject);
        }
        subproject = doc.createElementNS(Util.NAMESPACE, "subprojects"); // NOI18N
        XMLUtil.appendChildElement(data, subproject, rootElementsOrder);

        for (String proj : subprojects) {
            Element projEl = doc.createElementNS(Util.NAMESPACE, "project"); // NOI18N
            projEl.appendChild(doc.createTextNode(proj));
            subproject.appendChild(projEl);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    /**
     * Try to guess project's build folders. See issue #50934 for more details.
     */
    public static List<String> guessBuildFolders(PropertyEvaluator evaluator,
            List<JavaCompilationUnit> javaCompilationUnits, File projectBase, File freeformBase) {

        List<String> buildFolders = new ArrayList<String>();
        for (JavaCompilationUnit cu : javaCompilationUnits) {
            if (cu.output != null) {
                for (String output : cu.output) {
                    File f = Util.resolveFile(evaluator, freeformBase, output);
                    // include only directories
                    if (!f.isDirectory()) {
                        continue;
                    }
                    String absOutput = f.getAbsolutePath();
                    if (!absOutput.endsWith(File.separator)) {
                        absOutput += File.separatorChar;
                    }

                    if (absOutput.startsWith(projectBase.getAbsolutePath()+File.separatorChar) ||
                        absOutput.startsWith(freeformBase.getAbsolutePath()+File.separatorChar)) {
                        // ignore output which lies below project base or freeform base
                        continue;
                    }
                    boolean add = true;
                    Iterator<String> it = buildFolders.iterator();
                    while (it.hasNext()) {
                        String path = it.next();
                        if (!path.endsWith(File.separator)) {
                            path += File.separatorChar;
                        }
                        if (path.equals(absOutput)) {
                            // such a path is already there
                            add = false;
                            break;
                        } else if (absOutput.startsWith(path)) {
                            // such a patch is already there
                            add = false;
                            break;
                        } else if (path.startsWith(absOutput)) {
                            it.remove();
                        }
                    }
                    if (add) {
                        buildFolders.add(output);
                    }
                }
            }
        }
        return buildFolders;
    }
    
    /**
     * Update build folders of the project. 
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param buildFolders list of build folder locations
     */
    public static void putBuildFolders(AntProjectHelper helper, List<String> buildFolders) {
        putBuildElement(helper, buildFolders, "build-folder");
    }
    
    private static void putBuildElement(AntProjectHelper helper, List<String> buildFolders, String elemName) {
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element foldersEl = XMLUtil.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            foldersEl = doc.createElementNS(Util.NAMESPACE, "folders"); // NOI18N
            XMLUtil.appendChildElement(data, foldersEl, rootElementsOrder);
        } else {
            List<Element> folders = XMLUtil.findSubElements(foldersEl);
            for (Element buildFolderEl  : folders) {
                if (!buildFolderEl.getLocalName().equals(elemName)) { // NOI18N
                    continue;
                }
                foldersEl.removeChild(buildFolderEl);
            }
        }

        for (String location : buildFolders) {
            Element buildFolderEl = doc.createElementNS(Util.NAMESPACE, elemName); // NOI18N
            Element locationEl = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
            locationEl.appendChild(doc.createTextNode(location));
            buildFolderEl.appendChild(locationEl);
            XMLUtil.appendChildElement(foldersEl, buildFolderEl, folderElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    public static List<String> getBuildFiles(PropertyEvaluator evaluator,
            List<JavaCompilationUnit> compUnits, File projectBase, File freeformBase) {
        
        List<String> buildFiles = new ArrayList<String>();
        for (JavaCompilationUnit cu : compUnits) {
            if (cu.output != null) {
                for (String output : cu.output) {
                    File f = Util.resolveFile(evaluator, freeformBase, output);
                    try {
                        if (f.exists() && !FileUtil.isArchiveFile(Utilities.toURI(f).toURL())) {
                            continue;
                        }
                    } catch (MalformedURLException murle) {
                        Exceptions.printStackTrace(murle);
                    }
                    String absOutput = f.getAbsolutePath();
                    if (absOutput.startsWith(projectBase.getAbsolutePath() + File.separatorChar) ||
                        absOutput.startsWith(freeformBase.getAbsolutePath() + File.separatorChar)) {
                        // ignore output which lies below project base or freeform base
                        continue;
                    }
                    boolean add = true;
                    Iterator<String> it = buildFiles.iterator();
                    while (it.hasNext()) {
                        String path = it.next();
                        if (path.equals(absOutput)) {
                            // such a path is already there
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        buildFiles.add(output);
                    }
                }
            }
        }
        return buildFiles;
    }
    
    public static void putBuildFiles(AntProjectHelper helper, List<String> buildFiles) {
        putBuildElement(helper, buildFiles, "build-file");
    }
    
    // XXX: copy&pasted from FreeformProjectGenerator
    /**
     * Read target mappings from project.
     * @param helper AntProjectHelper instance
     * @return list of TargetMapping instances
     */
    public static List<TargetMapping> getTargetMappings(AntProjectHelper helper) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<TargetMapping> list = new ArrayList<TargetMapping>();
        Element genldata = Util.getPrimaryConfigurationData(helper);
        Element actionsEl = XMLUtil.findElement(genldata, "ide-actions", Util.NAMESPACE); // NOI18N
        if (actionsEl == null) {
            return list;
        }
        for (Element actionEl : XMLUtil.findSubElements(actionsEl)) {
            TargetMapping tm = new TargetMapping();
            tm.name = actionEl.getAttribute("name"); // NOI18N
            List<String> targetNames = new ArrayList<String>();
            EditableProperties props = new EditableProperties(false);
            for (Element subEl : XMLUtil.findSubElements(actionEl)) {
                if (subEl.getLocalName().equals("target")) { // NOI18N
                    targetNames.add(XMLUtil.findText(subEl));
                    continue;
                }
                if (subEl.getLocalName().equals("script")) { // NOI18N
                    tm.script = XMLUtil.findText(subEl);
                    continue;
                }
                if (subEl.getLocalName().equals("context")) { // NOI18N
                    TargetMapping.Context ctx = new TargetMapping.Context();
                    for (Element contextSubEl : XMLUtil.findSubElements(subEl)) {
                        if (contextSubEl.getLocalName().equals("property")) { // NOI18N
                            ctx.property = XMLUtil.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("format")) { // NOI18N
                            ctx.format = XMLUtil.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("folder")) { // NOI18N
                            ctx.folder = XMLUtil.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("pattern")) { // NOI18N
                            ctx.pattern = XMLUtil.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("arity")) { // NOI18N
                            Element sepFilesEl = XMLUtil.findElement(contextSubEl, "separated-files", Util.NAMESPACE); // NOI18N
                            if (sepFilesEl != null) {
                                ctx.separator = XMLUtil.findText(sepFilesEl);
                            }
                            continue;
                        }
                    }
                    tm.context = ctx;
                }
                if (subEl.getLocalName().equals("property")) { // NOI18N
                    readProperty(subEl, props);
                    continue;
                }
            }
            tm.targets = targetNames;
            if (props.keySet().size() > 0) {
                tm.properties = props;
            }
            list.add(tm);
        }
        return list;
    }
    
    
    /*package private*/ static boolean isFolder (PropertyEvaluator eval, File baseFolder, String folder) {
        File f = Util.resolveFile(eval, baseFolder, folder);
        if (f != null && f.isDirectory()) {
            return true;
        }
        int dotIndex = folder.lastIndexOf('.');    //NOI18N
        int slashIndex = folder.lastIndexOf('/');  //NOI18N
        return dotIndex == -1 || (dotIndex < slashIndex) ;
    }
    
    /**
     * Structure describing target mapping.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class TargetMapping {
        public String script;
        public List<String> targets;
        public String name;
        public EditableProperties properties;
        public Context context; // may be null
        
        public static final class Context {
            public String property;
            public String format;
            public String folder;
            public String pattern; // may be null
            public String separator; // may be null
        }
    }
    
    private static void readProperty(Element propertyElement, EditableProperties props) {
        String key = propertyElement.getAttribute("name"); // NOI18N
        String value = XMLUtil.findText(propertyElement);
        props.setProperty(key, value);
    }

    private static int minimalNS(final JavaCompilationUnit unit) {
        int min = 1;
        if (unit.isTests || (unit.javadoc != null && !unit.javadoc.isEmpty())) {
            min = 2;
        }
        if (unit.annotationPorocessing != null) {
            min = 3;
        }
        if (unit.sourceLevel != null) {
            final SpecificationVersion JAVA_6 = new SpecificationVersion("1.6");  //NOI18N
            final SpecificationVersion JAVA_7 = new SpecificationVersion("1.7");  //NOI18N
            final SpecificationVersion JAVA_8 = new SpecificationVersion("1.8");  //NOI18N
            final SpecificationVersion current = new SpecificationVersion(unit.sourceLevel);
            if (JAVA_6.equals(current) || JAVA_7.equals(current)) {
                min = 3;
            } else if (JAVA_8.compareTo(current) <= 0) {
                min = 4;
            }
        }
        return min;
    }
}
