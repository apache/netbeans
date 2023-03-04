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
