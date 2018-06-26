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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

        private ELNode.Nodes expr;

        public Root(ELNode.Nodes expr) {
            this.expr = expr;
        }

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

        Function(String prefix, String name) {
            this.prefix = prefix;
            this.name = name;
        }

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

        public void setParameters(String[] parameters) {
            this.parameters = parameters;
        }

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
        String mapName = null;
        private final List<ELNode> list;

        public Nodes() {
            list = new ArrayList<ELNode>();
        }

        public void add(ELNode en) {
            list.add(en);
        }

        /**
        * Visit the nodes in the list with the supplied visitor
        * @param v The visitor used
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
            return list.size() == 0;
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

