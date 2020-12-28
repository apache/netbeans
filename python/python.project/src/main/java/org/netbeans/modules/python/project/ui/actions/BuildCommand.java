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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.project.ui.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.ui.Utils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Amit Saha <amitksaha@netbeans.org>
 */
public class BuildCommand extends Command {

    public BuildCommand(PythonProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ActionProvider.COMMAND_BUILD;
    }

    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
        final PythonProject pyProject = getProject();
        final PythonPlatform platform = checkProjectPythonPlatform(pyProject);

        // A 'setup.py' file is needed build a Python egg
        // If a 'setup.py' already exists in the source root, do not create a new
        // file, else create a bare minimal 'setup.py'
        // file for the Egg building process
        // the template file is defined in /org/netbeans/modules/python/editor
        ///templates/setup.py.ftl

        //Find the source root(s) directory in  which all the sources live
       FileObject[] roots = pyProject.getSourceRoots().getRoots();

        for (FileObject root : roots) {
            System.out.println("Src Folder:  " + root.getPath());
        }



        if (findSetupFile(pyProject) == null) {
            try {

                // XXX creates the 'setup.py' file in the first source root: roots[0]
                createSetupFile(Repository.getDefault().getDefaultFileSystem().findResource("Templates/Python/_setup.py"), roots[0], "setup.py");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }



        if (platform == null) {
            return; // invalid platform user has been warn in check so safe to return
        }

        if (getProperties().getMainModule() == null ||
                getProperties().getMainModule().equals("")) {
            String main = Utils.chooseMainModule(getProject().getSourceRoots().getRoots());
            getProperties().setMainModule(main);
            getProperties().save();
        }


        // Obtain the FileObject of the 'setup.py' file
        FileObject script=null;
        for (FileObject root : roots) {
            script = root.getFileObject("setup", "py");
        }

        assert script != null; //check

        // This code is borrowed from the other '*Command.java' classes

        PythonExecution pyexec = new PythonExecution();
        pyexec.setDisplayName(ProjectUtils.getInformation(pyProject).getDisplayName());

        //Set working directory.
        FileObject path = script.getParent();

        //System.out.println("Working directory" + path);

        pyexec.setWorkingDirectory(path.toString());
        pyexec.setCommand(platform.getInterpreterCommand());

        //Set python script
        pyexec.setScript(FileUtil.toFile(script).getAbsolutePath());
        pyexec.setCommandArgs(platform.getInterpreterArgs());
        pyexec.setScriptArgs("bdist_egg"); //build the binary Egg

        //build path & set
        pyexec.setPath(PythonPlatform.buildPath(super.buildPythonPath(platform, pyProject)));
        pyexec.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform, pyProject)));
        pyexec.setShowControls(true);
        pyexec.setShowInput(true);
        pyexec.setShowWindow(true);
        pyexec.addStandardRecognizers();

        //System.out.println("Executing::" + pyexec.getScript() + " with::" + pyexec.getScriptArgs());
        pyexec.run();
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        return true;
    }

    protected static FileObject findSetupFile(final PythonProject pyProject) {
        final FileObject[] roots = pyProject.getSourceRoots().getRoots();
        final String setupFile = "setup.py";
        if (setupFile == null) {
            return null;
        }
        FileObject fo = null;
        for (FileObject root : roots) {
            fo = root.getFileObject(setupFile);
            if (fo != null) {
                break;
            }
        }
        return fo;
    }

    private void createSetupFile(FileObject template, FileObject parent, String filename) throws IOException {
        try {
            DataFolder dataFolder = DataFolder.findFolder(parent);
            DataObject dataTemplate = DataObject.find(template);
            //Strip extension when needed
            int index = filename.lastIndexOf('.');
            if (index > 0 && index < filename.length() - 1 && "py".equalsIgnoreCase(filename.substring(index + 1))) {
                filename = filename.substring(0, index);
            }

            //create the map of objects to be 'fed' to FTL
            Map ftl_objects = new HashMap();
            ftl_objects.put("project_name", getProject().getName());


            dataTemplate.createFromTemplate(dataFolder, filename, ftl_objects);


        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
