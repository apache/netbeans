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

package org.netbeans.editor.ext;

import java.util.Arrays;
import java.util.ArrayList;
import org.netbeans.editor.Syntax;

/**
* Composition of several syntaxes together. There are several different
* situations in which this class can be used efficiently:
* 1) Syntax that wants to use some other syntax internally for
*   recognizing one or more tokens. Example is java analyzer that would
*   like to use html-syntax for detail parsing block comments.
*
* 2) Master syntax that will manage two or more slave syntaxes. Example is the
*   master syntax managing java-syntax and html-syntax. The result would
*   be the same like in the previous example but it's more independent.
*
* 3) Master syntax that handles switching of the two or more other syntaxes. Only one 
*   slave syntax is active at one time.
*
* 4) An aribitrary combination and nesting of the previous examples.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class MultiSyntax extends Syntax {

    /** Slave syntaxes that can be used for scanning. They can
    * be added by registerSyntax().
    */
    private SyntaxInfo slaveSyntaxChain;

    /** Last chain member of the slaveSyntaxChain */
    private SyntaxInfo slaveSyntaxChainEnd;

    /** Register a particular slave syntax. */
    protected void registerSyntax(Syntax slaveSyntax) {
        slaveSyntaxChainEnd = new SyntaxInfo(slaveSyntax, slaveSyntaxChainEnd);
        if (slaveSyntaxChain == null) {
            slaveSyntaxChain = slaveSyntaxChainEnd;
        }
    }

    /** Store state of this analyzer into given mark state. */
    public void storeState(StateInfo stateInfo) {
        super.storeState(stateInfo);
        ((MultiStateInfo)stateInfo).store(this);
    }

    public void loadInitState() {
        super.loadInitState();
        SyntaxInfo syntaxItem = slaveSyntaxChain;
        while (syntaxItem != null) {
            syntaxItem.syntax.loadInitState();
            syntaxItem = syntaxItem.next;
        }
    }

    public void load(StateInfo stateInfo, char buffer[], int offset, int len,
                     boolean lastBuffer, int stopPosition) {
        ((MultiStateInfo)stateInfo).load(this, buffer, offset, len, lastBuffer, stopPosition);
        super.load(stateInfo, buffer, offset, len, lastBuffer, stopPosition);
    }

    public StateInfo createStateInfo() {
        return new MultiStateInfo();
    }

    /** Compare state of this analyzer to given state info. The basic
    * implementation does the following:
    * 1. state info of the main syntax is compared
    * 2. if the result is EQUAL_STATE then go through all the registered slave syntaxes:
    *    a) get the info 
    */
    public int compareState(StateInfo stateInfo) {
        int diff = super.compareState(stateInfo);
        if (diff == EQUAL_STATE) {
            diff = ((MultiStateInfo)stateInfo).compare(this);
        }
        return diff;
    }


    /** Class that can contain any number of the additional state infos from
    * other syntaxes. The state infos stored are identified
    * by the their syntax classes.
    */
    public static class MultiStateInfo extends BaseStateInfo {

        private ChainItem stateInfoChain;

        /** Goes through all the syntaxes and inits them. If the multi-state-info has
        * valid state-info for the given syntax the state-info is used.
        * Otherwise the syntax is inited to the init state.
        */
        void load(MultiSyntax masterSyntax, char[] buffer, int offset, int len,
                  boolean lastBuffer, int stopPosition) {
            SyntaxInfo syntaxItem = masterSyntax.slaveSyntaxChain;
            while (syntaxItem != null) {
                StateInfo loadInfo = null;
                int masterOffsetDelta = 0;
                Syntax s = syntaxItem.syntax;
                if (syntaxItem.active) {
                    Class sc = s.getClass();
                    ChainItem item = stateInfoChain;
                    while (item != null) {
                        if (item.syntaxClass == sc && item.valid) {
                            loadInfo = item.stateInfo;
                            masterOffsetDelta = item.masterOffsetDelta;
                            break;
                        }
                        item = item.prev;
                    }
                }
                s.load(loadInfo, buffer, offset + masterOffsetDelta,
                       len - masterOffsetDelta, lastBuffer, stopPosition);
                syntaxItem = syntaxItem.next;
            }
        }

        void store(MultiSyntax masterSyntax) {
            // Invalidate all state-info chain items
            ChainItem item = stateInfoChain;
            while (item != null) {
                item.valid = false;
                item = item.prev;
            }

            // Go through active syntaxes and store their info and master-offset
            SyntaxInfo syntaxItem = masterSyntax.slaveSyntaxChain;
            while (syntaxItem != null) {
                if (syntaxItem.active) {
                    Syntax s = syntaxItem.syntax;
                    Class sc = s.getClass();
                    item = stateInfoChain;
                    while (item != null) {
                        if (item.syntaxClass == sc) { // found right item
                            break;
                        }
                        item = item.prev;
                    }
                    if (item == null) { // not found, add new
                        item = stateInfoChain = new ChainItem(s.createStateInfo(),
                                                              sc, stateInfoChain);
                    }
                    // Store the state and compute masterOffsetDelta
                    s.storeState(item.stateInfo);
                    item.masterOffsetDelta = s.getOffset() - masterSyntax.getOffset();
                    item.valid = true;
                }
                syntaxItem = syntaxItem.next;
            }
        }

        int compare(MultiSyntax masterSyntax) {
            int ret = Syntax.EQUAL_STATE;
            // Go through valid state-info chain items
            ChainItem item = stateInfoChain;
            while (item != null && ret == Syntax.EQUAL_STATE) {
                if (item.valid) {
                    Class sc = item.syntaxClass;
                    SyntaxInfo syntaxItem = masterSyntax.slaveSyntaxChain;
                    while (syntaxItem != null) {
                        if (syntaxItem.syntax.getClass() == sc) {
                            if (syntaxItem.active) {
                                ret = syntaxItem.syntax.compareState(item.stateInfo);
                            } else { // syntax not active but should be
                                ret = Syntax.DIFFERENT_STATE;
                            }
                            break;
                        }
                        syntaxItem = syntaxItem.next;
                    }
                }
                item = item.prev;
            }
            return ret;
        }

        static class ChainItem {

            /** Whether this item is valid. It can become invalid if the syntax that it represents
            * becomes inactive in this item.
            */
            boolean valid;

            /** State info of the particular slave syntax */
            StateInfo stateInfo;

            /* Delta of the offset variable of the slave syntax against the offset
            * variable of the master syntax.
            */
            int masterOffsetDelta;

            /** Class of the syntax this info is for */
            Class syntaxClass;


            /** Previous chain item in the list */
            ChainItem prev;

            ChainItem(StateInfo stateInfo, Class syntaxClass, ChainItem prev) {
                this.stateInfo = stateInfo;
                this.syntaxClass = syntaxClass;
                this.prev = prev;
            }

        }

    }


    /** Extended info about one slave syntax */
    static class SyntaxInfo {

        SyntaxInfo(Syntax syntax, SyntaxInfo prevChainEnd) {
            this.syntax = syntax;

            if (prevChainEnd != null) {
                prev = prevChainEnd;
                prevChainEnd.next = this;
            }
        }

        /** The slave syntax itself */
        Syntax syntax;

        /** Whether this syntax is actively scanning the text. There can be possibly more
        * syntaxes scanning the in a nested way.
        */
        boolean active;

        /** Next member in the chain */
        SyntaxInfo next;

        /** Previous member in the chain */
        SyntaxInfo prev;

    }

}
