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

package org.netbeans.modules.cnd.apt.debug;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ComponentType;

/**
 * A common place for APT tracing flags that are used by several classes
 */
public class APTTraceFlags {

    public static final boolean USE_CLANK;
    public static final boolean TRACE_PREPROC = Boolean.getBoolean("apt.clank.trace.pp"); // NOI18N
    public static final boolean TRACE_PREPROC_STACKS = Boolean.getBoolean("apt.clank.trace.pp.stacks"); // NOI18N
    private static final String APT_USE_CLANK_PROP = "apt.use.clank"; // NOI18N

    static {
        String propUseClank = System.getProperty(APT_USE_CLANK_PROP); //NOI18N
        boolean val;
        boolean explicitlySet = false;
        if (propUseClank != null) {
            val = Boolean.parseBoolean(propUseClank);
            explicitlySet = true;
        } else {
            final ComponentType product = ComponentType.getComponent();
            switch (product) {
                case CND:
                    val = false; // FIXME: should be true after clank speed optimization
                    break;
                case PROJECT_CREATOR:
                case OSS_IDE:
                case DBXTOOL:
                case DLIGHTTOOL:
                case CODE_ANALYZER:
                    val = false;
                    break;
                default:
                    val = false;
                    APTUtils.LOG.severe("Unexpected product type: " + product); //NOI18N
                    break;
            }
        }
        USE_CLANK = val;
        if (!CndUtils.isUnitTestMode()) {
            // APTUtils.LOG has level SEVERE by default, so we can't use it here
            String msg = (USE_CLANK ? "new" : "old"); // NOI18N
            if (explicitlySet) {
                msg += " (explicitly set by " + APT_USE_CLANK_PROP + "=" + propUseClank + ")"; // NOI18N
            }
            msg += " [ " + ComponentType.getFullName() + "]"; // NOI18N
            Logger.getLogger(APTTraceFlags.class.getName()).log(Level.INFO, "C/C++ code model: using {0} preprocessor", msg); //NOI18N
        }
    }
    
    public static final boolean DEFERRED_MACRO_USAGES = DebugUtils.getBoolean("apt.deferred.macro.usages", true); // NOI18N

    public static final boolean FIX_NOT_FOUND_INCLUDES = DebugUtils.getBoolean("apt.fix.includes", true); // NOI18N
    public static final boolean ALWAYS_USE_NB_FS = DebugUtils.getBoolean("apt.always.use.filesystem", true); // NOI18N
    public static final boolean ALWAYS_USE_BUFFER_BASED_FILES = DebugUtils.getBoolean("apt.use.buffer.fs", true); // NOI18N

    public static final boolean INCLUDE_TOKENS_IN_TOKEN_STREAM = DebugUtils.getBoolean("apt.include.tokens", false); // NOI18N
    public static final boolean APT_SHARE_MACROS = DebugUtils.getBoolean("apt.share.macros", true); // NOI18N

    public static final boolean APT_SHARE_TEXT = DebugUtils.getBoolean("apt.share.text", true); // NOI18N
    //public static final boolean APT_USE_STORAGE_SET = DebugUtils.getBoolean("apt.share.storage", true); // NOI18N
    public static final boolean APT_NON_RECURSE_VISIT = DebugUtils.getBoolean("apt.nonrecurse.visit", true); // NOI18N

    public static final int     BUF_SIZE = 8192*Integer.getInteger("cnd.file.buffer", Integer.getInteger("antlr.input.buffer", 1).intValue()).intValue(); // NOI18N
    
    public static final boolean OPTIMIZE_INCLUDE_SEARCH = DebugUtils.getBoolean("cnd.optimize.include.search", true); // NOI18N

    public static final boolean TRACE_APT = Boolean.getBoolean("cnd.apt.trace"); // NOI18N
    public static final boolean TRACE_APT_LEXER = Boolean.getBoolean("cnd.aptlexer.trace"); // NOI18N
    public static final boolean TRACE_APT_CACHE = Boolean.getBoolean("cnd.apt.cache.hits"); // NOI18N
    public static final boolean USE_SOFT_APT_CACHE = DebugUtils.getBoolean("cnd.apt.cache.soft", true); // NOI18N

    //public static final boolean USE_INCLIDE_RESOLVER_CACHE = DebugUtils.getBoolean("cnd.apt.include.resolver.cache", true); // NOI18N

    public static final boolean USE_APT_TEST_TOKEN = Boolean.getBoolean("cnd.apt.apttoken"); // NOI18N

    public static final boolean TEST_APT_SERIALIZATION = DebugUtils.getBoolean("cnd.cache.apt", false); // NOI18N

    public static final boolean APT_DISPOSE_TOKENS = DebugUtils.getBoolean("apt.dispose.tokens", false); // NOI18N
    
    public static final boolean APT_USE_SOFT_REFERENCE = DebugUtils.getBoolean("apt.soft.reference", true); // NOI18N
    
    public static final boolean APT_ABSOLUTE_INCLUDES = DebugUtils.getBoolean("apt.absolute.include", true); // NOI18N
    
    public static final boolean APT_RECURSIVE_BUILD = DebugUtils.getBoolean("apt.recursive.build", true); // NOI18N
}
