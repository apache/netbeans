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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.netbeans.modules.progress.spi.ProgressUIWorkerWithModel;
import org.netbeans.modules.progress.spi.TaskModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public class StatusLineComponent extends JPanel implements ProgressUIWorkerWithModel {
    private NbProgressBar bar;
    private JLabel label;
    private JSeparator separator;
    private InternalHandle handle;
    private boolean showingPopup = false;
    private TaskModel model;
    private MouseListener mouseListener;
    private HideAWTListener hideListener;
    private JWindow popupWindow;
    private PopupPane pane;
    private Map<InternalHandle, ListComponent> handleComponentMap;
    private final int preferredHeight;
    private JButton closeButton;
    private JLabel extraLabel;
    public StatusLineComponent() {
        handleComponentMap = new HashMap<InternalHandle, ListComponent>();
        FlowLayout flay = new FlowLayout();
        flay.setVgap(1);
        flay.setHgap(5);
        setLayout(flay);
        mouseListener = new MListener();
        addMouseListener(mouseListener);
        hideListener = new HideAWTListener();
        
        createLabel();
        createBar();
        // tricks to figure out correct height.
        bar.setStringPainted(true);
        bar.setString("@@@"); // NOI18N
        label.setText("@@@"); // NOI18N
        preferredHeight = Math.max(label.getPreferredSize().height, bar.getPreferredSize().height) + 3;
        setOpaque(false);
        discardLabel();
        discardBar();
        
        pane = new PopupPane();
        pane.getActionMap().put("HidePopup", new AbstractAction() {
            public @Override void actionPerformed(ActionEvent actionEvent) {
//                System.out.println("escape pressed - hiding");
                hidePopup();
            }
        });
        pane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "HidePopup");
        pane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "HidePopup");
        
        
    }
    
    private void createLabel() {
        discardLabel();
        label = new JLabel();
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(mouseListener);
    }
    
    private void discardLabel() {
        if (label != null) {
            label.removeMouseListener(mouseListener);
            label = null;
        }
    }

    private void createExtraLabel() {
        discardExtraLabel();
        extraLabel = new JLabel();
        extraLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        extraLabel.addMouseListener(mouseListener);
    }

    private void discardExtraLabel() {
        if (extraLabel != null) {
            extraLabel.removeMouseListener(mouseListener);
            extraLabel = null;
        }
    }

    private void createBar() {
        discardBar();
        bar = new NbProgressBar();
        bar.setUseInStatusBar(true);
        bar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
        // HACK - put smaller font inside the progress bar to keep
        // the height of the progressbar constant for determinate and indeterminate bars
//        Font fnt = UIManager.getFont("ProgressBar.font");
//        bar.setFont(fnt.deriveFont(fnt.getStyle(), fnt.getSize() - 3));
        bar.addMouseListener(mouseListener);
        
    }
    
    private void discardBar() {
        if (bar != null) {
            bar.removeMouseListener(mouseListener);
            //EMI: On OSX, an animation thread is started when the progressbar is created, even if not displayed (or added to a container).
            // The following line is needed to kill the animation thread otherwise this tends to be alive for the rest of the JVM execution
            // pumping a lot of repaing events in the event queue (in my tests, about 50% of the CPU while idle).
            // The culprit apple.laf.CUIAquaProgressBar$Animator was discovered with the normal Profiler, while Tim Boudreau told me about a similar
            // problem with OSX and the pulsating button (that also has a thread for that animation).
            // Finally, btrace and this IDEA bug report (http://www.jetbrains.net/jira/browse/IDEADEV-25376) connected the dots.
            bar.getUI().uninstallUI(bar);
            bar = null;
        }
    }
    
    private void createCloseButton() {
        discardCloseButton();
        closeButton = new JButton();
        closeButton.setBorderPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        
        Object img = UIManager.get("nb.progress.cancel.icon");
        if( null != img ) {
            closeButton.setIcon( ListComponent.iconOrImage2icon( img ) );
        }
        img = UIManager.get("nb.progress.cancel.icon.mouseover");
        if( null != img ) {
            closeButton.setRolloverEnabled(true);
            closeButton.setRolloverIcon( ListComponent.iconOrImage2icon( img ) );
        }
        img = UIManager.get("nb.progress.cancel.icon.pressed");
        if( null != img ) {
            closeButton.setPressedIcon( ListComponent.iconOrImage2icon( img ) );
        }
    }
    
    private void setCloseButtonNameAndTooltip() {
        closeButton.setName(NbBundle.getMessage(ListComponent.class, "ListComponent.btnClose.name"));
        closeButton.setToolTipText(NbBundle.getMessage(ListComponent.class, "ListComponent.btnClose.tooltip"));
    }
    
    private void discardCloseButton() {
        closeButton = null;
    }

    private void createSeparator() {
        discardSeparator();
        separator = new JSeparator(JSeparator.VERTICAL);
//        separator.setPreferredSize(new Dimension(5, prefferedHeight));
        separator.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
    }
    
    private void discardSeparator() {
        separator = null;
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension retValue;
        retValue = super.getPreferredSize();
        retValue.height = preferredHeight;
        return retValue;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension retValue;
        retValue = super.getMinimumSize();
        retValue.height = preferredHeight;
        return retValue;
    }        
    
    @Override
    public Dimension getMaximumSize() {
        Dimension retValue;
        retValue = super.getMaximumSize();
        retValue.height = preferredHeight;
        return retValue;
    }        
    
    public @Override void setModel(TaskModel mod) {
        model = mod;
        model.addListDataListener(new Listener());
        model.addListSelectionListener(new ListSelectionListener() {
            public @Override void valueChanged(ListSelectionEvent e) {
                pane.updateBoldFont(model.getSelectedHandle());
            }
        });
    }
    
    private void setTooltipForAll() {
        int size = model.getSize();
        String key = "NbProgressBar.tooltip1"; //NOI18N
        if (size == 1) {
            key = "NbProgressBar.tooltip2"; //NOI18N
        }
        String text = NbBundle.getMessage(StatusLineComponent.class, key, size);
        setToolTipText(text);
        if (label != null) {
            label.setToolTipText(text);
        }
        if (bar != null) {
            bar.setToolTipText(text);
        }
    }
    
    public @Override void processProgressEvent(ProgressEvent event) {
        if (event.getType() == ProgressEvent.TYPE_START) {
            createListItem(event.getSource());
        } else if (event.getType() == ProgressEvent.TYPE_PROGRESS || 
                   event.getType() == ProgressEvent.TYPE_SWITCH || 
                   event.getType() == ProgressEvent.TYPE_SILENT) {
            ListComponent comp = handleComponentMap.get(event.getSource());
            if (comp == null) {
                createListItem(event.getSource());
                comp = handleComponentMap.get(event.getSource());
            }
            comp.processProgressEvent(event);
        } else if (event.getType() == ProgressEvent.TYPE_FINISH) {
            removeListItem(event.getSource());
            if (model.getSelectedHandle() != null && handle != model.getSelectedHandle()) {
                ProgressEvent snap = model.getSelectedHandle().requestStateSnapshot();
                initializeComponent(snap);
                if (snap.getSource().isInSleepMode()) {
                    bar.setString(snap.getMessage());
                }
                
            }
            //release handle reference if it is the last one
            if (model.getSize() == 0 && event.getSource().equals(handle)) {
                handle = null;
            }
        }
        
    }
    
    public @Override void processSelectedProgressEvent(ProgressEvent event) {
        if (event.getType() == ProgressEvent.TYPE_START) {
            initializeComponent(event);
            return;
        } else if (event.getType() == ProgressEvent.TYPE_FINISH) {
            //happens only when there's no more handles.
            hidePopup();
            removeAll();
            discardSeparator();
            discardCloseButton();
            discardBar();
            discardLabel();
            discardExtraLabel();
            //#63393, 61940 fix - removeAll() just invalidates. seems to work without revalidate/repaint on some platforms, fail on others.
            revalidate();
            repaint();
            return;
        } else {
            if (event.getSource() != handle || event.isSwitched() || 
                event.getType() == ProgressEvent.TYPE_SILENT ||                    // the following condition re-initiates the bar when going from/to sleep mode..
                    (event.getSource().isInSleepMode() != (bar.getClientProperty(NbProgressBar.SLEEPY) != null))) { //NIO18N
                initializeComponent(event);
            }
            if (bar != null) {
                if (event.getWorkunitsDone() > 0) {
                   bar.setValue(event.getWorkunitsDone());
                }
                bar.setString(getBarString(event.getPercentageDone(), event.getEstimatedCompletion()));
                if (event.getDisplayName() != null) {
                    label.setText(event.getDisplayName());
                }
                if (event.getSource().isInSleepMode()) {
                    bar.setString(event.getMessage());
                }
            }
        } 
    }
    
    static String formatEstimate(long estimate) {
        long minutes = estimate / 60;
        long seconds = estimate - (minutes * 60);
        return "" + minutes + (seconds < 10 ? ":0" : ":") + seconds;
    }
    
    static String getBarString(double percentage, long estimatedCompletion) {
        if (estimatedCompletion != -1) {
            return formatEstimate(estimatedCompletion);
        }
        if (percentage != -1) {
            int rounded = (int) Math.round(percentage);
            if (rounded > 100) {
                rounded = 100;
            }
            return "" + rounded + "%"; //NOI18N
        }
        return "";
    }

    private void updateExtraLabel() {
        if (extraLabel != null) {
            if (handleComponentMap.size() > 1) {
                extraLabel.setText(NbBundle.getMessage(StatusLineComponent.class, "StatusLineComponent.extra", handleComponentMap.size() - 1));
            } else {
                extraLabel.setText(null);
            }
        }
    }
    
    private void initializeComponent(ProgressEvent event) {
        handle = event.getSource();
        boolean toShow = false;
        if (label == null) {
            createLabel();
            add(label);
            toShow = true;
            label.setToolTipText(getToolTipText());
        }
        label.setText(handle.getDisplayName());
        
        if (bar == null) {
            createBar();
            add(bar);
            toShow = true;
            bar.setToolTipText(getToolTipText());
            
        }
        NbProgressBar.setupBar(event.getSource(), bar);
        
        if (closeButton == null) {
            createCloseButton();
            add(closeButton);
            toShow = true;
        }
        if (extraLabel == null) {
            createExtraLabel();
            updateExtraLabel();
            add(extraLabel);
            toShow = true;
        }
        if (separator == null) {
            createSeparator();
            add(separator);
            toShow = true;
        }
        if (handle.isAllowCancel()) {
            closeButton.setAction(new CancelAction(false));
        } else {
            closeButton.setAction(new EmptyCancelAction());
        }
        
        // #200126: tooltip property must be set following the action or it will be overwritten
        setCloseButtonNameAndTooltip();
        
        if (toShow) {
            revalidate();
            repaint();
        }
    }
    
    private class Listener implements ListDataListener {
        public @Override void intervalAdded(ListDataEvent e) {
            setTooltipForAll();
        }
        public @Override void intervalRemoved(ListDataEvent e) {
            setTooltipForAll();
        }
        public @Override void contentsChanged(ListDataEvent e) {
            setTooltipForAll();
        }
    }
    
    public void hidePopup() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        if (popupWindow != null) {
//            popupWindow.getContentPane().removeAll();
            popupWindow.setVisible(false);
        }
        Toolkit.getDefaultToolkit().removeAWTEventListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().removeComponentListener(hideListener);
        showingPopup = false;
    }
    
    private void createListItem(InternalHandle handle) {
        ListComponent comp;
        if (handleComponentMap.containsKey(handle)) {
            // happens when we click to display on popup and there is a 
            // new handle waiting in the queue.
            comp = handleComponentMap.get(handle);
        } else {
            comp = new ListComponent(handle);
            handleComponentMap.put(handle, comp);
        }
        pane.addListComponent(comp);            
        pane.updateBoldFont(model.getSelectedHandle());
        if (showingPopup) {
            resizePopup();
        }
        updateExtraLabel();
    }
    
    private void removeListItem(InternalHandle handle) {
        ListComponent c = handleComponentMap.remove(handle);
        pane.removeListComponent(handle);
        pane.updateBoldFont(model.getSelectedHandle());
        if (showingPopup) {
            resizePopup();
        }
        if (c != null) {
            c.clearProgressBarOSX();
        }
        updateExtraLabel();
    }

    
    public @Override void showPopup() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        if (showingPopup) {
            return;
        }
        InternalHandle[] handles = model.getHandles();
        if (handles.length == 0) {
            // just in case..
            return;
        }
        showingPopup = true;
        
        // NOT using PopupFactory
        // 1. on linux, creates mediumweight popup taht doesn't refresh behind visible glasspane
        // 2. on mac, needs an owner frame otherwise hiding tooltip also hides the popup. (linux requires no owner frame to force heavyweight)
        // 3. the created window is not focusable window
        if (popupWindow == null) {
            popupWindow = new JWindow(WindowManager.getDefault().getMainWindow());
            popupWindow.getContentPane().add(pane);
        }
        Toolkit.getDefaultToolkit().addAWTEventListener(hideListener, AWTEvent.MOUSE_EVENT_MASK);
        WindowManager.getDefault().getMainWindow().addWindowStateListener(hideListener);
        WindowManager.getDefault().getMainWindow().addComponentListener(hideListener);
        resizePopup();
        popupWindow.setVisible(true);
        pane.requestFocus();
        pane.repaint();
//        System.out.println("     window focusable=" + popupWindow.isFocusableWindow());
    }
    
    private void resizePopup() {
        popupWindow.pack();
        Point point = new Point(0,0);
        SwingUtilities.convertPointToScreen(point, this);
        Dimension dim = popupWindow.getSize();
        //#63265 
        Rectangle usableRect = Utilities.getUsableScreenBounds();
        int sepShift = separator != null ? separator.getSize().width : 0;
        Point loc = new Point(point.x + this.getSize().width - dim.width - sepShift - 5 * 2  , point.y - dim.height - 5);
        // -5 in x coordinate is becuase of the hgap between the separator and button and separator and edge
        //#JDEV #17370036
        if( loc.x < usableRect.x )
            loc.x = Math.max( loc.x, usableRect.x );
        if( loc.x+dim.width > usableRect.x+usableRect.width )
            loc.x = usableRect.x + usableRect.width - dim.width;
        if (! usableRect.contains(loc)) {
            loc = new Point(loc.x, point.y + 5 + this.getSize().height);
        }
            // +4 here because of the width of the close button in popup, we
            // want the progress bars to align visually.. but there's separator in status now..
        popupWindow.setLocation(loc);
//        System.out.println("count=" + count);
//        System.out.println("offset =" + offset);
    }
    
    private class HideAWTListener extends ComponentAdapter implements  AWTEventListener, WindowStateListener {
        public @Override void eventDispatched(java.awt.AWTEvent aWTEvent) {
            if (aWTEvent instanceof MouseEvent) {
                MouseEvent mv = (MouseEvent)aWTEvent;
                if (mv.getClickCount() > 0) {
                    //#118828
                    if (! (aWTEvent.getSource() instanceof Component)) {
                        return;
                    }
                    Component comp = (Component)aWTEvent.getSource();
                    Container par = SwingUtilities.getAncestorNamed("progresspopup", comp); //NOI18N
                    Container barpar = SwingUtilities.getAncestorOfClass(StatusLineComponent.class, comp);
                    if (par == null && barpar == null) {
                        hidePopup();
                    }
                }
            }
        }

        public @Override void windowStateChanged(WindowEvent windowEvent) {
            if (showingPopup) {
                int oldState = windowEvent.getOldState();
                int newState = windowEvent.getNewState();
            
                if (((oldState & Frame.ICONIFIED) == 0) &&
                    ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                    hidePopup();
//                } else if (((oldState & Frame.ICONIFIED) == Frame.ICONIFIED) && 
//                           ((newState & Frame.ICONIFIED) == 0 )) {
//                    //TODO remember we showed before and show again? I guess not worth the efford, not part of spec.
                }
            }

        }
        
        @Override
        public void componentResized(ComponentEvent evt) {
            if (showingPopup) {
                resizePopup();
            }
        }
        
        @Override
        public void componentMoved(ComponentEvent evt) {
            if (showingPopup) {
                resizePopup();
            }
        }        
        
    }
    
    private class MListener extends MouseAdapter {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                showMenu(e);
            } else {
                if (showingPopup) {
                    hidePopup();
                } else {
                    showPopup();
                }
            }
        }
        
    }
    
    private void showMenu(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new ProgressListAction(NbBundle.getMessage(StatusLineComponent.class, "StatusLineComponent.ShowProcessList"))); 
        popup.add(new ViewAction());
        popup.add(new CancelAction(true));
        popup.show((Component)e.getSource(), e.getX(), e.getY());
    }
    
  private class CancelAction extends AbstractAction {
        public CancelAction(boolean text) {
            if (text) {
                putValue(Action.NAME, NbBundle.getMessage(StatusLineComponent.class, "StatusLineComponent.Cancel"));
            } else {
                Object icon = UIManager.get("nb.progress.cancel.icon");
                if (icon == null) {
                       // for custom L&F?
                    putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/progress/module/resources/buton.png", true));
                } else {
                    putValue(Action.SMALL_ICON, ListComponent.iconOrImage2icon(icon));
                }
            }
            setEnabled(handle == null ? false : handle.isAllowCancel());
        }
        public @Override void actionPerformed(ActionEvent actionEvent) {
            InternalHandle hndl = handle;
            if (hndl !=null && hndl.getState() == InternalHandle.STATE_RUNNING) {
                String message = NbBundle.getMessage(StatusLineComponent.class, "Cancel_Question", handle.getDisplayName());
                String title = NbBundle.getMessage(StatusLineComponent.class, "Cancel_Question_Title");
                NotifyDescriptor dd = new NotifyDescriptor(message, title, 
                                           NotifyDescriptor.YES_NO_OPTION,
                                           NotifyDescriptor.QUESTION_MESSAGE, null, null);
                Object retType = DialogDisplayer.getDefault().notify(dd);
                if (retType == NotifyDescriptor.YES_OPTION && hndl.getState() == InternalHandle.STATE_RUNNING) {
                    hndl.requestCancel();
                }
            }
        }
    }

    private class ViewAction extends AbstractAction {
        public ViewAction() {
            putValue(Action.NAME, NbBundle.getMessage(StatusLineComponent.class, "StatusLineComponent.View"));
            setEnabled(handle == null ? false : handle.isAllowView());
            
        }
        public @Override void actionPerformed(ActionEvent actionEvent) {
            if (handle != null) {
                handle.requestView();
            }
        }
    }    
    
    
    private class EmptyCancelAction extends AbstractAction {
        public EmptyCancelAction() {
            setEnabled(false);
            putValue(Action.SMALL_ICON, new Icon() {
                public @Override int getIconHeight() {
                    return 12;
                }
                public @Override int getIconWidth() {
                    return 12;
                }
                public @Override void paintIcon(Component c, Graphics g, int x, int y) {
                }
            });
            putValue(Action.NAME, "");
        }

        public @Override void actionPerformed(ActionEvent e) {
        }
    }

}
