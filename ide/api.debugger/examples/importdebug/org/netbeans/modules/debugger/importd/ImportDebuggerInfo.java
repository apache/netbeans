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

package org.netbeans.modules.debugger.importd;

import org.netbeans.modules.debugger.*;

/**
* Contains information about a class to debug.
* Consists of these pieces of information:
* <UL>
* <LI>the class to run
* <LI>parameters for its main method
* <LI>a class name to stop execution in, if desired
* </UL>
* Uses Import debugger.
*
* @author Jan Jancura
*/
public class ImportDebuggerInfo extends AbstractDebuggerInfo {

    /**
    * Construct a new <code>DebuggerInfo</code> with the class to run and its parameters specified.
    * Sets class to stop in to be the class to run.
    *
    * @param className name of debugged class
    * @param argv command-line arguments used for debugging this class; may be empty but not <code>null</code>
    */
    public ImportDebuggerInfo (
        String className,
        String[] argv,
        String stopClassName
    ) {
        super (
            className,
            argv,
            stopClassName
        );
    }
    
    /**
     * Return display name of debugged process.
     *
     * @return display name of debugged process
     */
    public String getProcessName () {
        return getClassName ();
    }
    
    /**
     * Return display name of location where this process will run.
     *
     * @return display name of location where this process will run
     */
    public String getLocationName () {
        return "localhost";
    }
    
    /**
     * Return type of debugger which should be used to debug this DebuggerInfo.
     *
     * @return type of debugger which should be used to debug this DebuggerInfo
     */
    public DebuggerImpl getDebuggerImpl () {
        return ImportDebuggerModule.getDebuggerImpl ();
    }
}
