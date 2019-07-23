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
 * Payara server administration command entity with local Java SE support
 * and class path.
 * <p/>
 * @author Tomas Kraus
 */
public abstract class CommandJavaClassPath extends CommandJava {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Class path to be passed to java executable formated as
        <code>-cp &lt;path1&gt;:&lt;path2&gt;:...:&lt;pathN&gt;</code>. */
    final String classPath;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server administration command entity
     * with specified server command, Java SE home and class path.
     * <p/>
     * @param command   Server command represented by this object.
     * @param javaHome  Java SE home used to select JRE for Payara server.
     * @param classPath Java SE class path.
     */
    public CommandJavaClassPath(final String command, final String javaHome,
            final String classPath) {
        super(command, javaHome);
        this.classPath = classPath;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get class path to be passed to java executable.
     * <p/>
     * @return the classPath Class path to be passed to java executable.
     */
    public String getClassPath() {
        return classPath;
    }

}
