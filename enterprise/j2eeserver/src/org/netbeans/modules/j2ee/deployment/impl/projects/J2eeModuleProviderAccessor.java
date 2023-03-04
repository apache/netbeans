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

package org.netbeans.modules.j2ee.deployment.impl.projects;

import org.netbeans.modules.j2ee.deployment.config.ConfigSupportImpl;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.Exceptions;

/**
 * Utility class for accessing some of the non-public methods of the J2eeModuleProvider.
 *
 * @author Petr Hejl
 */
public abstract class J2eeModuleProviderAccessor {

    private static volatile J2eeModuleProviderAccessor accessor;

    public static void setDefault(J2eeModuleProviderAccessor accessor) {
        if (J2eeModuleProviderAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor"); // NOI18N
        }
        J2eeModuleProviderAccessor.accessor = accessor;
    }

    public static J2eeModuleProviderAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }

        Class c = J2eeModuleProvider.class;
        try {
            Class.forName(c.getName(), true, J2eeModuleProviderAccessor.class.getClassLoader());
        } catch (ClassNotFoundException cnf) {
            Exceptions.printStackTrace(cnf);
        }

        return accessor;
    }

    public abstract ConfigSupportImpl getConfigSupportImpl(J2eeModuleProvider impl);
}
