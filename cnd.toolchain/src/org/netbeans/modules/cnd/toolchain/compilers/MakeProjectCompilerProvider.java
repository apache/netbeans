/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.toolchain.compilers;

import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.spi.toolchain.CompilerProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Override the cnd default compiler type "Tool". MakeProjects uses classes derived from Tool but cnd/core
 * can't depend on makeproject classes. So this allows makeproject to provide a tool creator factory.
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.toolchain.CompilerProvider.class, position=1000)
public class MakeProjectCompilerProvider extends CompilerProvider {

    /**
     * Create a class derived from Tool
     *
     * Thomas: If you want/need different information to choose which Tool derived class to create we can change
     * this method. We can also add others, if desired. This was mainly a proof-of-concept that tool creation
     * could be deferred to makeproject.
     */
    @Override
    public Tool createCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        if (flavor.isSunStudioCompiler()) {
            if (kind == PredefinedToolKind.CCompiler) {
                if (flavor.getToolchainDescriptor().getC() != null && flavor.getToolchainDescriptor().getC().getFingerPrintFlags() != null) {
                    return OracleCCompiler.create(env, flavor, kind, name, displayName, path);
                } else {
                    return SunCCompiler.create(env, flavor, kind, name, displayName, path);
                }
            } else if (kind == PredefinedToolKind.CCCompiler) {
                if (flavor.getToolchainDescriptor().getCpp()!= null && flavor.getToolchainDescriptor().getCpp().getFingerPrintFlags() != null) {
                    return OracleCppCompiler.create(env, flavor, kind, name, displayName, path);
                } else {
                    return SunCCCompiler.create(env, flavor, kind, name, displayName, path);
                }
            } else if (kind == PredefinedToolKind.FortranCompiler) {
                return SunFortranCompiler.create(env, flavor, kind, name, displayName, path);
            } else if (kind == PredefinedToolKind.MakeTool) {
                return SunMaketool.create(env, flavor, name, displayName, path);
            } else if (kind == PredefinedToolKind.DebuggerTool) {
                return SunDebuggerTool.create(env, flavor, name, displayName, path);
            } else if (kind == PredefinedToolKind.Assembler) {
                return Assembler.create(env, flavor, kind, name, displayName, path);
            }
        } else /* if (flavor.isGnuCompiler()) */ { // Assume GNU (makeproject system doesn't handle Unknown)
           if (kind == PredefinedToolKind.CCompiler) {
               if ("MSVC".equals(flavor.getToolchainDescriptor().getName())) { // NOI18N
                   return MsvcCompiler.create(env, flavor, kind, name, displayName, path);
               } else {
                   return GNUCCompiler.create(env, flavor, kind, name, displayName, path);
               }
           } else if (kind == PredefinedToolKind.CCCompiler) {
               if ("MSVC".equals(flavor.getToolchainDescriptor().getName())) { // NOI18N
                   return new MsvcCompiler(env, flavor, kind, name, displayName, path);
               } else {
                   return GNUCCCompiler.create(env, flavor, kind, name, displayName, path);
               }
            } else if (kind == PredefinedToolKind.FortranCompiler) {
                return GNUFortranCompiler.create(env, flavor, kind, name, displayName, path);
            } else if (kind == PredefinedToolKind.MakeTool) {
                return GNUMaketool.create(env, flavor, name, displayName, path);
            } else if (kind == PredefinedToolKind.DebuggerTool) {
                return GNUDebuggerTool.create(env, flavor, name, displayName, path);
            } else if (kind == PredefinedToolKind.Assembler) {
                return Assembler.create(env, flavor, kind, name, displayName, path);
            }
        }
        if (kind == PredefinedToolKind.CustomTool) {
            return CustomTool.create(env);
        } else if (kind == PredefinedToolKind.QMakeTool || kind == PredefinedToolKind.CMakeTool) {
            return GeneralTool.create(env, flavor, kind, name, displayName, path);
        }
        return null;
    }
}
