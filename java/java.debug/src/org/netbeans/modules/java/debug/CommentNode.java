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

import org.netbeans.api.java.source.Comment;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class CommentNode extends AbstractNode implements OffsetProvider {

    private Comment comment;

    /** Creates a new instance of CommentNode */
    public CommentNode(Comment comment) {
        super(Children.LEAF);
        this.comment = comment;
        setDisplayName(NbBundle.getMessage(CommentNode.class, "NM_Comment"));
    }

    public int getStart() {
        return comment.pos();
    }

    public int getEnd() {
        return comment.endPos();
    }

    public int getPreferredPosition() {
        return -1;
    }
}
