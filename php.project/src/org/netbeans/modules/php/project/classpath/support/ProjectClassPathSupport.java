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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.php.project.classpath.support;

import java.io.File;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

// copied from java.project
/**
 * ProjectClassPathSupport is a support class for creating classpath based
 * on the list of ant properties.
 * @author Tomas Zezula
 */
public final class ProjectClassPathSupport {

    /** Creates a new instance of NewClass */
    private ProjectClassPathSupport() {
    }


    /**
     * Creates new classpath based on the ant property. The returned classpath
     * listens on changes of property value.
     * @param projectFolder {@link File} the project folder used to resolve relative paths
     * @param evaluator {@link PropertyEvaluator} used for obtaining the value of
     * given property and listening on value changes.
     * @param propertyNames the names of ant properties the classpath will be build on,
     * can't be or contain null. It can contain duplicates, in this case the duplicated property
     * is used multiple times. The values of given properties are concatenated into a single path.
     * @return an {@link ClassPathImplementation} based on the given ant properties.
     */
    public static ClassPathImplementation createPropertyBasedClassPathImplementation(File projectFolder,
            PropertyEvaluator evaluator, String[] propertyNames) {
        return new ProjectClassPathImplementation(projectFolder, propertyNames, evaluator);
    }
}
