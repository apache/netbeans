/**
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
package org.netbeans.modules.hibernate.refactoring;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.hibernate.cfg.HibernateCfgXmlConstants;
import org.netbeans.modules.hibernate.editor.HibernateEditorUtil;
import org.netbeans.modules.hibernate.mapping.HibernateMappingXmlConstants;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Andrei Badea, Dongmei Cao
 */
public class HibernateRefactoringUtil {

    private static final Logger LOGGER = Logger.getLogger(HibernateRefactoringUtil.class.getName());
    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N

    public static boolean isJavaFile(FileObject fo) {
        return JAVA_MIME_TYPE.equals(fo.getMIMEType());
    }

    public static RenamedClassName getRenamedClassName(final TreePathHandle oldHandle, final JavaSource javaSource, final String newName) throws IOException {
        final RenamedClassName[] result = {null};
        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                Element element = oldHandle.resolveElement(cc);
                if (element == null || element.getKind() != ElementKind.CLASS) {
                    return;
                }
                String oldBinaryName = ElementUtilities.getBinaryName((TypeElement) element);
                String oldSimpleName = element.getSimpleName().toString();
                String newBinaryName = null;
                element = element.getEnclosingElement();
                if (element.getKind() == ElementKind.CLASS) {
                    newBinaryName = ElementUtilities.getBinaryName((TypeElement) element) + '$' + newName;
                } else if (element.getKind() == ElementKind.PACKAGE) {
                    String packageName = ((PackageElement) element).getQualifiedName().toString();
                    newBinaryName = createQualifiedName(packageName, newName);
                } else {
                    LOGGER.log(Level.WARNING, "Enclosing element of {0} was neither class nor package", oldHandle);
                }
                result[0] = new RenamedClassName(oldSimpleName, oldBinaryName, newBinaryName);
            }
        }, true);
        return result[0];
    }

    public static List<String> getTopLevelClassNames(FileObject fo) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource == null) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<String>(1);
        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                for (TypeElement typeElement : cc.getTopLevelElements()) {
                    result.add(ElementUtilities.getBinaryName(typeElement));
                }
            }
        }, true);
        return result;
    }

    public static String getPackageName(FileObject folder) {
        ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(folder, '.', false);
        }
        return null;
    }

    public static String getRenamedPackageName(FileObject folder, String newName) {
        FileObject parent = folder.getParent();
        if (parent == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(parent, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        String parentName = cp.getResourceName(parent, '.', false);
        if (parentName == null) {
            return null;
        }
        if (parentName.length() > 0) {
            return parentName + '.' + newName;
        } else {
            return newName;
        }
    }

    public static String getPackageName(URL url) {
        File f = null;
        try {
            String path = URLDecoder.decode(url.getPath(), "UTF-8"); // NOI18N
            f = FileUtil.normalizeFile(new File(path));
        } catch (UnsupportedEncodingException u) {
            throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
        }
        String suffix = "";
        do {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                if ("".equals(suffix)) {
                    return getPackageName(fo);
                }
                String prefix = getPackageName(fo);
                return prefix + ("".equals(prefix) ? "" : ".") + suffix; // NOI18N
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix; // NOI18N
            }
            try {
                suffix = URLDecoder.decode(f.getPath().substring(f.getPath().lastIndexOf(File.separatorChar) + 1), "UTF-8") + suffix; // NOI18N
            } catch (UnsupportedEncodingException u) {
                throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
            }
            f = f.getParentFile();
        } while (f != null);
        throw new IllegalArgumentException("Cannot create package name for URL " + url); // NOI18N
    }

    public static String getSimpleElementName(String elementName) {
        for (;;) {
            if (elementName.length() == 0) {
                return elementName;
            }
            int lastDot = elementName.lastIndexOf('.');
            if (lastDot == -1) {
                return elementName;
            }
            if (lastDot == elementName.length() - 1) {
                elementName = elementName.substring(0, lastDot);
                continue;
            }
            return elementName.substring(lastDot + 1);
        }
    }

    public static String createQualifiedName(String packageName, String simpleName) {
        if (packageName.length() == 0) {
            return simpleName;
        } else {
            if (simpleName.length() == 0) {
                return packageName;
            } else {
                return packageName + '.' + simpleName;
            }
        }
    }

    public static final class RenamedClassName {

        private final String oldSimpleName;
        private final String oldBinaryName;
        private final String newBinaryName;

        public RenamedClassName(String oldSimpleName, String oldBinaryName, String newBinaryName) {
            this.oldSimpleName = oldSimpleName;
            this.oldBinaryName = oldBinaryName;
            this.newBinaryName = newBinaryName;
        }

        public String getOldSimpleName() {
            return oldSimpleName;
        }

        public String getOldBinaryName() {
            return oldBinaryName;
        }

        public String getNewBinaryName() {
            return newBinaryName;
        }
    }

    public static Map<FileObject, List<OccurrenceItem>> getJavaClassOccurrences(List<FileObject> allMappingFiles, String origBinaryName) {
        Map<FileObject, List<OccurrenceItem>> occurrences = new HashMap<FileObject, List<OccurrenceItem>>();
        for (FileObject mFileObj : allMappingFiles) {
            occurrences.put(mFileObj, getOccurPlaces(mFileObj, origBinaryName, false));
        }
        return occurrences;
    }

    public static Map<FileObject, List<OccurrenceItem>> getJavaPackageOccurrences(List<FileObject> allMappingFiles, String origPkgName) {
        Map<FileObject, List<OccurrenceItem>> occurrences = new HashMap<FileObject, List<OccurrenceItem>>();
        for (FileObject mFileObj : allMappingFiles) {
            occurrences.put(mFileObj, getOccurPlaces(mFileObj, origPkgName, true));
        }

        return occurrences;
    }

    public static Map<FileObject, List<OccurrenceItem>> getJavaFieldOccurrences(List<FileObject> allMappingFiles, String className, String fieldName) {
        Map<FileObject, List<OccurrenceItem>> occurrences = new HashMap<FileObject, List<OccurrenceItem>>();
        for (FileObject mFileObj : allMappingFiles) {
            occurrences.put(mFileObj, getJavaFieldOccurPlaces(mFileObj, className, fieldName));
        }
        return occurrences;
    }

    private static List<OccurrenceItem> getOccurPlaces(FileObject mappingFile, String searchingForName, boolean searchingPackageName) {
        List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
        try {
            // Get the document for this file
            DataObject dataObject = DataObject.find(mappingFile);
            EditorCookie result = dataObject.getCookie(EditorCookie.class);
            if (result == null) {
                throw new IllegalStateException("File " + mappingFile + " does not have an EditorCookie.");
            }

            CloneableEditorSupport editor = (CloneableEditorSupport) result;
            Document document = editor.openDocument();
            XMLSyntaxSupport syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(document);
            if (syntaxSupport == null) {
                return foundPlaces;
            }
            return syntaxSupport.runWithSequence(0, 
                    (TokenSequence ts) -> {
                        int start = document.getStartPosition().getOffset();
                        return getOccurPlacesLocked(syntaxSupport, start,
                                editor, ts, searchingForName, searchingPackageName);
            });
        } catch (BadLocationException | IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return foundPlaces;
    }
    
    private static List<OccurrenceItem> getOccurPlacesLocked(
            XMLSyntaxSupport sup,
            int start,
            CloneableEditorSupport editor,
            TokenSequence seq, 
            String searchingForName, boolean searchingPackageName) throws BadLocationException {
        List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
        String mappingPackage = null;
        seq.move(start);
        while (seq.moveNext()) {
            Token<XMLTokenId> item = seq.token();
            XMLTokenId tokenId = item.id();

            if (tokenId == XMLTokenId.TAG) {

                SyntaxElement element = sup.getElementChain(seq.offset() + 1);
                String[] attributeValues = null; // Multiple attributes can have class name as values
                boolean pkgValue = false; // To indicate the attributeValues are Java package, not full class name
                if (sup.isStartTag(element) || sup.isEmptyTag(element)) {

                    Node theNode = (Node) element;
                    String nodeName = theNode.getNodeName();
                    String itemImage = item.text().toString();
                    int itemOffset = seq.offset();
                    
                    if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MAPPING_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.MAPPING_TAG)) {
                        if(searchingPackageName) {
                            // <class> element
                            attributeValues = new String[1];
                            attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.PACKAGE_ATTRIB);
                            pkgValue = true;
                        } else {
                            mappingPackage = getAttributeValue(theNode, HibernateMappingXmlConstants.PACKAGE_ATTRIB);
                        }
                    } // Search the element/attrubutes that take class names
                    else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.CLASS_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.CLASS_TAG)) {
                        // <class> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ONE_TO_MANY_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.ONE_TO_MANY_TAG)) {
                        // <one-to-many> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.COMPOSITE_ID_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.COMPOSITE_ID_TAG)) {
                        // <composite-id> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG)) {
                        // <key-many-to-one> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MANY_TO_ONE_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.MANY_TO_ONE_TAG)) {
                        // <many-to-one> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ONE_TO_ONE_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.ONE_TO_ONE_TAG)) {
                        // <one-to-one> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.COMPONENT_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.COMPONENT_TAG)) {
                        // <component> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.SUBCLASS_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.SUBCLASS_TAG)) {
                        // <subclass> element
                        attributeValues = new String[2];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB);
                        attributeValues[1] = getAttributeValue(theNode, HibernateMappingXmlConstants.EXTENDS_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG)) {
                        // <joined-subclass> element
                        attributeValues = new String[3];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB);
                        attributeValues[1] = getAttributeValue(theNode, HibernateMappingXmlConstants.EXTENDS_ATTRIB);
                        attributeValues[2] = getAttributeValue(theNode, HibernateMappingXmlConstants.PERSISTER_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG)) {
                        // <union-subclass> element
                        attributeValues = new String[3];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB);
                        attributeValues[1] = getAttributeValue(theNode, HibernateMappingXmlConstants.EXTENDS_ATTRIB);
                        attributeValues[2] = getAttributeValue(theNode, HibernateMappingXmlConstants.PERSISTER_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.IMPORT_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.IMPORT_TAG)) {
                        // <import> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                    } else if (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MANY_TO_MANY_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.MANY_TO_MANY_TAG)) {
                        // <many-to-many> element
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.CLASS_ATTRIB);
                    } else if(nodeName.equalsIgnoreCase("property") &&//NOI18N
                            itemImage.contains("property")) {//NOI18N
                        attributeValues = new String[1];
                        attributeValues[0] = getAttributeValue(theNode, HibernateMappingXmlConstants.TYPE_ATTRIB);
                    }
                    if (attributeValues != null) {
                        for (int i = 0; i < attributeValues.length; i++) {

                            String text = sup.getDocument().getText(itemOffset, element.getElementLength());

                            String value = attributeValues[i];
                            if (searchingPackageName && !pkgValue) {
                                value = getPackageName(value);
                            }

                            if (value != null && (value.equals(searchingForName) || (mappingPackage!=null && mappingPackage.length()>0 && value.indexOf('.')==-1 && (mappingPackage + "." + value).equals(searchingForName)))) {

                                // TODO: can not just do indexof. It does not work correctly if there are multiple
                                // attributes have the same class searchingForName. Though, it does not make sense to have such case.

                                if (text.indexOf(value) != -1) {
                                    int startOffset = itemOffset + text.indexOf(value);
                                    int endOffset = startOffset + value.length();

                                    PositionBounds loc = new PositionBounds(editor.createPositionRef(startOffset, Bias.Forward),
                                            editor.createPositionRef(endOffset, Bias.Forward));

                                    foundPlaces.add(new OccurrenceItem(loc, text, value));
                                }
                            }
                        }
                    }
                }
            }
        }
        return foundPlaces;
    }

    private static List<OccurrenceItem> getJavaFieldOccurPlaces(FileObject mappingFile, String className, String fieldName) {
        List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
        try {
            // Get the document for this file
            DataObject dataObject = DataObject.find(mappingFile);
            EditorCookie result = dataObject.getCookie(EditorCookie.class);
            if (result == null) {
                throw new IllegalStateException("File " + mappingFile + " does not have an EditorCookie.");
            }

            CloneableEditorSupport editor = (CloneableEditorSupport) result;
            Document document = editor.openDocument();
            XMLSyntaxSupport syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(document);
            if (syntaxSupport == null) {
                return foundPlaces;
            }
            foundPlaces = syntaxSupport.runWithSequence(0, 
                    (TokenSequence ts) -> {
                int start = document.getStartPosition().getOffset();
                return getJavaFieldOccurPlacesLocked(syntaxSupport, ts, start, editor, className, fieldName);
            });
        } catch (IOException  | BadLocationException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return foundPlaces;
    }


    private static List<OccurrenceItem> getJavaFieldOccurPlacesLocked(
            XMLSyntaxSupport sup, TokenSequence seq, int start, 
            CloneableEditorSupport editor, String className, String fieldName) throws BadLocationException {
        List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
        seq.move(start);
        String text = null;
        while (seq.moveNext()) {
            Token<XMLTokenId> item = seq.token();
            XMLTokenId tokenId = item.id();

            if (tokenId == XMLTokenId.TAG) {
                // Did we find the <class> element

                SyntaxElement element = sup.getElementChain(seq.offset() + 1);
                String nameAttribValue = null;
                if (sup.isStartTag(element) || sup.isEmptyTag(element)) {

                    Node theNode = (Node) element;
                    String nodeName = theNode.getNodeName();
                    String itemImage = item.text().toString();

                    if ((nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.PROPERTY_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.PROPERTY_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ID_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.ID_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.SET_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.SET_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.COMPOSITE_ID_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.COMPOSITE_ID_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.KEY_PROPERTY_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.KEY_PROPERTY_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.VERSION_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.VERSION_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.TIMESTAMP_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.TIMESTAMP_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MANY_TO_ONE_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.MANY_TO_ONE_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ONE_TO_ONE_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.ONE_TO_ONE_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.COMPONENT_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.COMPONENT_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.ANY_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.ANY_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.MAP_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.MAP_TAG)) ||
                            (nodeName.equalsIgnoreCase(HibernateMappingXmlConstants.LIST_TAG) &&
                            itemImage.contains(HibernateMappingXmlConstants.LIST_TAG))) {

                        nameAttribValue = getAttributeValue(theNode, HibernateMappingXmlConstants.NAME_ATTRIB);

                        if (nameAttribValue != null && nameAttribValue.equals(fieldName)) {

                            // Check class name
                            if (HibernateEditorUtil.getClassName(theNode).equals(className)) {
                                text = sup.getDocument().getText(seq.offset(), element.getElementLength());

                                // find the offset for the field name
                                int index = text.indexOf(fieldName);
                                int startOffset = seq.offset() + index;
                                int endOffset = startOffset + fieldName.length();
                                PositionBounds loc = new PositionBounds(
                                        editor.createPositionRef(startOffset, Bias.Forward),
                                        editor.createPositionRef(endOffset, Bias.Forward));

                                foundPlaces.add(new OccurrenceItem(loc, text, fieldName));
                            }
                        }
                    }
                }
            }
        }
        return foundPlaces;
    }
    
    public static Map<FileObject, List<OccurrenceItem>> getMappingResourceOccurrences(List<FileObject> configFiles, String origName, boolean searchingPathOnly) {
        Map<FileObject, List<OccurrenceItem>> occurrences = new HashMap<FileObject, List<OccurrenceItem>>();
        for (FileObject file : configFiles) {
            occurrences.put(file, getMappingResourceOccurPlaces(file, origName, searchingPathOnly));
        }
        return occurrences;
    }
    
    private static List<OccurrenceItem> getMappingResourceOccurPlaces(FileObject configFile, String resourceName, boolean searchingPathOnly) {
        List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
        try {
            // Get the document for this file
            DataObject dataObject = DataObject.find(configFile);
            EditorCookie result = dataObject.getCookie(EditorCookie.class);
            if (result == null) {
                throw new IllegalStateException("File " + configFile + " does not have an EditorCookie.");
            }

            CloneableEditorSupport editor = (CloneableEditorSupport) result;
            BaseDocument document = (BaseDocument) editor.openDocument();
            XMLSyntaxSupport syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(document);
            if (syntaxSupport == null) {
                return foundPlaces;
            }
            return syntaxSupport.runWithSequence(0, 
                (TokenSequence ts) -> {
                    int start = document.getStartPosition().getOffset();
                    return getMappingResourceOccurPlacesLocked(syntaxSupport, ts, start, editor, resourceName, searchingPathOnly);
            });
        } catch (IOException | BadLocationException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return foundPlaces;
    }

    private static List<OccurrenceItem> getMappingResourceOccurPlacesLocked(
            XMLSyntaxSupport sup, TokenSequence seq, int start, CloneableEditorSupport editor, 
            String resourceName, boolean searchingPathOnly) throws BadLocationException {
        List<OccurrenceItem> foundPlaces = new ArrayList<OccurrenceItem>();
        seq.move(start);
        String text = null;
        while (seq.moveNext()) {
            Token<XMLTokenId> item = seq.token();
            XMLTokenId tokenId = item.id();

            if (tokenId == XMLTokenId.TAG) {
                // Did we find the <class> element

                SyntaxElement element = sup.getElementChain(seq.offset() + 1);
                String mappingResourceAttribValue = null;
                if (sup.isStartTag(element) || sup.isEmptyTag(element)) {

                    Node theNode = (Node) element;
                    String nodeName = theNode.getNodeName();
                    String itemImage = item.text().toString();

                    if(nodeName.equalsIgnoreCase(HibernateCfgXmlConstants.MAPPING_TAG) && 
                            itemImage.contains(HibernateCfgXmlConstants.MAPPING_TAG)){ 

                        mappingResourceAttribValue = getAttributeValue(theNode, HibernateCfgXmlConstants.RESOURCE_ATTRIB);
                        if(mappingResourceAttribValue != null) {
                            if(searchingPathOnly) {
                                int lastIndex = mappingResourceAttribValue.lastIndexOf('/');
                                if (lastIndex > -1) {
                                    mappingResourceAttribValue = mappingResourceAttribValue.substring(0, lastIndex);
                                } else {
                                    mappingResourceAttribValue = "";
                                }
                            }
                            if (mappingResourceAttribValue.equals(resourceName)) {
                                text = sup.getDocument().getText(seq.offset(), element.getElementLength());

                                // find the offset for the field name
                                int index = text.indexOf(resourceName);
                                int startOffset = seq.offset() + index;
                                int endOffset = startOffset + resourceName.length();
                                PositionBounds loc = new PositionBounds(editor.createPositionRef(startOffset, Bias.Forward),
                                        editor.createPositionRef(endOffset, Bias.Forward));
                                foundPlaces.add(new OccurrenceItem(loc, text, resourceName));
                            }
                        }
                    }
                }
            }
        }
        return foundPlaces;
    }

    public static final class OccurrenceItem {

        private String text;
        private String matching;
        private PositionBounds location;

        public OccurrenceItem(PositionBounds location, String text, String matching) {
            this.location = location;
            this.text = text;
            this.matching = matching;
        }

        public String getText() {
            return this.text;
        }
        
        public String getMatching(){
            return matching;
        }

        public PositionBounds getLocation() {
            return this.location;
        }
    }

    public static boolean anyHibernateMappingFiles(FileObject fo) {
        Project proj = org.netbeans.api.project.FileOwnerQuery.getOwner(fo);
        HibernateEnvironment env = proj.getLookup().lookup(HibernateEnvironment.class);
        if (env == null) {
            // The project does not support Hibernate framework
            return false;
        }
        List<FileObject> mFileObjs = env.getAllHibernateMappingFileObjects();
        if (mFileObjs == null || mFileObjs.size() == 0) {
            // OK, no mapping files at all. 
            return false;
        } else {
            return true;
        }
    }

    private static String getAttributeValue(Node node, String attributeName) {
        if (node == null) {
            return null;
        }

        NamedNodeMap attribs = node.getAttributes();
        if (attribs != null && attribs.getNamedItem(attributeName) != null) { // NOI18N
            return attribs.getNamedItem(attributeName).getNodeValue(); // NOI18N
        }

        return null;
    }

    public static String getPackageName(String binaryClassName) {
        if (binaryClassName == null) {
            return null;
        }

        int lastDot = binaryClassName.lastIndexOf(".");
        if(lastDot == -1) {
            return null;
        } else {
            return binaryClassName.substring(0, lastDot);
        }
    }
    
    public static final Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(result);
            return problem;
        } else {
            Problem p = result;
            while (p.getNext() != null)
                p = p.getNext();
            p.setNext(problem);
            return result;
        }
    }
    
    public static final boolean isValidMappingFileName(String name) {
        if (name == null) {
            return false;
        }

        if (name.equals("")) {
            return false;
        }

        if (!name.endsWith(".hbm")) { // NOI18N
            return false;
        }

        return true;
    }
    
    public static boolean nameNotUnique(String name, Project project) {
        HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
        if (hibernateEnv == null) {
            // The project does not support Hibernate framework
            return false;
        }
        List<String> mappingFiles = hibernateEnv.getAllHibernateMappings();
        if(mappingFiles.contains(name)){
            return true;
        } else {
            return false;
        }
    }
    
   static class ChangeTracker implements PropertyChangeListener{
        private boolean changed;
        public boolean isChanged (){
            return changed;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object nV = evt.getNewValue();
            Object oV = evt.getOldValue();
            changed = changed || ((nV!=null && !nV.equals(oV)) || (nV==null && nV!=oV));
        }
    }
}
