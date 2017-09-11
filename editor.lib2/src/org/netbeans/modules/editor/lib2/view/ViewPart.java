/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

/**
 * Part of a view (but may also be used as a container for full view).
 */
final class ViewPart {

    /**
     * Part view.
     */
    final EditorView view;
    
    /**
     * Width of the part view (or full view).
     */
    final float width;
    
    /**
     * Relative X of the part against start of whole child view (from which view splitting
     * was initiated).
     * This needs to be included in a 'pos' parameter of a possible breakView()
     * so that e.g. tab widths are properly computed.
     * If this container is used for a full view then this field is 0f.
     */
    final float xShift;
    
    /**
     * Index of view part among other parts (starting at 0).
     * Full view has index -1.
     */
    final int index;

    /**
     * Constructor for whole view.
     */
    ViewPart(EditorView view, float width) {
        this(view, width, 0f, -1);
    }

    /**
     * Constructor for view part.
     */
    ViewPart(EditorView part, float width, float xShift, int index) {
        assert (part != null) : "Null view"; // NOI18N
        this.view = part;
        this.width = width;
        this.xShift = xShift;
        this.index = index;
    }

    boolean isPart() {
        return (index != -1);
    }
    
    boolean isFirstPart() {
        return (index == 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("view=").append(view).append(", width=").append(width). // NOI18N
                append(", xShift=").append(xShift).append(", index=").append(index); // NOI18N
        return sb.toString();
    }

}
