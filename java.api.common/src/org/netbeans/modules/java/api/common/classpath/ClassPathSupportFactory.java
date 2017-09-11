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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.classpath;

import java.net.URL;
import java.util.function.Function;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Support class for creating different types of classpath related implementations.
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class ClassPathSupportFactory {

    private ClassPathSupportFactory() {
    }

    /**
     * Creates implementation of BOOT classpath based on project's <code>platform.active</code>
     * property.
     * @param evaluator project's property evaluator
     * @return classpath implementation
     */
    public static ClassPathImplementation createBootClassPathImplementation(PropertyEvaluator evaluator) {
        return createBootClassPathImplementation(evaluator, null, null);
    }

    /**
     * Creates implementation of BOOT classpath based on project's <code>platform.active</code>
     * property and given endorsed classpath which will have precedence of platform classpath.
     * @param evaluator project's property evaluator
     * @param endorsedClassPath endorsed classpath to prepend to boot classpath
     * @return classpath implementation
     * @since org.netbeans.modules.java.api.common/0 1.11
     */
    public static ClassPathImplementation createBootClassPathImplementation(PropertyEvaluator evaluator, ClassPath endorsedClassPath) {
        return createBootClassPathImplementation(evaluator, null, endorsedClassPath, null);
    }

    /**
     * Creates implementation of BOOT classpath based on project's <code>platform.active</code>
     * property and given endorsed classpath which will have precedence of platform classpath.
     * @param evaluator project's property evaluator
     * @param endorsedClassPath endorsed classpath to prepend to boot classpath
     * @param platformType the type of {@link JavaPlatform}
     * @return classpath implementation
     * @since 1.59
     */
    public static ClassPathImplementation createBootClassPathImplementation(
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final ClassPath endorsedClassPath,
            @NullAllowed final String platformType) {
        return createBootClassPathImplementation(evaluator, null, endorsedClassPath, platformType);
    }
    
    public static ClassPathImplementation createBootClassPathImplementation(
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final Project project,
            @NullAllowed final ClassPath endorsedClassPath,
            @NullAllowed final String platformType) {
        return new BootClassPathImplementation(project, evaluator, endorsedClassPath, platformType);
    }

    /**
     * Creates implementation of SOURCE classpath for given source roots and project
     * assuming build classes folder is stored in property <code>build.dir</code>.
     *
     * @param sourceRoots project source roots
     * @param projectHelper AntProjectHelper
     * @param evaluator PropertyEvaluator
     * @return classpath implementation
     */
    public static ClassPathImplementation createSourcePathImplementation(SourceRoots sourceRoots, AntProjectHelper projectHelper, PropertyEvaluator evaluator) {
        return new SourcePathImplementation(sourceRoots, projectHelper, evaluator);
    }

    /**
     * 
     * @param base
     * @param sourceRoots
     * @param systemModules
     * @param userModules
     * @param legacyClassPath
     * @param filter
     * @return 
     */
    public static ClassPathImplementation createModuleInfoBasedPath(
            @NonNull final ClassPath base,
            @NonNull final ClassPath sourceRoots,
            @NonNull final ClassPath systemModules,
            @NonNull final ClassPath userModules,
            @NullAllowed final ClassPath legacyClassPath,
            @NullAllowed final Function<URL,Boolean> filter) {
        return ModuleClassPaths.createModuleInfoBasedPath(base, sourceRoots, systemModules, userModules, legacyClassPath, filter);
    }
}
