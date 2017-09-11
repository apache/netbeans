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

package org.netbeans.modules.editor.lib2.view;

/**
 * Information about a single visual line in a wrapped paragraph.
 * 
 * @author Miloslav Metelka
 */

final class WrapLine {

    /**
     * Start view of this line that was obtained by breaking a view
     * at (firstViewIndex - 1). It may be null if this line starts at view boundary
     * with a view at viewIndex.
     */
    ViewPart startPart;

    /**
     * Ending view of this line that was obtained by breaking a view
     * at endViewIndex.
     * It may be null if the line ends at view boundary.
     */
    ViewPart endPart;

    /**
     * Index of a first view located at this line.
     * <br>
     * Logically if there's a non-null startPart then it comes from view
     * at (firstViewIndex - 1).
     */
    int firstViewIndex;

    /**
     * Index that follows last view located at this line.
     * <br>
     * It should be >= firstViewIndex.
     */
    int endViewIndex;
    
    WrapLine() {
    }

    boolean hasFullViews() {
        return firstViewIndex != endViewIndex;
    }

    /**
     * Get first view (or fragment) of this wrap line.
     *
     * @param pView paragraph view to which this wrap line belongs.
     * @return starting child view or fragment.
     */
    EditorView startView(ParagraphView pView) {
        return (startPart != null)
                ? startPart.view
                : ((firstViewIndex != endViewIndex)
                        ? pView.getEditorView(firstViewIndex)
                        : endPart.view);
    }
    
    float startPartWidth() {
        return (startPart != null) ? startPart.width : 0f;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("startPart=").append(startPart); // NOI18N
        sb.append(" [").append(firstViewIndex).append(",").append(endViewIndex).append("]"); // NOI18N
        sb.append(" endPart=").append(endPart); // NOI18N
        return sb.toString();
    }

}
