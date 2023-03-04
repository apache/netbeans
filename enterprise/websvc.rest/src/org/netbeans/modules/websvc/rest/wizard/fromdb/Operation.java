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
package org.netbeans.modules.websvc.rest.wizard.fromdb;

import org.netbeans.modules.websvc.rest.codegen.RestMethod;

public enum Operation implements RestMethod {

    CREATE("javax.ws.rs.POST", "create", true ),
    EDIT("javax.ws.rs.PUT", "edit", "{id}" ),
    REMOVE("javax.ws.rs.DELETE", "remove", "{id}"),
    FIND("javax.ws.rs.GET", "find", "{id}"),
    FIND_ALL("javax.ws.rs.GET", "findAll", true),
    FIND_RANGE("javax.ws.rs.GET", "findRange", "{from}/{to}"),
    COUNT("javax.ws.rs.GET", "countREST", "count");

    private String method, methodName, uriPath;
    private boolean override;
    
    private Operation(String method, String methodName, boolean override) {
        this.method = method;
        this.methodName = methodName;
        this.override = override;
    }

    private Operation(String method, String methodName) {
        this.method = method;
        this.methodName = methodName;
    }

    private Operation(String method, String methodName, String uriPath) {
        this.method = method;
        this.methodName = methodName;
        this.uriPath = uriPath;
    }
    
    public String getMethod() {
        return method;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getUriPath() {
        return uriPath;
    }
    
    public boolean overrides(){
        return override;
    }
}