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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class ContentProcessor  {
    private static final Map<String, ContentProcessor> clsname2Delegate = new HashMap<>();
    protected String systemOptionInstanceName;
    
    static {
        registerContentProcessor(new JUnitContentProcessor("org.netbeans.modules.junit.JUnitSettings"));//NOI18N
    }
    
    private static void registerContentProcessor(ContentProcessor instance) {
        if (clsname2Delegate.put(instance.systemOptionInstanceName, instance) != null) {
            throw new IllegalArgumentException();
        }
    }
        
            
    protected ContentProcessor(String systemOptionInstanceName) {
        this.systemOptionInstanceName = systemOptionInstanceName;
    }
            
    protected Result parseContent(final Iterator<Object> it, boolean types) {
        Map<String, String> m;
        Result result = null;
        try {
            Map<String, Object> props = parseProperties(it);
            assert props != null;
            //debugInfo("before: ", m);                        
            m = processProperties(props, types);
            //assert debugInfo("after: ", m);
            result = new DefaultResult(systemOptionInstanceName, m);
        } catch (IllegalStateException isx) {
            Logger.getLogger(ContentProcessor.class.getName()).log(Level.WARNING, systemOptionInstanceName + " not parsed", isx);
        }
        return result;        
    }
    
    static Result parseContent(String systemOptionInstanceName, boolean types, final Iterator<Object> it) {
        ContentProcessor cp = clsname2Delegate.get(systemOptionInstanceName);
        if (cp == null) {
            cp = new ContentProcessor(systemOptionInstanceName);
        }
        return cp.parseContent(it, types);
    }
    
    private final Map<String, String> processProperties(final Map<String, Object> properties, boolean types) {
        Map<String, String> allProps = new HashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            allProps.putAll(PropertyProcessor.processProperty(name, value, types));
        }
        return allProps;
    }
    
    private final  Map<String, Object> parseProperties(final Iterator<Object> it) { // sequences String, Object, SerParser.ObjectWrapper
        Map<String, Object> properties = new HashMap<>();
        for (; it.hasNext();) {
            Object name = it.next();
            if ("null".equals(name) || name == null) {
                //finito
                return properties;
            } else if (!(name instanceof String)) {
                throw new IllegalStateException(name.getClass().getName());
            } else {
                if (!it.hasNext()) {
                    throw new IllegalStateException(name.toString());
                }
                Object value = it.next();
                properties.put((String)name, value);
                Object propertyRead = it.next();
                if (!(propertyRead instanceof SerParser.ObjectWrapper )) {
                    throw new IllegalStateException(propertyRead.getClass().getName());
                } else {
                    SerParser.ObjectWrapper ow = (SerParser.ObjectWrapper)propertyRead;
                    if (!ow.classdesc.name.endsWith("java.lang.Boolean;")) {//NOI18N
                        throw new IllegalStateException(ow.classdesc.name);
                    }
                }
            }
        }
        throw new IllegalStateException("Unexpected end");//NOI18N
    }        
}
