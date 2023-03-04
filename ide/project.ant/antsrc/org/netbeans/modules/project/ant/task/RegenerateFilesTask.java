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

package org.netbeans.modules.project.ant.task;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;

// XXX should this task also do XML Schema validation of project.xml?

/**
 * Ant task to regenerate project metadata files (build scripts).
 * Currently semantics are identical to that of opening the project
 * in the IDE's GUI: i.e. both <code>build.xml</code> and <code>build-impl.xml</code>
 * will be regenerated if they are missing, or if they are out of date with
 * respect to either <code>project.xml</code> or the stylesheet (and are
 * not modified according to <code>genfiles.properties</code>).
 * @author Jesse Glick
 */
public final class RegenerateFilesTask extends Task {
    
    /** Standard constructor. */
    public RegenerateFilesTask() {}
    
    private File buildXsl;
    /**
     * Set the stylesheet to use for the main build script.
     * @param f a <code>build.xsl</code> file
     */
    public void setBuildXsl(File f) {
        // XXX could also support jar: URIs etc.
        buildXsl = f;
    }
    
    private File buildImplXsl;
    /**
     * Set the stylesheet to use for the automatic build script.
     * @param f a <code>build-impl.xsl</code> file
     */
    public void setBuildImplXsl(File f) {
        buildImplXsl = f;
    }
    
    private File projectDir;
    /**
     * Set the project directory to regenerate files in.
     * @param f the top directory of an Ant-based project
     */
    public void setProject(File f) {
        projectDir = f;
    }
    
    public @Override void execute() throws BuildException {
        if (projectDir == null) {
            throw new BuildException("Must set 'project' attr", getLocation());
        }
        // XXX later may provide more control here...
        if (buildXsl == null && buildImplXsl == null) {
            throw new BuildException("Must set either 'buildxsl' or 'buildimplxsl' attrs or both", getLocation());
        }
        try {
            // Might be running inside IDE, in which case already have a mount...
            FileObject projectFO = FileUtil.toFileObject(projectDir);
            if (projectFO == null) {
                // XXX for some reason including masterfs.jar in <taskdef> does not work. Why?
                // Possibly a bug in AntClassLoader.getResources(String)?
                LocalFileSystem lfs = new LocalFileSystem();
                lfs.setRootDirectory(projectDir);
                projectFO = lfs.getRoot();
                assert projectFO != null;
            }
            GeneratedFilesHelper h = new GeneratedFilesHelper(projectFO);
            if (buildXsl != null && h.refreshBuildScript(GeneratedFilesHelper.BUILD_XML_PATH, org.openide.util.BaseUtilities.toURI(buildXsl).toURL(), true)) {
                log("Regenerating " + new File(projectDir, GeneratedFilesHelper.BUILD_XML_PATH.replace('/', File.separatorChar)).getAbsolutePath());
            }
            if (buildImplXsl != null && h.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH, org.openide.util.BaseUtilities.toURI(buildImplXsl).toURL(), true)) {
                log("Regenerating " + new File(projectDir, GeneratedFilesHelper.BUILD_IMPL_XML_PATH.replace('/', File.separatorChar)).getAbsolutePath());
            }
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        } catch (PropertyVetoException e) {
            throw new BuildException(e, getLocation());
        }
    }
    
}
