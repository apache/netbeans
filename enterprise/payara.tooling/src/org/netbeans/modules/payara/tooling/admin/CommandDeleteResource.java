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
 * Command that deletes resource from server.
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
@RunnerHttpClass(runner=RunnerHttpDeleteResource.class)
@RunnerRestClass(runner=RunnerRestDeleteResource.class)
public class CommandDeleteResource extends CommandTarget {

    private static final String COMMAND_PREFIX = "delete-";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Name of the resource. */
    String name;

    /** Key name that defines the deleted property. */
    String cmdPropertyName;

    /** Delete also dependent resources. */
    boolean cascade;

    /**
     * Constructor for delete resource command entity.
     * <p/>
     * @param target            Target Payara instance or cluster.
     * @param name              Name of resource to be deleted.
     * @param resourceCmdSuffix Resource related command suffix. Command string
     *                          is build by appending this value after
     *                          <code>delete-</code>.
     * @param cmdPropertyName   Name of query property which contains
     *                          resource name.
     * @param cascade           Delete also dependent resources.
     */
    public CommandDeleteResource(String target, String name,
            String resourceCmdSuffix, String cmdPropertyName, boolean cascade) {
        super(COMMAND_PREFIX + resourceCmdSuffix, target);
        this.name = name;
        this.cmdPropertyName = cmdPropertyName;
        this.cascade = cascade;
    }
    
    /**
     * Constructor for delete resource command entity.
     * <p/>
     * @param name              Name of resource to be deleted.
     * @param resourceCmdSuffix Resource related command suffix. Command string
     *                          is build by appending this value after
     *                          <code>delete-</code>.
     * @param cmdPropertyName   Name of query property which contains
     *                          resource name.
     * @param cascade           Delete also dependent resources.
     */
    public CommandDeleteResource(String name,
            String resourceCmdSuffix, String cmdPropertyName, boolean cascade) {
        this(null, name, resourceCmdSuffix, cmdPropertyName, cascade);
    }
    
}
