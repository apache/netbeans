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
package org.netbeans.modules.websvc.project;

import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.api.ServiceDescriptor;
import org.netbeans.modules.websvc.project.spi.ServiceDescriptorImplementation;
import org.netbeans.modules.websvc.project.spi.WebServiceImplementation;

/**
 * This class provides access to the {@link WebService}'s  private constructor.
 * A concrete instance of this class is implemented in {@link WebService} and the
 * instance is assigned to {@link DEFAULT}.
 * @see WebService
 */

public abstract class WebServiceAccessor {

    public static WebServiceAccessor DEFAULT;
    
    public static WebServiceAccessor getDefault() {
        if (DEFAULT != null) {
            return DEFAULT;
        }
    
        Class c = WebService.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        return DEFAULT;
    }
    
    public abstract WebService createWebService(WebServiceImplementation serviceImpl);

    public abstract static class DescriptorAccessor {
        public static DescriptorAccessor DEFAULT;

        public static DescriptorAccessor getDefault() {
            if (DEFAULT != null) {
                return DEFAULT;
            }

            Class c = ServiceDescriptor.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
            return DEFAULT;
        }

        public abstract ServiceDescriptor createWebServiceDescriptor(ServiceDescriptorImplementation descImpl);
    }
}
