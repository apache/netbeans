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

package org.netbeans.modules.cnd.asm.core.assistance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import java.util.List;

import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;

public class CodeAnnotationSidebar extends JComponent 
                                   implements ComponentListener {

    private List<AnnotationEntry> curAnnotations;
    
    private static final int SIDEBAR_WIDTH = 9;    
    private static final Color colorBorder = new Color(102, 102, 102);
    
    private final EditorUI editorUI;
    private final JTextComponent target;
    
    private boolean enabled;
    
    public CodeAnnotationSidebar(JTextComponent target) {
        this.target = target;
        this.editorUI = Utilities.getEditorUI(target);
        
        enabled = true;
        
        target.addComponentListener(this);
        updatePreferredSize();
    }

    public void setAnnotations(List<AnnotationEntry> annotations) {                
        this.curAnnotations = annotations;
        
        repaint();
    }
    
    private boolean enableSideBarComponent(boolean enable){
        if (enable == enabled) {
            return false;
        }
        enabled = enable;
        updatePreferredSize();
        return true;
    }
    
    private void updatePreferredSize() {
        if (enabled) {
            setPreferredSize(new Dimension(SIDEBAR_WIDTH, target.getHeight()));    
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }else{
            setPreferredSize(new Dimension(0,0));
            setMaximumSize(new Dimension(0,0));
        }
        revalidate();
    }
    
    @Override
    public void paintComponent(Graphics g) {        
        super.paintComponent(g);
        
        Rectangle clip = g.getClipBounds();
        if (clip.y >= 16) {            
            clip.y -= 16;
            clip.height += 16;
        }

        JTextComponent component = editorUI.getComponent();
        if (component == null) return;

        BaseTextUI textUI = (BaseTextUI)component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;

        g.setColor(target.getBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        
        try {            
            int startPos = textUI.getPosFromY(clip.y);
            int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
            int rootViewCount = rootView.getViewCount();

            if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                
                Rectangle rec = textUI.modelToView(component, rootView.getView(startViewIndex).getStartOffset());
                int y = (rec == null) ? 0 : rec.y;               

                int clipEndY = clip.y + clip.height;
                Element rootElem = textUI.getRootView(component).getElement();
                  
                if (curAnnotations == null)
                    return;
                
                for (int i = startViewIndex; i < rootViewCount; i++){
                    View view = rootView.getView(i);
                    int line = rootElem.getElementIndex(view.getStartOffset()) + 1;
                    
                    AnnotationEntry an = getAnnotationAtLine(line);                                        
                    if (an != null) {
                        g.setColor(an.getColor());
                        
                        g.fillRect(3, y, SIDEBAR_WIDTH - 3, editorUI.getLineHeight());
                        g.setColor(colorBorder);
                        int y1 = y + editorUI.getLineHeight();
                        if (line == an.getStartAnn()) {
                            g.drawLine(2, y, SIDEBAR_WIDTH - 1, y);
                        }
                        g.drawLine(2, y, 2, y1);                       
                        if (line == an.getEndAnn()) {
                            g.drawLine(2, y1, SIDEBAR_WIDTH - 1, y1);                        
                        }
                    }
                    y += editorUI.getLineHeight();
                    if (y >= clipEndY) {
                        break;
                    }
                }
            }
            revalidate();
        } catch (BadLocationException ble){
            
        }
    }
        
    private AnnotationEntry getAnnotationAtLine(int line) {
        for (AnnotationEntry ann : curAnnotations) {
            if (ann.getStartAnn() > line) 
                return null;
            if (ann.getEndAnn() >= line)
                return ann;
        }
        
        return null;
    }
    
    public static class AnnotationEntry {

        private final Color color;
        private final int startAnn;
        private final int endAnn;

        public AnnotationEntry(Color color, int start, int end) {
            this.color = color;
            this.startAnn = start;
            this.endAnn = end;
        }

        public Color getColor() {
            return color;
        }

        public int getStartAnn() {
            return startAnn;
        }

        public int getEndAnn() {
            return endAnn;
        }
    }

    public void componentResized(ComponentEvent ev) {
        revalidate();
    }
    
    public void componentMoved(ComponentEvent e) {
       
    }

    public void componentShown(ComponentEvent e) {
        
    }

    public void componentHidden(ComponentEvent e) {
        
    }
}
