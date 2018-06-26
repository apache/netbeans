/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.build;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class AdvancedTasksStorage {

    private static final Logger LOGGER = Logger.getLogger(AdvancedTasksStorage.class.getName());

    private static final String NAMESPACE_URI = "http://www.netbeans.org/ns/build-tool-tasks/1"; // NOI18N
    private static final String ELEMENT_TASKS = "%s-tasks"; // NOI18N
    private static final String ELEMENT_WORKDIR = "workdir"; // NOI18N
    private static final String ATTR_WORKDIR_PATH = "path"; // NOI18N
    private static final String ATTR_WORKDIR_SHOW_SIMPLE_TASKS = "showSimpleTasks"; // NOI18N
    private static final String ELEMENT_TASK = "task"; // NOI18N
    private static final String ATTR_TASK_NAME = "name"; // NOI18N
    private static final String ATTR_TASK_OPTIONS = "options"; // NOI18N
    private static final String ATTR_TASK_TASKS = "tasks"; // NOI18N
    private static final String ATTR_TASK_PARAMETERS = "parameters"; // NOI18N
    private static final String ATTR_TASK_INDEX = "index"; // NOI18N

    private final Project project;
    private final String ident;
    private final String workDir;


    private AdvancedTasksStorage(Project project, String ident, String workDir) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notEmpty("ident", ident); // NOI18N
        Parameters.notNull("workDir", workDir); // NOI18N
        this.project = project;
        this.ident = ident.toLowerCase().replace(' ', '-'); // NOI18N
        this.workDir = workDir;
    }

    //~ Factories

    public static AdvancedTasksStorage forBuildToolSupport(BuildTools.BuildToolSupport support) {
        Parameters.notNull("support", support); // NOI18N
        String workDirPath;
        FileObject workDir = support.getWorkDir();
        String relativePath = FileUtil.getRelativePath(support.getProject().getProjectDirectory(), workDir);
        if (relativePath != null) {
            workDirPath = relativePath;
        } else {
            workDirPath = FileUtil.getFileDisplayName(workDir);
        }
        return new AdvancedTasksStorage(support.getProject(), support.getIdentifier(), workDirPath);
    }

    public Data loadTasks() {
        assert !EventQueue.isDispatchThread();
        Map<Integer, AdvancedTask> tasks = new TreeMap<>();
        AtomicBoolean showSimpleTasks = new AtomicBoolean(true);
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
        loadTasks(config, true, tasks, showSimpleTasks);
        boolean sharedShowSimpleTasks = showSimpleTasks.get();
        loadTasks(config, false, tasks, showSimpleTasks);
        boolean privateShowSimpleTasks = showSimpleTasks.get();
        assert sharedShowSimpleTasks == privateShowSimpleTasks : sharedShowSimpleTasks + " != " + privateShowSimpleTasks;
        return new Data()
                .setTasks(new ArrayList<>(tasks.values()))
                .setShowSimpleTasks(showSimpleTasks.get());
    }

    public void storeTasks(Data data) throws IOException {
        assert !EventQueue.isDispatchThread();
        assert data != null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            storeTasks(data, config, true, builder);
            storeTasks(data, config, false, builder);
            ProjectManager.getDefault().saveProject(project);
        } catch (ParserConfigurationException pcex) {
            LOGGER.log(Level.SEVERE, "Unable to store tasks!", pcex);
        }
    }

    private void loadTasks(AuxiliaryConfiguration config, boolean shared, Map<Integer, AdvancedTask> tasks, AtomicBoolean showSimpleTasks) {
        Element tasksElement = config.getConfigurationFragment(getTasksElement(), NAMESPACE_URI, shared);
        if (tasksElement == null) {
            return;
        }
        NodeList workDirList = tasksElement.getElementsByTagName(ELEMENT_WORKDIR);
        for (int i = 0; i < workDirList.getLength(); i++) {
            Element workDirElement = (Element) workDirList.item(i);
            if (workDir.equals(workDirElement.getAttribute(ATTR_WORKDIR_PATH))) {
                showSimpleTasks.set(Boolean.parseBoolean(workDirElement.getAttribute(ATTR_WORKDIR_SHOW_SIMPLE_TASKS)));
                NodeList taskList = workDirElement.getElementsByTagName(ELEMENT_TASK);
                for (int j = 0; j < taskList.getLength(); j++) {
                    Element taskElement = (Element) taskList.item(j);
                    AdvancedTask original = tasks.put(Integer.parseInt(taskElement.getAttribute(ATTR_TASK_INDEX)), new AdvancedTask()
                            .setName(taskElement.getAttribute(ATTR_TASK_NAME))
                            .setOptions(taskElement.getAttribute(ATTR_TASK_OPTIONS))
                            .setTasks(taskElement.getAttribute(ATTR_TASK_TASKS))
                            .setParameters(taskElement.getAttribute(ATTR_TASK_PARAMETERS))
                            .setShared(shared));
                    assert original == null : "Task already exists: " + original;
                }
                break;
            }
        }
    }

    private void storeTasks(Data data, AuxiliaryConfiguration config, boolean shared, DocumentBuilder builder) {
        Element tasksElement = config.getConfigurationFragment(getTasksElement(), NAMESPACE_URI, shared);
        if (tasksElement == null) {
            insertTasks(data, config, shared, builder);
            return;
        }
        NodeList workDirList = tasksElement.getElementsByTagName(ELEMENT_WORKDIR);
        Element existingWorkDirElement = null;
        for (int i = 0; i < workDirList.getLength(); i++) {
            Element workDirElement = (Element) workDirList.item(i);
            if (workDir.equals(workDirElement.getAttribute(ATTR_WORKDIR_PATH))) {
                existingWorkDirElement = workDirElement;
                break;
            }
        }
        if (existingWorkDirElement != null) {
            tasksElement.removeChild(existingWorkDirElement);
        }
        insertWorkDir(data, config, shared, builder, tasksElement.getOwnerDocument(), tasksElement);
    }

    private void insertTasks(Data data, AuxiliaryConfiguration config, boolean shared, DocumentBuilder builder) {
        Document document = builder.newDocument();
        Element tasksElement = document.createElementNS(NAMESPACE_URI, getTasksElement());
        insertWorkDir(data, config, shared, builder, document, tasksElement);
    }

    private void insertWorkDir(Data data, AuxiliaryConfiguration config, boolean shared, DocumentBuilder builder,
            Document document, Element tasksElement) {
        // workdir
        Element workDirElement = document.createElementNS(NAMESPACE_URI, ELEMENT_WORKDIR);
        workDirElement.setAttribute(ATTR_WORKDIR_PATH, workDir);
        workDirElement.setAttribute(ATTR_WORKDIR_SHOW_SIMPLE_TASKS, Boolean.toString(data.isShowSimpleTasks()));
        tasksElement.appendChild(workDirElement);
        // tasks
        List<AdvancedTask> tasks = data.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            AdvancedTask task = tasks.get(i);
            if (task.isShared() != shared) {
                continue;
            }
            Element taskElement = document.createElementNS(NAMESPACE_URI, ELEMENT_TASK);
            taskElement.setAttribute(ATTR_TASK_NAME, task.getName());
            taskElement.setAttribute(ATTR_TASK_OPTIONS, task.getOptions());
            taskElement.setAttribute(ATTR_TASK_TASKS, task.getTasks());
            taskElement.setAttribute(ATTR_TASK_PARAMETERS, task.getParameters());
            taskElement.setAttribute(ATTR_TASK_INDEX, String.valueOf(i));
            workDirElement.appendChild(taskElement);
        }
        config.putConfigurationFragment(tasksElement, shared);
    }

    private String getTasksElement() {
        return String.format(ELEMENT_TASKS, ident);
    }

    //~ Inner classes

    public static final class Data {

        private List<AdvancedTask> tasks;
        private boolean showSimpleTasks;


        public List<AdvancedTask> getTasks() {
            return Collections.unmodifiableList(tasks);
        }

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public Data setTasks(List<AdvancedTask> tasks) {
            assert tasks != null;
            this.tasks = tasks;
            return this;
        }

        public boolean isShowSimpleTasks() {
            return showSimpleTasks;
        }

        public Data setShowSimpleTasks(boolean showSimpleTasks) {
            this.showSimpleTasks = showSimpleTasks;
            return this;
        }

        @Override
        public String toString() {
            return "Data{" + "tasks=" + tasks + ", showSimpleTasks=" + showSimpleTasks + '}'; // NOI18N
        }

    }

}
