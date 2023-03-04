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

package org.netbeans.modules.xml.multiview.ui;

import java.awt.Color;

/**
 * This class defines the visual theme (i.e. colors) of the 
 * multiview editor.
 *
 * Created on September 24, 2003, 9:14 AM
 * @author  bashby, mkuchtiak
 */
public class SectionVisualTheme {

    /** Creates a new instance of SectionColorTheme */
     static Color documentBackgroundColor =  new java.awt.Color(255, 255, 255);
     static Color sectionActiveBackgroundColor =  new java.awt.Color(252, 250, 245);
     static Color documentMarginColor = new java.awt.Color(153, 153, 153);
     static Color sectionHeaderColor = new java.awt.Color(255, 255, 255);
     static Color containerHeaderColor = new java.awt.Color(230, 228, 223);
     static Color sectionHeaderActiveColor = new java.awt.Color(250, 232, 213);
     static Color fillerColor = javax.swing.UIManager.getDefaults().getColor("Button.background"); //NOI18N
     static Color tableHeaderColor = new java.awt.Color(204, 204, 204);
     static Color tableGridColor = new java.awt.Color(255, 255, 255);
     static Color sectionHeaderLineColor = new java.awt.Color(230, 139, 44);
     static Color hyperlinkColor = new java.awt.Color(0, 0, 255);
     static Color hyperlinkColorFocused = new java.awt.Color(04,84,145);
     static Color textColor = new java.awt.Color(0, 0, 0);
     static Color foldLineColor = new java.awt.Color(102, 102, 102);
     
     static Color errorLabelColor = javax.swing.UIManager.getDefaults().getColor("ToolBar.dockingForeground"); //NOI18N
   
     public SectionVisualTheme() {
    }
    
    public static Color getDocumentBackgroundColor(){
        return documentBackgroundColor;
    }
    public static Color getMarginColor(){
        return documentMarginColor;
    }
    public static Color getSectionHeaderColor(){
        return sectionHeaderColor;
    }
    public static Color getContainerHeaderColor(){
        return containerHeaderColor;
    }
    public static Color getSectionHeaderActiveColor(){
        return sectionHeaderActiveColor;
    }
    public static Color getSectionActiveBackgroundColor(){
        return sectionActiveBackgroundColor;
    }
    public static Color getTableHeaderColor(){
        return tableHeaderColor;
    }
    public static Color getTableGridColor(){
        return tableGridColor;
    }
    
    public static Color getSectionHeaderLineColor(){
        return sectionHeaderLineColor;
    }
    
    public static Color getHyperlinkColor(){
        return hyperlinkColor;
    }
    
    public static Color getHyperlinkColorFocused(){
        return hyperlinkColorFocused;
    }
    
    public static Color getTextColor(){
        return textColor;
    }
    
    public static Color getFillerColor(){
        return fillerColor;
    }
    
    public static Color getErrorLabelColor(){
        return errorLabelColor;
    }
    
    public static Color getFoldLineColor(){
        return foldLineColor;
    }
    
}
