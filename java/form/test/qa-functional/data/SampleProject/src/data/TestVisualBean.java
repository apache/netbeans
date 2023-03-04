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

package data;

import java.awt.*;
import java.io.Serializable;

/**
 *  Simple visual javabean
 *
 * @author Jiri Vagner
 */
public class TestVisualBean extends Canvas implements Serializable {
    
    private Color textColor = Color.blue;
    private String text = "Lancia Lybra"; // NOI18N
    
    /** Creates a new instance of VisualTestBean */
    public TestVisualBean() {
        resize(100,40);
    }
    
    public void paint(Graphics g) {
        g.setColor(Color.blue);
        g.drawString(text,10, 10);
    }
    
    /**
     * Returns color value
     * @return Color value
     */
    public Color getColor() {
        return textColor;
    }
    
    /**
     * Sets new color
     * @param newColor value
     */
    public void setColor(Color newColor) {
        textColor = newColor;
        repaint();
    }
    
    /**
     * Returns text
     * @return text
     */
    public String getText() {
        return text;
    }
    
    /**
     * Sets text value
     * @param newText 
     */
    public void setText(String newText) {
        text = newText;
        repaint();
    }
}
