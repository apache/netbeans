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

package org.netbeans.modules.cnd.api.toolchain;

import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerFlavorImpl;

/**
 *
 */
public abstract class CompilerFlavor {
    
    public static List<CompilerFlavor> getFlavors(int platform) {
        return CompilerFlavorImpl.getFlavors(platform);
    }

    public static CompilerFlavor getUnknown(int platform) {
        return CompilerFlavorImpl.getUnknown(platform);
    }

    public static CompilerFlavor toFlavor(String name, int platform) {
        return CompilerFlavorImpl.toFlavor(name, platform);
    }

    /**
     *
     * @return The tool collection descriptor that loaded from xml file from folder CND/ToolChain/ in file system
     */
    public abstract ToolchainDescriptor getToolchainDescriptor();

    /**
     *
     * @return True if tool chain like to GNU compilers
     */
    public abstract boolean isGnuCompiler();

    /**
     *
     * @return True if tool chain like to SunStudio compilers
     */
    public abstract boolean isSunStudioCompiler();

    /**
     *
     * @return True if tool chain like to Windows Cygwin compilers
     */
    public abstract boolean isCygwinCompiler();

    /**
     *
     * @return True if tool chain like to Windows MinGW compilers
     */
    public abstract boolean isMinGWCompiler();
    

    protected CompilerFlavor() {
        if (!getClass().equals(CompilerFlavorImpl.class)) {
            throw new UnsupportedOperationException("this class can not be overriden by clients"); // NOI18N
        }
    }
}
