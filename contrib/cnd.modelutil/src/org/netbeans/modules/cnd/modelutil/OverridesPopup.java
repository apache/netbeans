/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.modelutil;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.utils.ui.PopupUtil;
import org.openide.util.ImageUtilities;

/**
 *
 */

public class OverridesPopup extends JPanel implements FocusListener {

    private static enum Kind {
        BASE,
        BASE_PSEUDO,
        DESC,
        DESC_PSEUDO,
        MAIN,
        BASE_TEMPLATE,
        SPECIALIZATION,
        MAIN_TEMPLATE,
    }

    /*package-local for test purposes*/
    private static class Item implements Comparable<Item> {

        public final CsmOffsetableDeclaration declaration;
        public final Kind kind;

        public Item(CsmOffsetableDeclaration decl, Kind direction) {
            this.declaration = decl;
            this.kind = direction;
        }

        public String getDisplayName() {
            return declaration.getQualifiedName().toString();
        }

        private Image getBadge() {
            switch (kind) {
                case BASE:
                    return ImageUtilities.loadImage("org/netbeans/modules/cnd/modelutil/resources/overrides-badge.png");
                case BASE_PSEUDO:
                    return ImageUtilities.loadImage("org/netbeans/modules/cnd/modelutil/resources/overrides-badge-pseudo.png");
                case DESC:
                    return ImageUtilities.loadImage("org/netbeans/modules/cnd/modelutil/resources/is-overridden-badge.png");
                case DESC_PSEUDO:
                    return ImageUtilities.loadImage("org/netbeans/modules/cnd/modelutil/resources/is-overridden-badge-pseudo.png");
                case BASE_TEMPLATE:
                    return ImageUtilities.loadImage("org/netbeans/modules/cnd/modelutil/resources/base-template-badge.png");
                case SPECIALIZATION:
                    return ImageUtilities.loadImage("org/netbeans/modules/cnd/modelutil/resources/specialization-badge.png");
                default:
                    return null;
            }
        }

        public Icon getIcon() {
            ImageIcon icon = CsmImageLoader.getIcon(declaration);
            Image badge = getBadge();
            if (badge == null) {
                BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = img.createGraphics();
                Color transparent = new Color(0, 0, 0, 0);
                g2d.setColor(transparent);
                g2d.setComposite(AlphaComposite.Src);
                g2d.fill(new Rectangle2D.Float(0, 0, img.getWidth(), img.getHeight()));
                badge = img;
            }
            return ImageUtilities.image2Icon(ImageUtilities.mergeImages(icon.getImage(), badge, 16, 0));
        }

        @Override
        public String toString() {
            return declaration.getQualifiedName().toString();
        }

        @Override
        public int compareTo(Item o) {
            if (o == null) {
                return -1;
            } else {
                if (o.kind == this.kind) {
                    return declaration.getQualifiedName().toString().compareTo(o.declaration.getQualifiedName().toString());
                } else {
                    return this.kind == Kind.BASE ? -1 : 1;
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Item other = (Item) obj;
            if (this.declaration != other.declaration && (this.declaration == null || !this.declaration.getQualifiedName().equals(other.declaration.getQualifiedName()))) {
                return false;
            }
            if (this.kind != other.kind && (this.kind == null || !this.kind.equals(other.kind))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.declaration != null ? this.declaration.getQualifiedName().hashCode() : 0);
            hash = 97 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }

    }

    private static class RenderComponent extends JLabel {

        private final Item item;
        private final boolean selected;
        private final boolean hasFocus;
        private final JList<?> list;

        public RenderComponent(Item item, boolean selected, boolean hasFocus, JList<?> list) {
            super(item.getDisplayName(), item.getIcon(), SwingConstants.LEFT);
            this.item = item;
            this.selected = selected;
            this.list = list;
            this.hasFocus = hasFocus;
            setOpaque(true);
            if (selected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(true);
            setFont(list.getFont());
            Border border = BorderFactory.createEmptyBorder(1, 1, 1, 1);
            if (hasFocus) {
                if (selected) {
                    border = UIManager.getBorder("List.focusSelectedCellHighlightBorder"); // NOI18N
                }
                if (border == null) {
                    border = UIManager.getBorder("List.focusCellHighlightBorder"); // NOI18N
                }
            } else {
                border = BorderFactory.createEmptyBorder(1, 1, 1, 1);
            }
            Border outer;
            if (item.kind == Kind.MAIN) {
                outer = BorderFactory.createEmptyBorder(0, 6, 3, 6);
            } else {
                outer = BorderFactory.createEmptyBorder(0, 6, 0, 6);
            }
            border = BorderFactory.createCompoundBorder(border, outer);
            setBorder(border);
        }

        public @Override void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (item.kind == Kind.MAIN) {
                g.setColor(Color.gray);
                int h = getHeight();
                h -= 2;
                g.drawLine(0, h, getWidth(), h);
            }
        }
    }

    private static class RendererImpl extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (value instanceof Item) {
                return new RenderComponent((Item) value, isSelected, cellHasFocus, list);
            } else {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }

    private JList<Item> list;
    private JScrollPane scrollPane;
    private final List<Item> elements;
    private final CsmOffsetableDeclaration mainDeclaration;
    private final boolean gotoDefinitions;

    public OverridesPopup(String caption,
            Collection<? extends CsmOffsetableDeclaration> baseDeclarations,
            Collection<? extends CsmOffsetableDeclaration> pseudoBaseDeclarations,
            Collection<? extends CsmOffsetableDeclaration> descendantDeclarations,
            Collection<? extends CsmOffsetableDeclaration> pseudoDescendantDeclarations,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates,
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations) {
        this(caption, null, baseDeclarations, pseudoBaseDeclarations,
             descendantDeclarations, pseudoDescendantDeclarations, baseTemplates, templateSpecializations, false);
    }

    public OverridesPopup(String caption, CsmOffsetableDeclaration mainDeclaration,
            Collection<? extends CsmOffsetableDeclaration> baseDeclarations,
            Collection<? extends CsmOffsetableDeclaration> descendantDeclarations,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates,
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations,
            boolean gotoDefinitions) {
        this(caption, mainDeclaration, baseDeclarations, Collections.<CsmOffsetableDeclaration>emptyList(),
             descendantDeclarations, Collections.<CsmOffsetableDeclaration>emptyList(), baseTemplates, templateSpecializations, gotoDefinitions);
    }

    private OverridesPopup(String caption, CsmOffsetableDeclaration mainDeclaration,
            Collection<? extends CsmOffsetableDeclaration> baseDeclarations,
            Collection<? extends CsmOffsetableDeclaration> pseudoBaseDeclarations,
            Collection<? extends CsmOffsetableDeclaration> descendantDeclarations,
            Collection<? extends CsmOffsetableDeclaration> pseudoDescendantDeclarations,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates,
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations,
            boolean gotoDefinitions) {

        super(new BorderLayout());
        this.mainDeclaration = mainDeclaration;
        this.gotoDefinitions = gotoDefinitions;

        this.elements = new ArrayList<Item>();

        if (caption != null) {
            JLabel title = new JLabel(caption);
            title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            title.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            add(title, BorderLayout.NORTH);
        }

        list = new JList<>();
        //list.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        scrollPane = new JScrollPane(list);
        add(scrollPane, BorderLayout.CENTER);

        for (CsmOffsetableDeclaration decl : baseDeclarations) {
            elements.add(new Item(decl, Kind.BASE));
        }
        for (CsmOffsetableDeclaration decl : pseudoBaseDeclarations) {
            elements.add(new Item(decl, Kind.BASE_PSEUDO));
        }
        for (CsmOffsetableDeclaration decl : baseTemplates) {
            elements.add(new Item(decl, Kind.BASE_TEMPLATE));
        }
        for (CsmOffsetableDeclaration decl : descendantDeclarations) {
            elements.add(new Item(decl, Kind.DESC));
        }
        for (CsmOffsetableDeclaration decl : pseudoDescendantDeclarations) {
            elements.add(new Item(decl, Kind.DESC_PSEUDO));
        }
        for (CsmOffsetableDeclaration decl : templateSpecializations) {
            elements.add(new Item(decl, Kind.SPECIALIZATION));
        }
        Collections.sort(elements);

        DefaultListModel<Item> model = new DefaultListModel<>();
        if (this.mainDeclaration != null) {
            model.addElement(new Item(this.mainDeclaration, Kind.MAIN));
        }
        for (Item element : elements) {
            model.addElement(element);
        }
        list.setModel(model);
        list.setSelectedIndex(0);
        list.setCellRenderer(new RendererImpl());
        if (model.getSize() < 10) {
            list.setVisibleRowCount(model.getSize());
        }

        list.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER && evt.getModifiers() == 0) {
                    openSelected();
                }
            }
        });
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 1) {
                    openSelected();
                }
            }
        });

        list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addFocusListener(OverridesPopup.this);
    }

    private void openSelected() {
        final String taskName = "Open override function"; //NOI18N
        Runnable run = new Runnable() {

            @Override
            public void run() {
                Item el = list.getSelectedValue();
                if (el != null) {
                    CsmOffsetableDeclaration decl = el.declaration;
                    if (gotoDefinitions) {
                        if (CsmKindUtilities.isFunctionDeclaration(decl)) {
                            CsmFunctionDefinition definition = ((CsmFunction) decl).getDefinition();
                            if (definition != null) {
                                decl = definition;
                            }
                        }
                    }
                    CsmUtilities.openSource(decl);
                }
            }
        };
        CsmModelAccessor.getModel().enqueue(run, taskName);
        PopupUtil.hidePopup();
    }

    @Override
    public void focusGained(FocusEvent arg0) {
        list.requestFocus();
        list.requestFocusInWindow();
    }

    @Override
    public void focusLost(FocusEvent arg0) {
    }
}
