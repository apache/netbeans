/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;

/**
 *
 * @author sdedic
 */
class SnippetFileSystem extends MultiFileSystem implements Callable<FileObject> {

    @Override
    protected FileSystem createWritableOn(String name) throws IOException {
        return super.createWritableOn(name); //To change body of generated methods, choose Tools | Templates.
    }
    private final FileObject  projectRoot;
    private final FileObject  configRoot;
    
    private final FileSystem  projectFileSystem;
    private final FileSystem  configFileSystem;
    /**
     * Path to the project root, will be prepended when using projectFS
     */
    private final String      projectRootPath;
    
    /**
     * Prefix within the project - snippet storage root
     */
    private final String      projectFSPrefix;
    
    private final String      configFSPathPrefix;

    public SnippetFileSystem(FileObject projectRoot, FileObject configRoot, String projectFSPathPrefix, String configFSPathPrefix) throws IOException {
        super(new FileSystem[] {
            projectRoot.getFileSystem(),
            configRoot.getFileSystem()
        });
        this.projectRoot = projectRoot;
        this.projectFileSystem = projectRoot.getFileSystem();
        this.configFileSystem = configRoot.getFileSystem();
        this.configRoot = configRoot;
        this.projectRootPath = projectRoot.getPath();
        this.projectFSPrefix = projectFSPathPrefix;
        this.configFSPathPrefix = configFSPathPrefix;
    }
    
    @Override
    protected FileObject findResourceOn(FileSystem fs, String res) {
        if (fs == projectFileSystem) {
            String append = projectRootPath + "/";
            if (!projectFSPrefix.isEmpty()) {
                append += projectFSPrefix + "/";
            }
            return projectFileSystem.findResource(append + res);
        } else {
            return configFileSystem.findResource(configFSPathPrefix + "/" + res);
        }
    }

    @Override
    public FileObject call() throws IOException {
        return FileUtil.createFolder(projectRoot, projectFSPrefix);
    }
    
    boolean isObsolete() {
        return !projectRoot.isValid();
    }
}
