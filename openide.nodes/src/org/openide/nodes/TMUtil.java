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

package org.openide.nodes;

import java.util.Hashtable;
import org.openide.util.Mutex;

/** Class that serves as interface to various parts of OpenAPIs that need
 * not be present.
 *
 * @author  Jaroslav Tulach
 */
abstract class TMUtil extends Object {
    /** variable that will contain the argument to a call and then a result.
     */
    private static final ThreadLocal<Object> TALK = new ThreadLocal<Object>();

    /** maps names of algorithms (that use the ARGUMENT and the RESULT)
     * and runnables that has to be executed to compute the algorithm
     * (String, Runnable) or (String, Exception)
     */
    private static Hashtable<String, Object> algorithms = new Hashtable<String, Object>(10);
    private static java.awt.Frame owner;

    /** Dynamically loads a class.
     * @param className name of the class
     * @return the class
     */
    private static Class loadClass(String className) throws Exception {
        ClassLoader loader = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);

        if (loader == null) {
            loader = NodeOperation.class.getClassLoader();
        }

        return Class.forName(className, true, loader);
    }

    /** Creates InstanceCookie, if available.
     * @param bean the object to create cookie for
     * @return Node.Cookie or null
     */
    static Node.Cookie createInstanceCookie(Object bean) {
        try {
            TALK.set(bean);

            return exec("Bean") ? (Node.Cookie) TALK.get() : null; // NOI18N
        } finally {
            TALK.set(null);
        }
    }

    /** Checks whether an object is instance of DialogDescriptor and if
     * so it used top manager to create its instance.
     * @param maybeDialogDescriptor an object
     * @return a dialog or null
     */
    static java.awt.Dialog createDialog(Object maybeDialogDescriptor) {
        try {
            TALK.set(maybeDialogDescriptor);

            return exec("Dial") ? (java.awt.Dialog) TALK.get() : null; // NOI18N
        } finally {
            TALK.set(null);
        }
    }

    /** Attaches a customizer to given node.
     * @param node the bean node
     * @param cust customizer to attach
     */
    static void attachCustomizer(Node node, java.beans.Customizer cust) {
        try {
            TALK.set(new Object[] { node, cust });
            exec("Cust"); // NOI18N
        } finally {
            TALK.set(null);
        }
    }

    /** Finds main window.
     * @return main window or null
     */
    static java.awt.Frame mainWindow() {
        try {
            if (exec("Win")) { // NOI18N

                return (java.awt.Frame) TALK.get();
            } else {
                // default owner for JDialog
                if (owner == null) {
                    owner = (java.awt.Frame) new javax.swing.JDialog().getOwner();
                }

                return owner;
            }
        } finally {
            TALK.set(null);
        }
    }

    /** Finds usable list cell renderer.
     */
    static javax.swing.ListCellRenderer findListCellRenderer() {
        try {
            if (exec("Rend")) { // NOI18N

                return (javax.swing.ListCellRenderer) TALK.get();
            } else {
                return new javax.swing.DefaultListCellRenderer();
            }
        } finally {
            TALK.set(null);
        }
    }

    /** Invoke an indexed customizer. */
    static void showIndexedCustomizer(Index idx) {
        try {
            TALK.set(idx);

            if (!exec("IndexC")) { // NOI18N

                // Fallback to simple method.
                final IndexedCustomizer ic = new IndexedCustomizer();
                ic.setObject(idx);
                ic.setImmediateReorder(false);
                Mutex.EVENT.readAccess(
                    new Mutex.Action<Void>() {
                        public Void run() {
                            ic.setVisible(true);

                            return null;
                        }
                    }
                );
            }
        } finally {
            TALK.set(null);
        }
    }

    /** Executes algorithm of given name.
     * @param name the name of algorithm
     * @return true iff successfule
     */
    private static boolean exec(String name) {
        Object obj = algorithms.get(name);

        if (obj == null) {
            try {
                Class c = Class.forName("org.openide.nodes.TMUtil$" + name); // NOI18N
                obj = c.newInstance();
            } catch (ClassNotFoundException ex) {
                obj = ex;
                NodeOp.exception(ex);
            } catch (InstantiationException ex) {
                // that is ok, we should not be able to create an
                // instance if some classes are missing
                obj = ex;
            } catch (IllegalAccessException ex) {
                obj = ex;
                NodeOp.exception(ex);
            } catch (NoClassDefFoundError ex) {
                // that is ok, some classes need not be found
                obj = ex;
            }

            algorithms.put(name, obj);
        }

        try {
            if (obj instanceof Runnable) {
                ((Runnable) obj).run();

                return true;
            }
        } catch (NoClassDefFoundError ex) {
            // in case of late linking the error can be thrown
            // just when the runnable is executed
            algorithms.put(name, ex);
        }

        return false;
    }

    /** Creates instance of InstanceCookie for given object.
     * ARGUMENT contains the bean to create instance for.
     */
    static final class Bean implements Runnable, org.openide.cookies.InstanceCookie {
        private Object bean;

        public void run() {
            Bean n = new Bean();
            n.bean = TALK.get();
            TALK.set(n);
        }

        public String instanceName() {
            return bean.getClass().getName();
        }

        public Class instanceClass() {
            return bean.getClass();
        }

        public Object instanceCreate() {
            return bean;
        }
    }

    /** Creates dialog from DialogDescriptor
     * ARGUMENT contains the descriptor.
     */
    static final class Dial implements Runnable {
        public void run() {
            Object obj = TALK.get();

            if (obj instanceof org.openide.DialogDescriptor) {
                TALK.set(org.openide.DialogDisplayer.getDefault().createDialog((org.openide.DialogDescriptor) obj));
            } else {
                TALK.set(null);
            }
        }
    }

    /** Attaches the node to a customizer if it implements NodeCustomizer.
     * ARGUMENT contains array of node and customizer
     */
    static final class Cust implements Runnable {
        private static Class<?> nodeCustomizer;
        private static java.lang.reflect.Method attach;

        public void run() {
            try {
                if (nodeCustomizer == null) {
                    // load method
                    nodeCustomizer = loadClass("org.openide.explorer.propertysheet.editors.NodeCustomizer"); // NOI18N
                    attach = nodeCustomizer.getMethod("attach", Node.class); // NOI18N
                }

                Object[] arr = (Object[]) TALK.get();

                Node n = (Node) arr[0];
                Object cust = arr[1];

                if (nodeCustomizer.isInstance(cust)) {
                    // ((org.openide.explorer.propertysheet.editors.NodeCustomizer)cust).attach (n);
                    attach.invoke(cust, new Object[] { n });
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage());
            }
        }
    }

    /** Finds the main window.
     */
    static final class Win implements Runnable {
        private static java.lang.reflect.Method getDefault;
        private static java.lang.reflect.Method getMainWindow;

        public void run() {
            try {
                if (getDefault == null) {
                    // load all methods
                    Class<?> wm = loadClass("org.openide.windows.WindowManager"); // NOI18N
                    getDefault = wm.getMethod("getDefault"); // NOI18N
                    getMainWindow = wm.getMethod("getMainWindow"); // NOI18N
                }

                //
                // call: WindowManager.getDefault().getMainWindow ()
                //
                Object[] param = new Object[0];
                TALK.set(getMainWindow.invoke(getDefault.invoke(null, param), param));
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage());
            }
        }
    }

    /** Finds renderer.
     */
    static final class Rend implements Runnable {
        private static Class nodeRenderer;

        public void run() {
            try {
                if (nodeRenderer == null) {
                    nodeRenderer = loadClass("org.openide.explorer.view.NodeRenderer"); // NOI18N
                }

                TALK.set(nodeRenderer.newInstance());
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage());
            }
        }
    }

    static final class IndexC implements Runnable {
        public void run() {
            Index idx = (Index) TALK.get();
            java.awt.Container p = new javax.swing.JPanel();
            IndexedCustomizer ic = new IndexedCustomizer(p, false);
            ic.setObject(idx);
            ic.setImmediateReorder(false);

            org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor(p, Node.getString("LAB_order"));
            dd.setModal(true);
            dd.setOptionType(org.openide.DialogDescriptor.DEFAULT_OPTION);

            Object result = org.openide.DialogDisplayer.getDefault().notify(dd);

            if (result == org.openide.DialogDescriptor.OK_OPTION) {
                ic.doClose();
            }
        }
    }
}
