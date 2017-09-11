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

package org.netbeans.modules.xml.schema.model.visitor;

import org.netbeans.modules.xml.schema.model.*;

/**
 * This visitor will visit all nodes in a tree. A typical subclass visitor
 * would inherit from this class and override methods for elements which need
 * to be visited. This would remove the reliance on the heirarchy for visiting.
 * @author Chris Webster
 */
public class DeepSchemaVisitor implements SchemaVisitor {
    public void visit(LocalSimpleType type) {
        visitChildren(type);
    }

    public void visit(Union u) {
        visitChildren(u);
    }

    public void visit(Enumeration e) {
        visitChildren(e);
    }

    public void visit(AttributeGroupReference agr) {
        visitChildren(agr);
    }

    public void visit(GlobalAttributeGroup gag) {
        visitChildren(gag);
    }

    public void visit(KeyRef kr) {
        visitChildren(kr);
    }

    public void visit(GlobalSimpleType gst) {
        visitChildren(gst);
    }

    public void visit(AnyElement any) {
        visitChildren(any);
    }

    public void visit(Include include) {
        visitChildren(include);
    }

    public void visit(MinInclusive mi) {
        visitChildren(mi);
    }

    public void visit(Import im) {
        visitChildren(im);
    }

    public void visit(Choice choice) {
        visitChildren(choice);
    }

    public void visit(Unique u) {
        visitChildren(u);
    }

    public void visit(MaxLength ml) {
        visitChildren(ml);
    }

    public void visit(Redefine rd) {
        visitChildren(rd);
    }

    public void visit(SimpleContentRestriction scr) {
        visitChildren(scr);
    }

    public void visit(LocalElement le) {
        visitChildren(le);
    }
    
    public void visit(ElementReference le) {
        visitChildren(le);
    }

    public void visit(Selector s) {
        visitChildren(s);
    }

    public void visit(Annotation ann) {
        visitChildren(ann);
    }

    public void visit(ComplexExtension ce) {
        visitChildren(ce);
    }

    public void visit(FractionDigits fd) {
        visitChildren(fd);
    }

    public void visit(SimpleExtension se) {
        visitChildren(se);
    }

    public void visit(Whitespace ws) {
        visitChildren(ws);
    }

    public void visit(LocalComplexType type) {
        visitChildren(type);
    }

    public void visit(TotalDigits td) {
        visitChildren(td);
    }

    public void visit(MaxExclusive me) {
        visitChildren(me);
    }

    public void visit(SimpleContent sc) {
        visitChildren(sc);
    }

    public void visit(AnyAttribute anyAttr) {
        visitChildren(anyAttr);
    }

    public void visit(GlobalAttribute ga) {
        visitChildren(ga);
    }

    public void visit(All all) {
        visitChildren(all);
    }

    public void visit(ComplexContentRestriction ccr) {
        visitChildren(ccr);
    }

    public void visit(GroupReference gr) {
        visitChildren(gr);
    }

    public void visit(Key key) {
        visitChildren(key);
    }

    public void visit(List l) {
        visitChildren(l);
    }

    public void visit(Pattern p) {
        visitChildren(p);
    }

    public void visit(Documentation d) {
        visitChildren(d);
    }

    public void visit(AppInfo d) {
        visitChildren(d);
    }

    public void visit(Notation notation) {
        visitChildren(notation);
    }

    public void visit(MinExclusive me) {
        visitChildren(me);
    }

    public void visit(GlobalGroup gd) {
        visitChildren(gd);
    }

    public void visit(MinLength ml) {
        visitChildren(ml);
    }

    public void visit(Schema s) {
        visitChildren(s);
    }

    public void visit(GlobalComplexType gct) {
        visitChildren(gct);
    }

    public void visit(Sequence s) {
        visitChildren(s);
    }

    public void visit(MaxInclusive mi) {
        visitChildren(mi);
    }

    public void visit(SimpleTypeRestriction str) {
        visitChildren(str);
    }

    public void visit(LocalAttribute la) {
        visitChildren(la);
    }
    
    public void visit(AttributeReference la) {
        visitChildren(la);
    }

    public void visit(ComplexContent cc) {
        visitChildren(cc);
    }

    public void visit(GlobalElement ge) {
        visitChildren(ge);
    }

    public void visit(Length length) {
        visitChildren(length);
    }

    public void visit(Field f) {
        visitChildren(f);
    }
    
    protected void visitChildren(SchemaComponent sc) {
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
        }
    }
}
