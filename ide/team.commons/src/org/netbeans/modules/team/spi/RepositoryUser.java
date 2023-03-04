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

package org.netbeans.modules.team.spi;

/**
 * Repository user.
 *
 * @author Jan Stola
 */
public class RepositoryUser {
    /** User name (login) of the user. */
    private final String userName;
    /** Full name of the user. */
    private final String fullName;

    /**
     * Creates new <code>RepositoryUser</code>.
     *
     * @param userName user name.
     * @param fullName full name.
     */
    public RepositoryUser(String userName, String fullName) {
        this.userName = userName;
        this.fullName = fullName;
    }

    /**
     * Returns user name.
     *
     * @return user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns full name.
     *
     * @return full name.
     */
    public String getFullName() {
        return fullName;
    }

}
