/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
