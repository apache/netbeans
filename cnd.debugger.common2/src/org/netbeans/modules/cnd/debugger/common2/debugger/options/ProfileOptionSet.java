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

