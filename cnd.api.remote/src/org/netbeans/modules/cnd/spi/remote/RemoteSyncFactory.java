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

package org.netbeans.modules.cnd.spi.remote;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * A factory for RemoteSyncWorker
 */
public abstract class RemoteSyncFactory {

    /**
     * Creates an instance of RemoteSyncWorker.
     *
     * @param files local directories and files that should be synchronized
     *
     * @param executionEnvironment
     *
     * @param out output stream:
     * in the case implementation uses an external program (rsync? scp?),
     * it can redirect its stdout here
     *
     * @param err error stream:
     * in the case implementation uses an external program (rsync? scp?),
     * it can redirect its stderr here
     *
     * @param privProjectStorageDir a directory to store misc. cache-ing information;
     * it is caller's responsibility top guarantee that different local dirs
     * has different privProjectStorage associated
     * (usually it is "nbprohect/private" :-))
     *
     * @return new instance of the RemoteSyncWorker
     */
    public abstract RemoteSyncWorker createNew(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            FileObject privProjectStorageDir, String workingDir, List<FSPath> files, List<FSPath> buildResults);
    
    @Deprecated
    public RemoteSyncWorker createNew(ExecutionEnvironment executionEnvironment, 
            PrintWriter out, PrintWriter err, FileObject privProjectStorageDir, String workingDir, 
            FSPath... files) {
        return createNew(executionEnvironment, out, err, privProjectStorageDir, workingDir,                
                Arrays.asList(files), Collections.<FSPath>emptyList());
    }

    /**
     * Creates an instance of RemoteSyncWorker.
     *
     * @param project determines executionEnvironment and dirs to sync
     *
     * @param out output stream:
     * in the case implementation uses an external program (rsync? scp?),
     * it can redirect its stdout here
     *
     * @param err error stream:
     * in the case implementation uses an external program (rsync? scp?),
     * it can redirect its stderr here
     *
     * @return new instance of the RemoteSyncWorker
     */
    public abstract RemoteSyncWorker createNew(Lookup.Provider project,
            PrintWriter out, PrintWriter err);

    /**
     * Determines whether this factory is applicable for the given execution environment
     * @param execEnv execution environment
     * @return true in the case this factory is applicable, otherwise false
     */
    public abstract boolean isApplicable(ExecutionEnvironment execEnv);

    /**
     * Returns a name of this factory to display in the UI
     * @return a name of this factory to be displayed in the UI
     */
    public abstract String getDisplayName();

    /**
     * Returns a brief description of this factory to be used for tool tips, etc
     * @return a brief description of this factory
     */
    public abstract String getDescription();

    /**
     * Returns a unique string that identifies this factory
     * among the others. It can be stored in preferences,
     * and be restored later via fromID()
     * @return A unique string that identifies this factory
     */
    public abstract String getID();
    
    /**
     * Determines whether files are copies to remote host 
     * (as in FTP, AutoCopy) or not (as in Shared and FullRemote)
     * @return true i
     */
    public abstract boolean isCopying();

    /**
     * Gets a factory by its ID. See comments to getID() method.
     * @param id
     * @return
     */
    public static RemoteSyncFactory fromID(String id) {
        assert id != null;
        for (RemoteSyncFactory factory : getFactories()) {
            if (id.equals(factory.getID())) {
                return factory;
            }
        }
        Logger log = Logger.getLogger("org.netbeans.modules.cnd.spi.remote"); // NOI18N
        log.log(Level.SEVERE, "No RemoteSyncFactory found by with ID {0}", id); //NOI18N
        return null;
    }

    /**
     * Gets all available factories.
     * That's just a shortcut for the standard Lookup calls.
     * @return
     */
    public static RemoteSyncFactory[] getFactories() {
        final Collection<? extends RemoteSyncFactory> instances = Lookup.getDefault().lookupAll(RemoteSyncFactory.class);
        List<RemoteSyncFactory> result = new ArrayList<RemoteSyncFactory>(instances);
        String defaultId = System.getProperty("cnd.remote.default.sync");
        if (defaultId != null) {
            for (int i = 0; i < result.size(); i++) {
                if (defaultId.equals(result.get(i).getID())) {
                    if (i > 0) {
                        RemoteSyncFactory oldFirst = result.get(0);
                        result.set(0, result.get(i));
                        result.set(i, oldFirst);
                    }
                    break;
                }
            }
        }
        return result.toArray(new RemoteSyncFactory[result.size()]);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }


    public static RemoteSyncFactory getDefault() {
        RemoteSyncFactory[] factories = getFactories();
        assert factories.length > 0;
        return factories[0];
    }

    public boolean isPathMappingCustomizable() {
        return false;
    }

    public abstract PathMap getPathMap(ExecutionEnvironment executionEnvironment);
}
