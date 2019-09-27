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

import java.util.Map;

/**
 * Command that creates administered object with the specified JNDI name and
 * the interface definition for a resource adapter on server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpCreateAdminObject.class)
public class CommandCreateAdminObject extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create administered object command. */
    private static final String COMMAND = "create-admin-object";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** The JNDI name of this JDBC resource. */
    final String jndiName;

    /** Resource type. */
    final String resType;

    /** The name of the resource adapter associated with this administered
     *  object. */
    final String raName;

    /** Optional properties for configuring administered object. */
    final Map<String, String> properties;

    /** If this object is enabled. */
    final boolean enabled;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of Payara server create administered object
     * command entity.
     * <p/>
     * @param jndiName   The JNDI name of this JDBC resource.
     * @param resType    Resource type.
     * @param raName     The name of the resource adapter associated with
     *                   this administered object.
     * @param properties Optional properties for configuring the pool.
     * @param enabled    If this object is enabled.
     */
    public CommandCreateAdminObject(final String jndiName,
            final String resType, final String raName,
            final Map<String, String> properties, final boolean enabled) {
        super(COMMAND);
        this.resType = resType;
        this.jndiName = jndiName;
        this.raName = raName;
        this.properties = properties;
        this.enabled = enabled;
    }

    /**
     * Constructs an instance of Payara server create administered object
     * command entity.
     * <p/>
     * This object will be enabled on server by default.
     * <p/>
     * @param jndiName   The JNDI name of this JDBC resource.
     * @param resType    Resource type.
     * @param raName     The name of the resource adapter associated with
     *                   this administered object.
     * @param properties Optional properties for configuring the pool.
     */
    public CommandCreateAdminObject(final String jndiName,
            final String resType, final String raName,
            final Map<String, String> properties) {
        this(jndiName, resType, raName, properties, true);
    }

}
