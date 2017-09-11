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
package org.netbeans.modules.xml.text.syntax.javacc.lib;

import java.io.*;

import org.netbeans.editor.*;

import org.netbeans.modules.xml.text.syntax.javacc.*;

/**
 * General purpose framework for javacc generated grammars.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class JJEditorSyntax extends Syntax implements JJConstants {

    /** debugging support. */
    private static final boolean DEBUG = false;
    private static PrintStream debug = null;
    private static boolean debugColoring = Boolean.getBoolean("netbeans.debug.editor.draw"); // NOI18N
    private static int dbgOffset;

    /** Instance of analyzer. */
    private final JJSyntaxInterface lexan;
    private final JJMapperInterface  mapper;
    private final StringParserInput pi;
    
    /** Creates new XMLEditorSyntax */
    public JJEditorSyntax(JJSyntaxInterface lexan,JJMapperInterface mapper, TokenContextPath context) {
        if (DEBUG) {
            try {
                debug = new PrintStream(new FileOutputStream("/home/pkuzel/tmp/jj1.log")); // NOI18N
            } catch (IOException ex) {
                debug = System.err;
            }
        }
        
        tokenContextPath = context;
        this.lexan = lexan;
        this.mapper = mapper;
        pi = new UCode_CharStream();
    }

    /** General implementation frame. */
    protected TokenID parseToken() {

        try {
            JJTokenID token;                

            //!!! jj HACK never stop in the middle of regexp
            //that change jj state
            if ((stopOffset-tokenOffset < 10) && !lastBuffer) {
                offset = stopOffset;            
                return null;
            } else {
                offset = tokenOffset;
            }

            pi.setBuffer(buffer, tokenOffset, stopOffset-tokenOffset);

            if (DEBUG) {
                if ( tokenOffset != dbgOffset ) {
                    dbgOffset = tokenOffset;
                    debug.println("Tokenize at [" + offset + "," + tokenOffset + "] state:" + state + ":\n" + // NOI18N
                        new String(buffer, tokenOffset, stopOffset-tokenOffset)
                    );
                }
            }

            /** Map syntax init state to javacc init state. */
            if (state == INIT) {
                lexan.init(pi);
            } else {
                lexan.init(pi, state);  //restore state
            }

            lexan.next();  // call jj analyzer bridge

            state = lexan.getState();        
            int id = lexan.getID();

            if (id != JJ_EOF) {           
                token = mapper.createToken(id);
                
                //!!! hack: if grammar does not recognize a char at input
                //let try recognize next one, otherwise StackOverFlowError                
                if (id == JJ_ERR && lexan.getLength() == 0) {
                    offset++;
                }
            } else {
                token = mapper.guessToken(lexan.getImage(), state, lastBuffer);
            }

            // move offset ahead
            if (token != null) {
                offset += lexan.getLength(); 

                // if it is kind of error token set supposed one
                if (token.isError()) {
                    supposedTokenID = mapper.supposedToken(lexan.getImage(), state, id);
                }
                
            } else {
                //lexan.getLength() may return invalid value at buffer boundary (EOF)
                offset = stopOffset;            
            }

            
            if (DEBUG)
                debug.println("Tokenized: " + lexan.getImage() + " as: "+ token + " offset:" +  offset); // NOI18N

            return token;
            
        } catch (Error err) {
            if (DEBUG) {
                err.printStackTrace(debug);
                return mapper.guessToken("", -999, true); // NOI18N
            } else {
                throw err;
            }
        } catch (RuntimeException ex) {
            if (DEBUG) {
                ex.printStackTrace(debug);
                return mapper.guessToken("", -999, true); // NOI18N
            } else {
                throw ex;
            }            
        }
    }

    /** Load valid mark state into the analyzer. Offsets
    * are already initialized when this method is called. This method
    * must get the state from the mark and set it to the analyzer. Then
    * it must decrease tokenOffset by the preScan stored in the mark state.
    * @param markState mark state to be loaded into syntax. It must be non-null value.
    */
    public void loadState(StateInfo stateInfo) {

        if (DEBUG) debug.println("Loading state ["+ offset + "," + tokenOffset + "]: " + stateInfo + "@" + stopOffset); // NOI18N
        
        super.loadState(stateInfo);
        
        JJStateInfo info = (JJStateInfo) stateInfo;
        lexan.setStateInfo(info.getSubStates());        
    }

    /** Store state of this analyzer into given mark state. */
    public void storeState(StateInfo stateInfo) {
        
//        Thread.dumpStack();
        super.storeState(stateInfo);        
        
        JJStateInfo info = (JJStateInfo) stateInfo;
        info.setSubStates(lexan.getStateInfo());
        
        if (DEBUG) debug.println("Storing state ["+ offset + "," + tokenOffset + "]: " + info + "@" + stopOffset); // NOI18N
        
    }

    /** compare state of this analyzed to given state info. */
    public int compareState(StateInfo state) {
        if (super.compareState(state) == Syntax.DIFFERENT_STATE)
            return Syntax.DIFFERENT_STATE;
        
        return ((JJStateInfo)state).compareSubStates(lexan.getStateInfo());
    }
    
    /** Create state info appropriate for particular analyzer */
    public StateInfo createStateInfo() {
        return new JJStateInfo();
    }

}
