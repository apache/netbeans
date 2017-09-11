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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.Platform;

import static org.netbeans.installer.utils.helper.Platform.WINDOWS;
import static org.netbeans.installer.utils.helper.Platform.LINUX;
import static org.netbeans.installer.utils.helper.Platform.SOLARIS_X86;
import static org.netbeans.installer.utils.helper.Platform.SOLARIS_SPARC;
import static org.netbeans.installer.utils.helper.Platform.MACOSX;
import static org.netbeans.installer.utils.helper.Platform.MACOSX_X86;
import static org.netbeans.installer.utils.helper.Platform.MACOSX_PPC;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class CreateBundle extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameterValues("component") != null) {
            doPost(request, response);
            return;
        }
        
        response.setContentType("text/html; charset=UTF-8");
        
        String[] registries = request.getParameterValues("registry");
        
        PrintWriter out = response.getWriter();
        
        try {
            String userAgent = request.getHeader("User-Agent");
            
            Platform platform = SystemUtils.getCurrentPlatform();
            if (userAgent.contains("Windows")) {
                platform = WINDOWS;
            }
            if (userAgent.contains("PPC Mac OS")) {
                platform = MACOSX_PPC;
            }
            if (userAgent.contains("Intel Mac OS")) {
                platform = MACOSX_X86;
            }
            if (userAgent.contains("Linux")) {
                platform = LINUX;
            }
            if (userAgent.contains("SunOS i86pc")) {
                platform = SOLARIS_X86;
            }
            if (userAgent.contains("SunOS sun4u")) {
                platform = SOLARIS_SPARC;
            }
            
            
            if (request.getParameter("platform") != null) {
                try {
                    platform = StringUtils.parsePlatform(
                            request.getParameter("platform"));
                } catch (ParseException e) {
                    e.printStackTrace(out);
                }
            }
            
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
            out.println("<html>");
            out.println("    <head>");
            out.println("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
            out.println("        <title>Registries Manager</title>");
            out.println("        <link rel=\"stylesheet\" href=\"admin/css/main.css\" type=\"text/css\"/>");
            out.println("        <script src=\"js/main.js\" type=\"text/javascript\"></script>");
            out.println("    </head>");
            out.println("    <body>");
            out.println("        <p>");
            out.println("            Select the components that you would like to include in the bundle and click Next.");
            out.println("        </p>");
            out.println("        <form name=\"Form\" action=\"create-bundle\" method=\"post\">");
            
            String registriesUrl = "";
            
            for (String registry: registries) {
                registriesUrl += "&registry=" + URLEncoder.encode(registry, "UTF-8");
                out.println("            <input type=\"hidden\" name=\"registry\" value=\"" + registry + "\"/>");
            }
            out.println("            <input type=\"hidden\" name=\"registries\" value=\"" + registriesUrl + "\"/>");
            out.println("            <input type=\"hidden\" name=\"platform\" value=\"" + platform + "\"/>");
            
            out.println("        <select id=\"platforms-select\" onchange=\"update_target_platform()\">");
            for (Platform temp: Platform.values()) {
                out.println("            <option value=\"" + temp.getCodeName() + "\"" + (temp.isCompatibleWith(platform) ? " selected" : "") + ">" + temp.getDisplayName() + "</option>");
            }
            out.println("        </select>");
            
            out.println("        <div class=\"registry\">");
            buildRegistryTable(out, manager.loadRegistry(registries).getRegistryRoot(), platform);
            out.println("        </div>");
            
            out.println("            <input type=\"submit\" value=\"Create Bundle\"/>");
            
            out.println("        </form>");
            out.println("        <br/>");
            out.println("        <p class=\"small\">" + userAgent + "</p>");
            out.println("    </body>");
            out.println("</html>");
        } catch (ManagerException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            e.printStackTrace(out);
        }
        
        out.close();
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            final String[] registries = 
                    request.getParameterValues("registry");
            final String[] components = 
                    request.getParameterValues("component");
            final Platform platform = 
                    StringUtils.parsePlatform(request.getParameter("platform"));
            
            final File file = 
                    manager.createBundle(platform, registries, components);
            
            String filename = "";
            for (String name: components) {
                filename += name.substring(0, name.indexOf(",")) + "_";
            }
            filename += platform.toString();
            if (platform == WINDOWS) {
                filename += ".exe";
            } else if (platform.isCompatibleWith(MACOSX)) {
                filename += ".zip";
            } else {
                filename += ".sh";
            }
            
            final OutputStream output = response.getOutputStream();
            
            response.setContentType(
                    "application/octet-stream");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + filename);
            response.setHeader(
                    "Last-Modified",
                    StringUtils.httpFormat(new Date(file.lastModified())));
            response.setHeader(
                    "Accept-Ranges",
                    "bytes");
            
            Utils.transfer(request, response, output, file);
        } catch (ParseException e) {
            e.printStackTrace(response.getWriter());
        } catch (ManagerException e) {
            e.printStackTrace(response.getWriter());
        }
    }
    
    private void buildRegistryTable(PrintWriter out, RegistryNode root, Platform platform) {
        out.println("            <table class=\"registry\">");
        
        buildRegistryNodes(out, root.getChildren(), platform);
        
        out.println("            </table>");
    }
    
    private void buildRegistryNodes(PrintWriter out, List<RegistryNode> nodes, Platform platform) {
        for (RegistryNode node: nodes) {
            if (node instanceof Product) {
                if (!((Product) node).getPlatforms().contains(platform)) {
                    continue;
                }
            }
            
            String icon        = null;
            String displayName = node.getDisplayName();
            String treeHandle  = null;
            
            if (node.getIconUri() == null) {
                icon = "img/default-icon.png";
            } else {
                icon = node.getIconUri().getRemote().toString();
            }
            
            if (node.getChildren().size() > 0) {
                treeHandle  = "img/tree-handle-open.png";
            } else {
                treeHandle  = "img/tree-handle-empty.png";
            }
            
            String id          = null;
            
            String uid         = node.getUid();
            String version     = null;
            String type        = null;
            String platforms   = null;
            String title       = "";
            
            if (node instanceof Product) {
                version   = ((Product) node).getVersion().toString();
                platforms = StringUtils.asString(((Product) node).getPlatforms(), " ");
                title     = StringUtils.asString(((Product) node).getPlatforms());
                type      = "component";
                
                id = uid + "_" + version + "_" + platforms.replace(" ", "_") + "_" + type;
            }
            
            if (node instanceof Group) {
                type = "group";
                
                id = uid + "_" + type;
            }
            
            out.println("                <tr id=\"" + id + "\">");
            
            out.println("                    <td class=\"tree-handle\"><img src=\"" + treeHandle + "\" onclick=\"_expand('" + id + "-children')\"/></td>");
            out.println("                    <td class=\"icon\"><img src=\"" + icon + "\"/></td>");
            if (version != null) {
                out.println("                    <td class=\"checkbox\"><input type=\"checkbox\" name=\"component\" value=\"" + uid + "," + version + "\"/></td>");
            } else {
                out.println("                    <td class=\"checkbox\"></td>");
            }
            out.println("                    <td class=\"display-name\" title=\"" + title + "\">" + displayName + "</td>");
            
            out.println("                </tr>");
            
            if (node.getChildren().size() > 0) {
                out.println("                <tr id=\"" + id + "-children\">");
                
                out.println("                    <td class=\"tree-handle\"></td>");
                out.println("                    <td colspan=\"3\" class=\"children\">");
                out.println("                    <table class=\"registry\">");
                buildRegistryNodes(out, node.getChildren(), platform);
                out.println("                    </table>");
                out.println("                    </td>");
                
                out.println("                </tr>");
            }
        }
    }
}
