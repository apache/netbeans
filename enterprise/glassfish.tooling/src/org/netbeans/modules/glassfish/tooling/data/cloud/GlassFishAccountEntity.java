/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.glassfish.tooling.data.cloud;

/**
 * GlassFish Cloud User Account Entity Interface.
 * <p/>
 * GlassFish Cloud User Account entity instance which is used when not defined
 * externally in IDE.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishAccountEntity implements GlassFishAccount {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud user account name (display name in IDE).
     *  Used as key attribute. */
    protected String name;

    /** GlassFish cloud server URL. Used as key attribute. */
    private String url;

    /** GlassFish cloud account name. */
    protected String account;

    /** GlassFish cloud account user name. */
    protected String userName;

    /** GlassFish cloud account user password. */
    protected String userPassword;

    /** GlassFish cloud entity reference. */
    protected GlassFishCloud cloudEntity;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public GlassFishAccountEntity() {
    }

    /**
     * Constructs class instance with ALL values set.
     * <p/>
     * @param name         GlassFish cloud account name to set.
     * @param url          GlassFish cloud server URL.
     * @param account      GlassFish cloud host to set.
     * @param userName     GlassFish cloud account user name to set.
     * @param userPassword GlassFish cloud account user password to set.
     * @param cloudEntity  GlassFish cloud entity reference to set.
     */
    public GlassFishAccountEntity(String name, String account, String userName,
            String userPassword, String url, GlassFishCloud cloudEntity) {
        this.name = name;
        this.url = url;
        this.account = account;
        this.userName = userName;
        this.userPassword = userPassword;
        this.cloudEntity = cloudEntity;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish cloud user account display name.
     * <p/>
     * Key attribute.
     * <p/>
     * This is display name given to the cloud user account.
     * <p/>
     * @return GlassFish cloud user account display name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set GlassFish cloud user account display name.
     * <p/>
     * Key attribute.
     * <p/>
     * This is display name given to the cloud user account.
     * <p/>
     * @param name GlassFish cloud user account display name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get GlassFish cloud URL.
     * <p/>
     * Key attribute.
     * <p/>
     * @return Cloud URL.
     */
    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Set GlassFish cloud URL.
     * <p/>
     * Key attribute.
     * <p/>
     * @param url Cloud URL to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get GlassFish cloud account name.
     * <p/>
     * @return GlassFish cloud account name.
     */
    @Override
    public String getAcount() {
        return account;
    }

    /**
     * Set GlassFish cloud account name.
     * <p/>
     * @param account GlassFish cloud account name to set.
     */
    public void setAcount(String account) {
        this.account = account;
    }

    /**
     * Get GlassFish cloud user name under account.
     * <p/>
     * @return GlassFish cloud user name under account.
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * Set GlassFish cloud user name under account.
     * <p/>
     * @param userName GlassFish cloud user name under account to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get GlassFish cloud user password under account.
     * <p/>
     * @return GlassFish cloud user password under account.
     */
    @Override
    public String getUserPassword() {
        return userPassword;
    }


    /**
     * Set GlassFish cloud user password under account.
     * <p/>
     * @param userPassword GlassFish cloud user password under account to set.
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Get GlassFish cloud entity reference.
     * <p/>
     * @return GlassFish cloud entity reference.
     */
    @Override
    public GlassFishCloud getCloudEntity() {
        return cloudEntity;
    }

    /**
     * Set GlassFish cloud entity reference.
     * <p/>
     * @param cloudEntity GlassFish cloud entity reference to set.
     */
    public void setCloudEntity(GlassFishCloud cloudEntity) {
        this.cloudEntity = cloudEntity;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * String representation of this GlassFish cloud entity.
     * <p/>
     * @return String representation of this GlassFish cloud entity.
     */
    @Override
    public String toString() {
        return name;
    }

}

