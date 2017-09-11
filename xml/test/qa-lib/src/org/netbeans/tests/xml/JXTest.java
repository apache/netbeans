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
