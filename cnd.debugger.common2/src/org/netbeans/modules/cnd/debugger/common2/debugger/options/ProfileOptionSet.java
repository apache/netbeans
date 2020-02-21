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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetSupport;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;

public class ProfileOptionSet extends OptionSetSupport {

    private static final Option[] options = {
	DebuggerOption.RUN_IO,
	DebuggerOption.RUN_PTY,
	DebuggerOption.RUN_QUICK,
	DebuggerOption.OUTPUT_AUTO_FLUSH,
	DebuggerOption.RUN_SAVETTY,
	DebuggerOption.RUN_SETPGRP,

	DebuggerOption.MT_SCALABLE,
	DebuggerOption.MT_RESUME_ONE,
	DebuggerOption.MT_SYNC_TRACKING,

        DebuggerOption.GDB_FOLLOW_FORK_MODE,
        DebuggerOption.GDB_DETACH_ON_FORK,
        
        DebuggerOption.GDB_REVERSE_DEBUGGING,
        
        DebuggerOption.FOLLOW_FORK_MODE,
	DebuggerOption.FOLLOW_FORK_INHERIT,

	DebuggerOption.OPTION_EXEC32,

	DebuggerOption.OUTPUT_SHORT_FILE_NAME,
	DebuggerOption.STACK_VERBOSE,
	DebuggerOption.STACK_MAX_SIZE,
	DebuggerOption.STACK_FIND_SOURCE,

	DebuggerOption.OUTPUT_BASE,
	DebuggerOption.OUTPUT_DYNAMIC_TYPE,
	DebuggerOption.OUTPUT_INHERITED_MEMBERS,
	DebuggerOption.SHOW_STATIC_MEMBERS,
	DebuggerOption.OUTPUT_CLASS_PREFIX,
	DebuggerOption.OUTPUT_MAX_STRING_LENGTH,
	DebuggerOption.OUTPUT_MAX_OBJECT_SIZE,

	DebuggerOption.INPUT_CASE_SENSITIVE,
	DebuggerOption.SCOPE_GLOBAL_ENUMS,
	DebuggerOption.C_ARRAY_OP,
	DebuggerOption.ARRAY_BOUNDS_CHECK,
	DebuggerOption.LANGUAGE_MODE,
	DebuggerOption.OVERLOAD_FUNCTION,
	DebuggerOption.OVERLOAD_OPERATOR,
	DebuggerOption.SCOPE_LOOK_ASIDE,
        DebuggerOption.MACRO_EXPAND,
        DebuggerOption.MACRO_SOURCE,
	DebuggerOption.OUTPUT_PRETTY_PRINT,

	DebuggerOption.STEP_EVENTS,
	DebuggerOption.STEP_GRANULARITY,

	DebuggerOption.OUTPUT_LOG_FILE,
	DebuggerOption.OUTPUT_LOG_FILE_NAME,
	DebuggerOption.SESSION_LOG_FILE,
	DebuggerOption.SESSION_LOG_FILE_NAME,

	DebuggerOption.SYMBOL_INFO_COMPRESSION,

	DebuggerOption.PROC_EXCLUSIVE_ATTACH,
	DebuggerOption.FIX_VERBOSE,
	DebuggerOption.POP_AUTO_DESTRUCT,
	DebuggerOption.DISSASSEMLER_VERSION,

	DebuggerOption.DBX_INIT_FILE,
	DebuggerOption.GDB_INIT_FILE,
        DebuggerOption.GDB_SOURCE_DIRS,
            
        DebuggerOption.DEBUG_COMMAND,
        DebuggerOption.DEBUG_DIR,
        DebuggerOption.SYMBOL_FILE
    };


    public ProfileOptionSet() {
	setup(options);
    } 

    public ProfileOptionSet(ProfileOptionSet that) {
	setup(options);
	copy(that);
    } 

    @Override
    public OptionSet makeCopy() {
	return new ProfileOptionSet(this);
    }

    // interface OptionSet
    @Override
    public void save() {
	// noop
    }

    // interface OptionSet
    @Override
    public void open() {
	// noop
    }

    // interface OptionSet
    @Override
    public String tag() {
	return "DebugOptions";		// NOI18N
    }

    // interface OptionSet
    @Override
    public String description() {
	return "debugger options";	// NOI18N
    }
}

