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
package org.netbeans.modules.remotefs.versioning.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FileObjectIndexingBridgeProvider {
    private static final Logger LOG = Logger.getLogger(FileObjectIndexingBridgeProvider.class.getName());
    private static FileObjectIndexingBridgeProvider instance;
    
    /**
     * Gets the singleton instance of <code>FileObjectIndexingBridgeProvider</code>.
     *
     * @return The <code>FileObjectIndexingBridgeProvider</code> instance.
     */
    public static synchronized FileObjectIndexingBridgeProvider getInstance() {
        if (instance == null) {
            instance = new FileObjectIndexingBridgeProvider();
        }
        return instance;
    }
    
    private FileObjectIndexingBridgeProvider() {
    }
    
    /**
     * Runs the <code>operation</code> without interfering with indexing. The indexing
     * is blocked while the operation is running and all events that would normally trigger
     * reindexing will be processed after the operation is finished. <br>
     * The filesystem will be asynchronously refreshed for all parent folders from the given files
     * after the operation has finished
     *
     * @param operation The operation to run.
     * @param files Files or folders affected by the operation.
     * @return Whatever value is returned from the <code>operation</code>.
     *
     * @throws Exception Any exception thrown by the <code>operation</code> is going to be rethrown from
     *   this method.
     */
    public <T> T runWithoutIndexing(final Callable<T> operation, VCSFileProxy ... files) throws Exception {
        if(LOG.isLoggable(Level.FINE)) {
            StringBuffer sb = new StringBuffer();
            if(files != null) {
                for (VCSFileProxy file : files) {
                    sb.append("\n"); // NOI18N
                    sb.append(file.getPath());
                }
            }
            LOG.fine("running vcs operaton without scheduling for files:" + sb.toString()); // NOI18N
        }
        final List<FileObject> fos = new ArrayList<>();
        for (VCSFileProxy f : files) {
            FileObject fo = f.normalizeFile().toFileObject();
            if (fo != null) {
                fos.add(fo);
            }
        }
        return IndexingManager.getDefault().runProtected(new Callable<T>() {
            @Override
            public T call() throws Exception {
                // Schedule the refresh task, which will then absorb all other tasks generated
                // by filesystem events caused by the operation
                IndexingManager.getDefault().refreshAllIndices(false, false, fos.toArray(new FileObject[fos.size()]));
                return operation.call();
            }
        });
    }


    /**
     * Determines if projects are being indexed or not.
     *
     * @return <code>true</code> if projects are being scanned, <code>false</code> otherwise.
     * @since 1.7
     */
    public boolean isIndexingInProgress() {
        return IndexingManager.getDefault().isIndexing();
    }
}
