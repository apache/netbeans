/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
