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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.SeparatorWidget;

/**
 * @author Ajit Bhate
 */
public abstract class AbstractTitledWidget extends Widget implements ExpandableWidget {
    
    public static final Color BORDER_COLOR = new Color(169, 197, 235);
    public static final Color TITLE_COLOR = new Color(184, 215, 255);
    public static final Color TITLE_COLOR_BRIGHT = new Color(241, 245, 252);
    public static final Color TITLE_COLOR_PARAMETER = new Color(235,255,255);
    public static final Color TITLE_COLOR_OUTPUT = new Color(240,240,240);
    public static final Color TITLE_COLOR_FAULT = new Color(245,227,225);
    public static final Color TITLE_COLOR_DESC = new Color(240,240,240);
    public static final Color BORDER_COLOR_BLACK = Color.BLACK;
    public static final int RADIUS = 12;
    
    private Color borderColor = BORDER_COLOR;
    private int radius = RADIUS;
    //private int hgap = RADIUS;
    private int cgap = RADIUS;
    private int depth = radius/3;
    
    private boolean expanded;
    private transient Widget headerWidget;
    private transient Widget seperatorWidget;
    private transient Widget contentWidget;
    private transient ExpanderWidget expander;
    
    /**
     * Creates a new instance of RoundedRectangleWidget
     * with default rounded radius and gap and no gradient color
     * @param scene scene this widget belongs to
     * @param radius of the rounded arc
     * @param color color of the border and gradient title
     */
    public AbstractTitledWidget(ObjectScene scene, int radius, Color color) {
        this(scene,radius,radius,radius,color);
    }
    /**
     * Creates a new instance of RoundedRectangleWidget
     * with default rounded radius and gap and no gradient color
     * @param scene scene this widget belongs to
     * @param radius of the rounded arc
     * @param gap for header widget
     * @param gap for content widget
     * @param color color of the border and gradient title
     */
    public AbstractTitledWidget(ObjectScene scene, int radius, int hgap, int cgap, Color color) {
        super(scene);
        this.radius = radius;
        this.borderColor = color;
        //this.hgap = hgap;
        this.cgap = cgap;
        depth = radius/3;
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        setBorder(new RoundedBorder3D(this,radius, depth, 0, 0, borderColor));
        headerWidget = new Widget(getScene());
        headerWidget.setBorder(BorderFactory.createEmptyBorder(hgap, hgap/2));
        headerWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, hgap));
//        headerWidget.setLayout(new BorderLayout(headerWidget));
        addChild(headerWidget);
        seperatorWidget = new SeparatorWidget(getScene(),SeparatorWidget.Orientation.HORIZONTAL);
        seperatorWidget.setForeground(borderColor);
        if(isExpandable()) {
            contentWidget = createContentWidget();
            expanded = ExpanderWidget.isExpanded(this, true);
            if(expanded) {
                expandWidget();
            } else {
                collapseWidget();
            }
            expander = new ExpanderWidget(getScene(), this, expanded);
        }
        getActions().addAction(scene.createSelectAction());
    }
    
    protected Widget getContentWidget() {
        return contentWidget;
    }
    
    protected final Widget createContentWidget() {
        Widget widget = new Widget(getScene());
        widget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, cgap));
        widget.setBorder(BorderFactory.createEmptyBorder(cgap));
        return widget;
    }

    protected final Widget createHeaderWidget() {
        Widget widget = new Widget(getScene());
        widget.setLayout(LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, radius));
        return widget;
    }

    protected Widget getHeaderWidget() {
        return headerWidget;
    }
    
    protected ExpanderWidget getExpanderWidget() {
        return expander;
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings(" NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    protected final void paintWidget() {
        Rectangle bounds = getClientArea();
        Graphics2D g = getGraphics();
        Paint oldPaint = g.getPaint();
        RoundRectangle2D rect = new RoundRectangle2D.Double(bounds.x + 0.75f,
                bounds.y+ 0.75f, bounds.width - 1.5f, bounds.height - 1.5f, radius, radius);
        if(isExpanded()) {
            int titleHeight = headerWidget.getBounds().height;
            Area titleArea = new Area(rect);
            titleArea.subtract(new Area(new Rectangle(bounds.x,
                    bounds.y + titleHeight, bounds.width, bounds.height)));
            g.setPaint(getTitlePaint(titleArea.getBounds()));
            g.fill(titleArea);
            if(isOpaque()) {
                Area bodyArea = new Area(rect);
                bodyArea.subtract(titleArea);
                g.setPaint(getBackground());
                g.fill(bodyArea);
            }
        } else {
            g.setPaint(getTitlePaint(bounds));
            g.fill(rect);
        }
        g.setPaint(oldPaint);
    }

    protected Paint getTitlePaint(Rectangle bounds) {
        return new GradientPaint(bounds.x, bounds.y, TITLE_COLOR_BRIGHT,
                    bounds.x, bounds.y + bounds.height, TITLE_COLOR);
    }
    
    protected void collapseWidget() {
        if(seperatorWidget.getParentWidget()!=null) {
            removeChild(seperatorWidget);
        }
        if(getContentWidget().getParentWidget()!=null) {
            removeChild(getContentWidget());
        }
    }
    
    protected void expandWidget() {
        if(seperatorWidget.getParentWidget()==null) {
            addChild(seperatorWidget);
        }
        if(getContentWidget().getParentWidget()==null) {
            addChild(getContentWidget());
        }
    }
    
    public Object hashKey() {
        return null;
    }
    
    public void setExpanded(boolean expanded) {
        if(!isExpandable()) return;
        if(this.expanded != expanded) {
            this.expanded = expanded;
            revalidate(true);
            if(expanded) {
                expandWidget();
            } else {
                collapseWidget();
            }
            getScene().validate();
            expander.setSelected(expanded);
        }
    }
    
    public boolean isExpanded() {
        return expanded;
    }
    
    protected boolean isExpandable() {
        return true;
    }
    
    protected void notifyAdded() {
        super.notifyAdded();
        final Object key = hashKey();
        if(key!=null) {
            getObjectScene().addObject(key, this);
        }
    }
    
    protected void notifyRemoved() {
        super.notifyRemoved();
        Object key = hashKey();
        if(key!=null&&getObjectScene().isObject(key)) {
            getObjectScene().removeObject(key);
        }
    }
    
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (previousState.isSelected() != state.isSelected())
            revalidate(true);
    }
    
    protected ObjectScene getObjectScene() {
        return (ObjectScene)super.getScene();
    }
}
