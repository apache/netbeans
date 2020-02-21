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

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.common2.debugger.Error;
import org.netbeans.modules.cnd.debugger.common2.debugger.Error.Msg;
import com.sun.tools.swdev.glue.dbx.GPDbxError;
import com.sun.tools.swdev.glue.dbx.GPDbxSeverity;

public final class DbxError extends Error {

    private static Msg[] msgs(GPDbxError errors[]) {
	Msg msgs[] = new Msg[errors.length];
	for (int ex = 0; ex < errors.length; ex++) {
	    final Severity severity;
	    switch (errors[ex].severity) {
		default:
		case GPDbxSeverity.NONE:
		    severity = Error.Severity.NONE;
		    break;
		case GPDbxSeverity.WARNING:
		    severity = Error.Severity.WARNING;
		    break;
		case GPDbxSeverity.ERROR:
		    severity = Error.Severity.ERROR;
		    break;
	    }
	    msgs[ex] = new Msg(severity, errors[ex].msg, errors[ex].cookie);
	}
	return msgs;
    }

    public DbxError(GPDbxError errors[]) {
	super(msgs(errors));
    }
}
