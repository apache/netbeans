/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.manager.test;

import java.io.File;
import java.util.ArrayList;
import org.openide.modules.InstalledFileLocator;

/**
 *  InstalledFileLocator implementation that searches the NB install directory
 * (uses java.endorsed.dirs value from nbproject/project.properties)
 * @author quynguyen
 */
public class InstalledFileLocatorImpl extends InstalledFileLocator {

    private ArrayList<File> baseDirs;
    private File userDirConfigRoot;
    
    public InstalledFileLocatorImpl() {
        super();
        File endorsedDir = new File(System.getProperty("java.endorsed.dirs"));
        for (int i = 0; i < 5; i++) {
            endorsedDir = endorsedDir.getParentFile();
        }

        File installRoot = endorsedDir;
        File[] subdirs = installRoot.listFiles();
        baseDirs = new ArrayList<File>();

        for (int i = 0; subdirs != null && i < subdirs.length; i++) {
            if (subdirs[i].isDirectory()) {
                baseDirs.add(subdirs[i]);
            }
        }
    }

    @Override
    public File locate(String relativePath, String codeNameBase, boolean localized) {
        for (File baseDir : baseDirs) {
            File f = new File(baseDir, relativePath);
            if (f.exists()) {
                return f;
            }
        }

        return null;
    }
    
    public void setUserConfigRoot(File baseDir) {
        if (userDirConfigRoot != null) {
            baseDirs.remove(userDirConfigRoot);
            userDirConfigRoot = null;
        }
        
        if (baseDir != null) {
            baseDirs.add(baseDir);
            userDirConfigRoot = baseDir;
        }
    }
}
