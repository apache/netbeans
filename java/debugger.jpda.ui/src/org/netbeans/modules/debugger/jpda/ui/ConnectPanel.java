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

package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.*;

import com.sun.jdi.connect.Connector.Argument;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.debugger.ui.PersistentController;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Panel for entering parameters of attaching to a remote VM.
 * If the debugger offers more
 * <A HREF="http://java.sun.com/j2se/1.3/docs/guide/jpda/jdi/com/sun/jdi/connect/connector.html">connectors</A>
 * then the panel contains also a combo-box for selecting a connector.
 *
 * @author Jan Jancura
 */
public class ConnectPanel extends JPanel implements ActionListener, HelpCtx.Provider {

    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.debugger"); // NOI18N

    /** List of all AttachingConnectors.*/
    private final List<Connector>   connectors;
    /** Combo with list of all AttachingConnector names.*/
    private JComboBox               cbConnectors;
    private Connector               selectedConnector;
    /** List of JTextFields containing all parameters of curentConnector. */
    private JTextField[]            tfParams;
    private ConnectController       controller;
    private final DocumentListener  validityDocumentListener = new ValidityDocumentListener();
    private final Cursor            standardCursor;
    private final AtomicBoolean     connectorsLoaded = new AtomicBoolean(false);
    private static final RequestProcessor RP = new RequestProcessor(ConnectPanel.class.getName());


    public ConnectPanel () {
        connectors = new ArrayList<Connector>();
        standardCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        RP.post(new Runnable() {
            @Override
            public void run() {
                initConnectors();
            }
        });
        
        controller = new ConnectController();
        controller.setValid(false);
    }

    public Controller getController() {
        return controller;
    }
    
    private void initConnectors() {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager ();
        connectors.addAll (vmm.attachingConnectors ());
        connectors.addAll (vmm.listeningConnectors ());
           
        // We temporary do not support these three connectors
        // use --cp:a ${JDK_HOME}/lib/sa-jdi.jar to activate them if you uncomment this
        for (Iterator<Connector> ci = connectors.iterator(); ci.hasNext(); ) {
            String name = ci.next().name();
            int index = name.lastIndexOf('.');
            if (index >= 0) {
                name = name.substring(index + 1);
            }
            if (name.equalsIgnoreCase("SACoreAttachingConnector") || 
                name.equalsIgnoreCase("SAPIDAttachingConnector") ||
                name.equalsIgnoreCase("SADebugServerAttachingConnector")) {
                ci.remove();
            }
        }
        
        int defaultIndex = 0;
        String lacn = Properties.getDefault ().getProperties ("debugger").
            getString ("last_attaching_connector", "");
        int i, k = connectors.size ();
        for (i = 0; i < k; i++) {
            Connector connector = connectors.get (i);
            if ((lacn != null) && connector.name ().equals (lacn)) {
                defaultIndex = i;
            }
        }
        final int finalDefaultIndex = defaultIndex;
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    addConnectors(finalDefaultIndex);
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void waitForConnectorsLoad() {
        synchronized (connectorsLoaded) {
            while (!connectorsLoaded.get()) {
                try {
                    connectorsLoaded.wait();
                } catch (InterruptedException ex) {}
            }
        }
    }
    
    private void addConnectors(int defaultIndex) {
        //assert connectorsLoaded.get();
        if (connectors.isEmpty()) {
            // no attaching connectors available => print message only
            add (new JLabel (
                NbBundle.getMessage (ConnectPanel.class, "CTL_No_Connector")
            ));
            return;
        }
        if (connectors.size () > 1) {
            // more than one attaching connector available => 
            // init cbConnectors & selext default connector
            
            cbConnectors = new JComboBox ();
            cbConnectors.getAccessibleContext ().setAccessibleDescription (
                NbBundle.getMessage (ConnectPanel.class, "ACSD_CTL_Connector")
            );
            int i, k = connectors.size ();
            for (i = 0; i < k; i++) {
                Connector connector = connectors.get (i);
                int jj = connector.name ().lastIndexOf ('.');
                              
                String s = (jj < 0) ? 
                    connector.name () : 
                    connector.name ().substring (jj + 1);
                cbConnectors.addItem (
                    s + " (" + connector.description () + ")"
                );
            }
            cbConnectors.setActionCommand ("SwitchMe!");
            cbConnectors.addActionListener (this);
        }
        cbConnectors.setSelectedIndex (defaultIndex);
        selectedConnector = connectors.get(defaultIndex);
        setCursor(standardCursor);
        
        synchronized (connectorsLoaded) {
            connectorsLoaded.set(true);
            connectorsLoaded.notifyAll();
        }
    }

    /**
     * Adds options for a selected connector type to this panel.
     */
    private void refresh (int index, Properties properties) {
        assert SwingUtilities.isEventDispatchThread();
        removeAll();
        
        Connector connector = connectors.get (index);
        selectedConnector = connector;

        GridBagConstraints c;
        GridBagLayout layout = new GridBagLayout ();
        setLayout (layout);
        
        if (cbConnectors != null) { 
            // more than oneconnection => first line contains connector 
            // selector
                c = new GridBagConstraints ();
                c.insets = new Insets (0, 0, 3, 3);
                c.anchor = GridBagConstraints.WEST;
                JLabel lblConnectors = new JLabel();
                Mnemonics.setLocalizedText(
                        lblConnectors,
                        NbBundle.getMessage (ConnectPanel.class, "CTL_Connector") // NOI18N
                );
                lblConnectors.getAccessibleContext ().setAccessibleDescription (
                    NbBundle.getMessage (ConnectPanel.class, "ACSD_CTL_Connector")
                );
                lblConnectors.setLabelFor (cbConnectors);
                layout.setConstraints (lblConnectors, c);
            add (lblConnectors);
                c.insets = new Insets (0, 3, 3, 0);
                c.weightx = 1.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridwidth = GridBagConstraints.REMAINDER;
                layout.setConstraints (cbConnectors, c);
            add (cbConnectors);
        }
        
        // second line => transport
            c = new GridBagConstraints ();
            c.insets = new Insets (3, 0, 0, 6);
            c.anchor = GridBagConstraints.WEST;
            JLabel lblTransport = new JLabel();
            Mnemonics.setLocalizedText(
                    lblTransport,
                    NbBundle.getMessage (ConnectPanel.class, "CTL_Transport") // NOI18N
            );
            lblTransport.getAccessibleContext ().setAccessibleDescription (
                NbBundle.getMessage (ConnectPanel.class, "ACSD_CTL_Transport")
            );
            layout.setConstraints (lblTransport, c);
        add (lblTransport);
            final JTextField tfTransport = new JTextField ();
            tfTransport.setEditable (false);
            lblTransport.setLabelFor (tfTransport);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets (3, 3, 0, 0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            layout.setConstraints (tfTransport, c);
            Transport t = connector.transport();
            tfTransport.setText (t != null ? t.name() : "");
            tfTransport.getAccessibleContext ().setAccessibleDescription (
                NbBundle.getMessage (ConnectPanel.class, "ACSD_CTL_Transport")
            );
            tfTransport.addFocusListener (new FocusAdapter () {
                @Override
                public void focusGained (FocusEvent evt) {
                    tfTransport.selectAll ();
                }
            });
        add (tfTransport);
        
        // other lines
        Map<String, Argument> args = getSavedArgs (connector, properties);
        tfParams = new JTextField [args.size ()];
        Iterator<String> it = new TreeSet<String>(args.keySet ()).iterator ();
        int i = 0;
        while (it.hasNext ()) {
            String name = it.next ();
            Argument a = args.get (name);
            String label = translate (a.name());
            if (label == null) {
                label = "&"+a.label();
            }
                c = new GridBagConstraints ();
                c.insets = new Insets (6, 0, 0, 3);
                c.anchor = GridBagConstraints.WEST;
                JLabel iLabel = new JLabel();// (label);
                Mnemonics.setLocalizedText(iLabel, label);
                iLabel.setToolTipText (a.description ());
            add (iLabel, c);
                JTextField tfParam = new JTextField (a.value ());
                iLabel.setLabelFor (tfParam);
                tfParam.setName (name);
                tfParam.getAccessibleContext ().setAccessibleDescription (
                    new MessageFormat (NbBundle.getMessage (
                        ConnectPanel.class, "ACSD_CTL_Argument"
                    )).format (new Object[] { label })
                ); 
                tfParam.setToolTipText (a.description ());
                c = new GridBagConstraints ();
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.insets = new Insets (6, 3, 0, 0);
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0;
            add (tfParam, c);
            tfParam.getDocument().addDocumentListener(validityDocumentListener);
            tfParams [i ++] = tfParam;
        }
        
        //
        // Create an empty panel that resizes vertically so that
        // other elements have fix height:
            c = new GridBagConstraints ();
            c.weighty = 1.0;
            JPanel p = new JPanel ();
            p.setPreferredSize (new Dimension (1, 1));
        add (p, c);
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                checkValid();
            }
        });
    }

    /**
     * Refreshes panel with options corresponding to the selected connector type.
     * This method is called when a user selects new connector type.
     */
    @Override
    public void actionPerformed (ActionEvent e) {
        int selectedIndex = ((JComboBox) e.getSource ()).getSelectedIndex ();
        refresh (selectedIndex, Properties.getDefault ().getProperties ("debugger"));
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.pack ();  // ugly hack...
        }
    }
    
    private static void log(Connector c, Map<String, Argument> args) {
        LogRecord record = new LogRecord(Level.INFO, "USG_DEBUG_ATTACH_JPDA");
        record.setResourceBundle(NbBundle.getBundle(ConnectPanel.class));
        record.setResourceBundleName(ConnectPanel.class.getPackage().getName() + ".Bundle"); // NOI18N
        record.setLoggerName(USG_LOGGER.getName());
        List<Object> params = new ArrayList<Object>();
        params.add(c.name());
        StringBuilder arguments = new StringBuilder();
        for (Map.Entry argEntry : args.entrySet()) {
            //arguments.append(argEntry.getKey());
            //arguments.append("=");
            arguments.append(argEntry.getValue());
            arguments.append(", ");
        }
        if (arguments.length() > 2) {
            arguments.delete(arguments.length() - 2, arguments.length());
        }
        params.add(arguments);
        record.setParameters(params.toArray(new Object[0]));
        USG_LOGGER.log(record);
    }
    
    
    // private helper methods ..................................................
    
    /**
     * Is host name unknown? This method resolves if the specified
     * string means &quot;hostname&nbsp;unknown&quot;.
     * Host name is considered unknown if:
     * <UL>
     *     <LI>the hostname is <TT>null</TT>
     *     <LI>the hostname is an empty string
     *     <LI>the hostname starts with &quot;<I>x</I><TT>none</TT><I>x</I>&quot;
     *         or &quot;<I>x</I><TT>unknown</TT><I>x</I>&quot;, where <I>x</I>
     *         is any character except <TT>'a'-'z'</TT>, <TT>'A'-'Z'</TT>,
     *         <TT>'-'</TT>.
     * </UL>
     *
     * @param  hostname  host name to be resolved
     * @return  <TT>true</TT> if the host name is considered as unknown,
     *          <TT>false</TT> otherwise
     */
    private static boolean isUnknownHost (String hostname) {
        if (hostname == null) {
            return true;
        }
        int length = hostname.length();
        if (length == 0) {
            return true;
        }
        if (length < 6) {
            return false;
        }
        char firstChar = hostname.charAt(0);
        if (('a' <= firstChar && firstChar <= 'z')
            || ('A' <= firstChar && firstChar <= 'Z')
            || (firstChar == '-')) {
                return false;
        }
        char c;
        c = hostname.charAt(5);
        if (c == firstChar) {
            return hostname.substring(1, 5).equalsIgnoreCase("none");   //NOI18N
        }
        if (length < 9) {
            return false;
        }
        c = hostname.charAt(8);
        if (c == firstChar) {
            return hostname.substring(1, 8).equalsIgnoreCase("unknown");    //NOI18N
        }
        return false;
    }

//    private static String getLastAttachingConnectorName () {
//        JPDADebuggerProjectSettings settings = (JPDADebuggerProjectSettings) 
//            JPDADebuggerProjectSettings.findObject (
//                JPDADebuggerProjectSettings.class,
//                true
//            );
//        return settings.getLastConnector ();
//    }
    
    private static Map<String, Argument> getSavedArgs (Connector connector, Properties properties) {
        // 1) get default set of args
        Map<String, Argument> args = connector.defaultArguments ();

        // 2) load saved version of args
        Map savedArgs = properties.getMap ("connection_settings", new HashMap ());
        savedArgs = (Map) savedArgs.get (connector.name ());
        if (savedArgs == null) {
            return args;
        }
        
        // 3) refres default args about saved values
        Iterator<String> i = args.keySet ().iterator ();
        while (i.hasNext()) {
            String argName = i.next ();
            String savedValue = (String) savedArgs.get (argName);
            if (savedValue != null) {
                args.get(argName).setValue(savedValue);
            }
        }
        return args;
    }

    private static Map<String, Argument> getEditedArgs (
        JTextField[]        tfParams, 
        Connector           connector
    ) {
        assert SwingUtilities.isEventDispatchThread();
        // 1) get default set of args
        Map<String, Argument> args = connector.defaultArguments ();

        // 2) update values from text fields
        int i, k = tfParams.length;
        for (i = 0; i < k; i++) {
            JTextField tf = tfParams [i];
            String paramName = tf.getName ();
            String paramValue = tf.getText ();
            Argument a = args.get (paramName);
            while ( ((!a.isValid (paramValue)) && (!"".equals (paramValue))) ||
                    ( "".equals (paramValue) && a.mustSpecify () )
            ) {
                NotifyDescriptor.InputLine in;
                String label = getLabel(a);
                if ( "".equals (paramValue) && a.mustSpecify ()) {
                    in = new NotifyDescriptor.InputLine (
                        label,
                        NbBundle.getMessage (
                            ConnectPanel.class, 
                            "CTL_Required_value_title"
                        )
                    );
                } else {
                    in = new NotifyDescriptor.InputLine (
                        label,
                        NbBundle.getMessage (
                            ConnectPanel.class, 
                            "CTL_Invalid_value_title"
                        )
                    );
                }
                if (DialogDisplayer.getDefault ().notify (in) == 
                    NotifyDescriptor.CANCEL_OPTION
                ) {
                    return null;
                }
                paramValue = in.getInputText ();
            }
            a.setValue (paramValue);
        }
        
        return args;
    }
    
    private static void saveArgs (
        Map<String, Argument> args,
        Connector             connector
    ) {
        Map<String, Argument> defaultValues = connector.defaultArguments ();
        Map<String, String> argsToSave = new HashMap<String, String>();
        for(Map.Entry<String, Argument> entry : args.entrySet ()) {
            String argName = entry.getKey ();
            Argument value = entry.getValue();
            Argument defaultValue = defaultValues.get (argName);
            if ( value != null &&
                 value != defaultValue &&
                 !value.equals (defaultValue)
            ) {
                argsToSave.put (argName, value.value ());
            }
        }

        Map m = Properties.getDefault ().getProperties ("debugger").
            getMap ("connection_settings", new HashMap ());
        String name = connector.name ();
        m.put (name, argsToSave);
        Properties.getDefault ().getProperties ("debugger").
                setString ("last_attaching_connector", name);
        Properties.getDefault ().getProperties ("debugger").
                setMap ("connection_settings", m);
    }

    private static String getLabel(Argument a) {
        String label = translate (a.name());
        if (label == null) {
            label = a.label();
        } else {
            int amp = Mnemonics.findMnemonicAmpersand(label);
            if (amp >= 0) {
                label = label.substring(0, amp) + label.substring(amp + 1);
            }
        }
        return label;
    }
    
    private static String translate (String str) {
        try {
            return NbBundle.getMessage(ConnectPanel.class, "CTL_CA_"+str);
        } catch (MissingResourceException mrex) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Missing resource "+"CTL_CA_"+str+" from "+ConnectPanel.class.getName());
            return null;
        }
    }

    private void checkValid() {
        assert connectorsLoaded.get();
        assert SwingUtilities.isEventDispatchThread() : "Called outside of AWT.";
        int i, k = tfParams.length;
        Map args = selectedConnector.defaultArguments ();
        for (i = 0; i < k; i++) {
            JTextField tf = tfParams [i];
            String paramName = tf.getName ();
            String paramValue = tf.getText ();
            Argument a = (Argument) args.get(paramName);
            if (a.mustSpecify() && "".equals(paramValue)) {
                String msg = NbBundle.getMessage(ConnectPanel.class,
                                                 "MSG_Required_value",
                                                 getLabel(a));
                controller.setInformationMessage(msg);
                controller.setValid(false);
                break;
            }
            if (!"".equals(paramValue) && !a.isValid(paramValue)) {
                String msg = NbBundle.getMessage(ConnectPanel.class,
                                                 "MSG_Invalid_value",
                                                 getLabel(a));
                controller.setErrorMessage(msg);
                controller.setValid(false);
                break;
            }
        }
        if (i >= k) {
            controller.setErrorMessage(null);
            controller.setValid(true);
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("debug.jpda.attach");
    }

    private class ValidityDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            checkValid();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            checkValid();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            checkValid();
        }
    }

    public class ConnectController implements PersistentController {

        PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private boolean valid = true;

        @Override
        public boolean cancel () {
            return true;
        }

        @Override
        public boolean ok () {
            assert connectorsLoaded.get();
            assert SwingUtilities.isEventDispatchThread() : "Called outside of AWT.";
            final Connector connector = selectedConnector;
            final Map<String, Argument> args = getEditedArgs (tfParams, connector);
            if (args == null) {
                return true;
            } // CANCEL
            saveArgs (args, connector);
            log(connector, args);

            // Take the start off the AWT EQ:
            final RequestProcessor.Task[] startTaskPtr = new RequestProcessor.Task[1];
            startTaskPtr[0] = new RequestProcessor("JPDA Debugger Starting").create(new Runnable() {
                @Override
                public void run() {
                    final Thread theCurrentThread = Thread.currentThread();
                    ProgressHandle progress = ProgressHandle.createHandle(
                            NbBundle.getMessage(ConnectPanel.class, "CTL_connectProgress"),
                            new Cancellable() {
                                @Override
                                public boolean cancel() {
                                    theCurrentThread.interrupt();
                                    return startTaskPtr[0].isFinished();
                                }
                    });
                    try {
                        //System.out.println("Before progress.start()");
                        progress.start();
                        //System.out.println("After progress.start()");
                        DebuggerEngine[] es = null;
                        if (connector instanceof AttachingConnector) {
                            es = DebuggerManager.getDebuggerManager ().startDebugging (
                                DebuggerInfo.create (
                                    AttachingDICookie.ID,
                                    new Object [] {
                                        AttachingDICookie.create (
                                            (AttachingConnector) connector,
                                            args
                                        )
                                    }
                                )
                            );
                        } else
                        if (connector instanceof ListeningConnector) {
                            es = DebuggerManager.getDebuggerManager ().startDebugging (
                                DebuggerInfo.create (
                                    ListeningDICookie.ID,
                                    new Object [] {
                                        ListeningDICookie.create (
                                            (ListeningConnector) connector,
                                            args
                                        )
                                    }
                                )
                            );
                        }
                        if (es != null) {
                            for (int i = 0; i < es.length; i++) {
                                JPDADebugger d = es[i].lookupFirst(null, JPDADebugger.class);
                                if (d == null) {
                                    continue;
                                }
                                try {
                                    // workaround for #64227
                                    if (d.getState() != JPDADebugger.STATE_RUNNING) {
                                        d.waitRunning ();
                                    }
                                } catch (DebuggerStartException dsex) {
                                    //ErrorManager.getDefault().notify(ErrorManager.USER, dsex);
                                    // Not necessary to notify - message written to debugger console.
                                }
                            }
                        }
                    } finally {
                        //System.out.println("Before progress.finish()");
                        progress.finish();
                        //System.out.println("After progress.finish()");
                    }
                }
            });
            startTaskPtr[0].schedule(0);
//            Runnable action = new Runnable() {
//
//                public void run() {
//                    startTaskPtr[0].schedule(0);
//                }
//            };
//            ScanDialog.runWhenScanFinished(action, NbBundle.getMessage (ConnectPanel.class, "CTL_Connect"));   //NOI18N
            //System.out.println("Before return from ConnectPanel.ok()");
            return true;
        }

        @Override
        public boolean load(final Properties props) {
            assert !SwingUtilities.isEventDispatchThread();
            waitForConnectorsLoad();
            String connectorName = props.getString ("attaching_connector", "");
            int index, k = connectors.size ();
            int indexToSelect = -1;
            for (index = 0; index < k; index++) {
                Connector connector = connectors.get (index);
                if (connector.name ().equals (connectorName)) {
                    indexToSelect = index;
                    break;
                }
            }
            if (indexToSelect < 0) {
                return false;
            }
            final int si = indexToSelect;
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        cbConnectors.setSelectedIndex(si);
                        refresh(si, props);
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return true;
        }

        @Override
        public void save(Properties props) {
            assert connectorsLoaded.get();
            final Connector[] connectorPtr = new Connector[] { null };
            final Map[] argsPtr = new Map[] { null };
            if (SwingUtilities.isEventDispatchThread()) {
                connectorPtr[0] = selectedConnector;
                argsPtr[0] = getEditedArgs (tfParams, selectedConnector);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            connectorPtr[0] = selectedConnector;
                            argsPtr[0] = getEditedArgs (tfParams, selectedConnector);
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    return ;
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                    return ;
                }
            }
            final Connector connector = connectorPtr[0];
            final Map<String, Argument> args = argsPtr[0];
            if (args == null) {
                return;  // nothing stored
            }
            Map<String, Argument> defaultValues = connector.defaultArguments ();
            Map<String, String> argsToSave = new HashMap<String, String>();
            Iterator<String> i = args.keySet ().iterator ();
            while (i.hasNext()) {
                String argName = i.next ();
                Argument value = args.get (argName);
                Argument defaultValue = defaultValues.get (argName);
                if (value != null && value != defaultValue && !value.equals (defaultValue)) {
                    argsToSave.put (argName, value.value ());
                } // if
            } // while

            Map m = new HashMap();
            String name = connector.name ();
            m.put (name, argsToSave);
            props.setMap ("connection_settings", m);
            props.setString ("attaching_connector", connector.name());
        }

        @Override
        public String getDisplayName() {
            assert connectorsLoaded.get();
            final Connector connector = selectedConnector;
            final Map<String, Argument> args = getEditedArgs (tfParams, connector);
            if (args == null) {
                return "";  // NOI18N
            }
            if (connector instanceof AttachingConnector) {
                AttachingDICookie c = AttachingDICookie.create ((AttachingConnector) connector, args);
                if (c.getHostName () != null) {
                    return new MessageFormat (NbBundle.getMessage(ConnectPanel.class, "CTL_Attach_to_socket")).format(
                        new Object[] {c.getHostName(), String.valueOf(c.getPortNumber())});
                } else if (c.getSharedMemoryName() != null) {
                    return new MessageFormat (NbBundle.getMessage(ConnectPanel.class, "CTL_Attach_to_shmem")).format(
                        new Object[] {c.getSharedMemoryName()});
                } else if (c.getArgs().get("pid") != null) {
                    return new MessageFormat (NbBundle.getMessage(ConnectPanel.class, "CTL_Attach_to_pid")).format(
                        new Object[] {c.getArgs().get("pid").toString()});
                }
            }
            if (connector instanceof ListeningConnector) {
                ListeningDICookie c = ListeningDICookie.create((ListeningConnector) connector, args);
                if (c.getSharedMemoryName() != null) {
                    return new MessageFormat (NbBundle.getMessage(ConnectPanel.class, "CTL_Listen_on_shmem")).format(
                        new Object[] {c.getSharedMemoryName()});
                } else if (c.getPortNumber() > 0) {
                    return new MessageFormat (NbBundle.getMessage(ConnectPanel.class, "CTL_Listen_on_socket")).format(
                        new Object[] {String.valueOf(c.getPortNumber ())});
                }
            }
            return ""; // NOI18N
        }


        /**
         * Return <code>true</code> whether value of this customizer
         * is valid (and OK button can be enabled).
         *
         * @return <code>true</code> whether value of this customizer
         * is valid
         */
        @Override
        public boolean isValid () {
            return valid;
        }

        void setValid(boolean valid) {
            this.valid = valid;
            firePropertyChange(PROP_VALID, !valid, valid);
        }

        void setErrorMessage(String msg) {
            firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, msg);
        }

        void setInformationMessage(String msg) {
            firePropertyChange(NotifyDescriptor.PROP_INFO_NOTIFICATION, null, msg);
        }

        private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            pcs.firePropertyChange(propertyName, oldValue, newValue);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
    }
    
}

