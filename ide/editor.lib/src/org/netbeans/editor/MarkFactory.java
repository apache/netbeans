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

package org.netbeans.editor;

import javax.swing.text.Position;

/**
* Various marks are located here
*
* @author Miloslav Metelka
* @version 1.00
*/

public class MarkFactory {

    private MarkFactory() {
        // no instantiation
    }

    /** Syntax mark holds info about scan state of syntax scanner.
     * This helps in redraws because reparsing after insert/delete is done
     * only from nearest left syntax mark. Moreover rescaning is done only
     * until there are marks with different scan state. As soon as mark
     * is found with same parsing info as rescanning scanner has, parsing
     * ends.
     * @deprecated syntax marks are no longer used to hold lexer states.
     */
    @Deprecated
    public static class SyntaxMark extends Mark {

        /** Syntax mark state info */
        private Syntax.StateInfo stateInfo;

        private TokenItem tokenItem;

        /** Get state info of this mark */
        public Syntax.StateInfo getStateInfo() {
            return stateInfo;
        }

        public void updateStateInfo(Syntax syntax) {
            if (stateInfo == null) {
                stateInfo = syntax.createStateInfo();
            }
            syntax.storeState(stateInfo);
        }

        void setStateInfo(Syntax.StateInfo stateInfo) {
          this.stateInfo = stateInfo;
        }

        public TokenItem getTokenItem() {
            return tokenItem;
        }

        void setTokenItem(TokenItem tokenItem) {
            this.tokenItem = tokenItem;
        }

        /** When removal occurs */
        protected @Override void removeUpdateAction(int pos, int len) {
            try {
                remove();
            } catch (InvalidMarkException e) {
                // shouldn't happen
            }
        }

    }

    /** Mark that can have its position updated by where it's located */
    public static class ContextMark extends Mark {

        public ContextMark(boolean stayBOL) {
            this(false, stayBOL);
        }

        public ContextMark(boolean insertAfter, boolean stayBOL) {
            this(insertAfter ? Position.Bias.Backward : Position.Bias.Forward, stayBOL);
        }

        public ContextMark(Position.Bias bias, boolean stayBOL) {
            super(bias);
        }

    }

}
