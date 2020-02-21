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

package org.netbeans.modules.cnd.modelutil;

import java.io.PrintStream;

/**
 * Utility class that prints with indentation.
 *
 * Each indent() call makes it adding 4 spaces in the beginning of each string
 * unindent() decreases the amount of leading spaces by 4
 *
 */
public class Tracer {
    public static final String PARSE_FILE_PERFORMANCE_EVENT = "PARSE_FILE_PERFORMANCE_EVENT"; //NOI18N
    
    private int step = 4;
    private PrintStream pstream;
    private StringBuilder indentBuffer = new StringBuilder();
    
    public Tracer() {
	this(System.err);
    }
    
    public Tracer(PrintStream pstream) {
	this.pstream = pstream;
    }
    
    public Tracer(PrintStream pstream, int step) {
	this.pstream = pstream;
	this.step = step;
    }
    
    public void indent() {
	setupIndentBuffer(indentBuffer.length() + step);
    }
    
    public void unindent() {
	setupIndentBuffer(indentBuffer.length() - step);
    }
    
    private void setupIndentBuffer(int len) {
	if( len <= 0 ) {
	    indentBuffer.setLength(0);
	} else {
	    indentBuffer.setLength(len);
	    for( int i = 0; i < len; i++ ) {
		indentBuffer.setCharAt(i,  ' ');
	    }
	}
    }
    
    public void trace(Object arg) {
	System.err.println(indentBuffer.toString() + arg);
    }
    
}
