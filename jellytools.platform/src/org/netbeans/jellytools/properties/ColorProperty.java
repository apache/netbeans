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
package org.netbeans.jellytools.properties;

import java.awt.Color;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.ColorCustomEditorOperator;

/** Operator serving property of type Color
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ColorProperty extends Property {

    /** Creates a new instance of ColorProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name
     */
    public ColorProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return ColorCustomEditorOperator */    
    public ColorCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new ColorCustomEditorOperator(getName());
    }
    
    /** getter for RGB value through Custom Editor
     * @param r int red
     * @param g int green
     * @param b int blue */    
    public void setRGBValue(int r, int g, int b) {
        ColorCustomEditorOperator customizer=invokeCustomizer();
        customizer.setRGBValue(r, g, b);
        customizer.ok();
    }        
    
    /** setter for Color value through Custom Editor
     * @param value Color */    
    public void setColorValue(Color value) {
        ColorCustomEditorOperator customizer=invokeCustomizer();
        customizer.setColorValue(value);
        customizer.ok();
    }        
    
    /** getter for Color value through Custom Editor
     * @return Color */    
    public Color getColorValue() {
        Color value;
        ColorCustomEditorOperator customizer=invokeCustomizer();
        value=customizer.getColorValue();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
