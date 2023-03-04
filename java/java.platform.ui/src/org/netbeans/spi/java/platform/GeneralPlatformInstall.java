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
