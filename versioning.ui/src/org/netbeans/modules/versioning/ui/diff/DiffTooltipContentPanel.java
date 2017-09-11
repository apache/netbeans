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
package org.netbeans.modules.versioning.ui.diff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import org.netbeans.api.diff.Difference;
import org.netbeans.editor.EditorUI;
import org.openide.text.CloneableEditorSupport;

/**
 * @author Maros Sandor
 */
class DiffTooltipContentPanel extends JComponent {
    private JEditorPane originalTextPane;
    private final Color color;
    private int maxWidth;
    private final JScrollPane jsp;

    public DiffTooltipContentPanel(final JTextComponent parentPane, final String mimeType, final Difference diff) {
        
        originalTextPane = new JEditorPane();

        EditorKit kit = CloneableEditorSupport.getEditorKit(mimeType);
        originalTextPane.setEditorKit(kit);

        Document xdoc = kit.createDefaultDocument();
        if (!(xdoc instanceof StyledDocument)) {
            xdoc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            originalTextPane.setEditorKit(kit);
        }

        StyledDocument doc = (StyledDocument) xdoc;
        try {
            kit.read(new StringReader(diff.getFirstText()), doc, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        originalTextPane.setDocument(doc);
        originalTextPane.setEditable(false);
        color = getBackgroundColor(diff.getType());
        originalTextPane.setBackground(color);
        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(originalTextPane);
        Element rootElement = org.openide.text.NbDocument.findLineRootElement(doc);
        int lineCount = rootElement.getElementCount();
        int height = eui.getLineHeight() * lineCount;
        maxWidth = 0;
        for(int line = 0; line < lineCount; line++) {
            Element lineElement = rootElement.getElement(line);
            String text = null;
            try {
                text = doc.getText(lineElement.getStartOffset(), lineElement.getEndOffset() - lineElement.getStartOffset());
            } catch (BadLocationException e) {
                //
            }
            text = replaceTabs(mimeType, text);
            int lineLength = parentPane.getFontMetrics(parentPane.getFont()).stringWidth(text);
            if (lineLength > maxWidth) {
                maxWidth = lineLength;
            }
        }
        if (maxWidth < 50) maxWidth = 50;   // too thin component causes repaint problems
        else if (maxWidth < 150) maxWidth += 10;
        maxWidth = Math.min(maxWidth * 7 / 6, parentPane.getVisibleRect().width);
        originalTextPane.setPreferredSize(new Dimension(maxWidth, height));

        if (!originalTextPane.isEditable()) {
            originalTextPane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        }

        jsp = new JScrollPane(originalTextPane);
        jsp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        setLayout(new BorderLayout());
        add(jsp);
    }

    void resize () {
        if (originalTextPane == null) return;
        originalTextPane.setBackground(color);
        Element rootElement = org.openide.text.NbDocument.findLineRootElement((StyledDocument) originalTextPane.getDocument());
        int lineCount = rootElement.getElementCount();

        int height = 0;
        assert lineCount > 0;
        Element lineElement = rootElement.getElement(lineCount - 1);
        Rectangle rec;
        try {
            rec = originalTextPane.modelToView(lineElement.getEndOffset() - 1);
            height = rec.y + rec.height;
        } catch (BadLocationException ex) {
        }
        if (height > 0) {
            Dimension size = originalTextPane.getPreferredSize();
            Dimension scrollpaneSize = jsp.getPreferredSize();
            height += jsp.getHorizontalScrollBar().getPreferredSize().height;

            jsp.setPreferredSize(new Dimension(maxWidth + Math.max(0, scrollpaneSize.width - size.width), height + Math.max(0, scrollpaneSize.height - size.height)));
            SwingUtilities.getWindowAncestor(originalTextPane).pack();
        }
        originalTextPane = null;
    }

    private Color getBackgroundColor (int key) {
        org.netbeans.modules.diff.DiffModuleConfig config = org.netbeans.modules.diff.DiffModuleConfig.getDefault();
        return key == Difference.DELETE ? config.getSidebarDeletedColor() : config.getSidebarChangedColor();
    }

    private Integer spacesPerTab;
    private String replaceTabs(String mimeType, String text) {
        if (text.contains("\t")) {                                      //NOI18N
            if (spacesPerTab == null) {
                spacesPerTab = org.netbeans.modules.diff.DiffModuleConfig.getDefault().getSpacesPerTabFor(mimeType);
            }
            text = text.replace("\t", strCharacters(' ', spacesPerTab)); //NOI18N
        }
        return text;
    }
    
    private static String strCharacters(char c, int num) {
        StringBuffer s = new StringBuffer();
        while(num-- > 0) {
            s.append(c);
        }
        return s.toString();
    }
}
