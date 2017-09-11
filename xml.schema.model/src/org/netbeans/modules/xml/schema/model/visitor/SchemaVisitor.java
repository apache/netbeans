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
 * This interface represents a way for
 * @author Chris Webster
 */
public interface SchemaVisitor {
	void visit(All all);
	void visit(Annotation ann);
	void visit(AnyElement any);
	void visit(AnyAttribute anyAttr);
        void visit(AppInfo appinfo);
	void visit(AttributeReference reference);
	void visit(AttributeGroupReference agr);
	void visit(Choice choice);
	void visit(ComplexContent cc);
	void visit(ComplexContentRestriction ccr);
	void visit(ComplexExtension ce);
	void visit(Documentation d);
	void visit(ElementReference er);
	void visit(Enumeration e);
	void visit(Field f);
	void visit(FractionDigits fd);
	void visit(GlobalAttribute ga);
	void visit(GlobalAttributeGroup gag);
	void visit(GlobalComplexType gct);
	void visit(GlobalElement ge);
	void visit(GlobalSimpleType gst);
	void visit(GlobalGroup gd);
	void visit(GroupReference gr);
	void visit(Import im);
	void visit(Include include);
	void visit(Key key);
	void visit(KeyRef kr);
	void visit(Length length);
	void visit(List l);
	void visit(LocalAttribute la);
	void visit(LocalComplexType type);
	void visit(LocalElement le);
	void visit(LocalSimpleType type);
	void visit(MaxExclusive me);
	void visit(MaxInclusive mi);
	void visit(MaxLength ml);
	void visit(MinInclusive mi);
	void visit(MinExclusive me);
	void visit(MinLength ml);
        void visit(Notation notation);
	void visit(Pattern p);
	void visit(Redefine rd);
	void visit(Schema s);
        void visit(Selector s);
	void visit(Sequence s);
	void visit(SimpleContent sc);
	void visit(SimpleContentRestriction scr);
	void visit(SimpleExtension se);
	void visit(SimpleTypeRestriction str);
	void visit(TotalDigits td);
	void visit(Union u);
	void visit(Unique u);
	void visit(Whitespace ws);
}
