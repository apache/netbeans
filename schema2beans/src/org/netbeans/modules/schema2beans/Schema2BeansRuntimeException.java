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

/*
 * Schema2BeansRuntimeException
 * I'd rather this class didn't exist.  I wanted to change the API to throw
 * proper exceptions, but this is difficult to do with so many users of
 * this library.  This class exists to fill in where I found it
 * difficult to throw a normal Schema2BeansException, since anything
 * that inherits from RuntimeException doesn't have to be caught.
 *
 * With that being said, there are a few times when a RuntimeException
 * would be approbate enough, but I used Schema2BeansRuntimeException
 * as it has more features.
 */

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;

public class Schema2BeansRuntimeException extends RuntimeException implements Serializable {
    protected Throwable childThrowable;
    protected String message;
    protected String stackTrace;

    public Schema2BeansRuntimeException(Throwable e) {
        super("");
        //System.out.println("Created Schema2BeansRuntimeException1: e="+e);
        //e.printStackTrace();
        childThrowable = e;
        message = childThrowable.getMessage();
        genStackTrace();
    }
    
    public Schema2BeansRuntimeException(String mesg) {
        super(mesg);
        //System.out.println("Created Schema2BeansRuntimeException3: mesg="+mesg);
        childThrowable = null;
        message = mesg;
        genStackTrace();
    }

    public Schema2BeansRuntimeException(String mesg, Throwable e) {
        super(mesg);
        //System.out.println("Created Schema2BeansRuntimeException2: e="+e+" mesg="+mesg);
        //e.printStackTrace();
        childThrowable = e;
        message = mesg+"\n"+childThrowable.getMessage();
        genStackTrace();
    }

    public Throwable getCause() {
        return childThrowable;
    }
    
    public String getMessage() {
        return message;
    }

    protected void genStackTrace() {
        StringWriter strWriter = new StringWriter();
        PrintWriter s = new PrintWriter(strWriter);
        if (childThrowable == null) {
            super.printStackTrace(s);
        } else {
            s.println(super.getMessage());
            childThrowable.printStackTrace(s);
        }
        stackTrace = strWriter.toString();
   }

    public void printStackTrace(PrintStream s) {
        s.println(stackTrace);
    }

    public void printStackTrace(PrintWriter s) {
        s.println(stackTrace);
    }

    public void printStackTrace() {
        System.out.println(stackTrace);
    }
}
