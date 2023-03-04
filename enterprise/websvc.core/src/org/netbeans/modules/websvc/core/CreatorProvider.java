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

package org.netbeans.modules.websvc.core;

import org.netbeans.modules.websvc.spi.support.ClientCreatorProvider;
import org.netbeans.modules.websvc.api.support.ClientCreator;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.support.ServiceCreator;
import org.netbeans.modules.websvc.spi.support.ServiceCreatorProvider;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @author mkuchtiak
 */
public class CreatorProvider {

    private static final Lookup.Result<ClientCreatorProvider> clientProviders =
        Lookup.getDefault().lookup(new Lookup.Template<ClientCreatorProvider>(ClientCreatorProvider.class));

    private static final Lookup.Result<ServiceCreatorProvider> serviceProviders =
        Lookup.getDefault().lookup(new Lookup.Template<ServiceCreatorProvider>(ServiceCreatorProvider.class));
     
    private static final Lookup.Result<HandlerCreatorProvider> handlerProviders =
        Lookup.getDefault().lookup(new Lookup.Template<HandlerCreatorProvider>(HandlerCreatorProvider.class));

    public static ClientCreator getClientCreator(Project project, WizardDescriptor wiz) {
        Collection<? extends ClientCreatorProvider> instances = clientProviders.allInstances();
        for (ClientCreatorProvider impl: instances) {
            ClientCreator creator = impl.getClientCreator(project,wiz);
            if (creator != null) {
                return creator;
            }
        }
        return null;
    }
    
    public static ServiceCreator getServiceCreator(Project project, WizardDescriptor wiz) {
        Collection<? extends ServiceCreatorProvider> instances = serviceProviders.allInstances();
        for (ServiceCreatorProvider impl: instances) {
            ServiceCreator creator = impl.getServiceCreator(project,wiz);
            if (creator != null) {
                return creator;
            }
        }
        return null;
    }
    
    public static HandlerCreator getHandlerCreator(Project project, WizardDescriptor wiz) {
        Collection<? extends HandlerCreatorProvider> instances = handlerProviders.allInstances();
        for (HandlerCreatorProvider impl: instances) {
            HandlerCreator creator = impl.getHandlerCreator(project,wiz);
            if (creator != null) {
                return creator;
            }
        }
        return null;
    }   
}
