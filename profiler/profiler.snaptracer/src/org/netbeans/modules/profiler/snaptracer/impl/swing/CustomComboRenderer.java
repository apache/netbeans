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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class CustomComboRenderer implements ListCellRenderer {

    private final JComboBox combo;
    private final ListCellRenderer renderer;
    private final JLabel rendererL;


    private CustomComboRenderer(JComboBox combo) {
        this.combo = combo;
        renderer = combo.getRenderer();
        if (renderer instanceof JLabel) rendererL = (JLabel)renderer;
        else rendererL = null;

        this.combo.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)   { repaint(); }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { repaint(); }
            public void popupMenuCanceled(PopupMenuEvent e)            { repaint(); }
            private void repaint() { CustomComboRenderer.this.combo.repaint(); }
        });
    }


    protected void setupRenderer(ListCellRenderer renderer, boolean popupVisible) {}

    protected void setupRenderer(JLabel renderer, boolean popupVisible) {}

    public abstract java.lang.String value(Object value);


    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {

        if (rendererL != null) setupRenderer(rendererL, combo.isPopupVisible());
        else setupRenderer(renderer, combo.isPopupVisible());
        
        return renderer.getListCellRendererComponent(list, value(value), index,
                                                     isSelected, cellHasFocus);
    }


    public static final class String extends CustomComboRenderer {

        public String(JComboBox combo) {
            super(combo);
        }

        public java.lang.String value(Object value) {
            return value == null ? "null" : value.toString(); // NOI18N
        }

    }


    public static final class Boolean extends CustomComboRenderer {

        public Boolean(JComboBox combo) {
            super(combo);
        }

        public java.lang.String value(Object value) {
            if (java.lang.Boolean.TRUE.equals(value)) return "enabled";
            if (java.lang.Boolean.FALSE.equals(value)) return "disabled";
            return "default";
        }

    }


    public static final class Number extends CustomComboRenderer {

        private final java.lang.String units;
        private final boolean lAlign;

        public Number(JComboBox combo, java.lang.String units, boolean lAlign) {
            super(combo);
            this.units = units;
            this.lAlign = lAlign;
        }

        protected void setupRenderer(JLabel renderer, boolean popupVisible) {
            if (popupVisible || !lAlign) renderer.setHorizontalAlignment(SwingConstants.TRAILING);
            else renderer.setHorizontalAlignment(SwingConstants.LEADING);
        }

        public java.lang.String value(Object value) {
            java.lang.String sunits = units == null ? "" : " " + units;
            return Integer.valueOf(-1).equals(value) ? "default" :
                   NumberFormat.getInstance().format(value) + sunits;
        }

    }

}
