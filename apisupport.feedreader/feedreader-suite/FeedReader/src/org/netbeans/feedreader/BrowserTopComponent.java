/*
 * Copyright (c) 2010, Oracle. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.netbeans.feedreader;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * A top component which can display a feed entry.
 */
public final class BrowserTopComponent extends TopComponent {

    /** The cache of opened browser components. */
    private static Map<String,BrowserTopComponent> browserComponents = new HashMap<String,BrowserTopComponent>();

    private final JScrollPane scrollPane;
    private final JEditorPane editorPane;
    private final SyndEntry entry;
    
    private BrowserTopComponent(SyndEntry entry) {
        this.entry = entry;
        setName(entry.getTitle());
        setToolTipText(NbBundle.getMessage(BrowserTopComponent.class, "HINT_BrowserTopComponent"));
        
        scrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        
        editorPane.setEditable(false);
        SyndContent description = entry.getDescription();
        if (description != null) {
            /* Not trustworthy, it seems:
            String type = description.getType();
            if (type == null) {
                editorPane.setContentType(type);
            }
             */
            editorPane.setContentType("text/html");
            editorPane.setText(description.getValue());
        }
        
        setLayout(new BorderLayout());
        scrollPane.setViewportView(editorPane);
        add(scrollPane, BorderLayout.CENTER);
        putClientProperty(/*PrintManager.PRINT_PRINTABLE*/"print.printable", true);

        JButton browse = new JButton(NbBundle.getMessage(BrowserTopComponent.class, "CTL_view_in_browser"));
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURLExternal(new URL(BrowserTopComponent.this.entry.getLink()));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        add(browse, BorderLayout.PAGE_END);
    }
    
    
    public static BrowserTopComponent getBrowserComponent(SyndEntry entry) {
        BrowserTopComponent win = browserComponents.get(entry.getUri());
        if (win == null) {
            win = new BrowserTopComponent(entry);
            browserComponents.put(entry.getUri(), win);
        }
        return win;
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    public synchronized void componentClosed() {
        browserComponents.remove(entry.getUri());
    }
    
}
