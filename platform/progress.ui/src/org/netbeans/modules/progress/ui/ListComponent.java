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

package org.netbeans.modules.progress.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ListComponent extends JPanel {
    private NbProgressBar bar;
    private JLabel mainLabel;
    private JLabel dynaLabel;
    private JButton closeButton;
    private InternalHandle handle;
    private boolean watched;
    private Action cancelAction;
    private Color selectBgColor;
    private Color selectFgColor;
    private Color bgColor;
    private Color fgColor;
//    private Color dynaFgColor;
    private int mainHeight;
    private int dynaHeight;
    
    /** Creates a new instance of ListComponent */
    public ListComponent(InternalHandle hndl) {
        setFocusable(true);
        setRequestFocusEnabled(true);
//        setVerifyInputWhenFocusTarget(false);
        mainLabel = new JLabel();
        dynaLabel = new JLabel();
        // in gtk, the panel is non-opague, meaning we cannot color background
        // #59419
        setOpaque(true);
        dynaLabel.setFont(dynaLabel.getFont().deriveFont((float) (dynaLabel.getFont().getSize() - 2)));
        bar = new NbProgressBar();        
        handle = hndl;
        Color bg = UIManager.getColor("nbProgressBar.popupText.background");
        if (bg != null) {
            setBackground(bg);
            mainLabel.setBackground(bg);
            dynaLabel.setBackground(bg);
        }
        bgColor = getBackground();
        Color dynaFg = UIManager.getColor("nbProgressBar.popupDynaText.foreground");
        if (dynaFg != null) {
            dynaLabel.setForeground(dynaFg);
        }
//        dynaFgColor = dynaLabel.getForeground();
        fgColor = UIManager.getColor("nbProgressBar.popupText.foreground");
        if (fgColor != null) {
            mainLabel.setForeground(fgColor);
        }
        fgColor = mainLabel.getForeground();
        selectBgColor = UIManager.getColor("nbProgressBar.popupText.selectBackground");
        if (selectBgColor == null) {
            selectBgColor = UIManager.getColor("List.selectionBackground");
        }
        selectFgColor = UIManager.getColor("nbProgressBar.popupText.selectForeground");
        if (selectFgColor == null) {
            selectFgColor = UIManager.getColor("List.selectionForeground");
        }
        bar.setToolTipText(NbBundle.getMessage(ListComponent.class, "ListComponent.bar.tooltip"));
        bar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //start figure out height
        mainLabel.setText("XYZ");
        dynaLabel.setText("XYZ");
        mainHeight = mainLabel.getPreferredSize().height;
        dynaHeight = dynaLabel.getPreferredSize().height;
        mainLabel.setText(null);
        dynaLabel.setText(null);
        //end figure out height
        
        setLayout(new CustomLayout());
        add(mainLabel);
        add(bar);
        MListener list = new MListener();
        if (handle.isAllowCancel()) {
            cancelAction = new CancelAction(false);
            closeButton = new JButton(cancelAction);
            closeButton.setBorderPainted(false);
            closeButton.setBorder(BorderFactory.createEmptyBorder());
            closeButton.setOpaque(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setFocusable(false);
            
            Object img = UIManager.get("nb.progress.cancel.icon");
            if( null != img ) {
                closeButton.setIcon( iconOrImage2icon( img ) );
            }
            img = UIManager.get("nb.progress.cancel.icon.mouseover");
            if( null != img ) {
                closeButton.setRolloverEnabled(true);
                closeButton.setRolloverIcon( iconOrImage2icon( img ) );
            }
            img = UIManager.get("nb.progress.cancel.icon.pressed");
            if( null != img ) {
                closeButton.setPressedIcon( iconOrImage2icon( img ) );
            }
            
            closeButton.setToolTipText(NbBundle.getMessage(ListComponent.class, "ListComponent.btnClose.tooltip"));
            add(closeButton);
            if (handle.getState() != InternalHandle.STATE_RUNNING) {
                closeButton.setEnabled(false);
            }
        }
        add(dynaLabel);
        setBorder(BorderFactory.createEmptyBorder());
        addMouseListener(list);
        bar.addMouseListener(list);
        mainLabel.addMouseListener(list);
        dynaLabel.addMouseListener(list);
        if (handle.isAllowCancel()) {
            closeButton.addMouseListener(list);
        }
        
        mainLabel.setText(handle.getDisplayName());
        mainLabel.setToolTipText(mainLabel.getText());
        NbProgressBar.setupBar(handle, bar);
        addFocusListener(new FocusListener() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (!e.isTemporary()) {
                    setBackground(selectBgColor);
                    mainLabel.setBackground(selectBgColor);
                    dynaLabel.setBackground(selectBgColor);
                    mainLabel.setForeground(selectFgColor);
                    // TODO assuming now that dynalabel has always the same foreground color
                    // seems to be the case according to the spec
                    scrollRectToVisible(getBounds());
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (!e.isTemporary()) {
                    setBackground(bgColor);
                    mainLabel.setBackground(bgColor);
                    dynaLabel.setBackground(bgColor);
                    mainLabel.setForeground(fgColor);
                    // TODO assuming now that dynalabel has always the same foreground color
                    // seems to be the case according to the spec
                    
                }
            }
            
        });
        
    }

    static Icon iconOrImage2icon( Object iconOrImage ) {
        return (iconOrImage instanceof Icon)
                ? (Icon) iconOrImage
                : new ImageIcon( (Image) iconOrImage );
    }
    
    
    Action getCancelAction() {
        return cancelAction;
    }
    
    InternalHandle getHandle() {
        return handle;
    }
    
    void processProgressEvent(ProgressEvent event) {
        if (event.getType() == ProgressEvent.TYPE_PROGRESS || 
                event.getType() == ProgressEvent.TYPE_SWITCH || 
                event.getType() == ProgressEvent.TYPE_SILENT) {
            if (event.getSource() != handle) {
                throw new IllegalStateException();
            }
            if (event.isSwitched()) {
                NbProgressBar.setupBar(event.getSource(), bar);
            }
            if (event.getWorkunitsDone() > 0) {
                bar.setValue(event.getWorkunitsDone());
            }
            bar.setString(StatusLineComponent.getBarString(event.getPercentageDone(), event.getEstimatedCompletion()));
            if (event.getMessage() != null) {
                dynaLabel.setText(event.getMessage());
            }
            if (event.getSource().getState() == InternalHandle.STATE_REQUEST_STOP) {
                closeButton.setEnabled(false);
            }
            if (event.getDisplayName() != null) {
                mainLabel.setText(event.getDisplayName());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    void markAsActive(boolean sel) {
        if (sel == watched) {
            return;
        }
        watched = sel;
        if (sel) {
            mainLabel.setFont(mainLabel.getFont().deriveFont(Font.BOLD));
        } else {
            mainLabel.setFont(mainLabel.getFont().deriveFont(Font.PLAIN));
        }
        if (mainLabel.isVisible()) {
            mainLabel.repaint();
        }
    }

    //Called to clear the progressbar
    void clearProgressBarOSX() {
        //EMI: On OSX, an animation thread is started when the progressbar is created, even if not displayed (or added to a container).
        // The following line is needed to kill the animation thread otherwise this tends to be alive for the rest of the JVM execution
        // pumping a lot of repaing events in the event queue (in my tests, about 50% of the CPU while idle).
        // The culprit apple.laf.CUIAquaProgressBar$Animator was discovered with the normal Profiler, while Tim Boudreau told me about a similar
        // problem with OSX and the pulsating button (that also has a thread for that animation).
        // Finally, btrace and this IDEA bug report (http://www.jetbrains.net/jira/browse/IDEADEV-25376) connected the dots.
        if (bar != null) {
            bar.getUI().uninstallUI(bar);
        }
    }
    
    private class MListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == bar) {
                handle.requestExplicitSelection();
//                markAsSelected(true);
            }
            if (e.getClickCount() > 1 && (e.getSource() == mainLabel || e.getSource() == dynaLabel)) {
                handle.requestView();
            }
            if (e.getButton() != MouseEvent.BUTTON1) {
                showMenu(e);
            } else {
                ListComponent.this.requestFocus();
            }
            
//            System.out.println("list component requesting focus..");
        }
    }
    
    private void showMenu(MouseEvent e) {
        if (!isShowing()) {
            return;
        }
        JPopupMenu popup = new JPopupMenu();
        //mark teh popup for the status line awt listener
        popup.setName("progresspopup");
        popup.add(new ViewAction());
        popup.add(new WatchAction());
        popup.add(new CancelAction(true));
        popup.show((Component)e.getSource(), e.getX(), e.getY());
    }    
    
   private class CancelAction extends AbstractAction {
       CancelAction(boolean text) {
           if (text) {
               putValue(Action.NAME, NbBundle.getMessage(ListComponent.class, "StatusLineComponent.Cancel"));
               putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
           } else {
               Object icon = UIManager.get("nb.progress.cancel.icon");
               if (icon == null) {
                   // for custom L&F?
                   icon = ImageUtilities.loadImage("org/netbeans/progress/module/resources/buton.png");
               }
               putValue(Action.SMALL_ICON, iconOrImage2icon(icon));
           }
            setEnabled(handle == null ? false : handle.isAllowCancel());
       }

        public void actionPerformed(ActionEvent actionEvent) {
            if (handle.getState() == InternalHandle.STATE_RUNNING) {
                String message = NbBundle.getMessage(ListComponent.class, "Cancel_Question", handle.getDisplayName());
                String title = NbBundle.getMessage(ListComponent.class, "Cancel_Question_Title");
                NotifyDescriptor dd = new NotifyDescriptor(message, title, 
                                           NotifyDescriptor.YES_NO_OPTION,
                                           NotifyDescriptor.QUESTION_MESSAGE, null, null);
                Object retType = DialogDisplayer.getDefault().notify(dd);
                if (retType == NotifyDescriptor.YES_OPTION) {
                    handle.requestCancel();
                }
            }
        }
    }

    private class ViewAction extends AbstractAction {
        public ViewAction() {
            putValue(Action.NAME, NbBundle.getMessage(ListComponent.class, "StatusLineComponent.View"));
            setEnabled(handle == null ? false : handle.isAllowView());
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        }
        public void actionPerformed(ActionEvent actionEvent) {
            if (handle != null) {
                handle.requestView();
            }
        }
    }    

    private class WatchAction extends AbstractAction {
        public WatchAction() {
            putValue(Action.NAME, NbBundle.getMessage(ListComponent.class, "ListComponent.Watch"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
            setEnabled(true);
        }
        public void actionPerformed(ActionEvent actionEvent) {
            if (handle != null) {
                handle.requestExplicitSelection();
            }
        }
    }    
    
    
    private static final int UPPERMARGIN = 3;
    private static final int LEFTMARGIN = 2;
    private static final int BOTTOMMARGIN = 2;
    private static final int BETWEENTEXTMARGIN = 3;
    
    static final int ITEM_WIDTH = 600;
    
    private class CustomLayout implements LayoutManager {
        
        /**
         * If the layout manager uses a per-component string,
         * adds the component <code>comp</code> to the layout,
         * associating it 
         * with the string specified by <code>name</code>.
         * 
         * @param name the string to be associated with the component
         * @param comp the component to be added
         */
        public void addLayoutComponent(String name, java.awt.Component comp) {
        }

        /**
         * Calculates the preferred size dimensions for the specified 
         * container, given the components it contains.
         * @param parent the container to be laid out
         *  
         * @see #minimumLayoutSize
         */
        public Dimension preferredLayoutSize(java.awt.Container parent) {
            int height = UPPERMARGIN + mainHeight + BETWEENTEXTMARGIN + dynaHeight + BOTTOMMARGIN;
            return new Dimension(ITEM_WIDTH, height);
        }

        /**
         * 
         * Lays out the specified container.
         * @param parent the container to be laid out 
         */
        public void layoutContainer(java.awt.Container parent) {
            int parentWidth = parent.getWidth();
            int parentHeight = parent.getHeight();
            int offset = parentWidth - 18;
            if (closeButton != null) {
                closeButton.setBounds(offset, UPPERMARGIN, 18, mainHeight);            
            }
            // have the bar approx 30 percent of the width
            int barOffset = offset - (ITEM_WIDTH / 3);
            int barY = UPPERMARGIN;
            int barHeight = mainHeight;
            if (UIManager.getLookAndFeel().getID().startsWith("FlatLaf")) {
                // use smaller (preferred) height
                barHeight = bar.getPreferredSize().height;
                barY += (mainHeight - barHeight) / 2;
            }
            bar.setBounds(barOffset, barY, offset - barOffset, barHeight);
            mainLabel.setBounds(LEFTMARGIN, UPPERMARGIN, barOffset - LEFTMARGIN, mainHeight);
            dynaLabel.setBounds(LEFTMARGIN, mainHeight + UPPERMARGIN + BETWEENTEXTMARGIN, 
                                parentWidth - LEFTMARGIN, dynaHeight);
        }

        /**
         * 
         * Calculates the minimum size dimensions for the specified 
         * container, given the components it contains.
         * @param parent the component to be laid out
         * @see #preferredLayoutSize
         */
        public Dimension minimumLayoutSize(java.awt.Container parent) {
            return preferredLayoutSize(parent);
        }

        /**
         * Removes the specified component from the layout.
         * @param comp the component to be removed
         */
        public void removeLayoutComponent(java.awt.Component comp) {
        }

        
    }
}
