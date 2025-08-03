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
package org.netbeans.modules.glassfish.tooling.admin;

/**
 * GlassFish Server Delete Instance Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpDeleteInstance.class)
@RunnerRestClass(runner=RunnerRestDeleteInstance.class)
public class CommandDeleteInstance extends CommandTarget {

    // Class attributes                                                       //
    /** Command string for delete-instance command. */
    private static final String COMMAND = "delete-instance";

    // Constructors                                                           //
    /**
     * Constructs an instance of GlassFish server delete-instance command entity.
     * <p/>
     * @param target Target GlassFish instance.
     */
    public CommandDeleteInstance(String target) {
        super(COMMAND, target);
    }

}
