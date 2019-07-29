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
package org.openide.explorer.propertysheet;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.Color;

import javax.swing.UIManager;


/**
* Settings for the property sheet.
* @see PropertySheet
*
* @deprecated None of the settings in this class are supported in the new property sheet.  The entire implementation
*            has been gutted to do nothing.
*
* @author Jan Jancura, Ian Formanek
* @version 0.11, May 16, 1998
*/
@Deprecated
public class PropertySheetSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3820718202747868830L;

    /** Property variables. */
    static int propertyPaintingStyle = -1;
    static boolean plastic = false;

    /** When it's true only writable properties are showen. */
    static boolean displayWritableOnly = false;
    static int sortingMode = -1;
    static Color valueColor = null;
    private static Color disabledColor;
    static PropertySheetSettings propertySheetSettings = null;

    static PropertySheetSettings getDefault() {
        return propertySheetSettings;
    }

    public String displayName() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    /*
    * Sets property showing mode.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    @Deprecated
    public void setPropertyPaintingStyle(int style) {
    }

    /*
    * Returns mode of showing properties.
    *
    * @return <CODE>int</CODE> mode of showing properties.
    * @see #setExpert
    *
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    @Deprecated
    public int getPropertyPaintingStyle() {
        return -1;
    }

    /*
    * Sets sorting mode.
    *
    * @param sortingMode New sorting mode.
    */
    public void setSortingMode(int sortingMode) {
    }

    /*
    * Returns sorting mode.
    *
    * @return Sorting mode.
    */
    public int getSortingMode() {
        return -1;
    }

    /*
    * Sets buttons in sheet to be plastic.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    @Deprecated
    public void setPlastic(boolean plastic) {
    }

    /*
    * Returns true if buttons in sheet are plastic.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    @Deprecated
    public boolean getPlastic() {
        return false;
    }

    /*
    * Sets foreground color of values.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    @Deprecated
    public void setValueColor(Color color) {
    }

    /*
    * Gets foreground color of values.
    */
    public Color getValueColor() {
        return null;
    }

    /*
    * Sets foreground color of disabled property.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    @Deprecated
    public void setDisabledPropertyColor(Color color) {
    }

    /*
    * Gets foreground color of values.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    @Deprecated
    public Color getDisabledPropertyColor() {
        return null;
    }

    /*
    * Setter method for visibleWritableOnly property. If is true only writable
    * properties are showen in propertysheet.
    */
    public void setDisplayWritableOnly(boolean b) {
    }

    /*
    * Getter method for visibleWritableOnly property. If is true only writable
    * properties are showen in propertysheet.
    * @deprecated Relic of the original property sheet implementation.  The new propertysheet
    * implementation does not support this kind of filtering of properties.
    */
    @Deprecated
    public boolean getDisplayWritableOnly() {
        return false;
    }
}
