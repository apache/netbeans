/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
