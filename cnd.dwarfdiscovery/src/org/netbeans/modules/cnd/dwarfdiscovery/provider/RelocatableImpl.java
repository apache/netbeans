/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapper.FS;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.RelocatablePathMapper.ResolvedPath;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;

/**
 *
 */
public class RelocatableImpl implements Relocatable {

        protected String compilePath;
        protected String fullName;
        protected List<String> userIncludes;
        protected List<String> userFiles;
        protected Set<String> includedFiles = Collections.<String>emptySet();

        @Override
        public void resetItemPath(ResolvedPath resolved, RelocatablePathMapper mapper, FS fs) {
            String path = resolved.getPath();
            ResolvedPath resolverCompilePath = mapper.getPath(compilePath);
            if (resolverCompilePath != null) {
                compilePath = PathCache.getString(resolverCompilePath.getPath());
            }
            resolveIncludePaths(resolved.getRoot(), mapper, fs);
            fullName = PathCache.getString(path);
        }

    @Override
    public void resolveIncludePaths(String root, RelocatablePathMapper mapper, FS fs) {
            HashSet<String> newIncludedFiles = new HashSet<String>();
            for(String incl : includedFiles) {
                ResolvedPath resolvedIncluded = mapper.getPath(incl);
                if (resolvedIncluded != null) {
                    newIncludedFiles.add(PathCache.getString(resolvedIncluded.getPath()));
                } else {
                    newIncludedFiles.add(incl);
                }
            }
            includedFiles = newIncludedFiles;
            List<String> newUserIncludes = new ArrayList<String>();
            for(String incl : userIncludes) {
                String newIncl = null;
                if (incl.startsWith("/") || (incl.length() > 2 && incl.charAt(1) == ':')) { //NOI18N
                    if (!incl.startsWith(root)) {
                        ResolvedPath resolvedIncluded = mapper.getPath(incl);
                        if (resolvedIncluded != null) {
                            newIncl = PathCache.getString(resolvedIncluded.getPath());
                        } else {
                            String tmpInkl = incl;
                            String suffix = "";
                            if (tmpInkl.endsWith(Driver.FRAMEWORK)) {
                                suffix = Driver.FRAMEWORK;
                                tmpInkl = tmpInkl.substring(0, tmpInkl.length()-Driver.FRAMEWORK.length());
                            }
                            if (mapper.discover(fs, root, tmpInkl)) {
                                newIncl = PathCache.getString(mapper.getPath(tmpInkl).getPath());
                                if (!suffix.isEmpty()) {
                                    newIncl += Driver.FRAMEWORK;
                                }
                            }
                        }
                    }
                }
                if (newIncl == null) {
                    newIncl = incl;
                }
                newUserIncludes.add(newIncl);
            }
            userIncludes = newUserIncludes;
            List<String> newUserFiles = new ArrayList<String>();
            for(String incl : userFiles) {
                String newIncl = null;
                if (incl.startsWith("/") || (incl.length() > 2 && incl.charAt(1) == ':')) { //NOI18N
                    if (!incl.startsWith(root)) {
                        ResolvedPath resolvedIncluded = mapper.getPath(incl);
                        if (resolvedIncluded != null) {
                            newIncl = PathCache.getString(resolvedIncluded.getPath());
                        } else {
                            if (mapper.discover(fs, root, incl)) {
                                newIncl = PathCache.getString(mapper.getPath(incl).getPath());
                            }
                        }
                    }
                }
                if (newIncl == null) {
                    newIncl = incl;
                }
                newUserFiles.add(newIncl);
            }
            userFiles = newUserFiles;
    }
}
