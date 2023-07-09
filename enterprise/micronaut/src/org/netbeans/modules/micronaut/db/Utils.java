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
package org.netbeans.modules.micronaut.db;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;

/**
 *
 * @author Dusan Balek
 */
public class Utils {

    public static boolean isJPASupported(SourceGroup sg) {
        if (sg == null) {
            return false;
        }
        ClassPath compile = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE);
        if (compile == null) {
            return false;
        }
        final String notNullAnnotation = "io.micronaut.data.jpa.repository.JpaRepository"; //NOI18N
        return compile.findResource(notNullAnnotation.replace('.', '/') + ".class") != null; //NOI18N
    }

    public static boolean isJakartaSupported(SourceGroup sg) {
        if (sg == null) {
            return false;
        }
        ClassPath compile = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE);
        if (compile == null) {
            return false;
        }
        final String notNullAnnotation = "jakarta.persistence.Persistence"; //NOI18N
        return compile.findResource(notNullAnnotation.replace('.', '/') + ".class") != null; //NOI18N
    }
}
