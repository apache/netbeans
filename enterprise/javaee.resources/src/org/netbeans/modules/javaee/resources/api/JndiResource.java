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
package org.netbeans.modules.javaee.resources.api;

import org.netbeans.modules.javaee.resources.api.model.Location;

/**
 * Describes single JNDI resource as defined in the chapter EE.5.19.2 of the JavaEE 7 platform specification.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface JndiResource {

    /**
     * Returns location of the JNDI resource definition - its file and offset.
     * @return location of the resource definition
     */
    Location getLocation();

    public enum Type {
        DATA_SOURCE,
        JMS_DESTINATION,
        JMS_CONNECTION_FACTORY,
        MAIL_SESSION,
        CONNECTOR_RESOURCE,
        ADMINISTRED_OBJECT
    }
}
