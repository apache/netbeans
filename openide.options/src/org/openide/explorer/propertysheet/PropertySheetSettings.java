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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    public void setPropertyPaintingStyle(int style) {
    }

    /*
    * Returns mode of showing properties.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    *
    * @return <CODE>int</CODE> mode of showing properties.
    * @see #setExpert
    */
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
    public void setPlastic(boolean plastic) {
    }

    /*
    * Returns true if buttons in sheet are plastic.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    public boolean getPlastic() {
        return false;
    }

    /*
    * Sets foreground color of values.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
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
    public void setDisabledPropertyColor(Color color) {
    }

    /*
    * Gets foreground color of values.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
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
     * implementation does not support this kind of filtering of properties.    */
    public boolean getDisplayWritableOnly() {
        return false;
    }
}
