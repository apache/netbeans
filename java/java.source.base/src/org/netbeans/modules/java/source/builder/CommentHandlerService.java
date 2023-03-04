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

package org.netbeans.modules.java.source.builder;

import org.netbeans.api.java.source.Comment;
import org.netbeans.modules.java.source.query.CommentHandler;

import com.sun.source.tree.Tree;

import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;

import java.util.*;
import org.netbeans.modules.java.source.query.CommentSet.RelativePosition;


/**
 * Generate Comments during scanning.
 */
public class CommentHandlerService implements CommentHandler {
    private static final Context.Key<CommentHandlerService> commentHandlerKey = 
        new Context.Key<CommentHandlerService>();
    
    /** Get the CommentMaker instance for this context. */
    public static CommentHandlerService instance(Context context) {
	CommentHandlerService instance = context.get(commentHandlerKey);
	if (instance == null) {
	    instance = new CommentHandlerService(context);
            setCommentHandler(context, instance);
        }
	return instance;
    }
    
    /**
     * Called from reattributor.
     */
    public static void setCommentHandler(Context context, CommentHandlerService instance) {
        assert context.get(commentHandlerKey) == null;
        context.put(commentHandlerKey, instance);
    }

    private final Map<Tree, CommentSetImpl> map = new HashMap<Tree, CommentSetImpl>();
    
    private boolean frozen;
    
    private CommentHandlerService(Context context) {
    }
    
    Map<Tree, CommentSetImpl> getCommentMap() {
        Map<Tree, CommentSetImpl> m = new HashMap<>(map);
        for (Iterator<Map.Entry<Tree, CommentSetImpl>> it = m.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Tree, CommentSetImpl> e = it.next();
            if (!e.getValue().hasComments()) {
                it.remove();
            }
        }
        return m;
    }
    
    public void freeze() {
        frozen = true;
        for (CommentSetImpl impl : map.values()) {
            impl.commentsFrozen(true);
        }
    }

    public void unFreeze() {
        frozen = false;
        for (CommentSetImpl impl : map.values()) {
            impl.commentsFrozen(false);
        }
    }
    
    public boolean hasComments(Tree tree) {
        synchronized (map) {
            return map.containsKey(tree);
        }
    }
    
    public CommentSetImpl getComments(Tree tree) {
        synchronized (map) {
            CommentSetImpl cs = map.get(tree);
            if (cs == null) {
                // note - subsequent change to the CommentSetImpl will clone the old (empty) set of comments into CommentSetImpl
                // optimization NOT to retain empty CSImpls is not possible; the caller may modify the return value.
                cs = new CommentSetImpl();
                if (frozen) {
                    cs.commentsFrozen(frozen);
                }
                map.put(tree, cs);
            }
            return cs;
        }
    }

    /**
     * Copies preceding and trailing comments from one tree to another,
     * appending the new entries to the existing comment lists.
     */
    public void copyComments(Tree fromTree, Tree toTree) {
        copyComments(fromTree, toTree, null, null, false);
    }
        
    /**
     * Copies comments from one Tree to another.
     * If non-empty is true, the contents of 'relative position' is only copied if it contains non-whitespaces. This is used
     * when moving comments to an unrelated Tree, often changing RelativePosition (copyToPos != null) - whitespaces at the start
     * or end only mess up the source.
     */
    public void copyComments(Tree fromTree, Tree toTree, RelativePosition copyToPos, Collection<Comment> copied, boolean nonEmpty) {
        if (fromTree == toTree) {
            return;
        }
        synchronized (map) {
            CommentSetImpl from = map.get(fromTree);
            if (from != null) {
                CommentSetImpl to = map.get(toTree);
                if (to == null) {
                    map.put(toTree, to = new CommentSetImpl());
                    if (frozen) {
                        to.commentsFrozen(true);
                    }
                }
                for (RelativePosition pos : RelativePosition.values()) {
                    int index = 0;
                    int last = -1;
                    int first = 0;
                    List<Comment> l = from.getComments(pos);
                    if (nonEmpty) {
                        boolean nonWs = false;
                        for (Comment c : l) {
                            if (c.style()  != Comment.Style.WHITESPACE) {
                                last = index;
                                if (!nonWs) {
                                    first = index;
                                    nonWs = true;
                                }
                            }
                            index++;
                        }
                        if (!nonWs) {
                            continue;
                        }
                    } 
                    if (last == -1) {
                        last = l.size() - 1;
                    }
                    CopyEntry en = new CopyEntry();
                    for (index = first; index <= last; index++) {
                        Comment c = l.get(index);
                        if (copied != null && !copied.add(c)) {
                            continue;
                        }
                        to.addComment(copyToPos == null ? pos : copyToPos, c);
                    }
                }
            }
        }
    }
    
    private static class CopyEntry {
        private Tree target;
        private RelativePosition pos;
        private Collection<Comment> comments = new ArrayList<>();
    }
    
    /**
     * Add a comment to a tree's comment set.  If a comment set
     * for the tree doesn't exist, one will be created.
     */
    public void addComment(Tree tree, Comment c) {
        synchronized (map) {
            CommentSetImpl set = map.get(tree);
            if (set == null) {
                set = new CommentSetImpl();
                if (frozen) {
                    set.commentsFrozen(frozen);
                }
                map.put(tree, set);
            }
            set.addPrecedingComment(c);
        }
    }


    public String toString() {
        return "CommentHandlerService[" +
                "map=" + map +
                ']';
    }
}
