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

import org.netbeans.spi.viewmodel.ModelListener;

import com.sun.tools.swdev.glue.dbx.*;
import java.math.BigInteger;
import javax.swing.SwingUtilities;

import org.netbeans.modules.cnd.debugger.common2.debugger.Address;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;

public final class DbxThread extends Thread {

    private final GPDbxThread thread;
    int tid;
    int htid;

    /**
     * Create a new Thread
     * <p>
     * 'thread' may be 'null, making this be a dummy placeholder thread.
     */

    DbxThread(NativeDebugger debugger, ModelListener updater, GPDbxThread thread) {
   	super(debugger, updater);

	if (thread == null) {
	    // Assign a dummy so we don't have to check for null all over.
	    // SHOULD assign a better-fleshed out dummy
	    thread = new GPDbxThread();
	}
        
	this.thread = thread;
	tid = thread.tid;
	htid = thread.htid;
	current_function = thread.current_function;
	address = Address.toHexString0x(thread.address, true);
	current = thread.current;
    }

    // implements NativeThread
    public String getName() {
	return tidToString(getId());
    }

    // implements NativeThread
    public long getId() {
	long ltid = htid;

	if (htid == 0 && tid > -6 && tid < 0) {
	    // sign extend
    	    ltid = tid;
	} else {
	    ltid <<= 32 ;
	    ltid |= (0x00000000ffffffffL & tid) ;
	}
	return ltid;
    }

    public boolean hasEvent() {
	return thread.event != GPDbxThreadEvent.NONE;
    } 
    
    /*
     * Property accessors for use by Nodes
     */

    public String getState() {
	if (thread.stop_reason != null)
	    return thread.stop_reason;

	switch (thread.state) {
	    case GPDbxThread.UNKNOWN:
		return Catalog.get("ThreadState_UNKNOWN");	// NOI18N
	    case GPDbxThread.ACTIVE:
		return Catalog.get("ThreadState_ACTIVE");	// NOI18N
	    case GPDbxThread.SLEEP:
		return Catalog.get("ThreadState_SLEEP");	// NOI18N
	    case GPDbxThread.RUN:
		return Catalog.get("ThreadState_RUN");		// NOI18N
	    case GPDbxThread.SUSPENDED:
		return Catalog.get("ThreadState_SUSPENDED");	// NOI18N
	    case GPDbxThread.ZOMBIE:
		return Catalog.get("ThreadState_ZOMBIE");	// NOI18N
	    default:
		return Catalog.get("ThreadState_DEFAULT");	// NOI18N
	}
    }

    @Override
    public void resume() {
        SwingUtilities.invokeLater(new Runnable() { // TODO better way to implement
            public void run() {
                DbxThread.super.resume();
            }
        });
    }

    public boolean isSuspended() {
	// dbx currently doesn't support thread suspension and resumption
	// So this will always be false
//	return thread.db_suspended;
        return !getState().trim().equals("running");	// NOI18N
    }

    public Integer getPriority() {
	// glue doesn't supply this property
	return new Integer(-1);
    }

    public String getLWP() {
	switch ((char) thread.lrelation) {
	    case 'a':
		return "a l@" + Integer.toString(thread.lid);	// NOI18N
	    case 'b':
		return "b l@" + Integer.toString(thread.lid);	// NOI18N
	    default:
		return "";
	}
    }

    public String getStartupFlags() {
	return "<unknown>";		// glue doesn't supply this property // NOI18N
    }

    public String getStartFunction() {
	if (thread.root_function == null) {
	    return "<unknown>"; // NOI18N
	} else  {
	    return thread.root_function;
	} 
    }

    public Integer getStackSize() {
	// glue doesn't supply this property
	return new Integer(-1);
    }

    /////////////////////////////////////////////////////////////////////
    // The following methods return (lazily created) objects suitable
    // for displaying the thread in a table
    /////////////////////////////////////////////////////////////////////
    
    /**
     * Convert a dbx thread id to a user string
     */
    public static String tidToString(long id) {
	// See bug 4351957 (a 'null' lwp is shown in many ways)
	long lid = id;
	

	if (lid == -1)
	    return "t@null";   // NOI18N
	else if (lid == -2)
	    return "t@all";    // NOI18N
	else if (lid == -3)
	    return "t@X";      // NOI18N
	else if (lid == -4)
	    return "t@?";      // NOI18N
	else if (lid == -5)
	    return "t@idle";   // NOI18N
        else {
            if (lid < 0) {
                BigInteger max = BigInteger.ONE.shiftLeft(64);
                BigInteger bi = BigInteger.valueOf(lid);
        	return "t@" + bi.add(max).toString(); // NOI18N
            } else {
                return "t@" + Long.toString(lid); // NOI18N
            }
        }
    }

    /**
     * Convert a dbx LWP id to a user string
     */
    public static String lidToString(byte lrelation, int id) {
	if (!(lrelation == 'a' ||
	      lrelation == 'b')) {
	    return ""; // NOI18N
	} else {
	    // LWP id
	    return "l@" + id;  // NOI18N
	}
    }

    @Override
    public String getFile() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public String getLine() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}
