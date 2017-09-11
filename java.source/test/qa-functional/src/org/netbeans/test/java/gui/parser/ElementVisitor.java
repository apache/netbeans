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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.test.java.gui.parser;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.PrintStream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner6;
import org.netbeans.api.java.source.CompilationController;

/**
 *
 * @author Jiri Prox
 */
public class ElementVisitor extends ElementScanner6<Void,PrintStream> {
    
    private CompilationController cc;
    
    private boolean canceled = false;
    
    private String text;
    void cancel() {
        canceled = true;
    }
    
    public ElementVisitor(CompilationController cc, String text) {
        this.text = text;
        this.cc = cc;
    }
    
    @Override
    public Void visitPackage(PackageElement arg0, PrintStream arg1) {
        if(!canceled) {
            arg1.print("Package Element: ");
            arg1.print(arg0.getSimpleName());
            long[] pos = getPosition(arg0);
            arg1.println(" "+pos[0]+" "+pos[1]);
            if(pos[1]!=-1) arg1.println(text.substring((int)pos[0],(int)pos[1]));
            return super.visitPackage(arg0, arg1);
        }
        return null;
    }
    
    @Override
    public Void visitType(TypeElement arg0, PrintStream arg1) {
        if(!canceled) {
            arg1.print("Type Element: ");
            arg1.print(arg0.getSimpleName());
            long[] pos = getPosition(arg0);
            arg1.println(" "+pos[0]+" "+pos[1]);
            if(pos[1]!=-1) arg1.println(text.substring((int)pos[0],(int)pos[1]));
            return super.visitType(arg0, arg1);
        }
        return null;
        
    }
    
    @Override
    public Void visitVariable(VariableElement arg0, PrintStream arg1) {
        if(!canceled) {
            arg1.print("Variable Element: ");
            arg1.print(arg0.getSimpleName());
            long[] pos = getPosition(arg0);
            arg1.println(" "+pos[0]+" "+pos[1]);
            if(pos[1]!=-1) arg1.println(text.substring((int)pos[0],(int)pos[1]));
            return super.visitVariable(arg0, arg1);
        }
        return null;
    }
    
    @Override
    public Void visitExecutable(ExecutableElement arg0, PrintStream arg1) {
        if(!canceled) {
            arg1.print("Executable Element: ");
            arg1.print(arg0.getSimpleName());
            long[] pos = getPosition(arg0);
            arg1.println(" "+pos[0]+" "+pos[1]);
            if(pos[1]!=-1) arg1.println(text.substring((int)pos[0],(int)pos[1]));
            return super.visitExecutable(arg0, arg1);
        }
        return null;
    }
    
    @Override
    public Void visitTypeParameter(TypeParameterElement arg0, PrintStream arg1) {
        if(!canceled) {
            arg1.print("TypeParameter Element: ");
            arg1.print(arg0.getSimpleName());
            long[] pos = getPosition(arg0);
            arg1.println(" "+pos[0]+" "+pos[1]);
            if(pos[1]!=-1) arg1.println(text.substring((int)pos[0],(int)pos[1]));
            return super.visitTypeParameter(arg0, arg1);
        }
        return null;
    }
    
    
    
    private long[] getPosition( Element e ) {
        Trees trees = cc.getTrees();
        CompilationUnitTree cut = cc.getCompilationUnit();
        Tree t = trees.getTree(e);        
        if ( t == null ) {            
            return new long[]{-1,-1};
        }        
        SourcePositions sourcePositions = trees.getSourcePositions();        
        return new long[] {sourcePositions.getStartPosition(cut, t),sourcePositions.getEndPosition(cut, t)};
    }
               
}
