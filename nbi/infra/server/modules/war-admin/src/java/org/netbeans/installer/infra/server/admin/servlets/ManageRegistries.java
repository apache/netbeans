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

package org.netbeans.installer.infra.server.admin.servlets;

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
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.Platform;

/**
 *
 * @author ks152834
 * @version
 */
public class ManageRegistries extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        
        final PrintWriter out = response.getWriter();
        
        try {
            final List<String> registries = manager.getRegistries();
            final String userAgent = request.getHeader("User-Agent");
            
            Platform platform = SystemUtils.getCurrentPlatform();
            if (userAgent.contains("Windows")) {
                platform = Platform.WINDOWS;
            }
            if (userAgent.contains("PPC Mac OS")) {
                platform = Platform.MACOSX_PPC;
            }
            if (userAgent.contains("Intel Mac OS")) {
                platform = Platform.MACOSX_X86;
            }
            if (userAgent.contains("Linux")) {
                platform = Platform.LINUX;
            }
            if (userAgent.contains("SunOS i86pc")) {
                platform = Platform.SOLARIS_X86;
            }
            if (userAgent.contains("SunOS sun4u")) {
                platform = Platform.SOLARIS_SPARC;
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
            out.println("        <link rel=\"stylesheet\" href=\"css/main.css\" type=\"text/css\"/>");
            out.println("        <script src=\"js/main.js\" type=\"text/javascript\"></script>");
            out.println("    </head>");
            out.println("    <body onload=\"update_current_registry()\">");
            out.println("        <div class=\"top-menu\">");
            out.println("            <a href=\"javascript: add_registry();\">Add Registry</a> |");
            if (registries.size() > 0) {
                out.println("            <a href=\"javascript: remove_registry();\">Remove Registry</a> |");
                out.println("            <a href=\"javascript: delete_bundles();\">Delete Bundles</a> | ");
                out.println("            <a href=\"javascript: generate_bundles();\">Generate Bundles</a> | ");
                out.println("            <a href=\"javascript: export_registry();\">Export Registry</a> | ");
            } else {
                out.println("            Remove Registry |");
                out.println("            Delete Bundles | ");
                out.println("            Generate Bundles | ");
                out.println("            Export Registry | ");
            }
            out.println("            <a href=\"javascript: update_engine();\">Update Engine</a>");
            out.println("        </div>");
            out.println("        ");
            if (registries.size() == 0) {
                out.println("        <p>");
                out.println("            Currently there are no existing registries on this server.");
                out.println("        </p>");
            } else {
                String selected = request.getParameter("registry");
                
                out.println("        <select id=\"registries-select\" onchange=\"update_current_registry()\">");
                for (String registry: registries) {
                    out.println("            <option value=\"" + registry + "\"" + (registry.equals(selected) ? " selected" : "") + ">" + registry + "</option>");
                }
                out.println("        </select>");
                
                out.println("        <select id=\"platforms-select\" onchange=\"update_target_platform()\">");
                for (Platform temp: Platform.values()) {
                    out.println("            <option value=\"" + temp.getCodeName() + "\"" + (temp.equals(platform) ? " selected" : "") + ">" + temp.getDisplayName() + "</option>");
                }
                out.println("        </select>");
                
                for (String registry: registries) {
                    out.println("        <div class=\"registry\" id=\"registry-" + registry + "\">");
                    
                    buildRegistryTable(out, registry, manager.loadRegistry(registry).getRegistryRoot(), platform);
                    
                    out.println("        </div>");
                }
            }
            out.println("        ");
            out.println("        <form name=\"Form\" method=\"post\" enctype=\"multipart/form-data\">");
            out.println("            <input type=\"hidden\" name=\"fallback_base\" value=\"" + request.getRequestURL() + "\"/>");
            out.println("            <input type=\"hidden\" name=\"fallback\"/>");
            out.println("            <input type=\"hidden\" name=\"uid\"/>");
            out.println("            <input type=\"hidden\" name=\"version\"/>");
            out.println("            <input type=\"hidden\" name=\"platforms\"/>");
            out.println("            <div class=\"pop-up\" id=\"form-registry\">");
            out.println("                <table>");
            out.println("                    <tr>");
            out.println("                        <td colspan=\"2\">Please define a name for a new registry.</td>");
            out.println("                    </tr>");
            out.println("                    <tr>");
            out.println("                        <td style=\"width: 100%\"><input type=\"text\" name=\"registry\" style=\"width: 100%\"/></td>");
            out.println("                        <td><input type=\"submit\"/></td>");
            out.println("                    </tr>");
            out.println("                    <tr>");
            out.println("                        <td colspan=\"2\"><a href=\"javascript: close_form_registry()\">close window</a></td>");
            out.println("                    </tr>");
            out.println("                </table>");
            out.println("            </div>");
            out.println("            <div class=\"pop-up\" id=\"form-codebase\">");
            out.println("                <table>");
            out.println("                    <tr>");
            out.println("                        <td colspan=\"2\">Please define the URL prefix for the export.</td>");
            out.println("                    </tr>");
            out.println("                    <tr>");
            out.println("                        <td style=\"width: 100%\"><input type=\"text\" name=\"codebase\" style=\"width: 100%\"/></td>");
            out.println("                        <td><input type=\"submit\"/></td>");
            out.println("                    </tr>");
            out.println("                    <tr>");
            out.println("                        <td colspan=\"2\"><a href=\"javascript: close_form_codebase()\">close window</a></td>");
            out.println("                    </tr>");
            out.println("                </table>");
            out.println("            </div>");
            out.println("            <div class=\"pop-up\" id=\"form-archive\">");
            out.println("                <table>");
            out.println("                    <tr>");
            out.println("                        <td colspan=\"2\">Please point to a package.</td>");
            out.println("                    </tr>");
            out.println("                    <tr>");
            out.println("                        <td style=\"width: 100%\"><input type=\"file\" name=\"archive\" style=\"width: 100%\"/></td>");
            out.println("                        <td><input type=\"submit\"/></td>");
            out.println("                    </tr>");
            out.println("                    <tr>");
            out.println("                        <td colspan=\"2\"><a href=\"javascript: close_form_archive()\">close window</a></td>");
            out.println("                    </tr>");
            out.println("                </table>");
            out.println("            </div>");
            out.println("        </form>");
            out.println("        <p class=\"small\">" + userAgent + "</p>");
            out.println("    </body>");
            out.println("</html>");
            
        } catch (ManagerException e) {
            e.printStackTrace(out);
        }
        
        out.close();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    private void buildRegistryTable(PrintWriter out, String registry, RegistryNode root, Platform platform) {
        out.println("            <table class=\"registry\">");
        
        final ArrayList<RegistryNode> nodes = new ArrayList<RegistryNode>();
        nodes.add(root);
        
        buildRegistryNodes(out, registry, nodes, platform);
        
        out.println("            </table>");
    }
    
    private void buildRegistryNodes(PrintWriter out, String registry, List<RegistryNode> nodes, Platform platform) {
        for (RegistryNode node: nodes) {
            try {
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
                    
                    id = registry + "_" + uid + "_" + version + "_" + platforms.replace(" ", "_") + "_" + type;
                }
                
                if (node instanceof Group) {
                    type = "group";
                    
                    id = registry + "_" + uid + "_" + type;
                }
                
                out.println("                <tr id=\"" + id + "\">");
                
                out.println("                    <td class=\"tree-handle\"><img src=\"" + treeHandle + "\" onclick=\"_expand('" + id + "-children')\"/></td>");
                out.println("                    <td class=\"icon\"><img src=\"" + icon + "\"/></td>");
                out.println("                    <td class=\"display-name\" title=\"" + title + "\">" + displayName + "</td>");
                if (node.getParent() != null) {
                    out.println("                    <td class=\"option\"><a href=\"javascript: remove_component('" + uid + "', '" + version + "', '" + platforms + "')\">Remove</a></td>");
                } else {
                    out.println("                    <td class=\"option\"></td>");
                }
                out.println("                    <td class=\"option\"><a href=\"javascript: add_package('" + uid + "', '" + version + "', '" + platforms + "')\">Add Package</a></td>");
                
                out.println("                </tr>");
                
                if (node.getChildren().size() > 0) {
                    out.println("                <tr id=\"" + id + "-children\">");
                    
                    out.println("                    <td class=\"tree-handle\"></td>");
                    out.println("                    <td colspan=\"4\" class=\"children\">");
                    out.println("                    <table class=\"registry\">");
                    buildRegistryNodes(out, registry, node.getChildren(), platform);
                    out.println("                    </table>");
                    out.println("                    </td>");
                    
                    out.println("                </tr>");
                }
            } catch (Throwable e) {
                e.printStackTrace(out);
            }
        }
    }
}
