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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author Radek Matous
 */
class ListProcessor extends PropertyProcessor {
    ListProcessor() {
        super("java.util.ArrayList");//NOI18N
    }

    @Override
    void processPropertyImpl(String propertyName, Object value) {
        if ("openProjectsURLs".equals(propertyName) 
         || "recentProjectsURLs".equals(propertyName)
         || "recentTemplates".equals(propertyName)) {//NOI18N
            int s = 0;
            for (Object elem : ((SerParser.ObjectWrapper)value).data) {
                String prop = null;
                if (elem instanceof SerParser.ObjectWrapper) {
                    List list2 = ((SerParser.ObjectWrapper)elem).data;
                    try {
                        URL url = URLProcessor.createURL(list2);
                        prop = url.toExternalForm();
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                } else if (elem instanceof String) {
                    prop = (String)elem;
                }
                if (prop != null) {
                    addProperty(propertyName + "." + s, prop);
                    s = s + 1;
                }
            }
        }  
    }
}
