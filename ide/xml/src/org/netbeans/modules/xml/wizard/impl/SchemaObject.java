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

package org.netbeans.modules.xml.wizard.impl;

/**
 *
 * @author Sonali
 */
public class SchemaObject {
    String str;
    String[] rootElements;
    private String namespace;
    private String prefix;
    private String fileName;
    private boolean fromCatalog;
    
    public SchemaObject(String schemaFileName){
        str=schemaFileName;
    }
    
    public String toString(){
        return str;
    }

    public boolean isFromCatalog() {
        return fromCatalog;
    }

    public void setFromCatalog(boolean fromCatalog) {
        this.fromCatalog = fromCatalog;
    }

    String[] getRootElements() {
        return rootElements;
    }

    void setRootElements(String[] root) {
        this.rootElements = root;
    }
    
    public void setNamespace(String n){
        namespace = n;
    }
    
    public String getNamespace(){
        return namespace;        
    }
    
    public void setPrefix(String pre) {
        prefix = pre;
    }
    
    public String getPrefix(){
        return prefix;
    }
    
    public void setSchemaFileName(String name){
        fileName = name;
    }

    public String getSchemaFileName(){
        return fileName;
    }
}
