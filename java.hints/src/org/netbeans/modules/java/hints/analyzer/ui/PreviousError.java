/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
