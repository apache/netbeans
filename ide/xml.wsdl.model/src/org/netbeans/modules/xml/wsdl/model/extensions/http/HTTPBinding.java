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

package org.netbeans.modules.xml.wsdl.model.extensions.http;

/**
 * Represents the http:binding element under the wsdl binding element for HTTP binding.
 */
public interface HTTPBinding extends HTTPComponent {
    public static final String VERB_PROPERTY = "verb";
    
    Verb getVerb();
    void setVerb(Verb verb); 
    
    public enum Verb { 
        GET("GET"), POST("POST");
        
        private String tag;
        Verb(String tag) {
            this.tag = tag;
        }

        @Override
        public String toString() {
            return tag;
        }
    }
}
