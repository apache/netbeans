/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public final class Clusterize implements WizardDescriptor.ProgressInstantiatingIterator<Clusterize> {
    final File file;
    final Project project;
    final ClusterizeInfo modules;
    private WizardDescriptor.Panel[] panels;
    final WizardDescriptor wizardDescriptor;
    int index;

    private Clusterize(Project p, File f) {
        this.file = f;
        this.project = p;
        this.modules = new ClusterizeInfo("", null, f);
        this.modules.setDisplayName(f.getPath());
        this.wizardDescriptor = new WizardDescriptor(this, this);
    }

    public static boolean clusterize(Project p, File f) {
        return new Clusterize(p, f).perform();
    }

    private boolean perform() {
        wizardDescriptor.createNotificationLineSupport();
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(Clusterize.class, "LAB_ClusterizeWizard"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        return wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION;
    }

    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<Clusterize>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel<?>[]{
                new ClusterizeWizardPanel1(),
                new ClusterizeWizardPanel2(),
                new ClusterizeWizardPanel3()
            };
        }
        return panels;
    }

    String getStep(int i) {
        return getSteps()[i];
    }
    String[] getSteps() {
        return new String[] {
            NbBundle.getMessage(Clusterize.class, "LAB_ClusterizeNotValid"),
            NbBundle.getMessage(Clusterize.class, "LAB_ClusterizeChoose"),
            NbBundle.getMessage(Clusterize.class, "LAB_ClusterizeSummary"),
        };
    }

    void scanForJars() {
        try {
            scanForJars(modules);
        } catch (InterruptedException ex) {
            Logger.getLogger(Clusterize.class.getName()).log(Level.FINE, null, ex);
        }
    }


    private static boolean scanForJars(ClusterizeInfo folder)
    throws InterruptedException {
        File[] children = folder.jar.listFiles();
        folder.getChildren().remove(folder.getChildren().getNodes());
        if (children == null) {
            return false;
        }

        String pref = folder.path.length() == 0 ? "" : folder.path + '/';
        List<Node> arr = new ArrayList<Node>();
        for (File file : children) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (file.isDirectory()) {
                ClusterizeInfo subdir = new ClusterizeInfo(pref + file.getName(), null, file);
                if (scanForJars(subdir)) {
                    arr.add(subdir);
                }
                continue;
            }
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            ManifestManager mm = ManifestManager.getInstanceFromJAR(file);
            if (mm != null && mm.getCodeNameBase() != null) {
                arr.add(new ClusterizeInfo(pref + file.getName(), mm, file));
            }
        }
        folder.getChildren().add(arr.toArray(new Node[0]));
        return folder.getChildren().getNodesCount() > 0;
    }

    void generateConfigFiles() {
        Set<String> autoload = new HashSet<String>();
        Set<String> eager = new HashSet<String>();
        Set<String> enabled = new HashSet<String>();

        modules.categorize(autoload, eager, enabled);

        try {
            AntProjectCookie apc = AntScriptUtils.antProjectCookieFor(findBuildXml(project));
            AntTargetExecutor.Env execenv = new AntTargetExecutor.Env();
        //    execenv.setLogger(new NullOutputStream());
            Properties p = execenv.getProperties();
            toProperty(p, "include.autoload", autoload); // NOI18N
            toProperty(p, "include.enabled", enabled); // NOI18N
            toProperty(p, "include.eager", eager); // NOI18N
            p.setProperty("cluster", file.getPath()); // NOI18N
            execenv.setProperties(p);
            String[] targetNames = { "clusterize" }; // NOI18N
            ExecutorTask t = AntTargetExecutor.createTargetExecutor(execenv).execute(apc, targetNames);
            t.waitFinished();
        } catch (IOException e) {
            Util.err.notify(e);
        }
    }

    private void toProperty(Properties p, String property, Set<String> strings) {
        if (strings.size() == 0) {
            return;
        }
        String sep = ""; // NOI18N
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(sep);
            sb.append(s);
            sep = ","; // NOI18N
        }
        p.setProperty(property, sb.toString());
    }
    private static FileObject findBuildXml(Project project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        generateConfigFiles();
        handle.finish();
        return Collections.emptySet();
    }

    @Override
    public Set instantiate() throws IOException {
        assert false;
        generateConfigFiles();
        return Collections.emptySet();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public Panel<Clusterize> current() {
        return getPanels()[index];
    }

    @Override
    public String name() {
        return getStep(index);
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        index++;
    }

    @Override
    public void previousPanel() {
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
}
