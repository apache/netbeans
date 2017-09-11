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

package org.netbeans.modules.xml.xdm.nodes;

/**
 *
 * @author Ajit Bhate
 */
public class Token {
   
    Token(String val, TokenType type) {
	value = val;
	this.type = type;
    }
    
    public String getValue() {
	return value;
    }
    
    public TokenType getType() {
	return type;
    }
    
    @Override
    public int hashCode() {
	return getValue().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof Token)) return false;
	Token token = (Token)obj;
	return((token.getValue().equals(getValue())) && 
	       (token.getType().equals(getType())));
    }
    
    @Override
    public String toString() {
	return getType() + " '" + value + "'";
    }
    
    public static Token create(String value, TokenType type) {
	Token t = null;
        switch(type) {
	    case TOKEN_ATTR_EQUAL: {
		t = EQUALS_TOKEN;
		break;
	    }
            case TOKEN_ELEMENT_END_TAG: {
		t = value.length() == 1 ? CLOSE_ELEMENT:SELF_CLOSE_ELEMENT;
		break;
	    }
            case TOKEN_WHITESPACE: {
                if(value.equals(" ")){
                    t = WHITESPACE_TOKEN;
                    break;
                }
            }  
	    default: {
		t = new Token(value.intern(),type);
	    }
	}
	assert t != null;
	return t;
    }
    
    private static final Token EQUALS_TOKEN = 
	new Token("=", TokenType.TOKEN_ATTR_EQUAL); //NOI18N
    
    private static final Token WHITESPACE_TOKEN =
            new Token(" ", TokenType.TOKEN_WHITESPACE);
    
    private static final Token CLOSE_ELEMENT =
	new Token(">", TokenType.TOKEN_ELEMENT_END_TAG); //NOI18N
    
    private static final Token SELF_CLOSE_ELEMENT =
	new Token("/>", TokenType.TOKEN_ELEMENT_END_TAG); //NOI18N
    
    public static final Token CDATA_START = 
	new Token("<![CDATA[", TokenType.TOKEN_CDATA_VAL); //NOI18N
    
    public static final Token CDATA_END =
	new Token("]]>", TokenType.TOKEN_CDATA_VAL); //NOI18N
    
    public static final Token COMMENT_START = 
	new Token("<!--", TokenType.TOKEN_COMMENT); //NOI18N
    
    public static final Token COMMENT_END =
	new Token("-->", TokenType.TOKEN_COMMENT); //NOI18N
   
    private final String value;
    private final TokenType type;
    //private static Map<String, Token> tokenPool = new HashMap<String, Token>();
}
