/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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

