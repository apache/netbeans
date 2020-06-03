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
