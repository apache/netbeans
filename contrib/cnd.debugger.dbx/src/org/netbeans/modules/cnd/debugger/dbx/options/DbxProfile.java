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

package org.netbeans.modules.cnd.debugger.dbx.options;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Exceptions;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.ExceptionsNodeProp;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Pathmap;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.PathmapNodeProp;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.ProfileOptionSet;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.SignalsNodeProp;
import java.beans.PropertyChangeSupport;

import org.openide.nodes.Sheet;

import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.api.xml.*;

public class DbxProfile extends DbgProfile {

    public static final String PROFILE_ID = "dbxdebugger"; // NOI18N

    /**
     * Constructor
     * Don't call this directly. It will get called when creating
     * ...cnd.execution.profiles.Profile().
     */

    public DbxProfile() {
	options = new ProfileOptionSet();
	exceptions = new Exceptions(this);
	signals = new Signals(this);
	pathmap = new Pathmap(this);
    }

    protected DbxProfile(PropertyChangeSupport pcs, Configuration configuration) {
	super(pcs, configuration);
	options = new ProfileOptionSet();
	exceptions = new Exceptions(this);
	signals = new Signals(this);
	pathmap = new Pathmap(this);
    }

    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects and for storing the object in xml form and
     * parsing the xml code to restore the object.
     */
    public String getId() {
	return PROFILE_ID;
    }

    //
    // XML codec support
    // This stuff ends up in <projectdir>/nbproject/private/configuration.xml
    // 

    // interface ConfigurationAuxObject
    public XMLDecoder getXMLDecoder() {
	return new DbxProfileXMLCodec(this);
    }

    // interface ConfigurationAuxObject
    public XMLEncoder getXMLEncoder() {
	return new DbxProfileXMLCodec(this);
    }

    /**
     * Assign all values from a profileAuxObject to this object (reverse
     * of clone)
     */

    // interface ConfigurationAuxObject
    public void assign(ConfigurationAuxObject profileAuxObject) {
	if (!(profileAuxObject instanceof DbxProfile)) {
	    // FIXUP: exception ????
	    System.err.print("DbxProfile - assign: DbxProfile object type expected - got " + profileAuxObject); // NOI18N
	    return;
	}
	DbxProfile that = (DbxProfile) profileAuxObject;

	this.setValidatable(that.isValidatable());

	this.options.assign(that.options);
	notifyOptionsChange();

	this.exceptions().assign(that.exceptions());
	this.signals().assign(that.signals());
	this.pathmap().assign(that.pathmap());
	this.setHost(that.getHost());
	this.setBuildFirstOverriden(that.isBuildFirstOverriden());
	this.setSavedBuildFirst(that.isSavedBuildFirst());
    }

    

    /**
     * Clone itself to an identical (deep) copy.
     */

    // interface ConfigurationAuxObject
    public ConfigurationAuxObject clone(Configuration conf) {
	DbxProfile clone = new DbxProfile(null, conf);

	// don't clone pcs ... we'll end up notifying listeners prematurely
	// they will get notified on 'assign()'.

	clone.setValidatable(this.isValidatable());
	clone.options = this.options.makeCopy();
	clone.exceptions = (Exceptions) this.exceptions.clone();
	clone.signals = (Signals) this.signals.clone();
	clone.pathmap = (Pathmap) this.pathmap.clone();
	clone.setHost(getHost());
	clone.buildFirstOverriden = buildFirstOverriden;
	clone.savedBuildFirst = savedBuildFirst;

	return clone;
    }

    public Sheet getSheet() {
	Sheet sheet = new Sheet();
	Sheet.Set set;

	set = new Sheet.Set();
	set.setName(Catalog.get("General"));
	set.setDisplayName(Catalog.get("General"));
	set.setShortDescription(Catalog.get("General"));
        
        set.put(DebuggerOption.DEBUG_COMMAND.createNodeProp(this));
        set.put(DebuggerOption.DEBUG_DIR.createNodeProp(this));

	set.put(DebuggerOption.DBX_INIT_FILE.createNodeProp(this));
	set.put(new PathmapNodeProp(this, Catalog.get("PathmapPropDisplayName")));
	set.put(new ExceptionsNodeProp(this));
	set.put(new SignalsNodeProp(this));
	sheet.put(set);

	set = new Sheet.Set();
	set.setName(Catalog.get("ExpressionPresentation"));
	set.setDisplayName(Catalog.get("ExpressionPresentation"));
	set.setShortDescription(Catalog.get("ExpressionPresentation"));
	set.put(DebuggerOption.OUTPUT_BASE.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_DYNAMIC_TYPE.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_INHERITED_MEMBERS.createNodeProp(this));
	set.put(DebuggerOption.SHOW_STATIC_MEMBERS.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_CLASS_PREFIX.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_MAX_OBJECT_SIZE.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_MAX_STRING_LENGTH.createNodeProp(this));
	sheet.put(set);

	set = new Sheet.Set();
	set.setName(Catalog.get("ExpressionEvaluation"));
	set.setDisplayName(Catalog.get("ExpressionEvaluation"));
	set.setShortDescription(Catalog.get("ExpressionEvaluation"));
	set.put(DebuggerOption.ARRAY_BOUNDS_CHECK.createNodeProp(this));
	set.put(DebuggerOption.C_ARRAY_OP.createNodeProp(this));
	set.put(DebuggerOption.INPUT_CASE_SENSITIVE.createNodeProp(this));
	set.put(DebuggerOption.LANGUAGE_MODE.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_PRETTY_PRINT.createNodeProp(this));
	set.put(DebuggerOption.OVERLOAD_OPERATOR.createNodeProp(this));
	set.put(DebuggerOption.OVERLOAD_FUNCTION.createNodeProp(this));
	set.put(DebuggerOption.SCOPE_GLOBAL_ENUMS.createNodeProp(this));
	set.put(DebuggerOption.SCOPE_LOOK_ASIDE.createNodeProp(this));
        set.put(DebuggerOption.MACRO_EXPAND.createNodeProp(this));
        set.put(DebuggerOption.MACRO_SOURCE.createNodeProp(this));
	sheet.put(set);

	set = new Sheet.Set();
	set.setName(Catalog.get("SessionStartup"));
	set.setDisplayName(Catalog.get("SessionStartup"));
	set.setShortDescription(Catalog.get("SessionStartup"));
	set.put(DebuggerOption.OPTION_EXEC32.createNodeProp(this));
	/* Global options
	set.put(DebuggerOption.PROC_EXCLUSIVE_ATTACH.createNodeProp(this));
	set.put(DebuggerOption.MAIN_FUNC_WARNING.createNodeProp(this));
	set.put(DebuggerOption.SUPPRESS_STARTUP_MESSAGE.createNodeProp(this));
	*/
	sheet.put(set);
	
	/* Global options
	set = new Sheet.Set();
	set.setName(Catalog.get("WindowProperties"));
	set.setDisplayName(Catalog.get("WindowProperties"));
	set.setShortDescription(Catalog.get("WindowProperties"));
	set.put(DebuggerOption.FRONT_DBX.createNodeProp(this));
	set.put(DebuggerOption.FRONT_PIO.createNodeProp(this));
	set.put(DebuggerOption.OPEN_THREADS.createNodeProp(this));
	set.put(DebuggerOption.OPEN_SESSIONS.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_LIST_SIZE.createNodeProp(this));
	sheet.put(set);
	*/
	
	/* Global options
	set = new Sheet.Set();
	set.setName(Catalog.get("Persistence"));
	set.setDisplayName(Catalog.get("Persistence"));
	set.setShortDescription(Catalog.get("Persistence"));
	set.put(DebuggerOption.SAVE_BREAKPOINTS.createNodeProp(this));
	set.put(DebuggerOption.SAVE_WATCHES.createNodeProp(this));
	sheet.put(set);
	*/
	
	set = new Sheet.Set();
	set.setName(Catalog.get("ExecutionIORunProfile"));
	set.setDisplayName(Catalog.get("ExecutionIORunProfile"));
	set.setShortDescription(Catalog.get("ExecutionIORunProfile"));
	set.put(DebuggerOption.RUN_IO.createNodeProp(this));
	set.put(DebuggerOption.RUN_PTY.createNodeProp(this));
	set.put(DebuggerOption.RUN_QUICK.createNodeProp(this));
	set.put(DebuggerOption.RUN_SAVETTY.createNodeProp(this));
	set.put(DebuggerOption.RUN_SETPGRP.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName(Catalog.get("MultiThreading"));
	set.setDisplayName(Catalog.get("MultiThreading"));
	set.setShortDescription(Catalog.get("MultiThreading"));
	set.put(DebuggerOption.MT_SYNC_TRACKING.createNodeProp(this));
	set.put(DebuggerOption.MT_SCALABLE.createNodeProp(this));
	set.put(DebuggerOption.MT_RESUME_ONE.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName(Catalog.get("FollowFork"));
	set.setDisplayName(Catalog.get("FollowFork"));
	set.setShortDescription(Catalog.get("FollowFork"));
	set.put(DebuggerOption.FOLLOW_FORK_MODE.createNodeProp(this));
	// OLD See 6573955
	// OLD set.put(DebuggerOption.FOLLOW_FORK_INHERIT.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName(Catalog.get("StackPresentation"));
	set.setDisplayName(Catalog.get("StackPresentation"));
	set.setShortDescription(Catalog.get("StackPresentation"));
	set.put(DebuggerOption.OUTPUT_SHORT_FILE_NAME.createNodeProp(this));
	set.put(DebuggerOption.STACK_VERBOSE.createNodeProp(this));
	set.put(DebuggerOption.STACK_MAX_SIZE.createNodeProp(this));
	set.put(DebuggerOption.STACK_FIND_SOURCE.createNodeProp(this));
	sheet.put(set);

	set = new Sheet.Set();
	set.setName(Catalog.get("DebuggingBehaviour"));
	set.setDisplayName(Catalog.get("DebuggingBehaviour"));
	set.setShortDescription(Catalog.get("DebuggingBehaviour"));
	set.put(DebuggerOption.STEP_EVENTS.createNodeProp(this));
	set.put(DebuggerOption.STEP_GRANULARITY.createNodeProp(this));
	/* Global options
	set.put(DebuggerOption.TRACE_SPEED.createNodeProp(this));
	set.put(DebuggerOption.RUN_AUTOSTART.createNodeProp(this));
	*/
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName(Catalog.get("Logging"));
	set.setDisplayName(Catalog.get("Logging"));
	set.setShortDescription(Catalog.get("Logging"));
	set.put(DebuggerOption.OUTPUT_LOG_FILE.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_LOG_FILE_NAME.createNodeProp(this));
	set.put(DebuggerOption.SESSION_LOG_FILE.createNodeProp(this));
	set.put(DebuggerOption.SESSION_LOG_FILE_NAME.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName(Catalog.get("DebuggingPerformance"));
	set.setDisplayName(Catalog.get("DebuggingPerformance"));
	set.setShortDescription(Catalog.get("DebuggingPerformance"));
//	set.put(DebuggerOption.STACK_VERBOSE.createNodeProp(this));
//	set.put(DebuggerOption.STACK_MAX_SIZE.createNodeProp(this));
//	set.put(DebuggerOption.OUTPUT_MAX_OBJECT_SIZE.createNodeProp(this));
//	set.put(DebuggerOption.RTC_USE_TRAPS.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_AUTO_FLUSH.createNodeProp(this));
	set.put(DebuggerOption.SYMBOL_INFO_COMPRESSION.createNodeProp(this));
//	set.put(DebuggerOption.MT_SCALABLE.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName(Catalog.get("BackDoors"));
	set.setDisplayName(Catalog.get("BackDoors"));
	set.setShortDescription(Catalog.get("BackDoors"));
	set.put(DebuggerOption.FIX_VERBOSE.createNodeProp(this));
	set.put(DebuggerOption.POP_AUTO_DESTRUCT.createNodeProp(this));
	set.put(DebuggerOption.DISSASSEMLER_VERSION.createNodeProp(this));
	sheet.put(set);

	return sheet;
    }
}
