/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.spi.remote.setup.support;

import java.util.Collection;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
abstract public class RemoteSyncNotifier {

    private static final RemoteSyncNotifier INSTANCE = new FsSkewNotifierDefault();

    protected RemoteSyncNotifier() {
    }

    public static RemoteSyncNotifier getInstance() {
        if (CndUtils.isStandalone() || CndUtils.isUnitTestMode()) {
            return INSTANCE;
        }
        Collection<? extends RemoteSyncNotifier> notifiers = Lookup.getDefault().lookupAll(RemoteSyncNotifier.class);
        if (notifiers.isEmpty()) {
            return INSTANCE;
        }
        return notifiers.iterator().next();
    }

    protected static CharSequence secondsToString(long skew) {
        long seconds = skew % 60;
        long minutes = skew / 60;
        long hours = minutes / 60;
        minutes %= 60;
        long days = hours / 24;
        hours %= 24;
        StringBuilder sb = new StringBuilder();
        String[] unitNamesSingle = new String[]{
            NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Day"),
            NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Hour"),
            NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Minute"),
            NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Second")
        };
        String[] unitNamesPlural = new String[]{
            NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Days"),
            NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Hours"),
            NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Minutes"),
            NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Seconds")
        };
        long unitVlues[] = new long[]{days, hours, minutes, seconds};
        assert unitNamesSingle.length == unitVlues.length;
        String comma = NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Comma");
        String and = NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_And");
        for (int i = 0; i < unitVlues.length; i++) {
            if (unitVlues[i] > 0) {
                if (sb.length() > 0) {
                    if (i == unitVlues.length - 1) {
                        sb.append(' ');
                        sb.append(and);
                    } else {
                        sb.append(comma);
                    }
                    sb.append(' ');
                }
                String unitName = (unitVlues[i] > 1) ? unitNamesPlural[i] : unitNamesSingle[i];
                sb.append(unitVlues[i]).append(' ').append(unitName);
            }
        }
        return sb;
    }

    abstract public void notify(final ExecutionEnvironment env, final long fsSkew);
    abstract public void warnDoubleRemote(ExecutionEnvironment buildEnv, FileSystem sourceFileSystem);

    private static class FsSkewNotifierDefault extends RemoteSyncNotifier {

        public FsSkewNotifierDefault() {
        }

        @Override
        public void notify(ExecutionEnvironment env, long fsSkew) {
            CharSequence skewString = secondsToString(fsSkew);
            if (fsSkew > 0) {
                skewString = NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Faster", skewString);
            } else {
                skewString = NbBundle.getMessage(RemoteSyncNotifier.class, "FS_Skew_Slower", skewString);
            }
            System.err.println(skewString);
        }

        @Override
        public void warnDoubleRemote(ExecutionEnvironment buildEnv, FileSystem sourceFileSystem) {
            String message = NbBundle.getMessage(RemoteSyncNotifier.class, "ErrorDoubleRemote", buildEnv, sourceFileSystem);
            System.err.println(message);
        }
    }
}
