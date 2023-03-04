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

import org.netbeans.api.java.source.Comment;

/**
 * The set of comments associated with a single tree node.
 * 
 * @since 0.45
 * @author Petr Hrebejk
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.org">RKo</a>)
 */
public interface CommentSet {
    /**
     * Define position of comment against corresponding tree element within java source.
     */
    public enum RelativePosition {
        /**
         * Represents preceding comment position. This comment is mean to be before corresponding tree in 
         * common literal sense.
         */
        PRECEDING,
        /**
         * This comment should be preceding or trailing to corresponding tree, 
         * but are still located on one continuous line in common literal sense.
         */
        INLINE,
        /**
         * This comment is inside corresponding tree. This allows specifying inner comments for block like empty 
         * statements. 
         */
        INNER,
        /**
         * Represents trailing comment position. This comment is mean to be following corresponding tree in 
         * common literal sense. 
         */
        TRAILING
    }
    /**
     * Adds the specified comment to the list of preceding comments.
     * @param c comment to add as preceding
     * @deprecated Use 
     * {@link #addComment(org.netbeans.modules.java.source.query.CommentSet.RelativePosition , org.netbeans.api.java.source.Comment)} 
     * instead of this. As {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition} use 
     * {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition#PRECEDING}
     */
    @Deprecated
    void addPrecedingComment(Comment c);

    /**
     * Adds the specified comment string to the list of preceding comments.
     * @param s textual representation of comment.
     * @deprecated Use 
     * {@link #addComment(org.netbeans.modules.java.source.query.CommentSet.RelativePosition , org.netbeans.api.java.source.Comment)} 
     * instead of this. As {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition} use 
     * {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition#PRECEDING}
     */
    @Deprecated
    void addPrecedingComment(java.lang.String s);

    /**
     * Adds a list of comments to the list of preceding comments.
     * @param comments list of comment to add.
     * @deprecated Use 
     * {@link #addComment(org.netbeans.modules.java.source.query.CommentSet.RelativePosition , org.netbeans.api.java.source.Comment)} 
     * instead of this. As {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition} use 
     * {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition#PRECEDING}
     */
    @Deprecated
    void addPrecedingComments(java.util.List<Comment> comments);

    /**
     * Adds the specified comment to the list of trailing comments.
     * @param c comment to add
     * @deprecated Use 
     * {@link #addComment(org.netbeans.modules.java.source.query.CommentSet.RelativePosition , org.netbeans.api.java.source.Comment)} 
     * instead of this. As {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition} use 
     * {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition#TRAILING}
     */
    @Deprecated
    void addTrailingComment(Comment c);

    /**
     * Adds the specified comment string to the list of trailing comments.
     * @param s textual content of comment to add.
     * @deprecated Use 
     * {@link #addComment(org.netbeans.modules.java.source.query.CommentSet.RelativePosition , org.netbeans.api.java.source.Comment)} 
     * instead of this. As {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition} use 
     * {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition#TRAILING}
     */
    @Deprecated
    void addTrailingComment(java.lang.String s);

    /**
     * Adds a list of comments to the list of preceding comments.
     * @param comments list of comments to add.
     * @deprecated Use 
     * {@link #addComment(org.netbeans.modules.java.source.query.CommentSet.RelativePosition , org.netbeans.api.java.source.Comment)} 
     * instead of this. As {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition} use 
     * {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition#TRAILING}
     */
    @Deprecated
    void addTrailingComments(java.util.List<Comment> comments);

    /**
     * Gets list of preceding comments. The returned list is read-only.
     * @return list of preceding comments. This method always return list event if list is empty.
     * @deprecated Use {@link #getComments(org.netbeans.modules.java.source.query.CommentSet.RelativePosition)} 
     * instead of this method. As {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition} use 
     * {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition#PRECEDING} 
     */
    @Deprecated
    java.util.List<Comment> getPrecedingComments();

    /**
     * Gets list of trailing comments. The returned list is read-only.
     * @return list of training comments. This method always return list event if list is empty.
     * @deprecated Use {@link #getComments(org.netbeans.modules.java.source.query.CommentSet.RelativePosition)} 
     * instead of this method. As {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition} use 
     * {@link org.netbeans.modules.java.source.query.CommentSet.RelativePosition#TRAILING} 
     */
    @Deprecated
    java.util.List<Comment> getTrailingComments();

    /**
     * Returns true if there has been added any newly created comment.
     * @return true if this list contains newly create comments by user.
     * @see org.netbeans.api.java.source.Comment#isNew() 
     */
    boolean hasChanges();

    /**
     * Returns true if this comment set not empty.
     * @return true if there is at least one comment in this set.
     */
    boolean hasComments();

    /**
     * Returns the first character position, which is either the initial
     * position of the first preceding comment, or {@link org.netbeans.modules.java.source.save.PositionEstimator#NOPOS} 
     * if there are no comments.
     *
     * @return start position of first comment or {@link org.netbeans.modules.java.source.save.PositionEstimator#NOPOS}
     *  if there is no comment.
     */
    int pos();

    /**
     * Returns document offset of first comment on relative position or 
     * {@link org.netbeans.modules.java.source.save.PositionEstimator#NOPOS} if there are no comments for this position. 
     * @param position the relative position of comment against associated tree.
     * @return document offset of first comment or {@link org.netbeans.modules.java.source.save.PositionEstimator#NOPOS}
     */
    int pos(RelativePosition position);

    /**
     * Adds comment with specified positioning.
     * @param positioning relative position of comment against corresponding tree. 
     * @param c comment to add.
     * 
     * @see org.netbeans.modules.java.source.query.CommentSet.RelativePosition
     */
    void addComment(RelativePosition positioning, Comment c);

    /**
     * Inserts comment with specified positioning to the given index.
     * @param positioning relative position of comment against corresponding tree.
     * @param c comment to add.
     * @param index -1 to add comment to the end or index at which the comment should be added
     * 
     * @see org.netbeans.modules.java.source.query.CommentSet.RelativePosition
     */
    void insertComment(RelativePosition positioning, Comment c, int index);

    /**
     * Gets non-null list of comments on specified relative position. This list is read-only. If you need to check if 
     * there is any comment in this set use {@link #hasComments()}.  
     * @param positioning relative position against associated tree. 
     * @return non-null read-only list of comments on specified position.
     * @see org.netbeans.modules.java.source.query.CommentSet.RelativePosition
     */
    java.util.List<Comment> getComments(RelativePosition positioning);
}
