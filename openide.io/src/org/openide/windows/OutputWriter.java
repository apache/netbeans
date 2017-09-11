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

package org.openide.windows;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
* A PrintWriter subclass for writing to a tab in the output window.  To
* create hyperlinked lines, call <code>println</code>, passing an instance
* of {@link org.openide.windows.OutputListener} which should be called when a line is
* clicked or the caret in the output window enters it.
*
* @author Ales Novak
* @version 0.13 Feb 24, 1997
*/
public abstract class OutputWriter extends PrintWriter {
    /** Make an output writer.
    * @param w the underlying writer
    */
    protected OutputWriter (Writer w) {
        super(w);
    }

    /** Print a line which will be displayed as a hyperlink, calling the 
    * passed {@link org.openide.windows.OutputListener} if it is clicked, if the caret 
    * enters it, or if the enter key is pressed over it.
    * 
    *
    * @param s a string to print to the tab
    * @param l a listener that will receive events about this line
    * @throws IOException if the string could not be printed
    */
    public abstract void println(String s, OutputListener l) throws IOException;

    /** Print a line which will be displayed as a hyperlink, calling the 
     * passed {@link org.openide.windows.OutputListener} if it is clicked, if the caret 
     * enters it, or if the enter key is pressed over it. 
     * 
     * Implementors of this class 
     * are encouraged to override this method, which is not abstract for backward 
     * compatibility reasons only.
     *
     * @param s a string to print to the tab
     * @param l a listener that will receive events about this line
     * @param important mark the line as important. 
     *        Makes the UI respond appropriately, eg. stop the automatic scrolling 
     *        or highlight the hyperlink.
     * @throws IOException if the string could not be printed
     * @since 1.5
     */
    public void println(String s, OutputListener l, boolean important) throws IOException {
        println(s, l);
    }
    
    /** Clear the output pane.
    * Expect this method to be deprecated in a future release and an 
    * equivalent created in {@link org.openide.windows.InputOutput}.  It is ambiguous what it means
    * to reset stdout but not stderr, etc.  For the current implementation, reset
    * should be called on the stdout.
    * 
    * @throws IOException if there is a problem
    */
    public abstract void reset() throws IOException;
}
