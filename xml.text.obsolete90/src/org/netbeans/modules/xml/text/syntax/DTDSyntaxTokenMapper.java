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
package org.netbeans.modules.xml.text.syntax;

import org.netbeans.editor.*;
import org.netbeans.modules.xml.text.syntax.javacc.lib.*;
import org.netbeans.modules.xml.text.syntax.javacc.DTDSyntaxConstants;

/**
 * Factory mappring jjID => TokenID.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class DTDSyntaxTokenMapper implements JJMapperInterface, JJConstants, DTDSyntaxConstants {


    /** Create token for particular ID.  */
    public JJTokenID createToken(int id) {

        switch(id) {

            case JJ_EOL:
                return DTDTokenContext.EOL;
                
            case JJ_EOF:
                throw new Error("guessToken() must be called for such case."); // NOI18N
                
            case JJ_ERR:
                return DTDTokenContext.ERROR;
                
            case DECL_START:
            case PI_START:
            case COND_END_IN_DEFAULT:
            case PI_CONTENT_START:
            case PI_END:
            case PI_CONTENT_END:
            case XML_DECL_END:
            case COND:
            case DECL_END:
            case ENTITY_END:
            case ELEMENT_END:
            case NOTATION_END:
            case COND_END:
            case ATTLIST_END:
            case SYMBOL_IN_ELEMENT:
                
                return DTDTokenContext.SYMBOL;
                
            case ERR_IN_PI:
            case ERR_IN_PI_CONTENT:
            case ERR_IN_DECL:
            case ERR_IN_COND:
            case ERR_IN_DEFAULT:
            case ERR_IN_COMMENT:
            case ERR_IN_CREF:
            case ERR_IN_CHREF:
                
                return DTDTokenContext.ERROR;
                
                
            case XML_TARGET:
            case PI_TARGET:
                
                return DTDTokenContext.TARGET;
                
            case ENTITY:
            case ATTLIST:
            case DOCTYPE:
            case ELEMENT:
            case NOTATION:
            case KW_IN_ELEMENT:
            case KW_IN_NOTATION:
            case KW_IN_COND:
            case KW_IN_ATTLIST:
            case KW_IN_XML_DECL:
            case KW_IN_ENTITY:
                
                return DTDTokenContext.KW;
                
                
            case PREF_START:
            case TEXT_IN_PREF:
            case PREF_END:
            case CREF_START:
            case CREF_END:
            case TEXT_IN_CREF:
            case CHREF_START:
            case CHREF_END:
            case TEXT_IN_CHREF:
                
                return DTDTokenContext.REF;
                
                
            case CHARS_START:
            case TEXT_IN_CHARS:
            case CHARS_END:
            case STRING_START:
            case TEXT_IN_STRING:
            case STRING_END:
                
                return DTDTokenContext.STRING;
                
            case COMMENT_START:
            case TEXT_IN_COMMENT:
            case COMMENT_END:
                
                return DTDTokenContext.COMMENT;
                
            default:
                return DTDTokenContext.PLAIN;
                                        
        }
        
    }
    
    /** @return token guessed for particular state.  */
    public final JJTokenID guessToken(String token,int state,boolean lastBuffer) {
        
        switch (state) {
            case IN_COMMENT:
                if (!("--".equals(token) || "-".equals(token))) { // NOI18N
                    return DTDTokenContext.COMMENT;
                } else {
                    return cannotGuess(lastBuffer);
                }
                
            case IN_CREF:
            case IN_PREF:
                return DTDTokenContext.REF;
                
            case IN_STRING:
            case IN_CHARS:
                return DTDTokenContext.STRING;
                
            default:
                return cannotGuess(lastBuffer);
        }
    }
    
    private JJTokenID cannotGuess(boolean lastBuffer) {
        return cannotGuess(lastBuffer, DTDTokenContext.PLAIN);
    }
    
    private JJTokenID cannotGuess(boolean lastBuffer, JJTokenID supposed) {
        if (lastBuffer) {
            return supposed;
        } else {
            //ask for next buffer
            return null;
        }
        
    }
    /** Called if  createToken(int id) return isError() token.
    * @return supposed token for particular id and state. 
    */
    public JJTokenID supposedToken(String token,int state,int id) {
        
        switch (state) {            
            case IN_COMMENT:
                return DTDTokenContext.COMMENT;
                
            default:
                return null;
        }
    }
    
}
