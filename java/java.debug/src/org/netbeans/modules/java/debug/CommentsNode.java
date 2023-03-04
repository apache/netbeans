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
package org.netbeans.modules.java.debug;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.Comment;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jan Lahoda
 */
public class CommentsNode extends AbstractNode implements OffsetProvider {

    private List<Comment> comments;

    /** Creates a new instance of CommentNode */
    public CommentsNode(String displayName, List<Comment> comments) {
        super(new ChildrenImpl(comments));
        this.comments = comments;
        setDisplayName(displayName);
    }

    public int getStart() {
        int start = Integer.MAX_VALUE;
        
        for (Comment c : comments) {
            if (start > c.pos()) {
                start = c.pos();
            }
        }
        
        return start == Integer.MAX_VALUE ? (-1) : start;
    }

    public int getEnd() {
        int end = -1;
        
        for (Comment c : comments) {
            if (end < c.endPos()) {
                end = c.endPos();
            }
        }
        
        return end;
    }

    public int getPreferredPosition() {
        return -1;
    }

    private static final class ChildrenImpl extends Children.Keys {

        private List<Comment> comments;
        
        public ChildrenImpl(List<Comment> comments) {
            this.comments = comments;
        }
        
        public void addNotify() {
            setKeys(comments);
        }
        
        public void removeNotify() {
            setKeys(Collections.emptyList());
        }
        
        protected Node[] createNodes(Object key) {
            return new Node[] {new CommentNode((Comment) key)};
        }
        
    }
    
}
