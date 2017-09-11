/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.apisupport.crudsample;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SampleAppWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private static final long serialVersionUID = 1L;

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private SampleAppWizardExtraPanel configurationPanel;
    
    public SampleAppWizardIterator() {}
    
    public static SampleAppWizardIterator createIterator() {
        return new SampleAppWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        configurationPanel = new SampleAppWizardExtraPanel();
        return new WizardDescriptor.Panel[] {
            new SampleAppWizardPanel(configurationPanel.isValid()),
            configurationPanel
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(SampleAppWizardIterator.class, "LBL_CreateProjectStep"), // NOI18N
            NbBundle.getMessage(SampleAppWizardIterator.class, "LBL_CreatePersistenceStep"), // NOI18N
        };
    }
    
    @Override
    public Set/*<FileObject>*/ instantiate() throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
        dirF.mkdirs();
        
        FileObject template = Templates.getTemplate(wiz);
        FileObject dir = FileUtil.toFileObject(dirF);

        // 1) unzip
        unZipFile(template.getInputStream(), dir);
        try {
            // 2) copy persistence libraries
            copyPersistenceLibraries(configurationPanel.getSelectedLibrary(), dir);
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        } catch (IllegalStateException ex) {
            throw new IOException(ex);
        }

        // 3) set DB location in Derby module
        configureDerby(configurationPanel.getDerbyLocation(), dir);

        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = (FileObject) e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }
        
        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        
        return resultSet;
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir",null); // NOI18N
        this.wiz.putProperty("name",null); // NOI18N
        this.wiz = null;
        panels = null;
    }
    
    @Override
    public String name() {
        return NbBundle.getMessage(SampleAppWizardIterator.class, "SampleAppWizardIterator.name.format", // NOI18N
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    
    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {}
    @Override
    public final void removeChangeListener(ChangeListener l) {}
    
    private static void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    OutputStream out = fo.getOutputStream();
                    try {
                        FileUtil.copy(str, out);
                    } finally {
                        out.close();
                    }
                }
            }
        } finally {
            source.close();
        }
    }

    private void copyPersistenceLibraries(Library l, FileObject projectRoot) throws URISyntaxException, IllegalStateException, FileNotFoundException, IOException {
        // 1) source libraries
        List<FileObject> libs = new ArrayList<FileObject>();
        for (URL url : l.getContent("classpath")) { //NOI18N
            FileObject fo = URLMapper.findFileObject(url);
            Logger.getLogger(SampleAppWizardIterator.class.getName()).log(Level.FINE, "Libary {0} has jar: {1}", new Object[]{l.getName(), fo});
            FileObject jarFO = null;
            if ("jar".equals(url.getProtocol())) {  //NOI18N
                jarFO = FileUtil.getArchiveFile(fo);
            }
            if (jarFO == null) {
                throw new IllegalStateException("No file object on " + url);
            }
            libs.add(jarFO);
        }

        // 2) target place
        File targetFile = new File(FileUtil.toFile(projectRoot), "CustomerDBAccessLibrary" + File.separator + "external"); // NOI18N
        targetFile.mkdirs();
        FileObject targetFO = FileUtil.toFileObject(targetFile);

        // 3) copying
        for (FileObject fo : libs) {
            File f = FileUtil.toFile(fo);
            Logger.getLogger(SampleAppWizardIterator.class.getName()).log(Level.FINE, "Copy {0} in {1} as {2}", new Object[]{fo, targetFO, f.getName()});
            FileUtil.copyFile(fo, targetFO, fo.getName());
        }

        // 4) modify project.xml with native jars names
        File projectXMLFile = new File(FileUtil.toFile(projectRoot), "CustomerDBAccessLibrary" + File.separator + // NOI18N
                "nbproject" + File.separator + "project.xml"); // NOI18N
        FileObject projectXMLFO = FileUtil.toFileObject(projectXMLFile);

        Set<String> publicPackages = new HashSet<String> ();
        Collections.addAll(publicPackages, "demo",
                "javax.persistence",
                "javax.persistence.spi",
                "javax.persistence.metamodel",
                "javax.persistence.criteria",
                "org.netbeans.modules.customerdb"); // NOI18N
        Map<String, String> cp2origin = new HashMap<String, String>(libs.size());
        // add wrapping project CustomerDBAccess
        cp2origin.put("ext/CustomerDBAccess.jar", "../CustomerDBAccess/dist/CustomerDBAccess.jar"); // NOI18N
        for (FileObject jar : libs) {
            String cp = "ext/persistence/" + jar.getNameExt(); // NOI18N
            String origin = "external/" + jar.getNameExt(); // NOI18N
            cp2origin.put(cp, origin);
        }
        Map<String, String> dep2version = new HashMap<String, String> ();
        dep2version.put("org.netbeans.modules.derbyclientlibrary", "1.1"); // NOI18N
        dep2version.put("org.openide.execution", "9.0"); // NOI18N
        dep2version.put("org.openide.util", "9.3"); // NOI18N
        generateLibraryModuleTemplate(projectXMLFO, "org.netbeans.modules.customerdb", // NOI18N
                publicPackages,
                cp2origin,
                dep2version);
        Logger.getLogger(SampleAppWizardIterator.class.getName()).log(Level.FINE, "project.xml file written : {0}", new Object[]{projectXMLFO}); // NOI18N

        // 5)  modify $persistencelibrary.xml to persistence.xml
        File libraryConfFile = new File(FileUtil.toFile(projectRoot), "CustomerDBAccess" + File.separator + // NOI18N
                "src" + File.separator + "META-INF" + File.separator + l.getName() + ".xml"); // NOI18N
        Logger.getLogger(SampleAppWizardIterator.class.getName()).log(Level.FINE, "META-INF/peristence.xml found at {0}", new Object[]{libraryConfFile});
        assert libraryConfFile.exists() : libraryConfFile + " exists."; // NOI18N
        final File persistenceConfFile = new File(libraryConfFile.getParent(), "persistence.xml"); // NOI18N
        libraryConfFile.renameTo(persistenceConfFile);
        // remove other xml containing persistence info
        for (File f : libraryConfFile.getParentFile().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".xml") && ! persistenceConfFile.equals(pathname); //
                }
            })) {
                f.delete();
            }
    }

    private void configureDerby(String loc, FileObject projectRoot) throws FileNotFoundException, IOException {
        File projectPropertiesFile = new File(FileUtil.toFile(projectRoot), "derbyclient-library" + File.separator + // NOI18N
                "nbproject" + File.separator + "project.properties"); // NOI18N
        FileObject projectPropertiesFO = FileUtil.toFileObject(projectPropertiesFile);
        Properties projectProperties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = projectPropertiesFO.getInputStream();
            projectProperties.load(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        projectProperties.setProperty("derbyclient.jar", loc + "/lib/derbyclient.jar"); // NOI18N
        OutputStream outputStream = null;
        try {
            outputStream = projectPropertiesFO.getOutputStream();
            projectProperties.store(outputStream, null);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        Logger.getLogger(SampleAppWizardIterator.class.getName()).log(Level.FINE, "derbyclient.jar location in project.properties is {0}", new Object[]{loc + "/lib/derbyclient.jar"});

        File bundleFile = new File(FileUtil.toFile(projectRoot), "CustomerDBAccessLibrary" + File.separator + // NOI18N
                "src" + File.separator + // NOI18N
                "org" + File.separator + // NOI18N
                "netbeans" + File.separator + // NOI18N
                "modules" + File.separator + // NOI18N
                "customerdb" + File.separator + // NOI18N
                "Bundle.properties"); // NOI18N
        FileObject bundleFO = FileUtil.toFileObject(bundleFile);
        Properties bundleProperties = new Properties();
        inputStream = null;
        try {
            inputStream = bundleFO.getInputStream();
            bundleProperties.load(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        bundleProperties.setProperty("javadb.home", loc); // NOI18N
        outputStream = null;
        try {
            outputStream = bundleFO.getOutputStream();
            bundleProperties.store(outputStream, null);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        Logger.getLogger(SampleAppWizardIterator.class.getName()).log(Level.FINE, "JavaDB home is {0}", new Object[]{loc});
    }

    /**
     * Create a library wrapper project.xml.
     *
     * @param publicPackages set of <code>String</code>s representing the packages
     * @param extensions &lt;key=runtime path(String), value=binary path (String)&gt;
     *
     * XXX: copy of apisupport.project/src/org/netbeans/modules/apisupport/project/ProjectXMLManager.java
     */
    private static void generateLibraryModuleTemplate(FileObject projectXml, String cnb,
            Set<String> publicPackages, Map<String,String> extensions, Map<String, String> dep2version) throws IOException {

        Document prjDoc = XMLUtil.createDocument("project", "http://www.netbeans.org/ns/project/1", null, null); // NOI18N

        // generate general project elements
        Element typeEl = prjDoc.createElementNS("http://www.netbeans.org/ns/project/1", "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode("org.netbeans.modules.apisupport.project")); // NOI18N
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS("http://www.netbeans.org/ns/project/1", "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);

        // generate NB Module project type specific elements
        Element dataEl = createModuleElement(confEl.getOwnerDocument(), "data"); // NOI18N
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createModuleElement(dataDoc, "code-name-base", cnb)); // NOI18N
        Element moduleTypeEl = createModuleElement(dataDoc, "suite-component"); // NOI18N
        if (moduleTypeEl != null) {
            dataEl.appendChild(moduleTypeEl);
        }
        Element moduleDependeciesElement = createModuleElement(dataDoc, "module-dependencies"); // NOI18N
        if (dep2version != null && ! dep2version.isEmpty()) {
            for (String dep : dep2version.keySet()) {
                createModuleDependencyElement(moduleDependeciesElement, dep, dep2version.get(dep));
            }
        }
        dataEl.appendChild(moduleDependeciesElement);
        Element packages = createModuleElement(dataDoc, "public-packages"); // NOI18N
        dataEl.appendChild(packages);
        for (String pkg : publicPackages) {
            packages.appendChild(createModuleElement(dataDoc, "package", pkg)); // NOI18N
        }
        for (Map.Entry<String,String> entry : extensions.entrySet()) {
            Element cp = createModuleElement(dataDoc, "class-path-extension"); // NOI18N
            dataEl.appendChild(cp);
            cp.appendChild(createModuleElement(dataDoc, "runtime-relative-path", entry.getKey())); // NOI18N
            cp.appendChild(createModuleElement(dataDoc, "binary-origin", entry.getValue())); // NOI18N
        }

        // store document to disk
        OutputStream os = projectXml.getOutputStream();
        try {
            XMLUtil.write(prjDoc, os, "UTF-8"); // NOI18N
        } finally {
            os.close();
        }
    }

    static void createModuleDependencyElement(
            Element moduleDependencies, String depCNB, String depSV) {

        Document doc = moduleDependencies.getOwnerDocument();
        Element modDepEl = createModuleElement(doc, "dependency"); // NOI18N
        moduleDependencies.insertBefore(modDepEl, null);

        modDepEl.appendChild(createModuleElement(doc, "code-name-base", depCNB)); // NOI18N
        if (depCNB.startsWith("org.openide")) { // NOI18N
            modDepEl.appendChild(createModuleElement(doc, "build-prerequisite")); // NOI18N
            modDepEl.appendChild(createModuleElement(doc, "compile-dependency")); // NOI18N
        }

        Element runDepEl = createModuleElement(doc, "run-dependency"); // NOI18N
        modDepEl.appendChild(runDepEl);

        if (depSV != null && ! "".equals(depSV)) { // NOI18N
            runDepEl.appendChild(createModuleElement(
                    doc, "specification-version", depSV)); // NOI18N
        }
    }

    private static Element createModuleElement(Document doc, String name) {
        return doc.createElementNS("http://www.netbeans.org/ns/nb-module-project/3", name); // NOI18N
    }

    private static Element createModuleElement(Document doc, String name, String innerText) {
        Element el = createModuleElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }

}
