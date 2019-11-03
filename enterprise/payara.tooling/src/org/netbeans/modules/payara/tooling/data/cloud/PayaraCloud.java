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
package org.netbeans.modules.payara.tooling.data.cloud;

import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara Cloud Entity Interface.
 * <p/>
 * Payara Cloud entity interface allows to use foreign entity classes.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface PayaraCloud {

    ////////////////////////////////////////////////////////////////////////////
    // Interface Methods                                                      //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Payara cloud name.
     * <p/>
     * This is display name given to the cloud.
     * <p/>
     * @return Payara cloud name.
     */
    public String getName();

    /**
     * Get Payara cloud (CPAS) host.
     * <p/>
     * @return Payara cloud (CPAS) host.
     */
    public String getHost();

    /**
     * Get Payara cloud port.
     * <p/>
     * @return Payara cloud port.
     */
    public int getPort();

    /**
     * Get Payara cloud local server.
     * <p/>
     * @return Payara cloud local server.
     */
    public PayaraServer getLocalServer();

}
