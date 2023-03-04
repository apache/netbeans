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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;

/**
 *  Component for displaying folded part of code in tooltip
 *
 *  @author  Martin Roskanin
 *  @deprecated This is an utility class which should not have been public. It is used
 *  only from code folding implementation and was not intended for general purpose.
 *  This implementation is retained for backward compatibility only. The live implementation
 *  can be found in <code>editor.fold.nbui</code> module.
 */
@Deprecated
public class FoldingToolTip extends JPanel {

    View view;
    EditorUI editorUI;
    public static final int BORDER_WIDTH = 2;
    
    /** Creates a new instance of FoldingToolTip */
    public FoldingToolTip(View view, EditorUI editorUI) {
        this.view = view;
        this.editorUI = editorUI;

        FontColorSettings fcs = MimeLookup.getLookup(
            org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(
            editorUI.getComponent())).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        Color foreColor = (Color) attribs.getAttribute(StyleConstants.Foreground);
        if (foreColor == null) {
            foreColor = Color.black;
        }
        
        setBorder(new LineBorder(foreColor));
        setOpaque(true);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension prefSize = editorUI.getComponent().getPreferredSize();
        int viewHeight = (int) view.getPreferredSpan(View.Y_AXIS);
        prefSize.height = viewHeight + (BORDER_WIDTH << 1);
        prefSize.width += editorUI.getSideBarWidth() + (BORDER_WIDTH << 1);
        return prefSize;
    }

    public @Override void setSize(Dimension d){
        setSize(d.width, d.height);
    }

   /** Setting size of popup panel. The height and size is computed to fit the best place on the screen */
    public @Override void setSize(int width, int height){
        int viewHeight = (int) view.getPreferredSpan(View.Y_AXIS);
        int viewWidth = (int) view.getPreferredSpan(View.X_AXIS);
        if (height<30) {
            putClientProperty(PopupManager.Placement.class, null);
        }else{
            height = Math.min(height, viewHeight);
        }
        
        height += 2*BORDER_WIDTH;
        
        width = Math.min(width, viewWidth);
        
        super.setSize(width,height);
    }
    
    private void updateRenderingHints(Graphics g){
        JTextComponent comp = editorUI.getComponent();
        if (comp != null) {
            String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(comp);
            FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
            Map renderingHints = (Map) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);

            // Possibly apply the rendering hints
            if (renderingHints != null) {
                ((java.awt.Graphics2D)g).addRenderingHints(renderingHints);
            }
        }
    }
    
    
    protected @Override void paintComponent(Graphics g) {

        updateRenderingHints(g);
        
        Rectangle shape = new Rectangle(getSize());
        Rectangle clip = g.getClipBounds();
        
        FontColorSettings fcs = MimeLookup.getLookup(
            org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(
            editorUI.getComponent())).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        Color backColor = (Color) attribs.getAttribute(StyleConstants.Background);
        if (backColor == null) {
            backColor = Color.white;
        }
        
        g.setColor(backColor);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        g.translate(BORDER_WIDTH, BORDER_WIDTH);

        JTextComponent component = editorUI.getComponent();
        if (component == null) return;
        int sideBarWidth = editorUI.getSideBarWidth();

        /*
        GlyphGutter gg = editorUI.getGlyphGutter();
        if (gg!=null){
            View docView = null;
            if (view.getViewCount() == 1){//lockview
                docView = view.getView(0);
            }
            int y = 0;
            if (docView!=null){
                AbstractDocument doc = (AbstractDocument)docView.getDocument();
                doc.readLock();
                try {
                    LockView lockView = LockView.get(docView);
                    if (lockView != null) {
                        lockView.lock();
                        try {
                            for (int i = 0; i<docView.getViewCount(); i++ ){
                                gg.paintGutterForView(g, docView.getView(i), y);
                                y += editorUI.getLineHeight();
                            }
                        } finally {
                            lockView.unlock();
                        }
                    }
                } finally {
                    doc.readUnlock();
                }
            }else{
                gg.paintGutterForView(g, view, 0);
            }

            g.translate(sideBarWidth,0);
        }

        view.paint(g, shape);

        if (gg!=null){
            g.translate(-sideBarWidth,0);
        }
        */

        g.translate(-BORDER_WIDTH, -BORDER_WIDTH);

        g.setColor(backColor);
        for (int i = 1; i<=BORDER_WIDTH; i++){
            g.drawRect(clip.x+i,clip.y+i,clip.width-i*2-1,clip.height-i*2-1);
        }
    }
}
