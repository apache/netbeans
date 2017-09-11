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
package org.netbeans.modules.maven.dependencies;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.*;

/**
 * @author Pavel Flaska
 */
public class CheckNode extends DefaultMutableTreeNode {

    public final static int SINGLE_SELECTION = 0;
    public final static int DIG_IN_SELECTION = 4;
  
    private int selectionMode;
    private boolean isSelected;

    private String nodeLabel;
    private Icon icon;
    
    private boolean disabled = false;
    private List<ChangeListener> listeners;
    private boolean strike;
    private boolean italic;
    
    public CheckNode(Object userObject, String nodeLabel, Icon icon) {
        super(userObject, false);
        this.isSelected = true;
        setSelectionMode(DIG_IN_SELECTION);
        this.nodeLabel = nodeLabel;
        this.icon = icon;
        listeners = new ArrayList<ChangeListener>();
    }
    
    String getLabel() {
        if (strike) {
            return "<s>" + nodeLabel + "</s>";
        }
        if (italic) {
            return "<i>" + nodeLabel + "</i>";
        }
        return nodeLabel;
    }
    
    Icon getIcon() {
        return icon;
    }
    
    public void setDisabled() {
        disabled = true;
        isSelected = false;
        removeAllChildren();
    }
    
    boolean isDisabled() {
        return disabled;
    }

    public final void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    public final int getSelectionMode() {
        return selectionMode;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
            Enumeration e = children.elements();      
            while (e.hasMoreElements()) {
                CheckNode node = (CheckNode)e.nextElement();
                node.setSelected(isSelected);
            }
        }
        fireChange();
    }

    public void addChangeListener(ChangeListener list) {
        listeners.add(list);
    }

    public void removeChangeListener(ChangeListener list) {
        listeners.remove(list);
    }

    public boolean isSelected() {
        return isSelected;
    }

    void strike() {
        strike = true;
    }

    void unitalic() {
        italic = false;
    }

    void italic() {
        italic = true;
    }

    void unstrike() {
        strike = false;
    }

    private void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(event);
        }
    }
}
