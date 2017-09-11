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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.performance.benchmarks;

/**
 * A reporter that will directly print out the results in raw form.
 *
 * @author  Petr Nejedly
 */
public class PlainReporter implements Reporter {

    /** Creates new PlainReporter */
    public PlainReporter() {
    }

    public void flush() {
        System.out.flush();
    }

    public void addSample(String className, String methodName, Object argument, float value) {
        System.out.println( className + '.' + methodName + "@" +
        argument2String(argument) + ": " + formatTime( value ) );
    }
    
    /** Formats a time */
    private static String formatTime(float time) {        
        if (time < 1e-3) {
            return (time * 1e6) + "[micro s]";
        } else if (time < 1) {
            return (time * 1e3) + "[ms]";
        } else {
            return time + "[s]";
        }        
    }

    /** Handles arrays */
    private static String argument2String( Object argument ) {
        StringBuffer sb = new StringBuffer(1000);
        argument2String(argument, sb);
        return sb.toString();
    }

    private static void argument2String( Object argument, StringBuffer sb ) {
        if (argument instanceof Object[]) {
            Object[] arg = (Object[]) argument;
            sb.append('[');
            for (int i = 0; i < arg.length - 1; i++) {
                argument2String(arg[i], sb);
                sb.append(',').append(' ');
            }
            argument2String(arg[arg.length - 1], sb);
            sb.append(']');
        } else if (argument instanceof int[]) {
            int[] arg = (int[]) argument;
            sb.append('[');
            for (int i = 0; i < arg.length - 1; i++) {
                sb.append(Integer.toString(arg[i]));
                sb.append(',').append(' ');
            }
            sb.append(Integer.toString(arg[arg.length - 1]));
            sb.append(']');
        } else {
            sb.append(argument.toString());
        }
    }

    
}
