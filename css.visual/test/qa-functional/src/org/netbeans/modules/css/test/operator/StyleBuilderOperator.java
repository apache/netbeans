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
package org.netbeans.modules.css.test.operator;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JColorChooserOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import static org.netbeans.modules.css.test.operator.StyleBuilderOperator.Panes.*;

/**Keeps methods to access style builder component.
 *
 * @author Jindrich Sedek
 */
public class StyleBuilderOperator extends TopComponentOperator{
    private JTabbedPaneOperator tabbles;
    private static final String uiBundle = "org.netbeans.modules.css.visual.ui.Bundle";
    private static final String DIALOG_NAME =
            Bundle.getString(uiBundle, "FONT_FAMILY_EDITOR_TITLE");
    
    /** CSS StyleBuilderOperator is created.
     *  Builder window must be displayed.
     */
    public StyleBuilderOperator(String ruleName){
        super(waitTopComponent(null, ruleName + " - Style Builder", 0, new StyleBuilderComponentChooser()));
    }
    
    /** This function dislays CSS style builder window and returns operator for it
     *
     *@return navigator operator
     *
     */
    public StyleBuilderOperator invokeBuilder() {
        new StyleBuilderAction().perform();
        return new StyleBuilderOperator("");
    }
    
    public CSSPaneOperator setPane(Panes panes){
        getTabs().selectPage(panes.title());
        switch (panes){
        case FONT:
            return new FontPaneOperator(this);
        case BACKGROUND:
            return new BackgroundPaneOperator(this);
        }
        return null;
    }
    
    private JTabbedPaneOperator getTabs(){
        if (tabbles == null){
            tabbles = new JTabbedPaneOperator(this, 0);
        }
        return tabbles;
    }
    
    private static final class StyleBuilderAction extends Action{
        private static final String StyleBuilderActionName = "Window|Other|CSS Style Builder";
        
        public StyleBuilderAction() {
            super(StyleBuilderActionName, null);
        }
    }
    
    private static final class StyleBuilderComponentChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.css.visual.api.StyleBuilderTopComponent"));
        }
        
        public String getDescription() {
            return "StyleBuilder Window";
        }
    }
    
    //-------ABSTRACT PANEOPERATOR---------//
    public abstract class CSSPaneOperator{
        protected TopComponentOperator topComp;

        protected Component getComponentByAN(String accessibleName){
            for (Component component : topComp.getComponents()) {
                if (component instanceof JComboBox){
                    if (component.getAccessibleContext().getAccessibleName().equals(accessibleName))
                        return component;
                }
            }
            return null;
        }
    }
    
    //-------FONT----------------//
    public class FontPaneOperator extends CSSPaneOperator{
        public FontPaneOperator(TopComponentOperator _topComp){
            topComp = _topComp;
        }
        
        public EditFontOperator getEditFont(){
            new JButtonOperator(topComp, Bundle.getString(uiBundle, "EDIT")).pushNoBlock();
            return new EditFontOperator();
        }
        
        public JListOperator fontFamilies(){
            return new JListOperator(topComp, 0);
        }
        
        public JListOperator fontSizes(){
            return new JListOperator(topComp, 1);
        }
        
        public JComboBoxOperator fontSizeUnits(){
            return new JComboBoxOperator(topComp, 0);
        }
        
        public JComboBoxOperator fontStyle(){
            return new JComboBoxOperator(topComp, 1);
        }
        
        public JComboBoxOperator fontWeight(){
            return new JComboBoxOperator(topComp, 2);
        }
        
        public JComboBoxOperator fontVariant(){
            return new JComboBoxOperator(topComp, 3);
        }
        
        public JComboBoxOperator fontColor(){
            return new JComboBoxOperator(topComp, 4);
        }
        
        public void overline(boolean b){
            changeSelection(b, new JCheckBoxOperator(topComp, Bundle.getString(uiBundle, "FONT_OVERLINE")));
        }
        
        public void underline(boolean b){
            changeSelection(b, new JCheckBoxOperator(topComp, Bundle.getString(uiBundle, "FONT_UNDERLINE")));
        }
        
        public void strikethrough(boolean b){
            changeSelection(b, new JCheckBoxOperator(topComp, Bundle.getString(uiBundle, "FONT_STRIKETHROUGH")));
        }
        
        public void noDecoration(boolean b){
            changeSelection(b, new JCheckBoxOperator(topComp, Bundle.getString(uiBundle, "NO_DECORATION_1")));
        }
        
        public boolean isOverline(){
            return new JCheckBoxOperator(topComp, Bundle.getString(uiBundle, "FONT_OVERLINE")).isSelected();
        }
        
        private void changeSelection(boolean b, JCheckBoxOperator operator){
            if (operator.isSelected() && !b){
                operator.setSelected(b);
            }else if (!operator.isSelected() && b){
                operator.setSelected(b);
            }
        }
        
        
        public JColorChooserOperator showColorChooser(){
            new JButtonOperator(topComp, 2).pushNoBlock();
            return new JColorChooserOperator(new NbDialogOperator(Bundle.getString(uiBundle, "COLOR_CHOOSER_TITLE")));
        }
    }
    
    //-------BACKGROUND----------//
    public class BackgroundPaneOperator extends CSSPaneOperator{
        private final String backgroundColorTitle = 
                Bundle.getString(uiBundle, "BACKGROUND_COLOR");
        private final String backgroundImage = 
                Bundle.getString(uiBundle, "BACKGROUND_IMAGE");
        private final String tile = 
                Bundle.getString(uiBundle, "BACKGROUNDTILE");
        private final String scroll = 
                Bundle.getString(uiBundle, "BACKGROUND_SCROLL");
        private final String hPosition = 
                Bundle.getString(uiBundle, "BG_HORIZONTAL_POS");
        private final String vPosition = 
                Bundle.getString(uiBundle, "BG_VERTICAL_POS");
        private final String vPositionUnitAN = 
                Bundle.getString(uiBundle, "HORIZPOS_UNIT_COMBO_ACCESSIBLE_NAME");
        private final String hPositionUnitAN = 
                Bundle.getString(uiBundle, "VERTPOS_UNIT_COMBO_ACCESSIBLE_NAME");
        
        public BackgroundPaneOperator(TopComponentOperator _topComp){
            topComp = _topComp;
        }
        
//        public ColorSelectionField getColor(){
//            Component comp = new JLabelOperator(topComp, backgroundColorTitle).getLabelFor();
//            return (ColorSelectionField) comp;
//        }
        
        public JTextFieldOperator getImage(){
            Component comp = new JLabelOperator(topComp, backgroundImage).getLabelFor();
            return new JTextFieldOperator((JTextField)comp);
        }
        
        public JFileChooserOperator getImageDialog(){
            new JButtonOperator(topComp, 1).pushNoBlock();
            return new JFileChooserOperator();
        }
        
        public JComboBoxOperator getTile(){
            Component comp = new JLabelOperator(topComp, tile).getLabelFor();
            return new JComboBoxOperator((JComboBox)comp);
        }
        
        public JComboBoxOperator getScroll(){
            Component comp = new JLabelOperator(topComp, scroll).getLabelFor();
            return new JComboBoxOperator((JComboBox)comp);
        }
        
        public JComboBoxOperator getHPosition(){
            Component comp = new JLabelOperator(topComp, hPosition).getLabelFor();
            return new JComboBoxOperator((JComboBox)comp);
        }
        public JComboBoxOperator getHPositionUnits(){
            return new JComboBoxOperator((JComboBox)getComponentByAN(hPositionUnitAN));
        }
        
        public JComboBoxOperator getVPositionOperator(){
            Component comp = new JLabelOperator(topComp, vPosition).getLabelFor();
            return new JComboBoxOperator((JComboBox)comp);
        }
        public JComboBoxOperator getVPositionUnits(){
            return new JComboBoxOperator((JComboBox)getComponentByAN(vPositionUnitAN));
        }
    }
    //---------------------------//
    public class EditFontOperator extends NbDialogOperator{
        public EditFontOperator(){
            super(DIALOG_NAME);
        }
        
        public void createNewFamily(){
            new JButtonOperator(this, Bundle.getString(uiBundle, "NEW")).push();
        }
        
        public void deleteFamily(){
            new JButtonOperator(this, Bundle.getString(uiBundle, "DELETE")).push();
        }
        
        public void up(){
            new JButtonOperator(this, Bundle.getString(uiBundle, "UP")).push();
        }
        
        public void down(){
            new JButtonOperator(this, Bundle.getString(uiBundle, "DOWN")).push();
        }
        
        public void selectAvailable(int order){
            new JComboBoxOperator(this, 0).selectItem(order);
        }
        
        public void add(){
            new JButtonOperator(this, ">").push();
        }
        
        public void remove(){
            new JButtonOperator(this, "<").push();
        }
        
        public JListOperator currentFontFamilies(){
            return new JListOperator(this, 0);
        }
        
        public JListOperator fonts(){
            return new JListOperator(this, 1);
        }
        
        public JListOperator selected(){
            return new JListOperator(this, 2);
        }
    }
    
    public enum Panes{FONT, BACKGROUND, TEXT_BLOCK, BORDER, MARGIN, POSITION ;
    private static final String FONT_PANE =
            Bundle.getString(uiBundle, "FONT_EDITOR_DISPNAME");
    private static final String BACK_PANE =
            Bundle.getString(uiBundle, "BACKGROUND_EDITOR_DISPNAME");
    private static final String TEXT_PANE =
            Bundle.getString(uiBundle, "TEXTBLOCK_EDITOR_DISPNAME");
    private static final String BORDER_PANE =
            Bundle.getString(uiBundle, "BORDER_EDITOR_DISPNAME");
    private static final String MARGIN_PANE =
            Bundle.getString(uiBundle, "MARGIN_EDITOR_DISPNAME");
    private static final String POSITION_PANE =
            Bundle.getString(uiBundle, "POSITION_EDITOR_DISPNAME");
    
    public String title(){
        switch (this){
        case FONT:
            return FONT_PANE;
        case BACKGROUND:
            return BACK_PANE;
        case TEXT_BLOCK:
            return TEXT_PANE;
        case BORDER:
            return BORDER_PANE;
        case MARGIN:
            return MARGIN_PANE;
        case POSITION:
            return POSITION_PANE;
        }
        return null;
    }
    }
}
