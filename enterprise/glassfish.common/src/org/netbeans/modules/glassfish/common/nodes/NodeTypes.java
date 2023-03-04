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
