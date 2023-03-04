/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide;

import java.io.*;
import java.util.*;
import org.netbeans.performance.Benchmark;

public class ErrorManagerTest extends Benchmark {

    static {
	Properties prop = System.getProperties();
        prop.put("perf.test.enabled", "-5");
//        prop.put("perf.test.disabled", "0x1000000");
    }

    private static final ErrorManager enabled;
    private static final ErrorManager disabled;

    static {
        ErrorManager en = ErrorManager.getDefault().getInstance("perf.test.enabled");
        enabled = en.isLoggable(ErrorManager.INFORMATIONAL) ? en : null;
        ErrorManager dis = ErrorManager.getDefault().getInstance("perf.test.disabled");
        disabled = dis.isLoggable(ErrorManager.INFORMATIONAL) ? dis : null;
	assertNull("disabled is loggable", disabled);
    }
    
    
    public ErrorManagerTest(String name) {
        super( name );
    }
 
    public void testLogEnabled() throws Exception {
        int count = getIterationCount();
        ErrorManager en = ErrorManager.getDefault().getInstance("perf.test.enabled");

        while( count-- > 0 ) {
            // do the stuff here, 
	    en.log("Logging event #" + count);
        }
    }    

    public void testLogDisabled() throws Exception {
        int count = getIterationCount();
        ErrorManager dis = ErrorManager.getDefault().getInstance("perf.test.disabled");

        while( count-- > 0 ) {
            // do the stuff here, 
	    dis.log("Logging event #" + count);
        }
    }
    
    public void testCheckedEnabled() throws Exception {
        int count = getIterationCount();
        ErrorManager en = ErrorManager.getDefault().getInstance("perf.test.enabled");

        while( count-- > 0 ) {
            // do the stuff here, 
	    if(en.isLoggable(ErrorManager.INFORMATIONAL)) en.log("Logging event #" + count);
        }
    }    

    public void testCheckedDisabled() throws Exception {
        int count = getIterationCount();
        ErrorManager dis = ErrorManager.getDefault().getInstance("perf.test.disabled");

        while( count-- > 0 ) {
            // do the stuff here, 
	    if(dis.isLoggable(ErrorManager.INFORMATIONAL)) dis.log("Logging event #" + count);
        }
    }    

    public void testNullEnabled() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
            // do the stuff here, 
	    if(enabled != null) enabled.log("Logging event #" + count);
        }
    }    

    public void testNullDisabled() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
            // do the stuff here, 
	    if(disabled != null) disabled.log("Logging event #" + count);
        }
    }    

    public void testNull16Disabled() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
            // do the stuff here, 
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
	    if(disabled != null) disabled.log("Logging event #" + count);
        }
    }    

    public static void main(String[] args) {
	simpleRun( ErrorManager.class );
    }

    /** Crippled version of NbErrorManager that does the logging the same way */
    public static final class EM extends ErrorManager {
	/** The writer to the log file*/
	private PrintWriter logWriter = new PrintWriter(System.err);
    
	/** Minimum value of severity to write message to the log file*/
	private int minLogSeverity = ErrorManager.INFORMATIONAL + 1; // NOI18N

	/** Prefix preprended to customized loggers, if any. */
	private String prefix = null;
    
	// Make sure two distinct EM impls log differently even with the same name.
	private int uniquifier = 0; // 0 for root EM (prefix == null), else >= 1
	static final Map uniquifiedIds = new HashMap(20); // Map<String,Integer>
    
    
	/** Initializes the log stream.
         */
	private PrintWriter getLogWriter () {
	    return logWriter;
        }

	public synchronized Throwable annotate (
    	    Throwable t,
    	    int severity, String message, String localizedMessage,
    	    Throwable stackTrace, java.util.Date date
	) {
	    return t;
	}


	/** Associates annotations with this thread.
	 *
	 * @param arr array of annotations (or null)
	 */
	public synchronized Throwable attachAnnotations (Throwable t, Annotation[] arr) {
	    return t;
	}

	/** Notifies all the exceptions associated with
	 * this thread.
	 */
	public synchronized void notify (int severity, Throwable t) {
	}

	public void log(int severity, String s) {
    	    if (isLoggable (severity)) {
        	PrintWriter log = getLogWriter ();
            
        	if (prefix != null) {
            	    boolean showUniquifier;
            	    // Print a unique EM sequence # if there is more than one
            	    // with this name. Shortcut: if the # > 1, clearly there are.
            	    if (uniquifier > 1) {
                	showUniquifier = true;
            	    } else if (uniquifier == 1) {
                	synchronized (uniquifiedIds) {
                    	    int count = ((Integer)uniquifiedIds.get(prefix)).intValue();
                    	    showUniquifier = count > 1;
                	}
            	    } else {
                	throw new IllegalStateException("prefix != null yet uniquifier == 0");
            	    }
            	    if (showUniquifier) {
                	log.print ("[" + prefix + " #" + uniquifier + "] "); // NOI18N
            	    } else {
                	log.print ("[" + prefix + "] "); // NOI18N
            	    }
        	}
        	log.println(s);
        	log.flush();
    	    }
	}
    
	/** Allows to test whether messages with given severity will be logged
         * or not prior to constraction of complicated and time expensive
         * logging messages.
         *
         * @param severity the severity to check
         * @return false if the next call to log method with the same severity will
         *    discard the message
         */
	public boolean isLoggable (int severity) {
    	    return severity >= minLogSeverity;
	}
    
    
	/** Returns an instance with given name. The name
         * can be dot separated list of names creating
	 * a hierarchy.
	 */
	public final ErrorManager getInstance(String name) {
    	    EM newEM = new EM();
    	    newEM.prefix = (prefix == null) ? name : prefix + '.' + name;
    	    synchronized (uniquifiedIds) {
        	Integer i = (Integer)uniquifiedIds.get(newEM.prefix);
        	if (i == null) {
            	    newEM.uniquifier = 1;
        	} else {
            	    newEM.uniquifier = i.intValue() + 1;
        	}
        	uniquifiedIds.put(newEM.prefix, new Integer(newEM.uniquifier));
    	    }
    	    newEM.minLogSeverity = minLogSeverity;
    	    String prop = newEM.prefix;
    	    while (prop != null) {
        	String value = System.getProperty (prop);
        	//System.err.println ("Trying; prop=" + prop + " value=" + value);
        	if (value != null) {
            	    try {
                	newEM.minLogSeverity = Integer.parseInt (value);                    
            	    } catch (NumberFormatException nfe) {
                	notify (WARNING, nfe);
            	    }
                break;
            } else {
                int idx = prop.lastIndexOf ('.');
                if (idx == -1)
                    prop = null;
                else
                    prop = prop.substring (0, idx);
        	}
    	    }
    	    //System.err.println ("getInstance: prefix=" + prefix + " mls=" + minLogSeverity + " name=" + name + " prefix2=" + newEM.prefix + " mls2=" + newEM.minLogSeverity);
    	    return newEM;
	}    
    

	/** Finds annotations associated with given exception.
	 * @param t the exception
	 * @return array of annotations or null
	 */
	public synchronized Annotation[] findAnnotations (Throwable t) {
	    return new Annotation[0];
	}

	public String toString() {
    	    return super.toString() + "<" + prefix + "," + minLogSeverity + ">"; // NOI18N
	}
    }
}
