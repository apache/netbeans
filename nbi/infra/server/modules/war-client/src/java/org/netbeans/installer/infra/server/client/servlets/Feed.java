/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
