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
package org.netbeans.modules.javaee.specs.support.api;

import org.netbeans.modules.javaee.specs.support.DefaultEjbSupportImpl;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.spi.EjbSupportImplementation;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 * @since 1.4
 */
public final class EjbSupport {

    private final EjbSupportImplementation impl;

    private EjbSupport(EjbSupportImplementation impl) {
        this.impl = impl;
    }

    @NonNull
    public static EjbSupport getInstance(@NonNull J2eePlatform j2eePlatform) {
        Parameters.notNull("j2eePlatform", j2eePlatform);
        EjbSupportImplementation supportImpl = j2eePlatform.getLookup().lookup(EjbSupportImplementation.class);
        if (supportImpl != null) {
            return new EjbSupport(supportImpl);
        }
        return new EjbSupport(new DefaultEjbSupportImpl());
    }

    /**
     * Says if the EJB 3.1 Lite is supported by {@link J2eePlatform}.
     *
     * @param j2eePlatform j2eePlatform
     * @return {@code true} if the server supports EJB Lite, {@code false} otherwise
     */
    public boolean isEjb31LiteSupported(@NonNull J2eePlatform j2eePlatform) {
        return impl.isEjb31LiteSupported(j2eePlatform);
    }

}
