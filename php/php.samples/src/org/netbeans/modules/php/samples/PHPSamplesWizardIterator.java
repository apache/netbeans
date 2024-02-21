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
package org.netbeans.modules.php.samples;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PHPSamplesWizardIterator implements WizardDescriptor./*Progress*/InstantiatingIterator {

    private static final boolean DO_NOT_OPEN_README_HTML = Boolean.getBoolean("org.netbeans.modules.php.samples.donotopenreadmehtml"); // NOI18N
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    public PHPSamplesWizardIterator() {
    }

    public static PHPSamplesWizardIterator createIterator() {
        return new PHPSamplesWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[]{
                    new PHPSamplesWizardPanel(),
                };
    }

    private String[] createSteps() {
        return new String[]{
                    NbBundle.getMessage(PHPSamplesWizardIterator.class, "LBL_CreateProjectStep")
                };
    }

    public Set/*<FileObject>*/ instantiate(/*ProgressHandle handle*/) throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<>();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty(WizardProperties.PROJ_DIR));
        createFolder(dirF);

        FileObject template = Templates.getTemplate(wiz);
        FileObject dir = FileUtil.toFileObject(dirF);
        unZipFile(template.getInputStream(), dir);
        ProjectManager.getDefault().clearNonProjectCache();

        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration<? extends FileObject> e = dir.getFolders(true);
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

        if (!DO_NOT_OPEN_README_HTML) {
            // Open readme.html in a browser
            File urlTempF = Files.createTempFile("phpSamplesReadme", ".url").toFile(); // NOI18N

            urlTempF.deleteOnExit();
            FileObject readmeURL = FileUtil.toFileObject(FileUtil.normalizeFile(urlTempF));
            writeLines(readmeURL, dir.getFileObject("readme.html").toURL().toString()); // NOI18N

            resultSet.add(readmeURL);
        }

        return resultSet;
    }

    // Workaround for opening the readme.html file
    // TODO: use FileUtils when #118087 is fixed
    private static void writeLines(final FileObject readme, final String... lines) throws FileAlreadyLockedException, IOException {
        PrintWriter readmeW = new PrintWriter(FileUtil.toFile(readme), "UTF-8"); // NOI18N
        for (String line : lines) {
            readmeW.println(line);
        }
        readmeW.close();
    }

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
                jc.putClientProperty(WizardProperties.SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardProperties.CONTENT_DATA, steps);
            }
        }

        FileObject template = Templates.getTemplate(wiz);

        wiz.putProperty(WizardProperties.NAME, template.getName());
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardProperties.PROJ_DIR, null);
        this.wiz.putProperty(WizardProperties.NAME, null);
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{Integer.valueOf(index + 1), Integer.valueOf(panels.length)});
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

    private static void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    if ("nbproject/project.xml".equals(entry.getName())) {
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterProjectXML(fo, str, projectRoot.getName());
                    } else {
                        writeFile(str, fo);
                    }
                }
            }
        } finally {
            source.close();
        }
    }

    private static void writeFile(ZipInputStream str, FileObject fo) throws IOException {
        OutputStream out = fo.getOutputStream();
        try {
            FileUtil.copy(str, out);
        } finally {
            out.close();
        }
    }

    private static void filterProjectXML(FileObject fo, ZipInputStream str, String name) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(str, baos);
            Document doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, false, null, null);
            NodeList nl = doc.getDocumentElement().getElementsByTagName("name");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    if (el.getParentNode() != null && "data".equals(el.getParentNode().getNodeName())) {
                        NodeList nl2 = el.getChildNodes();
                        if (nl2.getLength() > 0) {
                            nl2.item(0).setNodeValue(name);
                        }
                        break;
                    }
                }
            }
            OutputStream out = fo.getOutputStream();
            try {
                XMLUtil.write(doc, out, "UTF-8");
            } finally {
                out.close();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            writeFile(str, fo);
        }

    }

    private static FileObject createFolder(File dir) throws IOException {
        Stack<String> stack = new Stack<>();
        while (!dir.exists()) {
            stack.push(dir.getName());
            dir = dir.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(dir);
        if (dirFO == null) {
            refreshFileSystem(dir);
            dirFO = FileUtil.toFileObject(dir);
        }
        assert dirFO != null;
        while (!stack.isEmpty()) {
            dirFO = dirFO.createFolder(stack.pop());
        }
        return dirFO;
    }

    private static void refreshFileSystem(final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N

        dirFO.getFileSystem().refresh(false);
    }
}
