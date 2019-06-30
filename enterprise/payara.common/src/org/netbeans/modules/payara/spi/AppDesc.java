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

package org.netbeans.modules.payara.spi;

/**
 *
 * @author Peter Williams
 */
public class AppDesc {
    
    private final String name;
    private final String path;
    private final String contextRoot;
    private final boolean enabled;
    
    public AppDesc(final String name, final String path, final String contextRoot, boolean enabled) {
        this.name = name;
        this.path = path;
        this.contextRoot = contextRoot;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getContextRoot() {
        return contextRoot;
    }

    public boolean getEnabled() {
        return enabled;
    }
    
}
