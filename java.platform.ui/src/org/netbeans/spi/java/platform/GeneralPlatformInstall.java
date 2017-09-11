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

package org.netbeans.spi.java.platform;

import org.netbeans.api.java.platform.JavaPlatform;

/**
 * An super class of all the platform installers. You never subclass directly
 * this class but either the {@link CustomPlatformInstall} or {@link PlatformInstall}
 * <p>The {@link PlatformInstall} or {@link CustomPlatformInstall} instances should be
 * registered in the org-netbeans-api-java/platform/installers folder on the system filesystem.
 * </p>
 * <div class="nonnormative">
 * <p>Registration example:</p>
 * <pre>
 * <a href="@org-openide-util-lookup@/org/openide/util/lookup/ServiceProvider.html">&#64;ServiceProvider</a>(
 *    service=GeneralPlatformInstall.class,
 *    path="org-netbeans-api-java/platform/installers"
 * )
 * public final class MyPlatformInstall extends {@link PlatformInstall} {
 * ...
 * }
 * </pre>
 * 
 * <p>After the {@link PlatformInstall#createIterator createIterator} is finished,
 * a platform definition file shall be created at "Services/Platforms/org-netbeans-api-java-Platform" folder:
 * </p>
 * <pre>
 * public java.util.Set instantiate() throws IOException {
 *     MyPlatform p = new MyPlatform();
 *     p.setDisplayName(theName);
 *     p.setVendor(theVendor);
 *     <a href="@org-openide-loaders@/org/openide/loaders/InstanceDataObject.html">InstanceDataObject.create</a>(
 *         <a href="@org-openide-loaders@/org/openide/loaders/DataFolder.html">DataFolder.findFolder</a>(FileUtil.getConfigFile("Services/Platforms/org-netbeans-api-java-Platform")),
 *         theName,
 *         p,
 *         null,
 *         true);
 *     return Collections.singleton(p);
 * }
 * </pre>
 * <p>
 * The platform definition file has to represent the {@link JavaPlatform} instance.
 * This can be done in many ways. For example using the
 * <a href="@org-netbeans-modules-settings@/org/netbeans/api/settings/ConvertAsJavaBean.html">
 * ConvertAsJavaBean</a> annotation:</p>
 * <pre>
 * &#64;ConvertAsJavaBean
 * public static class MyPlatform extends JavaPlatform {
 *     ...
 * }
 * </pre>
 * </div>
 * @author Tomas Zezula
 * @since 1.5
 */
public abstract class GeneralPlatformInstall {

    GeneralPlatformInstall() {
    }

    /**
     * Gets the display name of the platform installer.
     * If the platform type has a single installer the display name should
     * correspond to the platform name. If there are more installers for
     * a single platform type the display name should also describe the installation process.
     * @return the display name
     */
    public abstract String getDisplayName ();
    
}
