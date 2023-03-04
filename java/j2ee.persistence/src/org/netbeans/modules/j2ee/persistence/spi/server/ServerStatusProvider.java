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

package org.netbeans.modules.j2ee.persistence.spi.server;

/**
 * This interface should be implemented by projects that can have a target
 * server. It provides means for querying whether a valid instance
 * of the target server is currently present. 
 * 
 * @author Erno Mononen
 */
public interface ServerStatusProvider {

    /**
     * Checks whether a valid instance of our project's  target server is present.
     * 
     * @return true if a valid target server instance was present, false otherwise.
     */ 
    boolean validServerInstancePresent();
    
}
