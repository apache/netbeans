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

/**
 *
 * @author rmatous
 */
public class JUnitContentProcessor extends ContentProcessor{
    protected JUnitContentProcessor(String systemOptionInstanceName) {
        super(systemOptionInstanceName);
    }
    
    @Override
    protected Result parseContent(final Iterator<Object> it, boolean types) {
        Map<String, String> properties = new HashMap<>();
        assert it.hasNext();
        Object o = it.next();
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        SerParser.ObjectWrapper ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Integer") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("version", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        assert it.hasNext();
        o = it.next();           
        assert o.getClass().equals(String.class);        
        properties.put("fileSystem", ((types)?"java.lang.String": (String)o));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("membersPublic", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);
        properties.put("membersProtected", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("membersPackage", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("bodyComments", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("bodyContent", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("javaDoc", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateAbstractImpl", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateExceptionClasses", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateSuiteClasses", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("includePackagePrivateClasses", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateMainMethod", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(String.class);        
        properties.put("generateMainMethodBody", ((types)?"java.lang.String": (String)o));//NOI18N
        o = it.next();           
        assert o.getClass().equals(String.class);        
        properties.put("rootSuiteClassName", ((types)?"java.lang.String": (String)o));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateSetUp", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateTearDown", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        
        
        return new DefaultResult(systemOptionInstanceName, properties);
    }        
}
