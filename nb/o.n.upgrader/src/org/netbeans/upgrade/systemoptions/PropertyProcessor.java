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
import java.util.Map;


abstract class PropertyProcessor  {
    private String className;
    private static Map<String, String> results;
    private static Map<String, PropertyProcessor> clsname2Delegate = new HashMap<>();
    
    static {
        //To extend behaviour of this class then regisetr your own implementation
        registerPropertyProcessor(new TaskTagsProcessor());
        registerPropertyProcessor(new HostPropertyProcessor());
        registerPropertyProcessor(new FileProcessor());//AntSettings
        registerPropertyProcessor(new NbClassPathProcessor());//AntSettings
        registerPropertyProcessor(new HashMapProcessor());//AntSettings
        registerPropertyProcessor(new IntrospectedInfoProcessor());//AntSettings                
        registerPropertyProcessor(new ListProcessor());//ProjectUISettings             
        registerPropertyProcessor(new URLProcessor());//ProjectUISettings             
        registerPropertyProcessor(new ColorProcessor(ColorProcessor.JAVA_AWT_COLOR));//FormLoaderSettings
        registerPropertyProcessor(new ColorProcessor(ColorProcessor.NETBEANS_COLOREDITOR_SUPERCOLOR));//FormLoaderSettings
        registerPropertyProcessor(new StringPropertyProcessor());//ProxySettings
        registerPropertyProcessor(new HashSetProcessor(HashSetProcessor.SVN_PERSISTENT_HASHSET));//SvnSettings
        registerPropertyProcessor(new DocumentationSettingsProcessor());
    }           


    private static void registerPropertyProcessor(PropertyProcessor instance) {
        if (clsname2Delegate.put(instance.className, instance) != null) {
            throw new IllegalArgumentException();
        }
    }
    
    private static PropertyProcessor DEFAULT = new PropertyProcessor(false) {
        @Override
        void processPropertyImpl(final String propertyName, final Object value) {
            String stringvalue = Utils.valueFromObjectWrapper(value);
            addProperty(propertyName, stringvalue);
        }
    };
    
    private static PropertyProcessor TYPES = new PropertyProcessor(true) {
        @Override
        void processPropertyImpl(final String propertyName, final Object value) {
            addProperty(propertyName, Utils.getClassNameFromObject(value));
        }        
    };
    
    private boolean types;
    
    
    private PropertyProcessor(boolean types) {
        this.types = types;
    }
    
    protected PropertyProcessor(String className) {
        this(false);        
        this.className = className;
    }
    
    static Map<String, String> processProperty(String propertyName, Object value, boolean types) {
        results = new HashMap<>();
        PropertyProcessor p = (types) ? TYPES : findDelegate(value);
        if (p == null) {
            p = DEFAULT;
        }
        assert p != null;
        p.processPropertyImpl(propertyName, value);
        return results;
    }
    
    abstract void processPropertyImpl(String propertyName, Object value);
    
    protected final void addProperty(String propertyName, String value) {
        if (results.put(propertyName, value) != null) {
            throw new IllegalArgumentException(propertyName);
        }
    }
    
    private static PropertyProcessor findDelegate(final Object value) {
        String clsName = Utils.getClassNameFromObject(value);
        return (PropertyProcessor)clsname2Delegate.get(clsName);
    }       
}
