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

package org.netbeans.modules.cnd.makefile.utils;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 *  Parse a Fortran file. Return complete Fortran statements, taking into consideration
 *  things like the source form, line length, continuation lines, and comments. This
 *  class works in conjunction with FortranParse to parse fortran files for mod and use
 *  statements.
 */
public class FortranParser {

    private FortranReader in;

    private boolean verbose = false;

    /** Contstructor for this semi-parsing class */
    public FortranParser(String file, String options,
			boolean verbose, boolean verboseReader) {

	this.verbose = verbose;

	try {
	    in = new FortranReader(file, options, verboseReader);
	} catch (FileNotFoundException ex) {
	    in = null;
	}
    }


    /** Contstructor for this semi-parsing class */
    public FortranParser(String file, String options) {
	this(file, options, false, false);
    }


    /**
     *  This is a semi-parser. Kind of. It gets complete Fortran statements (stripped of
     *  comments and labels) and checks for 2 specific keywords, MODULE and USE. It
     *  creates a list containing each MODULE or USE name preceded by an 'M' or 'U'.
     */
    public ArrayList<String> parser() {
        ArrayList<String> list = new ArrayList<String>();
        String line;
        String arg;

        if (in == null) {
            return null;
        }

        try {
            while ((line = in.getStatement()) != null) {
                if (line.length() >= 15 && line.substring(0, 15).
                        equalsIgnoreCase("moduleprocedure")) {	// NOI18N
                    // ignore this keyword
                } else if (line.length() >= 6 &&
                        line.substring(0, 6).equalsIgnoreCase("module")) { // NOI18N
                    arg = getName(line.substring(6));
                    list.add("M" + arg);    // NOI18N
                } else if (line.length() >= 3 &&
                        line.substring(0, 3).equalsIgnoreCase("use")) {	// NOI18N
                    arg = getName(line.substring(3));
                    list.add("U" + arg);    // NOI18N
                }
            }
        } catch (UnexpectedEOFException ex) {
            if (verbose) {
                System.err.println("Error: Unexpected EOF"); // NOI18N
                ex.printStackTrace();
            }
            return null;
        }

        return list;
    }


    /** Get the name following a MODULE or USE keyword */
    private String getName(String arg) {
	StringBuilder buf = new StringBuilder();
	String arg2 = arg.trim();
	
	for (int i = 0; i < arg2.length(); i++) {
	    char c = arg2.charAt(i);

	    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
					(c >= '0' && c <= '9') || c == '_') {
		buf.append(c);
	    } else {
		break;
	    }
	}
	return buf.toString();
    }



    /** This main method is used for testing the class */
    public static void main(String[] args) {
	FortranParser parser = null;
	boolean verbose = false;
	String file = null;

	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-v")) {	// NOI18N
		verbose = true;
	    } else {
		file = args[i];
	    }
	}

	parser = new FortranParser(file, null);
	parser.verbose = verbose;

	ArrayList<String> list = parser.parser();

	System.out.println("FortranParser: list has " + list.size() + " elements");// NOI18N
	for (int i = 0; i < list.size(); i++) {
	    String stmnt = list.get(i).toString();
	    if (stmnt.charAt(0) == 'M') {
		System.out.println("\tModule " + stmnt.substring(1));	// NOI18N
	    } else {
		System.out.println("\tUse " + stmnt.substring(1));  // NOI18N
	    }
	}
    }
}
