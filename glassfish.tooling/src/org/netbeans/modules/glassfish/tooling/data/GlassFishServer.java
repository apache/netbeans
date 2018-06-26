/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.data;

/**
 * GlassFish server entity interface.
 * <p/>
 * GlassFish Server entity interface allows to use foreign entity classes.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface GlassFishServer {

    ////////////////////////////////////////////////////////////////////////////
    // Interface Methods                                                      //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server name.
     * <p/>
     * @return The name.
     */
    public String getName();

    /**
     * Get GlassFish server host.
     * <p/>
     * @return The host.
     */
    public String getHost();

    /**
     * Get GlassFish server port.
     * <p/>
     * @return The port.
     */
    public int getPort();

   /**
     * Get GlassFish server administration port.
     * <p/>
     * @return The administration port.
     */
    public int getAdminPort();

    /**
     * Get GlassFish server administration user name.
     * <p/>
     * @return The adminUser.
     */
    public String getAdminUser();

    /**
     * Get GlassFish server administration user password.
     * <p/>
     * @return The adminPassword.
     */
    public String getAdminPassword();

    /**
     * Get GlassFish server domains folder.
     * <p/>
     * @return Domains folder.
     */
    public String getDomainsFolder();

    /**
     * Get GlassFish server domain name.
     * <p/>
     * @return Server domain name.
     */
    public String getDomainName();

    /**
     * Get GlassFish server URL.
     * <p/>
     * @return Server URL.
     */
    public String getUrl();

    /**
     * Get GlassFish server home which is <code>glassfish</code> subdirectory
     * under installation root.
     * <p/>
     * @return Server installation root.
     */
    public String getServerHome();

    /**
     * Get GlassFish server installation directory.
     * <p/>
     * @return Server server installation directory.
     */
    public String getServerRoot();

    /** Get GlassFish server version.
     * <p/>
     * @return The version
     */
    public GlassFishVersion getVersion();

    /**
     * Get GlassFish server administration interface type.
     * <p/>
     * @return GlassFish server administration interface type.
     */
    public GlassFishAdminInterface getAdminInterface();

    /**
     * Get information if this GlassFish server instance is local or remote.
     * <p/>
     * Local GlassFish server instance has domains folder attribute set while
     * remote does not.
     * <p/>
     * @return Value of <code>true</code> when this GlassFish server instance
     *         is remote or <code>false</code> otherwise.
     */
    public boolean isRemote();

}
