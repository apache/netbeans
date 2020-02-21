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
