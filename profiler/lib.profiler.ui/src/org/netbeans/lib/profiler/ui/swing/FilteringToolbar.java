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
package org.netbeans.lib.profiler.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.lib.profiler.filters.GenericFilter;
import org.netbeans.lib.profiler.filters.TextFilter;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class FilteringToolbar extends InvisibleToolbar {
    
    private TextFilter filter;
        
    private final List<Component> hiddenComponents = new ArrayList<>();
    private final AbstractButton filterButton;

    public FilteringToolbar(String name) {
        if (!UIUtils.isNimbusLookAndFeel())
            setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));

        filterButton = new JToggleButton(Icons.getIcon(GeneralIcons.FILTER)) {
            protected void fireActionPerformed(ActionEvent e) {
                if (isSelected()) showFilter(); else hideFilter();
            }
        };
        filterButton.setToolTipText(name);
        add(filterButton);
    }


    protected abstract void filterChanged();
    
    
    public final boolean isAll() {
        return filter == null;
    }
    
    public final boolean passes(String value) {
        return filter == null ? true : filter.passes(value);
    }
    
    public final GenericFilter getFilter() {
        TextFilter copy = new TextFilter();
        if (filter != null) copy.copyFrom(filter);
        return copy;
    }

    
    private void filterChanged(String value) {
        if (value == null) {
            filter = null;
        } else {
            if (filter == null) filter = new TextFilter();
            filter.setValue(value);
        }
        filterChanged();
    }

    private void showFilter() {
        filterButton.setSelected(true);

        final JTextField f = new JTextField();
        f.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { changed(); }
            public void removeUpdate(DocumentEvent e)  { changed(); }
            public void changedUpdate(DocumentEvent e) { changed(); }
            private void changed() { filterChanged(f.getText().trim()); }
        });
        f.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (esc(e)) hideFilter(); }
            public void keyReleased(KeyEvent e) { esc(e); }
            private boolean esc(KeyEvent e) {
                boolean esc = e.getKeyCode() == KeyEvent.VK_ESCAPE;
                if (esc) e.consume();
                return esc;
            }
        });

        for (int i = 1; i < getComponentCount(); i++)
            hiddenComponents.add(getComponent(i));

        for (Component c : hiddenComponents) remove(c);

        add(Box.createHorizontalStrut(3));
        add(f);
        f.requestFocusInWindow();

        invalidate();
        revalidate();
        doLayout();
        repaint();
    }

    private void hideFilter() {
        filterChanged(null);

        remove(2);
        remove(1);
        for (Component c : hiddenComponents) add(c);

        filterButton.setSelected(false);
        filterButton.requestFocusInWindow();

        invalidate();
        revalidate();
        doLayout();
        repaint();
    }

}
