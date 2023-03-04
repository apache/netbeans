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

package org.netbeans.modules.php.project;

import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.openide.util.Exceptions;

/**
 * Accessor for {@link PhpLanguageProperties}.
 */
public abstract class PhpLanguagePropertiesAccessor {

    private static volatile PhpLanguagePropertiesAccessor accessor;


    public static void setDefault(PhpLanguagePropertiesAccessor accessor) {
        if (PhpLanguagePropertiesAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor");
        }
        PhpLanguagePropertiesAccessor.accessor = accessor;
    }

    public static synchronized PhpLanguagePropertiesAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }

        Class<?> c = PhpLanguageProperties.class;
        try {
            Class.forName(c.getName(), true, PhpLanguagePropertiesAccessor.class.getClassLoader());
        } catch (ClassNotFoundException cnf) {
            Exceptions.printStackTrace(cnf);
        }
        assert accessor != null;
        return accessor;
    }

    public abstract PhpLanguageProperties createForProject(PhpProject project);

}
