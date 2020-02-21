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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.parser;

import org.netbeans.modules.cnd.antlr.BaseAST;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.openide.util.CharSequences;

/**
 * Fake AST managing type
 */
public class FakeAST extends BaseAST implements Serializable {
    private static final long serialVersionUID = -1975495157952844447L;
    
    private final static CharSequence[] tokenText = new CharSequence[CPPTokenTypes.CSM_END + 1];

    static {
        int flags = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        for (Field field : CPPTokenTypes.class.getDeclaredFields()) {
            if ((field.getModifiers() & flags) == flags &&
                    int.class.isAssignableFrom(field.getType())) {
                try {
                    int value = field.getInt(null);
                    String name = field.getName();
                    tokenText[value]=CharSequences.create(name);
                } catch (Exception ex) {
                    DiagnosticExceptoins.register(ex);
                }
            }
        }
    }
    
    int ttype = Token.INVALID_TYPE;
    
    String text = null;
    
    public FakeAST() {
    }
    
    /** Get the token type for this node */
    @Override
    public int getType() {
        return ttype;
    }
    
    @Override
    public void initialize(int t, String txt) {
        setType(t);
        setText(txt);
    }
    
    @Override
    public void initialize(AST t) {
        setText(t.getText());
        setType(t.getType());
    }
    
    @Override
    public void initialize(Token tok) {
        setText(tok.getText());
        setType(tok.getType());
    }
    
    /** Set the token type for this node */
    @Override
    public void setType(int ttype_) {
        ttype = ttype_;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text != null ? text : tokenText[getType()].toString();
    }

    public CharSequence getTextID() {
        return text != null ? text : tokenText[getType()].toString();
    }
}
