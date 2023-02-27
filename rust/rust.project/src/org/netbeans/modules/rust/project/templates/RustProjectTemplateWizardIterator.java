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
package org.netbeans.modules.rust.project.templates;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Project/Rust",
        displayName = "#RustProjectTemplate_displayName",
        description = "RustProjectTemplateDescription.html",
        iconBase = RustProjectAPI.ICON,
        position = 1000)
@Messages("RustProjectTemplate_displayName=New Rust project")
public class RustProjectTemplateWizardIterator implements WizardDescriptor./*Progress*/InstantiatingIterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    public RustProjectTemplateWizardIterator() {
    }

    public static RustProjectTemplateWizardIterator createIterator() {
        return new RustProjectTemplateWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[]{
            new RustProjectTemplateWizardPanel(),};
    }

    private String[] createSteps() {
        return new String[]{
            NbBundle.getMessage(RustProjectTemplateWizardIterator.class, "LBL_CreateProjectStep")
        };
    }

    public Set/*<FileObject>*/ instantiate(/*ProgressHandle handle*/) throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();

        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        String name = (String) wiz.getProperty("name");
        Boolean isLibrary = (Boolean) wiz.getProperty("is-rust-library");
        isLibrary = isLibrary == null ? false : isLibrary;

        // Create project dirctory
        dirF.mkdirs();
        FileObject dir = FileUtil.toFileObject(dirF);

        // Write Cargo.toml
        File cargoTOML = new File(dirF, "Cargo.toml");
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(cargoTOML), StandardCharsets.UTF_8))) {
            writer.println("[package]");
            writer.format("name = \"%s\"%n", name);
            writer.println("version = \"0.1.0\"");
            writer.println("edition= \"2021\"");
            writer.println();
            writer.println("# See more keys and their definitions at https://doc.rust-lang.org/cargo/reference/manifest.html");
            writer.println();
            writer.println("[dependencies]");
        }

        // Create "src" folder
        File src = new File(dirF, "src");
        src.mkdirs();

        // If this is a library create a "lib.rs" otherwise a "main.rs"
        if (isLibrary) {
            File librs = new File(src, "lib.rs");
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(librs), StandardCharsets.UTF_8))) {

                writer.println(
                        "pub fn add(left: usize, right: usize) -> usize {\n"
                        + "    left + right\n"
                        + "}");
            }
        } else {
            File mainrs = new File(src, "main.rs");
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(mainrs), StandardCharsets.UTF_8))) {
                writer.println(
                        "fn main() {\n"
                        + "    println!(\"Hello, world!\");\n"
                        + "}");
            }
        }

        // Create a .gitignore
        {
            File gitignore = new File(dirF, ".gitignore");
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(gitignore), StandardCharsets.UTF_8))) {
                writer.println(
                        "# Generated by NetBeans\n"
                        + "# will have compiled files and executables\n"
                        + "debug/\n"
                        + "target/\n"
                        + "\n"
                        + "# Remove Cargo.lock from gitignore if creating an executable, leave it for libraries\n"
                        + "# More information here https://doc.rust-lang.org/cargo/guide/cargo-toml-vs-cargo-lock.html\n"
                        + "Cargo.lock\n"
                        + "\n"
                        + "# These are backup files generated by rustfmt\n"
                        + "**/*.rs.bk\n"
                        + "\n"
                        + "# MSVC Windows builds of rustc generate these, which store debugging information\n"
                        + "*.pdb"
                );
            }
        }

        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration<? extends FileObject> e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = e.nextElement();
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
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null);
        this.wiz.putProperty("name", null);
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{new Integer(index + 1), new Integer(panels.length)});
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

}
