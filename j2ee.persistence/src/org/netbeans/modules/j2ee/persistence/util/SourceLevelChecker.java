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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.persistence.util;

import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;

/**
 * A helper class for checking the source level of projects.
 * 
 * @author Erno Mononen
 */
public class SourceLevelChecker {
    
    private SourceLevelChecker() {
    }

    /**
     * Checks whether the source level of the given <code>project</code>
     * is <code>1.4</code> or lower.
     * 
     * @return true if the source level of the given project was 1.4 or lower, false 
     * otherwise.
     */
    public static boolean isSourceLevel14orLower(Project project) {
        String srcLevel = getSourceLevel(project);
        return srcLevel != null ? srcLevel.matches("1\\.[0-4]([^0-9].*)?"): false;//only 1.0-1.4 should return true, also 1.1.3, 1.1_3 should return true, 1.10>1.9 and return false.
    }
    
    /**
     * Return source level 
     *
     * @return source level for the project
     */
    public static String getSourceLevel(Project project) {
        String srcLevel = null;
        SourceLevelQueryImplementation2 sl2 = project.getLookup().lookup(SourceLevelQueryImplementation2.class);
        if(sl2 != null){
            srcLevel = sl2.getSourceLevel(project.getProjectDirectory()).getSourceLevel();
        } else {
            //backward compartibility
            SourceLevelQueryImplementation sl = project.getLookup().lookup(SourceLevelQueryImplementation.class);
            if(sl != null){
                srcLevel = sl.getSourceLevel(project.getProjectDirectory());
            }
        }
        return srcLevel;
    }
}
