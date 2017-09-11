/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import javax.swing.text.AttributeSet;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

/**
 *
 * @author Miloslav Metelka
 */
public class TestOffsetsHighlightsContainer extends AbstractHighlightsContainer {
    
    int[] offsetPairs = new int[0];
    
    public void setOffsetPairs(int[] offsetPairs) {
        this.offsetPairs = offsetPairs;
        fireHighlightsChange(0, Integer.MAX_VALUE);
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return new HiSeq();
    }

    private final class HiSeq implements HighlightsSequence {
        
        int index;

        @Override
        public boolean moveNext() {
            if (index < offsetPairs.length) {
                index += 2;
                return true;
            }
            return false;
        }

        @Override
        public int getStartOffset() {
            return offsetPairs[index - 2];
        }

        @Override
        public int getEndOffset() {
            return offsetPairs[index - 1];
        }

        @Override
        public AttributeSet getAttributes() {
            return ViewUpdatesTesting.FONT_ATTRS[0];
        }
        
    }
    
}
