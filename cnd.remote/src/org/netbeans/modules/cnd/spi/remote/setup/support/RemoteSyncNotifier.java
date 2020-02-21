/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
