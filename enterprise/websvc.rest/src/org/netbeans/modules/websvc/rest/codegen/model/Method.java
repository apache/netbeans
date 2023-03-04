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
package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.List;

import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;


public class Method {

    public Method(String name) {
        this.name = name;
    }
    
    public RestEntity getReturnType(){
        return returnEntity;
    }
    
    public RestEntity getParameterType(){
        return paramEntity;
    }

    public String getName() {
        return name;
    }

    public HttpMethodType getType() {
        return type;
    }

    protected void setType(HttpMethodType type) {
        this.type = type;
    }

    public List<String> getRequestMimes() {
        return request;
    }

    protected void setRequestMimes(List<String> request) {
        this.request = request;
    }

    public List<String> getResponseMimes() {
        return response;
    }

    public void setResponseMimes(List<String> response) {
        this.response = response;
    }
    
    public String getPath(){
        return path;
    }
    
    void setPath(String path){
        this.path= path;
    }
    
    void setReturnType( RestEntity entity){
        returnEntity = entity;
    }
    
    void setParamType( RestEntity entity ){
        paramEntity = entity;
    }
    
    private String name;
    private HttpMethodType type;
    private List<String> request;
    private List<String> response; 
    private String path;
    private RestEntity returnEntity;
    private RestEntity paramEntity;
}