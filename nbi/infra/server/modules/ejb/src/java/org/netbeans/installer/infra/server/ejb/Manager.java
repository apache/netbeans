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

package org.netbeans.installer.infra.server.ejb;

import java.io.File;
import java.util.List;
import javax.ejb.Local;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.helper.Platform;

/**
 * This is the business interface for RegistryManager enterprise bean.
 */
@Local
public interface Manager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final File ROOT = new File("D:/temp/nbi-server/dev");
    public static final File TEMP = new File(ROOT, "temp");
    public static final File REGISTRIES = new File(ROOT, "registries");
    public static final File UPLOADS = new File(TEMP, "uploads");
    public static final File BUNDLES = new File(TEMP, "bundles");
    public static final File EXPORTED = new File(TEMP, "exported");
    public static final File NBI = new File(TEMP, ".nbi");
    
    public static final File REGISTRIES_LIST = new File(ROOT, "registries.list");
    public static final File ENGINE = new File(ROOT, "nbi-engine.jar");
    
    public static final String PRODUCTS = "products";
    public static final String GROUPS = "groups";
    public static final String REGISTRY_XML = "registry.xml";
    
    public static final String JNLP_STUB =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<jnlp spec=\"1.0+\" codebase=\"{0}\" href=\"{1}\">\n" +
            "    <information>\n" +
            "        <title>NetBeans Installer</title>\n" +
            "        <vendor>Sun Microsystems, Inc.</vendor>\n" +
            "        <description>NetBeans Installer Engine</description>\n" +
            "        <description kind=\"short\">NetBeans Installer Engine</description>\n" +
            "    </information>\n" +
            "    <security>\n" +
            "        <all-permissions/>\n" +
            "    </security>\n" +
            "    <resources>\n" +
            "        <j2se version=\"1.5+\"/>\n" +
            "        <j2se version=\"1.6+\"/>\n" +
            "        <j2se version=\"1.6.0-rc\"/>\n" +
            "        <j2se version=\"1.6.0-ea\"/>\n" +
            "        <j2se version=\"1.6.0-beta\"/>\n" +
            "        <j2se version=\"1.6.0-beta2\"/>\n" +
            "        <jar href=\"{2}\"/>\n" +
            "        <property name=\"nbi.product.remote.registries\" value=\"{3}\"/>\n" +
            "        <property name=\"nbi.product.suggest.install\" value=\"true\"/>\n" +
            "    </resources>\n" +
            "    <application-desc main-class=\"org.netbeans.installer.Installer\"/>\n" +
            "</jnlp>";
    
    // registry operations //////////////////////////////////////////////////////////
    void addRegistry(String registry) throws ManagerException;
    
    void removeRegistry(String registry) throws ManagerException;
    
    String getRegistry(String name) throws ManagerException;
    
    List<String> getRegistries() throws ManagerException;
    
    // engine operations ////////////////////////////////////////////////////////////
    File getEngine() throws ManagerException;
    
    void updateEngine(File engine) throws ManagerException;
    
    // components operations ////////////////////////////////////////////////////////
    void addPackage(String name, File archive, String parentUid, String parentVersion, String parentPlatforms, String uriPrefix) throws ManagerException;
    
    void removeProduct(String name, String uid, String version, String platforms) throws ManagerException;
    
    void removeGroup(String name, String uid) throws ManagerException;
    
    // miscellanea //////////////////////////////////////////////////////////////////
    File exportRegistries(String[] registryNames, String codebase) throws ManagerException;
    
    String getJnlp(String[] registryNames, String codebase) throws ManagerException;
    
    File getFile(String name, String file) throws ManagerException;
    
    Registry loadRegistry(String... names) throws ManagerException;
    
    List<Product> getProducts(String... names) throws ManagerException;
    
    File createBundle(Platform platform, String[] names, String[] components) throws ManagerException;
    
    void deleteBundles() throws ManagerException;
    
    void generateBundles(String[] names) throws ManagerException;
}
