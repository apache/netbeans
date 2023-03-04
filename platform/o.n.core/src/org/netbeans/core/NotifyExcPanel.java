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

package org.netbeans.core;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import org.netbeans.core.startup.CLIOptions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Notifies exceptions.
 *
 * This class is public only because the MainWindow needs get the flashing
 * icon to its status bar from this class (method getNotificationVisualizer()).
 *
 * @author  Jaroslav Tulach
 */
public final class NotifyExcPanel extends JPanel implements ActionListener {
    static final long serialVersionUID =3680397500573480127L;


    /** the instance */
    private static NotifyExcPanel INSTANCE = null;
    /** preferred width of this component */
    private static final int SIZE_PREFERRED_WIDTH=550;
    /** preferred height of this component */
    private static final int SIZE_PREFERRED_HEIGHT=350;
    private static final int MAX_STORED_EXCEPTIONS = 500;
    private static final boolean AUTO_FOCUS = Boolean.getBoolean("netbeans.winsys.auto_focus"); // NOI18N

    /** enumeration of NbExceptionManager.Exc to notify */
    static ArrayListPos exceptions;
    /** current exception */
    private NbErrorManager.Exc current;

    /** dialog descriptor */
    private DialogDescriptor descriptor;
    /** dialog that displayes the exceptions */
    java.awt.Dialog dialog;
    /** button to show next exceptions */
    private JButton next;
    /** button to show previous exceptions */
    private JButton previous;
    /** details button */
    private JButton details;
    /** details window */
    private JTextArea output;

    /** boolean to show/hide details */
    private static boolean showDetails;
    
    /** the last position of the exception dialog window */
    private static Rectangle lastBounds;
    
    private static int extraH = 0, extraW = 0;

    /** Constructor.
    */
    private NotifyExcPanel () {
        java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(NotifyExcPanel.class);
        next = new JButton ();
        Mnemonics.setLocalizedText(next, bundle.getString("CTL_NextException"));
        // bugfix 25684, don't set Previous/Next as default capable
        next.setDefaultCapable (false);
        previous = new JButton ();
        Mnemonics.setLocalizedText(previous, bundle.getString("CTL_PreviousException"));
        previous.setDefaultCapable (false);
        details = new JButton ();
        details.setDefaultCapable (false);

        output = new JTextArea() {
            public @Override boolean getScrollableTracksViewportWidth() {
                return false;
            }
        };
        output.setEditable(false);
        output.setLineWrap(false);
        Font f = output.getFont();
        output.setFont(new Font("Monospaced", Font.PLAIN, null == f ? 12 : f.getSize() + 1)); // NOI18N
        output.setForeground(UIManager.getColor("Label.foreground")); // NOI18N
        output.setBackground(UIManager.getColor("Label.background")); // NOI18N

        setLayout( new BorderLayout() );
        add(new JScrollPane(output));
        setBorder( new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
            
        next.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_NextException"));
        previous.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_PreviousException"));
        output.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ExceptionStackTrace"));
        output.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ExceptionStackTrace"));
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_NotifyExceptionPanel"));

        descriptor = new DialogDescriptor ("", ""); // NOI18N

        descriptor.setMessageType (DialogDescriptor.ERROR_MESSAGE);
        descriptor.setOptions (computeOptions(previous, next));
        descriptor.setAdditionalOptions (new Object[] {
                                             details
                                         });
        descriptor.setClosingOptions (new Object[0]);
        descriptor.setButtonListener (this);

        // bugfix #27176, create dialog in modal state if some other modal
        // dialog is opened at the time
        // #53328 do not let the error dialog to be created modal unless the main
        // window is visible. otherwise the error message may be hidden behind
        // the main window thus making the main window unusable
        descriptor.setModal( isModalDialogPresent() 
                && WindowManager.getDefault().getMainWindow().isVisible() );
        
        setPreferredSize(new Dimension(SIZE_PREFERRED_WIDTH + extraW, SIZE_PREFERRED_HEIGHT + extraH));

        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        if( null != lastBounds ) {
            lastBounds.width = Math.max( lastBounds.width, SIZE_PREFERRED_WIDTH+extraW );
            dialog.setBounds( lastBounds );
        }
        
        dialog.getAccessibleContext().setAccessibleName(bundle.getString("ACN_NotifyExcPanel_Dialog")); // NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(bundle.getString("ACD_NotifyExcPanel_Dialog")); // NOI18N
    }

    static Object[] computeOptions(Object previous, Object next) {
        ArrayList<Object> arr = new ArrayList<java.lang.Object>();
        arr.add(previous);
        arr.add(next);
        
        extraH = 0;
        extraW = 0;
        
        for (Handler h : Logger.getLogger("").getHandlers()) {
            if (h instanceof Callable<?>) {
                boolean foundCallableForJButton = false;
                for (Type t : h.getClass().getGenericInterfaces()) {
                    if (t instanceof ParameterizedType) {
                        ParameterizedType p = (ParameterizedType)t;
                        Type[] params = p.getActualTypeArguments();
                        if (params.length == 1 && params[0] == JButton.class) {
                            foundCallableForJButton = true;
                            break;
                        }
                    }
                }
                if (!foundCallableForJButton) {
                    continue;
                }
                
                
                try {
                    Object o = ((Callable<?>)h).call();
                    if (o == null) {
                        continue;
                    }
                    assert o instanceof JButton;
                    JButton b = (JButton) o;
                    extraH += b.getPreferredSize ().height;
                    extraW += b.getPreferredSize ().width;
                    arr.add(o);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        arr.add(NotifyDescriptor.CANCEL_OPTION);
        return arr.toArray();
    }
    
    private static boolean isModalDialogPresent() {
        return hasModalDialog(WindowManager.getDefault().getMainWindow())
            // XXX Trick to get the shared frame instance.
            || hasModalDialog(new JDialog().getOwner());
    }
    
    private static boolean hasModalDialog(Window w) {
        if (w == null) { // #63830
            return false;
        }
        Window[] ws = w.getOwnedWindows();
        for(int i = 0; i < ws.length; i++) {
            if(ws[i] instanceof Dialog && ((Dialog)ws[i]).isModal() && ws[i].isVisible()) {
                return true;
            } else if(hasModalDialog(ws[i])) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * For unit-testing only
     */
    static void cleanInstance() {
        INSTANCE = null;
    }


    /** Adds new exception into the queue.
     */
    static void notify (
        final NbErrorManager.Exc t
    ) {
        if (!t.isUserQuestion() && !shallNotify(t.getSeverity(), false)) {
            return;
        }
        
        // #50018 Don't try to show any notify dialog when reporting headless exception
        if (/*"java.awt.HeadlessException".equals(t.getClassName()) &&*/ GraphicsEnvironment.isHeadless()) { // NOI18N
            t.printStackTrace(System.err);
            return;
        }

        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run() {
                String glm = t.getLocalizedMessage();
                Level gs = t.getSeverity();
                boolean loc = t.isLocalized();
                
                if (t.isUserQuestion() && loc) {
                    Object ret = DialogDisplayer.getDefault().notify(
                               new NotifyDescriptor.Confirmation(glm, NotifyDescriptor.OK_CANCEL_OPTION));
                    if (ret == NotifyDescriptor.OK_OPTION) {
                        try {
                            t.confirm();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    return;
                }

                if (loc) {
                    if (gs == Level.WARNING) {
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(glm, NotifyDescriptor.WARNING_MESSAGE)
                        );
                        return;
                    }

                    if (gs.intValue() == 1973) {
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(glm, NotifyDescriptor.INFORMATION_MESSAGE)
                        );
                        return;
                    }

                    if (gs == Level.SEVERE) {
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(glm, NotifyDescriptor.ERROR_MESSAGE)
                        );
                        return;
                    }
                }

                
                if( null == exceptions ) {
                    exceptions = new ArrayListPos();
                } else if (exceptions.size() >= MAX_STORED_EXCEPTIONS) {
                    // Ignore huge number of exceptions, prevents from OOME.
                    return ;
                }
                exceptions.add(t);
                exceptions.position = exceptions.size()-1;

                if(shallNotify(t.getSeverity(), true)) {
                    // Assertions are on, so show the exception window.
                    if( INSTANCE == null ) {
                        INSTANCE = new NotifyExcPanel();
                    }
                    INSTANCE.updateState(t);
                } else {
                    // No assertions, use the flashing icon.
                    if( null == INSTANCE ) {
                        ImageIcon img1 = ImageUtilities.loadImageIcon("org/netbeans/core/resources/exception.gif", true);
                        String summary = getExceptionSummary(t);
                        ExceptionFlasher flash = ExceptionFlasher.notify(summary, img1);
                        //exception window is not visible, start flashing the icon
                    } else {
                        //exception window is already visible (or the flashing icon is not available)
                        //so we'll only update the exception window
                        if( INSTANCE == null ) {
                            INSTANCE = new NotifyExcPanel();
                        }
                        INSTANCE.updateState(t);
                    }
                }
            }
        });
    }
    
    /**
     * @return A brief exception summary for the flashing icon tooltip (either 
     * the exception message or exception class name).
     */
    private static String getExceptionSummary( final NbErrorManager.Exc t ) {
        String plainmsg;
        String glm = t.getLocalizedMessage();
        if (glm != null) {
            plainmsg = glm;
        } else if (t.getMessage() != null) {
            plainmsg = t.getMessage();
        } else {
            plainmsg = t.getClassName();
        }
        assert plainmsg != null;
        return plainmsg;
    }


    /**
     * updates the state of the dialog. called only in AWT thread.
     */
    private void updateState (NbErrorManager.Exc t) {
        if (!exceptions.existsNextElement()) {
            // it can be commented out while INSTANCE is not cached
            // (see the comment in actionPerformed)
            /*// be modal if some modal dialog is already opened, nonmodal otherwise
            boolean isModalDialogOpened = NbPresenter.currentModalDialog != null;
            if (descriptor.isModal() != isModalDialogOpened) {
                descriptor.setModal(isModalDialogOpened);
               // bugfix #27176, old dialog is disposed before recreating
               if (dialog != null) dialog.dispose ();
               // so we can safely send it to gc and recreate dialog
               // dialog = org.openide.DialogDisplayer.getDefault ().createDialog (descriptor);
            }*/
            // the dialog is not shown
            current = t;
            update ();
        } else {
            // add the exception to the queue
            next.setVisible (true);
            dialog.pack();
        }
        try {
            //Dialog.show() will pump events for the AWT thread.  If the 
            //exception happened because of a paint, it will trigger opening
            //another dialog, which will trigger another exception, endlessly.
            //Catch any exceptions and append them to the list instead.
            ensurePreferredSize();
            if (!dialog.isVisible()) {
                dialog.setVisible(true);
            }
            //throw new RuntimeException ("I am not so exceptional"); //uncomment to test
        } catch (Exception e) {
            exceptions.add(NbErrorManager.createExc(
                e, Level.SEVERE, null));
            next.setVisible(true);
        }
    }

    private void ensurePreferredSize() {
        if( null != lastBounds ) {
            return; //we remember the last window position
        } //we remember the last window position
        Dimension sz = dialog.getSize();
        Dimension pref = dialog.getPreferredSize();
        if (pref.height == 0) {
            pref.height = SIZE_PREFERRED_HEIGHT;
        }
        if (pref.width == 0) {
            pref.width = SIZE_PREFERRED_WIDTH;
        }
        if (!sz.equals(pref)) {
            dialog.setSize(pref.width, pref.height);
            dialog.validate();
            dialog.repaint();
        }
    }
    

    /** Updates the visual state of the dialog.
    */
    private void update () {
        // JST: this can be improved in future...
        boolean isLocalized = current.isLocalized();

        boolean repack;
        boolean visNext = next.isVisible();
        boolean visPrev = previous.isVisible();
        next.setVisible (exceptions.existsNextElement());
        previous.setVisible (exceptions.existsPreviousElement());
        repack = next.isVisible() != visNext || previous.isVisible() != visPrev;

        if (showDetails) {
            Mnemonics.setLocalizedText(details, org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("CTL_Exception_Hide_Details"));
            details.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("ACSD_Exception_Hide_Details"));
        } else {
            Mnemonics.setLocalizedText(details, org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("CTL_Exception_Show_Details"));
            details.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("ACSD_Exception_Show_Details"));
        }

        //    setText (current.getLocalizedMessage ());
        String title = org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("CTL_Title_Exception");

        if (showDetails) {
            descriptor.setMessage (this);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // XXX #28191: some other piece of code should underline these, etc.
                        StringWriter wr = new StringWriter();
                        current.printStackTrace(new PrintWriter(wr, true));
                        output.setText(wr.toString());
                        output.getCaret().setDot(0);
                        if (!AUTO_FOCUS && FocusManager.getCurrentManager().getActiveWindow() == null) {
                            // Do not steal focus if no Java window have it
                            output.requestFocusInWindow();
                        } else {
                            output.requestFocus ();
                        }
                }
            });
        } else {
            if (isLocalized) {
                String msg = current.getLocalizedMessage ();
                if (msg != null) {
                    descriptor.setMessage (msg);
                }
            } else {
                ResourceBundle curBundle = NbBundle.getBundle (NotifyExcPanel.class);
                String message;
                if (current.getSeverity() == Level.WARNING) {
                    // less scary message for warning level
                    message = MessageFormat.format(
                        curBundle.getString("NTF_ExceptionWarning"),
                        new Object[] { current.getClassName() }
                    );
                    title = curBundle.getString("NTF_ExceptionWarningTitle"); // NOI18N
                } else {
                    message = MessageFormat.format(
                        curBundle.getString("NTF_ExceptionalException"),
                        new Object[] { current.getClassName(), Paths.get(CLIOptions.getLogDir()).toUri() }
                    );
                    title = curBundle.getString("NTF_ExceptionalExceptionTitle"); // NOI18N
                }
                JTextPane pane = new JTextPane();
                pane.setContentType("text/html"); // NOI18N
                pane.setText(message);
                pane.setBackground(UIManager.getColor("Label.background")); // NOI18N
                pane.setBorder(BorderFactory.createEmptyBorder());
                pane.setEditable(false);
                pane.setFocusable(true);
                pane.addHyperlinkListener((e) -> {
                    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (IOException | URISyntaxException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                JScrollPane sp = new JScrollPane(pane);
                sp.setBorder(BorderFactory.createEmptyBorder());
                sp.setPreferredSize(new Dimension(300, 120));
                descriptor.setMessage(sp);
            }
        }

        descriptor.setTitle (title);
        if (repack) {
            dialog.pack();
        }
       
    }

    //
    // Handlers
    //

    public void actionPerformed(final java.awt.event.ActionEvent ev) {
        Object source = ev.getSource();
        if (source == next && exceptions.setNextElement() || source == previous && exceptions.setPreviousElement()) {
            current = exceptions.get();
            LogRecord rec = new LogRecord(Level.CONFIG, "NotifyExcPanel: " + ev.getActionCommand());// NOI18N
            String message = current.getMessage();
            String className = current.getClassName();
            if (message != null){
                className = className+": "+ message;
            }
            Object[] params = {className, current.getFirstStacktraceLine()}; // NOI18N
            rec.setParameters(params);
            //log changes in NotifyPanel - #119632
            Logger.getLogger("org.netbeans.ui.NotifyExcPanel").log(rec);// NOI18N
            update ();
            // bugfix #27266, don't change the dialog's size when jumping Next<->Previous
            //ensurePreferredSize();
            return;
        }

        if (source == details) {
            showDetails = !showDetails;
            lastBounds = null;
            try {
                update ();
                ensurePreferredSize();
                //throw new RuntimeException ("I am reallly exceptional!"); //uncomment to test
            } catch (Exception e) {
                //Do not allow an exception thrown here to trigger an endless
                //loop
                exceptions.add(NbErrorManager.createExc(e, //ugly but works
                    Level.SEVERE, null));
                next.setVisible(true);
            }
            return;
        }

        // bugfix #40834, remove all exceptions to notify when close a dialog
        if (source == NotifyDescriptor.OK_OPTION || source == NotifyDescriptor.CLOSED_OPTION || source == NotifyDescriptor.CANCEL_OPTION) {
            LogRecord rec = new LogRecord(Level.CONFIG, "NotifyExcPanel:  close");// NOI18N
            rec.setParameters(null);
            //log changes in NotifyPanel - dialog is closed - forget previous params
            Logger.getLogger("org.netbeans.ui.NotifyExcPanel").log(rec);// NOI18N
            try {
                if( null != exceptions )
                    exceptions.removeAll();
            //Fixed bug #9435, call of setVisible(false) replaced by call of dispose()
            //It did not work on Linux when JDialog is reused.
            //dialog.setVisible (false);
            // XXX(-ttran) no, it still doesn't work, getPreferredSize() on the
            // reused dialog returns (0,0).  We stop caching the dialog
            // completely by setting INSTANCE to null here.
                lastBounds = dialog.getBounds();
                dialog.dispose();
                exceptions = null;
                INSTANCE = null;
                //throw new RuntimeException ("You must be exceptional"); //uncomment to test
            } catch (RuntimeException e) {
                //Do not allow window of opportunity when dialog in a possibly
                //inconsistent state may be reuse
                exceptions = null;
                INSTANCE = null;
                throw e;
            } finally {
                exceptions = null;
                INSTANCE = null;
            }
        }
    }


    /** Method that checks whether the level is high enough to be notified
     * at all.
     * @param dialog shall we check for dialog or just a blinking icon (false)
     */
    private static boolean shallNotify(Level level, boolean dialog) {
        int minAlert = Integer.getInteger("netbeans.exception.alert.min.level", 900); // NOI18N
        int defReport = 1001;
        int minReport = Integer.getInteger("netbeans.exception.report.min.level", defReport); // NOI18N

        if (dialog) {
            return level.intValue() >= minReport;
        } else {
            return level.intValue() >= minAlert || level.intValue() >= minReport;
        }
    }
    
    static class ExceptionFlasher implements ActionListener {

        static ExceptionFlasher flash;

        private static synchronized ExceptionFlasher notify(String summary, ImageIcon icon) {
            if (flash == null) {
                flash = new ExceptionFlasher();
            } else {
                flash.timer.restart();
                if (flash.note != null) {
                    flash.note.clear();
                }
            }
            JComponent detailsPanel = getDetailsPanel(summary);
            JComponent bubblePanel = getDetailsPanel(summary);

            flash.note = NotificationDisplayer.getDefault().notify(
                    NbBundle.getMessage(NotifyExcPanel.class, "NTF_ExceptionalExceptionTitle"),
                    icon, bubblePanel, detailsPanel,
                    NotificationDisplayer.Priority.SILENT, NotificationDisplayer.Category.ERROR);
            return flash;
        }
        
        Notification note;
        private final Timer timer;

        public ExceptionFlasher() {
            timer = new Timer(300000, this);
            timer.setRepeats(false);
            timer.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == timer) {
                timeout();
                return;
            }
            synchronized (ExceptionFlasher.class) {
                if (note != null) {
                    note.clear();
                }
                flash = null;
            }
            if (null != exceptions && exceptions.size() > 0) {
                if (INSTANCE == null) {
                    INSTANCE = new NotifyExcPanel();
                }
                INSTANCE.updateState(exceptions.get(exceptions.size() - 1));
            }
        }
        
        private void timeout() {
            synchronized (ExceptionFlasher.class) {
                assert EventQueue.isDispatchThread();
                if( null != INSTANCE ) {
                    return;
                }
                if( null != exceptions ) {
                    exceptions.clear();
                }
                exceptions = null;
                flash = null;
                timer.stop();
                if( null != note ) {
                    note.clear();
                }
            }
        }

        private static JComponent getDetailsPanel(String summary) {
            JPanel details = new JPanel(new GridBagLayout());
            details.setOpaque(false);
            JLabel lblMessage = new JLabel(summary);
            
            JButton reportLink = new JButton("<html><a href=\"_blank\">" + NbBundle.getMessage(NotifyExcPanel.class, "NTF_ExceptionalExceptionReport")); //NOI18N
            reportLink.setFocusable(false);
            reportLink.setBorder(BorderFactory.createEmptyBorder());
            reportLink.setBorderPainted(false);
            reportLink.setFocusPainted(false);
            reportLink.setOpaque(false);
            reportLink.setContentAreaFilled(false);
            reportLink.addActionListener(flash);
            reportLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            details.add(reportLink, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 0, 3, 0), 0, 0));
            details.add(lblMessage, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 0, 3, 0), 0, 0));
            return details;
        }
    }

    static class ArrayListPos extends ArrayList<NbErrorManager.Exc> {
        static final long serialVersionUID = 2L;
        
        static final int SOFT_MAX_SIZE = 20;
        static final int HARD_MAX_SIZE = 100;   // To prevent from OOME when too many exceptions are thrown

        protected int position;

        protected ArrayListPos () {
            super();
            position=0;
        }

        @Override
        public boolean add(NbErrorManager.Exc e) {
            if (size() >= SOFT_MAX_SIZE && position < size() - 5) {
                set(size() - 1, e);
                return true;
            } else {
                if (size() >= HARD_MAX_SIZE) {
                    remove(5);  // it's beneficient to see the initial exceptions
                }
                return super.add(e);
            }
        }

        protected boolean existsElement () {
            return size()>0;
        }

        protected boolean existsNextElement () {
            return position+1<size();
        }

        protected boolean existsPreviousElement () {
            return position>0&&size()>0;
        }

        protected boolean setNextElement () {
            if(!existsNextElement()) {
                return false;
            }
            position++;
            return true;
        }

        protected boolean setPreviousElement () {
            if(!existsPreviousElement()) {
                return false;
            }
            position--;
            return true;
        }

        protected NbErrorManager.Exc get () {
            return existsElement()?get(position):null;
        }

        protected void removeAll () {
            clear();
            position=0;
        }
    }
}
