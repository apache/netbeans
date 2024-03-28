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

package org.netbeans.modules.bugzilla.issue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.bugtracking.commons.IssueSettingsStorage;
import org.netbeans.modules.bugtracking.commons.HyperlinkSupport;
import org.netbeans.modules.bugtracking.commons.LinkButton;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class CommentsPanel extends JPanel {
    static final RequestProcessor RP = new RequestProcessor("Bugzilla Comments Panel", 5, false); // NOI18N
    private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // NOI18N
    private static final String REPLY_TO_PROPERTY = "replyTo"; // NOI18N
    private static final String QUOTE_PREFIX = "> "; // NOI18N
    private static final int MAX_COMMENT_HEIGHT = 10000;
    
    private static Color blueBackground = null;
    private static Color greyForeground = null;
    
    private final JPopupMenu commentsPopup = new PopupMenu();
    private BugzillaIssue issue;
    private List<BugzillaIssue.Attachment> attachments;
    private List<String> attachmentIds;
    private NewCommentHandler newCommentHandler;

    private Set<Long> collapsedComments = Collections.synchronizedSet(new HashSet<Long>());

    static {
        blueBackground = UIManager.getColor( "nb.bugtracking.comment.background" ); //NOI18N
        if( null == blueBackground )
            blueBackground = new Color(0xf3f6fd);
        greyForeground = UIManager.getColor( "nb.bugtracking.comment.foreground" ); //NOI18N
        if( null == greyForeground )
            greyForeground = new Color(0x999999);
    }
    private ArrayList<ExpandLabel> sections;

    public CommentsPanel() {
        setOpaque( false );
    }

    void setIssue(BugzillaIssue issue,
                  List<BugzillaIssue.Attachment> attachments) {
        removeAll();
        this.issue = issue;
        initCollapsedComments();
        this.attachments = attachments;
        this.attachmentIds = getAttachmentIds(attachments);
        BugzillaIssue.Comment[] comments = issue.getComments();
        this.sections = new ArrayList<>(comments.length + 1);
        GroupLayout layout = new GroupLayout(this);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(horizontalGroup)
            .addContainerGap());
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(verticalGroup));
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
        String creationTxt = issue.getFieldValue(IssueField.CREATION);
        try {
            if (!creationTxt.isEmpty()) {
                Date creation = dateTimeFormat.parse(creationTxt);
                creationTxt = format.format(creation);
            }
        } catch (ParseException pex) {
            Bugzilla.LOG.log(Level.INFO, null, pex);
        }
        sections.add(addSection(layout,
            new Long(0),    
            issue.getFieldValue(IssueField.DESCRIPTION),
            issue.getFieldValue(IssueField.REPORTER),
            issue.getFieldValue(IssueField.REPORTER_NAME),
            creationTxt, horizontalGroup, verticalGroup, true));
        for (BugzillaIssue.Comment comment : comments) {
            String when = format.format(comment.getWhen());
            sections.add(addSection(layout, comment.getNumber(), comment.getText(), comment.getAuthor(), comment.getAuthorName(), when, horizontalGroup, verticalGroup, false));
        }
        verticalGroup.addContainerGap();
        setLayout(layout);
    }

    private static List<String> getAttachmentIds(
                                   List<BugzillaIssue.Attachment> attachments) {
        if (attachments.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>(attachments.size());
        for (BugzillaIssue.Attachment attachment : attachments) {
            result.add(attachment.getId());
        }
        return result;
    }

    public void setNewCommentHandler(NewCommentHandler handler) {
        newCommentHandler = handler;
    }

    private ExpandLabel addSection(GroupLayout layout, final Long number, String text, final String author, String authorName, String dateTimeString,
            GroupLayout.ParallelGroup horizontalGroup, GroupLayout.SequentialGroup verticalGroup, boolean description) {
        
        JTextPane textPane = new JTextPane();
        setupTextPane(textPane, text);
        
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque( false );
        JPanel placeholder = createTextPanelPlaceholder();      
        JLabel commentLabel = new JLabel();
        JLabel rightLabel = new JLabel();
        ExpandLabel iconLabel = new ExpandLabel(placeholder, textPane, headerPanel, commentLabel, number);
        JLabel leftLabel = new JLabel();
        
        headerPanel.addMouseListener(iconLabel);
        headerPanel.setComponentPopupMenu(expandPopup);
        
        // left label
        ResourceBundle bundle = NbBundle.getBundle(CommentsPanel.class);
        String leftTxt = "";
        String authorTxt = ((authorName != null) && (authorName.trim().length() > 0)) ? authorName : author;
        if (description) {
            String leftFormat = bundle.getString("CommentsPanel.leftLabel.format"); // NOI18N
            leftTxt = MessageFormat.format(leftFormat, authorTxt);
        } else {
            leftTxt = authorTxt;
        }
        leftLabel.setText(leftTxt);
        leftLabel.setLabelFor(textPane);
        leftLabel.setForeground(greyForeground);
        leftLabel.setOpaque(false);
        leftLabel.addMouseListener(iconLabel);
        leftLabel.setComponentPopupMenu(expandPopup);
        
        // comment label
        commentLabel.setOpaque(false);
        commentLabel.addMouseListener(iconLabel);
        commentLabel.setComponentPopupMenu(expandPopup);
        
        // right label
        rightLabel.setText(dateTimeString);
        rightLabel.setForeground(greyForeground);
        rightLabel.setOpaque(false);
        rightLabel.addMouseListener(iconLabel);
        rightLabel.setComponentPopupMenu(expandPopup);
        
        // state label
        JLabel stateLabel = null;
        
        // replay button
        LinkButton replyButton = new LinkButton(bundle.getString("Comments.replyButton.text")); // NOI18N
        replyButton.addActionListener(getReplyListener());
        replyButton.putClientProperty(REPLY_TO_PROPERTY, textPane);
        replyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.replyButton.AccessibleContext.accessibleDescription")); // NOI18N
        replyButton.setOpaque(false);
        
        // mailto button
        LinkButton.MailtoButton mailtoButton = null;
        if(author.indexOf("@") > -1) {
            mailtoButton = new LinkButton.MailtoButton(
                    NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.mailtoButton.text"),                                        // NOI18N
                    NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.mailtoButton.AccessibleContext.accessibleDescription"),     // NOI18N
                    author,
                    NbBundle.getMessage(CommentsPanel.class, "IssuePanel.headerLabel.format", new Object[] {issue.getID(), issue.getSummary()}), // subject NOI18N
                    getReplayText(text)); // body
            replyButton.setOpaque(false);
        }
        // IssueProvider 172653 - JTextPane too big
        JComponent pane = textPane;
        if (textPane.getPreferredSize().height>Short.MAX_VALUE) {
            pane = new JScrollPane(textPane);
            Dimension dim = new Dimension(textPane.getPreferredSize());
            dim.height = MAX_COMMENT_HEIGHT;
            pane.setPreferredSize(dim); 
        }

        // Layout
        layoutHeaderPanel(headerPanel, iconLabel, leftLabel, commentLabel, rightLabel, replyButton, mailtoButton, stateLabel);
        
        iconLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        placeholder.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        leftLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        commentLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        rightLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        
        horizontalGroup
            .addComponent(headerPanel)
            .addGroup(layout.createSequentialGroup()
                .addComponent(placeholder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pane));                
        
        if (!description) {
            verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        }
        verticalGroup
            .addComponent(headerPanel)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                .addComponent(placeholder)
                .addComponent(pane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        return iconLabel;
    }

    private void setupTextPane(final JTextPane textPane, String comment) {
        if( UIUtils.isNimbus() ) {
            textPane.setUI( new BasicTextPaneUI() );
        }
        textPane.setText(comment);
        
        Caret caret = textPane.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }

        // attachments
        if (!attachmentIds.isEmpty()) {
            AttachmentHyperlinkSupport.Attachement a = AttachmentHyperlinkSupport.findAttachment(comment, attachmentIds);
            if (a != null) {
                String attachmentId = a.id;
                if (attachmentId != null) {
                    int index = attachmentIds.indexOf(attachmentId);
                    if (index != -1) {
                        BugzillaIssue.Attachment attachment = attachments.get(index);
                        AttachmentLink attachmentLink = new AttachmentLink(attachment);
                        HyperlinkSupport.getInstance().registerLink(textPane, new int[] {a.idx1, a.idx2}, attachmentLink);
                    } else {
                        Bugzilla.LOG.log(Level.WARNING, "couldn''t find attachment id in: {0}", comment); // NOI18N
                    }
                }
            }
        }

        // pop-ups
        textPane.setComponentPopupMenu(commentsPopup);

        textPane.setBackground(blueBackground);

        textPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        textPane.setEditable(false);
        textPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleName")); // NOI18N
        textPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleDescription")); // NOI18N
    }

    private void layoutHeaderPanel(JPanel headerPanel, JLabel iconLabel, JLabel leftLabel, JLabel commentLabel, JLabel rightLabel, LinkButton replyButton, LinkButton mailtoButton, JLabel stateLabel) {
        GroupLayout layout = new GroupLayout(headerPanel);
        headerPanel.setLayout(layout);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup()
            .addComponent(iconLabel)
            .addComponent(leftLabel);
        if (stateLabel != null) {
            hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(stateLabel);
        }
        hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(commentLabel,0, 0, Short.MAX_VALUE)
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(rightLabel)
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(replyButton);
        if (mailtoButton != null) {
            hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(mailtoButton);
        }
        layout.setHorizontalGroup(hGroup);
        
        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(iconLabel)
            .addComponent(leftLabel);
        if (stateLabel != null) {
            vGroup.addComponent(stateLabel);
        }
        vGroup.addComponent(commentLabel)
              .addComponent(rightLabel)
              .addComponent(replyButton);
        if (mailtoButton != null) {
            vGroup.addComponent(mailtoButton);
        }
        layout.setVerticalGroup(vGroup);
    }

    private ActionListener replyListener;
    private ActionListener getReplyListener() {
        if (replyListener == null) {
            replyListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object source = e.getSource();
                    if (source instanceof JComponent) {
                        JComponent comp = (JComponent)source;
                        Object value = comp.getClientProperty(REPLY_TO_PROPERTY);
                        if (value instanceof JTextPane) {
                            JTextPane pane = (JTextPane)value;
                            newCommentHandler.append(getReplayText(pane.getText()));
                        }
                    }
                }

            };
        }
        return replyListener;
    }

    private String getReplayText(String text) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(text, "\n"); // NOI18N
        while (tokenizer.hasMoreElements()) {
            String line = tokenizer.nextToken();
            sb.append(QUOTE_PREFIX).append(line).append('\n'); // NOI18N
        }
        return sb.toString();
    }
    
    private JPanel createTextPanelPlaceholder() {
        JPanel placeholder = new JPanel();
        placeholder.setBackground(blueBackground);
        GroupLayout layout = new GroupLayout(placeholder);
        placeholder.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, ICON_WIDTH, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
        return placeholder;
    }

    void collapseAll () {
        for (ExpandLabel lbl : sections) {
            lbl.setState(true);
        }
    }

    void expandAll () {
        for (ExpandLabel lbl : sections) {
            lbl.setState(false);
        }
    }

    class PopupMenu extends JPopupMenu {

        /*
         * Holds the location of where the user invoked the pop-up menu.
         * It must be remembered before calling super.show(...) because
         * the method show() may change the location of the pop-up menu,
         * so the original location might not be available.
         */
        private final Point clickPoint = new Point();

        @Override
        public void show(Component invoker, int x, int y) {
            clickPoint.setLocation(x, y);
            super.show(invoker, x, y);
        }

        @Override
        public void setVisible(boolean b) {
            if (b) {
                JTextPane pane = (JTextPane) getInvoker();
                StyledDocument doc = pane.getStyledDocument();
                Element elem = doc.getCharacterElement(pane.viewToModel(clickPoint));
                Object l = elem.getAttributes().getAttribute(HyperlinkSupport.LINK_ATTRIBUTE);
                if (l instanceof AttachmentLink) {
                    BugzillaIssue.Attachment attachment = ((AttachmentLink) l).attachment;
                    if (attachment != null) {
                        add(new JMenuItem(attachment.getOpenAction()));
                        add(new JMenuItem(attachment.getSaveAction()));
                        Action openInStackAnalyzerAction = attachment.getOpenInStackAnalyzerAction();
                        if(openInStackAnalyzerAction != null) {
                            add(new JMenuItem(openInStackAnalyzerAction));
                        }
                        if (attachment.isPatch()) { 
                            Action a = attachment.getApplyPatchAction();
                            if(a != null) {
                                add(attachment.getApplyPatchAction());
                            }
                        }
                        super.setVisible(true);
                    }
                }
            } else {
                super.setVisible(false);
                removeAll();
            }
        }

    }

    public interface NewCommentHandler {
        void append(String text);
    }

    private final JPopupMenu expandPopup = new ExpandPopupMenu();
    private Set<ExpandLabel> expandLabels = new HashSet<ExpandLabel>();
    
    private class ExpandPopupMenu extends JPopupMenu {
        public ExpandPopupMenu() {
            add(new JMenuItem(new AbstractAction(NbBundle.getMessage(CommentsPanel.class, "LBL_ExpandAll")) { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (ExpandLabel l : expandLabels) {
                        l.setState(false);
                    }
                }
            }));
            add(new JMenuItem(new AbstractAction(NbBundle.getMessage(CommentsPanel.class, "LBL_CollapseAll")) { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (ExpandLabel l : expandLabels) {
                        l.setState(true);
                    }
                }
            }));
        }
    }    

    private void commentCollapsed(Long number) {
        collapsedComments.add(number);
    }

    private Set<Long> touchedCommenst = Collections.synchronizedSet(new HashSet<Long>());
    private void commentExpanded(Long number) {
        if(collapsedComments.remove(number)) {
            touchedCommenst.add(number);
        }
    }

    private boolean isCollapsed(Long number) {
        return collapsedComments.contains(number);
    }
    
    private void initCollapsedComments() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Collection<Long> s = IssueSettingsStorage.getInstance().loadCollapsedCommenst(issue.getRepository().getUrl(), issue.getID());
                for (Long l : s) {
                    if(!touchedCommenst.contains(l)) {
                        collapsedComments.add(l);
                    }
                }
            }
        });
    }
    
    void storeSettings() {
        if(issue != null) {
            IssueSettingsStorage.getInstance().storeCollapsedComments(collapsedComments, issue.getRepository().getUrl(), issue.getID());
        }
    }    
    
    private static final Icon ei;
    private static final Icon ci;
    private static final int ICON_WIDTH;
    static {
        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        ei = tvui.getExpandedIcon();
        ci = tvui.getCollapsedIcon();
        ICON_WIDTH = ei != null ? ei.getIconWidth() : 16;
    }
    private class ExpandLabel extends JLabel implements MouseListener {
        private final JTextPane textPane;
        private final JPanel headerPanel;
        private final JPanel placeholderPanel;
        private final JLabel commentLabel;
        private final Long number;
        
        public ExpandLabel(JPanel placeholderPanel, JTextPane textPane, JPanel headerPanel, JLabel commentLabel, Long number) {
            this.textPane = textPane;
            this.headerPanel = headerPanel;
            this.placeholderPanel = placeholderPanel;
            this.commentLabel = commentLabel;
            this.number = number;
            
            
            addMouseListener(this);
            setComponentPopupMenu(expandPopup);
            setState(isCollapsed(number));
            expandLabels.add(this);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                setState(!isCollapsed(number)); 
            } 
        }

        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        
        private void setState(boolean collapsed) {
            if(collapsed) {
                textPane.setVisible(false);
                placeholderPanel.setVisible(false);
                commentLabel.setText(textPane.getText().replace("\n", " ").replace("\t", " ")); // NOI18N
                setIcon(ci);
                headerPanel.setBackground(blueBackground);
                headerPanel.setOpaque( true );
                commentCollapsed(number);
            } else {
                textPane.setVisible(true);
                placeholderPanel.setVisible(true);
                commentLabel.setText("");
                setIcon(ei);
                headerPanel.setOpaque( false );
                commentExpanded(number);
            }           
        }
    }
    
    private class AttachmentLink implements HyperlinkSupport.Link {
        private BugzillaIssue.Attachment attachment;
        public AttachmentLink(BugzillaIssue.Attachment attachment) {
            this.attachment = attachment;
        }
        @Override
        public void onClick(String linkText) {
            attachment.open();
        }
    }
}
