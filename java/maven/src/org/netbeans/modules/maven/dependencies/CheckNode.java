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

    public static final int SINGLE_SELECTION = 0;
    public static final int DIG_IN_SELECTION = 4;
  
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
