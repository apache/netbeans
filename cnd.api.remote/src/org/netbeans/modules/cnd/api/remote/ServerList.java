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

package org.netbeans.modules.cnd.api.remote;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.remote.ServerListImplementation;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * This is a place holder for a RemoteServerList which will be implemented in cnd.remote.
 * 
 */
public class ServerList {

    public static final String PROP_DEFAULT_RECORD = "DEFAULT_RECORD"; //NOI18N
    public static final String PROP_RECORD_LIST = "RECORD_LIST"; //NOI18N

    private ServerList() {
    }

    private static ServerListImplementation DEFAULT;

    private static ServerListImplementation getDefault() {
        synchronized (ServerList.class) {
            ServerListImplementation result = DEFAULT;
            if (result == null) {
                result = Lookup.getDefault().lookup(ServerListImplementation.class);
                if (result == null) {
                    result = new DummyServerListImplementation();
                } else {
                    DEFAULT = result;
                }
            }
            return result;
        }
    };

    public static Collection<? extends ServerRecord> getRecords() {
        return getDefault().getRecords();
    }
    
    public static void setDefaultRecord(ServerRecord defaultRecord) {
        getDefault().setDefaultRecord(defaultRecord);
    }

    public static List<ExecutionEnvironment> getEnvironments() {
        return getDefault().getEnvironments();
    };

    public static ServerRecord get(ExecutionEnvironment env) {
        return getDefault().get(env);
    }

    /** 
     * Gets server record that corresponds the given project' environment .
     * Return value can be null!
     */
    /*package*/ static ServerRecord get(Project project) {
        return getDefault().get(project);
    }

    public static ServerRecord getDefaultRecord() {
        return getDefault().getDefaultRecord();
    }
    
    public static void set(List<ServerRecord> records, ServerRecord defaultRecord) {
        getDefault().set(records, defaultRecord);
    }
    
    public static void save() {
        getDefault().save();
    }

    public static ServerRecord addServer(ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory, boolean asDefault, boolean connect) {
        return getDefault().addServer(env, displayName, syncFactory, asDefault, connect);
    }

    public static ServerRecord createServerRecord(ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory) {
        return getDefault().createServerRecord(env, displayName, syncFactory);
    }

    public static boolean isValidExecutable(ExecutionEnvironment env, String path) {
        return getDefault().isValidExecutable(env, path);
    }

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        getDefault().addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        getDefault().removePropertyChangeListener(listener);
    }


    private static class DummyServerRecord implements ServerRecord {

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ServerList.class, "DUMMY_HOST_NAME");
        }

        @Override
        public ExecutionEnvironment getExecutionEnvironment() {
            return ExecutionEnvironmentFactory.getLocal();
        }

        @Override
        public String getServerDisplayName() {
            return getDisplayName();
        }

        @Override
        public String getServerName() {
            return getDisplayName();
        }

        @Override
        public RemoteSyncFactory getSyncFactory() {
            return RemoteSyncFactory.getDefault();
        }

        @Override
        public String getUserName() {
            return "";
        }

        @Override
        public boolean isDeleted() {
            return true;
        }

        @Override
        public boolean isOffline() {
            return false;
        }

        @Override
        public boolean isOnline() {
            return true;
        }

        @Override
        public boolean isRemote() {
            return false;
        }

        @Override
        public boolean isSetUp() {
            return true;
        }

        @Override
        public boolean setUp() {
            return true;
        }

        @Override
        public void validate(boolean force) {
        }
        
        @Override
        public void checkSetupAfterConnection(Runnable task) {
        }

        @Override
        public boolean getX11Forwarding() {
            return false;
        }

        @Override
        public boolean isRememberPassword() {
            return false;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }        
    }

    private static class DummyServerListImplementation implements ServerListImplementation {

        private ServerRecord record = new DummyServerRecord();

        @Override
        public ServerRecord addServer(ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory, boolean asDefault, boolean connect) {
            return record;
        }

        @Override
        public ServerRecord get(ExecutionEnvironment env) {
            return record;
        }

        @Override
        public ServerRecord get(Project project) {
            return record;
        }

        @Override
        public ServerRecord getDefaultRecord() {
            return record;
        }

        @Override
        public List<ExecutionEnvironment> getEnvironments() {
            return Arrays.asList(record.getExecutionEnvironment());
        }

        @Override
        public Collection<? extends ServerRecord> getRecords() {
            return Arrays.asList(record);
        }

        @Override
        public boolean isValidExecutable(ExecutionEnvironment env, String path) {
            return new File(path).exists();
        }

        @Override
        public void set(List<ServerRecord> records, ServerRecord defaultRecord) {
        }

        @Override
        public void setDefaultRecord(ServerRecord record) {
        }

        @Override
        public ServerRecord createServerRecord(ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory) {
            return new DummyServerRecord();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void save() {
        }
    }
}
