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

package org.netbeans.modules.j2ee.deployment.common.api;

/**
 * ConfigurationException occurs if there is a problem with the server-specific
 * configuration.
 * 
 * @author sherold
 * 
 * @since 1.23
 */
public class ConfigurationException extends Exception {

    /**
     * Constructs a new ConfigurationException with a message describing the error.
     * 
     * @param message describing the error. The message should be localized so that
     *        it can be displayed to the user.
     */
    public ConfigurationException(String message) {
	super(message);
    }

    /**
     * Constructs a new ConfigurationException with a message and cause of the error.
     * 
     * @param message describing the error. The message should be localized so that
     *        it can be displayed to the user.
     * @param cause the cause of the error.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
