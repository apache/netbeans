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
package org.netbeans.modules.cnd.apt.support.api;

/**
 *
 */
public interface PreprocHandler {
    /*
     * save/restore state of handler
     */
    public State getState();
    public void setState(State state);
    
    // key which can be used in caches 
    public interface StateKey {};

    /** immutable state object of preprocessor handler */
    public interface State {
        /**
         * check whether state correspond to compile phase or not;
         * the flag is "true" when state was created for source file or 
         * for header included from source file
         */ 
        public boolean isCompileContext();
        
        /**
         * check whether state has cached information or cleaned
         */
        public boolean isCleaned();       
        
        /**
         * check whether state is valid
         */
        public boolean isValid();
        
        public CharSequence getLanguage();

        public CharSequence getLanguageFlavor();
    };   
    
    public PPMacroMap getMacroMap();
    public PPIncludeHandler getIncludeHandler();
    
    public boolean isCompileContext();
    public boolean isValid();
    public CharSequence getLanguage();
    public CharSequence getLanguageFlavor();    
}
