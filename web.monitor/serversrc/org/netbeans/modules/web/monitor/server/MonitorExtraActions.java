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

package org.netbeans.modules.web.monitor.server;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * MonitorExtraActions.java
 *
 *
 * Created: Wed Oct 17 17:16:22 2001
 *
 * @author Ana von Klopp
 * @version
 */

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

    

   
    
