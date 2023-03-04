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
