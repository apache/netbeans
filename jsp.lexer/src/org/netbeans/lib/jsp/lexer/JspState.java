/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.lib.jsp.lexer;

/**
 * Holds JspLexer state.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JspState {

    //main internal lexer state
    private int lexerState;

    //secondary internal state for EL expressions in JSP
    //is it used to eliminate a number of lexer states when EL is found -
    //we have 8 states just in attribute value so I would have to copy the EL
    //recognition code eight-times.
    private int lexerStateBeforeEL;

    //the same for jsp scriptlets
    private int lexerStateBeforeScriptlet;

    //internal state signalling whether the lexer is in <jsp:scriptlet> tag
    private int lexerStateJspScriptlet;

    //internal state signalling deep of the curly bracket nested level
    private int lexerStateCurlyNestedLevel;

    public JspState(int lexerState, int lexerStateBeforeEL, int lexerStateBeforeScriptlet, int lexerStateJspScriptlet, int lexerStateCurlyNestedLevel) {
        this.lexerState = lexerState;
        this.lexerStateBeforeEL = lexerStateBeforeEL;
        this.lexerStateBeforeScriptlet = lexerStateBeforeScriptlet;
        this.lexerStateJspScriptlet = lexerStateJspScriptlet;
        this.lexerStateCurlyNestedLevel = lexerStateCurlyNestedLevel;
    }

    public int getLexerState() {
        return lexerState;
    }

    public int getLexerStateBeforeEL() {
        return lexerStateBeforeEL;
    }

    public int getLexerStateBeforeScriptlet() {
        return lexerStateBeforeScriptlet;
    }

    public int getLexerStateJspScriptlet() {
        return lexerStateJspScriptlet;
    }

    public int getLexerStateCurlyNestedLevel() {
        return lexerStateCurlyNestedLevel;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.lexerState;
        hash = 83 * hash + this.lexerStateBeforeEL;
        hash = 83 * hash + this.lexerStateBeforeScriptlet;
        hash = 83 * hash + this.lexerStateJspScriptlet;
        hash = 83 * hash + this.lexerStateCurlyNestedLevel;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JspState other = (JspState) obj;
        if (this.lexerState != other.lexerState) {
            return false;
        }
        if (this.lexerStateBeforeEL != other.lexerStateBeforeEL) {
            return false;
        }
        if (this.lexerStateBeforeScriptlet != other.lexerStateBeforeScriptlet) {
            return false;
        }
        if (this.lexerStateJspScriptlet != other.lexerStateJspScriptlet) {
            return false;
        }
        if (this.lexerStateCurlyNestedLevel != other.lexerStateCurlyNestedLevel) {
            return false;
        }
        return true;
    }
}
