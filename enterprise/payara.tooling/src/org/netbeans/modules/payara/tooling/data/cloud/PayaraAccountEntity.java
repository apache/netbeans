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
 * Payara Cloud User Account Entity Interface.
 * <p/>
 * Payara Cloud User Account entity instance which is used when not defined
 * externally in IDE.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class PayaraAccountEntity implements PayaraAccount {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara cloud user account name (display name in IDE).
     *  Used as key attribute. */
    protected String name;

    /** Payara cloud server URL. Used as key attribute. */
    private String url;

    /** Payara cloud account name. */
    protected String account;

    /** Payara cloud account user name. */
    protected String userName;

    /** Payara cloud account user password. */
    protected String userPassword;

    /** Payara cloud entity reference. */
    protected PayaraCloud cloudEntity;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public PayaraAccountEntity() {
    }

    /**
     * Constructs class instance with ALL values set.
     * <p/>
     * @param name         Payara cloud account name to set.
     * @param url          Payara cloud server URL.
     * @param account      Payara cloud host to set.
     * @param userName     Payara cloud account user name to set.
     * @param userPassword Payara cloud account user password to set.
     * @param cloudEntity  Payara cloud entity reference to set.
     */
    public PayaraAccountEntity(String name, String account, String userName,
            String userPassword, String url, PayaraCloud cloudEntity) {
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
     * Get Payara cloud user account display name.
     * <p/>
     * Key attribute.
     * <p/>
     * This is display name given to the cloud user account.
     * <p/>
     * @return Payara cloud user account display name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set Payara cloud user account display name.
     * <p/>
     * Key attribute.
     * <p/>
     * This is display name given to the cloud user account.
     * <p/>
     * @param name Payara cloud user account display name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Payara cloud URL.
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
     * Set Payara cloud URL.
     * <p/>
     * Key attribute.
     * <p/>
     * @param url Cloud URL to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get Payara cloud account name.
     * <p/>
     * @return Payara cloud account name.
     */
    @Override
    public String getAcount() {
        return account;
    }

    /**
     * Set Payara cloud account name.
     * <p/>
     * @param account Payara cloud account name to set.
     */
    public void setAcount(String account) {
        this.account = account;
    }

    /**
     * Get Payara cloud user name under account.
     * <p/>
     * @return Payara cloud user name under account.
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * Set Payara cloud user name under account.
     * <p/>
     * @param userName Payara cloud user name under account to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get Payara cloud user password under account.
     * <p/>
     * @return Payara cloud user password under account.
     */
    @Override
    public String getUserPassword() {
        return userPassword;
    }


    /**
     * Set Payara cloud user password under account.
     * <p/>
     * @param userPassword Payara cloud user password under account to set.
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Get Payara cloud entity reference.
     * <p/>
     * @return Payara cloud entity reference.
     */
    @Override
    public PayaraCloud getCloudEntity() {
        return cloudEntity;
    }

    /**
     * Set Payara cloud entity reference.
     * <p/>
     * @param cloudEntity Payara cloud entity reference to set.
     */
    public void setCloudEntity(PayaraCloud cloudEntity) {
        this.cloudEntity = cloudEntity;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * String representation of this Payara cloud entity.
     * <p/>
     * @return String representation of this Payara cloud entity.
     */
    @Override
    public String toString() {
        return name;
    }

}

