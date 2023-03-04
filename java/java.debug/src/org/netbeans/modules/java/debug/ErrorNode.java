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

import java.util.ArrayList;
import java.util.Iterator;
import org.openide.nodes.AbstractNode;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Shows all Errors inside navigator
 * @author Max Sauer
 */
public class ErrorNode extends AbstractNode implements OffsetProvider {

    private CompilationInfo info;
    private Diagnostic diag;
    
    /** Creates a new instance of ErrorNode */
    public ErrorNode(CompilationInfo info, Diagnostic diag) {
        super(Children.LEAF); //always leaf
        this.info = info;
        this.diag = diag;
        String ss = diag.getMessage(Locale.ENGLISH);
        setDisplayName(diag.getCode() + " " + diag.getKind() + ": " + ss); //NOI18N
        setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
    }
    
    public static Node getTree(CompilationInfo info) {
        List<Node> result = new ArrayList<Node>();
        new FindChildrenErrorVisitor(info).scan(result);
        Children.Array c = new Children.Array();
        c.add(result.toArray(new Node[0]));
        return new AbstractNode(c);
    }
    
    public int getStart() {
        return (int) diag.getStartPosition();
    }

    public int getEnd() {
        return (int) diag.getEndPosition();
    }

    public int getPreferredPosition() {
        return (int) diag.getPosition();
    }
    
    
    
    private static class FindChildrenErrorVisitor {
        
        private CompilationInfo info;
        
        public FindChildrenErrorVisitor(CompilationInfo info) {
            this.info = info;
        }
        
        private void scan(List<Node> result) {
            Iterator<Diagnostic> it = info.getDiagnostics().iterator();
            while(it.hasNext()) {
                Diagnostic diag = it.next();
                result.add(new ErrorNode(info, diag));
            }
        }
        
    }
    
}
