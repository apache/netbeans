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

package org.netbeans.modules.java.source.query;

import com.sun.source.tree.Tree;
import org.netbeans.api.java.source.Comment;

/**
 * The service that maps Trees with their associated comments.
 *
 * @see org.netbeans.modules.java.source.model.CommentSet
 */
public interface CommentHandler {
    
    /**
     * Returns true if the specified tree has an associated CommentSet.
     */
    boolean hasComments(Tree tree);
    
    /**
     * Returns the CommentSet associated with a tree, or null if the tree
     * does not have any comments.
     */
    CommentSet getComments(Tree tree);
    
    /**
     * Copies preceding and trailing comments from one tree to another,
     * appending the new entries to the existing comment lists.
     */
    void copyComments(Tree fromTree, Tree toTree);
    
    /**
     * Add a preceding comment to a tree's comment set.  If a comment set
     * for the tree doesn't exist, one will be created.
     */
    void addComment(Tree tree, Comment c);
}
