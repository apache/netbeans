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
package org.netbeans.upgrade.systemoptions;


/**
 *
 * @author Tomas Stupka
 */
class HashSetProcessor extends PropertyProcessor {

    static final String SVN_PERSISTENT_HASHSET = "org.netbeans.modules.subversion.settings.SvnModuleConfig.PersistentHashSet";              // NOI18N
    
    HashSetProcessor(String className) {
        super(className);
    }
    
    @Override
    void processPropertyImpl(String propertyName, Object value) {
        if ("commitExclusions".equals(propertyName)) { // NOI18N
            int c = 0;
            for (Object elem : ((SerParser.ObjectWrapper) value).data) {
                if(elem instanceof String) {
                    addProperty(propertyName + "." + c, (String) elem);
                    c = c + 1;
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }    
}
