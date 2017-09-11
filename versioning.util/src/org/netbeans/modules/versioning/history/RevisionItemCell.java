/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
    private JTextPane authorControl=new JTextPane();
    private JTextPane dateControl=new JTextPane();
    private JTextPane revisionControl=new JTextPane();
    private JTextPane commitMessageControl=new JTextPane();
    private JPanel northPanel=new JPanel();
    private JPanel authorDatePanel=new JPanel();

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
     * @param startComponent
     * @param r 
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
