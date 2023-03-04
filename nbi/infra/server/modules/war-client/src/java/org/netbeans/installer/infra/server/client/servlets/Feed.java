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

package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class Feed extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final PrintWriter out        = response.getWriter();
        final String[]    registries = request.getParameterValues("registry");
        
        try {
            List<Product> components;
            String                 feedType;
            
            // if the user did not specify any registry to look for the components,
            // we cannot guess for him - will return an empty feed
            if ((registries == null) || (registries.length == 0)) {
                components = new ArrayList<Product>();
            } else {
                components = manager.getProducts(registries);
            }
            
            feedType = request.getParameter("feed-type");
            if (feedType == null) {
                feedType = "rss-2.0";
            }
            
            response.setContentType("text/xml");
            
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            
            if (feedType.equals("rss-2.0")) {
                buildRss(components, out);
            }
        } catch (ManagerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(out);
        }
        
        out.close();
    }
    
    private void buildRss(List<Product> components, PrintWriter out) throws IOException {
        out.println("<rss version=\"2.0\">");
        out.println("    <channel>");
        
        out.println("        <title><![CDATA[NetBeans Installer Components Feed]]></title>");
        out.println("        <link>http://localhost/</link>");
        out.println("        <description><![CDATA[NetBeans Installer Components Feed]]></description>");
        
        for (Product component: components) {
            out.println("            <item>");
            out.println("                <guid>" + component.getUid() + "_" + component.getVersion() + "</guid>");
            out.println("                <title><![CDATA[" + component.getDisplayName() + "]]></title>");
            out.println("                <link>http://localhost/</link>");
            out.println("                <description><![CDATA[" + component.getDescription() + "]]></description>");
            out.println("                <pubDate>" + StringUtils.httpFormat(component.getBuildDate()) + "</pubDate>");
            out.println("            </item>");
        }
        
        out.println("    </channel>");
        out.println("</rss>");
    }
}
