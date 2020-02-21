/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
