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

package org.netbeans.modules.cnd.classview;

import java.io.*;
import org.netbeans.modules.cnd.modelutil.Tracer;

/**
 *
 */
public class Diagnostic {
    
    public static final boolean DEBUG = Boolean.getBoolean("cnd.classview.trace"); // NOI18N
    public static final boolean DUMP_MODEL = Boolean.getBoolean("cnd.classview.dumpmodel"); // NOI18N
    
    private static Tracer tracer = new Tracer(System.err);
    
    public static void trace(Object arg) {
	if( DEBUG ) {
	    tracer.trace(arg);
	}
    }
    
    public static void indent() {
	tracer.indent();
    }
    
    public static void unindent() {
	tracer.unindent();
    }
    
    public static void traceStack(String message) {
	if( DEBUG ) {
	    trace(message);
	    StringWriter wr = new StringWriter();
	    new Exception(message).printStackTrace(new PrintWriter(wr));
	    //StringReader sr = new StringReader(wr.getBuffer().toString());
	    BufferedReader br = new  BufferedReader(new StringReader(wr.getBuffer().toString()));
	    try {
		br.readLine(); br.readLine();
		for( String s = br.readLine(); s != null; s = br.readLine() ) {
		    trace(s);
		}
	    } catch( IOException e ) {
		e.printStackTrace(System.err);
	    }
	}
    }
}
