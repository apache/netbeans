/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
                for(NodeVisitor v : visitors) {
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
