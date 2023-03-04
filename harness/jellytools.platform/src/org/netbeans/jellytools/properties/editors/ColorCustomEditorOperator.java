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

package org.netbeans.jellytools.properties.editors;

/*
 * ColorCustomEditorOperator.java
 *
 * Created on June 13, 2002, 4:01 PM
 */

import java.awt.Color;
import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling Color Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class ColorCustomEditorOperator extends NbDialogOperator {

    private JColorChooserOperator _colorChooser=null;

    /** Creates a new instance of FileCustomEditorOperator
     * @param title String title of custom editor */
    public ColorCustomEditorOperator(String title) {
        super(title);
    }
    
    /** Creates a new instance of FileCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public ColorCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    /** locates and returns JColorChooserOperator
     * @return JColorChooserOperator */    
    public JColorChooserOperator colorChooser() {
        if (_colorChooser==null) {
            _colorChooser=new JColorChooserOperator(this);
        }
        return _colorChooser;
    }
    
    /** returns edited color
     * @return Color */    
    public Color getColorValue() {
        return colorChooser().getColor();
    }
    
    /** sets edited color
     * @param color Color */    
    public void setColorValue(Color color) {
        colorChooser().setColor(color);
    }
    
    /** sets edited color
     * @param r int red
     * @param g int green
     * @param b int blue */    
    public void setRGBValue(int r, int g, int b) {
        colorChooser().setColor(r, g, b);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        colorChooser();
    }
}
