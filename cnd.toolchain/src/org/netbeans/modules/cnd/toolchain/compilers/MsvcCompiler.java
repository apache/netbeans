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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.toolchain.compilers;

import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
/*package*/ final class MsvcCompiler extends GNUCCompiler {
   /** Creates a new instance of GNUCCompiler */
   protected MsvcCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
       super(env, flavor, kind, name, displayName, path);
   }

   @Override
   public MsvcCompiler createCopy(CompilerFlavor flavor) {
       MsvcCompiler copy = new MsvcCompiler(getExecutionEnvironment(), flavor, getKind(), getName(), getDisplayName(), getPath()); // NOI18N
       if (isReady()) {
            copy.copySystemIncludeDirectories(getSystemIncludeDirectories());
            copy.copySystemPreprocessorSymbols(getSystemPreprocessorSymbols());
            copy.copySystemIncludeHeaders(getSystemIncludeHeaders());
       }
       return copy;
   }

   public static MsvcCompiler create(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
       return new MsvcCompiler(env, flavor, kind, name, displayName, path);
   }

    @Override
    public CompilerDescriptor getDescriptor() {
        if (getKind() == PredefinedToolKind.CCCompiler) {
            return getFlavor().getToolchainDescriptor().getCpp();
        } else {
            return getFlavor().getToolchainDescriptor().getC();
        }
    }

   @Override
   protected CompilerDefinitions getFreshCompilerDefinitions() {
        CompilerDefinitions res = new CompilerDefinitions();
        completePredefinedMacros(res);
        String list = System.getenv("INCLUDE"); // NOI18N
        if (list != null) {
            StringTokenizer st = new StringTokenizer(list, ";"); // NOI18N
            while (st.hasMoreTokens()) {
                res.systemIncludeDirectoriesList.add(st.nextToken());
            }
        }
        return res;
   }

    @Override
   protected String getUniqueID() {
       return ""+getKind()+super.getUniqueID();
    }

}
