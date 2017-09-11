/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.commons;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author tomas
 */
public final class HyperlinkSupport {
    
    private static final HyperlinkSupport instance = new HyperlinkSupport();

    final static String STACKTRACE_ATTRIBUTE = "attribute.stacktrace.link";     // NOI18N
    final static String TYPE_ATTRIBUTE = "attribute.type.link";                 // NOI18N
    final static String URL_ATTRIBUTE = "attribute.url.link";                   // NOI18N
    public final static String LINK_ATTRIBUTE = "attribute.simple.link";        // NOI18N
    private final MotionListener motionListener;
    private final java.awt.event.MouseListener mouseListener;
    private final RequestProcessor rp = new RequestProcessor("Bugtracking hyperlinks", 50); // NOI18N
    
    private HyperlinkSupport() { 
        motionListener = new MotionListener();
        mouseListener = new MouseListener();
    }
    
    public static HyperlinkSupport getInstance() {
        return instance;
    }
    
    public interface IssueRefProvider {
        public int[] getIssueRefSpans(CharSequence text);
    }

    public interface Link {
        public void onClick(String linkText);
    }

    public interface IssueLinker extends Link, IssueRefProvider { }
        
    public void register(Component c) {
        registerChildren(c, null);
    }

    private static final String REGISTER_TASK = "hyperlink.task";
    public void register(final TopComponent tc, final IssueLinker issueLinker) {
        tc.removeContainerListener(regListener);
        tc.addContainerListener(regListener);        
        RequestProcessor.Task task = rp.create(new Runnable() {
            @Override
            public void run() {
                registerChildren(tc, issueLinker);
            }
        });
        tc.putClientProperty(REGISTER_TASK, task);
        task.schedule(1000);
    }
    
    private void registerChildren(Component c, IssueLinker issueLinker) {
        if(c instanceof Container) {
            Container container = (Container) c;
            container.removeContainerListener(regListener);
            container.addContainerListener(regListener);
            
            Component[] components = container.getComponents();
            for (Component cmp : components) {
                registerChildren(cmp, issueLinker);
            }
        } if(c instanceof JTextPane) {
            JTextPane tp = (JTextPane) c;
            if(!tp.isEditable()) {
                registerTask.add(tp, issueLinker);
            }
        }
    }
        
    private void registerForStacktraces(final JTextPane pane) {
        pane.removeMouseMotionListener(motionListener);
        StackTraceSupport.register(pane);
        pane.removeMouseMotionListener(motionListener);
        pane.addMouseMotionListener(motionListener);
    }
    
    private void registerForTypes(final JTextPane pane) {
        pane.removeMouseMotionListener(motionListener);
        FindTypesSupport.getInstance().register(pane);
        pane.removeMouseMotionListener(motionListener);
        pane.addMouseMotionListener(motionListener);
    }
    
    private void registerForURLs(final JTextPane pane) {
        pane.removeMouseMotionListener(motionListener);
        WebUrlHyperlinkSupport.register(pane);
        pane.removeMouseMotionListener(motionListener);
        pane.addMouseMotionListener(motionListener);
    }
    
    public void registerLink(final JTextPane pane, final int pos[], final Link link) {
        pane.removeMouseMotionListener(motionListener);
        rp.post(new Runnable() {
            @Override
            public void run() {
                registerLinkIntern(pane, pos, link);
                pane.removeMouseMotionListener(motionListener);
                pane.addMouseMotionListener(motionListener);
            }
        });    
    }
    
    private void registerForIssueLinks(final JTextPane pane, final Link issueLink, final IssueRefProvider issueIdProvider) {
        pane.removeMouseMotionListener(motionListener);
        try {
            String text = "";
            try {
                text = pane.getStyledDocument().getText(0, pane.getStyledDocument().getLength());
            } catch (BadLocationException ex) {
                Support.LOG.log(Level.INFO, null, ex);
            }
            registerLinkIntern(pane, issueIdProvider.getIssueRefSpans(text), issueLink);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        pane.addMouseMotionListener(motionListener);
    }

    private void registerLinkIntern(final JTextPane pane, final int[] pos, final Link link) {
        final StyledDocument doc = pane.getStyledDocument();
                
        if (pos.length > 0) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                    final Style hlStyle = doc.addStyle("regularBlue-link", defStyle); // NOI18N
                    hlStyle.addAttribute(LINK_ATTRIBUTE, link);
                    StyleConstants.setForeground(hlStyle, UIUtils.getLinkColor());
                    StyleConstants.setUnderline(hlStyle, true);

                    for (int i=0; i<pos.length; i+=2) {
                        int off = pos[i];
                        int length = pos[i+1]-pos[i];
                        doc.setCharacterAttributes(off, length, hlStyle, true);
                    }
                    pane.removeMouseListener(mouseListener);
                    pane.addMouseListener(mouseListener);
                }
            });
        }
    }
    
    private class MouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JTextPane pane = (JTextPane)e.getSource();
                    StyledDocument doc = pane.getStyledDocument();
                    Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                    AttributeSet as = elem.getAttributes();
                    Link link = (Link)as.getAttribute(LINK_ATTRIBUTE);
                    if (link != null) {
                        link.onClick(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()));
                    }
                }
            } catch(Exception ex) {
                Support.LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class MotionListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            JTextPane pane = (JTextPane)e.getSource();
            StyledDocument doc = pane.getStyledDocument();
            Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
            AttributeSet as = elem.getAttributes();
            if (StyleConstants.isUnderline(as)) {
                pane.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                pane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    };
    
    private final ContainerListener regListener = new ContainerListener() {
        @Override
        public void componentAdded(ContainerEvent e) {
            Component c = e.getChild();
            while(((c = c.getParent()) != null)) {
                if(c instanceof TopComponent) {
                    RequestProcessor.Task t = (RequestProcessor.Task) ((TopComponent)c).getClientProperty(REGISTER_TASK);
                    if(t != null) {
                        t.schedule(1000);
                    } 
                    break;
                }
            }
        }
        @Override public void componentRemoved(ContainerEvent e) { }
    };    
    
    private final RegisterTask registerTask = new RegisterTask();
    private class RegisterTask implements Runnable {
        private final ConcurrentLinkedQueue<R> toregister = new ConcurrentLinkedQueue<R>();
        private final RequestProcessor.Task task;

        private RegisterTask() {
            task = rp.create(this);
        }
        
        void add(JTextPane tp, IssueLinker issueLinker) {
            toregister.add(new R(tp, issueLinker));
            task.schedule(300);
        }
        
        @Override
        public void run() {
            List<R> rs = new LinkedList<R>();
            R r;
            while((r = toregister.poll()) != null) {
                rs.add(r);
            }
            for (R reg : rs) {
                registerForStacktraces(reg.tp);
                registerForTypes(reg.tp);
                registerForURLs(reg.tp);
                if(reg.issueLinker != null) {
                    registerForIssueLinks(reg.tp, reg.issueLinker, reg.issueLinker);
                }
            }            
        }
        
        private class R {
            private final JTextPane tp;
            private final IssueLinker issueLinker;
            public R(JTextPane tp, IssueLinker issueLinker) {
                this.tp = tp;
                this.issueLinker = issueLinker;
            }
        }
    }
}
