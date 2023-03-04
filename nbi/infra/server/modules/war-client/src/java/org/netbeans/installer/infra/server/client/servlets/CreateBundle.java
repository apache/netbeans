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
