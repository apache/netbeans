/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
