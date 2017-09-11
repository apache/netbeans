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
package org.netbeans.modules.java.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.modules.java.source.save.DiffContext;

/** Temporary Should be removed soon
 *
 * @author phrebejk
 */
public class ElementHeaderFormater {

    private ElementHeaderFormater() {
    }
    
    public static String getMethodHeader(MethodTree tree, ClassTree enclosingClass, CompilationInfo info, String s) {
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        if (enclosingClass != null) {
            veryPretty.enclClass = (com.sun.tools.javac.tree.JCTree.JCClassDecl) enclosingClass;
        }
        return veryPretty.getMethodHeader(tree, s);
    }

    public static String getClassHeader(ClassTree tree, CompilationInfo info, String s) {
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        return veryPretty.getClassHeader(tree, s);
    }
    
    public static String getVariableHeader(VariableTree tree, CompilationInfo info, String s) {
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        return veryPretty.getVariableHeader(tree, s);
    }
    
}
