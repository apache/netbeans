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

package org.netbeans.modules.cnd.debugger.gdb2.options;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetOwner;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Exceptions;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Pathmap;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.PathmapNodeProp;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.ProfileOptionSet;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;
import java.beans.PropertyChangeSupport;

import org.openide.nodes.Sheet;

import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.api.xml.*;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.SignalsNodeProp;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

public class GdbProfile extends DbgProfile implements 
			ConfigurationAuxObject, OptionSetOwner {

    public static final String PROFILE_ID = "dbx_gdbdebugger"; // NOI18N

    /**
     * Constructor
     * Don't call this directly. It will get called when creating
     * ...cnd.execution.profiles.Profile().
     */

    public GdbProfile() {
        options = new ProfileOptionSet();
        exceptions = new Exceptions(this);
        signals = new Signals(this);
        pathmap = new Pathmap(this);

    }

    public GdbProfile(PropertyChangeSupport pcs, Configuration configuration) {
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
    @Override
    public String getId() {
	return PROFILE_ID;
    }

    //
    // XML codec support
    // This stuff ends up in <projectdir>/nbproject/private/profiles.xml
    // 

    // interface ConfigurationAuxObject
    @Override
    public XMLDecoder getXMLDecoder() {
	return new GdbProfileXMLCodec(this);
    }

    // interface ConfigurationAuxObject
    @Override
    public XMLEncoder getXMLEncoder() {
	return new GdbProfileXMLCodec(this);
    }

    /**
     * Assign all values from a profileAuxObject to this object (reverse
     * of clone)
     */

    // interface ConfigurationAuxObject
    @Override
    public void assign(ConfigurationAuxObject profileAuxObject) {
	if (!(profileAuxObject instanceof GdbProfile)) {
	    // FIXUP: exception ????
	    System.err.print("GdbProfile - assign: GdbProfile object type expected - got " + profileAuxObject); // NOI18N
	    return;
	}
	GdbProfile that = (GdbProfile) profileAuxObject;

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
    @Override
    public ConfigurationAuxObject clone(Configuration conf) {
	GdbProfile clone = new GdbProfile(null, conf);

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

    @Override
    public Sheet getSheet() {
	Sheet sheet = new Sheet();
	Sheet.Set set;

	set = new Sheet.Set();
	set.setName("General"); // NOI18N
	set.setDisplayName(NbBundle.getMessage(GdbProfile.class, "GeneralTxt")); // NOI18N
	set.setShortDescription(NbBundle.getMessage(GdbProfile.class, "GeneralHint")); // NOI18N
        
        set.put(DebuggerOption.DEBUG_COMMAND.createNodeProp(this));
        
        final String baseDir = getConfiguration().getBaseDir();
        final FileSystem fileSystem = getConfiguration().getFileSystem();
        
        set.put(DebuggerOption.DEBUG_DIR.createNodeProp(this, baseDir, fileSystem));
        set.put(DebuggerOption.SYMBOL_FILE.createNodeProp(this, baseDir, fileSystem));
        
	set.put(DebuggerOption.GDB_INIT_FILE.createNodeProp(this, baseDir, fileSystem));
        set.put(DebuggerOption.GDB_SOURCE_DIRS.createNodeProp(this, baseDir, fileSystem));
        
        set.put(DebuggerOption.GDB_FOLLOW_FORK_MODE.createNodeProp(this));
        set.put(DebuggerOption.GDB_DETACH_ON_FORK.createNodeProp(this));
	
        set.put(DebuggerOption.GDB_REVERSE_DEBUGGING.createNodeProp(this));
        
        set.put(new PathmapNodeProp(this, "SubPropDisplayName")); // NOI18N
	// LATER set.put(new ExceptionsNodeProp(this));
        set.put(new SignalsNodeProp(this));
	sheet.put(set);

//	set = new Sheet.Set();
//	set.setName("Expression Presentation"); // NOI18N
//	set.setDisplayName(NbBundle.getMessage(GdbProfile.class, "Expression_PresentationTxt")); // NOI18N
//	set.setShortDescription(NbBundle.getMessage(GdbProfile.class, "Expression_PresentationHint")); // NOI18N
//	set.put(DebuggerOption.OUTPUT_BASE.createNodeProp(this));
//	// LATER set.put(DebuggerOption.OUTPUT_DYNAMIC_TYPE.createNodeProp(this));
//	set.put(DebuggerOption.OUTPUT_INHERITED_MEMBERS.createNodeProp(this));
//	// LATER set.put(DebuggerOption.OUTPUT_CLASS_PREFIX.createNodeProp(this));
//	// LATER set.put(DebuggerOption.OUTPUT_MAX_OBJECT_SIZE.createNodeProp(this));
//	// LATER set.put(DebuggerOption.OUTPUT_MAX_STRING_LENGTH.createNodeProp(this));
//	sheet.put(set);

/* LATER
	set = new Sheet.Set();
	set.setName("Expression Evaluation"); // NOI18N
	set.setDisplayName("Expression Evaluation"); // FIXUP I18N
	set.setShortDescription("Expression Evaluation"); // FIXUP I18N
	set.put(DebuggerOption.ARRAY_BOUNDS_CHECK.createNodeProp(this));
	set.put(DebuggerOption.C_ARRAY_OP.createNodeProp(this));
	set.put(DebuggerOption.INPUT_CASE_SENSITIVE.createNodeProp(this));
	set.put(DebuggerOption.LANGUAGE_MODE.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_PRETTY_PRINT.createNodeProp(this));
	set.put(DebuggerOption.OVERLOAD_OPERATOR.createNodeProp(this));
	set.put(DebuggerOption.OVERLOAD_FUNCTION.createNodeProp(this));
	set.put(DebuggerOption.SCOPE_GLOBAL_ENUMS.createNodeProp(this));
	set.put(DebuggerOption.SCOPE_LOOK_ASIDE.createNodeProp(this));
	sheet.put(set);

	set = new Sheet.Set();
	set.setName("Session Startup"); // NOI18N
	set.setDisplayName("Session Startup"); // FIXUP I18N
	set.setShortDescription("Session Startup"); // FIXUP I18N
	set.put(DebuggerOption.OPTION_EXEC32.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName("Execution & IO RunProfile"); // NOI18N
	set.setDisplayName("Execution & IO RunProfile"); // FIXUP I18N
	set.setShortDescription("Execution & IO RunProfile"); // FIXUP I18N
	set.put(DebuggerOption.RUN_IO.createNodeProp(this));
	set.put(DebuggerOption.RUN_PTY.createNodeProp(this));
	set.put(DebuggerOption.RUN_QUICK.createNodeProp(this));
	set.put(DebuggerOption.RUN_SAVETTY.createNodeProp(this));
	set.put(DebuggerOption.RUN_SETPGRP.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName("Multi-Threading"); // NOI18N
	set.setDisplayName("Multi-Threading"); // FIXUP I18N
	set.setShortDescription("Multi-Threading"); // FIXUP I18N
	set.put(DebuggerOption.MT_SYNC_TRACKING.createNodeProp(this));
	set.put(DebuggerOption.MT_SCALABLE.createNodeProp(this));
	set.put(DebuggerOption.MT_RESUME_ONE.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName("Follow Fork"); // NOI18N
	set.setDisplayName("Follow Fork"); // FIXUP I18N
	set.setShortDescription("Follow Fork"); // FIXUP I18N
	set.put(DebuggerOption.FOLLOW_FORK_MODE.createNodeProp(this));
	// OLD See 6573955
	// OLD set.put(DebuggerOption.FOLLOW_FORK_INHERIT.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName("Stack Presentation"); // NOI18N
	set.setDisplayName("Stack Presentation"); // FIXUP I18N
	set.setShortDescription("Stack Presentation"); // FIXUP I18N
	set.put(DebuggerOption.OUTPUT_SHORT_FILE_NAME.createNodeProp(this));
	set.put(DebuggerOption.STACK_VERBOSE.createNodeProp(this));
	set.put(DebuggerOption.STACK_MAX_SIZE.createNodeProp(this));
	set.put(DebuggerOption.STACK_FIND_SOURCE.createNodeProp(this));
	sheet.put(set);

	set = new Sheet.Set();
	set.setName("Debugging Behaviour"); // NOI18N
	set.setDisplayName("Debugging Behaviour"); // FIXUP I18N
	set.setShortDescription("Debugging Behaviour"); // FIXUP I18N
	set.put(DebuggerOption.STEP_EVENTS.createNodeProp(this));
	set.put(DebuggerOption.STEP_GRANULARITY.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName("Logging"); // NOI18N
	set.setDisplayName("Logging"); // FIXUP I18N
	set.setShortDescription("Logging"); // FIXUP I18N
	set.put(DebuggerOption.OUTPUT_LOG_FILE.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_LOG_FILE_NAME.createNodeProp(this));
	set.put(DebuggerOption.SESSION_LOG_FILE.createNodeProp(this));
	set.put(DebuggerOption.SESSION_LOG_FILE_NAME.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName("Debugging Performance"); // NOI18N
	set.setDisplayName("Debugging Performance"); // FIXUP I18N
	set.setShortDescription("Debugging Performance"); // FIXUP I18N
	set.put(DebuggerOption.STACK_VERBOSE.createNodeProp(this));
	set.put(DebuggerOption.STACK_MAX_SIZE.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_MAX_OBJECT_SIZE.createNodeProp(this));
//	set.put(DebuggerOption.RTC_USE_TRAPS.createNodeProp(this));
	set.put(DebuggerOption.OUTPUT_AUTO_FLUSH.createNodeProp(this));
	set.put(DebuggerOption.SYMBOL_INFO_COMPRESSION.createNodeProp(this));
	set.put(DebuggerOption.MT_SCALABLE.createNodeProp(this));
	sheet.put(set);
	
	set = new Sheet.Set();
	set.setName("Back Doors"); // NOI18N
	set.setDisplayName("Back Doors"); // FIXUP I18N
	set.setShortDescription("Back Doors"); // FIXUP I18N
	set.put(DebuggerOption.FIX_VERBOSE.createNodeProp(this));
	set.put(DebuggerOption.POP_AUTO_DESTRUCT.createNodeProp(this));
	set.put(DebuggerOption.DISSASSEMLER_VERSION.createNodeProp(this));
	sheet.put(set);
	*/

	return sheet;
    }
}
