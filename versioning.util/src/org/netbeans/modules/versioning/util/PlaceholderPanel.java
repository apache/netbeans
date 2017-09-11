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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.MenuComponent;
import javax.swing.JPanel;

/**
 * Panel which takes minimum, preferred and maximum sizes from its (only) child.
 * The panel can have only one child and it cannot be assigned a layout manager.
 * It always lays out its child such that the child covers the whole placeholder
 * panel.
 *
 * @author Marian Petras
 */
public class PlaceholderPanel extends JPanel {

    private static final Dimension ZERO_DIMENSION = new Dimension(0, 0);
    private static final Dimension MAX_DIMENSION = new Dimension(Short.MAX_VALUE,
                                                                 Short.MAX_VALUE);

    public PlaceholderPanel() {
        this(true);
    }

    public PlaceholderPanel(boolean isDoubleBuffered) {
        super(null, isDoubleBuffered);
    }

    public void setComponent(Component comp) {
        if (isEmpty()) {
            add(comp);
        } else {
            removeAll();
            add(comp);
            revalidate();
            repaint();
        }
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (mgr != null) {
            throw new UnsupportedOperationException(
                "PlaceholderPanel's layout manager cannot be changed.");//NOI18N
        }
    }

    @Override
    public Dimension getMinimumSize() {
        if (isEmpty()) {
            return new Dimension(ZERO_DIMENSION);
        }

        return getOccupant().getMinimumSize();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isEmpty()) {
            return new Dimension(ZERO_DIMENSION);
        }

        return getOccupant().getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        if (isEmpty()) {
            return new Dimension(MAX_DIMENSION);
        }

        return getOccupant().getMaximumSize();
    }


    @Override
    public void doLayout() {
        Component comp = getOccupant();
        if (comp != null) {
            comp.setBounds(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (!isEmpty()) {
            throw new IllegalStateException(
                    "This placeholder is already occupied.");           //NOI18N
        }

        super.addImpl(comp, constraints, index);
        updateOpacity();
        invalidate();
    }

    @Override
    public void remove(Component comp) {
        super.remove(comp);
        updateOpacity();
        invalidate();
    }

    @Override
    public void remove(int index) {
        super.remove(index);
        updateOpacity();
        invalidate();
    }

    @Override
    public synchronized void remove(MenuComponent popup) {
        super.remove(popup);
        updateOpacity();
        invalidate();
    }

    @Override
    public void removeAll() {
        super.removeAll();
        updateOpacity();
        invalidate();
    }

    private void updateOpacity() {
        setOpaque(isEmpty());
    }

    private Component getOccupant() {
        return isEmpty() ? null : getComponent(0);
    }

    public boolean isEmpty() {
        return getComponentCount() == 0;
    }

}
