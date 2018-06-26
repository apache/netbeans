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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.config.J2eeApplicationAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;

/**
 * A factory class to create {@link J2eeModule} and {@link J2eeApplication} 
 * instances. You are not permitted to create them directly; instead you implement 
 * {@link J2eeModuleImplementation} or {@link J2eeApplicationImplementation} 
 * and use this factory.
 * 
 * 
 * @author sherold
 * @since 1.23
 */
public class J2eeModuleFactory {
    
    /** Creates a new instance of J2eeModuleFactory */
    private J2eeModuleFactory() {
    }
    
    /**
     * Creates a J2eeModule for the specified J2eeModuleImplementation.
     * 
     * @param impl the J2eeModule SPI object
     * 
     * @return J2eeModule API instance.
     * @deprecated use {@link #createJ2eeApplication(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2)}
     */
    public static J2eeModule createJ2eeModule(J2eeModuleImplementation impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeModuleAccessor.getDefault().createJ2eeModule(impl);
    }

    /**
     * Creates a J2eeModule for the specified J2eeModuleImplementation2.
     *
     * @param impl the J2eeModule SPI object
     *
     * @return J2eeModule API instance.
     */
    public static J2eeModule createJ2eeModule(J2eeModuleImplementation2 impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeModuleAccessor.getDefault().createJ2eeModule(impl);
    }
    
    /**
     * Creates a J2eeApplication for the specified J2eeApplicationImplementation.
     * 
     * 
     * @param impl the J2eeApplication SPI object
     * @return J2eJ2eeApplicationI instance.
     * @deprecated use {@link #createJ2eeApplication(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2)}
     */
    public static J2eeApplication createJ2eeApplication(J2eeApplicationImplementation impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeApplicationAccessor.getDefault().createJ2eeApplication(impl);
    }

    /**
     * Creates a J2eeApplication for the specified J2eeApplicationImplementation.
     *
     *
     * @param impl the J2eeApplication SPI object
     * @return J2eJ2eeApplicationI instance.
     */
    public static J2eeApplication createJ2eeApplication(J2eeApplicationImplementation2 impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeApplicationAccessor.getDefault().createJ2eeApplication(impl);
    }
}
