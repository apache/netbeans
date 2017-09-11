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

package org.netbeans.core.execution;

import java.io.Reader;
import java.io.Writer;

import org.openide.windows.InputOutput;

/** simply contains all ins n' outs for running task
* There is one instance for every running task.
*
* @author Ales Novak
* @version 0.11 April 24, 1998
*/
class TaskIO {

    /** No name */
    static final String VOID = "noname"; // NOI18N

    /** stdout for task */
    Writer out;
    /** stderr */
    Writer err;
    /** stdin */
    Reader in;

    /** 'theme' for this task */
    InputOutput inout;

    /** name for the TaskIO */
    private String name;

    /** Should not be this TaskIO processed by IOTable? */
    boolean foreign;

    TaskIO () {
        name = VOID;
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    */
    TaskIO (InputOutput inout) {
        this(inout, VOID);
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    */
    TaskIO (InputOutput inout, String name) {
        this.inout = inout;
        this.name = name;
    }

    /**
    * @param inout is an InputOutput
    * @param name is a name
    * @param foreign if true then IOTable never cares about this TaskIO
    */
    TaskIO (InputOutput inout, String name, boolean foreign) {
        this.inout = inout;
        this.name = name;
        this.foreign = foreign;
    }

    /** inits out */
    void initOut () {
        if (out == null) {
            out = inout.getOut();
        }
    }

    /** inits err */
    void initErr() {
        if (err == null) {
            err = inout.getErr();
        }
    }

    /** inits in */
    void initIn() {
        if (in == null) {
            in = inout.getIn();
        }
    }

    /**
    * @return name
    */
    String getName() {
        return name;
    }

    /**
    * @return InputOutput for this TaskIO
    */
    InputOutput getInout() {
        return inout;
    }
}
