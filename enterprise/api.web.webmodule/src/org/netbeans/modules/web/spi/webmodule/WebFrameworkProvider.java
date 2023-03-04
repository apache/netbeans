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

package org.netbeans.modules.web.spi.webmodule;

import java.io.File;
import java.util.Set;

import org.netbeans.modules.web.api.webmodule.*;
import org.netbeans.modules.web.webmodule.WebModuleExtenderBridge;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Encapsulates a web framework.
 * 
 * <p>This class allows providing support for web frameworks. It can be used
 * to extend a web module with a web framework, to find out whether a web
 * module is already extender by a web framework, or to retrieve a web framework's
 * specific configuration files.</p>
 * 
 * <p>Instances of this class are registered in the <code>j2ee/webtier/framework</code>
 * in the module layer.</p>
 *
 * @author Petr Pisl, Andrei Badea, Petr Slechta
 */
public abstract class WebFrameworkProvider {

    private final String name;
    private final String description;

    /**
     * Creates a new web framework with a name and description.
     *
     * @param  name the short name of this web framework (e.g., "Struts"); never null.
     * @param  description the description of this web framework (e.g., "An open source framework based on the MVC pattern"); can be null.
     * @throws NullPointerException if the <code>name</code> parameter is null.
     */
    public WebFrameworkProvider(String name, String description){
        Parameters.notNull("name", name); // NOI18N
        this.name = name;
        this.description = description;
    }
    
    /**
     * Returns the name of this web framework.
     *
     * @return the name; never null.
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Returns the description of this web framework. Defaults to the name
     * if a null <code>description</code> parameter was passed to the constructor.
     * 
     * @return the description; never null.
     */
    public String getDescription(){
        if (description != null) {
            return this.description;
        }
        return getName();
    }
    
    /**
     * Extends a web module with this web frameworks. For example it might be
     * called in order to add the web framework to a newly created web application
     * or in order to add the web framework to an existing application.
     *
     * @param  wm the {@link org.netbeans.modules.web.api.webmodule.WebModule} to be extended; never null.
     * @return the list of new files created in the web module as the result
     *         of extending it with this framework; never null.
     *
     * @deprecated This method has been replaced with {@link #createWebModuleExtender createWebModuleExtender}.
     */
    @Deprecated
    public Set extend(WebModule wm) {
        throw new IllegalStateException("This framework does not implemement the deprecated extend() method. Use createWebModuleExtender() instead."); // NOI18N
    }
    
    /**
     * Finds out if a given web module has already been extended with this framework.
     *
     * @param  wm the web module; never null.
     * @return true if the web module has already been extended with this framework, false otherwise.
     */
    public abstract boolean isInWebModule (WebModule wm);
    
    /**
     * Returns the configuration files belonging to this framework.
     *
     * @param  wm the web module for which the configuration files are returned; never null.
     * @return an array containing the configuration files; never null.
     */
    public abstract File[] getConfigurationFiles(WebModule wm);
    
    /**
     * Returns a configuration panel for this web framework. The panel is used
     * to allow the user configure the way the web module will be extended.
     * The configuration panel might be displayed to the user when creating
     * a new web application or when editing the properties of an existing application.
     *
     * @param  wm the web module to be configured.
     * @return a configuration panel for this web framework.
     *
     * @deprecated This method has been replaced with {@link #createWebModuleExtender createWebModuleExtender}.
     */
    @Deprecated
    public FrameworkConfigurationPanel getConfigurationPanel(WebModule wm) {
        throw new IllegalStateException("This framework does not implemement the deprecated getConfigurationPanel() method. Use createWebModuleExtender() instead."); // NOI18N
    }
    
    /**
     * Creates a {@link WebModuleExtender web module extender} for this framework
     * and the given web module. This method needs to be implemented instead of the
     * deprecated {@link #extend} and {@link #getConfigurationPanel} methods. It
     * needs to be implemented even if this web framework doesn't support extending
     * a web module (it would just return null in this case).
     *
     * @param  wm the web module to be extended; can be null, e.g., if the
     *         method is called while creating a new web application, in which
     *         case the module doesn't exist yet.
     * @param  controller an instance of {@link ExtenderController} allowing the
     *         newly created extender to communicate with its environment. See the
     *         <code>ExtenderController</code> for details. Never null.
     * @return a new web module extender; can be null if the framework doesn't support
     *         extending (either web modules in general of the particular web module
     *         passed in the <code>wm</code> parameter.
     */
    public WebModuleExtender createWebModuleExtender(WebModule wm, ExtenderController controller) {
        return WebModuleExtenderBridge.create(this, wm, controller);
    }
    
    /**
     * Returns the path of the request URL to a given file .
     * This path starts with a "/" character and includes either the servlet name
     * or a path to the servlet/JSP. Includes the servlet mapping, but does not include
     * any extra path information or a query string. The method can return null.
     *
     * <p>JSF Example: consider an index.jsp file in the document base. Normaly the URL
     * for accessing this page in browser should be <code>http://server:port/contextpath/index.jsp</code>.
     * The servlet path is <code>/index.jsp</code>.</p>
     *
     * <p>However, because the <code>index.jsp</code> file includes JSF tags,
     * its URL should include the appropriate JSF servlet
     * mapping. If the mapping is /faces/*, then the URL is
     * <code>http://server:port/contextpath/faces/index.jsp</code> and this method
     * should return <code>/faces/index.jsp</code>.</p>
     *
     * @param  file an arbitrary <code>FileObject</code>, usually a JSP file; never null.
     * @return a string that contains the servlet path including the mapping; can be null.
     */
    public String getServletPath(FileObject file){
        return null;
    }

    /**
     * Test if this framework requires web.xml deployment descriptor for its
     * functioning.
     * @return true if web.xml is required by this framework
     * @since 1.15
     */
    public boolean requiresWebXml() {
        return true;
    }

}
