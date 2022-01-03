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

package org.netbeans.modules.cnd.debugger.gdb2;

import org.netbeans.modules.cnd.debugger.common2.debugger.Error;
import org.netbeans.modules.cnd.debugger.common2.debugger.Error.Msg;
import org.netbeans.modules.cnd.debugger.common2.debugger.Error.Severity;

public final class GdbError extends Error {

    private static Msg[] msgs(String errMsg) {
	Msg msgs[] = new Msg[1];
        msgs[0] = new Msg(Severity.ERROR, errMsg, null);
	return msgs;
    }

    public GdbError(String command, String errMsg) {
	super(msgs("command:\"" + command + "\"; error, msg=" + errMsg));//NOI18N
    }
}
