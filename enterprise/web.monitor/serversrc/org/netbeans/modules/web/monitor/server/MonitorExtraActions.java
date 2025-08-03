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

package org.netbeans.modules.web.monitor.server;

import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

/**
 * Containers who wish to provide servlet information and/or ability
 * to reset the session cookie to the HTTP Monitor must implement one
 * or both methods from this interface.
 */
public interface MonitorExtraActions {


    /**
     * This method returns a handle on the servlet that processes the
     * request.
     */
    public Servlet getServlet(HttpServletRequest request, 
				       FilterChain chain);
         

    /**
     * This method evaluates the cookies that come in through the
     * headers for a JSESSIONID cookie. If such a cookie is present,
     * the method replaces the current session with the session
     * corresponding to the ID from the cookie, if the session is
     * still present. If the session no longer exists, or if the
     * request did not include a session cookie, any existing session
     * will no longer associated with the request. 
     */
    public void replaceSessionID(HttpServletRequest request); 


    public boolean canReplaceSessionID();
    
}

    

   
    
