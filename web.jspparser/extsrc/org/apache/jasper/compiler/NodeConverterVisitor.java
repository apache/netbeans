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

package org.apache.jasper.compiler;

import org.apache.jasper.JasperException;

import org.xml.sax.Attributes;
import java.util.*;


// Note: this needs to live in the org.apache.jasper.compiler package.
class NodeConverterVisitor extends Node.Visitor {
	    // walk the nodes, convert them, then new Nodes(List).
	    // (tomorrow)
    org.netbeans.modules.web.jsps.parserapi.Node parentNode;
    List convertedNodeList = null;
    public NodeConverterVisitor(org.netbeans.modules.web.jsps.parserapi.Node parentNode) {
	this.parentNode = parentNode;
    }
    public static org.netbeans.modules.web.jsps.parserapi.Node.Nodes
	convertNodes(Node.Nodes jasperNodes) throws JasperException {
	return convertNodes(jasperNodes, null);
    }

    public static org.netbeans.modules.web.jsps.parserapi.Node.Nodes
	          convertNodes(Node.Nodes jasperNodes,
			       org.netbeans.modules.web.jsps.parserapi.Node parentNode) throws JasperException {
	NodeConverterVisitor serra = new NodeConverterVisitor(parentNode);
	return serra.convertNodesList(jasperNodes);
    }
    
    protected org.netbeans.modules.web.jsps.parserapi.Node.Nodes
           convertNodesList(Node.Nodes jasperNodes) throws JasperException {
	convertedNodeList = new Vector();
        int numChildNodes = jasperNodes.size();
        jasperNodes.visit(this);
        org.netbeans.modules.web.jsps.parserapi.Node.Nodes nbNodes = 
            new org.netbeans.modules.web.jsps.parserapi.Node.Nodes(convertedNodeList);
         return nbNodes;
    }
    
    public void convertBody(Node jn, org.netbeans.modules.web.jsps.parserapi.Node parentNode) 
    throws JasperException {
	Node.Nodes jnodes = jn.getBody();
	if (jnodes == null)
	    return;

	org.netbeans.modules.web.jsps.parserapi.Node.Nodes cnodes =
	    NodeConverterVisitor.convertNodes(jnodes, parentNode);
	
	parentNode.setBody(cnodes);
    }

    
    public org.netbeans.modules.web.jsps.parserapi.Mark convertMark(Mark m) {
        if (m == null) {
            return null;
        }
        else {
            return new org.netbeans.modules.web.jsps.parserapi.Mark(m.getFile(),
								m.getLineNumber(),
								m.getColumnNumber());
        }
    }

    public void visit(Node.PageDirective n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.PageDirective(n.getAttributes(),
									      convertMark(n.getStart()),
									      parentNode);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.TaglibDirective n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.TaglibDirective(n.getAttributes(),
										convertMark(n.getStart()),
										parentNode);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.AttributeDirective n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.AttributeDirective cn =
		new org.netbeans.modules.web.jsps.parserapi.Node.AttributeDirective(n.getAttributes(), 
									      convertMark(n.getStart()),
									      parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    public void visit(Node.VariableDirective n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.VariableDirective cn =
		new org.netbeans.modules.web.jsps.parserapi.Node.VariableDirective(n.getAttributes(), 
									      convertMark(n.getStart()),
									      parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    public void visit(Node.IncludeDirective n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.IncludeDirective cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.IncludeDirective(n.getAttributes(),
									      convertMark(n.getStart()),
									      parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.Comment n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.Comment(n.getText(),
									convertMark(n.getStart()),
									parentNode);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.Declaration n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.Declaration(n.getText(),
									    convertMark(n.getStart()),
									    parentNode);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.Expression n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.Expression(n.getText(),
									   convertMark(n.getStart()),
									   parentNode);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.Scriptlet n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.Scriptlet(n.getText(),
									  convertMark(n.getStart()),
									  parentNode);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.IncludeAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.IncludeAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.IncludeAction(n.getAttributes(),
									   convertMark(n.getStart()),
									   parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.DoBodyAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.DoBodyAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.DoBodyAction(n.getAttributes(),
									   convertMark(n.getStart()),
									   parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    
    public void visit(Node.ForwardAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.ForwardAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.ForwardAction(n.getAttributes(),
									   convertMark(n.getStart()),
									   parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.GetProperty n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.GetProperty(n.getAttributes(),
									    convertMark(n.getStart()),
									    parentNode);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.SetProperty n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.SetProperty cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.SetProperty(n.getAttributes(),
									 convertMark(n.getStart()),
									 parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.UseBean n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.UseBean cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.UseBean(n.getAttributes(),
								     convertMark(n.getStart()),
								     parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.PlugIn n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.PlugIn cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.PlugIn(n.getAttributes(),
								    convertMark(n.getStart()),
								    parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.ParamsAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.ParamsAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.ParamsAction(convertMark(n.getStart()),
									  parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.ParamAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.ParamAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.ParamAction(n.getAttributes(),
									 convertMark(n.getStart()),
									 parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.InvokeAction n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.InvokeAction(n.getAttributes(),
									  convertMark(n.getStart()),
									  parentNode);
	convertedNodeList.add(cn);
    }		
    
    public void visit(Node.NamedAttribute n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.NamedAttribute cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.NamedAttribute(n.getAttributes(),
									    convertMark(n.getStart()),
									    parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.JspBody n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.JspBody cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.JspBody(convertMark(n.getStart()),
								     parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.ELExpression n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.ELExpression(n.getText(),
									     convertMark(n.getStart()),
									     parentNode);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.CustomTag n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.CustomTag cn = null;
        if (n.getTagFileInfo() == null) {
            // no tag file
            cn = new org.netbeans.modules.web.jsps.parserapi.Node.CustomTag(n.getQName(),
                                                                            n.getPrefix(),
                                                                            n.getLocalName(),
                                                                            n.getURI(),
                                                                            n.getAttributes(),
                                                                            convertMark(n.getStart()),
                                                                            parentNode,
                                                                            n.getTagInfo(),
                                                                            n.getTagHandlerClass()
            );
        }
        else {
            // we do have a tag file
            cn = new org.netbeans.modules.web.jsps.parserapi.Node.CustomTag(n.getQName(),
                                                                            n.getPrefix(),
                                                                            n.getLocalName(),
                                                                            n.getURI(),
                                                                            n.getAttributes(),
                                                                            convertMark(n.getStart()),
                                                                            parentNode,
                                                                            n.getTagFileInfo()
            );
        }
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.UninterpretedTag n) throws JasperException {
	Attributes nonTaglibXmlnsAttrs = null; // ??
	Attributes taglibAttrs = null; // ??
	org.netbeans.modules.web.jsps.parserapi.Node.UninterpretedTag cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.UninterpretedTag(n.getQName(),
									      n.getLocalName(),
									      n.getAttributes(),
									      nonTaglibXmlnsAttrs,
									      taglibAttrs,
									      convertMark(n.getStart()),
									      parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    public void visit(Node.TemplateText n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.TemplateText(n.getText(),
									  convertMark(n.getStart()),
									  parentNode);
	convertedNodeList.add(cn);
    }		
    
    
}

