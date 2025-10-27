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

package org.netbeans.modules.web.jsps.parserapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.JspException;

/**
 * This class defines internal representation for an EL Expression
 *
 * It currently only defines functions.  It can be expanded to define
 * all the components of an EL expression, if need to.
 */

public abstract class ELNode {

    public abstract void accept(Visitor v) throws JspException;

    /**
     * Child classes
     */


    /**
     * Represents an EL expression: anything in ${ and }.
     */
    public static class Root extends ELNode {

        private final ELNode.Nodes expr;

        public Root(ELNode.Nodes expr) {
            this.expr = expr;
        }

        @Override
        public void accept(Visitor v) throws JspException {
            v.visit(this);
        }

        public ELNode.Nodes getExpression() {
            return expr;
        }
    }

    /**
     * Represents text outside of EL expression.
     */
    public static class Text extends ELNode {

        private final String text;

        public Text(String text) {
            this.text = text;
        }

        @Override
	public void accept(Visitor v) throws JspException {
            v.visit(this);
	}

        public String getText() {
            return text;
        }
    }

    /**
     * Represents anything else EL expression, including function arguments etc
     */
    public static class ELText extends ELNode {

        private final String text;

        public ELText(String text) {
            this.text = text;
        }

        @Override
	public void accept(Visitor v) throws JspException {
            v.visit(this);
	}

        public String getText() {
            return text;
        }
    }

    /**
     * Represents a function
     * Currently only the prefix and function name, but not its arguments.
     */
    public static class Function extends ELNode {

        private final String prefix;
        private final String name;
        private String uri;
        private FunctionInfo functionInfo;
        private String methodName;
        private String[] parameters;

        @SuppressWarnings("unused")
        Function(String prefix, String name) {
            this.prefix = prefix;
            this.name = name;
        }

        @Override
        public void accept(Visitor v) throws JspException {
            v.visit(this);
        }

        public String getPrefix() {
            return prefix;
        }

        public String getName() {
            return name;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public void setFunctionInfo(FunctionInfo f) {
            this.functionInfo = f;
        }

        public FunctionInfo getFunctionInfo() {
            return functionInfo;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public String[] getParameters() {
            return parameters;
        }
    }

    /**
     * An ordered list of ELNode.
     */
    public static class Nodes {

        /* Name used for creating a map for the functions in this
        EL expression, for communication to Generator.
        */
        private String mapName = null;

        private final List<ELNode> list;

        public Nodes() {
            list = new ArrayList<>();
        }

        public void add(ELNode en) {
            list.add(en);
        }

        /**
        * Visit the nodes in the list with the supplied visitor
        * @param v The visitor used
        * @throws jakarta.servlet.jsp.JspException
        */
        public void visit(Visitor v) throws JspException {
            for (ELNode n: list) {
                n.accept(v);
            }
        }

        public Iterator<ELNode> iterator() {
            return list.iterator();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        /**
        * @return true if the expression contains a ${...}
        */
        public boolean containsEL() {
            Iterator<ELNode> iter = list.iterator();
            while (iter.hasNext()) {
                ELNode n = iter.next();
                if (n instanceof Root) {
                    return true;
                }
            }
            return false;
        }

        public void setMapName(String name) {
            this.mapName = name;
        }

        public String getMapName() {
            return mapName;
        }
    }

    public static class Visitor {

        public void visit(Root n) throws JspException {
            n.getExpression().visit(this);
        }

        public void visit(Function n) throws JspException {
        }

        public void visit(Text n) throws JspException {
        }

        public void visit(ELText n) throws JspException {
        }
    }
}

