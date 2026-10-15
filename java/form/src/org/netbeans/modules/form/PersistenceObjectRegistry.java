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


package org.netbeans.modules.form;

import java.util.Map;
import java.util.HashMap;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tran Duc Trung
 */

public class PersistenceObjectRegistry
{

    private static Map<String,String> _nameToClassname = new HashMap<String,String>();
    private static Map<String,String> _classToPrimaryName = new HashMap<String,String>();

    private PersistenceObjectRegistry() {
    }

    public static void registerPrimaryName(String classname, String name) {
        _classToPrimaryName.put(classname, name);
        _nameToClassname.put(name, classname);
    }

    public static void registerPrimaryName(Class clazz, String name) {
        _classToPrimaryName.put(clazz.getName(), name);
        _nameToClassname.put(name, clazz.getName());
    }

    public static void registerAlias(String classname, String alias) {
        _nameToClassname.put(alias, classname);
    }

    public static void registerAlias(Class clazz, String alias) {
        _nameToClassname.put(alias, clazz.getName());
    }

    public static Object createInstance(String classname, FileObject form)
        throws ReflectiveOperationException
    {
        return loadClass(classname, form).getDeclaredConstructor().newInstance();
    }

    public static Class loadClass(String name, FileObject form)
        throws ClassNotFoundException
    {
        name = Utilities.translate(name);
        String classname = _nameToClassname.get(name);
        if (classname == null)
            classname = name;
        return FormUtils.loadClass(classname, form);
    }

    public static String getPrimaryName(Object instance) {
        return getPrimaryName(instance.getClass());
    }

    public static String getPrimaryName(Class clazz) {
        return getPrimaryName(clazz.getName());
    }

    static String getPrimaryName(String className) {
        String name = _classToPrimaryName.get(className);
        return name != null ? name : className;
    }

    static String getClassName(String primaryName) {
        primaryName = Utilities.translate(primaryName);
        String classname = _nameToClassname.get(primaryName);
        return classname != null ? classname : primaryName;
    }
}
