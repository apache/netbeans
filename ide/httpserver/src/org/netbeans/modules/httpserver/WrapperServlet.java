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

package org.netbeans.modules.httpserver;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

import javax.servlet.ServletOutputStream;

/**
 *
 * @author Radim Kubacki
 */
public class WrapperServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(WrapperServlet.class.getName());
    private static final long serialVersionUID = 8009602136746998361L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    /**
     * Processes the request for both HTTP GET and POST methods
     *
     * @param request servlet request
     * @param response servlet response
     */
    @SuppressWarnings("NestedAssignment")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        ServletOutputStream out = response.getOutputStream ();
        try {
            FileObject file = URLMapper.findFileObject(new URL(request.getRequestURL().toString()));
            if (file == null) {
                LOG.log(Level.FINE, "File not found: " + request.getRequestURL().toString());
                response.sendError (HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            URL internal = URLMapper.findURL(file, URLMapper.INTERNAL);
            URLConnection conn = internal.openConnection();

            String type = conn.getContentType();
            if (type == null || "content/unknown".equals(type)) { // NOI18N
                type = file.getMIMEType();
            }
            if ((type == null || "content/unknown".equals(type)) && file.getExt().equals("css")) { // NOI18N
                type = "text/css";
            }
            response.setContentType(type);
            // PENDING: copy all info - headers, length, encoding, ...

            try (InputStream in = conn.getInputStream ()) {
                byte [] buff = new byte [256];
                int len;

                while ((len = in.read (buff)) != -1) {
                    out.write (buff, 0, len);
                    out.flush();
                }
            }

        }
        catch (MalformedURLException | IllegalArgumentException ex) {
            LOG.log(Level.FINE, "Failed to parse target URL from request: " + request.getRequestURL().toString(), ex);
            try {
                response.sendError (HttpServletResponse.SC_NOT_FOUND,
                                   NbBundle.getMessage(WrapperServlet.class, "MSG_HTTP_NOT_FOUND"));
            }
            catch (IOException ex2) {}
        }
        catch (IOException ex) {
            LOG.log(Level.FINE, "Failed read data for request: " + request.getRequestURL().toString(), ex);
            try {
                response.sendError (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            catch (IOException ex2) {}
        }
    }

}
