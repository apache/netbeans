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

package org.netbeans.modules.cnd.debugger.common2.utils;

/**
 * Logging support.
 *
 * Subclass this class in each package. Then within it create logging
 * categories and within those create sub-categories. For example:

    <pre>
    public static class Bpt {
	public static final boolean pathway =
	    booleanProperty("cnd.nativedebugger.Bpt.pathway", false);
    }
    </pre>

 * Then you can do someting like this:

    <pre>
    if (Log.Bpt.pathway)
	System.out.printf("blah blah blah\n");
    </pre>

 * Log.Bpt.pathway can be turned on by passing
 *	-J-Dcnd.nativedebugger.Bpt.pathway=true
 * to NB.
 */

public class LogSupport {
    /**
     * Convert value of property to a boolean result.
     *
     * If there is no value the value of 'dflt' is used.
     * A Value of "true" or "on" converts to true.
     * A Value of "false" or "of" converts to false.
     * For any other value the value of 'dflt' is used.
     */

    protected static boolean booleanProperty(String prop, boolean dflt) {
        String value = System.getProperty(prop);
        if (value == null) {
            return dflt;
        } else if (value.equals("true") || value.equals("on")) { // NOI18N
            System.out.printf("Property %s = %b\n", prop, true); // NOI18N
            return true;
        } else if (value.equals("false") || value.equals("off")) { // NOI18N
            System.out.printf("Property %s = %b\n", prop, false); // NOI18N
            return false;
        } else {
            return dflt;
        }
    }

    //
    // From the older Log class ...
    //

    /**
     * Logging utility.
     * <p>
     * provides a set of static functons which take a category, like Log.BLAH,
     * and if it's enabled will print a trace or log message of some sort.
     * <p>
     * Categories are enabled using properties on the cmdline of runide.sh
     * as follows: -J-DLOG=BLAH,BLOH,FOOD
     * <p>
     * The various functions and how they print stuff out are as follows:
     * - Log.pr(Log.CAT, "msg")
     *	@@ msg
     * - Log.prf(Log.CAT)
     *	@@ Caller.caller()
     * - Log.prf(Log.CAT, "msg")
     *	@@ Caller.caller(): msg
     * - Log.prfa(Log.CAT, "a = " + a + ", b = " + b)
     *	@@ Caller.caller(a = 5, b = 9)
     * The callers class and function name are automaticaly determined
     * through Javas introspection abilities.
     */

    /* OLD
    public static final int IVAN = 1<<0;

    private static int categories = 0; // IVAN;
    private static boolean setup = false;
    */

    /**
     * Automatically figure the name of our caller
     * Assumptions:
     * - We're called from some method of Log && there _is_ a caller,
     *   of that function, hence 'stack[2]' below.
     * - Callers class is in a package, hence lastIndexOf('.')+1
     */

    private static void printCaller() {

	Throwable t = new Throwable();
	// t.fillInStackTrace(); redundant
	StackTraceElement stack[] = t.getStackTrace();
	StackTraceElement caller = stack[2];
	String className = caller.getClassName();
	className = className.substring(className.lastIndexOf('.')+1);
	String methodName = caller.getMethodName();
	System.out.print(className + "." + methodName); // NOI18N
    }

    /**
     * Print out 'msg' If the given category is enabled.
     */
    public static void pr(boolean flag, String msg) {
	if (flag) {
	    System.out.print("@@ "); // NOI18N
	    System.out.println(msg);
	}
    }

    /**
     * Print out a plain functon trace if the given category is enabled.
     */
    public static void prf(boolean flag) {
	if (flag) {
	    System.out.print("@@ "); // NOI18N
	    printCaller();
	    System.out.println("()"); // NOI18N
	}
    }

    /**
     * Print out a function trace with a string denoting argument values
     * inside '()'sif the given category is enabled.
     */
    public static void prfa(boolean flag, String args) {
	if (flag) {
	    System.out.print("@@ "); // NOI18N
	    printCaller();
	    System.out.print("("); // NOI18N
	    System.out.print(args);
	    System.out.println(")"); // NOI18N
	}
    }

    /**
     * Print out 'msg', prefixed with the function name,
     * if the given category is enabled.
     */
    public static void prf(boolean flag, String msg) {
	if (flag) {
	    System.out.print("@@ "); // NOI18N
	    printCaller();
	    System.out.print("(): "); // NOI18N
	    System.out.println(msg);
	}
    }

    /**
     * Return whethe rthe given category is enabled
     */
    /* OLD
    public static boolean enabled(int category) {
	if (!setup) {
	    String cats = System.getProperty("LOG");
	    if (cats != null) {
		StringTokenizer tokenizer = new StringTokenizer(cats, ",");
		while (tokenizer.hasMoreTokens()) {
		    String token = tokenizer.nextToken();
		    if (token.equals("IVAN"))
			categories |= IVAN;
		}
	    }
	    setup = true;
	}

	return (categories & category) == category;
    }
    */
}
