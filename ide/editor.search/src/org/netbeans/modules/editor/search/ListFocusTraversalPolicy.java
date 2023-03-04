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
package org.netbeans.modules.editor.search;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;

public class ListFocusTraversalPolicy extends FocusTraversalPolicy {

    private final List<Component> focusList = new ArrayList<>();
    public ListFocusTraversalPolicy(List<Component> focusList) {
        this.focusList.addAll(focusList);
    }

    public List<Component> getFocusList() {
        return focusList;
    }

    @Override
    public Component getComponentAfter(Container aContainer, Component aComponent) {
        int indexOf = getFocusList().indexOf(aComponent);
        if (indexOf == -1) {
            return null;
        } else if (indexOf == getFocusList().size() - 1) {
            Component nextComponent = isFocusableComponent(getFocusList().get(0)) ? getFocusList().get(0) : getComponentAfter(aContainer, getFocusList().get(0));
            deselectComponent(aComponent);
            selectComponent(nextComponent);
            return nextComponent;
        } else {
            Component nextComponent = isFocusableComponent(getFocusList().get(indexOf + 1)) ? getFocusList().get(indexOf + 1) : getComponentAfter(aContainer, getFocusList().get(indexOf + 1));
            deselectComponent(aComponent);
            selectComponent(nextComponent);
            return nextComponent;
        }
    }

    @Override
    public Component getComponentBefore(Container aContainer, Component aComponent) {
        int indexOf = getFocusList().indexOf(aComponent);
        if (indexOf == -1) {
            return null;
        } else if (indexOf == 0) {
            Component nextComponent = isFocusableComponent(getFocusList().get(getFocusList().size() - 1)) && getFocusList().get(getFocusList().size() - 1).isVisible() ? getFocusList().get(getFocusList().size() - 1) : getComponentBefore(aContainer, getFocusList().get(getFocusList().size() - 1));
            deselectComponent(aComponent);
            selectComponent(nextComponent);
            return nextComponent;
        } else {
            Component nextComponent = isFocusableComponent(getFocusList().get(indexOf - 1)) ? getFocusList().get(indexOf - 1) : getComponentBefore(aContainer, getFocusList().get(indexOf - 1));
            deselectComponent(aComponent);
            selectComponent(nextComponent);
            return nextComponent;
        }
    }
    
    private static void deselectComponent(Component aComponent) {
        if (aComponent instanceof JTextComponent) {
            ((JTextComponent) aComponent).select(0, 0);
        }
    }
    
    private static void selectComponent(Component aComponent) {
        if (aComponent instanceof JTextComponent) {
            ((JTextComponent) aComponent).selectAll();
        }
    }

    @Override
    public Component getFirstComponent(Container aContainer) {
        return getFocusList().get(0);
    }

    @Override
    public Component getLastComponent(Container aContainer) {
        return getFocusList().get(getFocusList().size() - 1);
    }

    @Override
    public Component getDefaultComponent(Container aContainer) {
        return getFocusList().get(0);
    }

    private boolean isFocusableComponent(Component aComponent) {
        return aComponent.isEnabled() && aComponent.isVisible();
    }
}
