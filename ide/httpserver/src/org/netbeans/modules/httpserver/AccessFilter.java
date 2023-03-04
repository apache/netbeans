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

package org.netbeans.modules.httpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.ServerException;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Filter to protect/limit access to NetBeans Open APIs and resources.
 */
public class AccessFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
        if(! (req instanceof HttpServletRequest)) {
            throw new ServerException("Invalid access");
        }

        if (!checkAccess(((HttpServletRequest) req))) {
            ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN,
                    NbBundle.getMessage(AccessFilter.class, "MSG_HTTP_FORBIDDEN"));
        }

        fc.doFilter(req, resp);
    }


    /**
     * Checks whether access should be permitted according to HTTP Server module
     * access settings
     * (localhost/anyhost, granted addesses)
     *
     * @return true if access is granted
     */
    protected boolean checkAccess(HttpServletRequest request) throws IOException {

        HttpServerSettings settings = HttpServerSettings.getDefault();
        if (settings == null)
            return false;

        if (settings.getHostProperty ().getHost ().equals(HttpServerSettings.ANYHOST))
            return true;

        Set hs = settings.getGrantedAddressesSet();

        if (hs.contains(request.getRemoteAddr().trim()))
            return true;

        String pathI = request.getPathInfo();
        if (pathI == null)
            pathI = "";      // NOI18N
        // ask user
        try {
            String address = request.getRemoteAddr().trim();
            if (settings.allowAccess(InetAddress.getByName(address), pathI)) return true;
        } catch (UnknownHostException | RuntimeException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }

        return false;
    }

}
