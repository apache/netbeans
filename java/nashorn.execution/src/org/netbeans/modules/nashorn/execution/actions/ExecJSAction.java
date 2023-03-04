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

package org.netbeans.modules.nashorn.execution.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.nashorn.execution.NashornPlatform;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin
 */
abstract class ExecJSAction extends AbstractAction implements ContextAwareAction, ActionListener, ChangeListener {
    
    protected static final Action NO_ACTION = createNoAction();
    
    private final FileObject js;
    
    protected ExecJSAction(String name) {
        putValue(Action.NAME, name);
        js = null;
        setEnabled(true);
    }
    
    protected ExecJSAction(String name, FileObject js, String command) {
        putValue(Action.NAME, name);
        this.js = js;
        KeyStroke actionKeyStroke = getActionKeyStroke(command);
        putValue(Action.ACCELERATOR_KEY, actionKeyStroke);
        setEnabled(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JavaPlatform javaPlatform = NashornPlatform.getDefault().getPlatform();
        if (javaPlatform == null) {
            //System.err.println("No suitable Java!");
            // TODO: Show a dialog that opens Java Platform Manager
            return ;
        }
        FileObject file = getCurrentFile();
        if (file == null) {
            return ;
        }
        try {
            exec(javaPlatform, file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private FileObject getCurrentFile() {
        if (js != null) {
            return js;
        }
        return Utilities.actionsGlobalContext().lookup(FileObject.class);
    }
    
    protected abstract void exec(JavaPlatform javaPlatform, FileObject fo) throws IOException, UnsupportedOperationException;

    @Override
    public void stateChanged(ChangeEvent e) {
        JavaPlatform platform = NashornPlatform.getDefault().getPlatform();
        setEnabled(platform != null);
    }
    
    protected static KeyStroke getActionKeyStroke(String command) {
        Action fileCommandAction = FileSensitiveActions.fileCommandAction(command, "name", null);
        if (fileCommandAction != null) {
            return (KeyStroke) fileCommandAction.getValue(Action.ACCELERATOR_KEY);
        } else {
            return null;
        }
    }
    
    protected static boolean isEnabledAction(String command, FileObject fo, Lookup actionContext) {
        Project p = findProject(fo);
        if (p != null) {
            ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
            if (ap != null && ap.getSupportedActions() != null && Arrays.asList(ap.getSupportedActions()).contains(command)) {
                return ap.isActionEnabled(command, actionContext);
            }
        }
        return false;
    }
    
    private static Project findProject(FileObject fo) {
        return FileOwnerQuery.getOwner(fo);
    }
    
    private static Action createNoAction() {
        return new NoAction();
    }
    
    private static final class NoAction implements Action, Presenter.Popup {
        
        private NoItem NO_ITEM = new NoItem();

        @Override
        public Object getValue(String key) {
            return null;
        }

        @Override
        public void putValue(String key, Object value) {}

        @Override
        public void setEnabled(boolean b) {}

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return NO_ITEM;
        }
        
        private static class NoItem extends JMenuItem implements DynamicMenuContent {

            @Override
            public JComponent[] getMenuPresenters() {
                return new JComponent[]{};
            }

            @Override
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                return items;
            }
            
        }
    }
}
