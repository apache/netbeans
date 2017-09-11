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

package org.netbeans.installer.infra.lib.registries;

import java.io.File;
import java.util.Properties;
import org.netbeans.installer.utils.helper.Platform;

public interface RegistriesManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ENGINE_JAR = "engine.jar";
    
    public static final String REGISTRY_XML = "registry.xml";
    public static final String BUNDLES_LIST = "bundles.list";
    
    public static final String BUNDLES = "bundles";
    
    public static final String COMPONENTS = "components";
    public static final String PRODUCTS = COMPONENTS + "/products";
    public static final String GROUPS = COMPONENTS + "/groups";
    
    public static final String TEMP = "temp";
    
    // engine operations ////////////////////////////////////////////////////////////
    File getEngine(
            final File root) throws ManagerException;
    
    void updateEngine(
            final File root,
            final File archive) throws ManagerException;
    
    // components operations ////////////////////////////////////////////////////////
    void addPackage(
            final File root,
            final File archive,
            final String parentUid,
            final String parentVersion,
            final String parentPlatforms) throws ManagerException;
    
    void removeProduct(
            final File root,
            final String uid,
            final String version,
            final String platforms) throws ManagerException;
    
    void removeGroup(
            final File root,
            final String uid) throws ManagerException;
    
    // bundles //////////////////////////////////////////////////////////////////////
    File createBundle(
            final File root,
            final Platform platform,
            final String[] components) throws ManagerException;
    
    // bundles //////////////////////////////////////////////////////////////////////
    File createBundle(
            final File root,
            final Platform platform,
            final String[] components,
            final Properties props,
            final Properties bundleProps) throws ManagerException;
    
    void deleteBundles(
            final File root) throws ManagerException;
    
    void generateBundles(
            final File root) throws ManagerException;
    
    // miscellanea //////////////////////////////////////////////////////////////////
    void initializeRegistry(
            final File root) throws ManagerException;
    
    File exportRegistry(
            final File root,
            final File destination,
            final String codebase) throws ManagerException;
            
    String generateComponentsJs(
            final File root) throws ManagerException;
    
    String generateComponentsJs(
            final File root, final File bundlesList) throws ManagerException;

    String generateComponentsJs(
            final File root, final File bundlesList, final String locale) throws ManagerException;
}
