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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.text.MessageFormat;
import java.util.logging.Level;
import javax.swing.undo.UndoableEdit;
import org.netbeans.modules.form.actions.DuplicateAction;
import org.netbeans.modules.form.assistant.AssistantModel;
import org.netbeans.spi.palette.PaletteController;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;

import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.layoutdesign.*;
import org.netbeans.modules.form.menu.MenuEditLayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * A transparent layer (glass pane) handling user operations in designer (mouse
 * and keyboard events) and painting selection and drag&drop feedback.
 * Technically, this is a layer in FormDesigner, placed over ComponentLayer.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class HandleLayer extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener
{
    // constants for mode parameter of getMetaComponentAt(Point,int) method
    public static final int COMP_DEEPEST = 0; // get the deepest component (at given position)
    public static final int COMP_SELECTED = 1; // get the deepest selected component
    public static final int COMP_ABOVE_SELECTED = 2; // get the component above the deepest selected component
    public static final int COMP_UNDER_SELECTED = 3; // get the component under the deepest selected component

    private static final int DESIGNER_RESIZING = 256; // flag for resizeType
    private static final int INBOUND_RESIZING = 512; // flag for resizeType
    private static final int RESIZE_MASK = LayoutSupportManager.RESIZE_UP | LayoutSupportManager.RESIZE_DOWN
                                         | LayoutSupportManager.RESIZE_LEFT | LayoutSupportManager.RESIZE_RIGHT;

    private static MessageFormat resizingHintFormat;
    private static MessageFormat sizeHintFormat;

    private FormDesigner formDesigner;
    private boolean viewOnly;

    private ComponentDrag draggedComponent;
    private JPanel dragPanel;
    
    private Point lastMousePosition;
    private int lastXPosDiff;
    private int lastYPosDiff;

    private Point lastLeftMousePoint;
    private Point prevLeftMousePoint;
    private boolean draggingEnded; // prevents dragging from starting inconveniently
    private int resizeType;

    private boolean draggingSuspended;

    private SelectionDragger selectionDragger;
    private Image resizeHandle;

    private DropTarget dropTarget;
    private DropTargetListener dropListener;

    private String mouseHint;

    private MouseWheeler wheeler;
    private boolean wheelerBlocked;

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = FormLoaderSettings.getInstance();

    // -------

    HandleLayer(FormDesigner fd) {
        formDesigner = fd;
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        setLayout(null);
        
        // Hack - the panel is used to ensure correct painting of dragged components
        dragPanel = new JPanel();
        dragPanel.setLayout(null);
        dragPanel.setBounds(-1,-1,0,0);
        add(dragPanel);

        // set Ctrl+TAB and Ctrl+Shift+TAB as focus traversal keys - to have
        // TAB and Shift+TAB free for component selection
        Set<AWTKeyStroke> keys = new HashSet<AWTKeyStroke>();
        keys.add(AWTKeyStroke.getAWTKeyStroke(9,
                                              InputEvent.CTRL_DOWN_MASK,
                                              true));
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                              keys);
        keys.clear();
        keys.add(AWTKeyStroke.getAWTKeyStroke(9,
                                              InputEvent.CTRL_DOWN_MASK
                                                 |InputEvent.SHIFT_DOWN_MASK,
                                              true));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                              keys);

        getAccessibleContext().setAccessibleName(
            FormUtils.getBundleString("ACSN_HandleLayer")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_HandleLayer")); // NOI18N
        
        dropListener = new NewComponentDropListener();
        dropTarget = new DropTarget(this, dropListener);
    }

    public boolean isSuspended() {
        return draggingSuspended;
    }

    public void suspend() {
        draggingSuspended = true;
    }

    public void resume() {
        draggingSuspended = false;
    }
    
    //expose the drop listener so the MenuEditLayer can access it
    public DropTargetListener getNewComponentDropListener() {
        return dropListener;
    }

    // allow a wrapper listener if anybody needs to augment the incoming data
    public void setNewComponentDropListener(DropTargetListener l) {
        dropTarget.removeDropTargetListener(dropListener);
        dropListener = l;
        try {
            dropTarget.addDropTargetListener(dropListener);                                        
        } catch (TooManyListenersException ex) {
        }
    }

    void setViewOnly(boolean viewOnly) {
        if(this.viewOnly == viewOnly) {
            return;
        }
        if(viewOnly) {
            dropTarget.removeDropTargetListener(dropListener);            
        } else {
	    try {
		dropTarget.addDropTargetListener(dropListener);                                        
	    } catch (TooManyListenersException ex) {
		ex.printStackTrace();
	    }
        }
        this.viewOnly = viewOnly;
    }

    private FormModel getFormModel() {
        return formDesigner.getFormModel();
    }

    private MetaComponentCreator getComponentCreator() {
        return formDesigner.getFormModel().getComponentCreator();
    }

    private LayoutModel getLayoutModel() {
        return formDesigner.getFormModel().getLayoutModel();
    }

    // ---------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // paint component in the connection mode (if any)
        if (formDesigner.getDesignerMode() == FormDesigner.MODE_CONNECT) {
            RADComponent conSource = formDesigner.getConnectionSource();
            RADComponent conTarget = formDesigner.getConnectionTarget();
            if (conSource != null || conTarget != null) {
                g2.setColor(formSettings.getConnectionBorderColor());
                if (conSource != null) {
                    paintSelection(g2, conSource, false);
                }
                if (conTarget != null) {
                    paintSelection(g2, conTarget, false);
                }
            }
            return; // that's all in connection mode
        }

        if (draggedComponent != null) {
            if (!isSuspended()) {
                try {
                    FormLAF.setUseDesignerDefaults(getFormModel());
                    draggedComponent.paintFeedback(g2);
                    
                    // Paint hidden components
                    Area draggedArea = null;
                    for (Map.Entry<RADComponent,Rectangle[]> entry : hiddenComponents.entrySet()) {
                        RADComponent metacomp = entry.getKey();
                        Object object = formDesigner.getComponent(metacomp);
                        if (!(object instanceof Component)) {
                            continue;
                        }
                        Component comp = (Component)object;
                        Rectangle[] value = entry.getValue();
                        Rectangle bounds = new Rectangle(value[0]);
                        Rectangle visibleBounds = value[1];
                        comp.setSize(bounds.getSize());
                        doLayout(comp);
                        bounds = convertRectangleFromComponent(bounds, comp.getParent());
                        
                        // Visible part of the component
                        Rectangle visibleRect = new Rectangle(
                            bounds.x+visibleBounds.x,
                            bounds.y+visibleBounds.y,
                            visibleBounds.width,
                            visibleBounds.height);
                        Area clip = new Area(visibleRect);
                        
                        // Hidden components should not be visible through the dragged components
                        if (draggedArea == null && draggedComponent.movingBounds.length > 0
                                && draggedComponent.movingBounds[0].x > Integer.MIN_VALUE) {
                            // Area of dragged components
                            draggedArea = new Area();
                            for (int i=0; i<draggedComponent.showingComponents.length; i++) {
                                Rectangle rect = new Rectangle(
                                    draggedComponent.movingBounds[i].x + draggedComponent.convertPoint.x,
                                    draggedComponent.movingBounds[i].y + draggedComponent.convertPoint.y,
                                    draggedComponent.movingBounds[i].width + 1,
                                    draggedComponent.movingBounds[i].height + 1);
                                draggedArea.add(new Area(rect));
                            }
                        }
                        if (draggedArea != null) {
                            clip.subtract(draggedArea);
                        }

                        Graphics gg = g.create();
                        gg.setClip(clip);
                        gg.translate(bounds.x, bounds.y);
                        paintDraggedComponent(comp, gg, 0.3f);
                        gg.dispose();
                    }
                } finally {
                    FormLAF.setUseDesignerDefaults(null);
                }
            }
        }
        else { // just paint the selection of selected components
            g2.setColor(formSettings.getSelectionBorderColor());
            boolean painted = false;
            try {
                boolean inLayout = selectedComponentsInSameVisibleContainer();
                if (inLayout || isNewLayoutRootSelection(true)) {
                    paintLayoutInnerSelection(g2);
                }
                for (RADComponent metacomp : formDesigner.getSelectedComponents()) {
                    RADVisualComponent layoutMetacomp = formDesigner.componentToLayoutComponent(metacomp);
                    if (layoutMetacomp != null)
                        metacomp = layoutMetacomp;
                    paintSelection(g2, metacomp, inLayout);
                }
                paintButtonGroups(g2);
                painted = true;
            } finally {
                // Make sure that problems in selection painting
                // doesn't cause endless stream of exceptions.
                if (!painted) {
                    formDesigner.clearSelection();
                }
            }

            if (selectionDragger != null) {
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(getPaintSelectionStroke());
                selectionDragger.paintDragFeedback(g2);
                g2.setStroke(oldStroke);
            }
        }
    }

    /**
     * @param inLayout indicates whether to paint layout related decorations
     *        (resize handles)
     */
    private void paintSelection(Graphics2D g, RADComponent metacomp, boolean inLayout) {
        if (!(metacomp instanceof RADVisualComponent) && !(metacomp instanceof RADMenuItemComponent))
            return;
        Object comp = formDesigner.getComponent(metacomp);
        if (!(comp instanceof Component))
            return;

        Component component = (Component) comp;
        Component parent = component.getParent();

        if (parent != null && component.isShowing()) {
            Rectangle selRect = component.getBounds();
            convertRectangleFromComponent(selRect, parent);
            Rectangle visible = new Rectangle(0, 0, parent.getWidth(), parent.getHeight());
            visible = convertVisibleRectangleFromComponent(visible, parent);

            int resizable = 0;
            if (inLayout) {
                resizable = getComponentResizable((RADVisualComponent)metacomp);
            }
            if (resizable == 0) {
                selRect = selRect.intersection(visible);
            }
            int correction = formSettings.getSelectionBorderSize() % 2;
            int x = selRect.x - correction;
            int y = selRect.y - correction;
            int width = selRect.width + correction;
            int height = selRect.height + correction;
            Stroke oldStroke = g.getStroke();
            g.setStroke(getPaintSelectionStroke());
            g.drawRect(x, y, width, height);
            g.setStroke(oldStroke);
            if (inLayout) {
                Image resizeHandle = resizeHandle();
                int iconHeight = resizeHandle.getHeight(null);
                int iconWidth = resizeHandle.getWidth(null);
                if ((resizable & LayoutSupportManager.RESIZE_LEFT) != 0) {
                    g.drawImage(resizeHandle, x-iconWidth+1, y+(height-iconHeight)/2, null);
                    if ((resizable & LayoutSupportManager.RESIZE_UP) != 0) {
                        g.drawImage(resizeHandle, x-iconWidth+1, y-iconHeight+1, null);
                    }
                    if ((resizable & LayoutSupportManager.RESIZE_DOWN) != 0) {
                        g.drawImage(resizeHandle, x-iconWidth+1, y+height, null);
                    }
                }
                if ((resizable & LayoutSupportManager.RESIZE_RIGHT) != 0) {
                    g.drawImage(resizeHandle, x+width, y+(height-iconHeight)/2, null);
                    if ((resizable & LayoutSupportManager.RESIZE_UP) != 0) {
                        g.drawImage(resizeHandle, x+width, y-iconHeight+1, null);
                    }
                    if ((resizable & LayoutSupportManager.RESIZE_DOWN) != 0) {
                        g.drawImage(resizeHandle, x+width, y+height, null);
                    }
                }
                if ((resizable & LayoutSupportManager.RESIZE_UP) != 0) {
                    g.drawImage(resizeHandle, x+(width-iconWidth)/2, y-iconHeight+1, null);
                }
                if ((resizable & LayoutSupportManager.RESIZE_DOWN) != 0) {
                    g.drawImage(resizeHandle, x+(width-iconWidth)/2, y+height, null);
                }
            }
        }
    }

    private void paintLayoutInnerSelection(Graphics2D g) {
        LayoutDesigner layoutDesigner = formDesigner.getLayoutDesigner();
        if (layoutDesigner != null) {
            Component topComp = formDesigner.getTopDesignComponentView();
            if (topComp != null && topComp.getParent() != null) {
                Point convertPoint = convertPointFromComponent(0, 0, topComp);
                g.translate(convertPoint.x, convertPoint.y);
                Color oldColor = g.getColor();
                g.setColor(formSettings.getGuidingLineColor());
                Stroke oldStroke = g.getStroke();
                g.setStroke(getPaintLayoutStroke());
                layoutDesigner.paintSelection(g);
                g.setStroke(oldStroke);
                g.setColor(oldColor);
                g.translate(-convertPoint.x, -convertPoint.y);
            }
        }
    }

    /** Length of the (shortest) lines in <code>ButtonGroup</code> visualization. */
    private static final int BUTTON_GROUP_OFFSET = 5;
    /**
     * Determines whether the primary division (in visualization
     * of <code>ButtonGroup</code>s) should be into columns or rows.
     */
    private static final boolean BUTTON_GROUP_COLUMNS_FIRST = false;
    /**
     * Visualization of <code>ButtonGroup</code>s.
     * 
     * @param g graphics object.
     */
    private void paintButtonGroups(Graphics2D g) {
        Iterator metacomps = formDesigner.getSelectedComponents().iterator();
        Map<Object, java.util.List<AbstractButton>> buttonGroups = null;
        
        // Find buttonGroups of all selected components.
        while (metacomps.hasNext()) {
            RADComponent metacomp = (RADComponent)metacomps.next();
            // Check whether metacomp is a member of some ButtonGroup
            Object buttonGroup = buttonGroupOfComponent(metacomp);
            // Check whether metacomp is some ButtonGroup
            if ((buttonGroup == null) && (metacomp.getBeanInstance() instanceof ButtonGroup)) {
                buttonGroup = metacomp.getBeanInstance();
            }
            if (buttonGroup != null) {
                if (buttonGroups == null) {
                    buttonGroups = new HashMap<Object, java.util.List<AbstractButton>>();
                }
                java.util.List<AbstractButton> members = buttonGroups.get(buttonGroup);
                if (members == null) {
                    members = new LinkedList<AbstractButton>();
                    buttonGroups.put(buttonGroup, members);
                }
            }
        }

        if (buttonGroups != null) {
            // Find all components that use the same button groups as the selected components
            for (RADComponent metacomp : getFormModel().getComponentList()) {
                Object buttonGroup = buttonGroupOfComponent(metacomp);
                if (buttonGroup != null) {
                    java.util.List<AbstractButton> members = buttonGroups.get(buttonGroup);
                    if (members != null) { // Can be null if no button from this group is selected
                        members.add((AbstractButton)formDesigner.getComponent(metacomp));
                    }
                }
            }
            
            // Visualize individual button groups
            for (java.util.List<AbstractButton> buttons : buttonGroups.values()) {
                if (buttons.size() > 1) {
                    Map<AbstractButton, Rectangle> bounds = new IdentityHashMap<AbstractButton, Rectangle>();
                    for (AbstractButton button : buttons) {
                        Point shift = formDesigner.pointFromComponentToHandleLayer(new Point(0,0), button);
                        Rectangle bound = new Rectangle(shift.x, shift.y, button.getWidth(), button.getHeight());
                        bounds.put(button, bound);
                    }
                    paintButtonGroup(g, buttons, BUTTON_GROUP_COLUMNS_FIRST, true, bounds, true);
                }
            }
        }
    }

    /**
     * Returns button group assigned to the given component.
     * 
     * @param metacomp component whose button group should be returned.
     * @return button group assigned to the given component
     * or <code>null</code> if no button group is assigned
     * or if the component is not a subclass of <code>AbstractButton</code>.
     */
    private Object buttonGroupOfComponent(RADComponent metacomp) {
        if (!(metacomp instanceof RADVisualComponent)
                || !formDesigner.isInDesigner((RADVisualComponent)metacomp)) {
            return null;
        }
        if (AbstractButton.class.isAssignableFrom(metacomp.getBeanClass())) {
            FormProperty prop = (FormProperty)metacomp.getPropertyByName("buttonGroup"); // NOI18N
            try {
                return prop.getRealValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Visualizes a button group (or its part).
     * 
     * @param g graphics object.
     * @param buttons buttons in the group.
     * @param columns determines whether the buttons should be divided
     * into columns (or rows) primarily.
     * @param root determines whether the whole button group is visualized or just a part.
     * @param compBounds bounds of buttons in the group.
     * @param lastSuccessful determines whether the last division into columns/rows
     * was successful, see issue 136370
     * @return the lowest x-coordinate (resp. y-coordinate) of the group
     * if column is set to <code>true</code> (resp. <code>false</code>).
     */
    private int paintButtonGroup(Graphics g, java.util.List<AbstractButton> buttons,
            boolean columns, boolean root, Map<AbstractButton, Rectangle> compBounds, boolean lastSuccessful) {
        // Preprocessing of information about starts/ends of individual buttons.
        // maps coordinates to the number of buttons starting/ending at this coordinate
        SortedMap<Integer, int[]> bounds = new TreeMap<Integer, int[]>();
        for (AbstractButton button : buttons) {
            Rectangle bound = compBounds.get(button);
            int start, end;
            if (columns) {
                start = bound.x;
                end = start + bound.width;
            } else {
                start = bound.y;
                end = start + bound.height;
            }
            paintButtonGroupInsertCount(bounds, start, true);
            paintButtonGroupInsertCount(bounds, end, false);
        }

        // Find cuts
        java.util.List<Integer> cuts = new LinkedList<Integer>();
        int between = 0;
        for (Map.Entry<Integer, int[]> entry : bounds.entrySet()) {
            int[] counts = entry.getValue();
            between -= counts[1];
            if ((between <= 0) && (counts[0] > 0)) {
                cuts.add(entry.getKey());
            }
            between += counts[0];
        }

        // Cut into sub-groups
        java.util.List<AbstractButton>[] groups = new java.util.List[cuts.size()];
        for (int i=0; i<cuts.size(); i++) {
            groups[i] = new LinkedList<AbstractButton>();
        }
        for (AbstractButton button : buttons) {
            Rectangle bound = compBounds.get(button);
            int start = columns ? bound.x : bound.y;
            int index = 0;
            for (Integer cut : cuts) {
                if (cut > start) {
                    break;
                } else {
                    index++;
                }
            }
            groups[--index].add(button);
        }

        // Issue 136370, this set of components cannot be separated neither
        // into columns not into rows. We split them manually into singletons.
        if (!lastSuccessful && (cuts.size() == 1)) {
            return -1;
        }
        
        // Visualize sub-groups
        int[] starts;
        int minStart;
        boolean ok = false;
        out: do {
            starts = new int[cuts.size()];
            minStart = Integer.MAX_VALUE;
            boolean succesful = (cuts.size() > 1);
            for (int i=0; i<cuts.size(); i++) {
                if (groups[i].size() > 1) {
                    starts[i] = paintButtonGroup(g, groups[i], !columns, root && !succesful, compBounds, succesful);
                    if (starts[i] == -1) { // Issue 136370
                        assert (cuts.size() == 1);
                        // Cuts must be sorted
                        java.util.List<int[]> cutOrder = new ArrayList<int[]>(buttons.size());
                        for (int j=0; j<buttons.size(); j++) {
                            AbstractButton button = buttons.get(j);
                            Rectangle bound = compBounds.get(button);
                            cutOrder.add(new int[] {columns ? bound.x : bound.y, j});
                        }
                        cutOrder.sort(new Comparator<int[]>() {
                            @Override
                            public int compare(int[] i1, int[] i2) {
                                return (i1[0] == i2[0]) ? (i1[1] - i2[1]) : (i1[0] - i2[0]);
                            }
                        });
                        cuts = new ArrayList<Integer>(buttons.size());
                        groups = new java.util.List[buttons.size()];
                        for (int[] ii : cutOrder) {
                            AbstractButton button = buttons.get(ii[1]);
                            groups[cuts.size()] = Collections.singletonList(button);
                            cuts.add(ii[0]);
                        }
                        continue out;
                    }
                } else {
                    Rectangle bound = compBounds.get(groups[i].get(0));
                    starts[i] = columns ? bound.y : bound.x;
                }
                if (minStart > starts[i]) {
                    minStart = starts[i];
                }
            }
            minStart -= BUTTON_GROUP_OFFSET;
            ok = true;
        } while (!ok);
        
        // Visualize connection of sub-groups
        int count = 0;
        int min = 0;
        int max = 0;
        for (Integer cut : cuts) {
            int off = 0;
            if (groups[count].size() == 1) {
                AbstractButton button = groups[count].get(0);
                Rectangle bound = compBounds.get(button);
                if ((button instanceof JRadioButton) || (button instanceof JCheckBox)) {
                    Dimension dim = button.getPreferredSize();
                    Insets insets = button.getInsets();
                    int textPos = columns ? button.getHorizontalTextPosition() : button.getVerticalTextPosition();
                    Icon icon = button.getIcon();
                    if (icon == null) {
                        icon = UIManager.getIcon((button instanceof JRadioButton) ? "RadioButton.icon" : "CheckBox.icon"); // NOI18N
                    }
                    if (icon != null) {
                        off = columns ? icon.getIconWidth() : icon.getIconHeight();
                        off /= 2;
                        if ((textPos == SwingConstants.LEADING) || (textPos == SwingConstants.TOP) || (textPos == SwingConstants.LEFT)) {
                            off = (columns ? dim.width : dim.height) - off;
                            off -= columns ? insets.right : insets.bottom;
                        } else if (textPos == SwingConstants.CENTER) {
                            off = columns ? (dim.width + insets.left - insets.right)/2
                                    : (dim.height + insets.top - insets.bottom)/2;
                        } else {
                            off += columns ? insets.left : insets.top;
                        }
                    }
                    starts[count] += columns ? insets.top : insets.left;
                    int diff = columns ? (bound.width - dim.width) : (bound.height - dim.height);
                    int alignment = columns ? button.getHorizontalAlignment() : button.getVerticalAlignment();
                    if ((alignment == SwingConstants.TRAILING) || (alignment == SwingConstants.BOTTOM) || (alignment == SwingConstants.RIGHT)) {
                        off += diff;
                    } else if (alignment == SwingConstants.CENTER) {
                        off += diff/2;
                    }
                    int oppDiff = columns ? (bound.height - dim.height) : (bound.width - dim.width);
                    int oppAlignment = columns ? button.getVerticalAlignment() : button.getHorizontalAlignment();
                    if ((oppAlignment == SwingConstants.TRAILING) || (oppAlignment == SwingConstants.BOTTOM) || (oppAlignment == SwingConstants.RIGHT)) {
                        starts[count] += oppDiff;
                    } else if (oppAlignment == SwingConstants.CENTER) {
                        starts[count] += oppDiff/2;
                    }
                } else {
                    off = (columns ? bound.width : bound.height)/2;
                }
            } else {
                off = -BUTTON_GROUP_OFFSET;
            }
            if (count == 0) {
                min = cut + off;
            }
            if (count == cuts.size()-1) {
                max = cut + off;
            }
            if (cuts.size() > 1) {
                if (columns) {
                    g.drawLine(cut + off, starts[count], cut + off, minStart);
                } else {
                    g.drawLine(starts[count], cut + off, minStart, cut + off);
                }
            }
            count++;
        }
        if (cuts.size() > 1) {
            if (!root) {
                min = bounds.firstKey();
            }
            if (columns) {
                g.drawLine(min, minStart, max, minStart);
            } else {
                g.drawLine(minStart, min, minStart, max);
            }
        }

        return bounds.firstKey();
    }

    // Helper method of paintButtonGroup()
    private void paintButtonGroupInsertCount(SortedMap<Integer, int[]> bounds, int value, boolean start) {
        int[] counts = bounds.get(value);
        if (counts == null) {
            counts = new int[2];
            bounds.put(value, counts);
        }
        counts[start ? 0 : 1]++;
    }
    
    private Image resizeHandle() {
        if (resizeHandle == null) {
            resizeHandle = ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/resize_handle.png", false).getImage(); // NOI18N
        }
        return resizeHandle;
    }

    // paint strokes cached
    private static int lastPaintWidth = -1;
    private Stroke paintSelectionStroke;
    private Stroke paintLayoutStroke;

    private Stroke getPaintSelectionStroke() {
        int width = formSettings.getSelectionBorderSize();
        if (lastPaintWidth != width) {
            paintSelectionStroke = null;
        }
        if (paintSelectionStroke == null) {
            paintSelectionStroke = new BasicStroke(width);
            lastPaintWidth = width;
        }
        return paintSelectionStroke;
    }

    private Stroke getPaintLayoutStroke() {
        if (paintLayoutStroke == null) {
            paintLayoutStroke = new BasicStroke(1);
        }
        return paintLayoutStroke;
    }

    void maskDraggingComponents() {
        if (draggedComponent != null) {
            draggedComponent.maskDraggingComponents();
        }
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_TAB || e.getKeyChar() == '\t') {
            if (!e.isControlDown() && !e.isMetaDown()) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    RADComponent nextComp = formDesigner.getNextVisualComponent(
                                                              !e.isShiftDown());
                    if (nextComp != null)
                        formDesigner.setSelectedComponent(nextComp);
                }
                e.consume();
                return;
            }
        }
        else if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_F2) {
            if (!viewOnly && e.getID() == KeyEvent.KEY_RELEASED) {
                java.util.List selected = formDesigner.getSelectedComponents();
                if (selected.size() == 1) { // just one component is selected
                    RADComponent comp = (RADComponent) selected.get(0);
                    if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT) {
                        // in selection mode SPACE starts in-place editing
                        formDesigner.startInPlaceEditing(comp);
                    }
//                    else if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
//                        // in add mode SPACE adds selected item as component
//                        PaletteItem item = CPManager.getDefault().getSelectedItem();
//                        if (item != null) {
//                            formDesigner.getModel().getComponentCreator()
//                                .createComponent(item.getComponentClassSource(),
//                                                 comp,
//                                                 null);
//                            formDesigner.toggleSelectionMode();
//                        }
//                    }
                }
            }
            e.consume();
            return;
        }
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            if (formDesigner.getDesignerMode() != FormDesigner.MODE_SELECT) {
                formDesigner.toggleSelectionMode(); // also calls endDragging(null)
                repaint();
                e.consume();
                return;
            }
            if (endDragging(null)) {
                repaint();
                e.consume();
                return;
            }
        } else if ((keyCode == KeyEvent.VK_CONTEXT_MENU)
                || ((keyCode == KeyEvent.VK_F10) && e.isShiftDown())) { // Shift F10 invokes context menu
            Point p = null;
            java.util.List selected = formDesigner.getSelectedComponents();
            if (selected.size() > 0) {
                RADComponent metacomp = (RADComponent) selected.get(0);
                Object sel = (Component) formDesigner.getComponent(metacomp);
                if (sel instanceof Component) {
                    Component comp = (Component) sel;
                    p = convertPointFromComponent(comp.getLocation(), comp.getParent());
                }
                else p = new Point(6, 6);

                showContextMenu(p);
                e.consume();
                return;
            }
        } else if (e.getID() == KeyEvent.KEY_PRESSED
                && (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP
                    || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)) {
            // cursor keys
            if ((e.isControlDown() || e.isMetaDown()) && !e.isAltDown() && !e.isShiftDown()) {
                // duplicating
                if (!getFormModel().hasPendingEvents()) { // don't duplicate when events from previous duplication might not be processed yet
                    DuplicateAction.performAction(formDesigner.getSelectedNodes(), keyCode);
                }
                e.consume();
                return;
            }
        } else if (((keyCode == KeyEvent.VK_D) || (keyCode == KeyEvent.VK_E)) && e.isAltDown() && e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED)) {
            FormModel formModel = formDesigner.getFormModel();
            LayoutModel layoutModel = formModel.getLayoutModel();
            if (layoutModel != null) {
                Map<String,String> idToNameMap = new HashMap<String,String>();
                for (RADComponent comp : formModel.getAllComponents()) {
                    if (comp != formModel.getTopRADComponent())
                        idToNameMap.put(comp.getId(), comp.getName());
                }
                RADComponent top = formDesigner.getTopDesignComponent();
                System.out.println(
                        layoutModel.dump(idToNameMap, top != null ? top.getId() : null, e.isShiftDown()));
            }
        } else if (((keyCode == KeyEvent.VK_W)) && e.isAltDown() && e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED)) {
            // generate layout test (one checkpoint)
            if (formDesigner.getLayoutDesigner().logTestCode()) {
                FormModel formModel = formDesigner.getFormModel();
                LayoutModel layoutModel = formModel.getLayoutModel();
                if (layoutModel != null) {
                    Map<String,String> idToNameMap = new HashMap<String,String>();
                    for (RADComponent comp : formModel.getAllComponents()) {
                        idToNameMap.put(comp.getId(), comp.getName());
                    }
                    FormDataObject formDO = formDesigner.getFormEditor().getFormDataObject();
                    LayoutTestUtils.writeTest(formDesigner, formDO, idToNameMap, layoutModel);
                    LayoutDesigner ld = formDesigner.getLayoutDesigner();
                    ld.setModelCounter(ld.getModelCounter() + 1);
                }
            }
        } else if (((keyCode == KeyEvent.VK_S)) && e.isAltDown() && e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED)) {
            // start layout test recording
            if (LayoutDesigner.testMode()) {
                FormDataObject formDO = formDesigner.getFormEditor().getFormDataObject();
                FileObject formFile = formDO.getFormFile();
                SaveCookie saveCookie = formDO.getCookie(SaveCookie.class);
                try {
                    if (saveCookie != null)
                        saveCookie.save();
                    FileObject copied = formFile.copy(LayoutTestUtils.getTargetFolder(formFile), 
                                formFile.getName() + "Test-StartingForm", // NOI18N
                                formFile.getExt()); 
                    formDesigner.getLayoutDesigner().setModelCounter(0);
                    formDesigner.resetTopDesignComponent(true);
                    StatusDisplayer.getDefault().setStatusText("The form was successfully copied to: " + FileUtil.getFileDisplayName(copied)); // NOI18N
                } catch (IOException ioe) {
                    //TODO
                }
            }
        } else if ((keyCode == KeyEvent.VK_R) && e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED)) {
            FormEditor formEditor = formDesigner.getFormEditor();
            EditorSupport editorSupport = formEditor.getEditorSupport();
            editorSupport.reloadForm();
            e.consume();
        }

        super.processKeyEvent(e);
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    // -------

    /**
     * Returns metacomponent for visual component at given location.
     * @param point - location in component layer's coordinates
     * @param mode - defines what level in the hierarchy to prefer (in order to
     *        distinguish between the leaf components and their parents):
     *   COMP_DEEPEST - get the component which is the deepest in the hierarchy (leaf component)
     *   COMP_SELECTED - get the deepest selected component
     *   COMP_ABOVE_SELECTED - get the component above the deepest selected component
     *   COMP_UNDER_SELECTED - get the component under the deepest selected component
     * @returns the metacomponent at given point
     *   If no component is currently selected then:
     *     for COMP_SELECTED the deepest component is returned
     *     for COMP_ABOVE_SELECTED the deepest component is returned
     *     for COMP_UNDER_SELECTED the top component is returned
     */
    public RADComponent getMetaComponentAt(Point point, int mode) {
        Component[] deepComps = getDeepestComponentsAt(
                                    formDesigner.getComponentLayer(), point);
        if (deepComps == null) {
            return null;
        }

        int dIndex = 0;
        Component comp = deepComps[dIndex];

        // find the component satisfying point and mode
        RADComponent topMetaComp = formDesigner.getTopDesignComponent();
        RADComponent firstMetaComp = null;
        RADComponent currMetaComp;
        RADComponent prevMetaComp = null;

        do {
            currMetaComp = formDesigner.getMetaComponent(comp);
            if (currMetaComp != null && !isDraggedComponent(currMetaComp)) {
                if (firstMetaComp == null)
                    firstMetaComp = currMetaComp;

                switch (mode) {
                    case COMP_DEEPEST: 
                        return currMetaComp;

                    case COMP_SELECTED:
                        if (formDesigner.isComponentSelected(currMetaComp))
                            return currMetaComp;
                        if (currMetaComp == topMetaComp)
                            return firstMetaComp; // nothing selected - return the deepest
                        break;

                    case COMP_ABOVE_SELECTED:
                        if (prevMetaComp != null 
                                && formDesigner.isComponentSelected(prevMetaComp))
                            return currMetaComp;
                        if (currMetaComp == topMetaComp)
                            return firstMetaComp; // nothing selected - return the deepest
                        break;

                    case COMP_UNDER_SELECTED:
                        if (formDesigner.isComponentSelected(currMetaComp))
                            return prevMetaComp != null ?
                                     prevMetaComp : topMetaComp;
                        if (currMetaComp == topMetaComp)
                            return topMetaComp; // nothing selected - return the top
                        break;
                }

                prevMetaComp = currMetaComp;
            }

            comp = dIndex + 1 < deepComps.length ?
                   deepComps[++dIndex] : comp.getParent();
        }
        while (comp != null);

        return firstMetaComp;
    }

    private static Component[] getDeepestComponentsAt(Container parent,
                                                      Point point)
    {
        Component deepestComp = SwingUtilities.getDeepestComponentAt(parent, point.x, point.y);
        if (deepestComp == null) {
            return null;
        }

        Container deepestParent = deepestComp.getParent();
        Point deepestPosition = SwingUtilities.convertPoint(parent, point, deepestParent);
        java.util.List<Component> compList = null; // in most cases there will be just one component
        for (int i=0, n=deepestParent.getComponentCount(); i < n; i++) {
            Component comp = deepestParent.getComponent(i);
            Point p = comp.getLocation();
            if (comp != deepestComp && comp.isVisible()
                    && comp.contains(deepestPosition.x - p.x, deepestPosition.y - p.y)) {
                if (compList == null) {
                    compList = new ArrayList<Component>(n - i + 1);
                    compList.add(deepestComp);
                }
                compList.add(comp);
            }
        }

        if (compList == null) { // just one component
            return new Component[] { deepestComp };
        } else {
            return compList.toArray(new Component[0]);
        }
    }

    private RADVisualContainer getMetaContainerAt(Point point, int mode) {
        RADComponent metacomp = getMetaComponentAt(point, mode);
        if (metacomp == null)
            return null;
        if (metacomp instanceof RADVisualContainer)
            return (RADVisualContainer) metacomp;
        if (metacomp instanceof RADVisualComponent)
            return (RADVisualContainer) metacomp.getParentComponent();
        return null;
    }

    /** Selects component at the position e.getPoint() on component layer.
     * What component is selected further depends on whether CTRL or ALT
     * keys are hold. */
    private RADComponent selectComponent(MouseEvent e, boolean mousePressed) {
        RADComponent hitMetaComp;
        if (formDesigner.getSelectedComponents().size() > 1
                && mousePressed && !extraModifier(e)) {
            // If multiple components already selected and some of them is on
            // current mouse position, keep this component selected on mouse
            // pressed (i.e. don't try to selected a possible subcomponent).
            // This is to ease dragging of multiple scrollpanes or containers
            // covered entirely by subcomponents.
            // OTOH mouse release should cancel the multiselection - if no
            // dragging happened.
            hitMetaComp = selectedComponentAt(e.getPoint(), 0, true);
            if (hitMetaComp != null) {
                return hitMetaComp;
            }
        }

        int selMode = !e.isAltDown() ? COMP_DEEPEST :
                (!e.isShiftDown() ? COMP_ABOVE_SELECTED : COMP_UNDER_SELECTED);
        hitMetaComp = getMetaComponentAt(e.getPoint(), selMode);

        // Help with selecting a component in scroll pane (e.g. JTable of zero size).
        // Prefer selecting the component rather than the scrollpane if the view port
        // or header is clicked.
        if (hitMetaComp != null && !e.isAltDown()
                && hitMetaComp.getAuxValue("autoScrollPane") != null // NOI18N
                && hitMetaComp instanceof RADVisualContainer) {
            RADVisualComponent[] sub = ((RADVisualContainer)hitMetaComp).getSubComponents();
            Component scroll = (Component) formDesigner.getComponent(hitMetaComp);
            if (sub.length > 0 && scroll instanceof JScrollPane) {
                Point p = e.getPoint();
                convertPointToComponent(p, scroll);
                Component clicked = SwingUtilities.getDeepestComponentAt(scroll, p.x, p.y);
                while (clicked != null && clicked != scroll) {
                    if (clicked instanceof JViewport) {
                        hitMetaComp = sub[0];
                        break;
                    }
                    clicked = clicked.getParent();
                }
            }
        }

        if (!e.isAltDown() && (e.isShiftDown() || ctrlOrCmdModifier(e))) {
            if (hitMetaComp != null) {
                // Shift adds to selection, Ctrl toggles selection,
                // other components selection is not affected
                if (!formDesigner.isComponentSelected(hitMetaComp)) {
                    formDesigner.addComponentToSelection(hitMetaComp);
                } else if (!e.isShiftDown()) {
                    formDesigner.removeComponentFromSelection(hitMetaComp);
                }
            }
        } else if (hitMetaComp != null) {
            formDesigner.setSelectedComponent(hitMetaComp);
        } else {
            formDesigner.clearSelection();
        }

        return hitMetaComp;
    }

    private static boolean extraModifier(MouseEvent e) {
        return e.isShiftDown()
            || e.isAltDown()
        // Mac: Ctrl not extra if used for popup menu
            || (e.isControlDown() && (!e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3))
        // Mac: don't confuse Command key with right mouse (InputEvent.META_MASK == InputEvent.BUTTON3_MASK)
            || (e.isMetaDown() && e.getButton() != MouseEvent.BUTTON3 && (e.getModifiersEx()&InputEvent.BUTTON3_DOWN_MASK) != InputEvent.BUTTON3_DOWN_MASK);
    }

    private static boolean ctrlOrCmdModifier(MouseEvent e) {
        // assuming this is asked only for a mouse press event
        if (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() == InputEvent.META_MASK) { // on Mac
            return (e.getModifiersEx() & InputEvent.META_DOWN_MASK) == InputEvent.META_DOWN_MASK;
        }
        return (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK;
    }

/*    private RADComponent[] getComponentsIntervalToSelect(
                             RADComponent clickedComp,
                             boolean forward)
    {
        if (!(clickedComp instanceof RADVisualComponent))
            return null;

        java.util.List toSelect = new LinkedList();
        RADVisualComponent comp = (RADVisualComponent) clickedComp;
        boolean selected = false;

        do  // starting with clickedComp,
        {   // go forward/backward in components until a selected one is reached
            if (forward)
                toSelect.add(comp);
            else
                toSelect.add(0, comp);

            comp = formDesigner.getNextVisualComponent(comp, forward);
        }
        while (comp != null && comp != clickedComp
               && !(selected = formDesigner.isComponentSelected(comp))
               && comp != formDesigner.getTopDesignComponent());

        if (selected) { // selected component found - we can make the interval
            if (comp == formDesigner.getTopDesignComponent()) {
                if (!forward) // top component is fine when going backward
                    toSelect.add(0, comp);
            }
            else { // add also already selected components in the direction
                selected = false;
                do {
                    if (forward)
                        toSelect.add(comp);
                    else
                        toSelect.add(0, comp);

                    comp = formDesigner.getNextVisualComponent(comp, forward);
                }
                while (comp != null
                       && (selected = formDesigner.isComponentSelected(comp))
                       && comp != formDesigner.getTopDesignComponent());

                if (selected && !forward)
                    toSelect.add(0, comp); // top comp is fine when going backward
            }            

            RADComponent[] compArray = new RADComponent[toSelect.size()];
            toSelect.toArray(compArray);
            return compArray;
        }

        return null;
    } */

    private void selectOtherComponentsNode() {
        formDesigner.setSelectedNodes(formDesigner.getFormEditor().getOthersContainerNode());
    }

    private boolean processDoubleClick(MouseEvent e) {
        if (e.isShiftDown() || e.isControlDown() || e.isMetaDown()) {
            return false;
        }

        RADComponent metacomp = getMetaComponentAt(e.getPoint(), COMP_SELECTED);
        if (metacomp == null) {
            return true;
        }

        if (e.isAltDown()) {
            if (metacomp == formDesigner.getTopDesignComponent()) {
                metacomp = metacomp.getParentComponent();
                if (metacomp == null) {
                    return true;
                }
            } else {
                 return false;
            }
        }

        Node node = metacomp.getNodeReference();
        if (node != null) {
            Action action = node.getPreferredAction();
            if (action != null) {// && action.isEnabled()) {
                action.actionPerformed(new ActionEvent(
                        node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                prevLeftMousePoint = null; // to prevent inplace editing on mouse release
                return true;
            }
        }

        return false;
    }

    private void processMouseClickInLayout(RADComponent metacomp, MouseEvent e) {
        if(formDesigner.getMenuEditLayer().isVisible()) {
            if(!formDesigner.getMenuEditLayer().isMenuLayerComponent(metacomp)) {
                formDesigner.getMenuEditLayer().hideMenuLayer();
            }
        }
        if(metacomp != null && metacomp.getBeanClass().getName().equals(javax.swing.JMenu.class.getName())) {
            formDesigner.openMenu(metacomp);
        }
        if (!(metacomp instanceof RADVisualComponent)) {
            return;
        }

        selectTabInUnknownTabbedPane((RADVisualComponent)metacomp, e.getPoint());

        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            (RADVisualContainer) metacomp.getParentComponent();
        if (metacont == null) {
            return;
        }
        LayoutSupportManager laysup = metacont.getLayoutSupport();
        if (laysup == null) {
            Point p = convertPointToComponent(e.getPoint(), formDesigner.getTopDesignComponentView());
            if (formDesigner.getLayoutDesigner().selectInside(p)) {
                FormEditor.getAssistantModel(getFormModel()).setContext("layoutGaps", "selectedLayoutGaps"); // NOI18N
                repaint();
                mouseHint = formDesigner.getLayoutDesigner().getToolTipText(p);
                showToolTip(e);
            }
        } else {
            Container cont = (Container) formDesigner.getComponent(metacont);
            Container contDelegate = metacont.getContainerDelegate(cont);
            Point p = convertPointToComponent(e.getPoint(), contDelegate);
            laysup.processMouseClick(p, cont, contDelegate);
        }
    }

    // Even if it's not our JTabbedPane container, we may allow the user to select its tab.
    private void selectTabInUnknownTabbedPane(RADVisualComponent metacomp, Point p) {
        if (metacomp instanceof RADVisualContainer) {
            LayoutSupportManager laysup = ((RADVisualContainer)metacomp).getLayoutSupport();
            if (laysup != null && laysup.getSupportedClass() != null && JTabbedPane.class.isAssignableFrom(laysup.getSupportedClass())) {
                return; // our support for tabbed pane will handle the selection
            }
        }
        Component[] clicked = getDeepestComponentsAt(formDesigner.getComponentLayer(), p);
        if (clicked != null && clicked.length > 0 && clicked[0] instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane)clicked[0];
            p = convertPointToComponent(p, tabbedPane);
            for (int i=0,n=tabbedPane.getTabCount(); i < n; i++) {
                Rectangle rect = tabbedPane.getBoundsAt(i);
                if (rect != null && rect.contains(p)) {
                    tabbedPane.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void showContextMenu(Point popupPos) {
        formDesigner.componentActivated(); // just for sure...

        Node[] selectedNodes = formDesigner.getSelectedNodes();
        JPopupMenu popup = NodeOp.findContextMenu(selectedNodes);
        if (popup != null) {
            popup.show(HandleLayer.this, popupPos.x, popupPos.y);
        }
    }

    // --------

    private boolean anyDragger() {
        return draggedComponent != null || selectionDragger != null;
    }

    private RADVisualComponent[] getComponentsToDrag() {
        assert (resizeType & DESIGNER_RESIZING) == 0;
        // all selected components must be visible in the designer and have the
        // same parent; redundant sub-contained components must be filtered out
        java.util.List<RADComponent> selectedComps = formDesigner.getSelectedComponents();
        java.util.List<RADComponent> workingComps = new ArrayList<RADComponent>(selectedComps.size());
        java.util.List<String> workingIds = null;
        RADVisualContainer parent = null;

 	//outside of a frame, there are no selected components so just return null
 	if(selectedComps.isEmpty()) return null;

        for (Iterator it = selectedComps.iterator(); it.hasNext(); ) {
            RADComponent sc = (RADComponent) it.next();
            if (!(sc instanceof RADVisualComponent)) {
                continue;
            }
            RADVisualComponent metacomp = (RADVisualComponent) sc;

            boolean subcontained = false;
            for (Iterator it2 = selectedComps.iterator(); it2.hasNext(); ) {
                RADComponent metacomp2 = (RADComponent) it2.next();
                if (metacomp2 != metacomp && metacomp2.isParentComponent(metacomp)) {
                    subcontained = true;
                    break;
                }
            }
            if (!subcontained) {
                if ((resizeType & INBOUND_RESIZING) != 0) {
                    // If trying to resize a layout gap in a container that is in scrollpane,
                    // make sure the enclosed container is used, not the scrollpane.
                    metacomp = FormDesigner.substituteWithSubComponent(metacomp);
                } else { // Otherwise, if trying to drag something in scrollpane,
                         // drag the whole scrollpane instead.
                    metacomp = formDesigner.substituteWithContainer(metacomp);
                }
                RADVisualContainer metacont = (RADVisualContainer) metacomp.getParentComponent();

                if (parent != null) {
                    if (parent != metacont)
                        return null; // components in different containers
                } else {
                    if ((resizeType & INBOUND_RESIZING) != 0 && isNewLayoutRootSelection(false)) {
                        metacont = (RADVisualContainer) metacomp;
                    } else if (metacont == null || !formDesigner.isInDesigner(metacont)) {
                        return null; // out of visible tree
                    }
                    parent = metacont;
                    if (metacont.getLayoutSupport() == null) { // new layout
                        workingIds = new ArrayList<String>(selectedComps.size());
                    }
                }
                workingComps.add(metacomp);
                if (workingIds != null) {
                    workingIds.add(metacomp.getId());
                }
            }
        }

        if (parent != null && parent.getLayoutSupport() == null) { // new layout may impose more limitation
            workingIds = formDesigner.getLayoutDesigner().getDraggableComponents(workingIds);
            if (workingIds.size() != workingComps.size()) {
                workingComps.clear();
                FormModel formModel = getFormModel();
                for (String compId : workingIds) {
                    workingComps.add(formModel.getMetaComponent(compId));
                }
            }
        }

        return workingComps.isEmpty() ? null :
            workingComps.toArray(new RADVisualComponent[0]);
    }

    boolean endDragging(MouseEvent e) {
        if (!anyDragger())
            return false;

        if (resizeType != 0) {
            resizeType = 0;
            Cursor cursor = getCursor();
            if (cursor != null && cursor.getType() != Cursor.DEFAULT_CURSOR)
                setCursor(Cursor.getDefaultCursor());
            if (getToolTipText() != null)
                setToolTipText(null);
        }

        boolean done = true;

        if (draggedComponent != null) {            
            boolean retVal = true;
            try {
                retVal = draggedComponent.end(e);
            } finally {
                if (retVal) {
                    draggedComponent = null;
                    draggingEnded = true;
                    repaint();
                } else {
                    done = false;
                }
            }
        }
        else if (selectionDragger != null) {
            if (e != null)
                selectionDragger.drop(e.getPoint());
            selectionDragger = null;
//                repaint();
        }

        if (done) {
            draggingEnded = true;
            StatusDisplayer.getDefault().setStatusText(""); // NOI18N
        }

        FormEditor.getAssistantModel(getFormModel()).setContext("select"); // NOI18N
        return done;
    }

    private boolean isDraggedComponent(RADComponent metacomp) {
        if (draggedComponent != null && draggedComponent.movingComponents != null) {
            for (RADComponent c : draggedComponent.movingComponents) {
                if (c == metacomp || c.isParentComponent(metacomp))
                    return true;
            }
        }
        return false;
    }

    // Highlighted panel
    private JPanel darkerPanel = null;
    private static class HighlightBorder extends javax.swing.border.LineBorder {
        HighlightBorder(Color color, int thickness) {
            super(color, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // Hack - don't affect component's content
            return new Insets(0, 0, 0, 0);
        }
    }
    
    // Highlights panel below mouse cursor.
    private void highlightPanel(MouseEvent e, boolean recheck) {
        Component[] comps = getDeepestComponentsAt(formDesigner.getComponentLayer(), e.getPoint());
        if (comps == null) {
            return;
        }
        Component comp = comps[comps.length-1];
        RADComponent radcomp = formDesigner.getMetaComponent(comp);
        if ((radcomp != null) && !(radcomp instanceof RADVisualContainer)) {
            radcomp = radcomp.getParentComponent();
            comp = radcomp != null ? (Component)formDesigner.getComponent(radcomp) : null;
        }
        if ((radcomp == null) || (radcomp == formDesigner.getTopDesignComponent())
            || (!(comp instanceof JPanel))) {
            comp = null;
        }
        JPanel panel = (JPanel)comp;
        if ((darkerPanel != panel) || (recheck && !shouldHighlightPanel(panel, radcomp))) {
            if (darkerPanel != null) {
                // Reset only HighlightBorder border
                if (darkerPanel.getBorder() instanceof HighlightBorder) {
                    darkerPanel.setBorder(null);
                }
                darkerPanel = null;
            }
            if (shouldHighlightPanel(panel, radcomp)) {
                panel.setBorder(new HighlightBorder(darkerPanelColor(panel.getBackground()), 1));
                darkerPanel = panel;
            }
        }
    }
    
    private boolean shouldHighlightPanel(JPanel panel, RADComponent radPanel) {
        if (panel != null) {
            if (panel.getBorder() != null) { // Maybe we should highlight also panels with EmptyBorder
                return false;
            }
            if (!(panel.getBackground() instanceof javax.swing.plaf.UIResource)) {
                return false;
            }
            if (radPanel == formDesigner.getTopDesignComponent()) {
                return false;
            }
            if ((formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT)
                && formDesigner.getSelectedLayoutComponents().contains(radPanel)) {
                return false;
            }
            if (radPanel instanceof RADVisualContainer) {
                RADVisualContainer metacont = (RADVisualContainer)radPanel;
                RADVisualContainer parent = metacont.getParentContainer();
                if (parent != null) {
                    LayoutSupportManager manager = parent.getLayoutSupport();
                    if ((manager != null) && manager.isDedicated()) {
                        return false;
                    }
                    JPanel realPanel = (JPanel)formDesigner.getComponent(radPanel);
                    Component parentBean = (Component)parent.getBeanInstance();
                    Component realParent = (Component)formDesigner.getComponent(parent);
                    if ((realPanel != null) && (realParent != null)
                            && realParent.getSize().equals(realPanel.getSize())
                            && realPanel.getLocation().equals(new Point(0,0))) {
                        if (parentBean instanceof JPanel) {
                            return shouldHighlightPanel((JPanel)parentBean, parent);
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return (panel != null);
    }
    
    private static Color darkerPanelColor(Color color) {
        double factor = 0.9;
	return new Color((int)(color.getRed()*factor), 
			 (int)(color.getGreen()*factor),
			 (int)(color.getBlue()*factor));
    }

    // Check the mouse cursor if it is at position where a component or the
    // designer can be resized. Change mouse cursor accordingly.
    private void checkResizing(MouseEvent e) {
        if (formDesigner.getTopDesignComponent() == null) {
            return;
        }

        resizeType = 0;
        int resizing = 0;
        if (!extraModifier(e)) {
            Point p = e.getPoint();
            boolean compInLayout = selectedComponentsInSameVisibleContainer();
            if (compInLayout || isNewLayoutRootSelection(false)) {
                resizing = checkLayoutResizing(p);
            }
            if (resizing == 0 && compInLayout) {
                resizing = checkComponentsResizing(p);
            }
            if (resizing == 0) {
                resizing = checkDesignerResizing(p);
            }
        }

        if (resizing != 0 && !viewOnly) {
            setResizingCursor(resizing);
        } else {
            Cursor cursor = getCursor();
            if (cursor != null && cursor.getType() != Cursor.DEFAULT_CURSOR)
                setCursor(Cursor.getDefaultCursor());
        }
    }

    // Check the mouse cursor if it is at position where designer can be
    // resized.
    private int checkDesignerResizing(Point p) {
        ComponentLayer compLayer = formDesigner.getComponentLayer();
        int resizing = getSelectionResizable(p,
                         compLayer.getComponentContainer(),
                         compLayer.getDesignerOutsets().right + 2);

        if (validDesignerResizing(resizing)) {
            resizeType = resizing | DESIGNER_RESIZING;
            if (mouseHint == null) {
                Dimension size = formDesigner.getComponentLayer().getDesignerSize();
                MessageFormat mf;
                if(viewOnly) {
                    if (sizeHintFormat == null){                    
                        sizeHintFormat = new MessageFormat(
                            FormUtils.getBundleString("FMT_HINT_DesignerSize")); // NOI18N                                            
                    }                                           
                    mf = sizeHintFormat;                    
                } else {
                    if (resizingHintFormat == null){                    
                        resizingHintFormat = new MessageFormat(
                            FormUtils.getBundleString("FMT_HINT_DesignerResizing")); // NOI18N                                            
                    } 
                    mf = resizingHintFormat;                                        
                }
                mouseHint = mf.format(new Object[] { Integer.valueOf(size.width),
                                                     Integer.valueOf(size.height) });
            }
        } else {
            resizeType = 0;
        }

        return resizeType;
    }

    // Check whether given resize type is valid for designer.
    private boolean validDesignerResizing(int resizing) {
        return resizing == (LayoutSupportManager.RESIZE_DOWN
                            | LayoutSupportManager.RESIZE_RIGHT)
            || resizing == LayoutSupportManager.RESIZE_DOWN
            || resizing == LayoutSupportManager.RESIZE_RIGHT;
    }

    // Check the mouse cursor if it is at position where a component (or more
    // components) can be resized.
    private int checkComponentsResizing(Point p) {
        RADComponent compAtPoint = selectedComponentAt(p, 6, true);

        if (!(compAtPoint instanceof RADVisualComponent)) {
            return 0;
        }
        resizeType = getComponentResizable(p, (RADVisualComponent)compAtPoint);

        return resizeType;
    }

    private int checkLayoutResizing(Point p) {
        LayoutDesigner layoutDesigner = formDesigner.getLayoutDesigner();
        if (layoutDesigner != null) {
            p = convertPointToComponent(new Point(p), formDesigner.getTopDesignComponentView());
            int[] resizability = layoutDesigner.getInnerResizability(p);
            if (resizability != null) {
                if (resizability[LayoutConstants.HORIZONTAL] == LayoutConstants.LEADING) {
                    resizeType |= LayoutSupportManager.RESIZE_LEFT;
                } else if (resizability[LayoutConstants.HORIZONTAL] == LayoutConstants.TRAILING) {
                    resizeType |=  LayoutSupportManager.RESIZE_RIGHT;
                }
                if (resizability[LayoutConstants.VERTICAL] == LayoutConstants.LEADING) {
                    resizeType |= LayoutSupportManager.RESIZE_UP;
                } else if (resizability[LayoutConstants.VERTICAL] == LayoutConstants.TRAILING) {
                     resizeType |= LayoutSupportManager.RESIZE_DOWN;
                }
                resizeType |= INBOUND_RESIZING;
            }
            mouseHint = layoutDesigner.getToolTipText(p);
       }
        return resizeType;
    }

    private boolean selectedComponentsInSameVisibleContainer() {
        RADVisualContainer parent = null;
        Iterator selected = formDesigner.getSelectedComponents().iterator();
        while (selected.hasNext()) {
            RADVisualComponent comp = formDesigner.componentToLayoutComponent((RADComponent)selected.next());
            if (comp == null)
                return false; // not visible in designer
            if (parent == null) {
                parent = comp.getParentContainer();
                if (!formDesigner.isInDesigner(parent)) {
                    return false; // not visible in designer
                }
            }
            else if (comp.getParentContainer() != parent) {
                return false; // different parent
            }
        }
        return true;
    }

    private boolean isNewLayoutRootSelection(boolean checkVisibility) {
        List<RADVisualComponent> selectedLayoutComponents = formDesigner.getSelectedLayoutComponents();
        if (selectedLayoutComponents.size() == 1) {
            RADVisualComponent metacomp = selectedLayoutComponents.get(0);
            metacomp = FormDesigner.substituteWithSubComponent(metacomp);
            if (metacomp instanceof RADVisualContainer
                    && ((RADVisualContainer)metacomp).getLayoutSupport() == null
                    && (!checkVisibility || formDesigner.isInDesigner(metacomp))) {
                return true;
            }
        }
        return false;
    }

    // Returns selected component at the given point (even outside the designer area).
    private RADComponent selectedComponentAt(Point p, int borderSize, boolean inLayout) {
        RADComponent compAtPoint = null;
        Iterator selected = (inLayout ? formDesigner.getSelectedLayoutComponents()
                                      : formDesigner.getSelectedComponents())
                .iterator();
        while (selected.hasNext()) {
            RADComponent metacomp = (RADComponent) selected.next();
            if (metacomp instanceof RADVisualComponent && formDesigner.isInDesigner((RADVisualComponent)metacomp)) {
                Component comp = (Component)formDesigner.getComponent(metacomp);
                Rectangle rect = new Rectangle(-borderSize, -borderSize, comp.getWidth()+2*borderSize, comp.getHeight()+2*borderSize);
                convertRectangleFromComponent(rect, comp);
                if (rect.contains(p)) {
                    compAtPoint = metacomp;
                }
            }
        }
        return compAtPoint;
    }

    // Check how possible component resizing (obtained from layout support)
    // matches with mouse position on component selection border. 
    private int getComponentResizable(Point p, RADVisualComponent metacomp) {
//        RADVisualContainer metacont = metacomp.getParentContainer();
//        if (substituteForContainer(metacont)) {
//            metacomp = metacont;
//            metacont = metacomp.getParentContainer();
//        }

        int resizable = getComponentResizable(metacomp);
        if (resizable != 0) {
            Component comp = (Component) formDesigner.getComponent(metacomp);
            resizable &= getSelectionResizable(p, comp, 6);
        }

        return resizable;
    }
    
    private int getComponentResizable(RADVisualComponent metacomp) {
        RADVisualContainer metacont = metacomp.getParentContainer();
        if (metacont == null || metacomp == formDesigner.getTopDesignComponent()) {
            return 0;
        }
        Component comp = (Component) formDesigner.getComponent(metacomp);
        int resizable = 0;
        LayoutSupportManager laySup = metacont.getLayoutSupport();
        if (laySup == null) { // new layout support
            java.util.List selectedComps = formDesigner.getSelectedComponents();
            if (selectedComps.size() == 1) {
                // [real resizability spec TBD]
                resizable = LayoutSupportManager.RESIZE_LEFT
                            | LayoutSupportManager.RESIZE_RIGHT
                            | LayoutSupportManager.RESIZE_UP
                            | LayoutSupportManager.RESIZE_DOWN;
            }
        }
        else { // old layout support
            Container cont = (Container) formDesigner.getComponent(metacont);
            if (cont != null) { // might be null if component just enclosed in container not yet cloned
                Container contDel = metacont.getContainerDelegate(cont);

                resizable = laySup.getResizableDirections(
                                           cont, contDel,
                                           comp, metacont.getIndexOf(metacomp));
            }
        }
        return resizable;
    }

    // Compute possible resizing directions according to mouse position on
    // component selection border.
    private int getSelectionResizable(Point p, Component comp, int borderWidth) {
        if (comp == null)
            return 0;

        int resizable = 0;

        Rectangle r = new Rectangle(0, 0, comp.getWidth(), comp.getHeight());
        convertRectangleFromComponent(r, comp);
        r.grow(borderWidth, borderWidth);
        if (r.contains(p)) {
            r.grow(-borderWidth, -borderWidth);
            r.grow(-3, -3);
            if (r.width < 0)
                r.width = 0;
            if (r.height < 0)
                r.height = 0;

            if (p.y >= r.y + r.height)
                resizable |= LayoutSupportManager.RESIZE_DOWN;
            else if (p.y < r.y)
                resizable |= LayoutSupportManager.RESIZE_UP;
            if (p.x >= r.x + r.width)
                resizable |= LayoutSupportManager.RESIZE_RIGHT;
            else if (p.x < r.x)
                resizable |= LayoutSupportManager.RESIZE_LEFT;
        }

        return resizable;
    }

    private void setResizingCursor(int resizeType) {
        Cursor cursor = null;
        if ((resizeType & LayoutSupportManager.RESIZE_UP) != 0) {
            if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            else if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            else
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
        }
        else if ((resizeType & LayoutSupportManager.RESIZE_DOWN) != 0) {
            if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            else if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            else
                cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
        }
        else if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0)
            cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
        else if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0)
            cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

        if (cursor == null)
            cursor = Cursor.getDefaultCursor();

        setCursor(cursor);
    }

    private void setUserDesignerSize() {
        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
            FormUtils.getBundleString("CTL_SetDesignerSize_Label"), // NOI18N
            FormUtils.getBundleString("CTL_SetDesignerSize_Title")); // NOI18N
        Dimension size = formDesigner.getComponentLayer().getDesignerSize();
        input.setInputText(Integer.toString(size.width) + ", " // NOI18N
                           + Integer.toString(size.height));

        if (DialogDisplayer.getDefault().notify(input) == NotifyDescriptor.OK_OPTION) {
            String txt = input.getInputText();
            int i = txt.indexOf(',');
            if (i > 0) {
                int n = txt.length();
                try {
                    int w = Integer.parseInt(txt.substring(0, i));
                    while (++i < n && txt.charAt(i) == ' ');
                    int h = Integer.parseInt(txt.substring(i, n));
                    if (w >= 0 && h >= 0) {
                        size = new Dimension(w ,h);
                        formDesigner.setDesignerSize(size, null);
                        setToolTipText(null);
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
                catch (NumberFormatException ex) {} // silently ignore, do nothing
            }
        }
    }

    private Object getConstraintsAtPoint(RADComponent metacomp, Point point, Point hotSpot) {
        if (!(metacomp instanceof RADVisualComponent))
            return null;

        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            (RADVisualContainer) metacomp.getParentComponent();
        LayoutSupportManager laysup = metacont != null ?
                                      metacont.getLayoutSupport() : null;

            Container cont = (Container) formDesigner.getComponent(metacont);
            Container contDel = metacont.getContainerDelegate(cont);
            Point p = convertPointToComponent(point.x, point.y, contDel);
            Object constraints = laysup.getNewConstraints(cont, contDel, null, -1, p, hotSpot);
            if ((constraints == null) && metacomp.getBeanInstance() instanceof Component) {
                int index = laysup.getNewIndex(cont, contDel, (Component)metacomp.getBeanInstance(), -1, p, hotSpot);
                if (index != -1) {
                    constraints = Integer.valueOf(index);
                }
            }
            return constraints;
    }

    // ------

    boolean mouseOnVisual(Point p) {
        Rectangle r = formDesigner.getComponentLayer().getDesignerOuterBounds();
        return r.contains(p);
    }
    
    /**
     * Determines whether the passed point is above the non-visual tray.
     *
     * @return <code>true</code> if the point is above the non-visual tray,
     * returns <code>false</code> otherwise.
     */
    boolean mouseOnNonVisualTray(Point p) {
        Component tray = formDesigner.getNonVisualTray();
        return tray != null ? tray.getBounds().contains(p) : false;
    }

    // NOTE: does not create a new Point instance
    private Point convertPointFromComponent(Point p, Component sourceComp) {
        return formDesigner.pointFromComponentToHandleLayer(p, sourceComp);
    }

    private Point convertPointFromComponent(int x, int y, Component sourceComp) {
        return formDesigner.pointFromComponentToHandleLayer(new Point(x, y), sourceComp);
    }

    // NOTE: does not create a new Point instance
    private Point convertPointToComponent(Point p, Component targetComp) {
        return formDesigner.pointFromHandleToComponentLayer(p, targetComp);
    }

    private Point convertPointToComponent(int x, int y, Component targetComp) {
        return formDesigner.pointFromHandleToComponentLayer(new Point(x, y), targetComp);
    }

    // NOTE: does not create a new Rectangle instance
    private Rectangle convertRectangleFromComponent(Rectangle rect,
                                                    Component sourceComp)
    {
        Point p = convertPointFromComponent(rect.x, rect.y, sourceComp);
        rect.x = p.x;
        rect.y = p.y;
        return rect;
    }

    // NOTE: does not create a new Rectangle instance
    Rectangle convertRectangleToComponent(Rectangle rect,
                                                  Component targetComp)
    {
        Point p = convertPointToComponent(rect.x, rect.y, targetComp);
        rect.x = p.x;
        rect.y = p.y;
        return rect;
    }

    Rectangle convertVisibleRectangleFromComponent(Rectangle rect, Component comp) {
        Component parent;
        while (!formDesigner.isCoordinatesRoot(comp)) {
            parent = comp.getParent();
            Rectangle size = new Rectangle(0, 0, parent.getWidth(), parent.getHeight());
            rect.translate(comp.getX(), comp.getY());
            rect = rect.intersection(size);
            comp = parent;
        }
        comp = this;
        while (!formDesigner.isCoordinatesRoot(comp)) {
            rect.translate(-comp.getX(), -comp.getY());
            comp = comp.getParent();
        }
        return rect;
    }
    
    
    // ---------
    // MouseListener implementation

    @Override
    public void mouseClicked(MouseEvent e) {
        highlightPanel(e, true);
        e.consume();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!HandleLayer.this.isVisible()) {
            return;
        }

        boolean leftMouseReleased = (e.getButton() == MouseEvent.BUTTON1);
        boolean rightMouseDown = (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK;
        boolean popup = e.isPopupTrigger();
        Point p = e.getPoint();

        if (leftMouseReleased && !popup && !rightMouseDown) {
            if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT
                    && !draggingEnded && !endDragging(e)) {
                // there was no dragging
                boolean anyModifier = extraModifier(e);
                if ((resizeType & DESIGNER_RESIZING) != 0 && !viewOnly
                        && e.getClickCount() == 2 && !anyModifier) {
                    // doubleclick on designer's resizing border
                    setUserDesignerSize();
                } else if (mouseOnNonVisualTray(p)) {
                    dispatchToNonVisualTray(e);
                } else if (prevLeftMousePoint != null
                         && prevLeftMousePoint.distance(p) <= 3
                         && !anyModifier) {
                    // second click on the same place in a component
                    RADComponent metacomp = getMetaComponentAt(p, COMP_SELECTED);
                    if (metacomp != null) {
                        formDesigner.startInPlaceEditing(metacomp);
                    }
                } else if (e.getClickCount() == 1 && (!anyModifier || e.isShiftDown())) {
                    // plain click: in case of multiselect to select just one component
                    //              (in mouse press don't know if dragging follows)
                    // or shift click: applied only on release (selection dragging
                    //                 may follow after mouse press)
                    if (mouseOnVisual(p)) {
                        selectComponent(e, false);
                    } // otherwise Other Components node selected in mousePressed
                }
            }

            prevLeftMousePoint = lastLeftMousePoint;
            lastLeftMousePoint = null;
        } else if (!draggingEnded) {
            if (mouseOnNonVisualTray(p)) {
                dispatchToNonVisualTray(e);
            } else if (popup && !endDragging(null)) {
                // Going to show context menu
                if (mouseOnVisual(p)) {
                    // If popup menu is already displayed (elsewhere), we did not receive
                    // mouse press event and now may have the clicked component unselected.
                    // We want to select the clicked component as usual but don't want
                    // to cancel multiselection - thus selecting like on press event.
                    selectComponent(e, true);
                } else {
                    selectOtherComponentsNode();
                }
                showContextMenu(p); // context menu on Windows on mouse release
            }
        }

        e.consume();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
            formDesigner.requestActive();
            PaletteItem item = PaletteUtils.getSelectedItem();
            if(formDesigner.getMenuEditLayer().isPossibleNewMenuComponent(item)) {
                formDesigner.getMenuEditLayer().startNewMenuComponentPickAndPlop(item,e.getPoint());
                return;
            }
            Node itemNode;
            if (item != null && (itemNode = item.getNode()) != null) {
                StatusDisplayer.getDefault().setStatusText(
                    FormUtils.getFormattedBundleString(
                        "FMT_MSG_AddingComponent", // NOI18N
                        new String[] { itemNode.getDisplayName() }));
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (draggedComponent != null && formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
            draggedComponent.move(null);
            repaint();
            StatusDisplayer.getDefault().setStatusText(""); // NOI18N
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        formDesigner.componentActivated();
        if (!HandleLayer.this.isVisible()) {
            return;
        }

        boolean leftMousePressed = (e.getButton() == MouseEvent.BUTTON1);
        boolean rightMousePressed = (e.getButton() == MouseEvent.BUTTON3);
        boolean leftMouseDown = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK;
        boolean rightMouseDown = (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK;
        boolean popup = e.isPopupTrigger();
        Point p = e.getPoint();

        if (rightMousePressed || popup) {
            if (formDesigner.getDesignerMode() != FormDesigner.MODE_SELECT) {
                formDesigner.toggleSelectionMode(); // calls endDragging(null)
                repaint();
            } else if (endDragging(null)) { // there was dragging, now canceled
                repaint();
            } else {
                if (mouseOnNonVisualTray(p)) {
                    dispatchToNonVisualTray(e);
                } else if (!leftMouseDown) {
                    // no dragging, ensure a component is selected for context menu
                    if (mouseOnVisual(p)) {
                        // We used to only select the component if there was nothing selected
                        // on current position, but changed to always select - bug 94543.
                        RADComponent hitMetaComp = selectComponent(e, true);
                        processMouseClickInLayout(hitMetaComp, e);
                    } else {
                        selectOtherComponentsNode();
                    }
                    if (popup) { // context menu on Mac on Ctrl+left mouse press, on Linux on right mouse press
                        showContextMenu(p); 
                    }
                }
                draggingEnded = false; // reset flag preventing dragging from start
            }

        } else if (formDesigner.getDesignerMode() != FormDesigner.MODE_ADD
                   && mouseOnNonVisualTray(p)) {
            dispatchToNonVisualTray(e);

        } else if (leftMousePressed && !rightMouseDown) {
            lastLeftMousePoint = p;
            if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT) {
                checkResizing(e);
                if (!mouseOnVisual(lastLeftMousePoint)) {
                    if ((resizeType == 0) && (selectedComponentAt(lastLeftMousePoint, 0, true) == null))
                        selectOtherComponentsNode();
                }
                // Shift+left is reserved for interval or area selection,
                // applied on mouse release or mouse dragged; ignore it here.
                else if ((e.getClickCount() != 2 || !processDoubleClick(e)) // no doubleclick
                         && resizeType == 0 // no resizing
                         && (!e.isShiftDown() || e.isAltDown())) {
                    RADComponent hitMetaComp = selectComponent(e, true); 
                    if (!extraModifier(e)) { // plain single click
                        processMouseClickInLayout(hitMetaComp, e);
                    }
                }
                draggingEnded = false; // reset flag preventing dragging from start
            } else if (!viewOnly) { // form can be modified
                if (formDesigner.getDesignerMode() == FormDesigner.MODE_CONNECT) {
                    selectComponent(e, true);
                } else if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
                    endDragging(e);
                    if (!e.isShiftDown()) {
                        formDesigner.toggleSelectionMode();
                    }
                    // otherwise (with Shift held) stay in adding mode
                }
            }
        }
        e.consume();
    }

    // ---------
    // MouseMotionListener implementation

    @Override
    public void mouseDragged(MouseEvent e) {
        if (formDesigner.getDesignerMode() != FormDesigner.MODE_SELECT) {
            return; // dragging makes sense only in selection mode
        }
        Point p = e.getPoint();
        if (lastMousePosition != null) {
            lastXPosDiff = p.x - lastMousePosition.x;
            lastYPosDiff = p.y - lastMousePosition.y;
        }

        if (!draggingEnded && !anyDragger() && lastLeftMousePoint != null) { // no dragging yet
            if (!viewOnly
                    && !e.isControlDown() && !e.isMetaDown() && (!e.isShiftDown() || e.isAltDown())
                    && (resizeType != 0 || lastLeftMousePoint.distance(p) > 6)) {
                // start component dragging
                RADVisualComponent[] draggedComps =
                    (resizeType & DESIGNER_RESIZING) == 0 ? getComponentsToDrag() :
                    new RADVisualComponent[] { formDesigner.getTopDesignComponent() };
                if (draggedComps != null) {
                    if (resizeType == 0) {
                        draggedComponent = new ExistingComponentDrag(
                            draggedComps, lastLeftMousePoint, e.getModifiers());
                    } else  {
                        draggedComponent = new ResizeComponentDrag(
                            draggedComps, lastLeftMousePoint, resizeType);
                    }
                }
            }
            if (draggedComponent == null // component dragging has not started
                    && lastLeftMousePoint.distance(p) > 4
                    && !e.isAltDown() && !e.isControlDown() && !e.isMetaDown()) {
                // check for possible selection dragging
                RADComponent topComp = formDesigner.getTopDesignComponent();
                RADComponent comp = getMetaComponentAt(lastLeftMousePoint, COMP_DEEPEST);
                if (topComp != null
                    && (e.isShiftDown() || comp == null || comp == topComp || comp.getParentComponent() == null)) {
                    // start selection dragging
                    selectionDragger = new SelectionDragger(lastLeftMousePoint);
                }
            }
        }

        if (draggedComponent != null) {
            draggedComponent.move(e);
            highlightPanel(e, false);
            repaint();
        } else if (selectionDragger != null) {
            selectionDragger.drag(p);
            repaint();
        }

        lastMousePosition = p;
        e.consume();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        if (lastMousePosition != null) {
            lastXPosDiff = p.x - lastMousePosition.x;
            lastYPosDiff = p.y - lastMousePosition.y;
        }
        if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
            PaletteItem item = PaletteUtils.getSelectedItem();
            if( null == item ) {
                if( null != draggedComponent ) {
                    endDragging( e );
                }
                return;
            }
            if (draggedComponent == null) {
                // first move event, pre-create visual component to be added
                draggedComponent = new NewComponentDrag(item);
            }
            if (draggedComponent.isValid()) {
                draggedComponent.move(e);
                repaint();
            } else { // new component failed
                formDesigner.toggleSelectionMode(); // also calls endDragging(null)
            }
        } else if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT
                 && !anyDragger()) {
            mouseHint = null;
            checkResizing(e);
            updateToolTip(e);
        }
        highlightPanel(e, false);
        lastMousePosition = p;
    }

    private void updateToolTip(MouseEvent e) {
        if (mouseHint != null) {
            boolean was = getToolTipText() != null;
            setToolTipText(mouseHint);
            if (!was) {
                ToolTipManager.sharedInstance().mouseEntered(e);
            }
        } else if (getToolTipText() != null) {
            setToolTipText(null);
        }
    }

    private void showToolTip(MouseEvent e) {
        updateToolTip(e);
        if (mouseHint != null) {
            ToolTipManager ttm = ToolTipManager.sharedInstance();
            int ttDelay = ttm.getInitialDelay();
            ttm.setInitialDelay(0);
            ttm.mouseMoved(e);
            ttm.setInitialDelay(ttDelay);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        RADVisualComponent metacomp = getMouseWheelComponent(formDesigner, false);
        if (metacomp instanceof RADVisualContainer && formDesigner.isInDesigner(metacomp)
                && ((RADVisualContainer)metacomp).getLayoutSupport() == null) {
            Point p = convertPointToComponent(e.getPoint(), formDesigner.getTopDesignComponentView());
            if (!viewOnly && formDesigner.getLayoutDesigner().acceptsMouseWheel(p)) {
                if (wheeler == null) {
                    wheeler = new MouseWheeler(metacomp);
                    if (!wheelerBlocked) {
                        EventQueue.invokeLater(wheeler);
                    }
                }
                wheeler.addEvent(e);
                e.consume();
            }
        }
        if (!e.isConsumed()) {
            Container p = getParent();
            while (p != null && !(p instanceof JScrollPane)) {
                p = p.getParent();
            }
            if (p != null) {
                p.dispatchEvent(e);
            }
        }
    }

    private static RADVisualComponent getMouseWheelComponent(FormDesigner designer, boolean later) {
        if (!later || (designer.getFormEditor() != null && designer.getFormEditor().isFormLoaded())
                && designer.getDesignerMode() == FormDesigner.MODE_SELECT) {
            java.util.List<RADComponent> selected = designer.getSelectedComponents();
            if (selected.size() == 1
                    && selected.get(0) instanceof RADVisualComponent) {
                return (RADVisualComponent) selected.get(0);
            }
        }
        return null;
    }

    private class MouseWheeler implements Runnable {
        private int coalesced;
        private int units;
        private RADVisualComponent component;
        private MouseEvent lastEvent;

        MouseWheeler(RADVisualComponent comp) {
            component = comp;
        }

        @Override
        public void run() {
            if (wheelerBlocked) {
                wheelerBlocked = false;
                if (wheeler != null) {
                    EventQueue.invokeLater(wheeler);
                }
                return;
            }
            wheeler = null;
            RADVisualComponent comp = getMouseWheelComponent(formDesigner, true);
            if (comp == component) {
                FormModel formModel = getFormModel();
                LayoutModel layoutModel = getLayoutModel();
                Object layoutUndoMark = layoutModel.getChangeMark();
                javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
                boolean autoUndo = true;
                try {
                    mouseHint = formDesigner.getLayoutDesigner().mouseWheelMoved(coalesced, units);
                    autoUndo = false;
                    showToolTip(lastEvent);
                } finally {
                    formModel.fireContainerLayoutChanged((RADVisualContainer)comp, null, null, null);
                    if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                        formModel.addUndoableEdit(ue);
                    }
                    if (autoUndo) {
                        formModel.forceUndoOfCompoundEdit();
                    } else { // successfully applied
                        wheelerBlocked = true; // don't allow next changes until the current one is built in the designer
                        EventQueue.invokeLater(this);
                    }
                }
            }
        }

        void addEvent(MouseWheelEvent e) {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                coalesced++;
                units += e.getUnitsToScroll();
                lastEvent = e;
            }
        }
    }

    /**
     * Dispatches the mouse event to the non-visual tray.
     *
     * @param e the event to dispatch.
     */
    private void dispatchToNonVisualTray(final MouseEvent e) {
        NonVisualTray tray = formDesigner.getNonVisualTray();
        if (tray == null) {
            return;
        }
        Point point = SwingUtilities.convertPoint(this, e.getPoint(), tray);
        Component component = SwingUtilities.getDeepestComponentAt(tray, point.x, point.y);
        point = SwingUtilities.convertPoint(tray, point, component);
        component.dispatchEvent(new MouseEvent(
            component,
            e.getID(),
            e.getWhen(),
            e.getModifiers(),
            point.x,
            point.y,
            e.getClickCount(),
            e.isPopupTrigger()));
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (mouseOnNonVisualTray(e.getPoint())) {
            NonVisualTray tray = formDesigner.getNonVisualTray();
            Point point = SwingUtilities.convertPoint(this, e.getPoint(), tray);
            JComponent component = (JComponent)SwingUtilities.getDeepestComponentAt(tray, point.x, point.y);
            point = SwingUtilities.convertPoint(tray, point, component);
            return component.getToolTipText(new MouseEvent(
                tray,
                e.getID(),
                e.getWhen(),
                e.getModifiers(),
                point.x,
                point.y,
                e.getClickCount(),
                e.isPopupTrigger()));
        } else {
            return super.getToolTipText(e);
        }
    }

    // ----------

    private class SelectionDragger {
        private Point startPoint;
        private Point lastPoint;

        public SelectionDragger(Point startPoint) {
            this.startPoint = startPoint;
        }

        public void paintDragFeedback(Graphics g) {
            if (startPoint != null && lastPoint != null) {
                Rectangle r = getRectangle();
                g.drawRect(r.x, r.y, r.width, r.height);
            }
        }

        public void drag(Point p) {
            lastPoint = p;
        }

        public void drop(Point endPoint) {
            if (startPoint != null && endPoint != null) {
                lastPoint = endPoint;
                java.util.List<RADComponent> toSelect = new ArrayList<RADComponent>();
                collectSelectedComponents(
                    getRectangle(),
                    formDesigner.getComponentLayer().getComponentContainer(),
                    toSelect);

                RADComponent[] selected = new RADComponent[toSelect.size()];
                toSelect.toArray(selected);
                formDesigner.setSelectedComponents(selected);
            }
        }

        private Rectangle getRectangle() {
            int x = startPoint.x <= lastPoint.x ? startPoint.x : lastPoint.x;
            int y = startPoint.y <= lastPoint.y ? startPoint.y : lastPoint.y;
            int w = lastPoint.x - startPoint.x;
            if (w < 0)
                w = -w;
            int h = lastPoint.y - startPoint.y;
            if (h < 0)
                h = -h;

            return new Rectangle(x, y, w, h);
        }

        private boolean collectSelectedComponents(Rectangle selRect,
                                                  Container cont,
                                                  java.util.List<RADComponent> toSelect)
        {
            java.util.List<Component> subContainers = new ArrayList<Component>();

            Component[] comps;
            if (cont instanceof JTabbedPane) {
                Component selectedTab = ((JTabbedPane)cont).getSelectedComponent();
                comps = (selectedTab == null) ? new Component[0] : new Component[] {selectedTab};
            } else {
                comps = cont.getComponents();
            }
            boolean contains = false;
            for (int i=0; i < comps.length; i++) {
                Component comp = comps[i];
                Rectangle bounds = convertRectangleFromComponent(comp.getBounds(), cont);
                if (selRect.intersects(bounds)) {
                    if (selRect.contains(bounds)) {
                        contains = true;
                    }
                    RADComponent metacomp = formDesigner.getMetaComponent(comp);
                    if (metacomp != null) {
                        toSelect.add(metacomp);
                    }
                    if (comp instanceof Container) {
                        subContainers.add(comp);
                    }
                }
            }

            if (toSelect.size() > 1
                    || (toSelect.size() == 1 && (subContainers.isEmpty() || contains))) {
                return true;
            }

            RADComponent theOnlyOne = toSelect.size() == 1 ? toSelect.get(0) : null;

            for (int i=0; i < subContainers.size(); i++) {
                toSelect.clear();
                if (collectSelectedComponents(selRect,
                                              (Container)subContainers.get(i),
                                              toSelect))
                    return true;
            }

            if (theOnlyOne != null) {
                toSelect.add(theOnlyOne);
                return true;
            }
            
            return false;
        }
    }

    // -------

    private abstract class ComponentDrag {
        RADVisualComponent[] movingComponents;
        boolean draggableLayoutComponents;
        boolean componentsMoving; // being moved during dragging (and so need repaint)?
        RADVisualContainer targetContainer;
        RADVisualContainer fixedTarget;
        boolean fixedDimension;
        Component[] showingComponents;
        Rectangle[] originalBounds; // in coordinates of HandleLayer
        Rectangle compoundBounds; // compound from original bounds
        Rectangle[] movingBounds; // in coordinates of ComponentLayer
        Point hotSpot; // in coordinates of ComponentLayer
        Point convertPoint; // from HandleLayer to ComponentLayer (top visual component)
        boolean newDrag;
        boolean oldDrag;
        Object layoutUndoMark;
        UndoableEdit layoutUndoEdit;

        // ctor for adding new
        ComponentDrag() {
            if (formDesigner.getTopDesignComponentView() == null) {
                convertPoint = new Point(0,0);
            } else {
                convertPoint = convertPointFromComponent(0, 0, formDesigner.getTopDesignComponentView());
            }
            componentsMoving = true;
        }

        // ctor for moving and resizing
        ComponentDrag(RADVisualComponent[] components, Point hotspot) {
            this();
            setMovingComponents(components);

            int count = components.length;
            showingComponents = new Component[count]; // [provisional - just one component can be moved]
            originalBounds = new Rectangle[count];
            movingBounds = new Rectangle[count];
            for (int i=0; i < count; i++) {
                showingComponents[i] = (Component) formDesigner.getComponent(movingComponents[i]);
                originalBounds[i] = showingComponents[i].getBounds();
                convertRectangleFromComponent(originalBounds[i], showingComponents[i].getParent());
                compoundBounds = compoundBounds != null ?
                                 compoundBounds.union(originalBounds[i]) : originalBounds[i];
                movingBounds[i] = new Rectangle();
                movingBounds[i].width = originalBounds[i].width;
                movingBounds[i].height = originalBounds[i].height;
            }

            this.hotSpot = hotspot == null ?
                new Point(4, 4) :
                new Point(hotspot.x - convertPoint.x, hotspot.y - convertPoint.y);
        }

        boolean isValid() {
            return showingComponents != null; // if null, initialization failed
        }

        final void setMovingComponents(RADVisualComponent[] components) {
            this.movingComponents = components;
            if (components != null && components.length > 0 && components[0] != null) {
                draggableLayoutComponents = !components[0].isMenuComponent();
            } else {
                draggableLayoutComponents = false;
            }
        }

        final RADVisualContainer getSourceContainer() {
            return movingComponents != null && movingComponents.length > 0
                   && formDesigner.getTopDesignComponent() != movingComponents[0]
                ? movingComponents[0].getParentContainer() : null;
        }

        final boolean isTopComponent() {
            return movingComponents != null && movingComponents.length > 0
                   && formDesigner.getTopDesignComponent() == movingComponents[0];
        }

        final boolean isDraggableLayoutComponent() {
            return draggableLayoutComponents;
        }

        /**
         * @return true if the dragged component changes position or size during dragging
         */
        final boolean isComponentMoving() {
            return componentsMoving;
        }

        final RADVisualContainer getTargetContainer(Point p, int modifiers) {
            if (fixedTarget != null) {
                return fixedTarget;
            }
            int mode = ((modifiers & InputEvent.ALT_MASK) != 0) ? COMP_SELECTED : COMP_DEEPEST;
            RADVisualContainer metacont = HandleLayer.this.getMetaContainerAt(p, mode);
            if ((metacont != null) && (metacont.getLayoutSupport() == null)) {
                RADVisualContainer dirMetacont = HandleLayer.this.getMetaContainerAt(
                        getMoveDirectionSensitivePoint(p, modifiers), mode);
                if ((dirMetacont != null) && (dirMetacont.getLayoutSupport() == null)) {
                    metacont = dirMetacont;
                }
            }
            if (movingComponents != null) {
                java.util.List comps = Arrays.asList(movingComponents);
                while (comps.contains(metacont)) {
                    metacont = metacont.getParentContainer();
                }
            }
            if (FormDesigner.isTransparentContainer(metacont)) {
                RADVisualContainer parent = metacont.getParentContainer();
                if (parent != null && formDesigner.isInDesigner(parent)) {
                    metacont = parent;
                }
            }
            return metacont;
        }

        private Point getMoveDirectionSensitivePoint(Point p, int modifiers) {
            if (lastMousePosition != null
                && compoundBounds != null
                && (modifiers & (InputEvent.ALT_MASK|InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK)) == 0)
            {
                if (compoundBounds.width <= 0 || compoundBounds.height <= 0) {
                    return p;
                }
                int x;
                int y;
                if (lastXPosDiff != 0 && lastYPosDiff != 0) {
                    double dx = lastXPosDiff;
                    double dy = lastYPosDiff;
                    double d = Math.abs(dy/dx);
                    double r = compoundBounds.getHeight() / compoundBounds.getWidth();
                    if (d > r) {
                        x = p.x + (int)Math.round(compoundBounds.getHeight() / d / 2.0) * (lastXPosDiff > 0 ? 1 : -1);
                        y = p.y - convertPoint.y - hotSpot.y + compoundBounds.y + (lastYPosDiff > 0 ? compoundBounds.height : 0);
                    }
                    else {
                        x = p.x - convertPoint.x - hotSpot.x + compoundBounds.x + (lastXPosDiff > 0 ? compoundBounds.width : 0);
                        y = p.y + (int)Math.round(compoundBounds.getWidth() * d / 2.0) * (lastYPosDiff > 0 ? 1 : -1);
                    }
                }
                else {
                    x = lastXPosDiff == 0 ? p.x :
                        p.x - convertPoint.x - hotSpot.x + compoundBounds.x + (lastXPosDiff > 0 ? compoundBounds.width : 0);
                    y = lastYPosDiff == 0 ? p.y :
                        p.y - convertPoint.y - hotSpot.y + compoundBounds.y + (lastYPosDiff > 0 ? compoundBounds.height : 0);
                }
                Rectangle boundaries = formDesigner.getComponentLayer().getDesignerInnerBounds();
                // don't let the component component fall into non-visual area easily
                if (x < boundaries.x && x + 8 >= boundaries.x) {
                    x = boundaries.x;
                }
                else if (x > boundaries.x + boundaries.width && x - 8 < boundaries.x + boundaries.width) {
                    x = boundaries.x + boundaries.width - 1;
                }
                if (y < boundaries.y && y + 8 >= boundaries.y) {
                    y = boundaries.y;
                }
                else if (y > boundaries.y + boundaries.height && y - 8 < boundaries.y + boundaries.height) {
                    y = boundaries.y + boundaries.height - 1;
                }
                return new Point(x, y);
            }
            else return p;
        }

        final void move(MouseEvent e) {
            if (e == null) {
                move(null, 0);
            } else {
                move(e.getPoint(), e.getModifiers());
                showToolTip(e);
            }
        }

        void move(Point p, int modifiers) {
            if (p == null) {
                for (int i=0; i<movingBounds.length; i++) {
                    movingBounds[i].x = Integer.MIN_VALUE;
                }
                return;
            }

            boolean lockDimension = (modifiers & InputEvent.CTRL_MASK) != 0;
            if (!lockDimension && fixedDimension) { // CTRL released in new layout
                fixedTarget = null;
                fixedDimension = false;
            }

            targetContainer = getTargetContainer(p, modifiers);

            
            // support for highlights in menu containers
            // hack: this only checks the first component.
            if(this.movingComponents != null) {
                RADVisualComponent moveComp = this.movingComponents[0];
                // if  have a menu component over a menu container then do a highlight
                // hack: this only works for new comps, not moving existing comps
                if(newDrag && formDesigner.getMenuEditLayer().canHighlightContainer(targetContainer,moveComp)) {
                    formDesigner.getMenuEditLayer().rolloverContainer(targetContainer);
                } else {
                    formDesigner.getMenuEditLayer().rolloverContainer(null);
                }
            }
            
            if (newDrag && isDraggableLayoutComponent()
                    && targetContainer != null && targetContainer.getLayoutSupport() == null) {
                p.x -= convertPoint.x;
                p.y -= convertPoint.y;
                LayoutDesigner layoutDesigner = formDesigner.getLayoutDesigner();
                layoutDesigner.move(p, targetContainer.getId(),
                                    ((modifiers & InputEvent.ALT_MASK) == 0),
                                    lockDimension, movingBounds);
                // set fixed target if dimension locked by holding CTRL, but only if
                // there's no fixed target used already (e.g. like when resizing)
                if (lockDimension && (fixedTarget == null || fixedDimension)) {
                    fixedDimension = true;
                    String targetId = layoutDesigner.getDragTargetContainer();
                    if (targetId != null) {
                        RADComponent targetComp = getFormModel().getMetaComponent(targetId);
                        if (targetComp instanceof RADVisualContainer) {
                            fixedTarget = (RADVisualContainer) targetComp;
                        }
                    }
                }
                String[] position = layoutDesigner.positionCode();
                FormEditor.getAssistantModel(getFormModel()).setContext(position[0], position[1]);
                mouseHint = formDesigner.getLayoutDesigner().getToolTipText(p);
            } else {
                if (oldDrag && isDraggableLayoutComponent()
                         && targetContainer != null
                         && targetContainer.getLayoutSupport() != null) {
                    oldMove(p);
                } else {
                    FormEditor.getAssistantModel(getFormModel()).setContext("generalPosition"); // NOI18N
                }
                for (int i=0; i<movingBounds.length; i++) {
                    movingBounds[i].x = p.x - convertPoint.x - hotSpot.x + originalBounds[i].x - convertPoint.x;
                    movingBounds[i].y = p.y - convertPoint.y - hotSpot.y + originalBounds[i].y - convertPoint.y;
                }
            }
        }

        final void maskDraggingComponents() {
            if (!isTopComponent() && showingComponents != null && isComponentMoving()
                    && movingBounds.length > 0 && movingBounds[0].x > Integer.MIN_VALUE) {
                for (int i=0; i < showingComponents.length; i++) {
                    Rectangle r = movingBounds[i];
                    showingComponents[i].setBounds(r.x + Short.MIN_VALUE, r.y + Short.MIN_VALUE, r.width, r.height);
                }
            }
        }

        final void paintFeedback(Graphics2D g) {
            if ((movingBounds.length < 1) || (movingBounds[0].x == Integer.MIN_VALUE))
                return;

            for (int i=0; i<showingComponents.length; i++) {
                if (newDrag && isDraggableLayoutComponent()
                    && ((targetContainer != null && targetContainer.getLayoutSupport() == null)
                        || (targetContainer == null && isTopComponent())))
                {   // new layout support
                    if (isComponentMoving()) {
                        Graphics gg = dragCompGraphics(g, movingBounds[i]);
                        // paint the component being moved
                        if (!isTopComponent()) {
                            doLayout(showingComponents[i]);
                            paintDraggedComponent(showingComponents[i], gg);
                        } // resized top design component is painted automatically otherwise

                        // paint the selection rectangle
                        // [the x,y coordinates are off by 1 pixel from the static selection rect]
                        gg.setColor(formSettings.getSelectionBorderColor());
                        gg.drawRect(0, 0, movingBounds[i].width, movingBounds[i].height);
                    }
                    // paint the layout designer feedback
                    g.translate(convertPoint.x, convertPoint.y);
                    g.setColor(formSettings.getGuidingLineColor());
                    Stroke oldStroke = g.getStroke();
                    g.setStroke(getPaintLayoutStroke());
                    formDesigner.getLayoutDesigner().paintMoveFeedback(g);
                    g.setStroke(oldStroke);
                    g.translate(-convertPoint.x, -convertPoint.y);
                }
                else if (oldDrag && isDraggableLayoutComponent()
                        && ((targetContainer != null && targetContainer.getLayoutSupport() != null)
                             || (targetContainer == null && isTopComponent()))) {
                    if (!isTopComponent()) {
                        doLayout(showingComponents[i]);
                        oldPaintFeedback(g, dragCompGraphics(g, movingBounds[i]), i);
                    }
                }
                else { // non-visual area
                    doLayout(showingComponents[i]);
                    paintDraggedComponent(showingComponents[i], dragCompGraphics(g, movingBounds[i]));
                }
            }
        }

        private Graphics dragCompGraphics(Graphics2D g, Rectangle bounds) {
            return g.create(bounds.x + convertPoint.x,bounds.y + convertPoint.y,
                            bounds.width + 1, bounds.height + 1);
        }

        final boolean end(final MouseEvent e) {
            dragPanel.removeAll();

            boolean retVal;
            if (e == null) {
                retVal = end(null, 0);
            }
            else {
                retVal = end(e.getPoint(), e.getModifiers());
            }
            if (retVal) {
                movingComponents = null;
                targetContainer = null;
                fixedTarget = null;
                showingComponents = null;
            }
            else {
                // re-init in next AWT round - to have the designer updated
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        init();
                        move(e);
                    }
                });
            }
            return retVal;
        }

        // methods to extend/override ---

        void init() {
            if (showingComponents != null) {
                // showing components need to be in a container to paint
                // correctly (relates to newly added components);
                // components in old layout need to be hidden
                RADVisualContainer sourceCont = getSourceContainer();
                boolean oldSource = sourceCont != null && sourceCont.getLayoutSupport() != null;
                dragPanel.removeAll();
                for (int i=0; i < showingComponents.length; i++) {
                    Component comp = showingComponents[i];
                    if (comp.getParent() == null) {
                        dragPanel.add(comp);
                    }
                    else if (oldSource) {
                        comp.setVisible(false);
                        // VisualReplicator makes it visible again...
                    }
                    avoidDoubleBuffering(comp);
                }
            }
        }

        protected void avoidDoubleBuffering(Component comp) {
            if (comp instanceof JComponent) {
                ((JComponent)comp).setDoubleBuffered(false);
            }
            if (comp instanceof Container) {
                Container cont = (Container)comp;
                for (int i=0; i<cont.getComponentCount(); i++) {
                    avoidDoubleBuffering(cont.getComponent(i));
                }
            }
        }

        boolean end(Point p, int modifiers) {
            // clear the rollover just in case it was set
            formDesigner.getMenuEditLayer().clearRollover();
            return true;
        }

        void oldMove(Point p) {
        }

        void oldPaintFeedback(Graphics2D g, Graphics gg, int index) {
        }

        // layout model undo/redo ---

        final void createLayoutUndoableEdit() {
            layoutUndoMark = getLayoutModel().getChangeMark();
            layoutUndoEdit = getLayoutModel().getUndoableEdit();
        }

        final void placeLayoutUndoableEdit(boolean autoUndo) {
            if (!layoutUndoMark.equals(getLayoutModel().getChangeMark())) {
                getFormModel().addUndoableEdit(layoutUndoEdit);
            }
            if (autoUndo) {
                getFormModel().forceUndoOfCompoundEdit();
            }
            layoutUndoMark = null;
            layoutUndoEdit = null;
        }
    }

    private static void doLayout(Component component) {
        if (component instanceof Container) {
            Container cont = (Container) component;
            cont.doLayout();
            for (int i=0, n=cont.getComponentCount(); i < n; i++) {
                doLayout(cont.getComponent(i));
            }
        }
    }

    private static void paintDraggedComponent(Component comp, Graphics g) {
        paintDraggedComponent(comp, g, 0.7f);
    }

    private static void paintDraggedComponent(Component comp, Graphics g, float opacity) {
        issue71257Hack(comp);
        Graphics2D g2 = (Graphics2D)g;
        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        try {
            if (comp instanceof JComponent) {
                comp.paint(g);
            } else {
                int width = comp.getWidth();
                int height = comp.getHeight();
                if ((width>0) && (height>0)) {
                    Image image = comp.createImage(width, height);
                    Graphics gImage = image.getGraphics();
                    gImage.setClip(0, 0, width, height);
                    FakePeerSupport.getPeer(comp).paint(gImage);
                    g.drawImage(image, 0, 0, null);
                }
            }
        }
        catch (RuntimeException ex) { // inspired by bug #62041 (JProgressBar bug #5035852)
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
        } finally {
            g2.setComposite(originalComposite);
        }
    }

    private static Field componentValidField;
    static {
        try {
            Field field = Component.class.getDeclaredField("valid"); // NOI18N
            field.setAccessible(true);
            componentValidField = field;
        } catch (NoSuchFieldException ex) {
            FormUtils.LOGGER.log(Level.INFO, null, ex);
        } catch (SecurityException ex) {
            FormUtils.LOGGER.log(Level.INFO, null, ex);
        }
    }
    private static void issue71257Hack(Component comp) {
        Container cont = comp.getParent();
        if (cont != null) {
            if (cont.getLayout() instanceof GroupLayout) {
                while (cont != null && !(cont instanceof ComponentLayer)) {
                    if (cont instanceof JTabbedPane) {
                        if (!cont.isValid() && (componentValidField != null)) {
                            try {
                                componentValidField.set(cont, true);
                            } catch (IllegalArgumentException ex) {
                                FormUtils.LOGGER.log(Level.INFO, null, ex);
                            } catch (IllegalAccessException ex) {
                                FormUtils.LOGGER.log(Level.INFO, null, ex);
                            }
                        }
                    }
                    cont = cont.getParent();
                }
            }
        }
    }

    private Map<RADComponent,Rectangle[]> hiddenComponents = new IdentityHashMap<RADComponent,Rectangle[]>();
    void updateHiddentComponent(RADComponent metacomp, Rectangle bounds, Rectangle visibleBounds) {
        if (bounds == null) {
            hiddenComponents.remove(metacomp);
        } else {
            hiddenComponents.put(metacomp, new Rectangle[] {bounds, visibleBounds});
        }
    }

    // for moving existing components
    private class ExistingComponentDrag extends ComponentDrag {
        private int modifiers; // for the old layout support
        private ComponentDragger oldDragger; // drags components in the old layout support

        ExistingComponentDrag(RADVisualComponent[] comps,
                              Point hotspot, // in HandleLayer coordinates
                              int modifiers)
        {
            super(comps, hotspot);
            this.modifiers = modifiers;
            init();
        }


        @Override
        void init() {
            RADVisualContainer metacont = getSourceContainer();
            String[] compIds = new String[showingComponents.length];
            for (int i=0; i < showingComponents.length; i++) {
                compIds[i] = movingComponents[i].getId();
                originalBounds[i].x -= convertPoint.x;
                originalBounds[i].y -= convertPoint.y;
            }

            if (metacont.getLayoutSupport() == null) { // new layout support
                formDesigner.getLayoutDesigner().startMoving(
                    compIds, originalBounds, hotSpot);
            }
            else { // dragging started in the old layout support
                LayoutComponent[] layoutComps = new LayoutComponent[compIds.length];
                for (int i=0; i < compIds.length; i++) {
                    layoutComps[i] = getLayoutModel().getLayoutComponent(compIds[i]);
                    if (layoutComps[i] == null) {
                        layoutComps[i] = new LayoutComponent(compIds[i], false);
                    }
                }
                formDesigner.getLayoutDesigner().startAdding(
                    layoutComps, originalBounds, hotSpot, null);                    
            }

            if ((modifiers & InputEvent.ALT_MASK) != 0) {
                // restricted dragging - within the same container, or one level up
                fixedTarget = (modifiers & InputEvent.SHIFT_MASK) != 0 
                           || formDesigner.getTopDesignComponent() == metacont ?
                    metacont : metacont.getParentContainer();
            }

            // old layout component dragger requires coordinates related to HandleLayer
            for (int i=0; i < originalBounds.length; i++) {
                originalBounds[i].x += convertPoint.x;
                originalBounds[i].y += convertPoint.y;
            }
            oldDragger = new ComponentDragger(
                formDesigner,
                HandleLayer.this,
                movingComponents,
                originalBounds,
                new Point(hotSpot.x + convertPoint.x, hotSpot.y + convertPoint.y),
                fixedTarget);

            newDrag = oldDrag = true;

            super.init();
        }

        @Override
        boolean end(Point p, int modifiers) {    
            // clear the rollover just in case it was set
            formDesigner.getMenuEditLayer().clearRollover();
            
            RADVisualContainer originalCont = getSourceContainer();
            // fail if trying to move a menu component to a non-menu container
            if(MenuEditLayer.containsMenuTypeComponent(movingComponents)) {
                if(!MenuEditLayer.isValidMenuContainer(targetContainer)) {
                    formDesigner.getLayoutDesigner().endMoving(false);
                    formDesigner.updateContainerLayout(originalCont);
                    return true;
                }
            }
            // fail if trying to move a non-menu component into a menu container
            if(!MenuEditLayer.containsMenuTypeComponent(movingComponents)) {
                if(MenuEditLayer.isValidMenuContainer(targetContainer)) {
                    formDesigner.getLayoutDesigner().endMoving(false);
                    formDesigner.updateContainerLayout(originalCont);
                    return true;
                }
            }
            if (p != null) {
                if (targetContainer == null || targetContainer.getLayoutSupport() != null) {
                    // dropped in old layout support, or on non-visual area
                    createLayoutUndoableEdit();
                    boolean autoUndo = true;
                    try {
                        formDesigner.getLayoutDesigner().removeDraggedComponents();
                        oldDragger.dropComponents(p, targetContainer);
                        autoUndo = false;
                    } finally {
                        placeLayoutUndoableEdit(autoUndo);
                    }
                }
                else { // dropped in new layout support
                    if (targetContainer != originalCont) {
                        for (int i=0; i < movingComponents.length; i++) {
                            getFormModel().removeComponent(movingComponents[i], false);
                        }
                        // Issue 69410 (don't mix remove/add chnages)
                        for (int i=0; i < movingComponents.length; i++) {
                            getFormModel().addVisualComponent(movingComponents[i], targetContainer, null, false);
                        }
                    }
                    createLayoutUndoableEdit();
                    boolean autoUndo = true;
                    try {
                        formDesigner.getLayoutDesigner().endMoving(true);
                        autoUndo = false;
                    } finally {
                        getFormModel().fireContainerLayoutChanged(targetContainer, null, null, null);
                        placeLayoutUndoableEdit(autoUndo);
                    }
                }
            }
            else { // canceled
                formDesigner.getLayoutDesigner().endMoving(false);
                formDesigner.updateContainerLayout(originalCont); //, false);
            }

            return true;
        }

        @Override
        void oldMove(Point p) {
            oldDragger.drag(p, targetContainer);
        }

        @Override
        void oldPaintFeedback(Graphics2D g, Graphics gg, int index) {
            oldDragger.paintDragFeedback(g);

            // don't paint if component dragged from old layout (may have strange size)
            Component comp = showingComponents[index];
            paintDraggedComponent(comp, gg);
        }
    }

    // for resizing existing components
    private class ResizeComponentDrag extends ComponentDrag {
        private int resizeType;
        private boolean inboundResizing;
        private Dimension originalSize;

        private ComponentDragger oldDragger; // drags components in the old layout support

        ResizeComponentDrag(RADVisualComponent[] comps,
                            Point hotspot, // in HandleLayer coordinates
                            int resizeType)
        {
            super(comps, hotspot);
            inboundResizing = (resizeType & INBOUND_RESIZING) != 0;
            this.resizeType = resizeType & RESIZE_MASK;
            init();
        }

        @Override
        final void init() {
            RADVisualContainer sourceCont;
            if (isTopComponent() || inboundResizing) {
                LayoutModel layoutModel = getLayoutModel();
                newDrag = layoutModel != null
                          && layoutModel.getLayoutComponent(movingComponents[0].getId()) != null;
                oldDrag = !newDrag;
                sourceCont = null;
                if (inboundResizing) {
                    if (movingComponents[0] instanceof RADVisualContainer) {
                        fixedTarget = (RADVisualContainer) movingComponents[0];
                    }
                } else {
                    originalSize = formDesigner.getComponentLayer().getDesignerSize();
                }
            } else {
                sourceCont = getSourceContainer();
                if (sourceCont != null) {
                    if (sourceCont.getLayoutSupport() == null) {
                        newDrag = true;
                    }
                    else {
                        oldDrag = true;
                    }
                    fixedTarget = sourceCont;
                }
            }

            if (newDrag) { // new layout support
                String[] compIds = new String[showingComponents.length];
                for (int i=0; i < showingComponents.length; i++) {
                    compIds[i] = movingComponents[i].getId();
                    originalBounds[i].x -= convertPoint.x;
                    originalBounds[i].y -= convertPoint.y;
                }

                int[] res = new int[2];
                int horiz = resizeType & (LayoutSupportManager.RESIZE_LEFT
                                          | LayoutSupportManager.RESIZE_RIGHT);
                if (horiz == LayoutSupportManager.RESIZE_LEFT) {
                    res[LayoutConstants.HORIZONTAL] = LayoutConstants.LEADING;
                }
                else if (horiz == LayoutSupportManager.RESIZE_RIGHT) {
                    res[LayoutConstants.HORIZONTAL] = LayoutConstants.TRAILING;
                }
                else {
                    res[LayoutConstants.HORIZONTAL] = LayoutConstants.DEFAULT;
                }
                int vert = resizeType & (LayoutSupportManager.RESIZE_UP
                                          | LayoutSupportManager.RESIZE_DOWN);
                if (vert == LayoutSupportManager.RESIZE_UP) {
                    res[LayoutConstants.VERTICAL] = LayoutConstants.LEADING;
                }
                else if (vert == LayoutSupportManager.RESIZE_DOWN) {
                    res[LayoutConstants.VERTICAL] = LayoutConstants.TRAILING;
                }
                else {
                    res[LayoutConstants.VERTICAL] = LayoutConstants.DEFAULT;
                }

                formDesigner.getLayoutDesigner().startResizing(
                        compIds, originalBounds, hotSpot, res, sourceCont != null);
                if (inboundResizing) {
                    componentsMoving = false;
                    movingBounds[0].x = originalBounds[0].x;
                    movingBounds[0].y = originalBounds[0].y;
                    movingBounds[0].width = originalBounds[0].width;
                    movingBounds[0].height = originalBounds[0].height;
                }

                // convert back to HandleLayer
                for (int i=0; i < originalBounds.length; i++) {
                    originalBounds[i].x += convertPoint.x;
                    originalBounds[i].y += convertPoint.y;
                }
            }
            else if (oldDrag) { // old layout support
                oldDragger = new ComponentDragger(
                    formDesigner,
                    HandleLayer.this,
                    movingComponents,
                    originalBounds,
                    new Point(hotSpot.x + convertPoint.x, hotSpot.y + convertPoint.y),
                    resizeType);
            }

            if (!inboundResizing) {
                super.init();
            }
        }

        @Override
        boolean end(Point p, int modifiers) {
            if (p != null) {
                if (newDrag) { // new layout support
                    if (!inboundResizing) {
                        // make sure the visual component has the current size set 
                        // (as still being in its container the layout manager tries to
                        // restore the original size)
                        showingComponents[0].setSize(movingBounds[0].width, movingBounds[0].height);
                        doLayout(showingComponents[0]);
                    }

                    createLayoutUndoableEdit();
                    boolean autoUndo = true;
                    try {
                        formDesigner.getLayoutDesigner().endMoving(true);
                        for (int i=0; i < movingComponents.length; i++) {
                            RADVisualComponent metacomp = movingComponents[i];
                            if (metacomp instanceof RADVisualContainer) {
                                RADVisualContainer visCont = (RADVisualContainer) metacomp;
                                if (visCont.getLayoutSupport() == null) {
                                    getFormModel().fireContainerLayoutChanged(
                                        visCont, null, null, null);
                                }
                            }
                        }
                        autoUndo = false;
                    } finally {
                        if (targetContainer != null) {
                            getFormModel().fireContainerLayoutChanged(targetContainer, null, null, null);
                        }
                        placeLayoutUndoableEdit(autoUndo);
                    }
                } else { // old layout support
                    if (targetContainer != null) {
                        oldDragger.dropComponents(p, targetContainer);
                    }
                }
                if (isTopComponent()) {
                    if (!newDrag && movingComponents[0] instanceof RADVisualContainer) {
                        designerResizedForNewLayout();
                    }
                    formDesigner.setDesignerSize(new Dimension(movingBounds[0].width, movingBounds[0].height),
                                                 originalSize);
                }
            }
            else { // resizing canceled
                formDesigner.getLayoutDesigner().endMoving(false);

                if (isTopComponent()) {
                    // just revert ComponentLayer's designer size (don't need to go through FormDesigner)
                    ComponentLayer compLayer = formDesigner.getComponentLayer();
                    if (!compLayer.getDesignerSize().equals(originalSize)) {
                        compLayer.setDesignerSize(originalSize);
                        compLayer.revalidate();
                    }
                    compLayer.repaint();
                }
                else { // add resized component back
                    formDesigner.updateContainerLayout(getSourceContainer()); //, false);
                }
            }
            return true;
        }

        private void designerResizedForNewLayout() {
            if (formDesigner.getLayoutDesigner() != null) {
                // LayoutDesigner needs to know when the user resized the whole
                // designer area - which may require to update size definition
                // of some containers. This is needed only if the top level container
                // is not in new layout (i.e. resized via LayoutDesigner already).
                boolean horizontal = movingBounds[0].width != originalSize.width;
                boolean vertical = movingBounds[0].height != originalSize.height;
                if (horizontal || vertical) {
                    formDesigner.getLayoutDesigner().designerResized(horizontal, vertical);
                }
            }
        }

        @Override
        void move(Point p, int modifiers) {
            if (isTopComponent()) {
                if (newDrag) {
                    p.x -= convertPoint.x;
                    p.y -= convertPoint.y;
                    formDesigner.getLayoutDesigner().move(p,
                                                          null,
                                                         ((modifiers & InputEvent.ALT_MASK) == 0),
                                                         ((modifiers & InputEvent.CTRL_MASK) != 0),
                                                         movingBounds);
                    showingComponents[0].setSize(movingBounds[0].width, movingBounds[0].height);
                    mouseHint = formDesigner.getLayoutDesigner().getToolTipText(p);
                } else {
                    Rectangle r = formDesigner.getComponentLayer().getDesignerInnerBounds();
                    int w = r.width;
                    int h = r.height;
                    if ((resizeType & LayoutSupportManager.RESIZE_DOWN) != 0) {
                        h = p.y - r.y;
                        if (h < 0)
                            h = 0;
                    }
                    if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0) {
                        w = p.x - r.x;
                        if (w < 0)
                            w = 0;
                    }
                    movingBounds[0].width = w;
                    movingBounds[0].height = h;
                }
                Dimension size = new Dimension(movingBounds[0].width, movingBounds[0].height);
                formDesigner.getComponentLayer().setDesignerSize(size);
                doLayout(formDesigner.getComponentLayer());
            } else if (oldDrag && (targetContainer = getTargetContainer(p, modifiers)) != null && targetContainer.getLayoutSupport() != null) {
                oldMove(p);
                for (int i=0; i<movingBounds.length; i++) {
                    int xchange = p.x - convertPoint.x - hotSpot.x;
                    if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0) {
                        movingBounds[i].x = originalBounds[i].x - convertPoint.x + xchange;
                        xchange = -xchange;
                    } else {
                        movingBounds[i].x = originalBounds[i].x - convertPoint.x;
                    }
                    if ((resizeType & (LayoutSupportManager.RESIZE_RIGHT | LayoutSupportManager.RESIZE_LEFT)) != 0) {
                        movingBounds[i].width = originalBounds[i].width + xchange;
                    }
                    int ychange = p.y - convertPoint.y - hotSpot.y;
                    if ((resizeType & LayoutSupportManager.RESIZE_UP) != 0) {
                        movingBounds[i].y = originalBounds[i].y - convertPoint.y + ychange;
                        ychange = -ychange;
                    } else {
                        movingBounds[i].y = originalBounds[i].y - convertPoint.y;
                    }
                    if ((resizeType & (LayoutSupportManager.RESIZE_DOWN | LayoutSupportManager.RESIZE_UP)) != 0) {
                        movingBounds[i].height = originalBounds[i].height + ychange;
                    }
                }
            } else {
                super.move(p, modifiers);
            }
        }

        @Override
        void oldMove(Point p) {
            oldDragger.drag(p, targetContainer);
            FormEditor.getAssistantModel(getFormModel()).setContext("generalResizing"); // NOI18N
        }

        @Override
        void oldPaintFeedback(Graphics2D g, Graphics gg, int index) {
            paintDraggedComponent(showingComponents[index], gg);
            oldDragger.paintDragFeedback(g);
        }
    }

    // for moving a component being newly added
    private class NewComponentDrag extends ComponentDrag {
        private PaletteItem paletteItem;
        RADComponent addedComponent;

        private int index = - 1; // for the old layout support
        private LayoutConstraints constraints; // for the old layout support

        NewComponentDrag(PaletteItem paletteItem) {
            super();
            this.paletteItem = paletteItem;
            showingComponents = new Component[1];
            init();
        }

        @Override
        void init() { // can be re-inited
            boolean failed = true;
            RADVisualComponent precreated = null;
            try {
                precreated = getComponentCreator().precreateVisualComponent(paletteItem);
                failed = false;
            } catch (Exception ex) { // creation failed, already reported to the user
            } catch (LinkageError ex) { // creation failed, already reported to the user
            }

            if (precreated != null) {
                if (movingComponents == null) {
                    setMovingComponents(new RADVisualComponent[] { precreated });
                } else { // continuing adding - new instance of the same component
                    movingComponents[0] = precreated;
                }
                 
                LayoutComponent precreatedLC = isDraggableLayoutComponent()
                        ? getComponentCreator().getPrecreatedLayoutComponent() : null;
                // (precreating LayoutComponent also adjusts the initial size of
                // the visual component which is used below)

                showingComponents[0] = (Component) precreated.getBeanInstance();
                // Force creation of peer - AWT components don't have preferred size otherwise
                if (!(showingComponents[0] instanceof JComponent)) {
                    FakePeerSupport.attachFakePeer(showingComponents[0]);
                    if (showingComponents[0] instanceof Container) {
                        FakePeerSupport.attachFakePeerRecursively((Container)showingComponents[0]);
                    }
                }

                Dimension size = showingComponents[0].getPreferredSize();
                if (originalBounds == null) { // new adding
                    hotSpot = new Point();
                    originalBounds = new Rectangle[] { new Rectangle(convertPoint.x, convertPoint.y, size.width, size.height) };
                    movingBounds = new Rectangle[] { new Rectangle(0, 0, size.width, size.height) };
                }
                else { // repeated adding of the same component type, reuse last bounds
                    movingBounds[0].width = size.width;
                    movingBounds[0].height = size.height;
                    originalBounds[0] = movingBounds[0];
                    movingBounds[0] = new Rectangle(movingBounds[0]);
                    originalBounds[0].x += convertPoint.x;
                    originalBounds[0].y += convertPoint.y;
                }
                compoundBounds = originalBounds[0];
                hotSpot.x = movingBounds[0].x + size.width/2 - 4;
                hotSpot.y = movingBounds[0].y + size.height/2;
                if (hotSpot.x < movingBounds[0].x)
                    hotSpot.x = movingBounds[0].x;

                if (precreatedLC != null) {
                    LayoutComponent[] layoutComponents = new LayoutComponent[] { precreatedLC };
                    if (formDesigner.getLayoutDesigner() != null) {
                        formDesigner.getLayoutDesigner().startAdding(
                                layoutComponents, movingBounds, hotSpot,
                                targetContainer != null && targetContainer.getLayoutSupport() == null
                                ? targetContainer.getId() : null);
                    }
                }

                newDrag = oldDrag = true;
            } else if (!failed) { // It's a non visual component, don't trying to instantiate
                // yet, present it as icon.
                Node node = paletteItem.getNode();
                Image icon;
                if (node == null) {
                    icon = paletteItem.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
                    if (icon == null) {
                        icon = ImageUtilities.loadImage("org/netbeans/modules/form/resources/form.gif"); // NOI18N
                    }
                } else {
                    icon = node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
                }
                showingComponents[0] = new JLabel(new ImageIcon(icon));
                Dimension dim = showingComponents[0].getPreferredSize();
                hotSpot = new Point(dim.width/2, dim.height/2);
                if (hotSpot.x < 0) {
                    hotSpot.x = 0;
                }
                originalBounds = new Rectangle[] { new Rectangle(convertPoint.x, convertPoint.y, dim.width, dim.height) };
                showingComponents[0].setBounds(originalBounds[0]);
                movingBounds = new Rectangle[] { showingComponents[0].getBounds() };

                newDrag = oldDrag = false;
            } else { // The corresponding class cannot be loaded, or the component instantiated. Cancel the drag.
                showingComponents = null;
                movingBounds = new Rectangle[0];
            }

            super.init();
        }

        Boolean doubleBuffered = null;
        @Override
        protected void avoidDoubleBuffering(Component comp) {
            // Issue 204184
            if (doubleBuffered == null) {
                doubleBuffered = comp.isDoubleBuffered();
            }
            super.avoidDoubleBuffering(comp);
        }

        /** Overrides end(Point,int) in ComponentDrag to support adding new components
         */
        @Override
        boolean end(Point p, int modifiers) {
            // clear the rollover just in case it was set
            formDesigner.getMenuEditLayer().clearRollover();
            
            if (p != null) {
                if (movingComponents != null) { // there is a precreated visual component
                    boolean newLayout;
                    boolean oldLayout;
                    Object constraints; // for old layout
                    if (targetContainer != null) {
                        newLayout = targetContainer.getLayoutSupport() == null;
                        oldLayout = !newLayout;
                        Point posInComp = new Point(hotSpot.x - originalBounds[0].x + convertPoint.x,
                                                    hotSpot.y - originalBounds[0].y + convertPoint.y);
                        constraints = oldLayout && isDraggableLayoutComponent()
                            ? getConstraintsAtPoint(targetContainer, p, posInComp) : null;
                    }
                    else {
                        newLayout = oldLayout = false;
                        constraints = null;
                    }
                    if ((doubleBuffered != null) && (showingComponents[0] instanceof JComponent)) {
                        // Issue 204184
                        ((JComponent)showingComponents[0]).setDoubleBuffered(doubleBuffered);
                    }
                    addedComponent = movingComponents[0];
                    LayoutComponent layoutComponent = isDraggableLayoutComponent()
                            ? getComponentCreator().getPrecreatedLayoutComponent() : null;
                    // add the component to FormModel
                    boolean added = getComponentCreator().addPrecreatedComponent(targetContainer, constraints);
                    // add the cmponent to LayoutModel
                    if (layoutComponent != null && getLayoutModel() != null) { // Some beans don't have layout
                        createLayoutUndoableEdit();
                        boolean autoUndo = true;
                        try {
                            formDesigner.getLayoutDesigner().endMoving(added && newLayout);
                            if (added) {
                                if (layoutComponent.isLayoutContainer()) {
                                    if (!newLayout) { // always add layout container to the model 
                                        getLayoutModel().addRootComponent(layoutComponent);
                                    }
                                }
                            } else {
                                repaint();
                            }
                            autoUndo = false;
                        } finally {
                            placeLayoutUndoableEdit(autoUndo);
                        }
                    }
                }
                else { // component not precreated ...
                    RADComponent targetComponent = targetContainer;
                    Class<?> clazz = paletteItem.getComponentClass();
                    if ((clazz!=null) && javax.swing.border.Border.class.isAssignableFrom(clazz)) {
                        int mode = ((modifiers & InputEvent.ALT_MASK) != 0) ? COMP_SELECTED : COMP_DEEPEST;
                        targetComponent = HandleLayer.this.getMetaComponentAt(p, mode);
                    }
                    addedComponent = getComponentCreator().createComponent(
                            paletteItem.getComponentClassSource(), targetComponent, null, false);
                    if (addedComponent == null) {
                        repaint();
                    }
                }

                if (addedComponent != null) {
                    java.beans.BeanDescriptor bDesc = addedComponent.getBeanInfo().getBeanDescriptor();
                    if ((bDesc != null) && (bDesc.getValue("customizeOnCreation") != null)) { // NOI18N
                        modifiers &= ~InputEvent.SHIFT_MASK;
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                RADComponentNode node = addedComponent.getNodeReference();
                                if (node.hasCustomizer()) {
                                    org.openide.nodes.NodeOperation.getDefault().customize(node);
                                }
                            }
                        });
                    }
                }
                if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
//                    init();
                    return false;
                }
            }
            else {
                if (formDesigner.getLayoutDesigner() != null) {
                    formDesigner.getLayoutDesigner().endMoving(false);
                }
                getComponentCreator().releasePrecreatedComponent();
            }
            formDesigner.toggleSelectionMode();
            return true;
        }

        @Override
        void oldMove(Point p) {
            LayoutSupportManager laysup = targetContainer.getLayoutSupport();
            Container cont = (Container) formDesigner.getComponent(targetContainer);
            Container contDel = targetContainer.getContainerDelegate(cont);
            Point posInCont = convertPointToComponent(p.x, p.y, contDel);
            Point posInComp = new Point(hotSpot.x - originalBounds[0].x + convertPoint.x,
                                        hotSpot.y - originalBounds[0].y + convertPoint.y);
            index = laysup.getNewIndex(cont, contDel,
                                       showingComponents[0], -1,
                                       posInCont, posInComp);
            constraints = laysup.getNewConstraints(cont, contDel,
                                                   showingComponents[0], -1,
                                                   posInCont, posInComp);
        }

        @Override
        void oldPaintFeedback(Graphics2D g, Graphics gg, int index) {
            LayoutSupportManager laysup = targetContainer.getLayoutSupport();
            Container cont = (Container) formDesigner.getComponent(targetContainer);
            Container contDel = targetContainer.getContainerDelegate(cont);
            Point contPos = convertPointFromComponent(0, 0, contDel);
            g.setColor(formSettings.getSelectionBorderColor());
            g.translate(contPos.x, contPos.y);
            Stroke oldStroke = g.getStroke();
            g.setStroke(ComponentDragger.dashedStroke1);
            laysup.paintDragFeedback(cont, contDel,
                                     showingComponents[0],
                                     constraints, this.index,
                                     g);
            g.setStroke(oldStroke);
            g.translate(-contPos.x, -contPos.y);
            paintDraggedComponent(showingComponents[0], gg);
        }
    }
    
    private class NewComponentDropListener implements DropTargetListener {
        private NewComponentDrop newComponentDrop;
        private int dropAction;
        /** Assistant context requested by newComponentDrop. */
        private String dropContext;
        /** Additional assistant context requested by newComponentDrop. */
        private String additionalDropContext;
        
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            try {
                dropAction = dtde.getDropAction();
                newComponentDrop = null;
                Transferable transferable = dtde.getTransferable();
                PaletteItem item = null;
                if (dtde.isDataFlavorSupported(PaletteController.ITEM_DATA_FLAVOR)) {
                    Lookup itemLookup = (Lookup)transferable.getTransferData(PaletteController.ITEM_DATA_FLAVOR);
                    item = itemLookup.lookup(PaletteItem.class);
                } else {
                    ClassSource classSource = CopySupport.getCopiedBeanClassSource(transferable);
                    if (classSource != null) {
                        Class componentClass = getComponentCreator().prepareClass(classSource); // possible failure was reported
                        if (componentClass != null) {
                            item = new PaletteItem(classSource, componentClass);
                        }
                    } else {
                        Lookup.Template<NewComponentDropProvider> template = new Lookup.Template<NewComponentDropProvider>(NewComponentDropProvider.class);
                        Collection<? extends NewComponentDropProvider> providers = Lookup.getDefault().lookup(template).allInstances();
                        for (NewComponentDropProvider provider : providers) {
                            newComponentDrop = provider.processTransferable(getFormModel(), transferable);
                            if (newComponentDrop != null) {
                                dropContext = null;
                                AssistantModel aModel = FormEditor.getAssistantModel(getFormModel());
                                String preContext = aModel.getContext();
                                item = newComponentDrop.getPaletteItem(dtde);
                                String postContext = aModel.getContext();
                                if (!preContext.equals(postContext)) {
                                    dropContext = postContext;
                                    additionalDropContext = aModel.getAdditionalContext();
                                }
                                break;
                            }
                        }
                    }
                }
                //switch to the menu layer if this is a menu component other than JMenuBar
                if(item != null && MenuEditLayer.isMenuRelatedComponentClass(item.getComponentClass()) &&
                        !JMenuBar.class.isAssignableFrom(item.getComponentClass())) {
                    if(!formDesigner.getMenuEditLayer().isDragProxying()) {
                        formDesigner.getMenuEditLayer().startNewMenuComponentDragAndDrop(item);
                        return;
                    }
                }
                if (item != null) {
                    if ((item.getComponentClassName().indexOf('.') != -1) // Issue 79573
                            || FormJavaSource.isInDefaultPackage(getFormModel())) {
                        draggedComponent = new NewComponentDrag(item);
                        if (draggedComponent.isValid()) {
                            draggedComponent.move(dtde.getLocation(), 0);
                            repaint();
                        } else { // failed to create instance of the component (already reported)
                            if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
                                formDesigner.toggleSelectionMode(); // also calls endDragging(null)
                            } else {
                                draggedComponent = null;
                            }
                            dtde.rejectDrag();
                        }
                    } else { // can't use class from default package
                        dtde.rejectDrag();
                    }
                } else { // could not load the dragged class, or dragging something unknown
                    dtde.rejectDrag();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        public void dragOver(java.awt.dnd.DropTargetDragEvent dtde) {
            if (draggedComponent != null) {
                if ((newComponentDrop != null) && (dropAction != dtde.getDropAction())) {
                    dragExit(dtde);
                    dragEnter(dtde);
                    return;
                }
                draggedComponent.move(dtde.getLocation(), 0);
                if (dropContext != null) {
                    FormEditor.getAssistantModel(getFormModel()).setContext(dropContext, additionalDropContext);
                }
                repaint();
            }
        }
        
        @Override
        public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dtde) {
        }
        
        @Override
        public void dragExit(java.awt.dnd.DropTargetEvent dte) {
            if (draggedComponent != null) {
                endDragging(null);
                repaint();
            }
        }
        
        @Override
        public void drop(java.awt.dnd.DropTargetDropEvent dtde) {
            if (draggedComponent != null) {
                NewComponentDrag newComponentDrag = ((NewComponentDrag)draggedComponent);
                try {
                    newComponentDrag.end(dtde.getLocation(), 0);
                } finally {
                    draggedComponent = null;
                    draggingEnded = true;
                }
                if (newComponentDrag.addedComponent != null) {
                    String id = newComponentDrag.addedComponent.getId();
                    if (newComponentDrop != null) {
                        String droppedOverId = null;
                        if (!(newComponentDrag.addedComponent instanceof RADVisualComponent)) {
                            RADComponent comp = getMetaComponentAt(dtde.getLocation(), COMP_DEEPEST);
                            if (comp != null) droppedOverId = comp.getId();
                        }
                        newComponentDrop.componentAdded(id, droppedOverId);
                    }
                }
                formDesigner.toggleSelectionMode();
                formDesigner.requestActive();
            }
        }
    }
}
