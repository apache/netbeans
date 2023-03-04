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

package org.netbeans.modules.payara.jakartaee.ide;

import javax.enterprise.deploy.spi.Target;

/**
 *
 * @author Ludo
 */
public class Hk2Target implements Target {

    private final String displayName;
    private final String uri;
    
    public Hk2Target(String displayName, String uri) {
        this.displayName = displayName;
        this.uri = uri;
    }
    
    public String getName() {
        return displayName;
    }

    public String getDescription() {
        return displayName;
    }
    
    public String getServerUri () {
        return uri;
    }
    
    @Override
    public String toString() {
        return getDescription();
    }

}
