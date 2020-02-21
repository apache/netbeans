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

import java.io.*;

import org.openide.execution.ExecutorTask;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.NbProcessDescriptor;
import org.openide.windows.InputOutput;


import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;


/**
 *  Executes a Make target asynchronously in the IDE.
 */
class FixExecutor implements Runnable {

    /** Fix file */
    private String file;
    
    /** Fix output file */
    private String errfile;
    
    /** I/O class for writing output to a build tab */
    private final InputOutput io;

    /** The BuildCustomizer */
    // Jean private BuildCustomizer customizer;

    /** Store all BuildListeners here */
    // Jean private ArrayList listeners = new ArrayList(5);

    /** Name of the tab in the Output window */
    private final String tabName;

    /** Private PrintStream to output window */
    private PrintStream out;

    /** targets may be null to indicate default target */
    public FixExecutor() {
	//customizer = MakeExecSupport.getBuildCustomizer();
	tabName = Catalog.get("LBL_FixTab"); // NOI18N
	out = null;

        synchronized (this) {
	    // Jean io = IOProvider.getDefault().getIO(tabName, false);
	    io = NativeDebuggerManager.get().getIO();
	    // TODO
	    if (io != null) {
// CR 7082759	io.setFocusTaken(true);
		io.setErrVisible(false);
		io.setErrSeparated(false);
	    }
	    //customizer.customizeIO(io);	// override default behavior
	}
    }


    /** Start it going. */
    @SuppressWarnings("deprecation")
    public ExecutorTask compile(String errfile) {
        final ExecutorTask task;

	this.errfile = errfile;

        synchronized (this) {
	    if (out == null) {
		// make Output window and Fix tab visible
		io.select();
		// Jean reset();
		// NB69 out = new PrintStream(new OutputWindowOutputStream(io.getOut()));
	    }

            task = ExecutionEngine.getDefault().execute(tabName, this, InputOutput.NULL);
        }

        return task;
    }


  
    /**
     *  Call execute(), not this method directly!
     */

    // interface Runnable
    synchronized public void run() {
        /* Jean
	addBuildListener(customizer.getBuildListener());
        
	FixLogger logger = new FixLogger();
	logger.setOutputPrintStream(out);
	addBuildListener((BuildListener) logger);

	fireBuildStarted(Catalog.format("MSG_FixStarted", file));
	*/

        // Save & restore system output streams.
        PrintStream sysout = System.out;
        PrintStream syserr = System.err;
        try {
            // Execute a cat command of the fix output
            displayFixOutput(io);
        } catch (Throwable t) {
	    // Problem displaying fix output, doesn't mean fix failed.
	    // Jean fireBuildFinished(Catalog.format("MSG_FixFault", file));
        } finally {
            System.setOut(sysout);
            System.setErr(syserr);
	    /* Jean
	    removeBuildListener(logger);

	    BuildListener l = customizer.getBuildListener();
	    if (l != null) {
		removeBuildListener(l);
	    }
	    */
	
	    if (errfile != null) {
		File f = new File(errfile);
		if (f.exists()) {
		    f.delete();
		}
	    }
        }
    }


    /** Generate and run the cat command to output the fix file */
    private void displayFixOutput(InputOutput io)
				throws IOException, InterruptedException {

	NbProcessDescriptor desc = new NbProcessDescriptor("/usr/bin/cat",  // NOI18N
			     errfile, Catalog.get("HINT_FixProcessDescriptor")); // NOI18N

	// Start the build process and a build reader.
	Process proc = desc.exec();
	FixReaderThread t = new FixReaderThread(proc.getInputStream(), out);
	t.start();

	proc.waitFor();
	t.join();	    // wait for the thread to complete
    }


    /** Save the file */
    public void setFile(String file) {
	this.file = file;
    }


    /** Clear the tab panel for a new fix command */
    /* Jean
    private void reset() {
	try {
	    customizer.reset(io);
	} catch (IOException ex) {
	    // ignore
	}
    }
    */


    /** Destroy the errfile and clear out for the next fix command */
    public void done(String msg) {

	if (out != null) {
	    // out can equal null if a fix is requested but no files need fixing
	    // Jean out.println("<B>" + msg + "</B>");  // NOI18N
	    out.println(msg);  // NOI18N
	    out = null;
	}
    }


/* Jean
    public void addBuildListener(BuildListener l) {

	if (l != null) {
	    listeners.add(l);
	}
    }


    public void removeBuildListener(BuildListener l) {
	listeners.remove(listeners.indexOf(l));
    }


    public void fireBuildStarted(String msg) {
	BuildEvent event = new BuildEvent(this);

	event.setMessage(msg);
	for (int i = 0; i < listeners.size(); i++) {
	    BuildListener listener = (BuildListener) listeners.get(i);
	    listener.buildStarted(event);
	}
    }


    public void fireBuildFinished(String msg) {
	BuildEvent event = new BuildEvent(this);

	event.setMessage(msg);
	for (int i = 0; i < listeners.size(); i++) {
	    BuildListener listener = (BuildListener) listeners.get(i);
	    listener.buildFinished(event);
	}
    }


    public void fireBuildEvent(String msg) {
	BuildEvent event = new BuildEvent(this);

	event.setMessage(msg);
	for (int i = 0; i < listeners.size(); i++) {
	    BuildListener listener = (BuildListener) listeners.get(i);
	    listener.messageLogged(event);
	}
    }
    
*/

    /** Helper class to read the input from the build */
    public final class FixReaderThread extends java.lang.Thread {

	/** This is all output, not just stdout */
	private final InputStream input;
	private final PrintStream out;

	public FixReaderThread(InputStream input, PrintStream out) {
	    this.input = input;	    // input is a FileInputStream
	    this.out = out;	    // out is a PrintStream
	}

	/**
	 *  Reader proc to read the combined stdout and stdout from the build process.
	 */
	public void run() {
	    BufferedReader in = new BufferedReader(new InputStreamReader(input));
	    String line;
	    String fmsg;

	    try {
		while ((line = in.readLine()) != null) {
	            out.println(line);  // NOI18N
		    /* Jean
		    fmsg = customizer.filterEvent(line);
		    // A customizer returns null for unwanted lines, so check for null
		    if (fmsg != null) {
			fireBuildEvent(fmsg);
		    }
		    */
		}
		in.close();
	    } catch (IOException ioe) {
	    }
	}
    }
}
