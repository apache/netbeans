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
package org.netbeans.modules.javaee.specs.support.api;

import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderImplementation;

/**
 *
 * @author Petr Hejl
 */
public final class JpaProvider {
    
    static {
        JpaProviderFactory.Accessor.setDefault(new JpaProviderFactory.Accessor() {

            @Override
            public JpaProvider createJpaProvider(JpaProviderImplementation impl) {
                return new JpaProvider(impl);
            }
        });
    }

    private final JpaProviderImplementation impl;

    private JpaProvider(JpaProviderImplementation impl) {
        this.impl = impl;
    }

    public boolean isJpa1Supported() {
        return impl.isJpa1Supported();
    }

    public boolean isJpa2Supported() {
        return impl.isJpa2Supported();
    }

    public boolean isJpa21Supported() {
        return impl.isJpa21Supported();
    }

    public boolean isJpa22Supported() {
        return impl.isJpa22Supported();
    }

    public boolean isJpa30Supported() {
        return impl.isJpa30Supported();
    }

    public boolean isJpa31Supported() {
        return impl.isJpa31Supported();
    }
    
    public boolean isJpa32Supported() {
        return impl.isJpa32Supported();
    }

    public boolean isDefault() {
        return impl.isDefault();
    }

    public String getClassName() {
        return impl.getClassName();
    }
}
