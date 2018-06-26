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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.classpath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Helper class for sharing the same code between {@link org.netbeans.modules.php.project.api.PhpSourcePath}
 * and {@link ClassPathProviderImpl}.
 * @author Tomas Mysik
 */
public final class CommonPhpSourcePath {

    private static final Logger LOGGER = Logger.getLogger(CommonPhpSourcePath.class.getName());

    // GuardedBy(CommonPhpSourcePath.class)
    private static List<FileObject> internalFolders = null;

    private CommonPhpSourcePath() {
    }

    public static synchronized List<FileObject> getInternalPath() {
        if (internalFolders == null) {
            internalFolders = getInternalFolders();
        }
        return internalFolders;
    }

    private static List<FileObject> getInternalFolders() {
        assert Thread.holdsLock(CommonPhpSourcePath.class);

        List<FileObject> preindexedFolders = PhpSourcePath.getPreindexedFolders();
        // XXX disabled, unit tests failures
        //assert !preindexedFolders.contains(null) : "Preindexed folders contains null";
        FileObject sfsFolder = FileUtil.getConfigFile("PHP/RuntimeLibraries"); // NOI18N
        List<FileObject> folders = new ArrayList<>(preindexedFolders.size() + 1);
        // #210578
        if (sfsFolder != null) {
            folders.add(sfsFolder);
        } else {
            LOGGER.info("SFS folder PHP/RuntimeLibraries not found");
        }
        folders.addAll(preindexedFolders);
        return folders;
    }
}
