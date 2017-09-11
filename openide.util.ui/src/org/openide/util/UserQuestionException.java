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
package org.openide.util;


/** Exception that is thrown when the process is about to perform some
* action that requires user confirmation. It can be useful when there
* is a call to a method which cannot open a dialog, but still would like
* to ask the user a question. It can raise this exception and higher level
* parts of the system can/should catch it and present a dialog to the user
* and if the user agrees reinvoke the action again.
* <P>
* The <code>getLocalizedMessage</code> method should return the user question,
* which will be shown to the user in a dialog with OK, Cancel options and
* if the user chooses OK, method <code>ex.confirmed ()</code> will be called.
* <p>
* Since version 8.29 one can just catch the exception and report it to the
* infrastructure of any NetBeans Platform based application (for example
* via {@link Exceptions#printStackTrace(java.lang.Throwable)}) and the
* question dialog will be displayed automatically.
*
* @author Jaroslav Tulach
*/
public abstract class UserQuestionException extends java.io.IOException {
    static final long serialVersionUID = -654358275349813705L;

    /** Creates new exception UserQuestionException
    */
    public UserQuestionException() {
        super();
    }

    /** Creates new exception UserQuestionException with text specified
    * string s.
    * @param s the text describing the exception
    */
    public UserQuestionException(String s) {
        super(s);
    }

    /** Invoke the action if the user confirms the action.
     * @exception IOException if another I/O problem exists
     */
    public abstract void confirmed() throws java.io.IOException;
}
