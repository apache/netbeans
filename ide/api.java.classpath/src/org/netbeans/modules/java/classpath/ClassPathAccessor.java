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
package org.netbeans.modules.java.classpath;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;


public abstract class ClassPathAccessor {

    private static volatile ClassPathAccessor DEFAULT;

    
    public static synchronized ClassPathAccessor getDefault() {
        ClassPathAccessor instance = DEFAULT;
        if (instance == null) {
            Class<?> c = ClassPath.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
                instance = DEFAULT;
                assert instance != null;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return instance;
    }
    
    public static void setDefault(@NonNull final ClassPathAccessor accessor) {
        Parameters.notNull("accessor", accessor);   //NOI18N
        DEFAULT = accessor;
    }

    public abstract ClassPath createClassPath(ClassPathImplementation spiClasspath);

    public abstract ClassPathImplementation getClassPathImpl (ClassPath cp);

}
