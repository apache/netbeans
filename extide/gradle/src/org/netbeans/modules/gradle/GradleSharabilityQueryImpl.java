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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import java.io.File;
import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Utilities;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleSharabilityQueryImpl implements SharabilityQueryImplementation2 {

    final Project project;

    public GradleSharabilityQueryImpl(Project project) {
        this.project = project;
    }


    @Override
    public SharabilityQuery.Sharability getSharability(URI uri) {
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            if (uri != Utilities.toURI(gbp.getBuildDir()).relativize(uri)) {
                return SharabilityQuery.Sharability.NOT_SHARABLE;
            }
            if (uri != Utilities.toURI(new File(gbp.getRootDir(), ".gradle")).relativize(uri)) {
                return SharabilityQuery.Sharability.NOT_SHARABLE;
            }
        } else {
            return SharabilityQuery.Sharability.UNKNOWN;
        }
        return SharabilityQuery.Sharability.SHARABLE;
    }

}
