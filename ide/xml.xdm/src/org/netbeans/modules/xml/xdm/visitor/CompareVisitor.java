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

/*
 * CompareVisitor.java
 *
 * Created on August 31, 2005, 10:24 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.nodes.Token;

/**
 * Does the comparison for only the needed methods for a given node with its target.
 * We may be able to do this comparison in the Nodes thenseleves rather than 
 * in a visitor since we dont necessarily walk the tree.
 *
 * @author Vidhya Narayanan
 */
public class CompareVisitor implements XMLNodeVisitor {
	
	/** Creates a new instance of CompareVisitor */
	public CompareVisitor() {
	}
	
	public boolean compare(Node n1, Node n2) {
		target = n2;
		n1.accept(this);
		return result;
	}
	
	public void visit(Attribute attr) {
		result = false;
		if (target instanceof Attribute) {
			if (attr.getName().equals(((Attribute)target).getName()) &&
				attr.getValue().equals(((Attribute)target).getValue()))
				result = true;
			if (result)
				tokenCompare(attr.getTokens(), ((Attribute)target).getTokens());
		}
	}

	public void visit(Document doc) {
		if (target instanceof Document)
			result = true;
	}

	public void visit(Element e) {
		result = false;
		if (target instanceof Element) {
			if (e.getLocalName().equals(((Element)target).getLocalName()))
				result = true;
		}
		if (result)
			tokenCompare(e.getTokens(), ((Element)target).getTokens());
	}

	public void visit(Text txt) {
		result = false;
		if (target instanceof Text) {
			if (txt.getText().equals(((Text)target).getText()))
				result = true;
		}
		if (result)
			tokenCompare(txt.getTokens(), ((Text)target).getTokens());
	}
	
	private void tokenCompare(List<Token> oldtokens, List<Token> newtokens) {
		assert oldtokens != null && newtokens != null;
		if (oldtokens.size() != newtokens.size())
			result = false;
		else {
			int i = 0;
			for (Token t : oldtokens) {
				if (t.getType() != newtokens.get(i).getType() ||
					!t.getValue().equals(newtokens.get(i).getValue()))
					result = false;
				i++;
			}
		}
	}
	
	private Node target;
	boolean result = false;
}
