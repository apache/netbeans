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
