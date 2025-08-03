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
 * GlassFish Server Cerate Cluster Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpTarget.class)
@RunnerRestClass(runner=RunnerRestCreateCluster.class)
public class CommandCreateCluster extends CommandTarget {

    // Class attributes                                                       //
    /** Command string for create-cluster command. */
    private static final String COMMAND = "create-cluster";

    // Constructors                                                           //
    /**
     * Constructs an instance of GlassFish server create-cluster command entity.
     * <p/>
     * @param clusterName Name of the created cluster.
     */
    public CommandCreateCluster(String clusterName) {
        super(COMMAND, clusterName);
    }

}
