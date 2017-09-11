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
        public void insertString (int offs, String str, AttributeSet a) throws BadLocationException {
            insOrRemoveOrRunnable (offs, str, a, 0, true, null);
        }

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

        protected void fireRemoveUpdate (javax.swing.event.DocumentEvent e) {
            super.fireRemoveUpdate(e);
            changes++;
        }

        protected void fireInsertUpdate (javax.swing.event.DocumentEvent e) {
            super.fireInsertUpdate(e);
            changes++;
        }

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
