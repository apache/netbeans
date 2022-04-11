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

package org.netbeans.modules.cnd.debugger.gdb2.mi;

/**
 * Representation of a complete MI record as specified here:
 * <br>
 * http://sourceware.org/gdb/current/onlinedocs/gdb_25.html
 */

public class MIRecord {
    private MICommand command;		// ... which generated this record

    boolean isError;
    String error = "MI parse error: NONE";	// NOI18N

    int token;
    char type = '?';
    boolean isStream;
    String stream = "";				// NOI18N
    String cls = "";
    MITList results;

    // Make sure default constructor is package-private.

    MIRecord() {
    }

    void setCommand(MICommand command) {
	this.command = command;
    }

    public MICommand command() {
	return command;
    }

    /**
     * Return true if there was an error during parsing.
     */
    public boolean isError() {
	return isError;
    }


    /**
     * Return the error message generated during parsing.
     * <br>
     * Will not return a null, even if isError() is false.
     */

    public String error() {
	return error;
    }


    /**
     * Retrieve the numerical/prefix token.
     */

    public int token() {
	return token;
    }


    /**
     * Retrieve the prefix character associated with the type of record.
     * One of "^*+=~@&".
     */

    public char type() {
	return type;
    }


    /**
     * Return true if the record is a "stream" message, one of "~@&".
     */
    public boolean isStream() {
	return isStream;
    }


    /**
     * Retrieve the actual contents of the stream.
     * Will return "" is isStream() is false.
     */

    public String stream() {
	return stream;
    }


    /**
     * Retrieve the "class" (result or async) of the record.
     *
     * class can, for example, be one of,
     * "running", "connected", "error", "exit" and "stopped".
     */

    public String cls() {
	return cls;
    }


    /**
     * Retrieve the result list portion of a 'result' or 'async' record.
     * <br>
     * If there were no results after the class or we're a stream record,
     * a valid MITuple is still returned which has isEmpty() as true.
     */

    public MITList results() {
	return results;
    }

    /**
     * Return true if there are no results.
     * This can happen if some error occurs and we nevertheless get 'done'
     * instead of 'error'. This happens for example like this:
     * <pre>
     * 8-break-insert nonexistent
     * &"Function \"nonexistent\" not defined.\n"
     * 8^done
     * </pre>
     */
    public boolean isEmpty() {
	return results == null || results.isEmpty();
    }


    @Override
    public String toString() {
	StringBuilder s = new StringBuilder();
	if (token != 0)
	    s.append(token);
	s.append(type);
	if (isStream) {
	    s.append('"').append(stream).append('"'); // NOI18N
	} else {
	    if (isEmpty())
		s.append(cls);
	    else
		s.append(cls).append(',').append(results.toString()); // NOI18N
	}

	if (isError)
	    s.append(" ERROR: ").append(error); // NOI18N

	return s.toString();
    }
}
