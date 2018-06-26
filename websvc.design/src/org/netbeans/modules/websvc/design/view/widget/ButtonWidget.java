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
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author Ajit Bhate
 */
public class ButtonWidget extends ImageLabelWidget implements PropertyChangeListener{
    
    private Action action;
    private char mnemonics_key = KeyEvent.CHAR_UNDEFINED;
    private HotKeyAction mnemAction;
    public static final int BORDER_RADIUS = 3;
    private Object key = new Object();
    
    private Object selectedImage;
    private boolean selected;

    /**
     *
     * @param scene
     * @param text
     */
    public ButtonWidget(Scene scene, String text) {
        this(scene, null, text);
    }
    
    
    /**
     *
     * @param scene
     * @param icon
     */
    public ButtonWidget(Scene scene, Image image) {
        this(scene, image, null);
    }
    
    
    /**
     *
     * @param scene
     * @param icon
     * @param text
     */
    public ButtonWidget(Scene scene, Image image, String text) {
        super(scene, image ,text);
        setRoundedBorder(BORDER_RADIUS,0,0,null);
        getActions().addAction(ActionFactory.createSelectAction(ButtonSelectProvider.DEFAULT));
        getActions().addAction(scene.createWidgetHoverAction());
    }
    
    
    /**
     *
     * @param scene
     * @param action
     */
    public ButtonWidget(Scene scene, Action action) {
        this(scene, null, getActionName(action));
        setAction(action);
        setToolTipText(getActionTooltip(action));
        setMnemonics(getActionMnemonics(action));
    }
    
    /**
     * Sets the button border as rounded border.
     * @param radius radius of the rounded border. 
     *          If radius is &lt;=0 then rectangular border will be created.
     * @param hgap horizontal gap
     * @param vgap vertical gap
     * @param borderColor color of for the border. If null default color will be used.
     */
    public void setRoundedBorder(int radius, int hgap, int vgap, Color borderColor) {
        setBorder(new ButtonBorder(this,new Insets(radius+vgap,radius+hgap,
                radius+vgap,radius+hgap),radius,borderColor));
    }

    /**
     *
     * @param action
     */
    public void setAction(Action action) {
        if(this.action!=null) {
            this.action.removePropertyChangeListener(this);
        }
        this.action = action;
        if(this.action!=null) {
            this.action.addPropertyChangeListener(this);
            setButtonEnabled(action.isEnabled());
        }
    }
    
    public void setMnemonics(char c) {
        mnemonics_key = Character.toUpperCase(c);
        if(mnemonics_key!=KeyEvent.CHAR_UNDEFINED && mnemAction == null) {
            mnemAction = new HotKeyAction(new ButtonHotKeyProvider(this));
            getScene().getActions().addAction(mnemAction);
        }
    }
    
    public char getMnemonics() {
        return mnemonics_key;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource()==action && "enabled".equals(evt.getPropertyName())) {
            setButtonEnabled((Boolean)evt.getNewValue());
        }
    }
    
    /**
     * Changed method name so that it doesnt clash with Widget.setEnabled.
     * @param v
     */
    public void setButtonEnabled(boolean v) {
        setEnabled(v);
        setPaintAsDisabled(!v);
        revalidate(true);
    }
    
    /**
     * Changed method name so that it doesnt clash with Widget.isEnabled.
     * @return
     */
    public boolean isButtonEnabled() {
        return isEnabled();
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean flag) {
        if(selected!=flag) {
            selected=flag;
            if(selectedImage!=null) 
                setImage(selectedImage);
            revalidate(true);
        }
    }
    
    public final void setImage(Object image) {
        this.selectedImage = getImage();
        if(selectedImage==null)
            this.selectedImage = getImageWidget();
        if(image instanceof Image)
            super.setImage((Image)image);
        if(image instanceof Widget)
            super.setImage((Widget)image);
    }

    public final void setSelectedImage(Object selectedImage) {
        this.selectedImage = selectedImage;
    }

    /**
     * Called when mouse is clicked on the widget.
     */
    public void performAction() {
        //simply delegate to swing action
        if(isButtonEnabled() && action!=null) {
            action.actionPerformed(new ActionEvent(this,0, getActionCommand()));
        }
    }
    
    public String getActionCommand() {
        return (String)action.getValue(Action.ACTION_COMMAND_KEY);
    }
    
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (previousState.isWidgetAimed() != state.isWidgetAimed() ||
                previousState.isWidgetHovered() != state.isWidgetHovered() ||
                previousState.isFocused() != state.isFocused())
            revalidate(true);
    }
    
    /**
     * Subclasses may override this.
     * @see SelectProvider.isAimingAllowed
     */
    protected boolean isAimingAllowed() {
        return true;
    }
    
    /**
     * Subclasses may override this.
      */
    protected Object hashKey() {
        return key;
    }
    
    @Override
    protected void notifyAdded() {
        super.notifyAdded();
        Scene scene = getScene();
        if(scene instanceof ObjectScene) {
            ObjectScene objectScene = (ObjectScene)scene;
            objectScene.addObject(hashKey(), this);
        }
    }
    
    @Override
    protected void notifyRemoved() {
        super.notifyRemoved();
        Scene scene = getScene();
        if(scene instanceof ObjectScene) {
            ((ObjectScene)scene).removeObject(hashKey());
        }
    }
    
    private static String getActionName(Action action) {
        return (String)action.getValue(Action.NAME);
    }
    
    private static Image getActionIcon(Action action) {
        Object icon = action.getValue(Action.SMALL_ICON);
        return (icon instanceof ImageIcon ? ((ImageIcon)icon).getImage(): null);
    }

    private static String getActionTooltip(Action action) {
        return (String)action.getValue(Action.SHORT_DESCRIPTION);
    }

    private static char getActionMnemonics(Action action) {
        String name = getActionName(action);
        if (name == null) return KeyEvent.CHAR_UNDEFINED;
        Integer mKey = (Integer) action.getValue(Action.MNEMONIC_KEY);
        if(mKey==null) return KeyEvent.CHAR_UNDEFINED;
        if(mKey<0||name.length()<mKey) return KeyEvent.CHAR_UNDEFINED;
        return name.charAt(mKey);
    }

    protected static class ButtonBorder implements Border {
        private ButtonWidget button;
        //private Color borderColor;
        private Insets insets;
        private int radius;
        
        public ButtonBorder(ButtonWidget button, Insets insets, int radius, Color borderColor) {
            this.button = button;
            this.insets = insets;
            this.radius = radius;
            //this.borderColor = borderColor!=null?borderColor:BORDER_COLOR;
        }
        
        public Insets getInsets() {
            return insets;
        }
        
        public void paint(Graphics2D g2, Rectangle rect) {
            Paint oldPaint = g2.getPaint();
            
            RoundRectangle2D buttonRect = new RoundRectangle2D.Double
                    (rect.x+0.5f, rect.y+0.5f, rect.width-1f, rect.height-1f, radius*2, radius*2);
            if (button.isButtonEnabled()) {
                
                if (button.isOpaque()){
                    g2.setPaint(button.isSelected()?BACKGROUND_COLOR_PRESSED:
                        new GradientPaint(
                            0, rect.y , BACKGROUND_COLOR_1,
                            0, rect.y + rect.height * 0.5f,
                            BACKGROUND_COLOR_2, true));
                    g2.fill(buttonRect);
                }
                if (button.getState().isWidgetAimed()) {
                    g2.setPaint(BACKGROUND_COLOR_PRESSED);
                    g2.fill(buttonRect);
                } else {
                    Area s = new Area(buttonRect);
                    Area inner = new Area(new RoundRectangle2D.Double(rect.x + 2.5f, rect.y + 2.5f,
                                rect.width - 5f, rect.height - 5f, radius*2, radius*2));
                    s.subtract(inner);
                    if (button.getState().isHovered()) {
                        g2.setPaint(HOVER_COLOR);
                        g2.fill(s);
                    } else if (button.getState().isFocused()) {
                        g2.setPaint(BORDER_COLOR);
                        g2.fill(s);
                    }
                }
                g2.setPaint(BORDER_COLOR);
                g2.draw(buttonRect);
            } else {
                if(button.isOpaque()) {
                    g2.setPaint(BACKGROUND_COLOR_DISABLED);
                    g2.fill(buttonRect);
                }
                g2.setPaint(grayFilter(BORDER_COLOR));
                g2.draw(buttonRect);
            }
            
            g2.setPaint(oldPaint);
        }
        
        public boolean isOpaque() {
            return false;
        }
        
    }
    
    
    private static Color grayFilter(Color color) {
        int y = Math.round(0.299f * color.getRed()
                + 0.587f * color.getGreen()
                + 0.114f * color.getBlue());
        
        if (y < 0) {
            y = 0;
        } else if (y > 255) {
            y = 255;
        }
        
        return new Color(y, y, y);
    }
    
    protected static final Color BORDER_COLOR = new Color(0x7F9DB9);
    private static final Color HOVER_COLOR = new Color(0xFF9900);
    private static final Color BACKGROUND_COLOR_1 = new Color(0xD2D2DD);
    private static final Color BACKGROUND_COLOR_2 = new Color(0xF8F8F8);
    private static final Color BACKGROUND_COLOR_PRESSED = new Color(0xCCCCCC);
    private static final Color BACKGROUND_COLOR_DISABLED = new Color(0xE4E4E4);
    
    private final static class ButtonSelectProvider implements SelectProvider {
        
        public static ButtonSelectProvider DEFAULT = new ButtonSelectProvider();
        
        public ButtonSelectProvider() {
        }
        
        public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return widget instanceof ButtonWidget && ((ButtonWidget)widget).
                    isAimingAllowed() && widget.isHitAt(localLocation);
        }
        
        public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return widget instanceof ButtonWidget && widget.isHitAt(localLocation);
        }
        
        public void select(Widget widget, Point localLocation, boolean invertSelection) {
            if(widget instanceof ButtonWidget)
                ((ButtonWidget)widget).performAction();
        }
        
    }

    public final static class ButtonHotKeyProvider implements HotKeyAction.HotKeyProvider {

        private ButtonWidget widget;
        
        public ButtonHotKeyProvider (ButtonWidget widget) {
            this.widget = widget;
        }

        public boolean processHotKey(Widget w, char mnemonics) {
            if(widget.isVisible() && widget.getMnemonics()==mnemonics) {
                widget.performAction();
                return true;
            }
            return false;
        }

    }
}
