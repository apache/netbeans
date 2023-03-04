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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.NbPreferences;

/**
 *
 * @author vk155633
 */
/*package*/ class HostInfoCache implements ConnectionListener {

    private static final HostInfoCache  INSTANCE = new HostInfoCache();

    private static final String KEY_USERID = "userId"; //NOI18N
    private static final String KEY_GRPID = "groupId"; //NOI18N
    private static final String KEY_GROUPS = "allGroups"; //NOI18N

    private final Preferences preferences;

    public static HostInfoCache getInstance() {
        return INSTANCE;
    }

    /*package*/ static void initializeIfNeeded() {
        getInstance();
    }

    private HostInfoCache() {
        this.preferences = NbPreferences.forModule(HostInfoCache.class);
        ConnectionManager.getInstance().addConnectionListener(this);
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                preferences.putInt(getKey(KEY_USERID, env), hostInfo.getUserId());
                preferences.putInt(getKey(KEY_GRPID, env), hostInfo.getGroupId());
                preferences.put(getKey(KEY_GROUPS, env), toString(hostInfo.getAllGroupIDs()));

            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } catch (CancellationException ex) {
                // never report CancellationException
            }
        } else {
            Logger.getInstance().log(Level.WARNING,
                    "HostInfo should be available for {0} at this point", //NOI18N
                    new Object[] {env}); 
        }
    }

    private String toString(int[] array) {
        StringBuilder sb = new StringBuilder();
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    sb.append(','); //NOI18N
                }
                sb.append(array[i]);
            }
        }
        return sb.toString();
    }

    private int[] fromString(String text) {
        if (text != null) {
            String[] split = text.trim().split(","); //NOI18N
            int[] tmp = new int[split.length];
            int cnt = 0;
            for (int i = 0; i < split.length; i++) {
                if (split[i].length() > 0 && Character.isDigit(split[i].charAt(0))) {
                    try {
                        tmp[cnt++] = Integer.parseInt(split[i]);
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                }
            }
            if (cnt == tmp.length) {
                return tmp;
            } else {
                int[] result = new int[cnt];
                System.arraycopy(tmp, 0, result, 0, cnt);
                return result;
            }
        }
        return new int[0];
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
    }

    private String getKey(String key, ExecutionEnvironment env) {
        return ExecutionEnvironmentFactory.toUniqueID(env) + '_' + key;
    }

    public int getUserId(ExecutionEnvironment env) {
        return preferences.getInt(getKey(KEY_USERID, env), -1);
    }

    public int getGroupId(ExecutionEnvironment env) {
        return preferences.getInt(getKey(KEY_GRPID, env), -1);
    }

    public int[] getAllGroupIDs(ExecutionEnvironment env) {
        return fromString(preferences.get(getKey(KEY_GROUPS, env), "")); //NOI18N
    }
}
