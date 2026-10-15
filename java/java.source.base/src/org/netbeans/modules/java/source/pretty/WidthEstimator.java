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
package org.netbeans.modules.java.source.pretty;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.comp.Operators;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeInfo;

import static com.sun.tools.javac.code.Flags.*;

/** Estimate the printed width of a tree
 */
public class WidthEstimator extends JCTree.Visitor {
    private int width;
    private int prec;
    private int maxwidth;
    private final Operators operators;

    public WidthEstimator(Context context) {
        operators = Operators.instance(context);
    }

    public int estimateWidth(JCTree t, int maxwidth) {
	width = 0;
	this.maxwidth = maxwidth;
	t.accept(this);
	return width;
    }
    public int estimateWidth(JCTree t) {
	return estimateWidth(t,100);
    }
    public int estimateWidth(List<? extends JCTree> t, int maxwidth) {
	width = 0;
	this.maxwidth = maxwidth;
        while(t.nonEmpty() && this.width < this.maxwidth) {
	    t.head.accept(this);
            t = t.tail;
        }
	return width;
    }
    private void open(int contextPrec, int ownPrec) {
	if (ownPrec < contextPrec)
	    width += 2;
    }
    private void width(Name n) { width += n.length(); }
    private void width(String n) { width += n.length(); }
    private void width(JCTree n) { if(width<maxwidth) n.accept(this); }
    private void width(List<? extends JCTree> n, int pad) {
	int nadd = 0;
	while(!n.isEmpty() && width<maxwidth) {
	    width(n.head);
	    n = n.tail;
	    nadd++;
	}
	if(nadd>1) width += pad*nadd;
    }
    private void width(List<? extends JCTree> n) {
	width(n, 2);
    }
    private void width(JCTree tree, int prec) {
	if (tree != null) {
	    int prevPrec = this.prec;
	    this.prec = prec;
	    tree.accept(this);
	    this.prec = prevPrec;
	}
    }
    public void visitTree(JCTree tree) {
System.err.println("Need width calc for "+tree);
	width = maxwidth;
    }
    public void visitParens(JCParens tree) {
	width+=2;
	width(tree.expr);
    }
    public void visitApply(JCMethodInvocation tree) {
	width+=2;
	width(tree.meth, TreeInfo.postfixPrec);
	width(tree.args);
    }
    public void visitNewClass(JCNewClass tree) {
	if (tree.encl != null) {
	    width(tree.encl);
	    width++;
	}
	width+=4;
        width(tree.clazz);
	width+=2;
	width(tree.args, 2);
	if (tree.def != null) {
	    width+=4;
	    width(((JCClassDecl) tree.def).defs, 2);
	}
    }
    public void visitNewArray(JCNewArray tree) {
	if (tree.elemtype != null) {
	    width+=4;
	    JCTree elemtype = tree.elemtype;
	    while (elemtype.getTag() == JCTree.Tag.TYPEARRAY) {
		width+=2;
		elemtype = ((JCArrayTypeTree) elemtype).elemtype;
	    }
	    width(elemtype);
	    for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
		width+=2;
		width(l.head);
	    }
	}
	if (tree.elems != null) {
	    width+=4;
	    width(tree.elems);
	}
    }
    private void widthAnnotations(List<JCAnnotation> anns) {
	int nadd = 0;
	while(!anns.isEmpty() && width<maxwidth) {
            width++; // '@'
	    width(anns.head);
	    anns = anns.tail;
	    nadd++;
	}
	if(nadd>1) width += nadd;
        
    }
    private void widthFlags(long flags) {
	if ((flags & SYNTHETIC) != 0)
	    width+=14;
	width+=VeryPretty.flagNames(flags).length();
	if ((flags & StandardFlags) != 0)
	    width++;
    }
    public void visitVarDef(JCVariableDecl tree) {
        widthAnnotations(tree.mods.annotations);
        if ((tree.mods.flags & Flags.ENUM) == 0) {
            widthFlags(tree.mods.flags);
            if (tree.vartype != null)
                width(tree.vartype);
            width++;
        }
        width(tree.name);
        if (tree.init != null && (tree.mods.flags & Flags.ENUM) == 0) {
            width+=3;
            width(tree.init);
        }
    }
    public void visitConditional(JCConditional tree) {
	open(prec, TreeInfo.condPrec);
	width+=6;
	width(tree.cond, TreeInfo.condPrec-1);
	width(tree.truepart, TreeInfo.condPrec);
	width(tree.falsepart, TreeInfo.condPrec);
    }

    public void visitAssignop(JCAssignOp tree) {
	open(prec, TreeInfo.assignopPrec);
	width+=3;
	width(operators.operatorName(tree.getTag()));
	width(tree.lhs, TreeInfo.assignopPrec + 1);
	width(tree.rhs, TreeInfo.assignopPrec);
    }
    public void visitAssign(JCAssign tree) {
	open(prec, TreeInfo.assignPrec);
	width+=3;
	width(tree.lhs, TreeInfo.assignPrec + 1);
	width(tree.rhs, TreeInfo.assignPrec);
    }
    public void visitUnary(JCUnary tree) {
	int ownprec = TreeInfo.opPrec(tree.getTag());
	Name opname = operators.operatorName(tree.getTag());
	open(prec, ownprec);
	width(opname);
	width(tree.arg, ownprec);
    }
    public void visitBinary(JCBinary tree) {
	int ownprec = TreeInfo.opPrec(tree.getTag());
	Name opname = operators.operatorName(tree.getTag());
	open(prec, ownprec);
	width(opname);
	width+=2;
	width(tree.lhs, ownprec);
	width(tree.rhs, ownprec + 1);
    }
    public void visitTypeCast(JCTypeCast tree) {
	width+=2;
	open(prec, TreeInfo.prefixPrec);
	width(tree.clazz);
	width(tree.expr, TreeInfo.prefixPrec);
    }

    public void visitTypeTest(JCInstanceOf tree) {
	open(prec, TreeInfo.ordPrec);
	width += 12;
	width(tree.expr, TreeInfo.ordPrec);
	width(tree.pattern);
    }

    public void visitIndexed(JCArrayAccess tree) {
	width+=2;
	width(tree.indexed, TreeInfo.postfixPrec);
	width(tree.index);
    }

    public void visitSelect(JCFieldAccess tree) {
        width+=1;
        width(tree.selected, TreeInfo.postfixPrec);
        width(tree.name);
    }

    public void visitIdent(JCIdent tree) {
	width(tree.name);
    }

    public void visitLiteral(JCLiteral tree) {
	switch (tree.typetag) {
	  case LONG:
	  case FLOAT:
	    width++;
	    width(tree.value.toString());
	    break;
	  case CHAR:
	    width += 3;
	    break;
	  case CLASS:
	    width+=2;
	    width(tree.value.toString());
	    break;
          case BOOLEAN:
            width(((Number)tree.value).intValue() == 1 ? "true" : "false");
            break;
          case BOT:
            width("null");
            break;
	  default:
	    width(tree.value.toString());
	}
    }

    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
        // see defect #239258; in ENGLISH locale, lowercase string has the same length as uppercase, the conversion can be omitted.
	width(tree.typetag.name());
    }

    public void visitTypeArray(JCArrayTypeTree tree) {
	width(tree.elemtype);
	width+=2;
    }

    @Override
    public void visitTypeApply(JCTypeApply that) {
        width(that.clazz);
        width(that.arguments);
        width += 2 * that.arguments.size();
    }

}
