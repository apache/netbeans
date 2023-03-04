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
