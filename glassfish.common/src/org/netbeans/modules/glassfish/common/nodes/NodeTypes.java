/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.glassfish.common.nodes;

import java.util.HashMap;
import org.netbeans.modules.glassfish.spi.GlassfishModule;

public class NodeTypes {
    
    private static HashMap<String, String[]> nodeTree;

    public static final String APPLICATIONS = "APPLICATIONS";
    public static final String EARS = "EARS";
    public static final String WEBAPPS = "WEBAPPS";
    public static final String EJBS = "EJBS";
    public static final String APPCLIENTS = "APPCLIENTS";
    
    public static final String RESOURCES = "RESOURCES";
            
    private static final String[] APPLICATIONS_TREE = {
        EARS, WEBAPPS, EJBS, APPCLIENTS };
    private static final String[] RESOURCES_TREE = {
        GlassfishModule.JDBC, GlassfishModule.CONNECTORS, GlassfishModule.JAVAMAIL };
    private static final String[] JDBC_TREE = {
        GlassfishModule.JDBC_RESOURCE, GlassfishModule.JDBC_CONNECTION_POOL };
    private static final String[] CONNECTORS_TREE = {
        GlassfishModule.CONN_RESOURCE, GlassfishModule.CONN_CONNECTION_POOL, GlassfishModule.ADMINOBJECT_RESOURCE };
    private static final String[] JAVAMAIL_TREE = {
        GlassfishModule.JAVAMAIL_RESOURCE };
    
    static {
        nodeTree = new HashMap<String, String[]>();
        nodeTree.put(APPLICATIONS, APPLICATIONS_TREE);
        nodeTree.put(RESOURCES, RESOURCES_TREE);
        nodeTree.put(GlassfishModule.JDBC, JDBC_TREE);
        nodeTree.put(GlassfishModule.CONNECTORS, CONNECTORS_TREE);
        nodeTree.put(GlassfishModule.JAVAMAIL, JAVAMAIL_TREE);
    }

    private NodeTypes() { }
    
    /**
     * Returns an array of tree children as strings given a particular
     * parent name.
     *
     * @param type The node from which children types are derived.
     *
     * @return All the children types for the node name passed.
     */
    static String[] getChildTypes(String type){
        return nodeTree.get(type);
    }
        
}
