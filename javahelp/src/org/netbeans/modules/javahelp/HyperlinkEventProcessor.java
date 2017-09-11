/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javahelp;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.javahelp.CopyLinkLocationAction.LinkOwner;

/**
 * Processor for the both <code>MouseEvent</code>s and
 * <code>HyperlinkEvent</code>s that may occur against hyperlinks
 * displayed in the <code>JEditorPane</code>.
 *
 * The <code>HyperlinkEventProcessor</code> is responsible to:
 * <ul>
 *     <li>show a <code>url</code> of the hyperlink as a tooltip;</li>
 *     <li>show a context menu of the hyperlink</li>
 *     <li>copy text of the <code>url</code> to the system clipboard</li>
 * </ul>
 *
 * <p>Usage:<br/>
 * <code>HyperlinkEventProcessor.addTo(pane);</code>
 * </p>
 *
 * @author Victor G. Vasilyev
 */
public class HyperlinkEventProcessor extends MouseAdapter
                      implements HyperlinkListener, LinkOwner {

    private boolean isInsideHyperlink = false;
    private URL url;
    private JEditorPane pane;
    private JPopupMenu popupMenu;

    // Use addTo(JEditorPane pane) instead.
    private HyperlinkEventProcessor(JEditorPane pane) {
        this.pane = pane;
        this.popupMenu = getPopupMenu(new CopyLinkLocationAction(this));
    }

    static JPopupMenu getPopupMenu(CopyLinkLocationAction cllAction) {
        JMenuItem copyItem = new JMenuItem(cllAction);
        JPopupMenu menu = new JPopupMenu();
        menu.add(copyItem);
        return menu;
    }

    /**
     * Adds the <code>HyperlinkEventProcessor</code> to the specified
     * <code>JEditorPane</code>.
     * @param pane an instance of the <code>JEditorPane</code>.
     */
    public static void addTo(JEditorPane pane) {
        assert pane != null;
        HyperlinkEventProcessor proc = new HyperlinkEventProcessor(pane);
        pane.addHyperlinkListener(proc);
        pane.addMouseListener(proc);
    }

    public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
        url = hyperlinkEvent.getURL();
        HyperlinkEvent.EventType type = hyperlinkEvent.getEventType();
        if (type == HyperlinkEvent.EventType.ENTERED) {
          isInsideHyperlink = true;
          pane.setToolTipText(getURLExternalForm()); // #176141
        }
        else if (type == HyperlinkEvent.EventType.ACTIVATED) {
          isInsideHyperlink = false;
          pane.setToolTipText(null);
        }
        else if (type == HyperlinkEvent.EventType.EXITED) {
          isInsideHyperlink = false;
          pane.setToolTipText(null);
        }
        else {
          Installer.log.log(Level.SEVERE, "Unknown hyperlinkEvent: " +
                                           hyperlinkEvent);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
       if (isInsideHyperlink && Utils.isMouseRightClick(e)) {
           Utils.showPopupMenu(e, popupMenu, pane);
       }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // do nothing
    }

    public String getURLExternalForm() {
        return url == null ? null : url.toExternalForm(); // #176141
    }

    public Clipboard getClipboard() {
        return pane.getToolkit().getSystemClipboard();
    }

}

