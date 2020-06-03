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

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.utils.options.CatalogDynamic;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Option;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionValue;
import org.netbeans.modules.cnd.debugger.common2.utils.options.Validity;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;

public class DebuggerOption extends Option {

    static CatalogDynamic catalog = new CatalogDynamic(DebuggerOption.class);
    static String[] follow_choices;
    static String default_finish;

    private static final Type radio_or_check = Type.CHECK_BOX;

    static {
        /* OLD with remote debugging, we can't count on this anymore
	if (Utilities.getOperatingSystem() == Utilities.OS_LINUX)
            follow_choices = new String[] {"parent", "child", "ask"};
        else
	*/
	follow_choices = new String[] {"parent", "child", "both", "ask"}; // NOI18N
	if (NativeDebuggerManager.isStandalone() || NativeDebuggerManager.isPL()) {
            default_finish = "off"; // NOI18N
        } else {
            default_finish = "on"; // NOI18N
        } 
    }

    public DebuggerOption(String inName, String[] inValues,
			   String inDefaultValue, boolean inIsEngineOption,
			   Type inType, boolean hasTooltip, boolean hasMnemonic) {
	super(inName, catalog, inValues, inDefaultValue, inIsEngineOption, inType,
	      hasTooltip, hasMnemonic);
    }

    public DebuggerOption(String inName, String[] inValues,
			   String inDefaultValue, boolean inIsEngineOption,
			   Type inType, boolean hasTooltip) {
	super(inName, catalog, inValues, inDefaultValue, inIsEngineOption, inType,
	      hasTooltip, false);
    }

    /** this constructor is here for the options where
     *  there are no defined values, but only default value
     *  mostly for TextArea options
     */
    public DebuggerOption(String inName, String inDefaultValue,
			   boolean inIsEngineOption, Type inType, boolean hasTip) {
	super(inName, catalog, null, inDefaultValue, inIsEngineOption, inType, hasTip, false);
    }

    public DebuggerOption(String inName, String inDefaultValue,
			   boolean inIsEngineOption, Type inType, boolean hasTip,
			   boolean hasMnemonic) {
	super(inName, catalog, null, inDefaultValue, inIsEngineOption, inType, hasTip, hasMnemonic);
    }

    /**
     * Given the option and a value, return the sub option
     * this function is created for special cases where an option
     * is enabled/disabled by the behaviour of another option
     */

    // interface Option
    @Override
    public Option getSubOption(String value) {

	if (this == OUTPUT_LOG_FILE && value.equals("custom")) // NOI18N
	    return OUTPUT_LOG_FILE_NAME;

	else if (this == SESSION_LOG_FILE && value.equals("custom")) // NOI18N
	    return SESSION_LOG_FILE_NAME;

	else if (this == RUN_IO && value.equals("pty"))	// NOI18N
	    return RUN_PTY ;

	return null;   
    }

    /** Hack to fix width rendering */
    // interface Option
    @Override
    public boolean verticalLayout() {
	return (this == RUN_IO);
    }

    /** Hack to fix width rendering */
    // interface Option
    @Override
    public boolean overrideHasLabel() {
	return (this != RUN_IO);
    }
    
    
    /**
     * return the sub option if any
     */
    // interface Option
    @Override
    public Option getSubOption() {
	if (this == OUTPUT_LOG_FILE)
	    return OUTPUT_LOG_FILE_NAME;
	else if (this == SESSION_LOG_FILE)
	    return SESSION_LOG_FILE_NAME;
	else if (this == RUN_IO)
	    return RUN_PTY ;

	return null;
    }


    /**
     * returns if this is a sub option or not
     */
    // interface Option
    @Override
    public boolean isSubOption() {
	if (this == RUN_PTY)
	    return true;
	if (this == OUTPUT_LOG_FILE_NAME)
	    return true;
	if (this == SESSION_LOG_FILE_NAME)
	    return true;
	return false;
    }

    // interface Option
    @Override
    public Validity getValidity(String text) {
	return Validity.TRUE;
    }

    // interface Option
    @Override
    public boolean isTrim() {
	return false;
    }

    // interface Option
    @Override
    public boolean persist(OptionValue value) {

	/* LATER?

	This is tricky. The notion of what is default might change with time
	either as dbx changes it's defaults or as we start considering what's
	in .dbxrc as overriden defaults

	if (isSameAsDefault())
	    return false;
	*/

        // We don't bother saving some options; startup options
        // aren't that interesting (while running under the IDE; we mask
        // them out anyway), and pty's don't make sense from invocation
        // to invocation since they're very dynamic in nature.
        // Auto-start is set at startup unconditionally so there's not
        // much point persisting it.

        if (this == DebuggerOption.RUN_PTY) {
            return false;
        }

	return true;
    }

    

    public static final DebuggerOption ARRAY_BOUNDS_CHECK  =
	new DebuggerOption( // NOI18N
			    "array_bounds_check",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "on",  // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true); //type
    
    public static final DebuggerOption DISSASSEMLER_VERSION  =
	new DebuggerOption(// NOI18N
			    "disassembler_version",   // NOI18N //name
			    new String[] {"autodetect", "v8","v9","v9vis"}, // NOI18N //values  
			    "autodetect",  // NOI18N //default value
			    true, // is engine option
			    Type.RADIO_BUTTON, true); //type
    
    public static final DebuggerOption FIX_VERBOSE  =
	new DebuggerOption(// NOI18N
			    "fix_verbose",   // NOI18N //name  
			    new String[] {"on", "off"}, // NOI18N //values  
			    "off",  // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    public static final DebuggerOption FOLLOW_FORK_INHERIT  =
	new DebuggerOption( // NOI18N
			    "follow_fork_inherit",   // NOI18N //name  
			    new String[] {"on", "off"}, // NOI18N //values  
			    "off",  // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    public static final DebuggerOption FOLLOW_FORK_MODE  =
	new DebuggerOption( // NOI18N
			    "follow_fork_mode",   // NOI18N //name   
			    //new String[] {"parent", "child", "both", "ask"}, // NOI18N //values  
			    follow_choices,
			    "parent",  // NOI18N //default value  
			    true, // is engine option
			    Type.RADIO_BUTTON, true); //type


    public static final DebuggerOption INPUT_CASE_SENSITIVE  =
	new DebuggerOption( // NOI18N
			    "input_case_sensitive",   // NOI18N //name  
			    new String[] {"autodetect", "true", "false"}, // NOI18N //values   
			    "autodetect",  // NOI18N //default value  
			    true, // is engine option
			    Type.RADIO_BUTTON, true); //type

    public static final DebuggerOption C_ARRAY_OP  =
	new DebuggerOption( // NOI18N
			    "c_array_op",  // NOI18N //name  
			    new String[] {"on", "off"},  // NOI18N //values  
			    "off",    // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type


    public static final DebuggerOption LANGUAGE_MODE  =
	new DebuggerOption( // NOI18N
			    "language_mode",     // NOI18N //name   
			    new String[] {"autodetect","main","c","c++","fortran","fortran90"},// NOI18N //values  
			    "autodetect",  // NOI18N //default value  
			    true, // is engine option
			    Type.COMBO_BOX, true); //type
    public static final DebuggerOption MT_SCALABLE  =
	new DebuggerOption(// NOI18N
			    "mt_scalable",   // NOI18N //name   
			    new String[] {"on", "off"}, // NOI18N //values   
			    "off",  // NOI18N //default value   
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    public static final DebuggerOption MT_RESUME_ONE  =
	new DebuggerOption(// NOI18N
			    "mt_resume_one",   // NOI18N //name   
			    new String[] {"on", "off", "auto"}, // NOI18N //values   
			    "auto",  // NOI18N //default value   
			    true, // is engine option
			    Type.RADIO_BUTTON, true); //type

    public static final DebuggerOption MT_SYNC_TRACKING  =
	new DebuggerOption(// NOI18N
			    "mt_sync_tracking",   // NOI18N //name   
			    new String[] {"on", "off"}, // NOI18N //values   
			    "on",  // NOI18N //default value   
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    public static final DebuggerOption OUTPUT_AUTO_FLUSH  =
	new DebuggerOption( // NOI18N
			    "output_auto_flush",   // NOI18N //name   
			    new String[] {"on", "off"}, // NOI18N //values   
			    "on",  // NOI18N //default value   
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    private static final String default_dbx_init_file = System.getProperty("user.home") + "/.dbxrc";
    public static final DebuggerOption DBX_INIT_FILE =
	new DebuggerOption( // NOI18N
			    "dbx_init_file", // NOI18N //name
			    default_dbx_init_file, // deault value
			    true, // is engine option
			    Type.FILE, true);  //type , hasToolTip

    private static final String default_gdb_init_file = System.getProperty("user.home") + "/.gdbinit";
    public static final DebuggerOption GDB_INIT_FILE =
	new DebuggerOption( // NOI18N
			    "gdb_init_file", // NOI18N //name
			    default_gdb_init_file, // deault value
			    false, // is engine option
			    Type.FILE, false);  //type , hasToolTip
    
    public static final DebuggerOption GDB_SOURCE_DIRS =
	new DebuggerOption( // NOI18N
			    "gdb_source_dirs", // NOI18N //name
			    "", //NOI18N // deault value
			    false, // is engine option
			    Type.DIRECTORIES, false);  //type , hasToolTip
    
    public static final DebuggerOption GDB_FOLLOW_FORK_MODE =
	new DebuggerOption( // NOI18N
			    "gdb_follow_fork_mode", // NOI18N //name
                            new String[] {"parent", "child"}, // NOI18N //values  
			    "parent", //NOI18N // deault value
			    true, // is engine option
			    Type.COMBO_BOX, false);  //type , hasToolTip
    
    public static final DebuggerOption GDB_DETACH_ON_FORK =
	new DebuggerOption( // NOI18N
			    "gdb_detach_on_fork", // NOI18N //name
                            new String[] {"on", "off"}, // NOI18N //values  
			    "on", //NOI18N // deault value
			    true, // is engine option
			    Type.COMBO_BOX, false);  //type , hasToolTip
    
    public static final DebuggerOption GDB_REVERSE_DEBUGGING = 
        new DebuggerOption( // NOI18N
			    "reverse_debugging", // NOI18N //name
                            new String[] {"on", "off"},  // NOI18N //values
                            "off",  // NOI18N // deault value
			    true, // is engine option
			    Type.CHECK_BOX, false);  //type , hasToolTip

    public static final DebuggerOption OUTPUT_BASE  =
	new DebuggerOption( // NOI18N
			    "output_base",   // NOI18N //name   
			    new String[] {"8", "10", "16", "automatic"}, // NOI18N //values   
			    "automatic",  // NOI18N //default value  
			    true, // is engine option
			    Type.RADIO_BUTTON, true); //type

    public static final DebuggerOption OUTPUT_DYNAMIC_TYPE  =
	new DebuggerOption( // NOI18N
			    "output_dynamic_type",  // NOI18N //name  
			    new String[] {"on", "off"},  // NOI18N //values  
			    "off",    // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption OUTPUT_INHERITED_MEMBERS  =
	new DebuggerOption( // NOI18N
			    "output_inherited_members",  // NOI18N //name  
			    new String[] {"on", "off"},  // NOI18N //values  
			    "off",    // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption SHOW_STATIC_MEMBERS  =
	new DebuggerOption( // NOI18N
			    "show_static_members",  // NOI18N //name  
			    new String[] {"on", "off"},  // NOI18N //values  
			    "on",    // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption OUTPUT_CLASS_PREFIX  =
	new DebuggerOption( // NOI18N
			    "output_class_prefix",  // NOI18N //name  
			    new String[] {"on", "off"},  // NOI18N //values  
			    "on",    // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption OUTPUT_MAX_OBJECT_SIZE  =
	new DebuggerOption(  // NOI18N
			    "output_max_object_size", // NOI18N //name  
			    "4096",   // NOI18N //defaultValue
			    true, // is engine option
			    Type.TEXT_AREA, true);  //type

    public static final DebuggerOption OUTPUT_LIST_SIZE  =
	new DebuggerOption(  // NOI18N
			    "output_list_size", // NOI18N //name  
			    "10",   // NOI18N //defaultValue
			    true, // is engine option
			    Type.TEXT_AREA, true, true);  //type

    public static final DebuggerOption OUTPUT_MAX_STRING_LENGTH  =
	new DebuggerOption( // NOI18N
			    "output_max_string_length", // NOI18N //name  
			    "4096",            // NOI18N //defaultValue  
			    true, // is engine option
			    Type.TEXT_AREA, true);  //type

    public static final DebuggerOption OPTION_EXEC32  =
        new DebuggerOption(  // NOI18N
                            "option_exec32",   // NOI18N //name   
                            new String[] {"on", "off"}, // NOI18N //values  
 
                            "off",  // NOI18N //default value    
                            false, // is engine option
                            Type.CHECK_BOX, false); //type
                                 
    public static final DebuggerOption OUTPUT_PRETTY_PRINT  =
	new DebuggerOption(  // NOI18N
			    "output_pretty_print",   // NOI18N //name   
			    new String[] {"on", "off"}, // NOI18N //values   
			    "on",  // NOI18N //default value    
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    public static final DebuggerOption OUTPUT_SHORT_FILE_NAME  =
	new DebuggerOption( // NOI18N
			    "output_short_file_name",   // NOI18N //name  
			    new String[] {"on", "off"}, // NOI18N //values   
			    "on",  // NOI18N //default value   
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    public static final DebuggerOption OVERLOAD_FUNCTION  =
	new DebuggerOption( // NOI18N
			    "overload_function",  // NOI18N //name   
			    new String[] {"on", "off"},  // NOI18N //values   
			    "on",    // NOI18N //default value   
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption OVERLOAD_OPERATOR  =
	new DebuggerOption( // NOI18N
			    "overload_operator",  // NOI18N //name    
			    new String[] {"on", "off"},  // NOI18N //values  
			    "on",    // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption POP_AUTO_DESTRUCT  =
	new DebuggerOption( // NOI18N
			    "pop_auto_destruct",   // NOI18N //name  
			    new String[] {"on", "off"}, // NOI18N //values   
			    "on",  // NOI18N //default value  
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    public static final DebuggerOption PROC_EXCLUSIVE_ATTACH  =
	new DebuggerOption( // NOI18N
			    "proc_exclusive_attach",   // NOI18N //name    
			    new String[] {"on", "off"}, // NOI18N //values   
			    "on",  // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true); //type



    public static final DebuggerOption RUN_AUTOSTART  =
	new DebuggerOption( // NOI18N
			    "autostart",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // is engine option
			    Type.CHECK_BOX, true, true);  //type

    public static final DebuggerOption RUN_IO  =
	new DebuggerOption( // NOI18N
			    "run_io",  // NOI18N //name
			    new String[] {"window","stdio", "pty"},  // NOI18N //values
			    "window",    // NOI18N //default value
			    true, // is engine option
			    Type.RADIO_BUTTON, true);  //type

    public static final DebuggerOption RUN_PTY  =
	new DebuggerOption( // NOI18N
			    "run_pty",   // NOI18N //name
			    "",      //default value
			    true, // is engine option
			    Type.TEXT_AREA, true);  //type

    public static final DebuggerOption RUN_QUICK  =
	new DebuggerOption( // NOI18N
			    "run_quick",   // NOI18N //name
			    new String[] {"on", "off"}, // NOI18N //values
			    "off",  // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true); //type

    public static final DebuggerOption RUN_SAVETTY  =
	new DebuggerOption( // NOI18N
			    "run_savetty",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption RUN_SETPGRP  =
	new DebuggerOption( // NOI18N
			    "run_setpgrp",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "off",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type




    public static final DebuggerOption OUTPUT_LOG_FILE =
	new DebuggerOption( // NOI18N
			    "OUTPUT_LOG_FILE",		// name // NOI18N
			    new String[] {"default", "custom"},	// NOI18N // values
			    "default",	// NOI18N //default value
			    false,	// is engine option
			    Type.RADIO_BUTTON, false);

    public static final DebuggerOption OUTPUT_LOG_FILE_NAME  =
	new DebuggerOption( // NOI18N
			    "output_log_file_name",   // NOI18N //name
			    "",			  // NOI18N //default value
			    true, // is engine option
			    Type.TEXT_AREA, true);  //type

    public static final DebuggerOption SESSION_LOG_FILE =
	new DebuggerOption( // NOI18N
			    "SESSION_LOG_FILE",		// name // NOI18N
			    new String[] {"disabled", "custom"},	// NOI18N // values
			    "disabled",	// NOI18N //default value
			    false,	// is engine option
			    Type.RADIO_BUTTON, false);

    public static final DebuggerOption SESSION_LOG_FILE_NAME  =
	new DebuggerOption( // NOI18N
			    "session_log_file_name",   // NOI18N //name
			    "",			  // NOI18N //default value
			    true, // is engine option
			    Type.TEXT_AREA, true);  //type



    public static final DebuggerOption SCOPE_GLOBAL_ENUMS  =
	new DebuggerOption( // NOI18N
			    "scope_global_enums",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "off",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption SCOPE_LOOK_ASIDE  =
	new DebuggerOption( // NOI18N
			    "scope_look_aside",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type
    
    public static final DebuggerOption MACRO_EXPAND  =
	new DebuggerOption( // NOI18N
			    "macro_expand",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type
    
    public static final DebuggerOption MACRO_SOURCE  =
	new DebuggerOption( // NOI18N
			    "macro_source",  // NOI18N //name
			    new String[] {"none", "compiler", "skim", "skim_unless_compiler"},  // NOI18N //values
			    "skim_unless_compiler",    // NOI18N //default value
			    true, // is engine option
			    Type.COMBO_BOX, true);  //type
   
   public static final DebuggerOption FRONT_IDE =
	new DebuggerOption( // NOI18N
			    "front_ide",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type

   public static final DebuggerOption FRONT_DBGWIN  =
	new DebuggerOption( // NOI18N
			    "front_dbgwin",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type
    
    public static final DebuggerOption FRONT_PIO  =
	new DebuggerOption( // NOI18N
			    "front_pio",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "off",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type

    public static final DebuggerOption FRONT_DBX  =
	new DebuggerOption( // NOI18N
			    "front_dbx",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "off",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type

    public static final DebuggerOption FRONT_ACCESS  =
	new DebuggerOption( // NOI18N
			    "front_access",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // isClientOption
			    radio_or_check, false, true);  //type

    public static final DebuggerOption FRONT_MEMUSE  =
	new DebuggerOption( // NOI18N
			    "front_memuse",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // isClientOption
			    radio_or_check, false, true);  //type

    public static final DebuggerOption OPEN_THREADS  =
	new DebuggerOption( // NOI18N
			    "open_threads",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type

    public static final DebuggerOption FINISH_SESSION  =
	new DebuggerOption( // NOI18N
			    "finish_session",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    default_finish, // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type

    public static final DebuggerOption OPEN_SESSIONS  =
	new DebuggerOption( // NOI18N
			    "open_sessions",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type


    public static final DebuggerOption SAVE_BREAKPOINTS  =
	new DebuggerOption( // NOI18N
			    "save_breakpoints",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type


    public static final DebuggerOption SAVE_WATCHES  =
	new DebuggerOption( // NOI18N
			    "save_watches",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "off",    // NOI18N //default value
			    false, // is engine option
			    Type.CHECK_BOX, false);  //type
			    

    public static final DebuggerOption BALLOON_EVAL  =
	new DebuggerOption( // NOI18N
			    "balloon_eval",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type
    
    public static final DebuggerOption ARGS_VALUES_IN_STACK  =
	new DebuggerOption( // NOI18N
			    "args_values_in_stack",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type    
    
    public static final DebuggerOption DO_NOT_POPUP_DEBUGGER_ERRORS_DIALOG  =
	new DebuggerOption( // NOI18N
			    "do_not_popup_debugger_errors_dialog",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "off",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type        


    public static final DebuggerOption SESSION_REUSE  =
	new DebuggerOption( // NOI18N
			    "session_reuse",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "off",    // NOI18N //default value
			    false, // is engine option
			    radio_or_check, false, true);  //type


    public static final DebuggerOption STACK_FIND_SOURCE  =
	new DebuggerOption( // NOI18N
			    "stack_find_source",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption STACK_MAX_SIZE  =
	new DebuggerOption( // NOI18N
			    "stack_max_size",   // NOI18N //name
			    "40",      // NOI18N //default value
			    true, // is engine option
			    Type.TEXT_AREA, true);  //type


    public static final DebuggerOption STACK_VERBOSE  =
	new DebuggerOption( // NOI18N
			    "stack_verbose",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption STEP_EVENTS  =
	new DebuggerOption( // NOI18N
			    "step_events",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "off",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption STEP_GRANULARITY  =
	new DebuggerOption( // NOI18N
			    "step_granularity",  // NOI18N //name
			    new String[] {"statement", "line"},  // NOI18N //values
			    "statement",    // NOI18N //default value
			    true, // is engine option
			    Type.RADIO_BUTTON, true);  //type

    public static final DebuggerOption SUPPRESS_STARTUP_MESSAGE  =
	new DebuggerOption( // NOI18N
			    "suppress_startup_message",   // NOI18N //name
			    "7.6",      // NOI18N //default value
			    true, // is engine option
			    Type.TEXT_AREA, true, true);  //type

    public static final DebuggerOption SYMBOL_INFO_COMPRESSION  =
	new DebuggerOption( // NOI18N
			    "symbol_info_compression",  // NOI18N //name
			    new String[] {"on", "off"},  // NOI18N //values
			    "on",    // NOI18N //default value
			    true, // is engine option
			    Type.CHECK_BOX, true);  //type

    public static final DebuggerOption TRACE_SPEED  =
	new DebuggerOption( // NOI18N
			    "trace_speed",   // NOI18N //name
			    "0.50",      // NOI18N //default value
			    true, // is engine option
			    Type.TEXT_AREA, true, true);  //type
    
    public static final DebuggerOption DEBUG_COMMAND =
	new DebuggerOption( // NOI18N
			    "debug_command", // NOI18N //name
			    "", // deault value
			    false, // is engine option
			    Type.TEXT_AREA, false);  //type , hasToolTip
    
    public static final DebuggerOption DEBUG_DIR =
	new DebuggerOption( // NOI18N
			    "debug_dir", // NOI18N //name
			    "", // deault value
			    false, // is engine option
			    Type.DIRECTORY, false);  //type , hasToolTip
    
    public static final DebuggerOption SYMBOL_FILE = 
        new DebuggerOption( // NOI18N
			    "symbol_file", // NOI18N //name
                            MakeConfiguration.CND_OUTPUT_PATH_MACRO, // deault value
			    false, // is engine option
			    Type.FILE, false);  //type , hasToolTip
}
