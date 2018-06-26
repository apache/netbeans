/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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
