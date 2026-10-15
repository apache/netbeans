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
package org.netbeans.modules.javaee.specs.support.spi;

import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
public final class JpaProviderFactory {
    
    public static JpaProvider createJpaProvider(JpaProviderImplementation impl) {
        return Accessor.getDefault().createJpaProvider(impl);
    }
    
    public static JpaProvider createJpaProvider(final String className, final boolean isDefault,
            final boolean isJpa1Supported, final boolean isJpa2Supported, final boolean isJpa21Supported,
            final boolean isJpa22Supported, final boolean isJpa30Supported, final boolean isJpa31Supported,
            final boolean isJpa32Supported) {
        return Accessor.getDefault().createJpaProvider(new JpaProviderImplementation() {

            @Override
            public boolean isJpa1Supported() {
                return isJpa1Supported;
            }

            @Override
            public boolean isJpa2Supported() {
                return isJpa2Supported;
            }

            @Override
            public boolean isJpa21Supported() {
                return isJpa21Supported;
            }

            @Override
            public boolean isJpa22Supported() {
                return isJpa22Supported;
            }

            @Override
            public boolean isJpa30Supported() {
                return isJpa30Supported;
            }

            @Override
            public boolean isJpa31Supported() {
                return isJpa31Supported;
            }
            
            @Override
            public boolean isJpa32Supported() {
                return isJpa32Supported;
            }
            
            @Override
            public boolean isDefault() {
                return isDefault;
            }

            @Override
            public String getClassName() {
                return className;
            }
        });
    } 
    
    public abstract static class Accessor {

        private static volatile Accessor accessor;

        public static void setDefault(Accessor accessor) {
            if (Accessor.accessor != null) {
                throw new IllegalStateException("Already initialized accessor"); // NOI18N
            }
            Accessor.accessor = accessor;
        }

        public static Accessor getDefault() {
            if (accessor != null) {
                return accessor;
            }

            Class c = JpaProvider.class;
            try {
                Class.forName(c.getName(), true, Accessor.class.getClassLoader());
            } catch (ClassNotFoundException cnf) {
                Exceptions.printStackTrace(cnf);
            }

            return accessor;
        }
        
        public abstract JpaProvider createJpaProvider(JpaProviderImplementation impl);
    }
}
