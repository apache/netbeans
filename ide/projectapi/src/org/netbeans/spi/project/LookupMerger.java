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

package org.netbeans.spi.project;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.util.Lookup;

/**
 * Allows project lookup to merge instances of known classes and replace them
 * with single instance. To be used in conjunction with the {@link org.netbeans.spi.project.LookupProvider}
 * and {@link org.netbeans.spi.project.support.LookupProviderSupport}
 * The interface is to be implemented by the project owner which decides which contracts make sense to have merged and
 * how they are to be merged.
 * The 3rd party {@link org.netbeans.spi.project.LookupProvider} implementors provide instances of mergeableClass.
 * {@link org.netbeans.spi.project.support.LookupProviderSupport#createCompositeLookup} handles the hiding of individual mergeable instances 
 * and exposing the merged instance created by the <code>LookupMerger</code>.
 * @param T the type of object being merged (see {@link org.netbeans.api.project.Project#getLookup} for examples)
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 */
public interface LookupMerger<T> {
    
    /**
     * Returns a class which is merged by this implementation of LookupMerger
     * @return Class instance
     */
    Class<T> getMergeableClass();
    
    /**
     * Merge instances of the given class in the given lookup and return merged 
     * object which substitutes them.
     * @param lookup lookup with the instances
     * @return object to be used instead of instances in the lookup
     */
    T merge(Lookup lookup);

    /**
     * Registers a lookup merger for some project types.
     * The annotated class must be assignable to {@link LookupMerger} with a type parameter.
     * @since org.netbeans.modules.projectapi/1 1.23
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface Registration {

        /**
         * Token(s) denoting one or more project types, e.g. {@code "org-netbeans-modules-java-j2seproject"}
         * {@link LookupProviderSupport#createCompositeLookup} may be used with the path {@code Projects/TYPE/Lookup}.
         */
        String[] projectType() default {};

        /**
         * Alternate registration of project types with positions.
         * You must specify either this or {@link #projectType} (or both).
         */
        ProjectType[] projectTypes() default {};

    }

}
