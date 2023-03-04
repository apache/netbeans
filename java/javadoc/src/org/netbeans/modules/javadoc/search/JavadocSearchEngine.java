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

package org.netbeans.modules.javadoc.search;

/**
 *
 * @author Petr Suchomel
 * @version 0.1
 */
public abstract class JavadocSearchEngine extends java.lang.Object {

    /** Used to search for set elements in javadoc repository
     * @param callback Callback giving new items and finished event
     * @param items to search for
     * @throws NoJavadocException if no javadoc directory is mounted, nothing can be searched
     */
    public abstract void search(String[] items, SearchEngineCallback callback) throws NoJavadocException;

    /** Stops execution of Javadoc search thread
     */    
    public abstract void stop();
    
    /** Gets default engine
     * @return default Javadoc search engine
     */    
    public static JavadocSearchEngine getDefault(){
        return new JavadocSearchEngineImpl();
    }    
    
    /** Call back interface for Javadoc search engine
     */    
    public static interface SearchEngineCallback {
        /**
         * Called if search process finished
         */
        public void finished();
        
        /** Called if javadoc item found
         * @param item  DocIndexItem with found data
         */        
        public void addItem(DocIndexItem item);
    }
    
}
