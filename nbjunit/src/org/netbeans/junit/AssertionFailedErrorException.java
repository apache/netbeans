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
 * Software is Sun Microsystems, Inc. Portions Copyright 1999-2002 Sun
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

package org.netbeans.junit;

/*
 * AssertionFileFailedError.java
 *
 * Created on March 21, 2002, 3:05 PM
 */
import junit.framework.AssertionFailedError;
import java.io.PrintWriter;
import java.io.PrintStream;

/** Error containing nested Exception.
 * It describes the failure and holds and print also the original Exception.
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class AssertionFailedErrorException extends AssertionFailedError {

    /** contains Exception that caused AssertionFailedError
     */    
    protected Throwable nestedException;

    /** Creates new AssertionFailedErrorException
     * @param nestedException contains Exception that caused AssertionFailedError
     */
    public AssertionFailedErrorException(Throwable nestedException) {
        this(null, nestedException);
    }

    /** Creates new AssertionFailedErrorException 
     *  @param message The error description menssage.
     *  @param nestedException contains Exception that caused AssertionFailedError
     */
    public AssertionFailedErrorException(String message, Throwable nestedException) {
        super(message);
        this.nestedException = nestedException;
    }
    
    /** prints stack trace of assertion error and nested exception into System.err
     */    
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /** prints stack trace of assertion error and nested exception
     * @param err PrintWriter where to print stack trace
     */    
    public void printStackTrace(PrintWriter err) {
        synchronized (err) {
            super.printStackTrace(err);
            err.println("\nNested Exception is:");
            nestedException.printStackTrace(err);
        }
    }

    /** prints stack trace of assertion error and nested exception
     * @param err PrintStream where to print stack trace
     */    
    public void printStackTrace(PrintStream err) {
        synchronized (err) {
            super.printStackTrace(err);
            err.println("\nNested Exception is:");
            nestedException.printStackTrace(err);
        }
    }
}
