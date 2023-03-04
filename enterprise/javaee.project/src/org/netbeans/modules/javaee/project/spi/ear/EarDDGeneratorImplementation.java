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

package org.netbeans.modules.javaee.project.spi.ear;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.ear.EarDDGenerator;
import org.openide.filesystems.FileObject;

/**
 * A SPI for EAR project's which needs to generate <i>application.xml</i> deployment descriptor.
 * <p>
 * Projects can provide implementation of this interface in its {@link Project#getLookup lookup}
 * to allow clients to generate <i>application.xml</i> independently on the build system.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 *
 * @see EarDDGenerator
 * @since 1.6
 */
public interface EarDDGeneratorImplementation {

    /**
     * Generate deployment descriptor (<i>application.xml</i>) if needed or forced (applies for Java EE 5).
     * <p>
     * For J2EE 1.4 or older the deployment descriptor is always generated if missing.
     * For Java EE 5 it is only generated if missing and forced as well.
     *
     * @param profile Java EE profile.
     * @param force if {@code true} <i>application.xml</i> is generated even if it's not needed (applies only for Java EE 5).
     *
     * @return {@link FileObject} of the deployment descriptor or {@code null} if <i>application.xml</i> were not successfully created.
     *
     * @see EarDDGenerator#setupDD(Project, Profile, FileObject, boolean)
     * @since 1.6
     */
    FileObject setupDD(boolean force);
    
}
