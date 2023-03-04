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

package org.netbeans.tests.xml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.jemmy.util.PNGEncoder;


/**
 * Provides the basic support for XML Jemmy tests.
 * @author  ms113234
 */
public class JXTest extends XTest {
    public static final String DELIM = "|";
    protected static boolean captureScreen = true;
    protected static boolean dumpScreen = true;
    
    
    /** Creates a new instance of JXMLXtest */
    public JXTest(String name) {
        super(name);
        boolean dbgTimeouts = Boolean.getBoolean(System.getProperty("xmltest.dbgTimeouts", "true"));
        try {
            if (dbgTimeouts) {
                JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
            }
        } catch (IOException ioe) {
            log("Load Debug Timeouts fail.", ioe);
        }
    }

    protected void fail(String msg, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        if (captureScreen) {
            try {
                PNGEncoder.captureScreen(getWorkDirPath()+File.separator+"screen.png");
            } catch (Exception e1) {}
        }
        if (dumpScreen) {
            try {
                Dumper.dumpAll(getWorkDirPath()+File.separator+"screen.xml");
            } catch (Exception e2) {}
        }
        fail(msg + "\n" + sw);
    }
    
    /**
     * Finds Node in the 'data' forlder.
     * @param path relative to the 'data' folder delimited by 'DELIM'
     */
    protected Node findDataNode(String path) {
        Node node = null;
        try {
            String treePath = getFilesystemName() + DELIM + getDataPackageName(DELIM) + DELIM + path;
            JTreeOperator tree = ExplorerOperator.invoke().repositoryTab().tree();
            tree.setComparator(new Operator.DefaultStringComparator(true, true));
            node = new Node(tree, treePath);
        } catch (Exception ex) {
            log("Cannot find data node: " + path, ex);
        }
        return node;
    }
    
    /**
     * Finds Catalog's node.
     * @param path relative to the 'XML Entity Catalogs' root delimited by 'DELIM'
     */
//    protected Node findCatalogNode(String path) {
//        Node node = null;
//        try {
//            String treePath = Bundle.getStringTrimmed("org.netbeans.modules.xml.catalog.Bundle", "TEXT_catalog_root");
//            if (path != null && path.length() > 0) treePath += DELIM + path;
//            JTreeOperator tree = ExplorerOperator.invoke().runtimeTab().tree();
//            node = new Node(tree, treePath);
//        } catch (Exception ex) {
//            log("Cannot find catalog node: " + path, ex);
//        }
//        return node;
//    }
        
//    /**
//     * Returns work directory subnode or null
//     */
//    protected FolderNode getWorkDirNode(String name) throws IOException {
//        final String FILESYSTEMS = JelloBundle.getString("org.netbeans.core.Bundle", "dataSystemName");
//        String path = FILESYSTEMS + ", " + getWorkDirPath() + ", " + name;
//        
//        Explorer explorer = new Explorer();
//        explorer.switchToFilesystemsTab();
//        //TreePath treePath = explorer.getJTreeOperator().findPath(path, ", ");
//        //explorer.getJTreeOperator().expandPath(treePath);
//        
//        return FolderNode.findFolder(FILESYSTEMS + ", " + getWorkDirPath() + ", " + name);
//    }
}
