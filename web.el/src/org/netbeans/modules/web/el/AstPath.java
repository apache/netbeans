/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.el;

import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.el.ELException;
import org.openide.util.Parameters;

/**
 * AST path for Expression Language AST nodes.
 *
 * @author Erno Mononen
 */
public final class AstPath {

    private final List<Node> nodes = new ArrayList<>();
    private final Node root;

    public AstPath(Node root) {
        Parameters.notNull("root", root);
        this.root = root;
        init();
    }

    private void init() {
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                nodes.add(node);
            }
        });
    }

    public Node getRoot() {
        return root;
    }

    public List<Node> rootToLeaf() {
        return nodes;
    }

    public List<Node> leafToRoot() {
        List<Node> copy = new ArrayList<>(nodes);
        Collections.reverse(copy);
        return copy;
    }

    public List<Node> rootToNode(Node target) {
        return rootToNode(target, false);
    }

    public List<Node> rootToNode(Node target, boolean inclusive) {
        List<Node> result = new ArrayList<>();
        for (Node each : nodes) {
            if (equalsNodes(each, target)) {
                if (inclusive) {
                    result.add(each);
                }
                break;
            }
            result.add(each);
        }
        return result;
    }

    private static boolean equalsNodes(Node src, Node target) {
        if (src.equals(target)) {
            // #228091 SimpleNode equal method doesn't check images in some cases
            return src.getImage() == null || src.getImage().equals(target.getImage());
        }
        return false;
    }

}
