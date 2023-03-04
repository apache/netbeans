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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Action that can always be invoked and work procedurally.
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class AbstractAddMethodAction extends AbstractAction implements Presenter.Popup, ContextAwareAction {

    /** Action context. */
    private Lookup context;
    private final AbstractAddMethodStrategy strategy;

    public AbstractAddMethodAction(AbstractAddMethodStrategy strategy) {
        super(/*strategy.getTitle()*/);
        this.strategy = strategy;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        this.context = actionContext;
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
        return enable ? this : null;
    }

    public String getName(){
        return strategy.getTitle();
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
        try {
            if (fileObject != null) {
                ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(activatedNodes[0]);
                if (elementHandle != null) {
                    if (strategy.supportsEjb(fileObject, elementHandle.getQualifiedName())) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        performAction(context.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
    }

    protected void performAction(final org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return;
        }
        final FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);

            if (fileObject != null) {
                try {
                    ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(activatedNodes[0]);
                    if (elementHandle != null) {
                        if (strategy.supportsEjb(fileObject, elementHandle.getQualifiedName())) {
                            strategy.addMethod(fileObject, elementHandle.getQualifiedName());
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(AbstractAddMethodAction.class.getName()).log(Level.WARNING, null, ex);
                }
            }
    }

    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return getName();
        } else {
            return super.getValue(key);
        }
    }

    public JMenuItem getPopupPresenter() {
        return new JMenuItem (this);
    }

}
