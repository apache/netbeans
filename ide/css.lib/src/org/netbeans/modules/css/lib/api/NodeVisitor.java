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
package org.netbeans.modules.css.lib.api;

import java.util.Collection;
import java.util.List;

/**
 * Allows to visit the css source parse trees.
 *
 * @author marekfukala
 */
public abstract class NodeVisitor<T> {
    
    private T result;
    private boolean cancelled = false;
    
    
    public NodeVisitor(T result) {
        this.result = result;
    }
    
    public NodeVisitor() {
        this(null);
    }
    
    /**
     * Performs the given node visit.
     * @param node
     * @return true if the visiting process should be interrupted
     */
    public abstract boolean visit(Node node);
    
    public T getResult() {
        return result;
    }
    
    /**
     * Implementors may use this flag to possibly stop the visit(...) method 
     * execution. The nodes recursive visiting is canceled automatically once
     * cancel() method is called.
     * 
     * @return true if the visitor should stop performing the code.
     */
    protected boolean isCancelled() {
        return cancelled;
    }
    
    public void cancel() {
        cancelled = true;
    }
    
    public void visitAncestors(Node node) {
        Node parent = node.parent();
        if (parent != null) {
            if(isCancelled()) {
                return ;
            }
            if(visit(parent)) {
                return; //visiting stopped by the visitor
            }
            visitAncestors(parent);
        }
    }
    
    public Node visitChildren(Node node) {
        List<Node> children = node.children();
        if (children != null) {
            for (Node child : children) {
                if(isCancelled()) {
                    return null;
                }
                if(visit(child)) {
                    return child; //visiting stopped by the visitor
                }
                //recursion
                Node breakNode = visitChildren(child); 
                if(breakNode != null) {
                    return breakNode;
                }
            }
        }
        return null;
    }
    
    public static <TE> void visitChildren(Node node, Collection<NodeVisitor<TE>> visitors) {
        List<Node> children = node.children();
        if (children != null) {
            for (Node child : children) {
                for(NodeVisitor<TE> v : visitors) {
                    if(v.isCancelled()) {
                        continue; //skip the cancelled visitors
                    }
                    v.visit(child);
                }
                //recursion
                visitChildren(child, visitors); 
            }
        }
    }
    
    
}
