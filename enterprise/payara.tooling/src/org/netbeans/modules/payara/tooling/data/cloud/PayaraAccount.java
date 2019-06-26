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

/**
 * Payara Cloud User Account Entity.
 * <p/>
 * Payara Cloud User Account entity interface allows to use foreign
 * entity classes.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface PayaraAccount {

    /**
     * Get Payara cloud user account name.
     * <p/>
     * This is display name given to the cluster.
     * <p/>
     * @return Payara cluster name.
     */
    public String getName();

    /**
     * Get Payara cloud account name.
     * <p/>
     * @return Payara cloud account name.
     */
    public String getAcount();

    /**
     * Get Payara cloud user name under account.
     * <p/>
     * @return Payara cloud user name under account.
     */
    public String getUserName();

    /**
     * Get Payara cloud URL.
     * <p/>
     * @return Cloud URL.
     */
    public String getUrl();

    /**
     * Get Payara cloud user password under account.
     * <p/>
     * @return Payara cloud user password under account.
     */
    public String getUserPassword();

    /**
     * Get Payara cloud entity reference.
     * <p/>
     * @return Payara cloud entity reference.
     */
    public PayaraCloud getCloudEntity();

    }
