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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.netbeans.api.java.source.JavaSource;


/**
 * @author (changed by)ads
 *
 */
public class Resource {

        private String name;
        private String path;
        private String desc;
        private boolean hasDefaultGet;
        private Set<String> entities;

        private JavaSource src;

        private List<Method> methodList = Collections.emptyList();

        public Resource(String name, String path, String desc) {
            this.name = name;
            this.path = path;
            this.desc = desc;
            this.methodList = new ArrayList<Method>();
        }

        public Resource(String name, String path) {
            this(name, path , name );
        }  

        public String getName() {
            return name;
        }
        
        public String getPath() {
            return path;
        }

        public String getDescription() {
            return desc;
        }

        public void setDescription(String desc) {
            this.desc = desc;
        }        

        public List<Method> getMethods() {
            return methodList;
        }

        public void addMethod(Method m) {
            if ( m != null ){
                methodList.add(m);
            }
        }
        
        public void setDefaultGet(){
            hasDefaultGet = true;
        }
        
        public boolean hasDefaultGet(){
            return hasDefaultGet;
        }
        
        public Set<String> getEntities(){
            return entities;
        }
        
        public void setEntities(Set<String> set){
            entities = set;
        }

        protected JavaSource getSource() {
            return this.src;
        }

        protected void setSource(JavaSource src) {
            this.src = src;
        }     

        @Override
        public String toString() {
            return getName()+" : "+getPath();
        }
    }