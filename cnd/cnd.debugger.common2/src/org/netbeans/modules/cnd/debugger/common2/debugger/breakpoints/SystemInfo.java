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


package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.util.*;
import java.io.*;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.DefaultComboBoxModel;

import org.openide.util.RequestProcessor;

public abstract class SystemInfo {

    protected Vector<String> items;
    protected String cmd;
    protected String ignore;
    private static final RequestProcessor RP = new RequestProcessor("SystemInfo", 1); // NOI18N

    protected SystemInfo(Vector<String> items, String cmd, String ignore) {
	this.items = items;
	this.cmd = cmd;
	this.ignore = ignore;
    }

    public abstract String all();

    public void stuffIntoAsync(final JComboBox combo) {
	RP.post(new Runnable () {
            @Override
	    public void run () {
		stuffInto(combo);
	    }
	}, 100);
    }

    public void stuffInto(final JComboBox combo) {
	fill();

	if (items.isEmpty())
	    return;

	final DefaultComboBoxModel model;
	model = new DefaultComboBoxModel(new Vector<String>(items));
	SwingUtilities.invokeLater(new Runnable () {
            @Override
	    public void run () {
		combo.setModel(model);
	    }
	});
    }

    /**
     * Run a command to find a set of items to add to a Vector, skipping
     * a given string match
     */

    public void fill() {
	Runtime rt = Runtime.getRuntime();
	try {
	    String [] args = new String[3];
	    args[0] = "/bin/sh"; // NOI18N
	    args[1] = "-c"; // NOI18N
	    args[2] = cmd; 
	    
	    Process proc = rt.exec(args);
	    InputStream procIn = proc.getInputStream();
	    BufferedReader br = new BufferedReader(new InputStreamReader(procIn, "UTF-8")); // NOI18N
	    while (true) {
		String moreoutput = br.readLine();
		if (moreoutput == null) {
		    break;
		} else {
		    if (moreoutput.endsWith("_H")) { // NOI18N
			// Known to not be a signal, fault, syscall, etc. but
			// sometimes erroneously included by the headerfile
			// greps
			continue;
		    }
		    if (moreoutput.startsWith("reserved_")) {  // NOI18N
			// Known to not be a syscall but is listed in the
			// syscall headerfile, exclude these.
			continue;
		    }
		    
		    if ((ignore == null) || !(ignore.equals(moreoutput))) {
			items.add(moreoutput);
		    }
		}
	    }
	    br.close();
	    proc.waitFor();
	} catch (Exception e) {
	    // e.printStackTrace();
	}
    }

    public static class Signals extends SystemInfo {
	public Signals(Vector<String> items) {
	    super(items, null, null);
	    if (new File("/usr/include/sys/iso/signal_iso.h").exists()) { // NOI18N
		cmd =
"LC_ALL=C /usr/bin/grep \"#define\tSIG.*[0-9].*/\" /usr/include/sys/iso/signal_iso.h | /usr/bin/nawk '{print $2}'"; // NOI18N
	    } else if (new File("/usr/include/asm/signal.h").exists()) { // NOI18N
		// Linux
		cmd =
"LC_ALL=C /bin/grep \"#define SIG.*[0-9]\" /usr/include/asm/signal.h | /bin/awk '{print $2}'"; // NOI18N

	    } else {
		cmd =
"LC_ALL=C /usr/bin/grep \"#define\tSIG.*[0-9].*/\" /usr/include/sys/signal.h | /usr/bin/nawk '{print $2}'"; // NOI18N
	    }
	    //System.out.println("cmd = " + cmd);
	}

        @Override
	public String all() {
	    return Catalog.get("Signal_AllCodes"); // NOI18N
	}
    }

    public static class Subcodes extends SystemInfo {
	public Subcodes(Vector<String> items) {
	    super(items, null, null);
	    items.add(all());
	    if (new File("/usr/include/bits/siginfo.h").exists()) { // NOI18N
		// Linux
	        cmd = 
"LC_ALL=C /bin/grep \"# define.*\" /usr/include/bits/siginfo.h | /bin/awk '{print $3}'"; // NOI18N
	    } else {
	        cmd = 
"LC_ALL=C /usr/bin/grep \"#define.*/\" /usr/include/sys/machsig.h | /usr/bin/nawk '{print $2}'"; // NOI18N
	    }
	    //System.out.println("cmd = " + cmd);
	}

	// override super
        @Override
	public String all() {
	    return Catalog.get("Signal_AllCodes"); // NOI18N
	}

	public Vector<String> subcodesFor(String sig) {
	    if (sig == null)
		return null;
	    sig = sig.trim();
	    if (sig.length() == 0)
		return null;
	    if (items == null || items.size() == 0)
		return null;

	    sig += "_";         // NOI18N
	    if (sig.startsWith("SIG"))  // NOI18N
		sig = sig.substring(3); // everything following SIG

	    Vector<String> actual = new Vector<String>(10);
	    actual.add(all());
	    for (int scx = 0; scx < items.size(); scx++) {
		if (items.get(scx).toString().startsWith(sig))
		    actual.add(items.get(scx));
	    }
	    return actual;
	}
    }

    public static class Faults extends SystemInfo {
	public Faults(Vector<String> items) {
	    super(items, null, "_SYS_FAULT_H");	// NOI18N
	    cmd =
"LC_ALL=C /usr/bin/grep \"#define\" /usr/include/sys/fault.h | /usr/bin/nawk '{print $2}'"; // NOI18N
	}
        @Override
	public String all() {
	    return ""; // NOI18N
	}
    }

    public static class Syscalls extends SystemInfo {
	public Syscalls(Vector<String> items) {
	    super(items, null, "syscall");	// NOI18N
	    items.add(all());
	    cmd =
"LC_ALL=C /usr/bin/grep \"#define\" /usr/include/sys/syscall.h | /usr/bin/nawk '{print $2}' | /usr/bin/cut -c 5-"; // NOI18N
	}
        @Override
	public String all() {
	    return Catalog.get("SysCall_All"); // NOI18N
	}
    }
}

