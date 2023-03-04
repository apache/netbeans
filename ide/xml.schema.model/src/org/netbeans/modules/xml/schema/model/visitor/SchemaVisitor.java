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
