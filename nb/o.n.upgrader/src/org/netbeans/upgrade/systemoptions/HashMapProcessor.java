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
 * @author Radek Matous
 */
class HashMapProcessor extends PropertyProcessor {
    HashMapProcessor() {
        super("java.util.HashMap");//NOI18N
    }

    @Override
    void processPropertyImpl(String propertyName, Object value) {
        if ("properties".equals(propertyName)) {//NOI18N
            StringBuilder b = new StringBuilder();
            int s = 0;
            for (Object elem : ((SerParser.ObjectWrapper)value).data) {
                if (elem instanceof String) {
                    switch (s) {
                        case 1:
                            b.append('\n');
                            // FALLTHROUGH
                        case 0:
                            b.append(elem);
                            s = 2;
                            break;
                        case 2:
                            b.append('=');
                            b.append(elem);
                            s = 1;
                    }
                }
            }
            addProperty(propertyName, b.toString());
        }  
    }
}
