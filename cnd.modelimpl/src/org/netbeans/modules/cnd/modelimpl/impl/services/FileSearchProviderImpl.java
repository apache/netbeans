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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.apt.support.spi.APTFileSearchImplementation;
import org.netbeans.modules.cnd.apt.support.spi.APTProjectFileSearchProvider;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.FSPath;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.apt.support.spi.APTProjectFileSearchProvider.class, position=1000)
public class FileSearchProviderImpl implements APTProjectFileSearchProvider {

    @Override
    public APTFileSearchImplementation getSearchImplementation(final Key prjKey) {
        for(CsmProject project : ModelImpl.instance().projects()){
            Object platformProject = project.getPlatformProject();
            if (platformProject instanceof NativeProject) {
                NativeProject nativeProject = (NativeProject) platformProject;
                Key aKey = KeyUtilities.createProjectKey(nativeProject);
                if (prjKey.equals(aKey)) {
                    return new APTFileSearchImplementationImpl(nativeProject);
                }
            }
        }
        return null;
    }

    private static final class APTFileSearchImplementationImpl implements APTFileSearchImplementation {
        private final NativeProject project;

        private APTFileSearchImplementationImpl(NativeProject project) {
            this.project = project;
        }

        @Override
        public FSPath searchInclude(String include, CharSequence basePath) {
            NativeFileSearch provider = NativeProjectSupport.getNativeFileSearch(project);
            if (provider != null) {
                Collection<FSPath> searchFile = provider.searchFile(project, include);
                if (searchFile.size() > 0) {
                    include = trimUpFolder(include);
                    List<FSPath> candidates = new ArrayList<>(1);
                    for(FSPath p : searchFile) {
                        String path = p.getPath();
                        path = path.replace('\\', '/');
                        if (path.endsWith(include)) {
                            candidates.add(p);
                        }
                    }
                    if (candidates.size() == 1) {
                        return candidates.get(0);
                    } else if (candidates.size() > 1) {
                        int best = -1;
                        FSPath candidate = null;
                        String from = basePath.toString();
                        from = from.replace('\\', '/');
                        for(FSPath p : candidates) {
                            String path = p.getPath();
                            path = path.replace('\\', '/');
                            int dist = distance(from, path);
                            if (best < dist) {
                                candidate = p;
                                best = dist;
                            }
                        }
                        return candidate;
                    }
                }
            }
            return null;
        }

        private int distance(String from, String to) {
            int res = 0;
            for(int i = 0; i < Math.min(from.length(), to.length()); i++) {
                if (from.charAt(i) != to.charAt(i)){
                    return res;
                }
                res++;
            }
            return res;
        }

        private String trimUpFolder(String include){
            while(include.startsWith("../")) { // NOI18N
                include = include.substring(3);
            }
            return include;
        }

        @Override
        public String toString() {
            return "APTFileSearchImplementationImpl{" + "project=" + project + '}'; // NOI18N
        }
        
        
    }
}
