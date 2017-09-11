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
