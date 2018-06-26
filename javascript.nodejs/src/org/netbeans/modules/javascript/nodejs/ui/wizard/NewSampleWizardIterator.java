/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
