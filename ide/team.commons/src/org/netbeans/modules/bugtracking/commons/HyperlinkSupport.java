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

    static final String STACKTRACE_ATTRIBUTE = "attribute.stacktrace.link";     // NOI18N
    static final String TYPE_ATTRIBUTE = "attribute.type.link";                 // NOI18N
    static final String URL_ATTRIBUTE = "attribute.url.link";                   // NOI18N
    public static final String LINK_ATTRIBUTE = "attribute.simple.link";        // NOI18N
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
