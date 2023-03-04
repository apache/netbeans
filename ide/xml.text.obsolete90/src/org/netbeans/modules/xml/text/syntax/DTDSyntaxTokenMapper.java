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
