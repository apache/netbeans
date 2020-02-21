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

import com.sun.tools.swdev.glue.dbx.*;

import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.debugger.common2.debugger.Frame;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;

final class DbxFrame extends Frame {
    private final int dbx_frameno;

    public DbxFrame(DbxDebuggerImpl debugger, GPDbxFrame frame, Thread thread) {
	super(debugger, thread);
	range_of_hidden = frame.range_of_hidden;
	current = frame.current;
	func = frame.func;
	loadobj = frame.loadobj;
	loadobj_base = frame.loadobj_base;
	args = frame.args;
	source = frame.source;
	dbx_frameno = frame.frameno;
	frameno = Integer.toString(frame.frameno);
	lineno = Integer.toString(frame.lineno);
	pc = Address.toHexString0x(frame.pc, true);
	optimized = frame.optimized;
	attr_user_call = frame.attr_user_call;
	attr_sig = frame.attr_sig;
	attr_signame = frame.attr_signame;
    }

     public int getFrameNo() {
	return dbx_frameno;
    }
     
     public void setThread(Thread thread) {
         this.thread = thread;
     }
}
