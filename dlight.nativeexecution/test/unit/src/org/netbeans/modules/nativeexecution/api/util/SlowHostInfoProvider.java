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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider.class, position = 10)
final public class SlowHostInfoProvider implements HostInfoProvider {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public HostInfo getHostInfo(final ExecutionEnvironment execEnv) throws IOException, InterruptedException {
        boolean enabled = Boolean.getBoolean("dlight.nativeexecution.SlowHostInfoProviderEnabled"); // NOI18N

        if (!enabled) {
            return null;
        }

        final Collection<? extends HostInfoProvider> providers = Lookup.getDefault().lookupAll(HostInfoProvider.class);
        HostInfo result = null;
        int providerIdx = 0;

        for (HostInfoProvider provider : providers) {
            if (provider == this) {
                continue;
            }

            providerIdx++;

            try {
                for (int i = 0; i < 3; i++) {
                    try {
                        log.log(Level.INFO, "Trying hard to get some information about the host... Not an easy task... [provider {0}/ delay {1}]", new Object[]{providerIdx, i}); // NOI18N
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        log.log(Level.SEVERE, "InterruptedException", ex); // NOI18N
                    }
                }
                result = provider.getHostInfo(execEnv);
            } catch (IOException ex) {
                String msg = "Exception while recieving hostinfo for " + execEnv.toString(); // NOI18N
                log.log(Level.SEVERE, msg, ex);
            }
            if (result != null) {
                break;
            }
        }

        return result;
    }
}
