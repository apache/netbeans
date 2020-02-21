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

package org.netbeans.modules.cnd.api.toolchain;

import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Utilities;

/**
 *
 */
public final class CompilerSetUtils {
    private CompilerSetUtils() {
    }

    /**
     * Get the Cygwin base directory from Cygwin.xml (toolchain definition, which users the Windows registry) or the user's path
     */
    public static String getCygwinBase() {
        return ToolUtils.getCygwinBase();
    }

    /**
     * Find command folder by toolchain definitions, which users the Windows registry or the user's path
     */
    public static String getCommandFolder(CompilerSet cs) {
        String res = null;
        if (cs != null) {
            if (cs.getCompilerFlavor().isCygwinCompiler()) {
                return res;
            }
            res = cs.getCommandFolder();
        }
        if (res != null) {
            return res;
        }
        return ToolUtils.getCommandFolder();
    }
    
    /**
     * 
     * @param cs tool collection
     * @return true if tool collection uses msys
     */
    public static boolean isMsysBased(CompilerSet cs) {
        if (!Utilities.isWindows()) {
            return false;
        }
        if (cs.getCompilerFlavor().isMinGWCompiler()) {
            return true;
        }
        String commandFolder = getCommandFolder(cs);
        if (commandFolder != null && commandFolder.toLowerCase().replace('\\', '/').contains("/msys")) { // NOI18N
            return true;
        }
        return false;
    }

    /**
     * Find MinGW toolchain folder.
     * Actual for tool collection based on MinGW tool collection.
     * For example: Clang reused MinGW linker.
     */
    public static String getMinGWBaseFolder(CompilerSet cs) {
        if (!Utilities.isWindows()) {
            return null;
        }
        if (cs.getCompilerFlavor().isMinGWCompiler()) {
            return null;
        }
        Tool tool = cs.getTool(PredefinedToolKind.CCompiler);
        if (tool != null && tool.getName().contains("clang")) { // NOI18N
            CompilerSetManager csm = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal());
            for(CompilerSet acs : csm.getCompilerSets()) {
                if (acs.getCompilerFlavor().isMinGWCompiler()) {
                    return acs.getDirectory();
                }
            }
        }
        return null;
    }
}
