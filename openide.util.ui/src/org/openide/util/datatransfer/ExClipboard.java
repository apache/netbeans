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
package org.openide.util.datatransfer;

import java.awt.EventQueue;
import java.awt.datatransfer.*;

import javax.swing.event.EventListenerList;


// and holding a list of services built over data flavors. [???]

/** Extended clipboard that supports listeners that can be notified about
* changes of content. Also contains support for attaching content convertors.
*
* @author Jaroslav Tulach
*/
public abstract class ExClipboard extends Clipboard {
    /** listeners */
    private EventListenerList listeners = new EventListenerList();

    /** Make a new clipboard.
    * @param name name of the clipboard
    */
    public ExClipboard(String name) {
        super(name);
    }

    /** Add a listener to clipboard operations.
    * @param list the listener
    */
    public final void addClipboardListener(ClipboardListener list) {
        listeners.add(ClipboardListener.class, list);
    }

    /** Remove a listener to clipboard operations.
    * @param list the listener
    */
    public final void removeClipboardListener(ClipboardListener list) {
        listeners.remove(ClipboardListener.class, list);
    }

    /** Fires event about change of content in the clipboard.
    */
    protected final void fireClipboardChange() {
        Object[] l = listeners.getListenerList();
        ClipboardEvent ev = null;

        for (int i = l.length - 2; i >= 0; i -= 2) {
            ClipboardListener list = (ClipboardListener) l[i + 1];

            if (ev == null) {
                ev = new ClipboardEvent(this);
            }

            list.clipboardChanged(ev);
        }
    }

    /** Obtain a list of convertors assigned to
    * this clipboard.
    * @return the convertors
    */
    protected abstract Convertor[] getConvertors();

    /** Method that takes a transferable, applies all convertors,
    * and creates a new transferable using the abilities of the
    * convertors.
    * <P>
    * This method is used when the contents of the clipboard are changed and
    * also can be used by Drag &amp; Drop to process transferables between source
    * and target.
    * <p>
    * Note that it is possible for the results to vary according to order
    * of the convertors as specified by {@link #getConvertors}. For example,
    * the input transferable may contain flavor A, and there may be a convertor
    * from A to B, and one from B to C; flavor B will always be available, but
    * flavor C will only be available if the convertor list is in the order
    * that these were mentioned. Since the standard clipboard implementation
    * searches for convertors in lookup as well as manifests, ordering might be
    * specified between a set of layer-supplied convertors by means of folder
    * ordering attributes.
    *
    * @param t input transferable
    * @return new transferable
    */
    public Transferable convert(Transferable t) {
        Convertor[] convertors = getConvertors();

        for (int i = 0; i < convertors.length; i++) {
            if (t == null) {
                return null;
            }

            t = convertors[i].convert(t);
        }

        return t;
    }

    /** Notifies the transferable that it has been accepted by a drop.
    * Works only for ExTransferable, other types of transferables are
    * not notified.
    *
    * @param t transferable to notify its listeners
    * @param action which action has been performed
    */
    public static void transferableAccepted(Transferable t, int action) {
        if (t instanceof ExTransferable) {
            ((ExTransferable) t).fireAccepted(action);
        } else if (t.isDataFlavorSupported(ExTransferable.multiFlavor)) {
            try {
                MultiTransferObject mto = (MultiTransferObject) t.getTransferData(ExTransferable.multiFlavor);
                int cnt = mto.getCount();

                for (int i = 0; i < cnt; i++) {
                    transferableAccepted(mto.getTransferableAt(i), action);
                }
            } catch (Exception e) {
                // shouldn't occure
            }
        }
    }

    /** Notifies the transferable that it has been rejected by a drop.
    * Works only for ExTransferable, other types of transferables are
    * not notified.
    *
    * @param t transferable to notify its listeners
    */
    public static void transferableRejected(Transferable t) {
        if (t instanceof ExTransferable) {
            ((ExTransferable) t).fireRejected();
        } else if (t.isDataFlavorSupported(ExTransferable.multiFlavor)) {
            try {
                MultiTransferObject mto = (MultiTransferObject) t.getTransferData(ExTransferable.multiFlavor);
                int cnt = mto.getCount();

                for (int i = 0; i < cnt; i++) {
                    transferableRejected(mto.getTransferableAt(i));
                }
            } catch (Exception e) {
                // shouldn't occure
            }
        }
    }

    @Override
    public void setContents(Transferable contents, ClipboardOwner owner) {
        synchronized (this) {
            if (this.contents != null) {
                transferableOwnershipLost(this.contents);
            }

            final ClipboardOwner oldOwner = this.owner;
            final Transferable oldContents = this.contents;

            this.owner = owner;
            this.contents = contents;

            if (oldOwner != null && oldOwner != owner) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        oldOwner.lostOwnership(ExClipboard.this, oldContents);
                    }
                });
            }
        }
        FlavorEvent e = new FlavorEvent(this);
        fireClipboardChange();
        for (FlavorListener l : getFlavorListeners()) {
            l.flavorsChanged(e);
        }
    }

    /** Notifies the transferable that it has lost ownership in clipboard.
    * Works only for ExTransferable, other types of transferables are
    * not notified.
    *
    * @param t transferable to notify its listeners
    */
    public static void transferableOwnershipLost(Transferable t) {
        if (t instanceof ExTransferable) {
            ((ExTransferable) t).fireOwnershipLost();
        } else if (t.isDataFlavorSupported(ExTransferable.multiFlavor)) {
            try {
                MultiTransferObject mto = (MultiTransferObject) t.getTransferData(ExTransferable.multiFlavor);
                int cnt = mto.getCount();

                for (int i = 0; i < cnt; i++) {
                    transferableOwnershipLost(mto.getTransferableAt(i));
                }
            } catch (Exception e) {
                // shouldn't occure
            }
        }
    }

    /** Convertor that can convert the {@link Transferable contents} of a clipboard to
    * additional {@link DataFlavor flavors}.
    */
    public interface Convertor {
        /** Convert a given transferable to a new transferable,
        * generally one which adds new flavors based on the existing flavors.
        * The recommended usage is as follows:
        *
        * <br><code><pre>
        * public Transferable convert (final Transferable t) {
        *   if (! t.isDataFlavorSupported (fromFlavor)) return t;
        *   if (t.isDataFlavorSupported (toFlavor)) return t;
        *   ExTransferable et = ExTransferable.create (t);
        *   et.put (new ExTransferable.Single (toFlavor) {
        *     public Object getData () throws IOException, UnsupportedFlavorException {
        *       FromObject from = (FromObject) t.getTransferData (fromFlavor);
        *       ToObject to = translateFormats (from);
        *       return to;
        *     }
        *   });
        *   return et;
        * }
        * </pre></code>
        *
        * <br>Note that this does not perform the conversion until <code>toFlavor</code> is
        * actually requested, nor does it advertise <code>toFlavor</code> as being available
        * unless <code>fromFlavor</code> already was.
        *
        * <p>You may also want to convert some flavor to a node selection, in which case you should do:
        *
        * <br><code><pre>
        * public Transferable convert (final Transferable t) {
        *   if (! t.isDataFlavorSupported (DataFlavor.stringFlavor)) return t;
        *   if (NodeTransfer.findPaste (t) != null) return t;
        *   ExTransferable et = ExTransferable.create (t);
        *   et.put (NodeTransfer.createPaste (new NodeTransfer.Paste () {
        *     public PasteType[] types (Node target) {
        *       if (isSuitable (target)) {
        *         return new PasteType[] { new PasteType () {
        *           public Transferable paste () throws IOException {
        *             try {
        *               String s = (String) t.getTransferData (DataFlavor.stringFlavor);
        *               addNewSubnode (target, s);
        *             } catch (UnsupportedFlavorException ufe) {
        *               throw new IOException (ufe.toString ());
        *             }
        *             return t;
        *           }
        *         }};
        *       } else {
        *         return new PasteType[0];
        *       }
        *     }
        *   }));
        *   return et;
        * }
        * </pre></code>
        *
        * <p>Convertors should generally avoid removing flavors from the transferable,
        * or changing the data for an existing flavor.
        *
        * @param t the incoming basic transferable
        * @return a possible enhanced transferable
        */
        public Transferable convert(Transferable t);
    }
}
