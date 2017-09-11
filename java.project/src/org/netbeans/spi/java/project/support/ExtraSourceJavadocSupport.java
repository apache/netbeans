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

package org.netbeans.spi.java.project.support;

import org.netbeans.api.project.Project;
import org.netbeans.modules.java.project.ExtraProjectSourceForBinaryQueryImpl;
import org.netbeans.modules.java.project.ExtraProjectJavadocForBinaryQueryImpl;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Utility factory class for ant-based project implementors, the created instances, once
 * put into project's lookup, make sure the javadoc and source references in project properties
 * are correctly recognized and used by the java infrastructure.
 * The format of project properties used is as follows:
 * <ul>
 *      <li>file.reference.__jarName__ - the binary file reference used to point to files on project classpath. </li>
 *      <li>javadoc.reference.__jarName__ - the file reference pointing to javadoc for file.reference.__jarName__ </li>
 *      <li>source.reference.__jarName__ - the file reference pointing to sources for file.reference.__jarName__ </li>
 * </ul>
 * @author mkleint
 * @since org.netbeans.modules.java.project 1.14
 */
public class ExtraSourceJavadocSupport {

    /**
     * Create project's SourceForBinaryQueryImplementation object for handling 
     * property based sources
     * @param helper project's AntProjectHelper instance
     * @param eval project's PropertyEvaluator instance
     * @return object to use in project's lookup.
     */
    public static SourceForBinaryQueryImplementation createExtraSourceQueryImplementation (Project project, AntProjectHelper helper, PropertyEvaluator eval) {
        return new ExtraProjectSourceForBinaryQueryImpl(project, helper, eval);
    }
    
    /**
     * Create project's JavadocForBinaryQueryImplementation object for handling 
     * property based javadoc
     * @param helper project's AntProjectHelper instance
     * @param eval project's PropertyEvaluator instance
     * @return object to use in project's lookup.
     */
    public static JavadocForBinaryQueryImplementation createExtraJavadocQueryImplementation (Project project, AntProjectHelper helper, PropertyEvaluator eval) {
        return new ExtraProjectJavadocForBinaryQueryImpl(project, helper, eval);
    }


}
