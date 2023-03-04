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
package org.netbeans.test.web;

import java.io.File;
import java.util.Arrays;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 *
 * @author lm97939
 */
public class Util {

    /**
     * Creates a new instance of Util
     */
    public Util() {
    }

    public static String dumpProjectView(String project) {
        // TODO replace sleep()
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        StringBuffer buff = new StringBuffer();
        Node node = new ProjectsTabOperator().getProjectRootNode(project);
        dumpNode(node, buff, 0);
        return buff.toString();
    }

    private static void dumpNode(Node node, StringBuffer buff, int level) {
        for (int i = 0; i < level; i++) {
            buff.append(".");
        }
        buff.append("+ ");
        buff.append(node.getText().trim());
        if (!node.isLeaf() && node.getText().indexOf('.') < 0) {
            buff.append(" ");
            boolean wasCollapsed = node.isCollapsed();
            buff.append("\n");
            String nodes[] = node.getChildren();
            for (int i = 0; i < nodes.length; i++) {
//                System.out.println("Parent:: " + node.getText() + " - subPath:: " + nodes[i]);
                Node child = new Node(node, nodes[i]);
                // prevents infinite loop in case the nodes[i].equals("");
                if (child.getPath().equals(node.getPath())) {
//                    System.out.println("===Continue===");
                    continue;
                }
                if (!(child.getText().equals(nodes[i]))) {
                    child = new Node(node, i);

                }
                dumpNode(child, buff, level + 1);
            }
            if (wasCollapsed) {
                node.collapse();
            }
        } else {
            buff.append("\n");
        }
    }

    public static String dumpFiles(File file) {
//        try { Thread.currentThread().sleep(3000); }
//        catch (InterruptedException e) {}
        StringBuffer buff = new StringBuffer();
        dumpFiles(file, buff, 0);
        return buff.toString();
    }

    private static void dumpFiles(File file, StringBuffer buff, int level) {
        for (int i = 0; i < level; i++) {
            buff.append(".");
        }
        buff.append(file.getName());
        buff.append("\n");
        if (file.isDirectory()) {
            String files[] = file.list();
            Arrays.sort(files);
            for (int i = 0; i < files.length; i++) {
                dumpFiles(new File(file, files[i]), buff, level + 1);
            }
        }
    }

    public static void deleteAllBreakpoints() {
        BreakpointsWindowOperator bwo = BreakpointsWindowOperator.invoke();
        bwo.deleteAll();
        bwo.close();
    }

    public static void cleanStatusBar() {
        MainWindowOperator.getDefault().setStatusText("STATUS CLEANED");
    }
}
