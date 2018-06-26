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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.modules.j2ee.deployment.config.J2eeApplicationAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2;

/**
 * Abstraction of J2EE Application. Provides access to basic server-neutral properties
 * of the application: J2EE version, module type, deployment descriptor and its child
 * modules.
 * <p>
 * It is not possible to instantiate this class directly. Implementators have to
 * implement the {@link J2eeApplicationImplementation} first and then use the
 * {@link org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory}
 * to create a J2eeApplication instance.
 *
 * @author Pavel Buzek, Petr Hejl
 */
public class J2eeApplication extends J2eeModule {

    static {
        J2eeApplicationAccessor.setDefault(new J2eeApplicationAccessor() {

            public J2eeApplication createJ2eeApplication(J2eeApplicationImplementation impl) {
                return new J2eeApplication(impl);
            }

            public J2eeApplication createJ2eeApplication(J2eeApplicationImplementation2 impl) {
                return new J2eeApplication(impl);
            }
        });
    }

    private final J2eeApplicationImplementation impl;

    private final J2eeApplicationImplementation2 impl2;

    private J2eeApplication(J2eeApplicationImplementation impl) {
        super(impl);
        this.impl = impl;
        this.impl2 = null;
    }

    private J2eeApplication(J2eeApplicationImplementation2 impl2) {
        super(impl2);
        this.impl = null;
        this.impl2 = impl2;
    }

    /**
     * Returns a list of all the J2EEModules which this J2eeApplication contains.
     *
     * @return list of all the child J2EEModules
     */
    public J2eeModule[] getModules() {
        if (impl2 != null) {
            return impl2.getModules();
        }
        return impl.getModules();
    }

    /**
     * Registers the specified ModuleListener for notification about the module
     * changes.
     *
     * @param listener ModuleListener
     */
    public void addModuleListener(ModuleListener listener) {
        if (impl2 != null) {
            impl2.addModuleListener(listener);
        }
        impl.addModuleListener(listener);
    }

    /**
     * Unregister the specified ModuleListener.
     *
     * @param listener ModuleListener
     */
    public void removeModuleListener(ModuleListener listener) {
        if (impl2 != null) {
            impl2.removeModuleListener(listener);
        }
        impl.removeModuleListener(listener);
    }

}
