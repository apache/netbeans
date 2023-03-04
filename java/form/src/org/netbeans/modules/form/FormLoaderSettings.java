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

package org.netbeans.modules.form;

import java.awt.Color;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import org.openide.util.HelpCtx;
import org.netbeans.modules.form.codestructure.*;
import org.openide.util.NbPreferences;

/**
 * Settings for the form editor.
 */
public class FormLoaderSettings implements HelpCtx.Provider   {
    private static final FormLoaderSettings INSTANCE = new FormLoaderSettings();
    public static final String PROP_USE_INDENT_ENGINE = "useIndentEngine"; // NOI18N
    /** Property name of the event listener code generation style option. */
    public static final String PROP_LISTENER_GENERATION_STYLE = "listenerGenerationStyle"; // NOI18N
    /** Property name of the selectionBorderSize property */
    public static final String PROP_SELECTION_BORDER_SIZE = "selectionBorderSize"; // NOI18N
    /** Property name of the selectionBorderColor property */
    public static final String PROP_SELECTION_BORDER_COLOR = "selectionBorderColor"; // NOI18N
    /** Property name of the connectionBorderColor property */
    public static final String PROP_CONNECTION_BORDER_COLOR = "connectionBorderColor"; // NOI18N
    /** Property name of the dragBorderColor property */
    public static final String PROP_DRAG_BORDER_COLOR = "dragBorderColor"; // NOI18N
    /** Property name of the guidingLineColor property */
    public static final String PROP_GUIDING_LINE_COLOR = "guidingLineColor"; // NOI18N
    /** Property name of the formDesignerBackgroundColor property */
    public static final String PROP_FORMDESIGNER_BACKGROUND_COLOR =
        "formDesignerBackgroundColor"; // NOI18N
    /** Property name of the formDesignerBorderColor property */
    public static final String PROP_FORMDESIGNER_BORDER_COLOR =
        "formDesignerBorderColor"; // NOI18N
    /** Property name of the gridX property */
    public static final String PROP_GRID_X = "gridX"; // NOI18N
    /** Property name of the gridY property */
    public static final String PROP_GRID_Y = "gridY"; // NOI18N
    /** Property name of the applyGridToPosition property */
    public static final String PROP_APPLY_GRID_TO_POSITION = "applyGridToPosition"; // NOI18N
    /** Property name of the applyGridToSize property */
    public static final String PROP_APPLY_GRID_TO_SIZE = "applyGridToSize"; // NOI18N
    /** Property name of the variablesModifier property */
    public static final String PROP_VARIABLES_MODIFIER = "variablesModifier"; // NOI18N
    /** Property name of the variablesLocal property */
    public static final String PROP_VARIABLES_LOCAL = "variablesLocal"; // NOI18N

    /** Property name of the autoSetComponentName property */
    public static final String PROP_AUTO_SET_COMPONENT_NAME = "autoSetComponentName"; // NOI18N
    static final int AUTO_NAMING_DEFAULT = 0;
    static final int AUTO_NAMING_ON = 1;
    static final int AUTO_NAMING_OFF = 2;

    /** Property name of the generateMnemonicsCode property */
    public static final String PROP_GENERATE_MNEMONICS = "generateMnemonicsCode"; // NOI18N
    /** Property name of the showMnemonicsDialog property */
    public static final String PROP_SHOW_MNEMONICS_DIALOG = "showMnemonicsDialog"; // NOI18N
    /** Property name of the displayWritableOnly property */
    public static final String PROP_DISPLAY_WRITABLE_ONLY = "displayWritableOnly"; // NOI18N
    /** Property name of the editorSearchPath property */
    public static final String PROP_EDITOR_SEARCH_PATH = "editorSearchPath"; // NOI18N
    /** Property name of the toolBarPalette property */
    public static final String PROP_PALETTE_IN_TOOLBAR = "toolBarPalette"; // NOI18N
    /** Property name of the foldGeneratedCode property. */
    public static final String PROP_FOLD_GENERATED_CODE = "foldGeneratedCode"; // NOI18N
    /** Property name of the assistantShown property. */
    public static final String PROP_ASSISTANT_SHOWN = "assistantShown"; // NOI18N
    /** Property name of the designerLAF property. */
    public static final String PROP_DESIGNER_LAF = "designerLAF"; // NOI18N
    /** Property name of the layout code target property. */
    public static final String PROP_LAYOUT_CODE_TARGET = "layoutCodeTarget"; // NOI18N
    /** Property name of the generate FQN property. */
    public static final String PROP_GENERATE_FQN = "generateFQN"; // NOI18N
    /** Property name of the pad empty property. */
    public static final String PROP_PAD_EMPTY_CELLS = "padEmptyCells"; // NOI18N
    /** Property name of the GridDesigner gap support gap width property. */
    public static final String PROP_GAP_WIDTH = "gapWidth"; // NOI18N
    /** Property name of the GridDesigner gap support gap height property. */
    public static final String PROP_GAP_HEIGHT = "gapHeight"; // NOI18N
    /** Property name of the Free Design's layout visualization setting. */
    public static final String PROP_PAINT_ADVANCED_LAYOUT = "paintAdvancedLayout"; // NOI18N

    /** Name of the property for automatic resources/i18n management.
     * The name refers only to i18n for compatibility reasons. */
    public static final String PROP_AUTO_RESOURCING = "i18nAutoMode"; // NOI18N
    static final int AUTO_RESOURCE_DEFAULT = 0;
    static final int AUTO_RESOURCE_ON = 1;
    static final int AUTO_RESOURCE_OFF = 2;

    /** Array of package names to search for property editors used in Form Editor */
    private static String[] editorSearchPath = null;
    private static final int MIN_SELECTION_BORDER_SIZE = 1;
    private static final int MAX_SELECTION_BORDER_SIZE = 15;

    private static final int MIN_GRID_X = 2;
    private static final int MIN_GRID_Y = 2;

    public static Preferences getPreferences() {
        return NbPreferences.forModule(FormLoaderSettings.class);
    }
    
    public static FormLoaderSettings getInstance() {
        return INSTANCE;
    }

    // property access methods

    public boolean getUseIndentEngine() {
        return getPreferences().getBoolean(PROP_USE_INDENT_ENGINE, false); 
    }

    public void setUseIndentEngine(boolean value) {
        getPreferences().putBoolean(PROP_USE_INDENT_ENGINE, value);
    }

    /**
     * Getter for the event listener code generation style option.
     *
     * @return listener generation style.
     */
    public int getListenerGenerationStyle() {
        return getPreferences().getInt(PROP_LISTENER_GENERATION_STYLE, 0);
    }

    /**
     * Setter for the event listener code generation style option.
     * 
     * @param style listener generation style.
     */
    public void setListenerGenerationStyle(int style) {
        getPreferences().putInt(PROP_LISTENER_GENERATION_STYLE, style);
    }

    /**
     * Getter for the selectionBorderSize option.
     * 
     * @return selection border size.
     */
    public int getSelectionBorderSize() {
        return getPreferences().getInt(PROP_SELECTION_BORDER_SIZE, 1);
    }

    /**
     * Setter for the selectionBorderSize option.
     * 
     * @param value selection border size.
     */
    public void setSelectionBorderSize(int value) {
        if (value < MIN_SELECTION_BORDER_SIZE)
            value = MIN_SELECTION_BORDER_SIZE;
        else if (value > MAX_SELECTION_BORDER_SIZE)
            value = MAX_SELECTION_BORDER_SIZE;

        
        getPreferences().putInt(PROP_SELECTION_BORDER_SIZE, value);
    }

    /**
     * Getter for the selectionBorderColor option.
     * 
     * @return color of selection border.
     */
    public java.awt.Color getSelectionBorderColor() {
        int rgb = getPreferences().getInt(PROP_SELECTION_BORDER_COLOR, new Color(255, 164, 0).getRGB());                
        return new Color(rgb);
    }

    /**
     * Setter for the selectionBorderColor option
     * 
     * @param value color of selection border.
     */
    public void setSelectionBorderColor(java.awt.Color value) {
        if (value == null) {
            return;
        }
        getPreferences().putInt(PROP_SELECTION_BORDER_COLOR, value.getRGB());
    }

    /**
     * Getter for the connectionBorderColor option.
     * 
     * @return color of connection border.
     */
    public java.awt.Color getConnectionBorderColor() {
        int rgb = getPreferences().getInt(PROP_CONNECTION_BORDER_COLOR, Color.red.getRGB());
        return new Color(rgb);
    }

    /**
     * Setter for the connectionBorderColor option.
     * 
     * @param value color of connection border.
     */
    public void setConnectionBorderColor(java.awt.Color value) {
        if (value == null) {
            return;
        }
        getPreferences().putInt(PROP_CONNECTION_BORDER_COLOR, value.getRGB());
    }

    /**
     * Getter for the dragBorderColor option.
     * 
     * @return color of drag border.
     */
    public java.awt.Color getDragBorderColor() {
        int rgb = getPreferences().getInt(PROP_DRAG_BORDER_COLOR, Color.gray.getRGB());
        return new Color(rgb);        
    }

    /**
     * Setter for the dragBorderColor option.
     * 
     * @param value color of drag border.
     */
    public void setDragBorderColor(java.awt.Color value) {
        if (value == null) {
            return;
        }        
        getPreferences().putInt(PROP_DRAG_BORDER_COLOR, value.getRGB());
    }
    
    /**
     * Getter for the guidingLineColor option.
     * 
     * @return color of guiding lines.
     */
    public java.awt.Color getGuidingLineColor() {
        int rgb = getPreferences().getInt(PROP_GUIDING_LINE_COLOR, new Color(143, 171, 196).getRGB());
        return new Color(rgb);        
        
    }

    /**
     * Setter for the dragBorderColor option.
     * 
     * @param value color of guiding lines.
     */
    public void setGuidingLineColor(java.awt.Color value) {
        if (value == null) {
            return;
        }        
        getPreferences().putInt(PROP_GUIDING_LINE_COLOR, value.getRGB());
    }

    /**
     * Getter for the gridX option.
     * 
     * @return size of horizontal grid.
     */
    public int getGridX() {
        return getPreferences().getInt(PROP_GRID_X, 10);
    }

    /**
     * Setter for the gridX option.
     *
     * @param value size of horizontal grid.
     */
    public void setGridX(int value) {
        if (value < MIN_GRID_X) value = MIN_GRID_X;
        getPreferences().putInt(PROP_GRID_X, value);
    }

    /**
     * Getter for the gridY option.
     * 
     * @return size of vertical grid.
     */
    public int getGridY() {        
        return getPreferences().getInt(PROP_GRID_Y, 10);
    }

    /**
     * Setter for the gridY option.
     * 
     * @param value size of vertical grid.
     */
    public void setGridY(int value) {
        if (value < MIN_GRID_Y) value = MIN_GRID_Y;
        getPreferences().putInt(PROP_GRID_Y, value);
    }

    /**
     * Getter for the applyGridToPosition option.
     * 
     * @return determines whether position of component should snap to grid.
     */
    public boolean getApplyGridToPosition() {
        return getPreferences().getBoolean(PROP_APPLY_GRID_TO_POSITION, true);
    }

    /**
     * Setter for the applyGridToPosition option.
     * 
     * @param value determines whether position of component should snap to grid.
     */
    public void setApplyGridToPosition(boolean value) {
        getPreferences().putBoolean(PROP_APPLY_GRID_TO_POSITION, value);
    }

    /**
     * Getter for the applyGridToSize option.
     * 
     * @return determines whether size of component should snap to grid.
     */
    public boolean getApplyGridToSize() {
        return getPreferences().getBoolean(PROP_APPLY_GRID_TO_SIZE, true);
        
    }

    /**
     * Setter for the applyGridToSize option.
     * 
     * @param value determines whether size of component should snap to grid.
     */
    public void setApplyGridToSize(boolean value) {
        getPreferences().putBoolean(PROP_APPLY_GRID_TO_SIZE, value);    
    }

    /**
     * Getter for the variablesLocal option.
     * 
     * @return determines whether variables should be local.
     */
    public boolean getVariablesLocal() {
        return getPreferences().getBoolean(PROP_VARIABLES_LOCAL, false);
    }

    /**
     * Setter for the variablesLocal option.
     *
     * @param value determines whether variables should be local.
     */
    public void setVariablesLocal(boolean value) {
        getPreferences().putBoolean(PROP_VARIABLES_LOCAL, value);
        int variablesModifier = getVariablesModifier();        
        int varType = value ?
            CodeVariable.LOCAL | (variablesModifier & CodeVariable.FINAL)
                               | CodeVariable.EXPLICIT_DECLARATION
            :
            CodeVariable.FIELD | variablesModifier;

        if (value) {            
            variablesModifier &= CodeVariable.FINAL;
            setVariablesModifier(variablesModifier);
        }
    }

    /**
     * Getter for the variablesModifier option.
     * 
     * @return variables modifier.
     */
    public int getVariablesModifier() {
        return getPreferences().getInt(PROP_VARIABLES_MODIFIER, java.lang.reflect.Modifier.PRIVATE);
    }

    /**
     * Setter for the variablesModifier option.
     *
     * @param value variables modifier.
     */
    public void setVariablesModifier(int value) {
        getPreferences().putInt(PROP_VARIABLES_MODIFIER, value);

        int varType;
        if (getVariablesLocal()) {
            varType = CodeVariable.LOCAL | value;
            if ((value & CodeVariable.FINAL) == 0)
                varType |= CodeVariable.EXPLICIT_DECLARATION;
        }
        else varType = CodeVariable.FIELD | value;
    }

    public int getAutoSetComponentName() {
        return getPreferences().getInt(PROP_AUTO_SET_COMPONENT_NAME, AUTO_NAMING_DEFAULT);
    }

    public void setAutoSetComponentName(int value) {
        getPreferences().putInt(PROP_AUTO_SET_COMPONENT_NAME, value);
    }

    /**
     * Getter for the generateMnemonicsCode option.
     * 
     * @return determines whether to generate <code>Mnemonics</code> code.
     */
    public boolean getGenerateMnemonicsCode() {
        return getPreferences().getBoolean(PROP_GENERATE_MNEMONICS, false);
    }

    /**
     * Setter for the generateMnemonicsCode option.
     *
     * @param value determines whether to generate <code>Mnemonics</code> code.
     */
    public void setGenerateMnemonicsCode(boolean value) {
        getPreferences().putBoolean(PROP_GENERATE_MNEMONICS, value);
    }

    public boolean getDisplayWritableOnly() {
        return getPreferences().getBoolean(PROP_DISPLAY_WRITABLE_ONLY, true);
    }

    public void setDisplayWritableOnly(boolean value) {
        getPreferences().putBoolean(PROP_DISPLAY_WRITABLE_ONLY, value);
    }

    private static final boolean USE_STORED_SEARCH_PATH = Boolean.getBoolean("nb.form.useStoredSearchPath"); // Issue 163705
    private static final String DEFAULT_EDITOR_SEARCH_PATH = ""; // NOI18N
    
    /**
     * Getter for the editorSearchPath option.
     *
     * @return property editor search path.
     */
    public String[] getEditorSearchPath() {
        if (editorSearchPath == null) {
            if (USE_STORED_SEARCH_PATH) {
                editorSearchPath = translatedEditorSearchPath(
                    toArray(getPreferences().get(PROP_EDITOR_SEARCH_PATH, DEFAULT_EDITOR_SEARCH_PATH)));
            } else {
                editorSearchPath = toArray(DEFAULT_EDITOR_SEARCH_PATH);
            }
        }
        return editorSearchPath;
    }

    /**
     * Setter for the editorSearchPath option.
     * 
     * @param value property editor search path.
     */
    public void setEditorSearchPath(String[] value) {
        editorSearchPath = value;
        if (USE_STORED_SEARCH_PATH) {
            getPreferences().put(PROP_EDITOR_SEARCH_PATH, fromArray(editorSearchPath));
        }
    }

    public boolean isPaletteInToolBar() {
        return getPreferences().getBoolean(PROP_PALETTE_IN_TOOLBAR, false);
    }

    public void setPaletteInToolBar(boolean value) {
        getPreferences().putBoolean(PROP_PALETTE_IN_TOOLBAR, value);    
    }

    /**
     * Getter for the formDesignerBackgroundColor option.
     * 
     * @return background color of the designer.
     */
    public java.awt.Color getFormDesignerBackgroundColor() {
        Color defaultBackground = UIManager.getColor("nb.formdesigner.background"); //NOI18N
        if (defaultBackground == null)
            defaultBackground = UIManager.getColor("Tree.background"); //NOI18N
        if (defaultBackground == null)
            defaultBackground = Color.white;
        int rgb = getPreferences().getInt(PROP_FORMDESIGNER_BACKGROUND_COLOR , defaultBackground.getRGB());
        return new Color(rgb);        
    }

    /**
     * Setter for the formDesignerBackgroundColor option.
     * 
     * @param value background color of the designer.
     */
    public void setFormDesignerBackgroundColor(java.awt.Color value) {
        if (value == null)
            return;
        getPreferences().putInt(PROP_FORMDESIGNER_BACKGROUND_COLOR , value.getRGB());    
    }

    /**
     * Getter for the formDesignerBorderColor option.
     * 
     * @return color of the border of the designer.
     */
    public java.awt.Color getFormDesignerBorderColor() {
        Color defaultBackground = UIManager.getColor("nb.formdesigner.borderColor"); //NOI18N
        if (defaultBackground == null)
            defaultBackground = new Color(224, 224, 255);
        int rgb = getPreferences().getInt(PROP_FORMDESIGNER_BORDER_COLOR , defaultBackground.getRGB());
        return new Color(rgb);        
    }

    /**
     * Setter for the formDesignerBorderColor option.
     * 
     * @param value color of the border of the designer.
     */
    public void setFormDesignerBorderColor(java.awt.Color value) {
        if (value == null)
            return;
        getPreferences().putInt(PROP_FORMDESIGNER_BORDER_COLOR , value.getRGB());    
    }
    
    /**
     * Getter for the foldGeneratedCode option
     * 
     * @return <code>true</code> if the code should be folded,
     * returns <code>false</code> otherwise.
     */
    public boolean getFoldGeneratedCode() {
        return getPreferences().getBoolean(PROP_FOLD_GENERATED_CODE, true);    
    }

    /**
     * Setter for the foldGeneratedCode option.
     *
     * @param value determines whether the code should be folded.
     */
    public void setFoldGeneratedCode(boolean value) {
        getPreferences().putBoolean(PROP_FOLD_GENERATED_CODE, value);    
    }

    /**
     * Getter for the assistantShown option.
     * 
     * @return <code>true</code> if the assistant should be shown,
     * return <code>false</code> otherwise.
     */
    public boolean getAssistantShown() {
        return getPreferences().getBoolean(PROP_ASSISTANT_SHOWN, true);    
    }

    /**
     * Setter for the foldGeneratedCode option.
     * 
     * @param value determines whether the assistant should be shown.
     */
    public void setAssistantShown(boolean value) {
        getPreferences().putBoolean(PROP_ASSISTANT_SHOWN, value);    
    }

    public int getLayoutCodeTarget() {
        return getPreferences().getInt(PROP_LAYOUT_CODE_TARGET, 0);    
    }

    public void setLayoutCodeTarget(int target) {
        getPreferences().putInt(PROP_LAYOUT_CODE_TARGET, target);    
    }

    public int getI18nAutoMode() {
        return getPreferences().getInt(PROP_AUTO_RESOURCING, 0);    
    }

    public void setI18nAutoMode(int mode) {
        getPreferences().putInt(PROP_AUTO_RESOURCING, mode);    
    }

    public boolean getGenerateFQN() {
        return getPreferences().getBoolean(PROP_GENERATE_FQN, true);    
    }

    public void setGenerateFQN(boolean generateFQN) {
        getPreferences().putBoolean(PROP_GENERATE_FQN, generateFQN);    
    }

    public boolean getPadEmptyCells() {
        return getPreferences().getBoolean(PROP_PAD_EMPTY_CELLS, true);
    }

    public void setPadEmptyCells(boolean padEmptyCells) {
        getPreferences().putBoolean(PROP_PAD_EMPTY_CELLS, padEmptyCells);
    }
   
    public int getGapWidth() {
        return getPreferences().getInt(PROP_GAP_WIDTH, -1);
    }

    public void setGapWidth(int gapWidth) {
        getPreferences().putInt(PROP_GAP_WIDTH, gapWidth);
    }
   
    public int getGapHeight() {
        return getPreferences().getInt(PROP_GAP_HEIGHT, -1);
    }

    public void setGapHeight(int gapHeight) {
        getPreferences().putInt(PROP_GAP_HEIGHT, gapHeight);
    }

    /**
     * @return 0: don't paint anything, 1: paint anchors and alignment,
     *         2: paint gaps, 3: paint everything (anchors, alignment, gaps)
     */
    public int getPaintAdvancedLayoutInfo() {
        return getPreferences().getInt(PROP_PAINT_ADVANCED_LAYOUT, 3);
    }

    public void setPaintAdvancedLayoutInfo(int paintLevel) {
        getPreferences().putInt(PROP_PAINT_ADVANCED_LAYOUT, paintLevel);
    }

    private static String[] toArray(String esp) {
        return esp.split(" , ");//NOI18N
    }
    
    private static String fromArray(String[] items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            sb.append(items[i]);
            if (i < items.length-1) {
                sb.append(" , ");//NOI18N
            }
        }
        return sb.toString();        
    }
    
    // XXX(-tdt) Hmm, backward compatibility with com.netbeans package name
    // again. The property editor search path is stored in user settings, we
    // must translate    
    private  static String[] translatedEditorSearchPath(String[] eSearchPath) {
        String[] retval = new String[eSearchPath.length];
        for (int i = 0; i < eSearchPath.length; i++) {
            String path = eSearchPath[i];
            path = org.openide.util.Utilities.translate(path + ".BogusClass"); // NOI18N
            path = path.substring(0, path.length() - ".BogusClass".length()); // NOI18N
            retval[i] = path;
        }        
        return retval;
    }

    /** This method must be overriden. It returns display name of this options.
     * 
     * @return display name.
     */
    public String displayName() {
        return FormUtils.getBundleString("CTL_FormSettings"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.configuring"); // NOI18N
    } 
}
