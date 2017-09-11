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
