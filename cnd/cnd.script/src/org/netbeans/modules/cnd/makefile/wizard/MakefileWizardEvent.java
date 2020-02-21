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

package  org.netbeans.modules.cnd.makefile.wizard;

import java.util.EventObject;

public class MakefileWizardEvent extends EventObject {
    /** Identifies one or more changes in the lists contents. */
    static int MAKEFILE_NEW = 0;

    /** * The type of this event; */
    private int type;

    private String makefilePath = null;

    private String buildDirectory = null;

    private String makeCommand = null;

    private String[] targets = null;

    private String[] executables = null;

    /**
     * Constructs a PicklistDataEvent object.
     *
     * @param source  the source Object (typically <code>this</code>)
     * @param type    an int specifying {@link #CONTENTS_CHANGED}
     */
    public MakefileWizardEvent(
	    Object source,
	    int type,
	    String makefilePath,
	    String buildDirectory,
	    String makeCommand,
	    String[] targets,
	    String[] executables) {
	super(source);
	this.type = type;
	this.makefilePath = makefilePath;
	this.buildDirectory = buildDirectory;
	this.makeCommand = makeCommand;
	this.targets = targets;
	this.executables = executables;
    }

    /**
     * Returns the event type. The possible values are:
     * <ul>
     * <li> {@link #CONTENTS_CHANGED}
     * </ul>
     *
     * @return an int representing the type value
     */
    public int getType() {
	return type;
    }

    public String getMakefilePath() {
	return makefilePath;
    }

    public String getBuildDirectory() {
	return buildDirectory;
    }

    public String getMakeCommand() {
	return makeCommand;
    }

    public String[] getTargets() {
	return targets;
    }

    public String[] getExecutables() {
	return executables;
    }
}
