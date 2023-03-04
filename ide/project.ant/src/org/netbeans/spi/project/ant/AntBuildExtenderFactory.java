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

package org.netbeans.spi.project.ant;

import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 * Factory class for creation of AntBuildExtender instances
 * @author mkleint
 * @since org.netbeans.modules.project.ant 1.16
 */
public final class AntBuildExtenderFactory {
    
    /** Creates a new instance of AntBuildExtenderSupport */
    private AntBuildExtenderFactory() {
    }
    
    /**
     * Create instance of {@link org.netbeans.api.project.ant.AntBuildExtender} that is
     * to be included in project's lookup.
     * @param implementation project type's spi implementation
     * @return resulting <code>AntBuildExtender</code> instance
     * @deprecated Use {@link #createAntExtender(AntBuildExtenderImplementation, ReferenceHelper)} instead
     */
    @Deprecated
    public static AntBuildExtender createAntExtender(AntBuildExtenderImplementation implementation) {
        return AntBuildExtenderAccessor.DEFAULT.createExtender(implementation);
    }
    
    /**
     * Create instance of {@link org.netbeans.api.project.ant.AntBuildExtender} that is
     * to be included in project's lookup.
     * @param implementation project type's spi implementation
     * @param refHelper project related reference helper
     * @return resulting <code>AntBuildExtender</code> instance
     * @since 1.23
     */
    public static AntBuildExtender createAntExtender(AntBuildExtenderImplementation implementation, ReferenceHelper refHelper) {
        return AntBuildExtenderAccessor.DEFAULT.createExtender(implementation, refHelper);
    }
    
}
