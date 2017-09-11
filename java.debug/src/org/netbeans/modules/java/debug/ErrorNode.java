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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
