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

package org.netbeans.modules.javaee.project.api.ear;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.spi.ear.EarDDGeneratorImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * A supplementary API for generation EAR project's deployment descriptor (<i>application.xml</i>).
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 *
 * @see EarDDGeneratorImpl
 * @since 1.6
 */
public final class EarDDGenerator {

    private EarDDGenerator() {
    }

    /**
     * Generate deployment descriptor (<i>application.xml</i>) if needed or forced (applies for Java EE 5).
     * <p>
     * For J2EE 1.4 or older the deployment descriptor is always generated if missing.
     * For Java EE 5 it is only generated if missing and forced as well.
     *
     * @param project EAR project instance.
     * @param force if {@code true} <i>application.xml</i> is generated even if it's not needed (applies only for Java EE 5).
     *
     * @return {@link FileObject} of the deployment descriptor or {@code null} if <i>application.xml</i> were not successfully created.
     *
     * @see EarDDGeneratorImpl#generate(Project, Profile, FileObject, boolean) 
     * @since 1.6
     */
    @CheckForNull
    public static FileObject setupDD(
            @NonNull Project project,
            boolean force) {

        Parameters.notNull("project", project); //NOI18N

        EarDDGeneratorImplementation ddGenerator = project.getLookup().lookup(EarDDGeneratorImplementation.class);
        if (ddGenerator != null) {
            return ddGenerator.setupDD(force);
        }

        // Maybe we should throw UnsupportedProjectTypeException here
        return null;
    }
}
