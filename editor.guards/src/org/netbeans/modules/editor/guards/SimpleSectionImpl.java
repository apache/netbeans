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

package org.netbeans.modules.editor.guards;

import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

/**
 * Represents a simple guarded section.
 * It consists of one contiguous block.
 */
public final class SimpleSectionImpl extends GuardedSectionImpl {
    /** Text range of the guarded section. */
    private PositionBounds bounds;

    /**
     * Creates new section.
     * @param name Name of the new section.
     * @param bounds The range of the section.
     */
    SimpleSectionImpl(String name, PositionBounds bounds, GuardedSectionsImpl guards) {
        super(name, guards);
        this.bounds = bounds;
    }

    @Override
    public void setName(String name) throws PropertyVetoException {
        super.setName(name);
        setText(getText());
    }

    /**
     * Set the text of the section.
     * @param text the new text
     */
    public void setText(String text) {
        setText(bounds, text, true, new ContentGetter() {
            @Override public PositionBounds getContent(GuardedSectionImpl t) {
                return ((SimpleSectionImpl) t).bounds;
            }
        });
    }

    void markGuarded(StyledDocument doc) {
        markGuarded(doc, bounds, true);
    }

    /**
     * Unmarks the section as guarded.
     * @param doc The styled document where this section placed in.
     */
    void unmarkGuarded(StyledDocument doc) {
        markGuarded(doc, bounds, false);
    }

    public Position getCaretPosition() {
        return bounds.getBegin();
    }

    public String getText() {
        String text = ""; // NOI18N
        try {
            text = bounds.getText();
        } catch (BadLocationException ex) {
            // ignore
            Logger.getLogger("guards").log(Level.ALL, null, ex);
        }
        return text;
    }

    /*
    public String toString() {
      StringBuffer buf = new StringBuffer("SimpleSection:"+name); // NOI18N
      buf.append("\"");
      try {
        buf.append(bounds.getText());
      }
      catch (Exception e) {
        buf.append("EXCEPTION:"); // NOI18N
        buf.append(e.getMessage());
      }
      buf.append("\"");
      return buf.toString();
    }*/

    public Position getEndPosition() {
        return bounds.getEnd();
    }

    public boolean contains(Position pos, boolean allowHoles) {
        return bounds.getBegin().getOffset() <= pos.getOffset() &&
                bounds.getEnd().getOffset() >= pos.getOffset();
    }

    public Position getStartPosition() {
        return bounds.getBegin();
    }

    public void resolvePositions() throws BadLocationException {
        bounds.resolvePositions();
    }
}
