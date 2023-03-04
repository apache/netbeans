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
package org.netbeans.modules.java.lsp.server.protocol;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Information specifying configuration update.
 * 
 * @author Jan Horvath
 */
public class UpdateConfigParams {
    
    /**
     * Configuration name, supports dotted names.
     */
    @NonNull
    String section;
    
    /**
     * Configuration name, supports dotted names.
     */
    String key;
    
    /**
     * The new value.
     */
    String value;

    public UpdateConfigParams(String section, String key, String value) {
        this.section = section;
        this.key = key;
        this.value = value;
    }
    
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
}
