/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.core.api.support.classpath;

import org.openide.filesystems.FileObject;



/**
 * Contract provided by some project types that don't put the entire application
 * server content on classpath (eg Maven) in the project's lookup.
 * Intended to be called by code that generates content into the project and assumes
 * certain j2ee api binaries to be on the project's classpath.
 *
 *<p>
 *
 * The usual workflow is to identify what APis the file template or framework (or anything else)
 * needs, then look up this interface from project's lookup. If present, then call the <code>extendClassPath()</code>
 * method with the list of symbolic names of the APIs in question.
 *
 * @author mkleint
 * @since org.netbeans.modules.j2ee.core.utilities 1.3
 */
public interface ContainerClassPathModifier {

    public final String API_SERVLET = "servlet-api"; //NOI18N
    public final String API_JSP = "jsp-api"; //NOI18N
    public final String API_JSF = "jsf-api"; //NOI18N
    /**
     * a fallback solution, adds the complete j2ee stack on classpath
     */
    public final String API_J2EE = "j2ee-api"; //NOI18N
    public final String API_PERSISTENCE = "persistence"; //NOI18N
    public final String API_ANNOTATION = "annotation"; //NOI18N
    public final String API_TRANSACTION = "transaction"; //NOI18N
    public final String API_EJB = "ejb"; //NOI18N


    /**
     * Extend the project's classpath with the application container's API required
     * by the functionality added to the project.
     * To be called outside of the Swing Event (AWT) thread as the processing can take a long time under some conditions.
     *
     * @param relevantFile a file from the project that was either created or is
     * from the project and is on correct classpath.
     * @param symbolicNames array of constant value for apis required. The list of supported
     * constants is included in this interface declaration.
     */
    void extendClasspath(FileObject relevantFile, String[] symbolicNames);
}
