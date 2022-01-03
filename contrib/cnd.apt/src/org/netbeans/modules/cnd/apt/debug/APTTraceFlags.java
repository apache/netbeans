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
