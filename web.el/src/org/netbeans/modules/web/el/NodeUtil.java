/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
