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
