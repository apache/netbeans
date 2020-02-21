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

class FixLogger {
}
//import java.io.*;
//
//import org.openide.util.NbBundle;
//
//import org.netbeans.modules.cnd.builds.BuildEvent;
//import org.netbeans.modules.cnd.builds.BuildListener;
//
//
///**
// *  Fix and Continue build logger.
// */
//public class FixLogger implements BuildListener {
// 
//    private PrintStream out;
//    
//    //static public PrintWriter dbout;		    // XXX - Debug
//
//
//    /** Set the output print stream for the fix */
//    public void setOutputPrintStream(PrintStream ps) {
//        out = ps;
//    }
//    
//    
//    /** Ignored */
//    public void setErrorPrintStream(PrintStream ps) {
//    }
//    
//
//    /** Start displaying fix output */
//    public void buildStarted(BuildEvent ev) {
//	String msg;
//
//	//try {					    // XXX - Debug code
//	    //dbout = new PrintWriter(new FileWriter("/dev/tty"), true);
//	//} catch (IOException ex) {};
//
//	if (ev != null && (msg = ev.getMessage()) != null) {
//	    out.println(msg);
//	}
//    }
//    
//
//    /** Done displaying fix output */
//    public void buildFinished(BuildEvent ev) {
//	//dbout.close();
//    }
//    
//
//    /** Log a message */
//    public void messageLogged(BuildEvent ev) {
//	out.println(ev.getMessage());
//	//dbout.println("Log: " + ev.getMessage());
//    }
//}
