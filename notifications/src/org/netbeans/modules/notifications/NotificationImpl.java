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
package org.netbeans.modules.notifications;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import java.util.Calendar;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.modules.notifications.center.NotificationCenterManager;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * Notification implementation.
 *
 * @author S. Aubrecht
 * @author jpeska
 */
public class NotificationImpl extends Notification implements Comparable<NotificationImpl> {

    private final String title;
    private final Icon icon;
    private final Priority priority;
    private JComponent balloonComp;
    private JComponent detailsComp;
    private String detailsText;
    private ActionListener al;
    private final Category category;
    private final Calendar dateCreated;
    private boolean read;

    public NotificationImpl(String title, Icon icon, Priority priority, Category category, Calendar dateCreated) {
        this.title = title;
        this.icon = icon;
        this.priority = priority;
        this.category = category;
        this.dateCreated = dateCreated;
        this.read = false;
    }

    @Override
    public void clear() {
        NotificationCenterManager.getInstance().delete(this);
    }

    public void markAsRead(boolean read) {
        if (read != this.read) {
            this.read = read;
            NotificationCenterManager manager = NotificationCenterManager.getInstance();
            manager.wasRead(this);
        }
    }

    @Override
    public int compareTo(NotificationImpl n) {
        int res = priority.compareTo(n.priority);
        if (0 == res) {
            //TODO ignore case??
            res = category.getDisplayName().compareTo(n.category.getDisplayName());
        }
        if (0 == res) {
            res = title.compareTo(n.title);
        }
        return res;
    }

    public JComponent getBalloonComp() {
        return balloonComp;
    }

    public Icon getIcon() {
        return icon;
    }

    public JComponent getDetailsComponent() {
        return detailsComp;
    }

    public String getTitle() {
        return title;
    }

    public Priority getPriority() {
        return priority;
    }

    public Category getCategory() {
        return category;
    }

    public Calendar getDateCreated() {
        return dateCreated;
    }

    public boolean isRead() {
        return read;
    }

    boolean showBallon() {
        //TODO where to show ballon
        return priority != Priority.SILENT;
    }

    void setDetails(String detailsText, ActionListener al) {
        this.detailsText = detailsText;
        this.al = al;
    }

    void setDetails(JComponent balloonComp, JComponent detailsComp) {
        this.balloonComp = balloonComp;
        this.detailsComp = detailsComp;
    }

    public void initDecorations() {
        if (null != detailsText) {
            balloonComp = createDetails(detailsText, al);
            detailsComp = createDetails(detailsText, al);
        }

        JComponent titleComp = createTitle(title, icon);
        JComponent balloon = createContent(titleComp, balloonComp);
        balloon.setBorder(BorderFactory.createEmptyBorder(8, 5, 0, 0));
        balloonComp = balloon;

        titleComp = createTitle(title, icon);
        detailsComp = createContent(titleComp, detailsComp, createPriority(priority), createCategory(category), createDate(dateCreated));
    }

    private JComponent createContent(JComponent titleComp, JComponent popupDetails) {
        return createContent(titleComp, popupDetails, null, null, null);
    }

    private JComponent createContent(JComponent titleComp, JComponent detailsComp, JComponent priorityComp, JComponent categoryComp, JComponent dateComp) {
        //TODO bold field, add label to timestamp - Created:
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.add(titleComp, new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 3, 3), 0, 0));
        if (priorityComp != null) {
            panel.add(priorityComp, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 3, 3), 0, 0));
        }
        if (categoryComp != null) {
            panel.add(categoryComp, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 3, 3), 0, 0));
        }

        panel.add(detailsComp, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10, 10, 3, 3), 0, 0));

        panel.add(new JLabel(), new GridBagConstraints(2, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        if (dateComp != null) {
            panel.add(dateComp, new GridBagConstraints(2, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0));
        }

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAsRead(true);
            }
        };
        addActionListener(detailsComp, actionListener);
        return panel;
    }

    private void addActionListener(Container c, ActionListener al) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).addActionListener(al);
        }
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                addActionListener((Container) child, al);
            }
        }
    }

    private JComponent createTitle(String title, Icon icon) {
        JLabel titleComp = new JLabel("<html>" + title); // NOI18N
        if (icon != null) {
            titleComp.setIcon(icon);
        }
        titleComp.setFont(titleComp.getFont().deriveFont(Font.BOLD));
        return titleComp;
    }

    private JComponent createDetails(String text, ActionListener action) {
        try {
            text = (action == null ? "<html>" : "<html><a href=\"_blank\">") + XMLUtil.toElementContent(text); //NOI18N
        } catch (CharConversionException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (null == action) {
            return new JLabel(text);
        }
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.addActionListener(action);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color c = UIManager.getColor("nb.html.link.foreground"); //NOI18N
        if (c != null) {
            btn.setForeground(c);
        }
        return btn;
    }

    private JComponent createPriority(Priority p) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.add(new JLabel(NbBundle.getMessage(NotificationImpl.class, "LBL_Priority")), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        panel.add(new JLabel(p.getIcon()), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel(p.name()), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
        panel.add(new JLabel(), new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        return panel;
    }

    private JComponent createCategory(Category c) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.add(new JLabel(NbBundle.getMessage(NotificationImpl.class, "LBL_Category")), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        panel.add(new JLabel(c.getDisplayName()), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel(), new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        return panel;
    }

    private JComponent createDate(Calendar dateCreated) {
        return new JLabel(Utils.getFullFormatedDate(dateCreated));
    }
}
