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
    
    static public Color getDocumentBackgroundColor(){
        return documentBackgroundColor;
    }
    static public Color getMarginColor(){
        return documentMarginColor;
    }
    static public Color getSectionHeaderColor(){
        return sectionHeaderColor;
    }
    static public Color getContainerHeaderColor(){
        return containerHeaderColor;
    }
    static public Color getSectionHeaderActiveColor(){
        return sectionHeaderActiveColor;
    }
    static public Color getSectionActiveBackgroundColor(){
        return sectionActiveBackgroundColor;
    }
    static public Color getTableHeaderColor(){
        return tableHeaderColor;
    }
    static public Color getTableGridColor(){
        return tableGridColor;
    }
    
    static public Color getSectionHeaderLineColor(){
        return sectionHeaderLineColor;
    }
    
    static public Color getHyperlinkColor(){
        return hyperlinkColor;
    }
    
    static public Color getHyperlinkColorFocused(){
        return hyperlinkColorFocused;
    }
    
    static public Color getTextColor(){
        return textColor;
    }
    
    static public Color getFillerColor(){
        return fillerColor;
    }
    
    static public Color getErrorLabelColor(){
        return errorLabelColor;
    }
    
    static public Color getFoldLineColor(){
        return foldLineColor;
    }
    
}
