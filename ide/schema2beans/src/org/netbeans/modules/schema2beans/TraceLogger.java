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

package org.netbeans.modules.schema2beans;

import java.io.*;


/**
 *  schema2beans simple logger implementation
 */

public class TraceLogger extends Object {
    public static int MAXGROUP = 255;

    public static int DEBUG = 1;

    public static int SVC_DD = 1;

    public static PrintStream output = System.out;

    static DDLogFlags flags = new DDLogFlags();

    public static void put(int type, int service, int group, int level,
			   int msg) {
	put(type, service, group, level, msg, null);
    }

    public static void put(int type, int service, int group, int level,
			   int msg, Object obj) {

	String strService = "DD ";	// NOI18N
	String strGroup = flags.dbgNames[group-1];
	String strMsg	= ((String[])(flags.actionSets[group-1]))[msg-1];
	
	if (obj != null) {
	    System.out.println( strService + " " + strGroup + " " +	// NOI18N
	    strMsg + "\t" + obj.toString());// NOI18N
	}
	else {
	    System.out.println( strService + " " + strGroup + " " +	strMsg);// NOI18N
	}
    }
    
    public static void error(String str) {
	output.println(str);
    }
    
    public static void error(Throwable e) {
	output.println("*** ERROR - got the following exception ---");	// NOI18N
	output.println(e.getMessage());
	e.printStackTrace(output);
	output.println("*** ERROR ---------------------------------");	// NOI18N
    }
}

