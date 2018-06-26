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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.util.Lookup;

/**
 * An interface that defines a container for all the server-specific configuration 
 * information for a single top-level J2EE module. The ModuleConfiguration object 
 * could represent a single stand-alone module or a J2EE application that contains 
 * several sub-modules. The ModuleConfiguration object contains in its lookup a set 
 * of configurations that are used for managing the server-specific settings.
 *
 * @since 1.23
 * @author sherold
 */
public interface ModuleConfiguration extends Lookup.Provider {
    
    /**
     * Returns lookup associated with the object. This lookup should contain
     * implementations of all the supported configurations.
     * <p>
     * The configuration are:  {@link ContextRootConfiguration},  {@link DatasourceConfiguration}, 
     * {@link MappingConfiguration}, {@link EjbResourceConfiguration}, {@link DeploymentPlanConfiguration},
     * {@link MessageDestinationConfiguration}
     * <p>
     * Implementators are advised to use {@link org.openide.util.lookup.Lookups#fixed}
     * to implement this method.
     * 
     * @return lookup associated with the object containing all the supported
     *         ConfigurationProvider implementations.
     */
    Lookup getLookup();
    
    /**
     * Returns a J2EE module associated with this ModuleConfiguration instance.
     * 
     * @return a J2EE module associated with this ModuleConfiguration instance.
     */
    J2eeModule getJ2eeModule();
    
    /**
     * The j2eeserver calls this method when it is done using this ModuleConfiguration 
     * instance. The server plug-in should free all the associated resources -
     * listeners for example.
     */
    void dispose();
}
