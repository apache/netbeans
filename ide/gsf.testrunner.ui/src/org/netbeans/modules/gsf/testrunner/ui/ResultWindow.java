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

package org.netbeans.modules.gsf.testrunner.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.IOContainer;
import org.openide.windows.IOContainer.CallBacks;
import org.openide.windows.InputOutput;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
public final class ResultWindow extends TopComponent {

    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "gsf-testrunner-results";              //NOI18N
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static WeakReference<ResultWindow> instance = null;

    private Map<String,JSplitPane> viewMap = new HashMap<String,JSplitPane>();
    private Map<String,InputOutput> ioMap = new HashMap<String,InputOutput>();

    private static JTabbedPane tabPane;
    private JPopupMenu pop;
    private PopupListener popL;
    private CloseListener closeL;

    /**
     * Returns a singleton of this class.
     *
     * @return  singleton of this class
     */
    public static ResultWindow getInstance() {
        final ResultWindow[] result = new ResultWindow[1];
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        result[0] = getResultWindow();
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            result[0] = getResultWindow();
        }
        return result[0];
    }


    private static synchronized ResultWindow getResultWindow() {
        ResultWindow result = (ResultWindow) WindowManager.getDefault().findTopComponent(ID);
        if (result == null) {
            result = getDefault();
        }
        return result;
    }

    /**
     * Singleton accessor reserved for the window system only.
     * The window system calls this method to create an instance of this
     * <code>TopComponent</code> from a <code>.settings</code> file.
     * <p>
     * <em>This method should not be called anywhere except from the window
     * system's code.</em>
     *
     * @return  singleton - instance of this class
     */
    public static synchronized ResultWindow getDefault() {
        ResultWindow window = (instance != null) ? instance.get() : null;
        if (window == null) {
            window = new ResultWindow();
            window.initActions();
            instance = new WeakReference<ResultWindow>(window);
        }
        return window;
    }

    private void initActions() {
        ActionMap actions = getActionMap();
        actions.put("jumpNext", new PrevNextFailure(true));  //NOI18N
        actions.put("jumpPrev", new PrevNextFailure(false));  //NOI18N
    }

    /** */
//    private JSplitPane view;
//    private Object lookup;

    /** Creates a new instance of ResultWindow */
    @NbBundle.Messages({"TITLE_TEST_RESULTS=Test Results",
        "ACSN_TestResults=Test Results",
        "ACSD_TestResults=Displays information about passed and failed tests and output generated by them"})
    public ResultWindow() {
        super();
        setFocusable(true);
        setLayout(new BorderLayout());

        setName(ID);
        setDisplayName(Bundle.TITLE_TEST_RESULTS());
        setIcon(ImageUtilities.loadImage( "org/netbeans/modules/gsf/testrunner/ui/resources/testResults.png", true));//NOI18N
	        
        AccessibleContext accContext = getAccessibleContext();
        accContext.setAccessibleName(Bundle.ACSN_TestResults());
        accContext.setAccessibleDescription(Bundle.ACSD_TestResults());

        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        popL = new PopupListener();
        closeL = new CloseListener();

        tabPane = TabbedPaneFactory.createCloseButtonTabbedPane();
        tabPane.setMinimumSize(new Dimension(0, 0));
        tabPane.addMouseListener(popL);
        tabPane.addPropertyChangeListener(closeL);
        add(tabPane);
    }

    /**
     */
    public void addDisplayComponent(JSplitPane displayComp, InputOutput io) {
        assert EventQueue.isDispatchThread();
        String key = displayComp.getToolTipText();

	boolean alwaysOpenNewTab = NbPreferences.forModule(StatisticsPanel.class).getBoolean(StatisticsPanel.PROP_ALWAYS_OPEN_NEW_TAB, false);
	if (alwaysOpenNewTab) {
	    int frequency = getFrequency(viewMap.keySet(), key);
	    if (frequency > 0) {
		key = key.concat(" #").concat(Integer.toString(frequency));   //NOI18N
		displayComp.setToolTipText(key);
	    }
	}

        JSplitPane prevComp = viewMap.put(key, displayComp);
        InputOutput prevIo = ioMap.put(key, io);
        if (alwaysOpenNewTab || prevComp == null){
            addView(displayComp);
        }else{
            replaceView(prevComp, displayComp);
            if (prevIo != null) {
                prevIo.closeInputOutput();
            }
        }
        revalidate();
    }

    private int getFrequency(Set<String> c, String tooltip) {
	int result = 0;
	int max = 0;
	for (String key : c) {
	    if (key.startsWith(tooltip)) {
		result++;
                int index = key.indexOf(" #");   //NOI18N
                if (index != -1) {
                    max = Math.max(max, Integer.parseInt(key.substring(index + 2)));
                }
	    }
	}
	return result == 0 ? 0 : Math.max(max + 1, result);
    }

    public void updateOptionStatus(String property, boolean selected) {
	NbPreferences.forModule(StatisticsPanel.class).putBoolean(property, selected);
	for (int i = 0; i < tabPane.getTabCount(); i++) {
	    StatisticsPanel sp = (StatisticsPanel)((JSplitPane)tabPane.getComponentAt(i)).getLeftComponent();
	    sp.updateOptionStatus(property, selected);
	}
    }

    /**
     */
    private void addView(final JSplitPane view) {
        assert EventQueue.isDispatchThread();

        view.setMinimumSize(new Dimension(0, 0));
        tabPane.addTab(view.getToolTipText(), view);
        tabPane.setSelectedComponent(view);
        tabPane.validate();
    }

    private void replaceView(JSplitPane oldView, JSplitPane newView){
        for (int i=0; i < tabPane.getTabCount(); i++){
            if (oldView.equals(tabPane.getComponentAt(i))){
                tabPane.setComponentAt(i, newView);
                tabPane.setSelectedComponent(newView);
                tabPane.validate();
                copyFilterMask(oldView, newView);
                continue;
            }
        }
    }
    
    private void copyFilterMask(JSplitPane oldView, JSplitPane newView) {
        StatisticsPanel oldSP = (StatisticsPanel)oldView.getLeftComponent();
        StatisticsPanel newSP = (StatisticsPanel)newView.getLeftComponent();
        newSP.copyFilterMask(oldSP);
    }

    /**
     */
    public void promote() {
        assert EventQueue.isDispatchThread();

        open();
        requestVisible();
        // don't activate, see #145382
        //requestActive();
    }

    /**
     * Sets the layout orientation of the contained result pane.
     *
     * @param orientation the orientation (see {@link JSplitPane#VERTICAL_SPLIT}
     * and {@link JSplitPane#HORIZONTAL_SPLIT}) to set.
     */
    public void setOrientation(int orientation) {
        for(JSplitPane view: viewMap.values()){
            if (view.getOrientation() != orientation) {
                view.setOrientation(orientation);
            }
        }
    }

    /**
     */
    @Override
    protected String preferredID() {
        return ID;
    }

    /**
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    /**
     */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
/*
    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            return super.getLookup();
        }
        if (lookup instanceof Reference) {
            Object l = ((Reference) lookup).get();

            if (l instanceof Lookup) {
                return (Lookup) l;
            }
        }
        return Lookup.EMPTY;
    }
*/
    /**
     * Resolves to the {@linkplain #getDefault default instance} of this class.
     *
     * This method is necessary for correct functinality of window system's
     * mechanism of persistence of top components.
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        return ResultWindow.getDefault();
    }

    private boolean activated;
    private JComponent outputComp;
    private JComponent outputTab;
    private IOContainer ioContainer;

    public IOContainer getIOContainer() {
        if (ioContainer == null) {
            ioContainer = IOContainer.create(new IOContainerImpl());
        }
        return ioContainer;
    }

    public void setOutputComp(JComponent comp) {
        outputComp = comp;
    }

    @Override
    protected void componentActivated() {
        activated = true;
    }

    @Override
    protected void componentDeactivated() {
        activated = false;
    }

    private static JSplitPane getCurrentResultView(){
        if(tabPane == null) {
            // Test Results Window is not opened yet
            return null;
        }
        return (JSplitPane)tabPane.getSelectedComponent();
    }

    public @Override boolean requestFocusInWindow() {
        JSplitPane view = getCurrentResultView();
        if (view == null) {
            return super.requestFocusInWindow();
        }
        Component left = view.getLeftComponent();
        if (left == null) {
            return super.requestFocusInWindow();
        }
        return left.requestFocusInWindow();
    }

    private void closeAll(boolean butCurrent) {
        Component current = tabPane.getSelectedComponent();
        Component[] c =  tabPane.getComponents();
        for (int i = 0; i< c.length; i++) {
            if (butCurrent && c[i]==current) {
                continue;
            }
            if(c[i] instanceof JSplitPane) {
                removeView((JSplitPane) c[i]);
            }
        }
    }

    @Override
    protected void componentClosed() {
        closeAll(false);
        super.componentClosed();
    }

    private void removeView(JSplitPane view) {
        // probably it's need to stop testing if in progress?
        
        if (view == null) {
            view = (JSplitPane) tabPane.getSelectedComponent();
            if (view == null) {
                return;
            }
        }
        tabPane.remove(view);
        viewMap.remove(view.getToolTipText());
        InputOutput io = ioMap.remove(view.getToolTipText());
        if (io != null) {
            io.closeInputOutput();
        }

        validate();
    }

    private class IOContainerImpl implements IOContainer.Provider {

        public void remove(JComponent comp) {
            outputTab = null;
            outputComp.remove(comp);
        }

        public void select(JComponent comp) {
        }

        public JComponent getSelected() {
            return outputTab;
        }

        public boolean isActivated() {
            return activated;
        }

        public void open() {
        }

        public void requestActive() {
        }

        public void requestVisible() {
        }

        public void setIcon(JComponent comp, Icon icon) {
        }

        public void setTitle(JComponent comp, String name) {
        }

        public void setToolTipText(JComponent comp, String name) {
        }

        public void add(JComponent comp, CallBacks cb) {
            outputTab = comp;
            outputComp.add(comp);
        }

        public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
        }

        public boolean isCloseable(JComponent comp) {
            return false;
        }
    }

    private final class PrevNextFailure extends AbstractAction {

        private final boolean next;

        public PrevNextFailure(boolean next) {
            this.next = next;
        }

        public void actionPerformed(ActionEvent e) {
            JSplitPane view = getCurrentResultView();
            if (view == null || !(view.getLeftComponent() instanceof StatisticsPanel)) {
                return;
            }
            StatisticsPanel statisticsPanel = (StatisticsPanel) view.getLeftComponent();
            if (next) {
                statisticsPanel.selectNextFailure();
            } else {
                statisticsPanel.selectPreviousFailure();
            }
        }
    }

    @ActionID(category = "CommonTestRunner", id = "org.netbeans.modules.gsf.testrunner.ui.ResultWindow.Rerun")
    @ActionRegistration(displayName = "#CTL_Rerun")
    @NbBundle.Messages("CTL_Rerun=Rerun All Tests")
    public static final class Rerun extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
	    StatisticsPanel statisticsPanel = getStatisticsPanel();
	    if(statisticsPanel != null) {
		statisticsPanel.rerun(false);
	    }
        }
    }

    @ActionID(category = "CommonTestRunner", id = "org.netbeans.modules.gsf.testrunner.ui.ResultWindow.RerunFailed")
    @ActionRegistration(displayName = "#CTL_RerunFailed")
    @NbBundle.Messages("CTL_RerunFailed=Rerun Failed Tests")
    public static final class RerunFailed extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
	    StatisticsPanel statisticsPanel = getStatisticsPanel();
	    if(statisticsPanel != null) {
		statisticsPanel.rerun(true);
	    }
        }
    }

    private static StatisticsPanel getStatisticsPanel() {
	JSplitPane view = getCurrentResultView();
	if (view == null || !(view.getLeftComponent() instanceof StatisticsPanel)) {
	    return null;
	}
	return (StatisticsPanel) view.getLeftComponent();
    }

    @NbBundle.Messages("LBL_CloseWindow=Close Tab")
    private class Close extends AbstractAction {
        public Close() {
            super(Bundle.LBL_CloseWindow());
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            removeView(null);
        }
    }

    @NbBundle.Messages("LBL_CloseAll=Close All Tabs")
    private final class CloseAll extends AbstractAction {
        public CloseAll() {
            super(Bundle.LBL_CloseAll());
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            closeAll(false);
        }
    }

    @NbBundle.Messages("LBL_CloseAllButCurrent=Close Other Tabs")
    private class CloseAllButCurrent extends AbstractAction {
        public CloseAllButCurrent() {
            super(Bundle.LBL_CloseAllButCurrent());
        }
        public void actionPerformed(ActionEvent e) {
            closeAll(true);
        }
    }

    private class CloseListener implements PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                removeView((JSplitPane) evt.getNewValue());
            }
        }
    }

    private class PopupListener extends MouseUtils.PopupMouseAdapter {
        protected void showPopup (MouseEvent e) {
            pop.show(ResultWindow.this, e.getX(), e.getY());
        }
    }
}
