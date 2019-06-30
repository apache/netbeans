/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.tooling.admin;

/**
 * Payara server process.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ValueProcess {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** The name of the executable to run. */
    private String processName;

    /** Arguments passed to the executable. */
    private String arguments;

    /** Process information. */
    private Process process;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Payara server process entity.
     * <p/>
     * Entity is initialized in <code>RunnerLocal</code> method
     * <code>call</code>.
     * method.
     * <p/>
     * @param processName The name of the executable to run..
     * @param arguments Arguments passed to the executable.
     */
    ValueProcess(String processName, String arguments, Process process) {
        this.processName = processName;        
        this.arguments = arguments;
        this.process = process;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get name of the executable to run.
     * <p/>
     * @return Name of the executable to run.
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * Get arguments passed to the executable.
     * <p/>
     * @return Arguments passed to the executable.
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Get process information.
     * <p/>
     * @return Process information.
     */
    public Process getProcess() {
        return process;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert stored process information to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        int length = (processName != null ? processName.length() : 0) +
                (arguments != null ? arguments.length() : 0) + 1;
        StringBuilder sb = new StringBuilder(length);
        if (processName != null) {
            sb.append(processName);
        }
        sb.append(' ');
        if (arguments != null) {
            sb.append(arguments);
        }
        return sb.toString();
    }

}
