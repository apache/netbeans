/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.versioning.history;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport;

/**
 * Composite panel for displaying revision, author, commit message and other
 * info.
 */
public class RevisionItemCell extends JPanel implements VCSHyperlinkSupport.BoundsTranslator {
    private final JTextPane authorControl = new JTextPane();
    private final JTextPane dateControl = new JTextPane();
    private final JTextPane revisionControl = new JTextPane();
    private final JTextPane commitMessageControl = new JTextPane();
    private final JPanel northPanel = new JPanel();
    private final JPanel authorDatePanel = new JPanel();

    public RevisionItemCell () {
        this.setBorder(null);
        this.setLayout(new BorderLayout(0, 0));
        this.add(commitMessageControl, java.awt.BorderLayout.CENTER);
        this.add(northPanel, java.awt.BorderLayout.NORTH);
        northPanel.setBorder(BorderFactory.createEmptyBorder());
        northPanel.setLayout(new BorderLayout(5, 0));
        northPanel.add(authorDatePanel, java.awt.BorderLayout.EAST);
        northPanel.add(revisionControl, java.awt.BorderLayout.CENTER);
        authorDatePanel.setLayout(new BorderLayout(5, 0));
        authorDatePanel.setBorder(BorderFactory.createEmptyBorder());
        authorDatePanel.add(authorControl, BorderLayout.CENTER);
        authorDatePanel.add(dateControl, BorderLayout.EAST);

        //
        initTextPane(dateControl);
        initTextPane(authorControl);
        initTextPane(revisionControl);
        initTextPane(commitMessageControl);
        northPanel.setOpaque(false);
        authorDatePanel.setOpaque(false);
        this.setFocusable(false);
        northPanel.setFocusable(false);
        authorDatePanel.setFocusable(false);
    }
    /**
     * Corrects the bounding rectangle of nested textpanes.
     */
    @Override
    public void correctTranslation (final Container startComponent, final Rectangle r) {
        if (null == startComponent) {
            return;
        }
        if (null == r) {
            return;
        }
        
        final RevisionItemCell stopComponent = this;
        Container current=startComponent;
        while (current != stopComponent) {
            r.translate(current.getX(), current.getY());
            current = current.getParent();
        }
        r.translate(current.getX(), current.getY());
    }

    public JTextPane getAuthorControl () {
        return authorControl;
    }

    public JTextPane getDateControl () {
        return dateControl;
    }

    public JTextPane getRevisionControl () {
        return revisionControl;
    }

    public JTextPane getCommitMessageControl () {
        return commitMessageControl;
    }

    public JPanel getNorthPanel () {
        return northPanel;
    }

    private void initTextPane (JTextPane pane) {
        pane.setBorder(null);
        pane.setLayout(null);
        //fix for nimbus laf
        pane.setOpaque(false);
        pane.setBackground(new Color(0, 0, 0, 0));
    }
    
}
