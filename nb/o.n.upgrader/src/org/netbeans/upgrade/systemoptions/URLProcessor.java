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
import java.util.Iterator;
import java.util.List;

/**
 * @author Milos Kleint
 */
class URLProcessor extends PropertyProcessor {    
    URLProcessor() {
        super("java.net.URL");//NOI18N
    }
    
    @Override
    void processPropertyImpl(String propertyName, Object value) {
        if ("mainProjectURL".equals(propertyName)) {//NOI18N
            List<Object> l = ((SerParser.ObjectWrapper)value).data;
            try {
                URL url = createURL(l);
                addProperty(propertyName, url.toExternalForm());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }  else {
            throw new IllegalStateException();
        }
    }
    
    public static URL createURL(List<Object> l) throws MalformedURLException {
            String protocol = null;
            String host = null;
            int port = -1;
            String file = null;
            String authority = null;
            String ref = null;
            for (Object elem : l) {
                if (elem instanceof SerParser.NameValue) {
                    SerParser.NameValue nv = (SerParser.NameValue)elem;
                    if (nv.value != null && nv.name != null) {
                        if (nv.name.name.equals("port")) {//NOI18N
                            port = (Integer)nv.value;//NOI18N
                        }
                        else if (nv.name.name.equals("file")) {//NOI18N
                            file = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("authority")) {//NOI18N
                            authority = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("host")) {//NOI18N
                            host = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("protocol")) {//NOI18N
                            protocol = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("ref")) {//NOI18N
                            ref = nv.value.toString();//NOI18N
                        }
                    }
                }
            }
            return new URL(protocol, host, port, file);
        
    }
}
