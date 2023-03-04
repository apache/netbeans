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
package org.netbeans.modules.web.el;

import com.sun.el.parser.AstBracketSuffix;
import com.sun.el.parser.AstDotSuffix;
import com.sun.el.parser.Node;
import java.util.List;
import org.openide.util.Parameters;


/**
 *
 * @author marekfukala
 */
public class NodeUtil {

    private static final String INDENT = "    ";//NOI18N

    public static String dump(Node node) {
        StringBuilder buf = new StringBuilder();
        dump(node, "", buf);
        return buf.toString();
    }

    private static void dump(Node node, String prefix, StringBuilder buf) {
        buf.append(prefix);
        buf.append(node.toString());
        buf.append('\n');
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            dump(child, prefix + INDENT, buf);
        }
    }
    
    private static int getIndex(Node parent, Node child) {
        for(int i = 0; i < parent.jjtGetNumChildren(); i++) {
            Node n = parent.jjtGetChild(i);
            if(n.equals(child)) {
                return i;
            }
        }
        return -1;
    }
    
    public static Node getSiblingBefore(Node node) {
        Parameters.notNull("node", node);
        Node parent = node.jjtGetParent();
        if(parent == null) {
            return null;
        }
        
        int index = getIndex(parent, node);
        assert index >= 0;
        if(index == 0) {
            //first child, no sibling before
            return null; 
        }
        
        return parent.jjtGetChild(index - 1);
    }

    public static boolean isMethodCall(Node n) {
        if (n instanceof AstDotSuffix && n.jjtGetNumChildren() > 0) {
            return true;
        }
        if (n instanceof AstBracketSuffix && n.jjtGetNumChildren() > 1) {
            return true;
        }
        return false;
    }

    public static List<Node> getRootToNode(ELElement element, Node target) {
        AstPath path = new AstPath(element.getNode());
        return path.rootToNode(target);
    }

}
