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

package org.netbeans.modules.cnd.toolchain.compilers;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.toolchain.compilers.CCCCompiler.CompilerDefinitions;
import org.netbeans.modules.cnd.toolchain.support.CompilerDefinition;

/**
 *
 */
public class SPICompilerAccesor {
    private final Tool compiler;

    public SPICompilerAccesor(Tool compiler) {
        this.compiler = compiler;
    }
    
    public List<List<String>> getCompilerDefinitions() {
        if (compiler instanceof CCCCompiler) {
            CompilerDefinitions systemIncludesAndDefines = ((CCCCompiler) compiler).getFreshCompilerDefinitions();
            List<List<String>> res = new ArrayList<List<String>>();
            res.add(systemIncludesAndDefines.systemIncludeDirectoriesList);
            res.add(systemIncludesAndDefines.systemPreprocessorSymbolsList);
            res.add(systemIncludesAndDefines.systemIncludeHeadersList);
            return res;
        }
        return null;
    }

    public void applyCompilerDefinitions(List<List<String>> pair) {
        if (compiler instanceof CCCCompiler) {
            List<Integer> user = merge(((CCCCompiler) compiler).getSystemIncludeDirectories(), pair.get(0));
            ((CCCCompiler) compiler).setSystemIncludeDirectories(pair.get(0));
            setUserAdded(user, ((CCCCompiler) compiler).getSystemIncludeDirectories());

            user = merge(((CCCCompiler) compiler).getSystemPreprocessorSymbols(), pair.get(1));
            ((CCCCompiler) compiler).setSystemPreprocessorSymbols(pair.get(1));
            setUserAdded(user, ((CCCCompiler) compiler).getSystemPreprocessorSymbols());

            user = merge(((CCCCompiler) compiler).getSystemIncludeHeaders(), pair.get(2));
            ((CCCCompiler) compiler).setSystemIncludeHeaders(pair.get(2));
            setUserAdded(user, ((CCCCompiler) compiler).getSystemIncludeHeaders());
        }
    }
    
    private List<Integer> merge(List<String> old, List<String> newList) {
        List<Integer> user = new ArrayList<Integer>();
        if (old instanceof CompilerDefinition) {
            CompilerDefinition def = (CompilerDefinition) old;
            for (int i = 0; i < def.size(); i++) {
                if (def.isUserAdded(i)) {
                    int j = newList.indexOf(def.get(i));
                    if (j < 0) {
                        j = newList.size();
                        newList.add(def.get(i));
                    }
                    user.add(j);
                }
            }
        }
        return user;
    }
    
    private void setUserAdded(List<Integer> user, List<String> newList) {
        if (newList instanceof CompilerDefinition) {
            CompilerDefinition def = (CompilerDefinition) newList;
            for(int i = 0; i < def.size(); i++) {
                def.setUserAdded(false, i);
            }
            for(Integer i : user) {
                def.setUserAdded(true, i);
            }
        }
    }
}
