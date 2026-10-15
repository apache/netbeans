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

package org.netbeans.modules.web.core.syntax.deprecated;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

/**
* Syntax for JSP files. This is a MultiSyntax consisting of three slave syntaxes:
* content language syntax (for example HTMLSyntax), JspTagSyntax and scripting
* language syntax (for example JavaSyntax). The content language and scripting language
* syntaxes are completely pluggable, moreover, they can be changed dynamically by 
* setContentSyntax() and setScriptingSyntax() methods. The caller of these methods should 
* make sure that the whole document is recolored after calling these methods.
*
* @author Petr Jiricka
* @version 1.00
* @deprecated Use JSP Lexer instead
*/
@Deprecated
public class JspMultiSyntax extends Syntax {

    //<editor-fold defaultstate="collapsed" desc="class & instance members">
    
    // modes of nesting of languages
    public static final int MODE_HOST             = 1;
    public static final int MODE_HOST_JSPTAG      = 2;
    public static final int MODE_HOST_EL          = 5;
    public static final int MODE_HOST_JSPTAG_EL   = 6;
    public static final int MODE_HOST_JSPTAG_JAVA = 3;
    public static final int MODE_HOST_JAVA        = 4;
                                 
    // constants for result of operation of checking delimiters                               
    protected static final int DELIMCHECK_NO = -1;
    protected static final int DELIMCHECK_PART = -2;
  
    // states of this multisyntax
    private static final int ISI_LANGUAGE = 1; // one syntax is active and working
    // states for switching from the host language to JSP tag or Java or EL
    private static final int ISI_HOST_JSPTAG = 2; // just before <jsptag or similar (recognized by JspTagSyntax)
    private static final int ISI_HOST_JAVA = 3; // just before <% or similar, after such a delimiter Java block starts, host language
    private static final int ISI_HOST_JAVA_LT = 4; // as ISI_HOST_JAVA after <
    private static final int ISI_HOST_JAVA_LT_PC = 5; // as ISI_HOST_JAVA after <%
    private static final int ISI_HOST_JAVA_JUMP = 6; // after a Java delimiter in host language, now really switch
    private static final int ISI_HOST_EL = 17; // just before ${  , after such a delimiter EL block starts, host language
    private static final int ISI_HOST_EL_D = 18; // as ISI_HOST_EL after $
    private static final int ISI_HOST_EL_JUMP = 20; // after a EL delimiter ${ in host language, now really switch
    // states for switching from a JSP tag to Java or EL
    private static final int ISI_JSPTAG_JAVA = 7; // just before <% or similar, after such a delimiter Java block starts, JSPTAG language
    private static final int ISI_JSPTAG_JAVA_LT = 8; // as ISI_JSPTAG_JAVA after <
    private static final int ISI_JSPTAG_JAVA_LT_PC = 9; // as ISI_JSPTAG_JAVA after <%
    private static final int ISI_JSPTAG_JAVA_JUMP = 10; // after a Java delimiter in JSPTAG language, now really switch
    private static final int ISI_JSPTAG_EL = 21; // just before ${  , after such a delimiter EL block starts, JSPTAG language
    private static final int ISI_JSPTAG_EL_D = 22; // as ISI_JSPTAG_EL after $
    private static final int ISI_JSPTAG_EL_JUMP = 24; // after a EL delimiter ${ in JSPTAG language, now really switch
    // states for switching from Java to a JSP tag
    private static final int ISI_JAVA1_SWITCH = 11; // just before %> in Java (go to JSPTAG)
    private static final int ISI_JAVA1_PC = 12; // as ISI_JAVA1_SWITCH after %
    private static final int ISI_JAVA1_JUMP = 13; // after %> in Java, now really switch to JSPTAG
    // states for switching from Java to host
    private static final int ISI_JAVA2_SWITCH = 14; // just before %> in Java (go to host)
    private static final int ISI_JAVA2_PC = 15; // as ISI_JAVA2_SWITCH after %
    private static final int ISI_JAVA2_JUMP = 16; // after %> in Java, now really switch to host
    // states for switching from EL to a JSP tag
    private static final int ISI_EL1_SWITCH = 25; // just before } in EL (go to JSPTAG)
    private static final int ISI_EL1_JUMP = 26; // after } in EL, now really switch to JSPTAG
    // states for switching from EL to host
    private static final int ISI_EL2_SWITCH = 27; // just before } in EL (go to host)
    private static final int ISI_EL2_JUMP = 28; // after } in EL, now really switch to host
  
    // states of the automaton which looks for delimiters in the host language
    private static final int HOST_INIT = 1; // initial state - host language
    private static final int HOST_LT = 2; // after < - host language
    private static final int HOST_LT_PC = 3; // after <% - host language
    private static final int HOST_LT_BLANK = 4; // after < or </ and several blanks or \t - host language
    private static final int HOST_TAG = 5; // inside a tag, don't know whether html or JSP - host language
    private static final int HOST_LT_SLASH = 6; // after </ - host lanaguage
    private static final int HOST_BS = 7; // after \ (escapes $) - host language
    private static final int HOST_D = 8; // after $ - host language
  
    // states of the automaton which looks for delimiters in the JSP tag
    private static final int JSPTAG_INIT = 1; // initial state - JSP tag
    private static final int JSPTAG_LT = 2; // after < - JSP tag
    private static final int JSPTAG_LT_PC = 3; // after <% - JSP tag
    private static final int JSPTAG_BS = 7; // after \ (escapes $) - JSPTAG language
    private static final int JSPTAG_D = 8; // after $ - JSPTAG language
  
    // states of the automaton which looks for delimiters in Java
    private static final int JAVA_INIT = 1; // initial state - Java block
    private static final int JAVA_PC = 2; // after % - Java block
  
    // states of the automaton which looks for delimiters in EL
    private static final int EL_INIT = 1; // initial state - EL block
  
    //States of java scripting element type
    private static final int JAVA_SCRIPTLET = 1;
    private static final int JAVA_DECLARATION = 2;
    private static final int JAVA_EXPRESSION = 3;
       
    //state info defining contexts of java tokens (declaration/scriptlet/expression)
    private static int javaNestMode;
    
    protected int nestMode;
        
    protected Syntax hostSyntax;
    protected Syntax jspTagSyntax;
    protected Syntax elSyntax;
    protected Syntax javaSyntax;
  
    /** When returning from parseToken(), contains the state of the 'host' slave syntax at 'offset'. 
    *  Always a part of the StateInfo. */
    protected StateInfo hostStateInfo;
    /** When returning from parseToken(), contains the state of the 'jspTag' slave syntax at 'offset'.
    *  Always a part of the StateInfo. */
    protected StateInfo jspTagStateInfo;
    /** When returning from parseToken(), contains the state of the 'el' slave syntax at 'offset'.
    *  Always a part of the StateInfo. */
    protected StateInfo elStateInfo;
    /** When returning from parseToken(), contains the state of the 'java' slave syntax at 'offset'.
    *  Always a part of the StateInfo. */
    protected StateInfo javaStateInfo;
  
    // Contains the tokenLength returned by the first call of nextToken() on the slave syntax. May need to be
    // stored in the stateinfo if tokenOffset != offset.
    // If the first call of slave's nextToken() returned null, this variable will not be valid, and
    // the next call must update this variable with the correct value
    private int firstTokenLength;

    // Contains the tokenID returned by the first call of nextToken() on the slave syntax. May need to be
    // stored in the stateinfo if tokenOffset != offset.
    // If the first call of slave's nextToken() returned null, it will be reflected in this variable, and
    // the next call must update this variable with the correct value
    private TokenID firstTokenID;

    // One of the following stateInfos will be a part of the stateInfo, if we are returning null.
    // In such a case it will contain the state of the scanning syntax at 'tokenOffset'.
    private StateInfo helpHostStateInfo;
    private StateInfo helpJspTagStateInfo;
    private StateInfo helpELStateInfo;
    private StateInfo helpJavaStateInfo;
                         
    // These stateinfos hold the stateinfo after the first token returned by the scanning stave syntax.
    // Only when tokenOffset == offset, in the other case need to rescan the first token before returning.                       
    private StateInfo firstHostStateInfo;
    private StateInfo firstJspTagStateInfo;
    private StateInfo firstELStateInfo;
    private StateInfo firstJavaStateInfo;
  
    //</editor-fold>
    
    public JspMultiSyntax() {
        // create the JSP tag syntax
        jspTagSyntax = new JspTagSyntax();
        firstJspTagStateInfo = jspTagSyntax.createStateInfo();
        helpJspTagStateInfo  = jspTagSyntax.createStateInfo();
        // create the EL syntax
        elSyntax = new ELSyntax();
        firstELStateInfo = elSyntax.createStateInfo();
        helpELStateInfo  = elSyntax.createStateInfo();
        
    }

    public JspMultiSyntax(Syntax contentSyntax, Syntax scriptingSyntax) {
        this();
        setContentSyntax(contentSyntax);
        setScriptingSyntax(scriptingSyntax);
    }

    public void setContentSyntax(Syntax contentSyntax) {
        hostSyntax = contentSyntax;
        firstHostStateInfo = hostSyntax.createStateInfo();
        helpHostStateInfo = hostSyntax.createStateInfo();
    }

    public Syntax getContentSyntax() {
        return hostSyntax;
    }

    public void setScriptingSyntax(Syntax scriptingSyntax) {
        javaSyntax = scriptingSyntax;
        firstJavaStateInfo = javaSyntax.createStateInfo();
        helpJavaStateInfo = javaSyntax.createStateInfo();
    }

    public Syntax getScriptingSyntax() {
        return javaSyntax;
    }

    /** Parses the next token. Before entering this method the following assumptions hold: 
    *  <ul>
    *  <li>'Regular' stateinfos contain the state of all active languages at 'offset'. For inactive 
    *    languages they are <code>null</code>.</li>
    *  <li>If <code>tokenOffset != offset</code>, the scanning 'help' stateinfo contains the state of the scanning
    *    language at 'tokenOffset'.</li>
    *  <li>If <code>tokenOffset != offset</code>, firstTokenID contains the token returned by the first call of
    *    slave's nextToken() in the current token, may be null !</li>
    *  </ul>
    */
    @Override
    protected TokenID parseToken() {
        //<editor-fold defaultstate="collapsed" desc="setting states">
        if (state != ISI_LANGUAGE) {
            char actChar;
            while(offset < stopOffset) {
                actChar = buffer[offset];
                switch (state) {
                    case ISI_HOST_JSPTAG: // switch to JspTagSyntax
                        nestMode = MODE_HOST_JSPTAG;
                        state = ISI_LANGUAGE;
                        transferMasterToSlave(jspTagSyntax, null);
                        if (jspTagStateInfo == null) {
                            jspTagStateInfo = jspTagSyntax.createStateInfo();
                        }
                        jspTagSyntax.storeState(jspTagStateInfo);
                        break;

                    case ISI_HOST_JAVA: // switch from hostSyntax to JavaSyntax
                        switch (actChar) {
                            case '<':
                                state = ISI_HOST_JAVA_LT;
                                break;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;

                    case ISI_HOST_JAVA_LT: 
                        switch (actChar) {
                            case '%':
                                state = ISI_HOST_JAVA_LT_PC;
                                break;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;

                    case ISI_HOST_JAVA_LT_PC: 
                        switch (actChar) {
                            case '!': // declaration
                                javaNestMode = JAVA_DECLARATION;
                                state = ISI_HOST_JAVA_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                                return JspTagTokenContext.SYMBOL2;
                            case '=': // expression
                                javaNestMode = JAVA_EXPRESSION;
                                state = ISI_HOST_JAVA_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                                return JspTagTokenContext.SYMBOL2;
                            default: // assume this is a scriptlet
                                javaNestMode = JAVA_SCRIPTLET;
                                state = ISI_HOST_JAVA_JUMP;
                                tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                                return JspTagTokenContext.SYMBOL2;
                        } // switch (actChar)
                        // break; - not reached

                    case ISI_HOST_JAVA_JUMP: 
                        nestMode = MODE_HOST_JAVA;
                        state = ISI_LANGUAGE;
                        transferMasterToSlave(javaSyntax, null);
                        //javaSyntax.load(null, buffer, offset, stopOffset - offset, lastBuffer);
                        if (javaStateInfo == null) {
                            javaStateInfo = javaSyntax.createStateInfo();
                        }
                        javaSyntax.storeState(javaStateInfo);
                        break;

                    case ISI_HOST_EL: // switch from hostSyntax to ELSyntax
                        switch (actChar) {
                            case '$':
                            case '#':    
                                state = ISI_HOST_EL_D;
                                break;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;

                    case ISI_HOST_EL_D: 
                        switch (actChar) {
                            case '{': // EL expression
                                state = ISI_HOST_EL_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.elContextPath;
                                return ELTokenContext.EL_DELIM;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;

                    case ISI_HOST_EL_JUMP: 
                        nestMode = MODE_HOST_EL;
                        state = ISI_LANGUAGE;
                        transferMasterToSlave(elSyntax, null);
                        //elSyntax.load(null, buffer, offset, stopOffset - offset, lastBuffer);
                        if (elStateInfo == null) {
                            elStateInfo = elSyntax.createStateInfo();
                        }
                        elSyntax.storeState(elStateInfo);
                        break;

                    case ISI_JSPTAG_JAVA: // switch from JSP tag to JavaSyntax
                        switch (actChar) {
                            case '<':
                                state = ISI_JSPTAG_JAVA_LT;
                                break;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;

                    case ISI_JSPTAG_JAVA_LT: 
                        switch (actChar) {
                            case '%':
                                state = ISI_JSPTAG_JAVA_LT_PC;
                                break;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;

                    case ISI_JSPTAG_JAVA_LT_PC: 
                        switch (actChar) {
                            case '!': // declaration
                                javaNestMode = JAVA_DECLARATION;
                                state = ISI_JSPTAG_JAVA_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                                return JspTagTokenContext.SYMBOL2;
                            case '=': // expression
                                javaNestMode = JAVA_EXPRESSION;
                                state = ISI_JSPTAG_JAVA_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                                return JspTagTokenContext.SYMBOL2;
                            default: // assume this is a scriptlet
                                javaNestMode = JAVA_SCRIPTLET;
                                state = ISI_JSPTAG_JAVA_JUMP;
                                tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                                return JspTagTokenContext.SYMBOL2;
                        } // switch (actChar)
                        // break; - not reached

                    case ISI_JSPTAG_JAVA_JUMP: 
                        nestMode = MODE_HOST_JSPTAG_JAVA;
                        state = ISI_LANGUAGE;
                        transferMasterToSlave(javaSyntax, null);
                        if (javaStateInfo == null) {
                            javaStateInfo = javaSyntax.createStateInfo();
                        }
                        javaSyntax.storeState(javaStateInfo);
                        break;
            
                    case ISI_JSPTAG_EL: // switch from JspTagSyntax to ELSyntax
                        switch (actChar) {
                            case '$':
                            case '#':    
                                state = ISI_JSPTAG_EL_D;
                                break;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;

                    case ISI_JSPTAG_EL_D: 
                        switch (actChar) {
                            case '{': // EL expression
                                state = ISI_JSPTAG_EL_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.elContextPath;
                                return ELTokenContext.EL_DELIM;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;

                    case ISI_JSPTAG_EL_JUMP: 
                        nestMode = MODE_HOST_JSPTAG_EL;
                        state = ISI_LANGUAGE;
                        transferMasterToSlave(elSyntax, null);
                        //elSyntax.load(null, buffer, offset, stopOffset - offset, lastBuffer);
                        if (elStateInfo == null) {
                            elStateInfo = elSyntax.createStateInfo();
                        }
                        elSyntax.storeState(elStateInfo);
                        break;

                    // switching from Java back to JSPTAG
                    case ISI_JAVA1_SWITCH:
                        switch (actChar) {
                            case '%':
                                state = ISI_JAVA1_PC;
                                break;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;                  
            
                    case ISI_JAVA1_PC:
                        switch (actChar) {
                            case '>':
                                state = ISI_JAVA1_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                                return JspTagTokenContext.SYMBOL2;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        }
          
                    case ISI_JAVA1_JUMP:
                        nestMode = MODE_HOST_JSPTAG;
                        jspTagStateInfo.setPreScan(0);
                        state = ISI_LANGUAGE;
                        javaStateInfo = null;
                        break;
                                               
                    // switching from Java back to host                                     
                    case ISI_JAVA2_SWITCH:
                        switch (actChar) {
                            case '%':
                                state = ISI_JAVA2_PC;
                                break;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;                  
            
                    case ISI_JAVA2_PC:

                        switch (actChar) {
                            case '>':
                                state = ISI_JAVA2_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                                return JspTagTokenContext.SYMBOL2;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        }
          
                    case ISI_JAVA2_JUMP:
                        nestMode = MODE_HOST;
                        hostStateInfo.setPreScan(0);
                        state = ISI_LANGUAGE;
                        javaStateInfo = null;
                        break;

                    // switching from EL back to JSPTAG
                    case ISI_EL1_SWITCH:
                        switch (actChar) {
                            case '}':
                                state = ISI_EL1_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.elContextPath;
                                return ELTokenContext.EL_DELIM;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;                  
            
                    case ISI_EL1_JUMP:
                        nestMode = MODE_HOST_JSPTAG;
                        jspTagStateInfo.setPreScan(0);
                        state = ISI_LANGUAGE;
                        elStateInfo = null;
                        break;
                                               
                    // switching from EL back to host
                    case ISI_EL2_SWITCH:
                        switch (actChar) {
                            case '}':
                                state = ISI_EL2_JUMP;
                                offset++;
                                tokenContextPath = JspMultiTokenContext.elContextPath;
                                return ELTokenContext.EL_DELIM;
                            default:  
                                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad hostsyntax analyzer"));  // NOI18N
                        } // switch (actChar)
                        break;                  
            
                    case ISI_EL2_JUMP:
                        nestMode = MODE_HOST;
                        hostStateInfo.setPreScan(0);
                        state = ISI_LANGUAGE;
                        elStateInfo = null;
                        break;
                                               
                }

                if (state == ISI_LANGUAGE)
                    break;

                offset++;
            } // end of while(offset...)

            if (state != ISI_LANGUAGE) {
                /** At this stage there's no more text in the scanned buffer.
                * Scanner first checks whether this is completely the last
                * available buffer.
                */
                if (lastBuffer) {
                    switch(state) {
                        case ISI_HOST_JSPTAG:
                        case ISI_HOST_JAVA:
                        case ISI_HOST_JAVA_LT:
                        case ISI_HOST_JAVA_LT_PC:
                        case ISI_HOST_JAVA_JUMP:
                        case ISI_JSPTAG_JAVA:
                        case ISI_JSPTAG_JAVA_LT:
                        case ISI_JSPTAG_JAVA_LT_PC:
                        case ISI_JSPTAG_JAVA_JUMP:
                        case ISI_JAVA1_SWITCH:
                        case ISI_JAVA1_PC:
                        case ISI_JAVA1_JUMP:
                        case ISI_JAVA2_SWITCH:
                        case ISI_JAVA2_PC:
                        case ISI_JAVA2_JUMP:
                            tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                            return JspTagTokenContext.SYMBOL2;
                            
                        case ISI_HOST_EL:
                        case ISI_HOST_EL_D:
                        case ISI_HOST_EL_JUMP:
                        case ISI_JSPTAG_EL:
                        case ISI_JSPTAG_EL_D:
                        case ISI_JSPTAG_EL_JUMP:
                        case ISI_EL1_SWITCH:
                        case ISI_EL1_JUMP:
                        case ISI_EL2_SWITCH:
                        case ISI_EL2_JUMP:
                            tokenContextPath = JspMultiTokenContext.elContextPath;
                            return ELTokenContext.EL_DELIM;
                    } // switch (state)
                } // if lastBuffer
                return null;
            }  // if state != ISI_LANGUAGE - inner
        } // if state != ISI_LANGUAGE - outer
    
        //</editor-fold>
        
        // now state is ISI_LANGUAGE
        TokenID slaveTokenID = null;
        TokenID returnedTokenID;

        int slaveOffset;
        int canBe;
        boolean firstTokenNotRead = ((tokenOffset == offset) || (firstTokenID == null));
        boolean equalPositions = (tokenOffset == offset);

        switch (nestMode) {
            // BIG BRANCH - we are in the HOST mode
            //<editor-fold defaultstate="collapsed" desc="MODE_HOST & MODE_JSPTAG">
            case MODE_HOST: 
                if (hostStateInfo == null) {
                    hostStateInfo = hostSyntax.createStateInfo();
                    hostSyntax.reset();
                    hostSyntax.storeState(hostStateInfo);
                }
                    
/*if (debug) {
System.out.print("NOT EQUAL tokenOffset=" + tokenOffset + ", offset=" + offset + ", tokenPart='");   
for (int i = tokenOffset; i<offset;i++) System.out.print(buffer[i]);
System.out.println("', firstTokenID=" + firstTokenID + ", firstTokenLength=" + firstTokenLength);
System.out.println("hoststate " + hostStateInfo.getState() + ", prescan=" + hostStateInfo.getPreScan());
System.out.println("helpstate " + helpHostStateInfo.getState() + ", prescan=" + helpHostStateInfo.getPreScan());
}         */
        
//if (equalPositions && (hostStateInfo.getPreScan() != 0))
//new Exception("prescan should be 0 !!").printStackTrace();
//if (debug)
//System.out.println("html state at offset " + ((BaseStateInfo)hostStateInfo).toString(this));
                if (firstTokenNotRead) {
                    // the first step - parse the first token of the slave
                    transferMasterToSlave(hostSyntax, hostStateInfo);
                    returnedTokenID = hostSyntax.nextToken();
                    slaveTokenID = returnedTokenID;
                    tokenContextPath = JspMultiTokenContext.context.getContextPath(
                        hostSyntax.getTokenContextPath());
                    slaveOffset = hostSyntax.getOffset();
                    firstTokenID = slaveTokenID;
                    firstTokenLength = hostSyntax.getTokenLength();
                    if (slaveTokenID == null) {
                        offset = slaveOffset;
                        firstTokenLength = -1;
                        // need to property transfer states
                        if (equalPositions) {
                            helpHostStateInfo = hostStateInfo;
                            hostStateInfo = hostSyntax.createStateInfo();
                            hostSyntax.storeState(hostStateInfo);
                        }
                        else {
                            if (hostStateInfo == null) {
                                hostStateInfo = hostSyntax.createStateInfo();
                            }
                            hostSyntax.storeState(hostStateInfo);
                        }
//if (debug)
//System.out.println("returnuju (1) " + null + " at " + offset);            
                        return null;
                    }  
                    if (returnedTokenID.getNumericID() == HtmlTokenContext.BLOCK_COMMENT_ID && isXMLSyntax() )
                        canBe = DELIMCHECK_NO;
                    else{
                        // find out if the token could contain a starting symbol for JspTag or Java
                        canBe = canBeHostDelimiter(tokenOffset, slaveOffset, slaveOffset, false);
                    }
                    if (canBe == DELIMCHECK_NO) { // do not switch 
                        offset = slaveOffset;
                        if (hostStateInfo == null) {
                            hostStateInfo = hostSyntax.createStateInfo();
                        }
                        hostSyntax.storeState(hostStateInfo);
//if (debug)
//System.out.println("returnuju (2) " + slaveTokenID + " at " + offset);            
                        return slaveTokenID;
                    }
                    // store the state
                    hostSyntax.storeState(firstHostStateInfo);
//if (firstHostStateInfo == hostStateInfo)
//new Exception("stateinfo instance conflict").printStackTrace();
                }
                else { // first position read - offsets different and firstTokenID is a valid token
                    transferMasterToSlave(hostSyntax, hostStateInfo);
                    canBe = DELIMCHECK_PART;
                }

                // we have successfully read the first token, the following statements hold:
                // - canBe is not DELIMCHECK_NO
                // - firstTokenID and firstTokenLength are meaningful
                // - if (equalPositions) then firstHostStateInfo is meaningful
//if (firstTokenID == null) {
//new Exception("invalid firstTokenID !!!!!!!").printStackTrace();
//}  
                while (canBe == DELIMCHECK_PART) { // need another token
                    // now get the new token
                    returnedTokenID = hostSyntax.nextToken();
                    slaveTokenID = returnedTokenID;
                    tokenContextPath = JspMultiTokenContext.context.getContextPath(
                        hostSyntax.getTokenContextPath());
                    slaveOffset = hostSyntax.getOffset();

                    if ((slaveTokenID == null) && lastBuffer) {
                        // ask about the delimiter, but with lastPart=true
                        canBe = canBeHostDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, true);
                        if (canBe != DELIMCHECK_PART)
                            break;
                    }

                    if (slaveTokenID == null) {
                        if (lastBuffer) {
                            canBe = DELIMCHECK_NO;
                            break;
                        }
                        offset = slaveOffset;
                        if (equalPositions) {
                            helpHostStateInfo = hostStateInfo;
                            hostStateInfo = hostSyntax.createStateInfo();
                            hostSyntax.storeState(hostStateInfo);
                        }
                        else {
                            if (hostStateInfo == null) {
                                hostStateInfo = hostSyntax.createStateInfo();
                            }
                            hostSyntax.storeState(hostStateInfo);
                        }
//if (debug)
//System.out.println("returnuju (3) " + null + " at " + offset);            
                        return null;
                    }
                    canBe = canBeHostDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, false);
                }
        
                // now canBe is not DELIMCHECK_PART
                // now we have read possibly more tokens and know whether to switch or not
                if (canBe == DELIMCHECK_NO) { // do not switch 
                    offset = tokenOffset + firstTokenLength;
                    if (equalPositions) {
                        hostStateInfo = firstHostStateInfo;
                        firstHostStateInfo = hostSyntax.createStateInfo();
                    }
                    else {
//if (debug)
//System.out.println("= imagine - rescan called !!");          
//if (helpHostStateInfo.getPreScan() != 0)
//new Exception("help prescan should be 0 !!").printStackTrace();
                        // we need to rescan the first token to find out the state
                        // now helpHostStateInfo is useful
                        hostSyntax.load(helpHostStateInfo, buffer, tokenOffset, stopOffset - tokenOffset, lastBuffer, -1);
                        returnedTokenID = hostSyntax.nextToken();
//if (tokenOffset != hostSyntax.getTokenOffset())
//new Exception("starts of tokens do not match").printStackTrace();            
                        slaveTokenID = returnedTokenID;
                        tokenContextPath = JspMultiTokenContext.context.getContextPath(
                            hostSyntax.getTokenContextPath());
                        if (hostStateInfo == null) {
                            hostStateInfo = hostSyntax.createStateInfo();
                        }
                        hostSyntax.storeState(hostStateInfo);

                    }
//if (debug)
//System.out.println("returnuju (4) " + firstTokenID + " at " + offset);            

                    return firstTokenID;
                }
                else { // we found a delimiter
//if (canBe >= tokenOffset + firstTokenLength)        
//new Exception("value of canBe is invalid !!!!!!!").printStackTrace();
                    // now use the saved state
                    if (equalPositions) {
                        hostSyntax.load(hostStateInfo, buffer, tokenOffset, canBe - tokenOffset, true, -1);
                    }
                    else {
                        hostSyntax.load(helpHostStateInfo, buffer, tokenOffset, canBe - tokenOffset, true, -1);
                    }
                    returnedTokenID = hostSyntax.nextToken();
                    tokenContextPath = JspMultiTokenContext.context.getContextPath(
                                    hostSyntax.getTokenContextPath());
                    // we got the StateInfo, which is why we did all this
                    if (hostStateInfo == null) {
                        hostStateInfo = hostSyntax.createStateInfo();
                    }
//if (debug)
//System.out.println("html state before saving back " + ((BaseStateInfo)hostStateInfo).toString(this));
                    hostSyntax.storeState(hostStateInfo);
                    hostStateInfo.setPreScan(0);
//if (hostSyntax.getOffset() != canBe)
//new Exception("bad number of characters parsed !!!").printStackTrace();          
                    offset = canBe;
/*if (debug) {
System.out.println("switching from HOST to JSPTAG at offset " + offset + ", hostState " + ((BaseStateInfo)hostStateInfo).toString(this));
System.out.println("offset of the returned (a)" + hostSyntax.getOffset());          
System.out.println("found delimiter at " + offset);          
System.out.println("returnuju (5) " + firstTokenID + " at " + offset);            
}*/
                    return firstTokenID;
                }
                //break; //- not reached
        
        
            // BIG BRANCH - we are in the HOST_JSPTAG mode
            case MODE_HOST_JSPTAG:
                // check if the JSP tag hasn't finished on its own will
                if ((jspTagStateInfo != null) && (jspTagStateInfo.getState() == JspTagSyntax.ISA_END_JSP)) {
                    // give up control
                    jspTagStateInfo = null;
                    nestMode = MODE_HOST;
//if (debug) {
//System.out.println("switching back to HOST from JSPTAG at offset " + offset + ", hostState " + ((BaseStateInfo)hostStateInfo).toString(this));
//System.out.println("returnuju (6) " /*+ JspTagSyntax.TEXT + jspTagSyntaxInfo.tokenIDShift */+ " at " + offset);            
//}
                    tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                    return JspTagTokenContext.TEXT;
                }

                if (jspTagStateInfo == null) {
                    jspTagStateInfo = jspTagSyntax.createStateInfo();
                    jspTagSyntax.reset();
                    jspTagSyntax.storeState(jspTagStateInfo);
                }
                if (firstTokenNotRead) {
                    // the first step - parse the first token of the slave
                    transferMasterToSlave(jspTagSyntax, jspTagStateInfo);
                    returnedTokenID = jspTagSyntax.nextToken();
                    if (returnedTokenID == JspTagTokenContext.AFTER_UNEXPECTED_LT) {
                        // give up control
                        jspTagStateInfo = null;
                        nestMode = MODE_HOST;
                        hostStateInfo.setPreScan(0);
                        tokenContextPath = JspMultiTokenContext.jspTagContextPath;
//System.out.println("switch to host " + returnedTokenID + " at " + offset);            
                        return returnedTokenID;
                    }   
                    slaveTokenID = returnedTokenID;
                    tokenContextPath = JspMultiTokenContext.jspTagContextPath;
//if (debug)
//System.out.println("first JSPtoken returned '" + getToken(jspTagSyntax) + "' id " + slaveTokenID);
                    slaveOffset = jspTagSyntax.getOffset();
                    firstTokenID = slaveTokenID;
                    firstTokenLength = jspTagSyntax.getTokenLength();
                    if (slaveTokenID == null) {
                        offset = slaveOffset;
                        firstTokenLength = -1;
                        // need to properly transfer states
                        if (equalPositions) {
                            helpJspTagStateInfo = jspTagStateInfo;
                            jspTagStateInfo = jspTagSyntax.createStateInfo();
                            jspTagSyntax.storeState(jspTagStateInfo);
                        }
                        else {
                            if (jspTagStateInfo == null) {
                                jspTagStateInfo = jspTagSyntax.createStateInfo();
                            }
                            jspTagSyntax.storeState(jspTagStateInfo);
                        }
//if (debug)
//System.out.println("returnuju (7) " + null + " at " + offset);            
                        return null;
                    }  
                    // find out if the token could contain a starting symbol for Java
                    canBe = canBeJspTagDelimiter(tokenOffset, slaveOffset, slaveOffset, false, returnedTokenID == JspTagTokenContext.COMMENT);
                    if (canBe == DELIMCHECK_NO) { // do not switch 
                        offset = slaveOffset;
                        if (jspTagStateInfo == null) {
                            jspTagStateInfo = jspTagSyntax.createStateInfo();
                        }
                        jspTagSyntax.storeState(jspTagStateInfo);
//if (debug)
//System.out.println("returnuju (8) " + slaveTokenID + " at " + offset);            
                        return slaveTokenID;
                    }
                    // store the state
                    jspTagSyntax.storeState(firstJspTagStateInfo);
                }
                else { // first position read - offsets different and firstTokenID is a valid token
                    transferMasterToSlave(jspTagSyntax, jspTagStateInfo);
                    canBe = DELIMCHECK_PART;
                }

                // we have successfully read the first token, the following statements hold:
                // - canBe is not DELIMCHECK_NO
                // - firstTokenID and firstTokenLength are meaningful
                // - if (equalPositions) then firstJspTagStateInfo is meaningful
//if (firstTokenID == null) {
//new Exception("invalid firstTokenID !!!!!!!").printStackTrace();
//}  
                while (canBe == DELIMCHECK_PART) { // need another token
                    // now get the new token
                    returnedTokenID = jspTagSyntax.nextToken();
                    slaveTokenID = returnedTokenID;
                    tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                    slaveOffset = jspTagSyntax.getOffset();

                    if ((slaveTokenID == null) && lastBuffer) {
                        // ask about the delimiter, but with lastPart=true
                        canBe = canBeJspTagDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, true, returnedTokenID == JspTagTokenContext.COMMENT);
                        if (canBe != DELIMCHECK_PART)
                            break;
                    }

                    if (slaveTokenID == null) {
                        if (lastBuffer) {
                            canBe = DELIMCHECK_NO;
                            break;
                        }
                        offset = slaveOffset;
                        if (equalPositions) {
                            helpJspTagStateInfo = jspTagStateInfo;
                            jspTagStateInfo = jspTagSyntax.createStateInfo();
                            jspTagSyntax.storeState(jspTagStateInfo);
                        }
                        else {
                            if (jspTagStateInfo == null) {
                                jspTagStateInfo = jspTagSyntax.createStateInfo();
                            }
                            jspTagSyntax.storeState(jspTagStateInfo);
                        }
//if (debug)
//System.out.println("returnuju (9) " + null + " at " + offset);            
                        return null;
                    }
                    canBe = canBeJspTagDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, false, returnedTokenID == JspTagTokenContext.COMMENT);
                }
        
                // now canBe is not DELIMCHECK_PART
                // now we have read possibly more tokens and know whether to switch or not
                if (canBe == DELIMCHECK_NO) { // do not switch 
                    offset = tokenOffset + firstTokenLength;
                    if (equalPositions) {
                        jspTagStateInfo = firstJspTagStateInfo;
                        firstJspTagStateInfo = jspTagSyntax.createStateInfo();
                    }
                    else {
//if (debug)
//System.out.println("= imagine - rescan called !!");          
                        // we need to rescan the first token to find out the state
                        // now helpJspTagStateInfo is useful
                        jspTagSyntax.load(helpJspTagStateInfo, buffer, tokenOffset, stopOffset - tokenOffset, lastBuffer, -1);
                        returnedTokenID = jspTagSyntax.nextToken();
                        tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                        slaveTokenID = returnedTokenID;
                        if (jspTagStateInfo == null) {
                            jspTagStateInfo = jspTagSyntax.createStateInfo();
                        }
                        jspTagSyntax.storeState(jspTagStateInfo);
//if (slaveTokenID != firstTokenID)
//new Exception("token ID does not match !!!!!!!").printStackTrace();
//if (offset != jspTagSyntax.getOffset())
//new Exception("offset does not match !!!!!!!").printStackTrace();
                    }
//if (debug)
//System.out.println("returnuju (10) " + firstTokenID + " at " + offset);            
                    return firstTokenID;
                }
                else { // we found a delimiter
//if (canBe >= tokenOffset + firstTokenLength)        
//new Exception("value of canBe is invalid !!!!!!!").printStackTrace();
                    // now use the saved state
                    if (equalPositions) {
                        jspTagSyntax.load(jspTagStateInfo, buffer, tokenOffset, canBe - tokenOffset, true, -1);
                    }
                    else {
                        jspTagSyntax.load(helpJspTagStateInfo, buffer, tokenOffset, canBe - tokenOffset, true, -1);
                    }
                    returnedTokenID = jspTagSyntax.nextToken();
                    tokenContextPath = JspMultiTokenContext.jspTagContextPath;
                    // we got the StateInfo, which is why we did all this
                    if (jspTagStateInfo == null) {
                        jspTagStateInfo = jspTagSyntax.createStateInfo();
                    }
                    jspTagSyntax.storeState(jspTagStateInfo);
                    jspTagStateInfo.setPreScan(0);
//if (jspTagSyntax.getOffset() != canBe)
//new Exception("bad number of characters parsed !!!").printStackTrace();          
                    offset = canBe;
//if (debug) {
//System.out.println("offset of the returned (a)" + jspTagSyntax.getOffset());          
//System.out.println("found delimiter at " + offset);          
//System.out.println("returnuju (11) " + firstTokenID + " at " + offset);            
//}
                    return firstTokenID;
                }
                //break; //- not reached
        
                //</editor-fold>
            case MODE_HOST_JSPTAG_JAVA:
            case MODE_HOST_JAVA: 
                if (javaStateInfo == null) {
                    javaStateInfo = javaSyntax.createStateInfo();
                    javaSyntax.reset();
                    javaSyntax.storeState(javaStateInfo);
                }
                if (firstTokenNotRead) {
                    // the first step - parse the first token of the slave
                    transferMasterToSlave(javaSyntax, javaStateInfo);
                    returnedTokenID = javaSyntax.nextToken();
                    tokenContextPath = getJavaTokenContextPath();
                    slaveTokenID = returnedTokenID;
                    slaveOffset = javaSyntax.getOffset();
                    firstTokenID = slaveTokenID;
                    firstTokenLength = javaSyntax.getTokenLength();
                    if (slaveTokenID == null) {
                        offset = slaveOffset;
                        firstTokenLength = -1;
                        // need to property transfer states
                        if (equalPositions) {
                            helpJavaStateInfo = javaStateInfo;
                            javaStateInfo = javaSyntax.createStateInfo();
                            javaSyntax.storeState(javaStateInfo);
                        }
                        else {
                            if (javaStateInfo == null) {
                                javaStateInfo = javaSyntax.createStateInfo();
                            }
                            javaSyntax.storeState(javaStateInfo);
                        }
                        return null;
                    }  
                    // find out if the token could contain an ending symbol for a Java block
                    canBe = canBeJavaDelimiter(tokenOffset, slaveOffset, slaveOffset, false, nestMode);
                    if (canBe == DELIMCHECK_NO) { // do not switch 
                        offset = slaveOffset;
                        if (javaStateInfo == null) {
                            javaStateInfo = javaSyntax.createStateInfo();
                        }
                        javaSyntax.storeState(javaStateInfo);
                        return slaveTokenID;
                    }
                    // store the state
                    javaSyntax.storeState(firstJavaStateInfo);
                }
                else { // first position read - offsets different and firstTokenID is a valid token
                    transferMasterToSlave(javaSyntax, javaStateInfo);
                    canBe = DELIMCHECK_PART;
                }

                // we have successfully read the first token, the following statements hold:
                // - canBe is not DELIMCHECK_NO
                // - firstTokenID and firstTokenLength are meaningful
                // - if (equalPositions) then firstJavaStateInfo is meaningful
                while (canBe == DELIMCHECK_PART) { // need another token
                    // now get the new token
                    returnedTokenID = javaSyntax.nextToken();
                    slaveTokenID = returnedTokenID;
                    tokenContextPath = getJavaTokenContextPath();
                    slaveOffset = javaSyntax.getOffset();

                    if ((slaveTokenID == null) && lastBuffer) {
                        // ask about the delimiter, but with lastPart=true
                        canBe = canBeJavaDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, true, nestMode);
                        if (canBe != DELIMCHECK_PART)
                            break;
                    }

                    if (slaveTokenID == null) {
                        if (lastBuffer) {
                            canBe = DELIMCHECK_NO;
                            break;
                        }
                        offset = slaveOffset;
                        if (equalPositions) {
                            helpJavaStateInfo = javaStateInfo;
                            javaStateInfo = javaSyntax.createStateInfo();
                            javaSyntax.storeState(javaStateInfo);
                        }
                        else {
                            if (javaStateInfo == null) {
                                javaStateInfo = javaSyntax.createStateInfo();
                            }
                            javaSyntax.storeState(javaStateInfo);
                        }
                        return null;
                    }
                    canBe = canBeJavaDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, false, nestMode);
                }
        
                // now canBe is not DELIMCHECK_PART
                // now we have read possibly more tokens and know whether to switch or not
                if (canBe == DELIMCHECK_NO) { // do not switch 
                    offset = tokenOffset + firstTokenLength;
                    if (equalPositions) {
                        javaStateInfo = firstJavaStateInfo;
                        firstJavaStateInfo = javaSyntax.createStateInfo();
                    }
                    else {
                        // we need to rescan the first token to find out the state
                        // now helpJavaStateInfo is useful
                        javaSyntax.load(helpJavaStateInfo, buffer, tokenOffset, stopOffset - tokenOffset, lastBuffer, -1);
                        returnedTokenID = javaSyntax.nextToken();
                        slaveTokenID = returnedTokenID;
                        tokenContextPath = getJavaTokenContextPath();
                        if (javaStateInfo == null) {
                            javaStateInfo = javaSyntax.createStateInfo();
                        }
                        javaSyntax.storeState(javaStateInfo);
                    }
                    return firstTokenID;
                }
                else { // we found a delimiter
                    // now use the saved state
                    if (equalPositions) {
                        javaSyntax.load(javaStateInfo, buffer, tokenOffset, canBe - tokenOffset, true, -1);
                    }
                    else {
                        javaSyntax.load(helpJavaStateInfo, buffer, tokenOffset, canBe - tokenOffset, true, -1);
                    }
                    returnedTokenID = javaSyntax.nextToken();
                    tokenContextPath = getJavaTokenContextPath();
                    // we got the StateInfo, which is why we did all this
                    if (javaStateInfo == null) {
                        javaStateInfo = javaSyntax.createStateInfo();
                    }
                    javaSyntax.storeState(javaStateInfo);
                    javaStateInfo.setPreScan(0);
                    offset = canBe;
                    
                    return firstTokenID;
                }
                // break; //- not reached
                
                //<editor-fold defaultstate="collapsed" desc="MODE_HOST_EL">
            case MODE_HOST_JSPTAG_EL:
            case MODE_HOST_EL: 
                if (elStateInfo == null) {
                    elStateInfo = elSyntax.createStateInfo();
                    elSyntax.reset();
                    elSyntax.storeState(elStateInfo);
                }
                if (firstTokenNotRead) {
                    // the first step - parse the first token of the slave
                    transferMasterToSlave(elSyntax, elStateInfo);
                    returnedTokenID = elSyntax.nextToken();
                    tokenContextPath = JspMultiTokenContext.elContextPath;
                    slaveTokenID = returnedTokenID;
                    slaveOffset = elSyntax.getOffset();
                    firstTokenID = slaveTokenID;
                    firstTokenLength = elSyntax.getTokenLength();
                    if (slaveTokenID == null) {
                        offset = slaveOffset;
                        firstTokenLength = -1;
                        // need to property transfer states
                        if (equalPositions) {
                            helpELStateInfo = elStateInfo;
                            elStateInfo = elSyntax.createStateInfo();
                            elSyntax.storeState(elStateInfo);
                        }
                        else {
                            if (elStateInfo == null) {
                                elStateInfo = elSyntax.createStateInfo();
                            }
                            elSyntax.storeState(elStateInfo);
                        }
//if (debug)
//System.out.println("returnuju (12.5) " + null + " at " + offset);            
                        return null;
                    }  
                    // find out if the token could contain an ending symbol for a EL block
                    canBe = canBeELDelimiter(tokenOffset, slaveOffset, slaveOffset, false, nestMode);
                    if (canBe == DELIMCHECK_NO) { // do not switch 
                        offset = slaveOffset;
                        if (elStateInfo == null) {
                            elStateInfo = elSyntax.createStateInfo();
                        }
                        elSyntax.storeState(elStateInfo);
//if (debug)
//System.out.println("returnuju (13.5) " + slaveTokenID + " at " + offset);            
                        return slaveTokenID;
                    }
                    // store the state
                    elSyntax.storeState(firstELStateInfo);
                }
                else { // first position read - offsets different and firstTokenID is a valid token
                    transferMasterToSlave(elSyntax, elStateInfo);
                    canBe = DELIMCHECK_PART;
                }

                // we have successfully read the first token, the following statements hold:
                // - canBe is not DELIMCHECK_NO
                // - firstTokenID and firstTokenLength are meaningful
                // - if (equalPositions) then firstELStateInfo is meaningful
//if (firstTokenID == null) {
//new Exception("invalid firstTokenID !!!!!!!").printStackTrace();
//}  
                while (canBe == DELIMCHECK_PART) { // need another token
                    // now get the new token
                    returnedTokenID = elSyntax.nextToken();
                    slaveTokenID = returnedTokenID;
                    tokenContextPath = JspMultiTokenContext.elContextPath;
                    slaveOffset = elSyntax.getOffset();

                    if ((slaveTokenID == null) && lastBuffer) {
                        // ask about the delimiter, but with lastPart=true
                        canBe = canBeELDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, true, nestMode);
                        if (canBe != DELIMCHECK_PART)
                            break;
                    }

                    if (slaveTokenID == null) {
                        if (lastBuffer) {
                            canBe = DELIMCHECK_NO;
                            break;
                        }
                        offset = slaveOffset;
                        if (equalPositions) {
                            helpELStateInfo = elStateInfo;
                            elStateInfo = elSyntax.createStateInfo();
                            elSyntax.storeState(elStateInfo);
                        }
                        else {
                            if (elStateInfo == null) {
                                elStateInfo = elSyntax.createStateInfo();
                            }
                            elSyntax.storeState(elStateInfo);
                        }
//if (debug)
//System.out.println("returnuju (14) " + null + " at " + offset);            
                        return null;
                    }
                    canBe = canBeELDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, false, nestMode);
                }
        
                // now canBe is not DELIMCHECK_PART
                // now we have read possibly more tokens and know whether to switch or not
                if (canBe == DELIMCHECK_NO) { // do not switch 
                    offset = tokenOffset + firstTokenLength;
                    if (equalPositions) {
                        elStateInfo = firstELStateInfo;
                        firstELStateInfo = elSyntax.createStateInfo();
                    }
                    else {
//if (debug)
//System.out.println("= imagine - rescan called !!");          
                        // we need to rescan the first token to find out the state
                        // now helpELStateInfo is useful
                        elSyntax.load(helpELStateInfo, buffer, tokenOffset, stopOffset - tokenOffset, lastBuffer, -1);
                        returnedTokenID = elSyntax.nextToken();
                        slaveTokenID = returnedTokenID;
                        tokenContextPath = JspMultiTokenContext.elContextPath;
                        if (elStateInfo == null) {
                            elStateInfo = elSyntax.createStateInfo();
                        }
                        elSyntax.storeState(elStateInfo);
//if (slaveTokenID != firstTokenID)
//new Exception("token ID does not match !!!!!!!").printStackTrace();
//if (offset != elSyntax.getOffset())
//new Exception("offset does not match !!!!!!!").printStackTrace();
                    }
//if (debug)
//System.out.println("returnuju (15.5) " + firstTokenID + " at " + offset);            
                    return firstTokenID;
                }
                else { // we found a delimiter
//if (canBe >= tokenOffset + firstTokenLength)        
//new Exception("value of canBe is invalid !!!!!!!").printStackTrace();
                    // now use the saved state
                    if (equalPositions) {
                        elSyntax.load(elStateInfo, buffer, tokenOffset, canBe - tokenOffset, true, -1);
                    }
                    else {
                        elSyntax.load(helpELStateInfo, buffer, tokenOffset, canBe - tokenOffset, true, -1);
                    }
                    returnedTokenID = elSyntax.nextToken();
                    tokenContextPath = JspMultiTokenContext.elContextPath;
                    // we got the StateInfo, which is why we did all this
                    if (elStateInfo == null) {
                        elStateInfo = elSyntax.createStateInfo();
                    }
                    elSyntax.storeState(elStateInfo);
                    elStateInfo.setPreScan(0);
//if (elSyntax.getOffset() != canBe)
//new Exception("bad number of characters parsed !!!").printStackTrace();          
                    offset = canBe;
/*if (debug) {
System.out.println("offset of the returned (a)" + elSyntax.getOffset());          
System.out.println("found delimiter at " + offset);          
System.out.println("returnuju (16.5) " + firstTokenID + " at " + offset);            
}*/
                    return firstTokenID;
                }
                // break; //- not reached
                
                //</editor-fold>
            default:  
                Logger.getLogger("global").log(Level.INFO, null, new Exception("bad nestmode"));  // NOI18N
                tokenContextPath = JspMultiTokenContext.contextPath;
                return JspMultiTokenContext.ERROR; // !!! don't know what to return
        }
    }
        
    //<editor-fold defaultstate="collapsed" desc="help methods">
    
    private TokenContextPath getJavaTokenContextPath() {
        switch(javaNestMode) {
            case JAVA_DECLARATION:
                return JspMultiTokenContext.javaDeclarationContextPath;
            case JAVA_EXPRESSION:
                return JspMultiTokenContext.javaExpressionContextPath;
            case JAVA_SCRIPTLET:
            default:
                return JspMultiTokenContext.javaScriptletContextPath;
        }
    }
  
    /** Checks if the part of the buffer starting at tokenOffset and ending just before endOffset
    * contains a "delimiter" or could contain a starting part of a "delimiter", where
    * "delimiter" is a lexical structure which could start a JSP tag of a Java block in
    * the host language, i.e. <code>&lt;jsp:useBean</code> or <code>&lt;%=</code>.
    * @return <ul>
    *      <li><code>DELIMCHECK_NO</code> if the part of the buffer does not contain a delimiter or its part</li>
    *      <li><code>DELIMCHECK_PART</code> if the part of the buffer contains part of the delimiter</li>
    *      <li>index of the starting symbol of the delimiter if the part of the buffer contains the delimiter.
    *           In such a case variable <code>state</code> is set properly.</li>
    *         </ul>
    */
    protected int canBeHostDelimiter(int tokenOffset, int endOffset, int firstTokenEnd, boolean lastPart) {
        int offset = tokenOffset;
        char actChar;
                 
        int possibleBeginning = DELIMCHECK_NO;
        StringBuffer tagString = null;
        int delimState = HOST_INIT;
    
        while(offset < endOffset) {
            actChar = buffer[offset];

            switch (delimState) {
                case HOST_INIT:
                    switch (actChar) {
                        case '<':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            delimState = HOST_LT;
                            possibleBeginning = offset;
                            break;
                        case '$':
                        case '#':    
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            if (!isELIgnored(false)) {
                                delimState = HOST_D;
                                possibleBeginning = offset;
                            }
                            break;
                        case '\\':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            delimState = HOST_BS;
                            possibleBeginning = offset; // not really true, the delimiter can't start here
                            break;
                        default:
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            break;  
                    }
                    break;
          
                case HOST_LT:
                    if (Character.isLetter(actChar) || 
                        (actChar == '_')
                    ) { // possible tag begining
                        delimState = HOST_TAG;
                        tagString = new StringBuffer();
                        tagString.append(actChar);
                        break; // the switch statement
                    }
          
                    switch (actChar) {
                        case '\n':
                            delimState = HOST_INIT;
                            break;
                        case '%':
                            delimState = HOST_LT_PC;
                            break;
                        case '/':
                            delimState = HOST_LT_SLASH;
                            break;
                        case ' ':  
                        case '\t':  
                            delimState = HOST_LT_BLANK;
                            break;
                        default:
                            delimState = HOST_INIT;
                            offset--;
                            break;
                    }
                    break;
          
                case HOST_LT_SLASH:
                    if (Character.isLetter(actChar) ||
                        (actChar == '_')
                    ) { // possible tag begining
                        delimState = HOST_TAG;
                        tagString = new StringBuffer();
                        tagString.append(actChar);
                        break; // the switch statement
                    }
          
                    switch (actChar) {
                        case '\n':
                            delimState = HOST_INIT;
                            break;
                        case ' ':  
                        case '\t':  
                            delimState = HOST_LT_BLANK;
                            break;
                        default:
                            delimState = HOST_INIT;
                            offset--;
                            break;
                    }
                    break;
          
                case HOST_LT_BLANK:
                    if (Character.isLetter(actChar) ||
                        (actChar == '_') ||
                        (actChar == '/')
                    ) { // possible tag begining
                        delimState = HOST_TAG;
                        // we are checking whether this is a JSP tag, however even if it is, 
                        // JspTagSyntax will report any spaces between < and tagname as errors
                        tagString = new StringBuffer();
                        //if there is a space after </ let the text be parsed by JSP parser - it will report an error
                        if(actChar != '/') tagString.append(actChar);
                        break; // the switch statement
                    }
          
                    switch (actChar) {
                        case '\n':
                            delimState = HOST_INIT;
                            break;
                        case ' ':  
                        case '\t':  
                            break;
                        default:
                            delimState = HOST_INIT;
                            offset--;
                            break;
                    }
                    break;
          
                case HOST_TAG:
                    if (Character.isLetter(actChar) ||
                        Character.isDigit(actChar) ||
                        (actChar == ':') ||
                        (actChar == '-') ||
                        (actChar == '_')
                    ) { // the tag continues
                        tagString.append(actChar);
                        break; // the switch statement
                    }
          
                    switch (actChar) {
                        default:
                            if (isJspTag(tagString.toString())) {
                                state = ISI_HOST_JSPTAG;
                                return possibleBeginning;
                            }
                            else {
                                delimState = HOST_INIT;
                                offset--;
                                break;
                            }  
                    }
                    break;
          
                case HOST_LT_PC:
                    switch (actChar) { 
                        case '@': // directive
                        case '-': // JSP comment
                            state = ISI_HOST_JSPTAG;
                            return possibleBeginning;
                        case '!': // declaration
                            javaNestMode = JAVA_DECLARATION;
                            state = ISI_HOST_JAVA;
                            return possibleBeginning;
                            
                        case '=': // expression
                            javaNestMode = JAVA_EXPRESSION;
                            state = ISI_HOST_JAVA;
                            return possibleBeginning;
                        default: // scriptlet  
                            javaNestMode = JAVA_SCRIPTLET;
                            state = ISI_HOST_JAVA;
                            return possibleBeginning;
                    }
                    
                case HOST_D:
                    switch (actChar) {
                        case '\n':
                            delimState = HOST_INIT;
                            break;
                        case '{':
                            state = ISI_HOST_EL;
                            return possibleBeginning;
                        default:
                            delimState = HOST_INIT;
                            offset--;
                            break;
                    }
                    break;
          
                case HOST_BS:
                    switch (actChar) {
                        case '<':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            delimState = HOST_LT;
                            possibleBeginning = offset;
                            break;
                        case '\\':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            possibleBeginning = offset; // not really true, the delimiter can't start here
                            break;
                        default:
                            delimState = HOST_INIT;
                            break;  
                    }
                    break;
            }
      
            offset++;
        }
    
        if (lastPart) {
            switch (delimState) {
                case HOST_LT_PC:
                    state = ISI_HOST_JAVA;
                    return possibleBeginning;
                case HOST_TAG:
                    if (isJspTag(tagString.toString())) {
                        state = ISI_HOST_JSPTAG;
                        return possibleBeginning;
                    }
            }    
        }
    
        // we have reached the end of the scanned area
        switch (delimState) {
            case HOST_INIT: 
                return DELIMCHECK_NO;
            case HOST_LT:
            case HOST_LT_SLASH:
            case HOST_LT_PC:
            case HOST_LT_BLANK:
            case HOST_TAG:
            case HOST_D:
            case HOST_BS:
                return DELIMCHECK_PART;
            default:
                Logger.getLogger("global").log(Level.INFO, null, new Exception("invalid state"));  // NOI18N
                return DELIMCHECK_NO;
        }
    }
  
    /** Checks if the part of the buffer starting at tokenOffset and ending just before endOffset
    * contains a "delimiter" or could contain a starting part of a "delimiter", where
    * "delimiter" is a lexical structure which could start a Java block inside a JSP tag,
    * i.e. <code>&lt;%=</code>.
    * @return <ul>
    *      <li><code>DELIMCHECK_NO</code> if the part of the buffer does not contain a delimiter or its part</li>
    *      <li><code>DELIMCHECK_PART</code> if the part of the buffer contains part of the delimiter</li>
    *      <li>index of the starting symbol of the delimiter if the part of the buffer contains the delimiter.
    *           In such a case variable <code>state</code> is set properly.</li>
    *         </ul>
    */
    protected int canBeJspTagDelimiter(int tokenOffset, int endOffset, int firstTokenEnd, boolean lastPart, boolean isComment) {
        if (isComment)
            return DELIMCHECK_NO;
    
        int offset = tokenOffset;
        char actChar;
                 
        int possibleBeginning = DELIMCHECK_NO;
        int delimState = JSPTAG_INIT;
    
        while(offset < endOffset) {
            actChar = buffer[offset];
      
            switch (delimState) {
                case JSPTAG_INIT:
                    switch (actChar) {
                        case '<':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            delimState = JSPTAG_LT;
                            possibleBeginning = offset;
                            break;
                        case '$':
                        case '#':    
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            if (!isELIgnored(true)) {
                                delimState = JSPTAG_D;
                                possibleBeginning = offset;
                            }
                            break;
                        case '\\':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            delimState = JSPTAG_BS;
                            possibleBeginning = offset; // not really true, the delimiter can't start here
                            break;
                        default:
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            break;  
                    }
                    break;
          
                case JSPTAG_LT:
                    switch (actChar) {
                        case '\n':
                            delimState = JSPTAG_INIT;
                            break;
                        case '%':
                            delimState = JSPTAG_LT_PC;
                            break;
                        default:
                            delimState = JSPTAG_INIT;
                            offset--;
                            break;
                    }
                    break;
          
                case JSPTAG_LT_PC:
                    switch (actChar) { 
                        case '!': // declaration
                            javaNestMode = JAVA_DECLARATION;
                            state = ISI_JSPTAG_JAVA;
                            return possibleBeginning;
                            
                        case '=': // expression
                            javaNestMode = JAVA_EXPRESSION;
                            state = ISI_JSPTAG_JAVA;
                            return possibleBeginning;
                        case '@': // declaration
                        case '-': // comment
                            delimState = JSPTAG_INIT;
                            break;
                        default: // scriptlet  
                            javaNestMode = JAVA_SCRIPTLET;
                            state = ISI_JSPTAG_JAVA;
                            return possibleBeginning;
                    }
                    //break;
                    
                case JSPTAG_D:
                    switch (actChar) {
                        case '\n':
                            delimState = JSPTAG_INIT;
                            break;
                        case '{':
                            state = ISI_JSPTAG_EL;
                            return possibleBeginning;
                        default:
                            delimState = JSPTAG_INIT;
                            offset--;
                            break;
                    }
                    break;
          
                case JSPTAG_BS:
                    switch (actChar) {
                        case '<':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            delimState = JSPTAG_LT;
                            possibleBeginning = offset;
                            break;
                        case '\\':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            possibleBeginning = offset; // not really true, the delimiter can't start here
                            break;
                        default:
                            delimState = JSPTAG_INIT;
                            break;  
                    }
                    break;
            }
      
            offset++;
        }
    
        if (lastPart) {
            switch (delimState) {
                case JSPTAG_LT_PC:
                    state = ISI_JSPTAG_JAVA;
                    return possibleBeginning;
            }
        }
    
        // we have reached the end of the scanned area
        switch (delimState) {
            case JSPTAG_INIT: 
                return DELIMCHECK_NO;
            case JSPTAG_LT:
                return DELIMCHECK_PART;
            case JSPTAG_LT_PC:
                return DELIMCHECK_PART;
            case JSPTAG_D:
                return DELIMCHECK_PART;    
            case JSPTAG_BS:
                //this state (error???) happens when only one character '\'
                //is scanned by this method (tokenOffset = endOffset - 1) 
                return DELIMCHECK_NO; 
            default:
                Logger.getLogger("global").log(Level.INFO, null, new Exception("invalid state " + delimState));  // NOI18N
                return DELIMCHECK_NO;
        }
    }
  
    /** Checks if the part of the buffer starting at tokenOffset and ending just before endOffset
    * contains a "delimiter" or could contain a starting part of a "delimiter", where
    * "delimiter" is a lexical structure which could end a Java block,
    * i.e. <code>%&gt;</code>.
    * @return <ul>
    *      <li><code>DELIMCHECK_NO</code> if the part of the buffer does not contain a delimiter or its part</li>
    *      <li><code>DELIMCHECK_PART</code> if the part of the buffer contains part of the delimiter</li>
    *      <li>index of the starting symbol of the delimiter if the part of the buffer contains the delimiter.
    *           In such a case variable <code>state</code> is set properly.</li>
    *         </ul>
    */
    protected int canBeJavaDelimiter(int tokenOffset, int endOffset, int firstTokenEnd, boolean lastPart, int myNestMode) {
        int offset = tokenOffset;
        char actChar;
                 
        int possibleBeginning = DELIMCHECK_NO;
        int delimState = JAVA_INIT;
    
        while(offset < endOffset) {
            actChar = buffer[offset];
      
            switch (delimState) {
                case JAVA_INIT:
                    switch (actChar) {
                        case '%':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            delimState = JAVA_PC;
                            possibleBeginning = offset;
                            break;
                        default:  
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            break;
                    }
                    break;
          
                case JAVA_PC:
                    switch (actChar) {
                        case '>':
                            switch (myNestMode) {
                                case MODE_HOST_JSPTAG_JAVA:
                                    state = ISI_JAVA1_SWITCH;
                                    return possibleBeginning;
                                case MODE_HOST_JAVA:
                                    state = ISI_JAVA2_SWITCH;
                                    return possibleBeginning;
                            }
                            Logger.getLogger("global").log(Level.INFO, null, new Exception("bad nestMode"));  // NOI18N
                        case '%':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            delimState = JAVA_PC;
                            possibleBeginning = offset;
                            break;
                        default:
                            delimState = JAVA_INIT;
                            break;
                    }
                    break;
            }
      
            offset++;
        }
    
        // we have reached the end of the scanned area
        switch (delimState) {
            case JAVA_INIT: 
                return DELIMCHECK_NO;
            case JAVA_PC:
                return DELIMCHECK_PART;
            default:
                Logger.getLogger("global").log(Level.INFO, null, new Exception("invalid state"));  // NOI18N
                return DELIMCHECK_NO;
        }
    }
  
    /** Checks if the part of the buffer starting at tokenOffset and ending just before endOffset
    * contains a "delimiter" or could contain a starting part of a "delimiter", where
    * "delimiter" is a lexical structure which could end a EL block,
    * i.e. <code>}</code>.
    * @return <ul>
    *      <li><code>DELIMCHECK_NO</code> if the part of the buffer does not contain a delimiter or its part</li>
    *      <li><code>DELIMCHECK_PART</code> if the part of the buffer contains part of the delimiter</li>
    *      <li>index of the starting symbol of the delimiter if the part of the buffer contains the delimiter.
    *           In such a case variable <code>state</code> is set properly.</li>
    *         </ul>
    */
    protected int canBeELDelimiter(int tokenOffset, int endOffset, int firstTokenEnd, boolean lastPart, int myNestMode) {
        int offset = tokenOffset;
        char actChar;
                 
        int possibleBeginning = DELIMCHECK_NO;
        int delimState = EL_INIT;
    
        while(offset < endOffset) {
            actChar = buffer[offset];
      
            switch (delimState) {
                case EL_INIT: // the only state for now
                    switch (actChar) {
                        case '}':
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            possibleBeginning = offset;
                            switch (myNestMode) {
                                case MODE_HOST_JSPTAG_EL:
                                    state = ISI_EL1_SWITCH;
                                    return possibleBeginning;
                                case MODE_HOST_EL:
                                    state = ISI_EL2_SWITCH;
                                    return possibleBeginning;
                            }
                            Logger.getLogger("global").log(Level.INFO, null,
                                                           new Exception("bad nestMode"));  // NOI18N
                            break;
                        default:  
                            if (offset >= firstTokenEnd)
                                return DELIMCHECK_NO;
                            break;
                    }
                    break;
            }
      
            offset++;
        }
    
        // we have reached the end of the scanned area
        switch (delimState) {
            case EL_INIT: 
                return DELIMCHECK_NO;
            default:
                Logger.getLogger("global").log(Level.INFO, null, new Exception("invalid state"));  // NOI18N
                return DELIMCHECK_NO;
        }
    }
  
    /** Determines whether a given string is a JSP tag. */
    protected boolean isJspTag(String tagName) {
        boolean canBeJsp = tagName.startsWith("jsp:");  // NOI18N
        return canBeJsp;
    }
    
    /** Determines whether any EL expressions should be colored as expressions, 
     * or ignored. This class does not have all the context necessary to decide this,
     * so it just returns false (meaning that EL should be colored). Subclasses 
     * should override this to return the correct value per section  JSP.3.3.2
     * of the specification.
     * @param whether this expression is inside the JSP tag value, or just in template text
     * @return true if the expression should be ignored, false if it should be treated as an expression
     */
    protected boolean isELIgnored(boolean inJspTag) {
        return false;
    }

    /** Determines whether the page is in xml syntax or not.
     * This class does not have all the context necessary to decide this,
     * so it just returns false. Subclasses 
     * should override this to return the correct value.
     * @return true if the page is JSP Document, false if the page is in standart syntax
     */
    protected boolean isXMLSyntax(){
        return false;
    }
    
    private void transferMasterToSlave(Syntax slave, StateInfo stateInfo) {
        slave.load(stateInfo, buffer, offset, stopOffset - offset, lastBuffer, -1);
        //slave.setLastBuffer(lastBuffer);  // PENDING - maybe not necessary
        //slave.setStopOffset(stopOffset);  // PENDING - maybe not necessary
    }
  
    /** Store state of this analyzer into given mark state. */
    @Override
    public void storeState(StateInfo stateInfo) {
        super.storeState(stateInfo);
        JspStateInfo jspsi = (JspStateInfo)stateInfo;
        // nest mode
        jspsi.nestMode = nestMode;
        jspsi.javaNestMode = javaNestMode;
        
        // regular stateinfos
        if (hostStateInfo == null) {
            jspsi.hostStateInfo = null;
        }
        else {
            jspsi.hostStateInfo = hostSyntax.createStateInfo();
            hostSyntax.load(hostStateInfo, buffer, offset, 0, false, -1);
            hostSyntax.storeState(jspsi.hostStateInfo);
        }
        if (jspTagStateInfo == null) {
            jspsi.jspTagStateInfo = null;
        }
        else {
            jspsi.jspTagStateInfo = jspTagSyntax.createStateInfo();
            jspTagSyntax.load(jspTagStateInfo, buffer, offset, 0, false, -1);
            jspTagSyntax.storeState(jspsi.jspTagStateInfo);
        }
        if (elStateInfo == null) {
            jspsi.elStateInfo = null;
        }
        else {
            jspsi.elStateInfo = elSyntax.createStateInfo();
            elSyntax.load(elStateInfo, buffer, offset, 0, false, -1);
            elSyntax.storeState(jspsi.elStateInfo);
        }
        if (javaStateInfo == null) {
            jspsi.javaStateInfo = null;
        }
        else {
            jspsi.javaStateInfo = javaSyntax.createStateInfo();
            javaSyntax.load(javaStateInfo, buffer, offset, 0, false, -1);
            javaSyntax.storeState(jspsi.javaStateInfo);
        }
        // stateOfScanningAtInit, firstTokenID, firstTokenLength
        if (jspsi.isFirstTokenValid()) {
            jspsi.firstTokenID = firstTokenID;
            jspsi.firstTokenLength = firstTokenLength;
            switch (nestMode) {
                case MODE_HOST: 
                    jspsi.stateOfScanningAtInit = hostSyntax.createStateInfo();
                    hostSyntax.load(helpHostStateInfo, buffer, offset, 0, false, -1);
                    hostSyntax.storeState(jspsi.stateOfScanningAtInit);
                    break;
                case MODE_HOST_JSPTAG: 
                    jspsi.stateOfScanningAtInit = jspTagSyntax.createStateInfo();
                    jspTagSyntax.load(helpJspTagStateInfo, buffer, offset, 0, false, -1);
                    jspTagSyntax.storeState(jspsi.stateOfScanningAtInit);
                    break;
                case MODE_HOST_JSPTAG_EL: 
                case MODE_HOST_EL: 
                    jspsi.stateOfScanningAtInit = elSyntax.createStateInfo();
                    elSyntax.load(helpELStateInfo, buffer, offset, 0, false, -1);
                    elSyntax.storeState(jspsi.stateOfScanningAtInit);
                    break;
                case MODE_HOST_JSPTAG_JAVA: 
                case MODE_HOST_JAVA: 
                    jspsi.stateOfScanningAtInit = javaSyntax.createStateInfo();
                    javaSyntax.load(helpJavaStateInfo, buffer, offset, 0, false, -1);
                    javaSyntax.storeState(jspsi.stateOfScanningAtInit);
                    break;
            }
        }
        else {
            jspsi.stateOfScanningAtInit = null;
            jspsi.firstTokenID = null;
            jspsi.firstTokenLength = -1;
        }
    }

    @Override
    public void loadState(StateInfo stateInfo) {
        super.loadState(stateInfo);
        JspStateInfo jspsi = (JspStateInfo)stateInfo;
        nestMode = jspsi.nestMode;
        javaNestMode = jspsi.javaNestMode;
 
        // now all the slave states
        if (jspsi.hostStateInfo == null) {
            hostStateInfo = null;
        }
        else {
            hostSyntax.load(jspsi.hostStateInfo, buffer, offset, 0, false, -1);
            hostStateInfo = hostSyntax.createStateInfo();
            hostSyntax.storeState(hostStateInfo);
        }
        if (jspsi.jspTagStateInfo == null) {
            jspTagStateInfo = null;
        }
        else {
            jspTagSyntax.load(jspsi.jspTagStateInfo, buffer, offset, 0, false, -1);
            jspTagStateInfo = jspTagSyntax.createStateInfo();
            jspTagSyntax.storeState(jspTagStateInfo);
        }
        if (jspsi.elStateInfo == null) {
            elStateInfo = null;
        }
        else {
            elSyntax.load(jspsi.elStateInfo, buffer, offset, 0, false, -1);
            elStateInfo = elSyntax.createStateInfo();
            elSyntax.storeState(elStateInfo);
        }
        if (jspsi.javaStateInfo == null) {
            javaStateInfo = null;
        }
        else {
            javaSyntax.load(jspsi.javaStateInfo, buffer, offset, 0, false, -1);
            javaStateInfo = javaSyntax.createStateInfo();
            javaSyntax.storeState(javaStateInfo);
        }
        // stateOfScanningAtInit, firstTokenID, firstTokenLength
        if (jspsi.isFirstTokenValid()) {
            firstTokenID = jspsi.firstTokenID;
            firstTokenLength = jspsi.firstTokenLength;
            switch (jspsi.nestMode) {
                case MODE_HOST: 
                    hostSyntax.load(jspsi.stateOfScanningAtInit, buffer, offset, 0, false, -1);
                    hostSyntax.storeState(helpHostStateInfo);
                    break;
                case MODE_HOST_JSPTAG: 
                    jspTagSyntax.load(jspsi.stateOfScanningAtInit, buffer, offset, 0, false, -1);
                    jspTagSyntax.storeState(helpJspTagStateInfo);
                    break;
                case MODE_HOST_JSPTAG_EL: 
                case MODE_HOST_EL: 
                    elSyntax.load(jspsi.stateOfScanningAtInit, buffer, offset, 0, false, -1);
                    elSyntax.storeState(helpELStateInfo);
                    break;
                case MODE_HOST_JSPTAG_JAVA: 
                case MODE_HOST_JAVA: 
                    javaSyntax.load(jspsi.stateOfScanningAtInit, buffer, offset, 0, false, -1);
                    javaSyntax.storeState(helpJavaStateInfo);
                    break;
            }
        }
        else {
            firstTokenID = null;
            firstTokenLength = -1;
        }
    }

    @Override
    public void loadInitState() {
        super.loadInitState();
        nestMode = MODE_HOST;
        state = ISI_LANGUAGE;
        hostStateInfo = null;
        jspTagStateInfo = null;
        elStateInfo = null;
        javaStateInfo = null;
        firstTokenID = null;
        firstTokenLength = -1;
    }

    public void load(StateInfo stateInfo, char buffer[], int offset, int len, boolean lastBuffer) {
        JspStateInfo jspsi = (JspStateInfo)stateInfo;
        if (jspsi == null) {
            hostStateInfo = null;
            jspTagStateInfo = null;
            elStateInfo = null;
            javaStateInfo = null;
            firstTokenID = null;
            firstTokenLength = -1;
        }
        super.load(stateInfo, buffer, offset, len, lastBuffer, -1);
    }

    @Override
    public StateInfo createStateInfo() {
        return new JspStateInfo();
    }
  
    @Override
    public int compareState(StateInfo stateInfo) {
        if (super.compareState(stateInfo) == DIFFERENT_STATE)
            return DIFFERENT_STATE;
        JspStateInfo jspsi = (JspStateInfo)stateInfo;
        if (jspsi.nestMode != nestMode)
            return DIFFERENT_STATE;
        if (jspsi.isFirstTokenValid()) {
            if (jspsi.firstTokenID != firstTokenID)
                return DIFFERENT_STATE;
            if (jspsi.firstTokenLength != firstTokenLength)
                return DIFFERENT_STATE;  
        }
        if(jspsi.javaNestMode != javaNestMode) return DIFFERENT_STATE;
        int ret;
        switch (nestMode) {
            case MODE_HOST:
                // host
                transferMasterToSlave(hostSyntax, hostStateInfo);
                ret = hostSyntax.compareState(jspsi.hostStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                if (jspsi.isFirstTokenValid()) {
                    transferMasterToSlave(hostSyntax, helpHostStateInfo);
                    ret = hostSyntax.compareState(jspsi.stateOfScanningAtInit);
                    if (ret == DIFFERENT_STATE) return ret;
                }
                break;
            case MODE_HOST_JSPTAG:
                // host
                transferMasterToSlave(hostSyntax, hostStateInfo);
                ret = hostSyntax.compareState(jspsi.hostStateInfo);
                if (ret == Syntax.DIFFERENT_STATE) return ret;
                // jspTag
                transferMasterToSlave(jspTagSyntax, jspTagStateInfo);
                ret = jspTagSyntax.compareState(jspsi.jspTagStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                if (jspsi.isFirstTokenValid()) {
                    transferMasterToSlave(jspTagSyntax, helpJspTagStateInfo);
                    ret = jspTagSyntax.compareState(jspsi.stateOfScanningAtInit);
                    if (ret == DIFFERENT_STATE) return ret;
                }
                break;
            case MODE_HOST_JSPTAG_EL:
                // host
                transferMasterToSlave(hostSyntax, hostStateInfo);
                ret = hostSyntax.compareState(jspsi.hostStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                // jspTag
                transferMasterToSlave(jspTagSyntax, jspTagStateInfo);
                ret = jspTagSyntax.compareState(jspsi.jspTagStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                // el
                transferMasterToSlave(elSyntax, elStateInfo);
                ret = elSyntax.compareState(jspsi.elStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                if (jspsi.isFirstTokenValid()) {
                    transferMasterToSlave(elSyntax, helpELStateInfo);
                    ret = elSyntax.compareState(jspsi.stateOfScanningAtInit);
                    if (ret == DIFFERENT_STATE) return ret;
                }
                break;
            case MODE_HOST_EL:
                // host
                transferMasterToSlave(hostSyntax, hostStateInfo);
                ret = hostSyntax.compareState(jspsi.hostStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                // el
                transferMasterToSlave(elSyntax, elStateInfo);
                ret = elSyntax.compareState(jspsi.elStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                if (jspsi.isFirstTokenValid()) {
                    transferMasterToSlave(elSyntax, helpELStateInfo);
                    ret = elSyntax.compareState(jspsi.stateOfScanningAtInit);
                    if (ret == DIFFERENT_STATE) return ret;
                }
                break;
            case MODE_HOST_JSPTAG_JAVA:
                // host
                transferMasterToSlave(hostSyntax, hostStateInfo);
                ret = hostSyntax.compareState(jspsi.hostStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                // jspTag
                transferMasterToSlave(jspTagSyntax, jspTagStateInfo);
                ret = jspTagSyntax.compareState(jspsi.jspTagStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                // java
                transferMasterToSlave(javaSyntax, javaStateInfo);
                ret = javaSyntax.compareState(jspsi.javaStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                if (jspsi.isFirstTokenValid()) {
                    transferMasterToSlave(javaSyntax, helpJavaStateInfo);
                    ret = javaSyntax.compareState(jspsi.stateOfScanningAtInit);
                    if (ret == DIFFERENT_STATE) return ret;
                }
                break;
            case MODE_HOST_JAVA:
                // host
                transferMasterToSlave(hostSyntax, hostStateInfo);
                ret = hostSyntax.compareState(jspsi.hostStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                // java
                transferMasterToSlave(javaSyntax, javaStateInfo);
                ret = javaSyntax.compareState(jspsi.javaStateInfo);
                if (ret == DIFFERENT_STATE) return ret;
                if (jspsi.isFirstTokenValid()) {
                    transferMasterToSlave(javaSyntax, helpJavaStateInfo);
                    ret = javaSyntax.compareState(jspsi.stateOfScanningAtInit);
                    if (ret == DIFFERENT_STATE) return ret;
                }
                break;
        }
        return EQUAL_STATE;
    }
    
    public String getNestModeName (int nestMode){
        switch (nestMode){
            case MODE_HOST:
                return "MODE_HOST";  // NOI18N
            case MODE_HOST_JSPTAG:
                return "MODE_HOST_JSPTAG";  // NOI18N
            case MODE_HOST_EL:
                return "MODE_HOST_EL";  // NOI18N
            case MODE_HOST_JSPTAG_EL:
                return "MODE_HOST_JSPTAG_EL";  // NOI18N
            case MODE_HOST_JSPTAG_JAVA:
                return "MODE_HOST_JSPTAG_JAVA"; // NOI18N
            case MODE_HOST_JAVA:
                return "MODE_HOST_JAVA"; // NOI18N
            default:
                return "unknown nestMode " + nestMode; // NOI18N
        }
    }

    @Override
    public String getStateName(int stateNumber) {
        switch(stateNumber) {
            case ISI_LANGUAGE:
                 return "ISI_LANGUAGE";     // NOI18N
            case ISI_HOST_JSPTAG:
                return "ISI_HOST_JSPTAG"; // NOI18N
            case ISI_HOST_JAVA:
                return "ISI_HOST_JAVA"; // NOI18N
            case ISI_HOST_JAVA_LT:
                return "ISI_HOST_JAVA_LT"; // NOI18N
            case ISI_HOST_JAVA_LT_PC:
                return "ISI_HOST_JAVA_LT_PC"; // NOI18N
            case ISI_HOST_JAVA_JUMP:
                return "ISI_HOST_JAVA_JUMP"; // NOI18N
            case ISI_HOST_EL:
                return "ISI_HOST_EL"; // NOI18N
            case ISI_HOST_EL_D:
                return "ISI_HOST_EL_D"; // NOI18N
            case ISI_HOST_EL_JUMP:
                return "ISI_HOST_EL_JUMP"; // NOI18N
            case ISI_JSPTAG_JAVA:
                return "ISI_JSPTAG_JAVA"; // NOI18N
            case ISI_JSPTAG_JAVA_LT:
                return "ISI_JSPTAG_JAVA_LT"; // NOI18N
            case ISI_JSPTAG_JAVA_LT_PC:
                return "ISI_JSPTAG_JAVA_LT_PC"; // NOI18N
            case ISI_JSPTAG_JAVA_JUMP:
                return "ISI_JSPTAG_JAVA_JUMP"; // NOI18N
            case ISI_JSPTAG_EL:
                return "ISI_JSPTAG_EL"; // NOI18N
            case ISI_JSPTAG_EL_D:
                return "ISI_JSPTAG_EL_D"; // NOI18N
            case ISI_JSPTAG_EL_JUMP:
                return "ISI_JSPTAG_EL_JUMP"; // NOI18N
            case ISI_JAVA1_SWITCH:
                return "ISI_JAVA1_SWITCH"; // NOI18N
            case ISI_JAVA1_PC:
                return "ISI_JAVA1_PC"; // NOI18N
            case ISI_JAVA1_JUMP:
                return "ISI_JAVA1_JUMP"; // NOI18N
            case ISI_JAVA2_SWITCH:
                return "ISI_JAVA2_SWITCH"; // NOI18N
            case ISI_JAVA2_PC:
                return "ISI_JAVA2_PC"; // NOI18N
            case ISI_JAVA2_JUMP:
                return "ISI_JAVA2_JUMP"; // NOI18N
            case ISI_EL1_SWITCH:
                return "ISI_EL1_SWITCH"; // NOI18N
            case ISI_EL1_JUMP:
                return "ISI_EL1_JUMP"; // NOI18N
            case ISI_EL2_SWITCH:
                return "ISI_EL2_SWITCH"; // NOI18N
            case ISI_EL2_JUMP:
                return "ISI_EL2_JUMP"; // NOI18N
            default:
                return super.getStateName(stateNumber); // NOI18N
        }
        
        //</editor-fold>
        
    }

    
    public static class JspStateInfo extends BaseStateInfo {
    
        int nestMode;
        StateInfo hostStateInfo;
        StateInfo jspTagStateInfo;
        StateInfo elStateInfo;
        StateInfo javaStateInfo;

        /** State info for the scanning syntax at 'tokenOffset', if tokenOffset != offset (i.e. null was returned). */
        StateInfo stateOfScanningAtInit;

        //state info defining contexts of java tokens (declaration/scriptlet/expression)
        int javaNestMode;
        
        /** Token ID returned by the first call of the scanning slave's nextToken(), possibly null. */
        TokenID firstTokenID;
    
        /** Token length of the token returned by the first call of the scanning slave's nextToken(), possibly invalid. */
        int firstTokenLength;     
    
        public boolean isFirstTokenValid() {
            return (JspStateInfo.this.getPreScan() != 0);
        }
    
        @Override
        public String toString(Syntax s) {
            return "JspStateInfo state=" + getState() + ", prescan=" + JspStateInfo.this.getPreScan() + ", nestMode=" + nestMode +    // NOI18N
                ((JspStateInfo.this.getPreScan() == 0) ? "" : "\n  firstTokenID=" + firstTokenID + ", firstTokenLength=" + firstTokenLength) + // NOI18N
                "\n  hostStateInfo=" + (hostStateInfo == null ? "null" : ((BaseStateInfo)hostStateInfo).toString(s)) +  // NOI18N
                "\n  jspTagStateInfo=" + (jspTagStateInfo == null ? "null" : ((BaseStateInfo)jspTagStateInfo).toString(s)) +    // NOI18N
                "\n  elStateInfo=" + (elStateInfo == null ? "null" : ((BaseStateInfo)elStateInfo).toString(s)) +    // NOI18N
                "\n  javaStateInfo=" + (javaStateInfo == null ? "null" : ((BaseStateInfo)javaStateInfo).toString(s)) +  // NOI18N
                "\n  scanning Info=" + (stateOfScanningAtInit == null ? "null" : ((BaseStateInfo)stateOfScanningAtInit).toString(s));   // NOI18N
        }
    }

  
}
