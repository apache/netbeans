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

package org.netbeans.modules.csl;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.zip.ZipOutputStream;
import org.netbeans.nbbuild.JarWithModuleAttributes;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Tash just like the <jarwithmoduleattributes> (used by the default
 * NetBeans build jar target), but subclassed to perform GSF
 * static registration during the build.
 *
 * @author Tor Norbye
 */
public class CslJar extends JarWithModuleAttributes {
    private static final String FILENAME = "name"; // NOI18N
    private static final String FILE = "file"; // NOI18N
    private static final String FOLDER = "folder"; // NOI18N
    private static final String ATTR = "attr"; // NOI18N
    private static final String BOOLVALUE = "boolvalue"; // NOI18N
    private static final String INTVALUE = "intvalue"; // NOI18N
    private static final String STRINGVALUE = "stringvalue"; // NOI18N
    private static final String BUNDLEVALUE = "bundlevalue"; // NOI18N
    private static final String METHODVALUE = "methodvalue"; // NOI18N
    private static final String USECUSTOMEDITORKIT = "useCustomEditorKit"; // NOI18N
    private static final String TRUE = "true"; // NOI18N
    private static final String FILESYSTEM = "filesystem"; // NOI18N

    // Keep in sync with LanguageRegistry
    private static final String STRUCTURE = "structure.instance"; // NOI18N
    private static final String LANGUAGE = "language.instance"; // NOI18N

    private Manifest mf;
    private String layer;

    public CslJar() {
    }

    @Override
    public void setManifest(File manifestFile) throws BuildException {
        super.setManifest(manifestFile);
    }

    @Override
    protected void zipFile(File file, ZipOutputStream zOut, String vPath, int mode) throws IOException {
        if (vPath.equals(layer)) {
            System.setProperty("CslJar", Boolean.TRUE.toString());
            try {
                // Create a tempfile and trick it!
                InputStream is = new FileInputStream(file);
                String modifiedLayer = getModifiedLayer(is);
                if (modifiedLayer != null) {
                    File tmpFile = File.createTempFile("csl", "tmp"); // NOI18N
                    BufferedWriter w = new BufferedWriter(new FileWriter(tmpFile));
                    w.write(modifiedLayer);
                    w.flush();
                    w.close();
                    // Note - we're passing the temp file instead of the "real" layer file
                    super.zipFile(tmpFile, zOut, vPath, mode);
                    // Remove the tmpfile
                    tmpFile.delete();
                    return;
                }
            } finally {
                System.setProperty("CslJar", Boolean.FALSE.toString());
            }
        }
        super.zipFile(file, zOut, vPath, mode);
    }

    @Override
    public void addConfiguredManifest(Manifest newManifest) throws ManifestException {
        super.addConfiguredManifest(newManifest);
        this.mf = newManifest;

        layer = mf.getMainSection().getAttributeValue("OpenIDE-Module-Layer"); // NOI18N
    }

    public String getModifiedLayer(InputStream is) throws BuildException {
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            docBuilder.setEntityResolver(new EntityResolver() {
                public org.xml.sax.InputSource resolveEntity(String pubid, String sysid) {
                    return new org.xml.sax.InputSource(new ByteArrayInputStream(new byte[0]));
                }
            });

            Document doc = docBuilder.parse(bis);

            // Process
            updateLayer(doc);

            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);

            // The above transform seems to drop the DOCTYPE etc. -- replace this part
            String emittedDom = sw.toString();
            String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><filesystem>"; // NOI18N
            if (emittedDom.startsWith(HEADER)) {
                emittedDom =
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + // NOI18N
                        "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" + // NOI18N
                        "<filesystem>" + // NOI18N
                        emittedDom.substring(HEADER.length());
            }

            return emittedDom;
        } catch (ParserConfigurationException pce) {
            throw new BuildException(pce);
        } catch (TransformerConfigurationException tce) {
            throw new BuildException(tce);
        } catch (TransformerException te) {
            throw new BuildException(te);
        } catch (SAXException se) {
            throw new BuildException("XML parsing exception - " + se.getMessage(), se);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new BuildException("IO error during CSL layer processing:" + ioe.getMessage(), ioe);
        }
    }

    private void updateLayer(Document doc) {
        // Find the CslPlugins folder
        Element cslFolder = findPath(doc, "CslPlugins"); // NOI18N
        if (cslFolder == null) {
            throw new BuildException("No CslPlugins folder in this layer");
        }

        // Challenge: I need to -instantiate- the CslLanguage class in order to
        // be able to register class services!!
        // Hmmm maybe I can limit myself to ONLY the stuff that is needed at
        // startup

        for (Element outerMime : getElementChildren(cslFolder)) {
            for (Element innerMime : getElementChildren(outerMime)) {

                // Record for infrastructure that we've processed this GsfPlugins.
                // Used by the infrastructure to warn about modules that haven't had
                // their plugin registrations pre-processed during the build using
                // this ant task.
                setFileAttribute(doc, innerMime, "genver", INTVALUE, "1");


                String mimeType = outerMime.getAttribute(FILENAME) + "/" + innerMime.getAttribute(FILENAME);
                System.out.println("   Handling " + mimeType);

                boolean customEditorKit = false;
                String cslLanguageClass = null;
                boolean hasStructureScanner = false;

                for (Element regFile : getElementChildren(innerMime)) {
                    String tag = regFile.getTagName();
                    if (ATTR.equals(tag)) { // NOI18N
                        String name = regFile.getAttribute(FILENAME);
                        if (USECUSTOMEDITORKIT.equals(name) && TRUE.equals(regFile.getAttribute(BOOLVALUE))) {
                            // It's a custom editor
                            customEditorKit = true;
                        }
                    } else {
                        if (!tag.equals(FILE)) {
                            throw new BuildException("Unexpected CslPlugin registration item: <" + tag + ">");
                        }
                    }

                    String name = regFile.getAttribute(FILENAME);
                    if (STRUCTURE.equals(name)) {
                        // This language has a navigator; insert one
                        registerStructureScanner(doc, mimeType);
                        hasStructureScanner = true;
                    } else if (LANGUAGE.equals(name)) {
                        List<Element> attributes = getAttributeElements(regFile);
                        for (Element attribute : attributes) {
                            if ("instanceClass".equals(attribute.getAttribute(FILENAME))) { // NOI18N
                                cslLanguageClass = attribute.getAttribute(STRINGVALUE);
                            }
                        }
                    }
                }


                if (!customEditorKit) {
                    // Loader registration

                    String linePrefix = null;
                    String displayName = null;
                    boolean hasDeclarationFinder = true; // unless we find out otherwise

                    try {
                        List<URL> urls = new ArrayList<URL>();
                        String classDir = getProject().getProperty("build.classes.dir"); // NOI18N
                        if (classDir != null && classDir.length() > 0) {
                            urls.add(new File(classDir).toURI().toURL());
                        }
                        String nballString = getProject().getProperty("nb_all");
                        if (nballString != null && nballString.length() > 0) {
                            File nball = new File(nballString); // NOI18N
                            if (nball.exists()) {
                                // This is a hack! I should try to find a good classloader which loads in ALL the
                                // jars needed by this module!! Can I use the classpath from the compilation step
                                // somehow? For now: MAJOR OVERKILL: I'm loading ALL modules...
                                for (File dir : nball.listFiles()) {
                                    if (dir.isDirectory()) {
                                        File moduleJar = new File(dir, "build" + File.separator + "classes"); // NOI18N
                                        urls.add(moduleJar.toURI().toURL());
                                    }
                                }
                            }
                        }
                        ClassLoader myClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));

                        Class<?> clz = Class.forName(cslLanguageClass, true, myClassLoader);
                        Object o = clz.newInstance();
                        if (o != null) {
                            Method method = clz.getMethod("getLineCommentPrefix", (Class[])null); // NOI18N
                            Object ret = method.invoke(o, (Object[])null);
                            if (ret != null) {
                                linePrefix = ret.toString();
                            }
                            method = clz.getMethod("getDisplayName", (Class[])null); // NOI18N
                            ret = method.invoke(o, (Object[])null);
                            if (ret != null) {
                                displayName = ret.toString();
                            }
                            method = clz.getMethod("getDeclarationFinder", (Class[])null); // NOI18N
                            ret = method.invoke(o, (Object[])null);
                            if (ret == null) {
                                hasDeclarationFinder = false;
                            }
                        }
                    } catch (NoClassDefFoundError ncdfe) {
                        // Not unexpected...
                        System.out.println("      Warning: Could not find " + cslLanguageClass + " so not introspecting the language config (" + ncdfe.getMessage() + ")");
                    } catch (ClassNotFoundException cfne) {
                        // Not unexpected...
                        System.out.println("      Warning: Could not find " + cslLanguageClass + " so not introspecting the language config (" + cfne.getMessage() + ")");
                    } catch (MalformedURLException mfue) {
                        System.out.println("      Warning: Malformed URL in directory name");
                    } catch (NoSuchMethodException nsme) {
                        // Not unexpected
                        System.out.println("      Warning: Could not create " + cslLanguageClass + " so not introspecting the language config");
                    } catch (IllegalAccessException iae) {
                        // Not unexpected
                        System.out.println("     Warning: Could not create " + cslLanguageClass + " so not introspecting the language config");
                    } catch (InvocationTargetException ite) {
                        // Not unexpected
                        System.out.println("     Warning: Could not create " + cslLanguageClass + " so not introspecting the language config");
                    } catch (InstantiationException ie) {
                        // Not unexpected
                        System.out.println("     Warning: Could not create " + cslLanguageClass + " so not introspecting the language config");
                    }


                    registerLoader(doc, mimeType, displayName);
                    registerPathRecognizer(doc, mimeType);
                    registerEditorServices(doc, mimeType, cslLanguageClass, linePrefix, displayName, hasStructureScanner, hasDeclarationFinder);
                }

                // TL Indexer factory
                Element mimeFolder = mkdirs(doc, "Editors/" + mimeType); // NOI18N
                Element item = createFile(doc, mimeFolder, "org-netbeans-modules-csl-core-TLIndexerFactory.instance"); // NOI18N
                setFileAttribute(doc, item, "instanceOf", "stringvalue", "org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory"); //NOI18N
            }
        }
    }

    private List<Element> getElementChildren(Element element) {
        NodeList nl = element.getChildNodes();
        List<Element> children = new ArrayList<Element>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                children.add(childElement);
            }
        }

        return children;
    }

    private Element findPath(Document doc, String path) throws BuildException {
        String tagName = doc.getDocumentElement().getTagName();
        if (!tagName.equals(FILESYSTEM)) { // NOI18N
            throw new BuildException("Unexpected layer root element: " + tagName);
        }

        return findPath(doc.getDocumentElement(), path);
    }

    private Element findPath(Element element, String path) {
        int index = path.indexOf('/');
        String name = path;
        if (index != -1) {
            name = path.substring(0, index);
            path = path.substring(index + 1);
        }
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                if (name.equals(childElement.getAttribute(FILENAME))) {
                    if (!childElement.getTagName().equals(FOLDER) && // NOI18N
                            !childElement.getTagName().equals(FILE)) { // NOI18N
                        throw new BuildException("The layer filesystem items should be <folder> or <file> - was " + childElement.getTagName());
                    }
                    if (childElement.getAttribute(FILENAME).length() == 0) {
                        throw new BuildException("Layer file error? There was no " + FILENAME + " attribute on file " + childElement.getTagName());
                    }
                    if (index == -1) {
                        return childElement;
                    } else {
                        Element result = findPath(childElement, path);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }

        return null;
    }

    private Element createFile(Document doc, Element parent, String name) {
        parent.appendChild(doc.createTextNode("\n")); // NOI18N
        Element file = doc.createElement(FILE);
        file.setAttribute(FILENAME, name);
        parent.appendChild(file);
        parent.appendChild(doc.createTextNode("\n")); // NOI18N

        return file;
    }

    private void setFileAttribute(Document doc, Element file, String name, String valueType, String value) {
        Element attribute = doc.createElement(ATTR);
        attribute.setAttribute(FILENAME, name);
        attribute.setAttribute(valueType, value);
        file.appendChild(doc.createTextNode("\n    ")); // NOI18N
        file.appendChild(attribute);
    }

    private Element mkdirs(Document doc, String path) throws BuildException {
        String tagName = doc.getDocumentElement().getTagName();
        if (!tagName.equals(FILESYSTEM)) { // NOI18N
            throw new BuildException("Unexpected layer root element: " + tagName);
        }

        return mkdirs(doc.getDocumentElement(), path);
    }

    private Element mkdirs(Element element, String path) {
        int index = path.indexOf('/');
        String name = path;
        if (index != -1) {
            name = path.substring(0, index);
            path = path.substring(index + 1);
        }
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                if (name.equals(childElement.getAttribute(FILENAME))) {
                    if (!childElement.getTagName().equals(FOLDER) && // NOI18N
                            !childElement.getTagName().equals(FILE)) { // NOI18N
                        throw new BuildException("The layer filesystem items should be <folder> or <file> - was " + childElement.getTagName());
                    }
                    if (childElement.getAttribute(FILENAME).length() == 0) {
                        throw new BuildException("Layer file error? There was no " + FILENAME + " attribute on file " + childElement.getTagName());
                    }
                    if (index == -1) {
                        return childElement;
                    } else {
                        Element result = mkdirs(childElement, path);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }

        // Didn't find the child - so create it!
        Element folder = element.getOwnerDocument().createElement(FOLDER);
        folder.setAttribute(FILENAME, name);
        element.appendChild(element.getOwnerDocument().createTextNode("\n"));
        element.appendChild(folder);
        element.appendChild(element.getOwnerDocument().createTextNode("\n"));

        if (index == -1) {
            return folder;
        } else {
            return mkdirs(folder, path);
        }
    }

    private void registerLoader(Document doc, String mimeType, String displayName) {
        Element factoryFolder = mkdirs(doc, "Loaders/" + mimeType + "/Factories"); // NOI18N

        if (findPath(factoryFolder, "org-netbeans-modules-csl-GsfDataLoader.instance") != null) { // NOI18N
            // Already registered!
            return;
        }

        Element file = createFile(doc, factoryFolder, "org-netbeans-modules-csl-core-GsfDataLoader.instance"); // NOI18N
        setFileAttribute(doc, file, "position", INTVALUE, "89998"); // NOI18N
        if (displayName != null && displayName.length() > 0) {
            setFileAttribute(doc, file, "displayName", STRINGVALUE, displayName); // NOI18N
        }
    }

    private List<Element> getAttributeElements(Document doc, String path) {
        Element parent = findPath(doc, path);

        List<Element> attributes = new ArrayList<Element>();

        if (parent != null) {
            for (Element f : getElementChildren(parent)) {
                String tag = f.getTagName();
                if (ATTR.equals(tag)) { // NOI18N
                    attributes.add(f);
                }
            }
        }

        return attributes;
    }

    private List<Element> getAttributeElements(Element parent) {
        List<Element> attributes = new ArrayList<Element>();

        if (parent != null) {
            for (Element f : getElementChildren(parent)) {
                String tag = f.getTagName();
                if (ATTR.equals(tag)) { // NOI18N
                    attributes.add(f);
                }
            }
        }

        return attributes;
    }

    private String makeFilesystemName(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append("-"); //NOI18N
            }
        }
        return sb.toString();
    }

    private void registerStructureScanner(Document doc, String mimeType) {
        Element navigatorFolder = mkdirs(doc, "Navigator/Panels/" + mimeType); // NOI18N
        createFile(doc, navigatorFolder, "org-netbeans-modules-csl-navigation-ClassMemberPanel.instance"); // NOI18N
    }

    private void registerPathRecognizer(Document doc, String mimeType) {
        Element servicesFolder = mkdirs(doc, "Services/Hidden/PathRecognizers"); // NOI18N
        String instanceFile = "org-netbeans-modules-csl-core-PathRecognizerImpl-" + makeFilesystemName(mimeType) + ".instance"; //NOI18N

        if (findPath(servicesFolder, instanceFile) != null) {
            // Already registered!
            return;
        }

        Element file = createFile(doc, servicesFolder, instanceFile);
        setFileAttribute(doc, file, "mimeType", STRINGVALUE, mimeType); // NOI18N
        setFileAttribute(doc, file, "instanceOf", STRINGVALUE, "org.netbeans.modules.parsing.spi.indexing.PathRecognizer"); // NOI18N
        setFileAttribute(doc, file, "instanceCreate", METHODVALUE, "org.netbeans.modules.csl.core.PathRecognizerImpl.createInstance"); // NOI18N
    }

    private void registerEditorServices(Document doc, String mimeType, String gsfLanguageClass, String linePrefix, String displayName, boolean hasStructureScanner, boolean hasDeclarationFinder) {
        Element mimeFolder = mkdirs(doc, "Editors/" + mimeType); // NOI18N
        // Hyperlinks
        if (hasDeclarationFinder) {
            Element hyperlinkFolder = mkdirs(doc, "Editors/" + mimeType + "/HyperlinkProviders"); // NOI18N
            Element file = createFile(doc, hyperlinkFolder, "GsfHyperlinkProvider.instance"); // NOI18N
            setFileAttribute(doc, file, "instanceClass", STRINGVALUE, "org.netbeans.modules.csl.editor.hyperlink.GsfHyperlinkProvider"); // NOI18N
            setFileAttribute(doc, file, "instanceOf", STRINGVALUE, "org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt"); // NOI18N
            setFileAttribute(doc, file, "position", INTVALUE, "1000"); // NOI18N
        }
        
        // Code Completion
        Element completionFolder = mkdirs(doc, "Editors/" + mimeType + "/CompletionProviders"); // NOI18N
        createFile(doc, completionFolder, "org-netbeans-lib-editor-codetemplates-CodeTemplateCompletionProvider.instance"); // NOI18N
        createFile(doc, completionFolder, "org-netbeans-modules-csl-editor-completion-GsfCompletionProvider.instance"); // NOI18N

        // Context menu
        Element popupFolder = mkdirs(doc, "Editors/" + mimeType + "/Popup"); // NOI18N

        Element renameFile = createFile(doc, popupFolder, "in-place-refactoring"); // NOI18N
        setFileAttribute(doc, renameFile, "position", INTVALUE, "680"); // NOI18N

        boolean alreadyLocalized = false;
        boolean alreadyPositioned = false;
        List<Element> gotoAttributes = getAttributeElements(doc, "Editors/" + mimeType + "/Popup/goto"); // NOI18N
        for (Element gotoAttribute : gotoAttributes) {
            if (gotoAttribute.getAttribute(FILENAME).equals("SystemFileSystem.localizingBundle") || //NOI18N
                gotoAttribute.getAttribute(FILENAME).equals("displayName") //NOI18N
            ) {
                alreadyLocalized = true;
            }
            if (gotoAttribute.getAttribute(FILENAME).equals("position")) { // NOI18N
                alreadyPositioned = true;
            }
        }

        Element gotoFolder = findPath(mimeFolder, "Popup/goto");
        if (gotoFolder == null) {
            gotoFolder = mkdirs(mimeFolder, "Popup/goto"); // NOI18N
        }
        if (!alreadyPositioned) {
            setFileAttribute(doc, gotoFolder, "position", INTVALUE, "500"); // NOI18N
        }

        if (!alreadyLocalized) {
            setFileAttribute(doc, gotoFolder, "displayName", BUNDLEVALUE, "org.netbeans.modules.csl.core.Bundle#generate-goto-popup");
        }

        Element item;
        if (hasDeclarationFinder) {
            item = createFile(doc, gotoFolder, "goto-declaration"); // NOI18N
            setFileAttribute(doc, item, "position", INTVALUE, "500"); // NOI18N
        }

        // Goto by linenumber
        item = createFile(doc, gotoFolder, "goto");  // NOI18N
        setFileAttribute(doc, item, "position", INTVALUE, "600"); // NOI18N

        // What about goto-source etc?
        // TODO: Goto Type (integrate with Java's GotoType)

        item = createFile(doc, popupFolder, "SeparatorBeforeCut.instance"); // NOI18N
        setFileAttribute(doc, item, "position", INTVALUE, "1200"); // NOI18N
        setFileAttribute(doc, item, "instanceClass", STRINGVALUE, "javax.swing.JSeparator"); // NOI18N

        item = createFile(doc, popupFolder, "format"); // NOI18N
        setFileAttribute(doc, item, "position", INTVALUE, "750"); // NOI18N

        item = createFile(doc, popupFolder, "SeparatorAfterFormat.instance"); // NOI18N
        // Should be between org-openide-actions-PasteAction.instance and format
        setFileAttribute(doc, item, "position", INTVALUE, "780"); // NOI18N
        setFileAttribute(doc, item, "instanceClass", STRINGVALUE, "javax.swing.JSeparator"); // NOI18N

        // UpToDateStatusProviders
        Element upToDateFolder = mkdirs(doc, "Editors/" + mimeType + "/UpToDateStatusProvider"); // NOI18N
        item = createFile(doc, upToDateFolder, "org-netbeans-modules-csl-hints-GsfUpToDateStateProviderFactory.instance"); // NOI18N
        item = createFile(doc, upToDateFolder, "org-netbeans-modules-csl-editor-semantic-OccurrencesMarkProviderCreator.instance"); // NOI18N

        // Code Folding
        if (hasStructureScanner) {
            Element sideBarFolder = mkdirs(doc, "Editors/" + mimeType + "/SideBar"); // NOI18N
            Element sidebarFile = createFile(doc, sideBarFolder, "org-netbeans-modules-csl-editor-GsfCodeFoldingSideBarFactory.instance"); // NOI18N
            setFileAttribute(doc, sidebarFile, "position", INTVALUE, "1200"); // NOI18N

            Element foldingFolder = mkdirs(doc, "Editors/" + mimeType + "/FoldManager"); // NOI18N
            createFile(doc, foldingFolder, "org-netbeans-modules-csl-editor-fold-GsfFoldManagerFactory.instance"); // NOI18N
        }

        // Highlighting Factories
        item = createFile(doc, mimeFolder, "org-netbeans-modules-csl-editor-semantic-HighlightsLayerFactoryImpl.instance"); // NOI18N

        // Toolbar
        if (linePrefix != null && linePrefix.length() > 0) {
            // Yes, found line comment prefix - register comment/uncomment toolbar buttons!
            Element toolbarFolder = mkdirs(mimeFolder, "Toolbars/Default"); // NOI18N

            item = createFile(doc, toolbarFolder, "Separator-before-comment.instance"); // NOI18N
            setFileAttribute(doc, item, "instanceClass", STRINGVALUE, "javax.swing.JSeparator"); // NOI18N
            setFileAttribute(doc, item, "position", INTVALUE, "30000"); // NOI18N

            item = createFile(doc, toolbarFolder, "comment"); // NOI18N
            setFileAttribute(doc, item, "position", INTVALUE, "30100"); // NOI18N

            item = createFile(doc, toolbarFolder, "uncomment"); // NOI18N
            setFileAttribute(doc, item, "position", INTVALUE, "30200"); // NOI18N
        }

        // Code Templates
        Element codeProcessorFolder = mkdirs(mimeFolder, "CodeTemplateProcessorFactories"); // NOI18N
        item = createFile(doc, codeProcessorFolder, "org-netbeans-modules-csl-editor-codetemplates-GsfCodeTemplateProcessor$Factory.instance"); // NOI18N

        // Code Template Filters
        Element codeFilter = mkdirs(mimeFolder, "CodeTemplateFilterFactories"); // NOI18N
        item = createFile(doc, codeFilter, "org-netbeans-modules-csl-editor-codetemplates-GsfCodeTemplateFilter$Factory.instance"); // NOI18N

        // Parser factory
        item = createFile(doc, mimeFolder, "org-netbeans-modules-csl-core-GsfParserFactory.instance"); // NOI18N
        setFileAttribute(doc, item, "instanceCreate", METHODVALUE, "org.netbeans.modules.csl.core.GsfParserFactory.create"); //NOI18N
        setFileAttribute(doc, item, "instanceOf", STRINGVALUE, "org.netbeans.modules.parsing.spi.ParserFactory"); //NOI18N

        // Indexer factory
        item = createFile(doc, mimeFolder, "org-netbeans-modules-csl-core-EmbeddingIndexerFactoryImpl.instance"); // NOI18N
        setFileAttribute(doc, item, "instanceCreate", METHODVALUE, "org.netbeans.modules.csl.core.EmbeddingIndexerFactoryImpl.create"); //NOI18N
        setFileAttribute(doc, item, "instanceOf", STRINGVALUE, "org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory"); //NOI18N
    }
}
