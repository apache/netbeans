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

package org.netbeans.modules.j2ee.dd.impl.web.metadata;

import java.util.List;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.openide.util.Exceptions;

/**
 * @author Petr Hejl
 */
public abstract class ServletInfoAccessor {

    private static volatile ServletInfoAccessor accessor;

    public static void setDefault(ServletInfoAccessor accessor) {
        if (ServletInfoAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor"); // NOI18N
        }
        ServletInfoAccessor.accessor = accessor;
    }

    public static ServletInfoAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }

        Class c = ServletInfo.class;
        try {
            Class.forName(c.getName(), true, ServletInfoAccessor.class.getClassLoader());
        } catch (ClassNotFoundException cnf) {
            Exceptions.printStackTrace(cnf);
        }

        return accessor;
    }

    public abstract ServletInfo createServletInfo(String name, String servletClass, List<String> urlPatterns);
}
