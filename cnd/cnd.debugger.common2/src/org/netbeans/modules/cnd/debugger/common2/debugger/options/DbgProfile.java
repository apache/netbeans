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

/*
 * "DbgProfile.java"
 * Common code for DbxProfile and GdbProfile:
 *   - deal with common properties of dbx and gdb, like pathmap, signal,
 *     exceptions, output redirection, host etc.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import java.beans.PropertyChangeSupport;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetOwner;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.openide.util.Utilities;

public abstract class DbgProfile extends ProfileSupport implements OptionSetOwner {

    public static final String PROP_OPTIONS = "options";        // NOI18N

    public static final String PROP_INTERCEPTLIST = "interceptlist";// NOI18N
    public static final String PROP_SIGNALS = "signals";	// NOI18N
    public static final String PROP_PATHMAP = "pathmap";	// NOI18N

    protected OptionSet options;
    protected boolean savedBuildFirst;
    protected boolean buildFirstOverriden;
    
    private final Configuration config;
    
    /**
     * Constructor
     * Don't call this directly. It will get called when creating
     * ...cnd.execution.profiles.Profile().
     */
    public DbgProfile() {
        config = null;
    }

    protected DbgProfile(PropertyChangeSupport pcs, final Configuration configuration) {
	super(pcs);
        this.config = configuration;
    }
    
    /**
     * Initializes the object to default values
     */
    @Override
    public void initialize() {
    }

    @Override
    public OptionSet getOptions() {
	return options;
    }
    
    protected final void notifyOptionsChange() {
	// clones don't have a pcs
	if (pcs != null)
	    pcs.firePropertyChange(PROP_OPTIONS, null, null);
	needSave = true;
    }
    
    protected Signals signals;
    protected Exceptions exceptions;
    protected Pathmap pathmap;

    public Pathmap pathmap() {
	return pathmap;
    }

    public Exceptions exceptions() {
	return exceptions;
    }

    public Signals signals() {
	return signals;
    }
    
    public Configuration getConfiguration() {
        return config;
    }

    protected String redirection;

    /**
     * @return Return the redirection in effect for the program.
     */
    public String getRedirection() {
	return redirection;
    }
    
    /**
     * Set the output redirection for this program
     * @param infile Input file
     * @param outfile Output file
     * @param append Append to outputfile?
     */
    public void setRedirection(String infile, String outfile,
			       boolean append) {
	StringBuilder sb = new StringBuilder();
	boolean in = (infile != null && infile.length() != 0);
	if (in) {
	    sb.append("< ");		// NOI18N
	    sb.append(infile);
	}
	if (outfile != null && outfile.length() != 0) {
	    if (in) {
		sb.append(" ");		// NOI18N
	    }
	    if (append) {
		sb.append(">> ");	// NOI18N
	    } else {
		sb.append("> ");	// NOI18N
	    }
	    sb.append(outfile);
	}

	if (sb.length() != 0) {
	    redirection = sb.toString();
	    needSave = true;
	}
    }

    // Host
    protected String host;

    public String getDefaultHost() {
	return "localhost"; // NOI18N
    }

    public String getHost() {
	return host;
    }

    public void setHost(String host) {
	this.host = host;
	needSave = true;
    }

    public boolean isDefaultHost() {
	return getHost().equals(getDefaultHost());
    }

    public void setBuildFirstOverriden(boolean buildFirstOverriden) {
	this.buildFirstOverriden = buildFirstOverriden;
	needSave = true;
    }

    public boolean isBuildFirstOverriden() {
	return buildFirstOverriden;
    }

    public boolean isSavedBuildFirst() {
	return savedBuildFirst;
    }

    public void setSavedBuildFirst(boolean savedBuildFirst) {
	this.savedBuildFirst = savedBuildFirst;
	needSave = true;
    }
    
    private String[] getDebugCommand() {
        String command = DebuggerOption.DEBUG_COMMAND.getCurrValue(options);
        if (config != null && config instanceof MakeConfiguration) {
            return Utilities.parseParameters(
                     ProjectActionEvent.getRunCommandAsString(
                        command, 
                        (MakeConfiguration) config, 
                        NativeDebuggerImpl.getPathMapFromConfig(config)
                    )
            );
        } else {
            return Utilities.parseParameters("");
        }
    }

    public String getExecutable() {
        String[] debugCommand = getDebugCommand();
        
        if (debugCommand.length == 0) {
            return "";
        }
        return debugCommand[0];
    }

    public String getArgsFlat() {
        String[] params = getDebugCommand();
        StringBuilder retVal = new StringBuilder();
        if (params.length > 1) {
            for(int i = 1; i < params.length; i++) {
                retVal.append("\"").append(params[i]).append("\" ");   // NOI18N
            }
        }
        return retVal.toString();
    }
    
    public String getDebugDir() {
        return DebuggerOption.DEBUG_DIR.getCurrValue(options);
    }
    
    public void setDebugDir(String value) {
        DebuggerOption.DEBUG_DIR.setCurrValue(options, value);
    }
}
