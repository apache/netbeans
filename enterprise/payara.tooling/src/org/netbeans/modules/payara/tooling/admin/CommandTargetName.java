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
 * Abstract Payara Server Command Entity containing name and target.
 * <p/>
 * Contains common <code>name</code> and <code>target</code> attribute.
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class CommandTargetName extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Name of module/cluster/instance to modify. */
    final String name;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server enable command entity.
     * <p/>
     * @param command Server command represented by this object.
     * @param name   Name of module/cluster/instance to modify.
     * @param target Target Payara instance or cluster where
     *               <code>name</code> is stored.
     */
    CommandTargetName(String command, String name, String target) {
        super(command, target);
        this.name = name;
    }

}
