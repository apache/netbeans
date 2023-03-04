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


package org.netbeans.modules.i18n.regexp;

/**
 * Root node of a regular expression syntax tree.
 *
 * @author  Marian Petras
 */
class TreeNodeRoot extends TreeNode {

    /** regular expression represented by the tree */
    private String regexp;

    /**
     * Creates a new tree node representing a given regular expression.
     *
     * @param  regexp  regular expression to be represented by this node
     */
    TreeNodeRoot(String regexp) {
        super(TreeNode.REGEXP, 0, regexp.length());
        this.regexp = regexp;
    }

    /**
     * Creates a new tree node representing a given regular expression.
     *
     * @param  regexp  regular expression to be represented by this node
     * @param  attribs  attributes of this node
     */
    TreeNodeRoot(String regexp, Object attribs) {
        super(TreeNode.REGEXP, 0, regexp.length(), attribs);
        this.regexp = regexp;
    }

    /**
     * Returns a regular expression represented by the tree
     *
     * @return  regular expression represented by this node and its subnodes
     */
    @Override
    final String getRegexp() {
        return regexp;
    }

}
