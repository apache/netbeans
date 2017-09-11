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
package org.netbeans.modules.subversion.ui.blame;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.text.DateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.AuthorLinker;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.IssueLinker;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.StyledDocumentHyperlink;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.KenaiUser;

/**
 * Window displaying the line annotation with links to bugtracking in the commit message.
 * @author Ondrej Vrabec
 */
class TooltipWindow implements AWTEventListener, MouseMotionListener, MouseListener, WindowFocusListener {

    private static final int SCREEN_BORDER = 20;
    private static final Color LINK_COLOR = UIManager.getColor("nb.html.link.foreground"); //NOI18N

    /**
     * Parent caller
     */
    private final AnnotationBar master;
    private JTextPane textPane;
    private final AnnotateLine annotateLine;
    /**
     * Start of the commit message inside the full displayed message
     */
    private int messageOffset;

    private VCSHyperlinkSupport linkerSupport = new VCSHyperlinkSupport();

    /**
     * Currently showing popup
     */
    private JWindow contentWindow;
    private TooltipContentPanel cp;

    public TooltipWindow(AnnotationBar master, final AnnotateLine al) {
        this.annotateLine = al;
        this.master = master;
    }

    public void show(Point location) {
        Rectangle screenBounds = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        for (GraphicsDevice device : gds) {
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            screenBounds = gc.getBounds();
            if (screenBounds.contains(location)) {
                break;
            }
        }

        // showing the popup tooltip
        cp = new TooltipContentPanel(master.getTextComponent());
        Window w = SwingUtilities.windowForComponent(master.getTextComponent());
        contentWindow = new JWindow(w);
        contentWindow.add(cp);
        contentWindow.pack();
        Dimension dim = contentWindow.getSize();

        if (location.y + dim.height + SCREEN_BORDER > screenBounds.y + screenBounds.height) {
            dim.height = (screenBounds.y + screenBounds.height) - (location.y + SCREEN_BORDER);
        }
        if (location.x + dim.width + SCREEN_BORDER > screenBounds.x + screenBounds.width) {
            dim.width = (screenBounds.x + screenBounds.width) - (location.x + SCREEN_BORDER);
        }

        contentWindow.setSize(dim);

        contentWindow.setLocation(location.x, location.y - 1);  // slight visual adjustment
        contentWindow.setVisible(true);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
        w.addWindowFocusListener(this);
        contentWindow.addWindowFocusListener(this);
    }

    public void eventDispatched(AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED || event.getID() == KeyEvent.KEY_PRESSED) {
            onClick(event);
        }
    }

    private void onClick(AWTEvent event) {
        Component component = (Component) event.getSource();
        if (outsideOfTooltipWindow(component)) {
            // hide the tooltip if event occurs outside of the tooltip
            shutdown();
        }
    }

    /**
     *
     * @param component
     * @return <code>true</code> if the <code>component</code> is not part of the tooltip window descendants, <code>false</code> otherwise
     */
    private boolean outsideOfTooltipWindow (Component component) {
        boolean retval = true;
        while (component != null) {
            if (component instanceof TooltipContentPanel) {
                retval = false;
                break;
            }
            component = component.getParent();
        }
        return retval;
    }

    /**
     * Closes the window
     */
    void shutdown() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (contentWindow != null) {
            contentWindow.getOwner().removeWindowFocusListener(this);
            contentWindow.removeWindowFocusListener(this);
            contentWindow.dispose();
        }
        contentWindow = null;
    }

    public void mouseDragged(MouseEvent e) {
        // not interested
    }

    public void mouseMoved(MouseEvent e) {
        if (e.getSource().equals(textPane)) {
            textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            linkerSupport.computeBounds(textPane, 0);
            linkerSupport.mouseMoved(e.getPoint(), textPane, messageOffset);
        }
        textPane.setToolTipText("");  // NOI18N
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource().equals(textPane)) {
            linkerSupport.computeBounds(textPane, 0);
            if(linkerSupport.mouseClicked(e.getPoint(), 0)) {
                shutdown(); // close this window
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        // not interested
    }

    public void mouseReleased(MouseEvent e) {
        // not interested
    }

    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    public void mouseExited(MouseEvent e) {
        // not interested
    }

    public void windowGainedFocus(WindowEvent e) {
        //
    }

    public void windowLostFocus(WindowEvent e) {
        if (contentWindow != null && e.getOppositeWindow() == null) {
            shutdown();
        }
    }

    private class TooltipContentPanel extends JComponent {

        public TooltipContentPanel(JTextComponent parentPane) {
            try {
                textPane = new JTextPane();
                StyledDocument doc = (StyledDocument) textPane.getDocument();
                
                Style normalStyle = textPane.getStyle("normal");
                Style hyperlinkStyle = textPane.addStyle("hyperlink", normalStyle);
                StyleConstants.setForeground(hyperlinkStyle, LINK_COLOR == null ? Color.BLUE : LINK_COLOR);
                StyleConstants.setUnderline(hyperlinkStyle, true);
                
                Style authorStyle = textPane.addStyle("author", normalStyle); //NOI18N
                StyleConstants.setForeground(authorStyle, LINK_COLOR == null ? Color.BLUE : LINK_COLOR);
                
                // revision
                doc.insertString(doc.getLength(), annotateLine.getRevision() + " - ", normalStyle);
                
                // author
                {
                    String author = annotateLine.getAuthor();
                    StyledDocumentHyperlink l = linkerSupport.getLinker(AuthorLinker.class, 0);
                    if (master.isKenai()) {
                        KenaiUser kenaiUser = master.getKenaiUser(author);
                        if (kenaiUser != null) {
                            l = new AuthorLinker(
                                    kenaiUser,
                                    authorStyle,
                                    doc,
                                    author,
                                    KenaiUser.getChatLink(
                                    master.getCurrentFileObject(),
                                    annotateLine.getLineNum()));
                            linkerSupport.add(l, 0);
                        }
                    }
                    if (l != null) {
                        l.insertString(doc, authorStyle);
                    } else {
                        doc.insertString(doc.getLength(), author, normalStyle);
                    }
                }

                // date
                doc.insertString(doc.getLength(), " ", normalStyle);
                doc.insertString(doc.getLength(), DateFormat.getDateInstance().format(annotateLine.getDate()), normalStyle);
                doc.insertString(doc.getLength(), "\n", normalStyle);
                
                // commit msg
                {
                    StyledDocumentHyperlink l = null;
                    String commitMessage = annotateLine.getCommitMessage();
                    List<VCSHyperlinkProvider> providers = Subversion.getInstance().getHyperlinkProviders();
                    for (VCSHyperlinkProvider hp : providers) {
                        l = IssueLinker.create(hp, hyperlinkStyle, master.getFile(), doc, commitMessage);
                        if (l != null) {
                            linkerSupport.add(l, 0);
                            break;
                        }
                    }
                    if (l != null) {
                        l.insertString(doc, normalStyle);
                    } else {
                        doc.insertString(doc.getLength(), commitMessage, normalStyle);
                    }
                }
                
                textPane.setDocument(doc);
                textPane.setEditable(false);
                Color color = UIManager.getColor( "nb.versioning.tooltip.background.color"); //NOI18N
                if( null == color )
                    color = new Color(233, 241, 255);
                textPane.setBackground(color);
                Element rootElement = org.openide.text.NbDocument.findLineRootElement(doc);
                int lineCount = rootElement.getElementCount();
                int height = textPane.getFontMetrics(textPane.getFont()).getHeight() * (lineCount + 1);
                int maxWidth = 0;
                for (int line = 0; line < lineCount; line++) {
                    Element lineElement = rootElement.getElement(line);
                    String text = null;
                    try {
                        text = doc.getText(lineElement.getStartOffset(), lineElement.getEndOffset() - lineElement.getStartOffset());
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    int lineLength = textPane.getFontMetrics(textPane.getFont()).stringWidth(text);
                    if (lineLength > maxWidth) {
                        maxWidth = lineLength;
                    }
                }
                if (maxWidth < 50) {
                    maxWidth = 50;
                }
                textPane.setPreferredSize(new Dimension(maxWidth * 7 / 6, height));
                if (!textPane.isEditable()) {
                    textPane.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$");
                }
                textPane.addMouseListener(TooltipWindow.this);
                textPane.addMouseMotionListener(TooltipWindow.this);
                JScrollPane jsp = new JScrollPane(textPane);
                jsp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
                setLayout(new BorderLayout());
                add(jsp);
            } catch (BadLocationException ex) {

            }
        }
    }
}
