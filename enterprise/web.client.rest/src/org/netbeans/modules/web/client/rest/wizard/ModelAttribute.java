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
package org.netbeans.modules.web.client.rest.wizard;



/**
 * @author ads
 *
 */
class ModelAttribute {
    
    private static final String NAME = "name";
    private static final ModelAttribute PREFFERED = new ModelAttribute(NAME);

    ModelAttribute(String name){
        myName = name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof ModelAttribute ){
            ModelAttribute attr = (ModelAttribute)obj;
            return attr.myName.equals( myName );
        }
        else {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return myName.hashCode();
    }
    
    String getName(){
        return myName;
    }
    
    static ModelAttribute getPreffered(){
        return PREFFERED;
    }
    
    private final String myName;

}
