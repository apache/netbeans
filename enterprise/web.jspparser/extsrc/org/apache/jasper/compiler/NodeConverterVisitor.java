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

package org.apache.jasper.compiler;

import org.apache.jasper.JasperException;

import org.xml.sax.Attributes;
import java.util.*;
import jakarta.servlet.jsp.tagext.TagAttributeInfo;


// Note: this needs to live in the org.apache.jasper.compiler package.
class NodeConverterVisitor extends Node.Visitor {
    private final org.netbeans.modules.web.jsps.parserapi.Node parentNode;
    private List<org.netbeans.modules.web.jsps.parserapi.Node> convertedNodeList = null;

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
    
    @SuppressWarnings("UseOfObsoleteCollectionType")
    protected org.netbeans.modules.web.jsps.parserapi.Node.Nodes
           convertNodesList(Node.Nodes jasperNodes) throws JasperException {
	convertedNodeList = new Vector<>();
        jasperNodes.visit(this);
        return new org.netbeans.modules.web.jsps.parserapi.Node.Nodes(convertedNodeList);
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

    @Override
    public void visit(Node.PageDirective n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.PageDirective(n.getAttributes(),
									      convertMark(n.getStart()),
									      parentNode);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.TaglibDirective n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.TaglibDirective(n.getAttributes(),
										convertMark(n.getStart()),
										parentNode);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.AttributeDirective n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.AttributeDirective cn =
		new org.netbeans.modules.web.jsps.parserapi.Node.AttributeDirective(n.getAttributes(), 
									      convertMark(n.getStart()),
									      parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.VariableDirective n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.VariableDirective cn =
		new org.netbeans.modules.web.jsps.parserapi.Node.VariableDirective(n.getAttributes(), 
									      convertMark(n.getStart()),
									      parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.IncludeDirective n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.IncludeDirective cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.IncludeDirective(n.getAttributes(),
									      convertMark(n.getStart()),
									      parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.Comment n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.Comment(n.getText(),
									convertMark(n.getStart()),
									parentNode);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.Declaration n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.Declaration(n.getText(),
									    convertMark(n.getStart()),
									    parentNode);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.Expression n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.Expression(n.getText(),
									   convertMark(n.getStart()),
									   parentNode);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.Scriptlet n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.Scriptlet(n.getText(),
									  convertMark(n.getStart()),
									  parentNode);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.IncludeAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.IncludeAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.IncludeAction(n.getAttributes(),
									   convertMark(n.getStart()),
									   parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.DoBodyAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.DoBodyAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.DoBodyAction(n.getAttributes(),
									   convertMark(n.getStart()),
									   parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }
    
    @Override
    public void visit(Node.ForwardAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.ForwardAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.ForwardAction(n.getAttributes(),
									   convertMark(n.getStart()),
									   parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.GetProperty n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.GetProperty(n.getAttributes(),
									    convertMark(n.getStart()),
									    parentNode);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.SetProperty n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.SetProperty cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.SetProperty(n.getAttributes(),
									 convertMark(n.getStart()),
									 parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.UseBean n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.UseBean cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.UseBean(n.getAttributes(),
								     convertMark(n.getStart()),
								     parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.PlugIn n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.PlugIn cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.PlugIn(n.getAttributes(),
								    convertMark(n.getStart()),
								    parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.ParamsAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.ParamsAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.ParamsAction(convertMark(n.getStart()),
									  parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.ParamAction n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.ParamAction cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.ParamAction(n.getAttributes(),
									 convertMark(n.getStart()),
									 parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.InvokeAction n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.InvokeAction(n.getAttributes(),
									  convertMark(n.getStart()),
									  parentNode);
	convertedNodeList.add(cn);
    }		

    @Override
    public void visit(Node.NamedAttribute n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.NamedAttribute cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.NamedAttribute(n.getAttributes(),
									    convertMark(n.getStart()),
									    parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.JspBody n) throws JasperException {
	org.netbeans.modules.web.jsps.parserapi.Node.JspBody cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.JspBody(convertMark(n.getStart()),
								     parentNode);
	convertBody(n, cn);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.ELExpression n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.ELExpression(n.getText(),
									     convertMark(n.getStart()),
									     parentNode);
	convertedNodeList.add(cn);
    }

    @Override
    public void visit(Node.CustomTag n) throws JasperException {
        org.netbeans.modules.web.jsps.parserapi.Node.CustomTag cn;
        if (n.getTagFileInfo() == null) {
            // no tag file
            cn = new org.netbeans.modules.web.jsps.parserapi.Node.CustomTag(n.getQName(),
                    n.getPrefix(),
                    n.getLocalName(),
                    n.getURI(),
                    n.getAttributes(),
                    convertMark(n.getStart()),
                    parentNode,
                    n.getTagHandlerClass()
            );
        } else {
            // we do have a tag file
            Set<String> fragmentAttributes = new HashSet<>();
            for (TagAttributeInfo tai : n.getTagFileInfo().getTagInfo().getAttributes()) {
                if (tai.isFragment()) {
                    fragmentAttributes.add(tai.getName());
                }
            }
            cn = new org.netbeans.modules.web.jsps.parserapi.Node.CustomTag(n.getQName(),
                    n.getPrefix(),
                    n.getLocalName(),
                    n.getURI(),
                    n.getAttributes(),
                    convertMark(n.getStart()),
                    parentNode,
                    true,
                    n.getTagFileInfo().getTagInfo().hasDynamicAttributes(),
                    fragmentAttributes
            );
        }
        convertBody(n, cn);
        convertedNodeList.add(cn);
    }

    @Override
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

    @Override
    public void visit(Node.TemplateText n) {
	org.netbeans.modules.web.jsps.parserapi.Node cn =
	    new org.netbeans.modules.web.jsps.parserapi.Node.TemplateText(n.getText(),
									  convertMark(n.getStart()),
									  parentNode);
	convertedNodeList.add(cn);
    }

}

