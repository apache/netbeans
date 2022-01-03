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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import org.netbeans.modules.cnd.debugger.common2.values.StringEditor;

/*
 * Convenience container for individual ColumnModels specified as inner classes.
 *
 * Registered in
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/SessionsView/
 *	org.netbeans.spi.viewmodel.ColumnModel
 *	NOTE: Use '...debugger.SessionColumnModels$PID'
 */

public final class SessionColumnModels {

    public static final class PID extends AbstractColumnModel {
	public PID() {
	    super(Constants.PROP_SESSION_PID,
		  Catalog.get("PROP_session_pid"), Catalog.get("PROP_session_pid"), // NOI18N
		  Long.class, true, new StringEditor());
	}
    }

    public static final class State extends AbstractColumnModel {
	public State() {
	    super(Constants.SESSION_STATE_COLUMN_ID,
		  Catalog.get("PROP_session_state"), Catalog.get("HINT_session_state"), // NOI18N
		  String.class, true, new StringEditor());
	}
    }

    public static final class Debugger extends AbstractColumnModel {
	public Debugger() {
	    super(Constants.SESSION_DEBUGGER_COLUMN_ID,
		  Catalog.get("PROP_session_debugger"), Catalog.get("HINT_session_debugger"), // NOI18N
		  String.class, false, new StringEditor());
	}
    }

    public static final class Location extends AbstractColumnModel {
	public Location() {
	    super(Constants.PROP_SESSION_LOCATION,
		  Catalog.get("PROP_session_location"), Catalog.get("HINT_session_location"), // NOI18N
		  String.class, false, null);
		  //String.class, false, new StringEditor());
	}
    }

    public static final class Mode extends AbstractColumnModel {
	public Mode() {
	    super(Constants.PROP_SESSION_MODE,
		  Catalog.get("PROP_session_mode"), Catalog.get("HINT_session_mode"), // NOI18N
		  String.class, false, new StringEditor());
	}
    }

    public static final class Args extends AbstractColumnModel {
	public Args() {
	    super(Constants.PROP_SESSION_ARGS,
		  Catalog.get("PROP_session_args"), Catalog.get("HINT_session_args"), // NOI18N
		  String.class, false, new StringEditor());
	}
    }
    
    public static final class Core extends AbstractColumnModel {
	public Core() {
	    super(Constants.PROP_SESSION_CORE,
		  Catalog.get("PROP_session_core"), Catalog.get("HINT_session_core"), // NOI18N
		  String.class, false, null);
		  //String.class, false, new StringEditor());
	}
    }

    public static final class Host extends AbstractColumnModel {
	public Host() {
	    super(Constants.PROP_SESSION_HOST,
		  Catalog.get("PROP_session_host"), Catalog.get("HINT_session_host"), // NOI18N
		  String.class, true, new StringEditor());
	}
    }

}
