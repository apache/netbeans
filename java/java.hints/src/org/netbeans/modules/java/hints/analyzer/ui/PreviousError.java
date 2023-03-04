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

package org.netbeans.modules.java.hints.analyzer.ui;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.List;
import javax.swing.AbstractAction;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class PreviousError extends AbstractAction implements PropertyChangeListener {

    private AnalyzerTopComponent comp;

    public PreviousError(AnalyzerTopComponent comp) {
        this.comp = comp;
        this.comp.getExplorerManager().addPropertyChangeListener(this);
    }
        
    @Override
    public boolean isEnabled() {
        Node node = getNextMeaningfullNode();
        boolean enabled = node != null;
        
        if (node != null) {
            comp.seenNodes.add(0, node);
        }
        
        return enabled;
    }

    public void actionPerformed(ActionEvent e) {
        Node node = getNextMeaningfullNode();
        
        comp.nodesForNext.add(0, node);
        FixDescription fd = node.getLookup().lookup(FixDescription.class);
            
        assert fd != null;
        
        try {
            comp.getExplorerManager().setSelectedNodes(new Node[]{node});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }

        ErrorDescription ed = fd.getErrors();

        UiUtils.open(ed.getFile(), ed.getRange().getBegin().getOffset());
        fireEnabledChanged();
    }

    void fireEnabledChanged() {
        firePropertyChange("enabled", null, isEnabled());
    }
    
    private Node getNextMeaningfullNode() {
        List<Node> seenNodes = comp.seenNodes;

        if (seenNodes == null || seenNodes.isEmpty()) {
            return  null;
        }
        
        while (seenNodes != null && !seenNodes.isEmpty()) {
            Node top = comp.seenNodes.remove(0);

            FixDescription fd = top.getLookup().lookup(FixDescription.class);
            
            if (fd == null) {
                continue;
            }
            
            Node[] selected = comp.getExplorerManager().getSelectedNodes();

            if (selected.length == 1 && selected[0] == top) {
                comp.nodesForNext.add(0, top);

                continue;
            }
            
            if (comp.goOverFixed() && !fd.isFixed()) {
                comp.nodesForNext.add(0, top);
                continue;
            }

            return top;
        }
        
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        fireEnabledChanged();
    }
}
