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
package org.netbeans.modules.payara.tooling.admin.cloud;

import org.netbeans.modules.payara.tooling.admin.Command;

/**
 * Payara cloud administration command entity.
 * <p/>
 * Holds common data for cloud administration command.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class CommandCloud extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Cloud account identifier. */
    final String account;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara cloud administration command entity
     * with specified cloud account and command.
     * <p/>
     * @param command Cloud command represented by this object.
     * @param account Cloud account identifier.
     */
     CommandCloud(final String command, final String account) {
        super(command);
        this.account = account;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns cloud account identifier of this command.
     * <p/>
     * @return Cloud account identifier.
     */
     public String getAccount() {
         return account;
     }

}
