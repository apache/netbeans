/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.caret;

import org.netbeans.spi.editor.caret.NavigationFilterBypass;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.openide.util.Exceptions;

/**
 * Implementation of the FilterBypass suitable for multi-caret environment.
 * Provides a special {@link Caret} implementatio, which works with the current
 * {@link CaretItem} being manipulated, so that if client calls {@link Caret#setDot}
 * or the like, the instruction will be executed and bypasses all NavigationFilters.
 * 
 * @author sdedic
 */
class FilterBypassImpl extends NavigationFilterBypass {
    private final CaretTransaction transaction;
    private final CaretInfo        item;
    private final Document         doc;
    private Caret                  itemCaret;
    private boolean                result;

    public FilterBypassImpl(CaretTransaction transaction, CaretInfo item, Document doc) {
        this.transaction = transaction;
        this.item = item;
        this.doc = doc;
    }
    
    @Override
    public CaretInfo getCaretItem() {
        return item;
    }

    @Override
    public EditorCaret getEditorCaret() {
        return transaction.getCaret();
    }

    @Override
    public Caret getCaret() {
        if (itemCaret == null) {
            itemCaret = new ItemCaret();
        }
        return itemCaret;
    }

    @Override
    public MoveCaretsOrigin getOrigin() {
        return transaction.getOrigin();
    }
    
    boolean getResult() {
        return result;
    }
    
    @Override
    public void setDot(final int dot, Position.Bias dotBias) {
        Position dotPos = createPosition(dot);
        result = transaction.setDotAndMark(item.getCaretItem(),
                dotPos, dotBias, dotPos, dotBias);
    }
    
    private Position createPosition(final int dot) {
        final Position[] p = new Position[1];
        
        doc.render(new Runnable() {
            public void run() {
                p[0] = createPosition0(dot);
            }
        });
        return p[0];
    }
    
    private Position createPosition0(int dot) {
        try {
            if (dot < 0) {
                return doc.createPosition(0);
            } else if (dot > doc.getLength()) {
                return doc.createPosition(doc.getLength());
            } else {
                return doc.createPosition(dot);
            }
        } catch (BadLocationException ex) {
            // should not happen, checked under doc lock
            Exceptions.printStackTrace(ex);
            return item.getDotPosition();
        }
    }

    @Override
    public void moveDot(int dot, Position.Bias dotBias) {
        result = transaction.setDotAndMark(item.getCaretItem(), 
                createPosition(dot), dotBias,
                item.getMarkPosition(), item.getMarkBias()
        );
    }
    
    /**
     * Custom caret implementation, which cleverly delegates to CaretItem
     * and transaction to carry out most of Caret API tasks against the current
     * CaretItem.
     * Unsupported operations throw an exception.
     */
    private class ItemCaret implements Caret {
        private void notPermitted() {
            throw new UnsupportedOperationException("Disallowed in NavigationFilter"); // NOI18N
        }
        @Override
        public void install(JTextComponent c) {
            notPermitted();
        }

        @Override
        public void deinstall(JTextComponent c) {
            notPermitted();
        }

        @Override
        public void paint(Graphics g) {
            notPermitted();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            transaction.getCaret().addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            transaction.getCaret().removeChangeListener(l);
        }

        @Override
        public boolean isVisible() {
            return transaction.getCaret().isVisible();
        }

        @Override
        public void setVisible(boolean v) {
            transaction.getCaret().setVisible(v);
        }

        @Override
        public boolean isSelectionVisible() {
            return transaction.getCaret().isSelectionVisible();
        }

        @Override
        public void setSelectionVisible(boolean v) {
            transaction.getCaret().setSelectionVisible(v);
        }

        @Override
        public void setMagicCaretPosition(Point p) {
            transaction.setMagicCaretPosition(item.getCaretItem(), p);
        }

        @Override
        public Point getMagicCaretPosition() {
            return item.getMagicCaretPosition();
        }

        @Override
        public void setBlinkRate(int rate) {
            transaction.getCaret().setBlinkRate(rate);
        }

        @Override
        public int getBlinkRate() {
            return transaction.getCaret().getBlinkRate();
        }

        @Override
        public int getDot() {
            return item.getDot();
        }

        @Override
        public int getMark() {
            return item.getMark();
        }

        @Override
        public void setDot(int dot) {
            FilterBypassImpl.this.setDot(dot, Position.Bias.Forward);
        }

        @Override
        public void moveDot(int dot) {
            FilterBypassImpl.this.moveDot(dot, Position.Bias.Forward);
        }
        
    }
}
