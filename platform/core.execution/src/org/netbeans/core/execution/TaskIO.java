/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
