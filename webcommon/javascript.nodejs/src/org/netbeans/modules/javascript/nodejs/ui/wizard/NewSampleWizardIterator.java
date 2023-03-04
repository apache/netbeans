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
package org.netbeans.modules.javascript.nodejs.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.simple.JSONValue;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class NewSampleWizardIterator extends BaseWizardIterator {

    private static final Logger LOGGER = Logger.getLogger(NewSampleWizardIterator.class.getName());

    private final String wizardTitle;
    private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;

    private NewSampleWizardIterator(String wizardTitle, String projectName) {
        assert wizardTitle != null;
        assert projectName != null;
        this.wizardTitle = wizardTitle;
        baseWizard = CreateProjectUtils.createBaseWizardPanel(projectName);
    }

    @TemplateRegistration(
            folder = "Project/Samples/HTML5",
            content = "../../samples/ListDirectory.zip",
            displayName = "#NewSampleWizardIterator.newListDirectorySample.displayName",
            description = "../resources/NewListDirectorySampleDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 1500)
    @NbBundle.Messages("NewSampleWizardIterator.newListDirectorySample.displayName=List Directory Sample")
    public static NewSampleWizardIterator newListDirectorySample() {
        return new NewSampleWizardIterator(
                Bundle.NewSampleWizardIterator_newListDirectorySample_displayName(),
                "ListDirectory"); // NOI18N
    }

    @TemplateRegistration(
            folder = "Project/Samples/HTML5",
            content = "../../samples/MessagesKnockout.zip",
            displayName = "#NewSampleWizardIterator.newMessagesKnockoutSample.displayName",
            description = "../resources/NewMessagesKnockoutSampleDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 1510)
    @NbBundle.Messages("NewSampleWizardIterator.newMessagesKnockoutSample.displayName=Knockout Messages Sample")
    public static NewSampleWizardIterator newMessagesKnockoutSample() {
        return new NewSampleWizardIterator(
                Bundle.NewSampleWizardIterator_newMessagesKnockoutSample_displayName(),
                "MessagesKnockout"); // NOI18N
    }

    @TemplateRegistration(
            folder = "Project/Samples/HTML5",
            content = "../../samples/MessagesAngular.zip",
            displayName = "#NewSampleWizardIterator.newMessagesAngularSample.displayName",
            description = "../resources/NewMessagesAngularSampleDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 1520)
    @NbBundle.Messages("NewSampleWizardIterator.newMessagesAngularSample.displayName=Angular Messages Sample")
    public static NewSampleWizardIterator newMessagesAngularSample() {
        return new NewSampleWizardIterator(
                Bundle.NewSampleWizardIterator_newMessagesAngularSample_displayName(),
                "MessagesAngular"); // NOI18N
    }

    @TemplateRegistration(
            folder = "Project/Samples/HTML5",
            content = "../../samples/MessagesExpress.zip",
            displayName = "#NewSampleWizardIterator.newMessagesExpressSample.displayName",
            description = "../resources/NewMessagesExpressSampleDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 1530)
    @NbBundle.Messages("NewSampleWizardIterator.newMessagesExpressSample.displayName=Express/Jade Messages Sample")
    public static NewSampleWizardIterator newMessagesExpressSample() {
        return new NewSampleWizardIterator(
                Bundle.NewSampleWizardIterator_newMessagesExpressSample_displayName(),
                "MessagesExpress"); // NOI18N
    }

    @Override
    String getWizardTitle() {
        return wizardTitle;
    }

    @Override
    WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        return new WizardDescriptor.Panel[] {
            baseWizard.first(),
        };
    }

    @Override
    String[] createSteps() {
        return new String[] {
            baseWizard.second(),
        };
    }

    @Override
    void uninitializeInternal() {
        // noop
    }

    @NbBundle.Messages("NewSampleWizardIterator.progress.creating=Creating project")
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.NewSampleWizardIterator_progress_creating());

        Set<FileObject> files = new HashSet<>();

        // project dir
        File projectDir = FileUtil.normalizeFile((File) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_DIRECTORY));
        if (!projectDir.isDirectory()
                && !projectDir.mkdirs()) {
            throw new IOException("Cannot create project directory: " + projectDir);
        }
        final FileObject projectDirectory = FileUtil.toFileObject(projectDir);
        assert projectDirectory != null : "FileObject must be found for " + projectDir;
        files.add(projectDirectory);

        // unzip sample
        final FileObject template = Templates.getTemplate(wizardDescriptor);
        final String projectName = (String) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_NAME);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    unZipFile(template, projectDirectory, projectName);
                    return null;
                }
            });
        } catch (MutexException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            throw (IOException) ex.getException();
        }
        ProjectManager.getDefault().clearNonProjectCache();

        // start file
        Project project = FileOwnerQuery.getOwner(projectDirectory);
        assert project != null : projectDirectory;
        NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
        String startFile = nodeJsSupport.getPreferences().getStartFile();
        assert startFile != null : projectDirectory;
        FileObject fo = FileUtil.toFileObject(new File(startFile));
        assert fo != null : startFile;
        files.add(fo);

        // package.json?
        PackageJson packageJson = nodeJsSupport.getPackageJson();
        if (packageJson.exists()) {
            files.add(FileUtil.toFileObject(packageJson.getFile()));
        }

        handle.finish();
        return files;
    }

    static void unZipFile(FileObject template, FileObject projectDir, String projectName) throws IOException {
        try (InputStream source = template.getInputStream()) {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                final String name = entry.getName();
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectDir, name);
                } else {
                    FileObject fo = FileUtil.createData(projectDir, name);
                    if ("nbproject/project.xml".equals(name)) { // NOI18N
                        // set proper project name
                        filterProjectXml(fo, str, projectName);
                    } else if ("package.json".equals(name) // NOI18N
                            || "bower.json".equals(name)) { // NOI18N
                        // set proper project name
                        writeFile(str, fo);
                        filterJson(fo, projectName);
                    } else {
                        writeFile(str, fo);
                    }
                }
            }
        }
    }

    private static void writeFile(ZipInputStream str, FileObject fo) throws IOException {
        try (OutputStream out = fo.getOutputStream()) {
            FileUtil.copy(str, out);
        }
    }

    private static void filterProjectXml(FileObject fo, ZipInputStream str, String name) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(str, baos);
            Document doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, false, null, null);
            NodeList nl = doc.getDocumentElement().getElementsByTagName("name"); // NOI18N
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    if (el.getParentNode() != null
                            && "data".equals(el.getParentNode().getNodeName())) { // NOI18N
                        NodeList nl2 = el.getChildNodes();
                        if (nl2.getLength() > 0) {
                            nl2.item(0).setNodeValue(name);
                        }
                        break;
                    }
                }
            }
            try (OutputStream out = fo.getOutputStream()) {
                XMLUtil.write(doc, out, StandardCharsets.UTF_8.name());
            }
        } catch (IOException | SAXException | DOMException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            writeFile(str, fo);
        }
    }

    private static void filterJson(FileObject fo, String name) throws IOException {
        Path path = FileUtil.toFile(fo).toPath();
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replace("${project.name}", JSONValue.escape(name)); // NOI18N
        Files.write(path, content.getBytes(charset));
    }

}
