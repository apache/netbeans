/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * SimpleEngine.java
 *
 * Created on January 25, 2004, 1:42 PM
 */

package org.netbeans.actions.simple;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.Keymap;
import org.netbeans.actions.api.ContextProvider;
import org.netbeans.actions.api.Engine;
import org.netbeans.actions.engine.spi.AbstractContextMenuFactory;
import org.netbeans.actions.engine.spi.MenuFactory;
import org.netbeans.actions.engine.spi.ToolbarFactory;
import org.netbeans.actions.engine.spi.AbstractEngine;
import org.netbeans.actions.engine.spi.AbstractMenuFactory;
import org.netbeans.actions.engine.spi.AbstractToolbarFactory;
import org.netbeans.actions.engine.spi.ActionFactory;
import org.netbeans.actions.engine.spi.ContextMenuFactory;
import org.netbeans.actions.spi.ActionProvider;
import org.netbeans.actions.spi.ContainerProvider;

/** A basic test implementation
 *
 * @author  tim
 */
public class SimpleEngine extends AbstractEngine implements ActionListener {
    Interpreter interp;
    ResourceBundle bundle;
    private Timer timer = new javax.swing.Timer(100, this);
    public static Engine createEngine (URL actionDefs, ResourceBundle bundle) {
        Interpreter interp = new Interpreter (actionDefs);
        return new SimpleEngine(interp, bundle);
    }

    /** Creates a new instance of SimpleEngine */
    private SimpleEngine(Interpreter interp, ResourceBundle bundle) {
        super (new SimpleActionProvider(interp, bundle), new SimpleContainerProvider(interp, bundle));
        this.bundle = bundle;
        this.interp = interp;
    }
    
    public ContextProvider getContextProvider() {
        return super.getContextProvider();
    }
    
    protected ActionFactory createActionFactory() {
        return new SimpleActionFactory ();
    }
    
    protected MenuFactory createMenuFactory() {
        return new SimpleMenuFactory();
    }
    
    protected ToolbarFactory createToolbarFactory() {
        return new SimpleToolbarFactory();
    }
    
    protected ContextMenuFactory createContextMenuFactory() {
        return new SimpleContextMenuFactory();
    }
    
    public InputMap createInputMap(JComponent jc) {
        return new SimpleInputMap(jc);
    }
    
    public ActionMap createActionMap() {
        return new SimpleActionMap();
    }

    private Keymap keymap = null;
    public Keymap createKeymap() {
        if (keymap == null) {
            keymap = new SimpleKeymap(this, interp);
        }
        return keymap;
    }
    
    boolean updateRecommended = false;
    public void recommendUpdate() {
        updateRecommended = true;
        timer.setRepeats(true);
        if (!timer.isRunning()) {
            timer.start();
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (updateRecommended) {
            update();
            updateRecommended = false;
        } else {
            timer.stop();
        }
    }
    
    Action getAction (String action) {
        //Used by keymap
        return getActionFactory().getAction (action, null, getContextProvider().getContext());
    }
    
    private class SimpleActionFactory implements ActionFactory {
        public Action getAction (final String action, String containerCtx, final Map context) {
            return new AbstractAction () {
                public void actionPerformed (ActionEvent ae) {
                    SimpleInvoker invoker = interp.getInvoker(action);
                    System.err.println("Invoker is " + invoker);
                    System.err.println("Invoking " + action + " on " + context);
                    if (invoker != null) {
                        invoker.invoke(context);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            };
        }
    }
    
    private class SimpleToolbarFactory extends AbstractToolbarFactory {
        public SimpleToolbarFactory() {
            super (SimpleEngine.this);
        }
    }
    
    private class SimpleMenuFactory extends AbstractMenuFactory {
        public SimpleMenuFactory() {
            super (SimpleEngine.this);
        }
    }
    
    private class SimpleContextMenuFactory extends AbstractContextMenuFactory {
        public SimpleContextMenuFactory () {
            super (SimpleEngine.this);
        }
    }
    
    private class SimpleInputMap extends ComponentInputMap {
        public SimpleInputMap (JComponent jc) {
            super (jc);
        }
        
        public Object get(KeyStroke keyStroke) {
            return interp.getActionForKeystroke(keyStroke);
        }
        
        public void remove(Object key) {
            throw new UnsupportedOperationException();
        }
        
        public KeyStroke[] keys() {
            return interp.getAllKeystrokes();
        }

        public int size() {
            return super.size() + keys().length;
        }
        
        public KeyStroke[] allKeys() {
            return keys(); //XXX merge w/ super
        }
        
        private void writeObject(ObjectOutputStream s) throws IOException {
            //do nothing
        } 

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            //do nothing
        }    
    }
    
    private class SimpleActionMap extends ActionMap {
        public Action get(Object key) {
            Action a = super.get(key);
            if (a == null && key instanceof String) {
                a = getAction((String)key);
            }
            return a;
        }
        
        public Object[] keys() {
            return interp.getAllActionsBoundToKeystrokes();
        }
        
    }
    
}
