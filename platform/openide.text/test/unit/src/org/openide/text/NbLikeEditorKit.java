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


package org.openide.text;

import java.beans.VetoableChangeListener;
import java.util.concurrent.Callable;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;

/**
 * Emulates the behaviour of NetBeans editor's kit with all its special
 * implementations.
 *
 * @author  Jaroslav Tulach
 */
class NbLikeEditorKit extends DefaultEditorKit implements Callable<Void> {
    @Override
    public javax.swing.text.Document createDefaultDocument() {
        return new Doc ();
    }

    class Doc extends PlainDocument
    implements NbDocument.WriteLockable, StyledDocument {
//    implements NbDocument.PositionBiasable, NbDocument.WriteLockable,
//    NbDocument.Printable, NbDocument.CustomEditor, NbDocument.CustomToolbar, NbDocument.Annotatable {

        public Doc() {
            super (new StringContent ());
            
            // mark yourself of supporting modificationListener
            putProperty ("supportsModificationListener", Boolean.TRUE); 
        }

        public void runAtomic (Runnable r) {
            try {
                runAtomicAsUser (r);
            } catch (BadLocationException ex) {
                // too bad, no modification allowed
            }
        }

        public void runAtomicAsUser (Runnable r) throws BadLocationException {
             insOrRemoveOrRunnable (-1, null, null, -1, false, r);
        }

        public javax.swing.text.Style getLogicalStyle(int p) {
            return null;
        }

        public javax.swing.text.Style getStyle(java.lang.String nm) {
            return null;
        }

        public javax.swing.text.Style addStyle(java.lang.String nm, javax.swing.text.Style parent) {
            return null;
        }

        public void setParagraphAttributes(int offset, int length, javax.swing.text.AttributeSet s, boolean replace) {
        }

        public void setCharacterAttributes(int offset, int length, javax.swing.text.AttributeSet s, boolean replace) {
        }

        public void removeStyle(java.lang.String nm) {
        }

        public java.awt.Font getFont(javax.swing.text.AttributeSet attr) {
            return null;
        }

        public java.awt.Color getBackground(javax.swing.text.AttributeSet attr) {
            return null;
        }

        public javax.swing.text.Element getCharacterElement(int pos) {
            return null;
        }

        public void setLogicalStyle(int pos, javax.swing.text.Style s) {
        }

        public java.awt.Color getForeground(javax.swing.text.AttributeSet attr) {
            return null;
        }

        private int changes;
        private boolean modifiable;
        @Override
        public void insertString (int offs, String str, AttributeSet a) throws BadLocationException {
            insOrRemoveOrRunnable (offs, str, a, 0, true, null);
        }

        @Override
        public void remove (int offs, int len) throws BadLocationException {
            insOrRemoveOrRunnable (offs, null, null, len, false, null);
        }
        
        
        private void insOrRemoveOrRunnable (int offset, String str, AttributeSet set, int len, boolean insert, Runnable run) 
        throws BadLocationException {
            // Current implementation does not require the document to fire VetoableChangeListener from runAtomic()
            // and since Doc allows DoucmentFilter attaching (PlainDocument extends AbstractDocument)
            // there is no need to do any extra things.
            if (run != null) {
                run.run();
            } else {
                if (insert) {
                    super.insertString(offset, str, set);
                } else {
                    super.remove(offset, len);
                }
            }
            return;
            
//            boolean alreadyInsideWrite = getCurrentWriter () == Thread.currentThread ();
//            if (alreadyInsideWrite) {
//                if (run != null) {
//                    run.run ();
//                } else {
//                    assertOffset (offset);
//                    if (!modifiable) {
//                        throw new BadLocationException("Document modification vetoed", offset);
//                    }
//                    if (insert) {
//                        super.insertString (offset, str, set);
//                    } else {
//                        super.remove(offset, len);
//                    }
//                }
//                return;
//            }
//            
//            Object o = getProperty ("modificationListener");
//            
//            if (run != null) {
//                boolean canBeModified = notifyModified(o); // Need to notify before acquiring write-lock (that will span the whole runnable)
//                writeLock ();
//                modifiable = canBeModified;
//                int prevChanges = changes;
//                try {
//                    run.run ();
//                } finally {
//                    writeUnlock ();
//                }
//                if (changes == prevChanges) { // No changes => property chane with Boolean.FALSE
//                    if (o instanceof VetoableChangeListener) {
//                        VetoableChangeListener l = (VetoableChangeListener)o;
//                        try {
//                            l.vetoableChange (new java.beans.PropertyChangeEvent (this, "modified", null, Boolean.FALSE));
//                        } catch (java.beans.PropertyVetoException ignore) {
//                        }
//                    }
//                }
//            } else {
//                assertOffset (offset);
//                modifiable = notifyModified (o);
//                try {
//                    if (!modifiable) {
//                        throw new BadLocationException("Document modification vetoed", offset);
//                    }
//                    if (insert) {
//                        super.insertString (offset, str, set);
//                    } else {
//                        super.remove(offset, len);
//                    }
//                } catch (BadLocationException ex) {
//                    if (o instanceof VetoableChangeListener) {
//                        VetoableChangeListener l = (VetoableChangeListener)o;
//                        try {
//                            l.vetoableChange (new java.beans.PropertyChangeEvent (this, "modified", null, Boolean.FALSE));
//                        } catch (java.beans.PropertyVetoException ignore) {
//                        }
//                    }
//                    throw ex;
//                }
//            }
        }
        
        private void assertOffset (int offset) throws BadLocationException {
            if (offset < 0) throw new BadLocationException ("", offset);
        }
        
        private boolean notifyModified (Object o) {
            boolean canBeModified = true;
            if (o instanceof VetoableChangeListener) {
                VetoableChangeListener l = (VetoableChangeListener)o;
                try {
                    l.vetoableChange (new java.beans.PropertyChangeEvent (this, "modified", null, Boolean.TRUE));
                } catch (java.beans.PropertyVetoException ex) {
                    canBeModified = false;
                }
            }
            return canBeModified;
        }

        @Override
        protected void fireRemoveUpdate (javax.swing.event.DocumentEvent e) {
            super.fireRemoveUpdate(e);
            changes++;
        }

        @Override
        protected void fireInsertUpdate (javax.swing.event.DocumentEvent e) {
            super.fireInsertUpdate(e);
            changes++;
        }

        @Override
        protected void fireChangedUpdate (javax.swing.event.DocumentEvent e) {
            super.fireChangedUpdate(e);
            changes++;
        }

    } // end of Doc

    Thread callThread;
    public Void call() throws Exception {
        callThread = Thread.currentThread();
        return null;
    }
}
