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
package org.netbeans.modules.templates;

import java.lang.reflect.InvocationTargetException;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

public final class HTMLWizard {
    private HTMLWizard() {
    }

    public static Object create(FileObject data)
    throws NoSuchMethodException, IllegalAccessException, 
    IllegalArgumentException, InvocationTargetException {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = HTMLWizard.class.getClassLoader();
        }
        Class<?> clazz;
        try {
            clazz = Class.forName("org.netbeans.modules.templatesui.HTMLWizard", true, l); // NOI18N
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                "Cannot load class from 'org.netbeans.modules.templatesui' module. " + // NOI18N
                "Fix that (in NetBeans Runtime Container) by requesting " // NOI18N
                + "token 'org.netbeans.api.templates.wizard'.", ex // NOI18N
            );
        }
        return clazz.getMethod("create", FileObject.class).invoke(null, data); // NOI18N
    }
}
