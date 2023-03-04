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

/**
 * Extends the functionality of {@link FindJSPServlet} by defining
 * the package name of the compiled JSPs and the prefix of source
 * path. In order to use it return implementation of this class from
 * {@link OptionalDeploymentManagerFactory#getFindJSPServlet(javax.enterprise.deploy.spi.DeploymentManager)}.
 *
 * @author Petr Hejl
 * @since 1.78
 */
public interface FindJSPServlet2 extends FindJSPServlet {

    /**
     * Returns the package name of compiled JSPs.
     * 
     * @param moduleContextPath the context path
     * @return package name of compiled JSPs
     */
    String getServletBasePackageName(String moduleContextPath);
    
    /**
     * Returns the relative source of the JSP reported in class file. For
     * example the JSP in web root is "index.jsp", but the server compile it as
     * coming from "jsp_servlet/index.jsp". This method should return
     * "jsp_servlet/index.jsp".
     * 
     * @param moduleContextPath the context path
     * @param jspRelativePath the relative path of the JSP
     * @return prefix of the source path reported in class file
     */
    String getServletSourcePath(String moduleContextPath, String jspRelativePath);
    
}
