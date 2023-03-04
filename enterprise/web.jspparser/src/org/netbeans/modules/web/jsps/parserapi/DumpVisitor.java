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

package org.netbeans.modules.web.jsps.parserapi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.JspException;
import org.xml.sax.Attributes;

class DumpVisitor extends Node.Visitor {
    
    private static final Logger LOGGER = Logger.getLogger(DumpVisitor.class.getName());

    private int indent = 0;

    private StringBuilder buf;

    private DumpVisitor() {
        super();
        buf = new StringBuilder();
    }

    /**
     * This method provides a place to put actions that are common to
     * all nodes. Override this in the child visitor class if need to.
     */
    protected void visitCommon(Node n) throws JspException {
        printString("\nNode [" + n.getStart() + ", " + getDisplayClassName(n.getClass().getName()) + "] "); // NOI18N
    }
    
    private String getDisplayClassName(String cn) {
        int amp = cn.indexOf('$'); // NOI18N
        return cn.substring(amp + 1);
    }

    private String getAttributes(Attributes attrs) {
        if (attrs == null) {
            return ""; // NOI18N
        }

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < attrs.getLength(); i++) {
            buffer.append(" "); // NOI18N
            buffer.append(attrs.getQName(i));
            buffer.append("=\""); // NOI18N
            buffer.append(attrs.getValue(i));
            buffer.append("\""); // NOI18N
        }
        return buffer.toString();
    }

    private void printString(String str) {
        printIndent();
        buf.append(str);
    }

    private void printString(String prefix, String str, String suffix) {
        printIndent();
        if (str != null) {
            buf.append(prefix);
            buf.append(str);
            buf.append(suffix);
        } else {
            buf.append(prefix);
            buf.append(suffix);
        }
    }

    private void printAttributes(String prefix, Attributes attrs,
                                 String suffix) {
        printString(prefix, getAttributes(attrs), suffix);
    }

    private void dumpBody(Node n) throws JspException {
        Node.Nodes page = n.getBody();
        if (page != null) {
            indent++;
            page.visit(this);
            indent--;
        }
    }

    @Override
    public void visit(Node.TagDirective n) throws JspException {
        visitCommon(n);
        printAttributes("<%@ tag", n.getAttributes(), "%>"); // NOI18N
    }

    @Override
    public void visit(Node.PageDirective n) throws JspException {
        visitCommon(n);
        printAttributes("<%@ page", n.getAttributes(), "%>"); // NOI18N
    }

    @Override
    public void visit(Node.TaglibDirective n) throws JspException {
        visitCommon(n);
        printAttributes("<%@ taglib", n.getAttributes(), "%>"); // NOI18N
    }

    @Override
    public void visit(Node.IncludeDirective n) throws JspException {
        visitCommon(n);
        printAttributes("<%@ include", n.getAttributes(), "%>"); // NOI18N
        dumpBody(n);
    }

    @Override
    public void visit(Node.Comment n) throws JspException {
        visitCommon(n);
        printString("<%--", n.getText(), "--%>"); // NOI18N
    }

    @Override
    public void visit(Node.Declaration n) throws JspException {
        visitCommon(n);
        printString("<%!", n.getText(), "%>"); // NOI18N
    }

    @Override
    public void visit(Node.Expression n) throws JspException {
        visitCommon(n);
        printString("<%=", n.getText(), "%>"); // NOI18N
    }

    @Override
    public void visit(Node.Scriptlet n) throws JspException {
        visitCommon(n);
        printString("<%", n.getText(), "%>"); // NOI18N
    }

    @Override
    public void visit(Node.IncludeAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:include", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:include>"); // NOI18N
    }

    @Override
    public void visit(Node.ForwardAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:forward", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:forward>"); // NOI18N
    }

    @Override
    public void visit(Node.GetProperty n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:getProperty", n.getAttributes(), "/>"); // NOI18N
    }

    @Override
    public void visit(Node.SetProperty n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:setProperty", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:setProperty>"); // NOI18N
    }

    @Override
    public void visit(Node.UseBean n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:useBean", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:useBean>"); // NOI18N
    }

    @Override
    public void visit(Node.PlugIn n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:plugin", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:plugin>"); // NOI18N
    }

    @Override
    public void visit(Node.ParamsAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:params", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:params>"); // NOI18N
    }

    @Override
    public void visit(Node.ParamAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:param", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:param>"); // NOI18N
    }

    @Override
    public void visit(Node.NamedAttribute n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:attribute", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:attribute>"); // NOI18N
    }

    @Override
    public void visit(Node.JspBody n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:body", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:body>"); // NOI18N
    }

    @Override
    public void visit(Node.ELExpression n) throws JspException {
        visitCommon(n);
        printString(n.getText());
    }

    @Override
    public void visit(Node.CustomTag n) throws JspException {
        visitCommon(n);
        printAttributes("<" + n.getQName(), n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</" + n.getQName() + ">"); // NOI18N
    }

    @Override
    public void visit(Node.UninterpretedTag n) throws JspException {
        visitCommon(n);
        String tag = n.getQName();
        printAttributes("<"+tag, n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</" + tag + ">"); // NOI18N
    }

    @Override
    public void visit(Node.InvokeAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:invoke", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:invoke>"); // NOI18N
    }

    @Override
    public void visit(Node.DoBodyAction n) throws JspException {
        visitCommon(n);
        printAttributes("<jsp:doBody", n.getAttributes(), ">"); // NOI18N
        dumpBody(n);
        printString("</jsp:doBody>"); // NOI18N
    }

    @Override
    public void visit(Node.TemplateText n) throws JspException {
        visitCommon(n);
        printString(new String(n.getText()));
    }

    private void printIndent() {
        for (int i = 0; i < indent; i++) {
            buf.append("  "); // NOI18N
        }
    }
    
    private String getString() {
        return buf.toString();
    }

    public static String dump(Node n) {
        try {
            DumpVisitor dv = new DumpVisitor();
            n.accept(dv);
            return dv.getString();
        } catch (JspException e) {
            LOGGER.log(Level.INFO, null, e);
            return e.getMessage();
	}
    }

    public static String dump(Node.Nodes page) {
        try {
            DumpVisitor dv = new DumpVisitor();
            page.visit(dv);
            return dv.getString();
        } catch (JspException e) {
            LOGGER.log(Level.INFO, null, e);
            return e.getMessage();
	}
    }
}
