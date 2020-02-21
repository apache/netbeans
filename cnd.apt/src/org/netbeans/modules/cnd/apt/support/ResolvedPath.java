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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.apt.support;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.CharSequences;

/**
 *
 */
public final class ResolvedPath {
    private final CharSequence folder;
    private final FileSystem fileSystem;
    private final CharSequence path;
    private final boolean isDefaultSearchPath;
    private final int index;
    
    public ResolvedPath(FileSystem fileSystem, CharSequence folder, CharSequence path, boolean isDefaultSearchPath, int index) {
        assert CharSequences.isCompact(folder) : "forgot to FilePathCache.getManager().getString(folder)? " + folder;
        this.folder = folder;// should be already shared
        this.fileSystem = fileSystem;
        CndPathUtilities.assertNoUrl(path);
        this.path = FilePathCache.getManager().getString(path);
        this.isDefaultSearchPath = isDefaultSearchPath;
        this.index = index;
        boolean debug = false;
        assert debug = true;
        if (debug) {
            if (!CndFileUtils.isExistingFile(fileSystem, this.path.toString())) {
                APTUtils.LOG.log(Level.WARNING, "ResolvedPath: isExistingFile failed in {0} for {1}", new Object[]{fileSystem, path});
            }
            // there are situations when file is edited, but included file is 
            // removed/created by running undeground build infrastructure,
            // so resolved path can correspond to the file which is already not a file
            if (CndFileUtils.isLocalFileSystem(fileSystem)) {
                // check file existence using java.io.file as well
                if (!new File(this.path.toString()).isFile()) {
                    APTUtils.LOG.log(Level.WARNING, "ResolvedPath: isFile failed for {0}", path);
                }
            }
            if (CndFileUtils.toFileObject(fileSystem, path) == null) {
                APTUtils.LOG.log(Level.WARNING, "ResolvedPath: no FileObject in {0} for {1} FileUtil.toFileObject = {2} second check = {3}", 
                        new Object[]{
                            fileSystem, path, 
                            FileUtil.toFileObject(new File(FileUtil.normalizePath(path.toString()))), 
                            fileSystem.findResource(path.toString())});
            }
        }
        CndUtils.assertNormalized(fileSystem, folder);
        CndUtils.assertNormalized(fileSystem, path);
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public FileObject getFileObject() {
        // using fileSystem.findResource is not safe, see #196425 -  AssertionError: no FileObject 
        return CndFileUtils.toFileObject(fileSystem, path);
    }
    
    /**
     * Resolved file path (normalized version)
     */
    public CharSequence getPath(){
        return path;
    }

    /**
     * Include path used for resolving file path
     */
    public CharSequence getFolder(){
        return folder;
    }

    /**
     * Returns true if the header is resolved against owner file directory
     */
    public boolean isDefaultSearchPath(){
        return isDefaultSearchPath;
    }

    /**
     * Returns index of resolved path in user and system include paths
     */
    public int getIndex(){
        return index;
    }
    
    @Override
    public String toString(){
        return "ResPath{" + path + " in " + folder + (CndFileUtils.isLocalFileSystem(fileSystem) ? "" : fileSystem) + "}"; // NOI18N
    }
}
