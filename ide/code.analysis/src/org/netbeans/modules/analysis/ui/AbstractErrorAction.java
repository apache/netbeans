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
package org.netbeans.modules.analysis.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public abstract class AbstractErrorAction extends AbstractAction implements PropertyChangeListener {

    private final AnalysisResultTopComponent comp;
    //@GuardedBy("AWT")
    private final List<Node> currentSubsequentNodes = new ArrayList<>();

    public AbstractErrorAction(AnalysisResultTopComponent comp) {
        this.comp = comp;
        this.comp.getExplorerManager().addPropertyChangeListener(this);

        updateEnabled();
    }

    private boolean selecting;

    public void actionPerformed(ActionEvent e) {
        if (currentSubsequentNodes.isEmpty()) {
            //should not happen
            updateEnabled();
            return ;
        }

        Node node = currentSubsequentNodes.remove(0);

        if (node == null) {
            //should not happen
            updateEnabled();
            return ;
        }

        OpenCookie oc = node.getLookup().lookup(OpenCookie.class);

        assert oc != null;

        selecting = true;

        try {
            comp.getExplorerManager().setSelectedNodes(new Node[]{node});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            selecting = false;
        }

        oc.open();

        ensureNodesFilled();
        updateEnabled();
    }

    private void updateEnabled() {
        setEnabled(!currentSubsequentNodes.isEmpty() && currentSubsequentNodes.get(0) != null);
    }

    private static final int DESIRED_PREPARED_NODES_COUNT = 2;
    protected static final RequestProcessor WORKER = new RequestProcessor(AnalysisResultTopComponent.class.getName(), 1, false, false);

    //@GuardedBy("AWT")
    private long stateId;

    //@GuardedBy("AWT")
    private void ensureNodesFilled() {
        if (currentSubsequentNodes.size() < DESIRED_PREPARED_NODES_COUNT) {
            final long currentRequest = stateId;
            final Node from;

            if (currentSubsequentNodes.isEmpty()) {
                Node[] selected = this.comp.getExplorerManager().getSelectedNodes();

                if (selected.length > 0) {
                    from = selected[0];
                } else {
                    from = this.comp.getExplorerManager().getRootContext();
                }
            } else {
                from = currentSubsequentNodes.get(currentSubsequentNodes.size() - 1);
            }

            if (from == null) {
                while (currentSubsequentNodes.size() < DESIRED_PREPARED_NODES_COUNT) {
                    currentSubsequentNodes.add(null);
                }

                return ;
            }

            WORKER.post(new Runnable() {
                @Override public void run() {
                    final Node next = findSubsequentNode(from);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            if (currentRequest != stateId) return ;

                            currentSubsequentNodes.add(next);
                            ensureNodesFilled();
                            updateEnabled();
                        }
                    });
                }
            });

        }
    }

    protected abstract Node findSubsequentNode(Node from);

    public void propertyChange(PropertyChangeEvent evt) {
        if (selecting) return ;
        stateId++;
        currentSubsequentNodes.clear();
        ensureNodesFilled();
        updateEnabled();
    }

    protected final boolean isUseful(Node n) {
        return n.getLookup().lookup(ErrorDescription.class) != null;
    }

}
