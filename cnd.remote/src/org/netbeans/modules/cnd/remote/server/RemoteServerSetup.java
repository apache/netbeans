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

package org.netbeans.modules.cnd.remote.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.SetupProvider;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * NB: the class is not thread safe!
 */
public class RemoteServerSetup {

    private static class BinarySetupMapEntry {
        public final File localFile;
        public final String remotePath;
        public final SetupProvider setupProvider;
        public BinarySetupMapEntry(File file, String remotePath, SetupProvider provider) {
            this.localFile = file;
            this.remotePath = remotePath;
            this.setupProvider = provider;
        }
    }

    private final Map<String, BinarySetupMapEntry> binarySetupMap;
    private final Map<ExecutionEnvironment, List<String>> updateMap;
    private final ExecutionEnvironment executionEnvironment;
    private boolean problems;
    private String reason;
    private String libDir;

    /*package*/ RemoteServerSetup(ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
        Lookup.Result<SetupProvider> results = Lookup.getDefault().lookup(new Lookup.Template<>(SetupProvider.class));
        Collection<? extends SetupProvider> list = results.allInstances();
        SetupProvider[] providers = list.toArray(new SetupProvider[list.size()]);
        libDir = HostInfoProvider.getLibDir(executionEnvironment); //NB: should contain trailing '/'
        if (!libDir.endsWith("/")) { // NOI18N
            libDir += "/"; // NOI18N
        }
        // Binary setup map
        binarySetupMap = new HashMap<>();
        for (SetupProvider provider : providers) {
            Map<String, File> map = provider.getBinaryFiles(executionEnvironment);
            if (map != null) {
                for (Map.Entry<String, File> entry : map.entrySet()) {
                    String remotePath = libDir + entry.getKey();
                    binarySetupMap.put(remotePath, new BinarySetupMapEntry(entry.getValue(), remotePath, provider));
                }
            }
        }

        updateMap = new HashMap<>();
    }

    /*package*/ boolean needsSetupOrUpdate() {
        List<String> updateList = new ArrayList<>();
        updateMap.clear();
        updateList = getBinaryUpdates();
        if (!updateList.isEmpty()) {
            updateMap.put(executionEnvironment, updateList);
            return true;
        } else {
            return false;
        }
    }

    protected  void setup() {
        List<String> list = updateMap.remove(executionEnvironment);
        // problematic entries to construct error message
        Map<SetupProvider, List<BinarySetupMapEntry>> problematic = new HashMap<>();
        for (String path : list) {
            RemoteUtil.LOGGER.log(Level.FINE, "RSS.setup: Updating \"{0}\" on {1}", new Object[]{path, executionEnvironment}); //NO18N
            if (binarySetupMap.containsKey(path)) {
                BinarySetupMapEntry entry = binarySetupMap.get(path);
                CndUtils.assertNotNullInConsole(entry, "Null entry"); //NOI18N
                if (entry != null) {
                    if (entry.localFile == null) {
                        RemoteUtil.LOGGER.severe("Can not find file " + entry.remotePath + " in IDE installation"); // NOI18N
                        continue;
                    }
                    File file = entry.localFile;
                    CndUtils.assertAbsoluteFileInConsole(file);
                    //String remotePath = REMOTE_LIB_DIR + file.getName();
                    String remotePath = path;
                    boolean success = false;
                    try {
                        success = file.exists() && copyTo(file, remotePath);
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                    if (!success) {
                        List<BinarySetupMapEntry> l = problematic.get(entry.setupProvider);
                        if (l == null) {
                            l = new ArrayList<>();
                            problematic.put(entry.setupProvider, l);
                        }
                        l.add(entry);
                    }
                }
            }
        }
        if (! problematic.isEmpty()) {
            // construct error message
            StringBuilder message = new StringBuilder(NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure_Start", executionEnvironment));
            StringBuilder consequences = new StringBuilder(NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure_Consequences"));
            for (Map.Entry<SetupProvider, List<BinarySetupMapEntry>> tmp : problematic.entrySet()) {
                List<File> files = new ArrayList<>();
                for (BinarySetupMapEntry entry : tmp.getValue()) {
                    files.add(entry.localFile);
                    message.append('\n').append(NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure_Line",
                            entry.localFile.getName(), CndPathUtilities.getDirName(entry.remotePath)));
                }
                consequences.append('\n');
                tmp.getKey().failed(files, consequences);
            }
            message.append('\n');
            message.append(consequences);
            setProblems(message.toString());
        }
    }

    private boolean copyTo(File file, String remoteFilePath) throws InterruptedException, ExecutionException {
        return CommonTasksSupport.uploadFile(file.getAbsolutePath(), executionEnvironment, remoteFilePath, 0775, true).get().isOK();
    }

    private List<String> getBinaryUpdates() {
        return new ArrayList<>(binarySetupMap.keySet());
    }

    /**
     * Map the reason to a more human readable form. The original reason is currently
     * always in English. This method would need changing were that to change.
     *
     * @return The reason, possibly localized and more readable
     */
    public String getReason() {
        return reason;
    }

    private void setProblems(String reason) {
        this.problems = true;
        this.reason = reason;
    }

    protected boolean hasProblems() {
        return problems;
    }
}
