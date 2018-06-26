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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
