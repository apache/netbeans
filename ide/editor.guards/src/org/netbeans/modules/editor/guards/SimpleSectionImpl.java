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
