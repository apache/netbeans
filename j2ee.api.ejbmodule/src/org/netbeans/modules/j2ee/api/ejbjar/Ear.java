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
package org.netbeans.modules.j2ee.api.ejbjar;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.ejbjar.EarAccessor;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Ear should be used to access properties of an ear module.
 * <p>
 * A client may obtain a Ear instance using
 * <code>Ear.getEar(fileObject)</code> static method, for any
 * FileObject in the ear module directory structure.
 * </p>
 * <div class="nonnormative">
 * Note that the particular directory structure for ear module is not guaranteed 
 * by this API.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class Ear {
    
    private static final Lookup.Result<EarProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template<EarProvider>(EarProvider.class));
    
    static  {
        EarAccessor.DEFAULT = new EarAccessor() {

            @Override
            public Ear createEar(EarImplementation spiEar) {
                return new Ear(spiEar, null);
            }

            @Override
            public Ear createEar(EarImplementation2 spiEar) {
                return new Ear(null, spiEar);
            }
        };
    }

    @SuppressWarnings("deprecation")
    private final EarImplementation impl;

    private final EarImplementation2 impl2;

    @SuppressWarnings("deprecation")
    private Ear (EarImplementation impl, EarImplementation2 impl2) {
        assert (impl != null && impl2 == null) || (impl == null && impl2 != null);
        this.impl = impl;
        this.impl2 = impl2;
    }
    
    /**
     * Find the Ear for given file or <code>null</code> if the file does not
     * belong to any Enterprise Application.
     */
    public static Ear getEar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to Ear.getEar(FileObject)"); // NOI18N
        }
        for (EarProvider earProvider : implementations.allInstances()) {
            Ear wm = earProvider.findEar(f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
     * @return J2EE platform version
     * @deprecated use {@link #getJ2eeProfile()}
     */
    public String getJ2eePlatformVersion () {
        if (impl2 != null) {
            // TODO null happens when EAR is deleted and getApplication is called
            // invent better fix #168399
            Profile profile = impl2.getJ2eeProfile();
            if (profile != null) {
                return profile.toPropertiesString();
            }
            return null;
        }
        return impl.getJ2eePlatformVersion();
    }

    public Profile getJ2eeProfile() {
        if (impl2 != null) {
            return impl2.getJ2eeProfile();
        }
        return Profile.fromPropertiesString(impl.getJ2eePlatformVersion());
    }
    
    /** Deployment descriptor (ejb-jar.xml file) of the ejb module.
     */
    public FileObject getDeploymentDescriptor () {
        if (impl2 != null) {
            impl2.getDeploymentDescriptor();
        }
        return impl.getDeploymentDescriptor();
    }
    
    /** Add j2ee webmodule into application.
     * @param module the module to be added
     */
    public void addWebModule (WebModule module) {
        if (impl2 != null) {
            impl2.addWebModule(module);
        } else {
            impl.addWebModule (module);
        }
    }
    
    /** Add j2ee Ejb module into application.
     * @param module the module to be added
     */
    public void addEjbJarModule (EjbJar module) {
        if (impl2 != null) {
            impl2.addEjbJarModule(module);
        } else {
            impl.addEjbJarModule (module);
        }
    }
    
    /** Add j2ee application client module into application.
     * @param module the module to be added
     */
    public void addCarModule(Car module) {
        if (impl2 != null) {
            impl2.addCarModule(module);
        } else {
            impl.addCarModule(module);
        }
    }
    
}
