/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.search.util;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;

public class SearchScopeValidator {

    private HostInfo.OSFamily localhostOSFamily;

    public SearchScopeValidator() {
        ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
        if (HostInfoUtils.isHostInfoAvailable(local)) {
            try {
                localhostOSFamily = HostInfoUtils.getHostInfo(local).getOSFamily();
            } catch (Exception ign) {
                // localhost -- not reachable
            }
        }
    }

    public boolean isSearchAllowed(final SearchInfo searchInfo) {
        try {
            if (searchInfo == null) {
                return false;
            }

            List<SearchRoot> searchRoots = searchInfo.getSearchRoots();

            for (SearchRoot searchRoot : searchRoots) {
                ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(searchRoot.getFileObject());

                if (env.isLocal()) {
                    if (localhostOSFamily == null || !localhostOSFamily.isUnix()) {
                        return false;
                    }
                } else {
                    if (HostInfoUtils.isHostInfoAvailable(env)
                            && !HostInfoUtils.getHostInfo(env).getOSFamily().isUnix()) {
                        return false;
                    }
                }

            }
        } catch (IOException ex) {
            return false;
        } catch (ConnectionManager.CancellationException ex) {
            return false;
        }
        return true;
    }
}
