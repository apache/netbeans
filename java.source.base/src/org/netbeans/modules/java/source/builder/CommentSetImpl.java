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
package org.netbeans.modules.java.source.builder;

import org.netbeans.api.java.source.Comment;
import org.netbeans.modules.java.source.query.CommentSet;
import static org.netbeans.modules.java.source.save.PositionEstimator.NOPOS;

import java.util.*;

/**
 * Class that associates the before and after comments to a tree.
 */
public final class CommentSetImpl implements Cloneable, CommentSet {
//    private final List<Comment> precedingComments = new ArrayList<Comment>();
//    private final List<Comment> trailingComments = new ArrayList<Comment>();
    private boolean commentsMapped;
    private final Map<RelativePosition, List<Comment>> commentsMap = new HashMap<RelativePosition, List<Comment>>();
    private Map<RelativePosition, List<Comment>> origMap = null;
    
    /**
     * True, if comments have been changed after the initial mapping from source.
     * Changed comments return true from {@link #hasComments} even though the comment set is empty
     */
    private boolean changed;
    /**
     * Add the specified comment string to the list of preceding comments. 
     */
    public void addPrecedingComment(String s) {
        addPrecedingComment(Comment.create(s));
    }

    /**
     * Add the specified comment to the list of preceding comments. 
     */
    public void addPrecedingComment(Comment c) {
        addComment(RelativePosition.PRECEDING, c);
    }

    /**
     * Add a list of comments to the list of preceding comments.
     */
    public void addPrecedingComments(List<Comment> comments) {
        for (Comment comment : comments) {
            addComment(RelativePosition.PRECEDING, comment);
        }
    }
    
    /**
     * Add the specified comment string to the list of trailing comments. 
     */
    public void addTrailingComment(String s) {
        addTrailingComment(Comment.create(s));
    }

    /**
     * Add the specified comment to the list of trailing comments. 
     */
    public void addTrailingComment(Comment c) {
        addComment(RelativePosition.TRAILING, c);
    }

    /**
     * Add a list of comments to the list of preceding comments.
     */
    public void addTrailingComments(List<Comment> comments) {
        for (Comment comment : comments) {
            addComment(RelativePosition.TRAILING, comment);
        }
    }
    
    public List<Comment> getPrecedingComments() {
        return getComments(RelativePosition.PRECEDING);
    }
    
    public List<Comment> getTrailingComments() {
        return getComments(RelativePosition.TRAILING);
    }
    
    public boolean hasComments() {
        return !commentsMap.isEmpty() || changed;
    }
    
    /** 
     * Returns the first character position, which is either the initial
     * position of the first preceding comment, or NOPOS if there are no comments.
     */
    public int pos() {
        return pos(RelativePosition.PRECEDING);
    }

    public int pos(RelativePosition position) {
        List<Comment> list = getComments(position);
        return list.isEmpty() ? NOPOS : list.get(0).pos();
    }

    /**
     * Adds a comment. Duplicate additions are ignored.
     * 
     * @param positioning relative positioning of the comment in the comment set
     * @param c the comment instance
     */
    public void addComment(RelativePosition positioning, Comment c) {
        addComment(positioning, c, -1);
    }

    /**
     * Inserts a comment to the specified index. Duplicate additions are ignored.
     * 
     * @param positioning relative positioning of the comment in the comment set
     * @param c the comment instance
     * @param index -1 to add comment to the end or index at which the comment should be added
     */
    public void insertComment(RelativePosition positioning, Comment c, int index) {
        addComment(positioning, c, index);
    }
    
    /**
     * First change should be tracked if all comments were initially mapped and no change
     * was made after that point yet. When first change occurs, the original comment Map
     * should be cloned and preserved for code generation phase.
     * 
     * @return true, if this is the first comment change.
     */
    private boolean trackFirstChange() {
        return (commentsMapped || commentsFrozen) && !changed;
    }
    
    /**
     * Adds a comment to the appropriate position. Newly created comments are always appended at the end.
     * If `mergeExisting' is true, copied comments are inserted according to their textual position among other already added comments. Duplicate
     * comments are ignored. This method is suitable for copying or collecting comments from several statements to 
     * a common target (mergeExisting = true).
     * 
     * @param positioning relative positioning of the comment in the comment set
     * @param c the comment instance
     * @param index
     */
    public void addComment(RelativePosition positioning, Comment c, int index) {
        Map<RelativePosition, List<Comment>> orig = null;
        if (trackFirstChange()) {
            orig = getOrigMap();
        }
        List<Comment> comments;
        if (commentsMap.containsKey(positioning)) {
            comments = commentsMap.get(positioning);
        } else {
            comments = new CL<Comment>();
            commentsMap.put(positioning, comments);
        }
        // new comments are always added at the end
        if (c.isNew()) {
            comments.add(c);
        } else {
            if (index < 0) {
                index = 0;
                int npos = c.pos();
                for (Comment o : comments) {
                    if (o.isNew()) {
                        comments.add(c);
                        break;
                    } else {
                        int pos = o.pos();
                        if (pos > npos) {
                            break;
                        } else if (pos == npos) {
                            if (c == o) {
                                // the same comment is being copied again; ignore.
                                return;
                            }
                        }
                    }
                    index++;
                }
            }
            comments.add(index, c);
        }
        if (orig != null) {
            origMap = orig;
        }
        changed = true;
    }

    public void addComments(RelativePosition positioning, Iterable<? extends Comment> comments) {
        for (Comment c : comments) {
            addComment(positioning, c);
        }
    }
    
    public List<Comment> getOrigComments(RelativePosition positioning) {
        if (origMap == null) {
            // no changes recorded yet
            return getComments(positioning);
        } else {
            List<Comment> c = origMap.get(positioning);
            return c != null ? c : Collections.<Comment>emptyList();
        }
    }
    
    public List<Comment> getComments(RelativePosition positioning) {
        if (commentsMap.containsKey(positioning)) {
            return commentsMap.get(positioning);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public boolean hasChanges() {
        return changed;
    }
    
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
	        throw new InternalError("Unexpected " + e);
        }
    }
    
    @SuppressWarnings({"MethodWithMultipleLoops"})
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<RelativePosition, List<Comment>> entry : commentsMap.entrySet()) {
            if (!first) {
                sb.append(", "); first = false;
            }
            sb.append("[").append(entry.getKey()).append(" -> ");
            for (Comment comment : entry.getValue()) {
                sb.append(',').append(comment.getText());
            }
            sb.append("]");
        }        
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * @return indicate that comments for this Tree were mapped in from source.
     */
    public boolean areCommentsMapped() {
        return commentsMapped;
    }

    /**
     * Temporary flag during comment replication, which makes CommentSet to pretend
     * it has all the comments already, so any change will result in comment cloning.
     */
    private boolean commentsFrozen;
    
    public void commentsFrozen(boolean freeze) {
        this.commentsFrozen = freeze;
    }
    
    public void commentsMapped() {
        commentsMapped = true;
        changed = false;
    }

    public void clearComments(RelativePosition forPosition) {
        commentsMap.remove(forPosition);
    }
    
    private Map<RelativePosition, List<Comment>> getOrigMap() {
        if (origMap != null) {
            return origMap;
        }
        if (commentsMap.isEmpty()) {
            return Collections.emptyMap();
        } else {
            return new HashMap<>(commentsMap);
        }
    }
    
    class CL<T> extends ArrayList<T> {
        @Override
        public T remove(int index) {
            if (trackFirstChange()) {
                origMap = getOrigMap();
            }
            changed = true;
            return super.remove(index);
        }

        @Override
        public boolean retainAll(Collection c) {
            Map<RelativePosition, List<Comment>> orig = trackFirstChange() ? getOrigMap() : null;
            boolean r = super.retainAll(c);
            if (orig != null && r) {
                origMap = orig;
            }
            changed |= r;
            return r;
        }

        @Override
        public boolean removeAll(Collection c) {
            Map<RelativePosition, List<Comment>> orig = trackFirstChange() ? getOrigMap() : null;
            boolean r = super.removeAll(c);
            if (orig != null && r) {
                origMap = orig;
            }
            changed |= r;
            return r;
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            boolean xch = trackFirstChange();
            changed |= (toIndex > fromIndex);
            if (xch && changed) {
                origMap = getOrigMap();
            }
            super.removeRange(fromIndex, toIndex);
        }

        @Override
        public boolean remove(Object o) {
            Map<RelativePosition, List<Comment>> orig = trackFirstChange() ? getOrigMap() : null;
            boolean r = super.remove(o);
            if (orig != null && r) {
                origMap = orig;
            }
            changed |= r;
            return r;
        }

        @Override
        public T set(int index, T element) {
            boolean xch = trackFirstChange();
            changed |= element != get(index);
            if (xch && changed) {
                origMap = getOrigMap();
            }
            return super.set(index, element);
        }
    }
}
