/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.gradle.java.output;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lkishalmi
 */
public class LookupProviders {

    @LookupProvider.Registration(projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
    public static LookupProvider createJavaBaseProvider() {
        return new LookupProvider() {
            @Override
            public Lookup createAdditionalLookup(Lookup baseContext) {
                return Lookups.fixed(
                        new JavaCompilerProcessorFactory(),
                        new JDPAProcessorFactory()
                );
            }
        };
    }

    @LookupProvider.Registration(projectTypes = {
        @LookupProvider.Registration.ProjectType(id = NbGradleProject.GRADLE_PLUGIN_TYPE + "/com.github.lkishalmi.gatling"),
        @LookupProvider.Registration.ProjectType(id = NbGradleProject.GRADLE_PLUGIN_TYPE + "/io.gatling.gradle")
    })
    public static LookupProvider createGatlingProvider() {
        return (baseContext) -> {
            return Lookups.fixed(
                    new JDPAProcessorFactory()
            );
        };
    }
}
