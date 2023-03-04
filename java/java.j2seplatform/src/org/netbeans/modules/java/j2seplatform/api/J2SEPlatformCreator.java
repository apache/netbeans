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

package org.netbeans.modules.java.j2seplatform.api;

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Creates a new platform definition.
 * @since 1.11
 */
public class J2SEPlatformCreator {

    private J2SEPlatformCreator() {}

    /**
     * Create a new J2SE platform definition.
     * @param installFolder the installation folder of the JDK
     * @return the newly created platform
     * @throws IOException if the platform was invalid or its definition could not be stored
     */
    @NonNull
    public static JavaPlatform createJ2SEPlatform(@NonNull final FileObject installFolder) throws IOException {
        Parameters.notNull("installFolder", installFolder); //NOI18N
        return J2SEPlatformFactory.getInstance().create(installFolder);
    }

    /**
     * Create a new J2SE platform definition with given display name.
     * @param installFolder the installation folder of the JDK
     * @param platformName  the desired display name
     * @return the newly created platform
     * @throws IOException if the platform was invalid or its definition could not be stored
     * @throws IllegalArgumentException if a platform of given display name already exists
     * @since 1.23
     */
    @NonNull
    public static JavaPlatform createJ2SEPlatform(
            @NonNull final FileObject installFolder,
            @NonNull final String platformName) throws IOException , IllegalArgumentException {
        Parameters.notNull("installFolder", installFolder);  //NOI18N
        Parameters.notNull("platformName", platformName);    //NOI18N
        return J2SEPlatformFactory.getInstance().create(installFolder, platformName, true);
    }    
}
