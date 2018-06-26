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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.io.File;

/** This interface allows plugins to specify the location of servlets generated
 * for JSPs.
 *
 * @author Petr Jiricka
 */
// FIXME perhaps the better API should pass application name or TargetModuleID
public interface FindJSPServlet {

    /** Returns the temporary directory where the server writes servlets generated
     * from JSPs. The servlets placed in this directory must honor the Java
     * directory naming conventions, i.e. the servlet must be placed in subdirectories
     * of this directory corresponding to the servlet package name.
     * @param moduleContextPath web module for which the temporary directory is requested.
     * @return the root temp directory containing servlets generated from JSPs for this module.
     */
    public File getServletTempDirectory(String moduleContextPath);
    
    /** Returns the resource path of the servlet generated for a particular JSP, relatively
     * to the main temporary directory.
     * @param moduleContextPath context path of web module in which the JSP is located.
     * @param jspResourcePath the path of the JSP for which the servlet is requested, e.g.
     *  "pages/login.jsp". Never starts with a '/'.
     * @return the resource name of the servlet generated for the JSP in the module, e.g.
     *  "org/apache/jsps/pages/login$jsp.java". Must never start with a '/'.
     *  The servlet file itself does not need to exist at this point - 
     *  if this particular page was not compiled yet.
     */
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath);
    
    /** Returns the encoding of the generated servlet file.
     * @param moduleContextPath context path of web module in which the JSP is located.
     * @param jspResourcePath the path of the JSP for which the servlet is requested, e.g.
     *  "pages/login.jsp". Never starts with a '/'.
     * @return the encoding of the servlet generated for the JSP in the module, 
     *  e.g. "UTF8".
     *  The servlet file itself does not need to exist at this point -
     *  if this particular page was not compiled yet.
     */
    public String getServletEncoding(String moduleContextPath, String jspResourcePath);
    
}
