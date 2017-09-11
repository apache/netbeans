/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import java.awt.Shape;

/**
 * Get information about a particular paragraph view in view hierarchy
 * obtained by {@link LockedViewHierarchy#getParagraphViewDescriptor(int) }.
 *
 * @author mmetelka
 */
public final class ParagraphViewDescriptor {
    
    private final DocumentView docView;
    
    private final int pViewIndex;

    ParagraphViewDescriptor(DocumentView docView, int pViewIndex) {
        this.docView = docView;
        this.pViewIndex = pViewIndex;
    }
    
    /**
     * Get start offset of the paragraph view represented by this descriptor.
     * <br>
     * When a LockedViewHierarchy that provided this paragraph view descriptor
     * is unlocked then operation of this method is undefined.
     *
     * @return start offset of paragraph view.
     */
    public int getStartOffset() {
        return docView.getParagraphView(pViewIndex).getStartOffset();
    }

    /**
     * Get textual length of the paragraph view represented by this descriptor.
     * <br>
     * When a LockedViewHierarchy that provided this paragraph view descriptor
     * is unlocked then operation of this method is undefined.
     *
     * @return textual length paragraph view.
     */
    public int getLength() {
        return docView.getParagraphView(pViewIndex).getLength();
    }
    
    /**
     * Get visual allocation of the whole paragraph view (represented by this descriptor).
     * <br>
     * When a LockedViewHierarchy that provided this paragraph view descriptor
     * is unlocked then operation of this method is undefined.
     *
     * @return visual allocation of paragraph view.
     */
    public Shape getAllocation() {
        return docView.getChildAllocation(pViewIndex);
    }
    
    /**
     * Get ascent (useful for text rendering using a particular font)
     * of the paragraph view represented by this descriptor.
     * <br>
     * This method is useful when a tool (such as a side bar performing rendering of a line
     * number) wants to render a text that should vertically match the text
     * rendered by the paragraph view.
     * <br>
     * When a LockedViewHierarchy that provided this paragraph view descriptor
     * is unlocked then operation of this method is undefined.
     *
     * @return visual allocation of paragraph view.
     */
    public float getAscent() {
        return docView.op.getDefaultAscent(); // Currently the ascent is global
    }


}
