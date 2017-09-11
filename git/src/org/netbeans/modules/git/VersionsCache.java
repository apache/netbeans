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

package org.netbeans.modules.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.Utils;

/**
 *
 * @author ondra
 */
public class VersionsCache {

    private static VersionsCache instance;

    /** Creates a new instance of VersionsCache */
    private VersionsCache() {
    }

    public static synchronized VersionsCache getInstance() {
        if (instance == null) {
            instance = new VersionsCache();
        }
        return instance;
    }

    /**
     * Loads the file in specified revision.
     *
     * @return null if the file does not exist in given revision
     */
    public File getFileRevision(File base, String revision, ProgressMonitor pm) throws IOException {
        if("-1".equals(revision)) return null; // NOI18N

        File repository = Git.getInstance().getRepositoryRoot(base);
        if (GitUtils.CURRENT.equals(revision)) {
            return base.exists() ? base : null;
        } else {
            File tempFile = new File(Utils.getTempFolder(), "nb-git-" + base.getName()); //NOI18N
            tempFile.deleteOnExit();
            GitClient client = null;
            try {
                client = Git.getInstance().getClient(repository);
                boolean result;
                FileOutputStream fos = new FileOutputStream(tempFile);
                try {
                    if (GitUtils.INDEX.equals(revision)) {
                        result = client.catIndexEntry(base, 0, fos, pm);
                    } else {
                        result = client.catFile(base, revision, fos, pm);
                    }
                } finally {
                    fos.close();
                }
                if (!result) {
                    tempFile.delete();
                    tempFile = null;
                }
            } catch (java.io.FileNotFoundException ex) {
                tempFile.delete();
                tempFile = null;
            } catch (GitException.MissingObjectException ex) {
                tempFile.delete();
                tempFile = null;
            } catch (GitException ex) {
                throw new IOException(ex);
            } finally {
                if (client != null) {
                    client.release();
                }
            }
            return tempFile;
        }
    }
}
