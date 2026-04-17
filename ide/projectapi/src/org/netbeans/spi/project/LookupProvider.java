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

package org.netbeans.spi.project;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.util.Lookup;

/**
 * interface for inclusion of 3rd party content in project's lookup. Typically, if the 
 * project type allows composition of lookup from multiple sources, it will make a layer
 * location public where 3rd parties will register implementations of this interface.
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 * @see LookupProviderSupport#createCompositeLookup
 */
public interface LookupProvider {
    
    /**
     * implementations will be asked to create their additional project lookup based on the baseContext
     * passed as parameter. The content of baseLookup is undefined on this level, is a contract
     * of the actual project type. Can be complete lookup of the project type, a portion of it or
     * something completely different that won't appear in the final project lookup.
     * Each implementation is only asked once for it's lookup for a given project instance at the time 
     * when project's lookup is being created.
     * @param baseContext implementation shall decide what to return for a given project instance based on context
     *  passed in.
     * @return a {@link org.openide.util.Lookup} instance that is to be added to the project's lookup, never null.
     */ 
    Lookup createAdditionalLookup(Lookup baseContext);

    /**
     * Annotation to register {@link LookupProvider} instances.
     * <p>If you wish to unconditionally register one or more objects,
     * it will be more efficient and may be easier to use
     * {@link ProjectServiceProvider} (and/or {@link LookupMerger.Registration}).
     * @since org.netbeans.modules.projectapi 1.21
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Registration {

        /**
         * Token(s) denoting one or more project types, e.g. org-netbeans-modules-maven or org-netbeans-modules-java-j2seproject
         * {@link LookupProviderSupport#createCompositeLookup} may be used with the path {@code Projects/TYPE/Lookup}.
         */
        String[] projectType() default {};

        /**
         * Alternate registration of project types with positions.
         * You must specify either this or {@link #projectType} (or both).
         * @since org.netbeans.modules.projectapi/1 1.22
         */
        ProjectType[] projectTypes() default {};

        /**
         * Alternate individual registration for one project type.
         * @since org.netbeans.modules.projectapi/1 1.22
         */
        @Retention(RetentionPolicy.SOURCE)
        @Target({})
        @interface ProjectType {

            /**
             * Token denoting project type.
             * @see org.netbeans.spi.project.LookupProvider.Registration#projectType
             * @see org.netbeans.spi.project.LookupMerger.Registration#projectType
             * @see org.netbeans.spi.project.ProjectServiceProvider#projectType
             */
            String id();

            /**
             * Optional ordering.
             */
            int position() default Integer.MAX_VALUE;
        }
    }

}
