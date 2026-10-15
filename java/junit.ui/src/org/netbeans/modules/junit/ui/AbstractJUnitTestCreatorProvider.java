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
package org.netbeans.modules.junit.ui;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import static org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider.getSourceGroup;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils;
import org.netbeans.modules.junit.api.JUnitVersion;
import org.netbeans.modules.junit.api.JUnitTestUtil;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

abstract class AbstractJUnitTestCreatorProvider extends TestCreatorProvider {
    abstract JUnitVersion useVersion();

    @Override
    public boolean enable(FileObject[] activatedFOs) {
        if (activatedFOs == null || activatedFOs.length == 0) {
            return false;
        }
        /*
         * In most cases, there is just one node selected - that is why
         * this case is handled in a special, more effective way
         * (no collections and iterators created).
         */
        if (activatedFOs.length == 1) {
            FileObject fileObj = activatedFOs[0];

            Project project;
            if ((fileObj != null)
                && fileObj.isValid()
                // selected FO might be folder or java file
                && (fileObj.isFolder() || JUnitTestUtil.isJavaFile(fileObj))
                && ((project = FileOwnerQuery.getOwner(fileObj)) != null)
                && (getSourceGroup(fileObj, project) != null)) {

                JUnitPlugin plugin = JUnitTestUtil.getPluginForProject(project, useVersion());
                return JUnitTestUtil.canCreateTests(plugin, fileObj);
            } else {
                return false;
            }
        }

        final Collection<FileObject> fileObjs = new ArrayList<>(activatedFOs.length);
        Project theProject = null;
        boolean result = false;
        for (FileObject fileObj : activatedFOs) {
            if ((fileObj == null) || !fileObj.isValid()
                // selected FO might be folder or java file
                && (fileObj.isFolder() || JUnitTestUtil.isJavaFile(fileObj))) {
                continue;
            }

            fileObjs.add(fileObj);

            Project prj = FileOwnerQuery.getOwner(fileObj);
            if (prj != null) {
                if (theProject == null) {
                    theProject = prj;
                }
                if (prj != theProject) {
                    return false;        /* files from different projects */

                }

                if (getSourceGroup(fileObj, prj) != null) {
                    result = true;
                }
            }
        }

        if (theProject != null) {
            JUnitPlugin plugin = JUnitTestUtil.getPluginForProject(theProject);
            result &= JUnitTestUtil.canCreateTests(plugin,
                            fileObjs.toArray(new FileObject[0]));
        }

        return result;
    }

    @Override
    public void createTests(Context context) {
        String problem;
        if ((problem = checkNodesValidity(context.getActivatedFOs())) != null) {
            // TODO report problem
            NotifyDescriptor msg = new NotifyDescriptor.Message(
                    problem, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }

        final FileObject[] filesToTest = context.getActivatedFOs();
        if (filesToTest == null) {
            return;     //XXX: display some message
        }

        /*
         * Determine the plugin to be used:
         */
        final JUnitPlugin plugin = JUnitTestUtil.getPluginForProject(
            FileOwnerQuery.getOwner(filesToTest[0]), useVersion()
        );

        if (!JUnitTestUtil.createTestActionCalled(
                plugin, filesToTest)) {
            return;
        }

        /*
         * Store the configuration data:
         */
        final boolean singleClass = context.isSingleClass();
        final Map<CommonPlugin.CreateTestParam, Object> params = JUnitTestUtil.getSettingsMap(!singleClass);
        if (singleClass) {
            params.put(CommonPlugin.CreateTestParam.CLASS_NAME, context.getTestClassName());
        }
        final FileObject targetFolder = context.getTargetFolder();
        if(context.isIntegrationTests()) {
            params.put(CommonPlugin.CreateTestParam.INC_GENERATE_INTEGRATION_TEST, Boolean.TRUE);
        } else {
            params.put(CommonPlugin.CreateTestParam.INC_GENERATE_INTEGRATION_TEST, Boolean.FALSE);
        }

        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                /*
                 * Now create the tests:
                 */
                final FileObject[] testFileObjects = JUnitTestUtil.createTests(
                        plugin,
                        filesToTest,
                        targetFolder,
                        params);

                /*
                 * Open the created/updated test class if appropriate:
                 */
                if (testFileObjects.length == 1) {
                    try {
                        DataObject dobj = DataObject.find(testFileObjects[0]);
                        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                        if (ec != null) {
                            EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    ec.open();
                                }
                            });
                        }
                    } catch (DataObjectNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Checks that the selection of nodes the dialog is invoked on is valid.
     * @return String message describing the problem found or null, if the
     *         selection is ok
     */
    private static String checkNodesValidity(FileObject[] files) {

        Project project = getProject(files);
        if (project == null) {
            return NbBundle.getMessage(JUnitTestCreatorProvider.class,
                                       "MSG_multiproject_selection");   //NOI18N
        }

        if (!checkPackages(files)) {
            return NbBundle.getMessage(JUnitTestCreatorProvider.class,
                                       "MSG_invalid_packages");         //NOI18N
        }

        return null;
    }

    /**
     * Extracts {@code FileObject}s from the given nodes.
     * Nodes that have (direct or indirect) parent nodes among the given
     * nodes are ignored.
     *
     * @return  a non-empty array of {@code FileObject}s
     *          represented by the given nodes;
     *          or {@code null} if no {@code FileObject} was found;
     */
    private static FileObject[] getFileObjectsFromNodes(final Node[] nodes){
        FileObject[] fileObjects = new FileObject[nodes.length];
        List<FileObject> fileObjectsList = null;

        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            final FileObject fo;
            if (!hasParentAmongNodes(nodes, i)
                    && ((fo = getTestFileObject(node)) != null)) {
                if (fileObjects != null) {
                    fileObjects[i] = fo;
                } else {
                    if (fileObjectsList == null) {
                        fileObjectsList = new ArrayList<FileObject>(
                                                        nodes.length - i);
                    }
                    fileObjectsList.add(fo);
                }
            } else {
                fileObjects = null;     //signs that some FOs were skipped
            }
        }
        if (fileObjects == null) {
            if (fileObjectsList != null) {
                fileObjects = fileObjectsList.toArray(new FileObject[0]);
                fileObjectsList = null;
            }
        }

        return fileObjects;
    }

    /**
     * Check that all the files (folders or java files) have correct java
     * package names.
     * @return true if all are fine
     */
    private static boolean checkPackages(FileObject[] files) {
        if (files.length == 0) {
            return true;
        } else {
            Project project = FileOwnerQuery.getOwner(files[0]);
            for (int i = 0 ; i < files.length; i++) {
                String packageName = getPackage(project, files[i]);
                if ((packageName == null)
                        || !JUnitTestUtil.isValidPackageName(packageName)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Get the package name of <code>file</code>.
     *
     * @param project owner of the file (for performance reasons)
     * @param file the FileObject whose packagename to get
     * @return package name of the file or null if it cannot be retrieved
     */
    private static String getPackage(Project project, FileObject file) {
        SourceGroup srcGrp = JUnitTestUtil.findSourceGroupOwner(project, file);
        if (srcGrp!= null) {
            ClassPath cp = ClassPathSupport.createClassPath(
                    new FileObject [] {srcGrp.getRootFolder()});
            return cp.getResourceName(file, '.', false);
        } else {
            return null;
        }
    }


    private static FileObject[] getFiles(Node[] nodes) {
        FileObject[] ret = new FileObject[nodes.length];
        for (int i = 0 ; i < nodes.length ; i++) {
            ret[i]  = UICommonUtils.getFileObjectFromNode(nodes[i]);
        }
        return ret;
    }

    /**
     * Get the single project for <code>nodes</code> if there is such.
     * If the nodes belong to different projects or some of the nodes doesn't
     * have a project, return null.
     */
    private static Project getProject(FileObject[] files) {
        Project project = null;
        for (int i = 0 ; i < files.length; i++) {
            Project nodeProject = FileOwnerQuery.getOwner(files[i]);
            if (project == null) {
                project = nodeProject;
            } else if (project != nodeProject) {
                return null;
            }
        }
        return project;
    }

    /**
     * Grabs and checks a <code>FileObject</code> from the given node.
     * If either the file could not be grabbed or the file does not pertain
     * to any project, a message is displayed.
     *
     * @param  node  node to get a <code>FileObject</code> from.
     * @return  the grabbed <code>FileObject</code>,
     *          or <code>null</code> in case of failure
     */
    private static FileObject getTestFileObject(final Node node) {
        final FileObject fo = UICommonUtils.getFileObjectFromNode(node);
        if (fo == null) {
            JUnitTestUtil.notifyUser(NbBundle.getMessage(
                    JUnitTestCreatorProvider.class,
                    "MSG_file_from_node_failed"));                      //NOI18N
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            JUnitTestUtil.notifyUser(NbBundle.getMessage(
                    JUnitTestCreatorProvider.class,
                    "MSG_no_project",                                   //NOI18N
                    fo));
            return null;
        }
        return fo;
    }

    private static boolean hasParentAmongNodes(final Node[] nodes,
                                               final int idx) {
        Node node;

        node = nodes[idx].getParentNode();
        while (null != node) {
            for (int i = 0; i < nodes.length; i++) {
                if (i == idx) {
                    continue;
                }
                if (node == nodes[i]) {
                    return true;
                }
            }
            node = node.getParentNode();
        }
        return false;
    }


}
