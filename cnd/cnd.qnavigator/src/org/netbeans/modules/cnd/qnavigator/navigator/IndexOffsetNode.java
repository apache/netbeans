/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.qnavigator.navigator;

import org.openide.nodes.Node;

/**
 *
 */
public class IndexOffsetNode implements Comparable<IndexOffsetNode>{
    private final Node node;
    private long startOffset;
    private long endOffset;
    private IndexOffsetNode scope;
    /** Creates a new instance of IndexOffsetNode */
    public IndexOffsetNode(Node node, long startOffset, long endOffset) {
        this.node = node;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public long getStartOffset(){
        return startOffset;
    }

    public long getEndOffset(){
        return endOffset;
    }

    public IndexOffsetNode getScope(){
        return scope;
    }

    public void setScope(IndexOffsetNode scope){
        this.scope = scope;
    }

    public Node getNode(){
        return node;
    }
    
    void resetContent(IndexOffsetNode content){
        startOffset = content.startOffset;
        endOffset = content.endOffset;
    }
    
    @Override
    public int compareTo(IndexOffsetNode o) {
        if (getStartOffset() < o.getStartOffset()){
            return -1;
        } else if (getStartOffset() > o.getStartOffset()) {
            return 1;
        }
        return 0;
    }
    
    @Override
    public String toString(){
        return ""+startOffset+node.getDisplayName();
    }
}
