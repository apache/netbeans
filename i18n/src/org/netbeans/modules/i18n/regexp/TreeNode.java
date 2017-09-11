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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.i18n.regexp;

import java.util.ArrayList;
import java.util.List;

/**
 * Node of a regular expression syntax tree.
 * This node and its subnodes represent a subexpression of a regular expression.
 *
 * @author  Marian Petras
 */
class TreeNode {

    static final int REGEXP             =  0;
    static final int MULTI_REGEXP       =  1;
    static final int SIMPLE_REGEXP      =  2;
    static final int Q_REGEXP           =  3;
    static final int QUANTIFIER         =  4;
    static final int NUMBER             =  5;
    static final int METACHAR           =  6;
    static final int UNICODE_CHAR       =  7;
    static final int CHAR               =  8;
    static final int SUBEXPR            =  9;
    static final int POSIX_SET          = 10;
    static final int SET                = 11;
    static final int RANGE              = 12;
    static final int TOKEN              = 13;

    /**
     * index of the first character of a subexpression represented by this node
     */
    int start;
    /**
     * index of the last character of a subexpression represented by this node
     */
    int end;
    /** type of a subexpression this node represents */
    private int tokenType;
    /** attributes of this node */
    private Object attribs;
    /** this node's parent node */
    private TreeNode parent;
    /** direct subnodes of this node */
    private List<TreeNode> children;

    /**
     * Creates a new node representing a given part of a regular expression.
     *
     * @param  tokenType  type of a subexpression this node represents
     * @param  start  index of the first character of a subexpression
     *                represented by this node
     * @param  end  index of the last character of a subexpression
     *              represented by this node
     */
    TreeNode(int tokenType, int start, int end) {
        this.tokenType = tokenType;
        this.start = start;
        this.end = end;
    }

    /**
     * Creates a new node representing a given part of a regular expression.
     *
     * @param  tokenType  type of a subexpression this node represents
     * @param  start  index of the first character of a subexpression
     *                represented by this node
     * @param  end  index of the last character of a subexpression
     *              represented by this node
     * @param  attribs  attributes of this node
     */
    TreeNode(int tokenType, int start, int end, Object attribs) {
        this(tokenType, start, end);
        this.attribs = attribs;
    }

    /**
     * Adds a subnode to this node.
     *
     * @param  child  subnode to be added
     */
    void add(TreeNode child) {
        if (child == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        child.parent = this;

        if (children == null) {
            children = new ArrayList<TreeNode>(4);
        }
        children.add(child);
    }

    /**
     * Returns a regular expression represented by the root node of the whole
     * tree.
     *
     * @return  regular expression represented by the whole tree this node
     *          is part of
     */
    String getRegexp() {

        TreeNode candidate = this;
        TreeNode candidParent;

        /* Find the root: */
        while ((candidParent = candidate.parent) != null) {
            candidate = candidParent;
        }
        assert candidate instanceof TreeNodeRoot;

        return candidate.getRegexp();
    }

    /**
     * Returns the type of regular expression represented by this node.
     *
     * @return  type of regular expression represented by this node's subtree
     */
    int getTokenType() {
        return tokenType;
    }

    /**
     * Returns this node's attributes.
     *
     * @return  attributes of this node
     * @see  #TreeNode(int, int, int, Object)
     */
    Object getAttribs() {
        return attribs;
    }

    /**
     * Returns this node's children.
     *
     * @return  list of this node's direct subnodes;
     *          or <code>null</code> if this node has no subnodes
     */
    List<TreeNode> getChildren() {
        return children != null ? new ArrayList<TreeNode>(children)
                                : null;
    }

}
