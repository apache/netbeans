/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/**
 * @author Ana von Klopp
 */

/*
 * TO DO FOR THIS CLASS:
 *
 * For PUT requests, the only option on the data panel should be to
 * upload a file.
 *
 * For POST requests, the user should be able to choose between
 * uploading a file or editing parameters.
 *
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.*;

import java.io.IOException;

import java.net.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;
import org.openide.awt.Mnemonics;


class EditPanel extends javax.swing.JPanel implements
    ActionListener, ChangeListener {

    // Code to get the displaying of the tabbed panels correct.
    //
    private int displayType = 0;
    private static final int DISPLAY_TYPE_QUERY   = 0;
    private static final int DISPLAY_TYPE_REQUEST = 1;
    private static final int DISPLAY_TYPE_COOKIES = 2;
    private static final int DISPLAY_TYPE_SERVER  = 3; 
    private static final int DISPLAY_TYPE_HEADERS = 4;

    private transient  Dimension tabD = new Dimension(450,280);

    private EditPanelQuery   queryPanel;
    private EditPanelRequest requestPanel;
    private EditPanelCookies cookiesPanel;
    private EditPanelServer  serverPanel;
    private EditPanelHeaders headersPanel;

    private MonitorData monitorData = null;
    
    // Do we need this to close it?
    private Dialog dialog = null; 
    private DialogDescriptor editDialog = null;
    
    private JButton sendButton;
    private JButton okButton;
    private JButton cancelButton; 

    /* These buttons were used for the feature that allows the user to
     * specify whether the browser's cookie should be used or whether
     * to replace it. In 3.6 ("Promotion B"), it is not
     * possible to configure the monitor to use user-specified
     * cookies, but I leave the method, in case it becomes possible in
     * the future. Basically, we can no longer set the cookie on the
     * server side (the Servlet APIs does not provide any method for
     * doing this) but we could technically tell the browser that
     * issues the replay request to send another cookie (the APIs for
     * that are not there now). If so, the feature can be
     * reintroduced. 
     * 
     * See also (PENDING) for other changes required to reintroduce
     * this feature. 
     */ 
    //private JToggleButton browserCookieButton, savedCookieButton; 
    //private static boolean useBrowserCookie = true;
    
    final static String METHOD = "method"; //NOI18N
    final static String GET = "GET";       //NOI18N
    final static String POST = "POST";     //NOI18N
    final static String PUT = "PUT";       //NOI18N

    private static EditPanel instance = null; 

    static void displayEditPanel(TransactionNode node) { 
	MonitorData md = null;	    
        // We retrieve the data from the file system, not from the 
        // cache
        md = Controller.getInstance().getMonitorData((TransactionNode)node, 
                                                     false,  // from file
                                                     false); // don't cache
        if (md == null) {
	    // We couldn't get the data. 
            String msg = NbBundle.getMessage(EditPanel.class, "MSG_NoMonitorData");
            Logger.getLogger("global").log(Level.INFO, msg);
	    return; 
	}

	if(md.getRequestData().getAttributeValue(METHOD).equals(POST)) 
	    Util.removeParametersFromQuery(md.getRequestData());

	md.getRequestData().deleteCookie("jsessionid"); 

	if(instance == null) instance = new EditPanel(); 
	
	//useBrowserCookie = MonitorAction.getController().getUseBrowserCookie();
	instance.showDialog(md); 
    } 

    static synchronized EditPanel getInstance() { 
	if(instance == null) instance = new EditPanel(); 
	return instance; 
    } 

    private EditPanel() {

	createDialogButtons();

	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	queryPanel   = new EditPanelQuery();
	requestPanel = new EditPanelRequest();
	cookiesPanel = new EditPanelCookies();
	serverPanel  = new EditPanelServer();
	headersPanel = new EditPanelHeaders();
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EditPanel.class,"ACS_MON_Replay_panel"));
	JTabbedPane tabs = new JTabbedPane();
        tabs.getAccessibleContext().setAccessibleName(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_Replay_tabsName"));
        tabs.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_Replay_tabsDesc"));

	tabs.setPreferredSize(tabD);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Query_Panel_Tab"),   queryPanel);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Request_Panel_Tab"),
		    requestPanel);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Cookies_Panel_Tab"), cookiesPanel);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Server_Panel_Tab"),  serverPanel);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Headers_Panel_Tab"), headersPanel);
	tabs.addChangeListener(this);

	this.add(tabs);
	this.add(Box.createGlue());
	this.add(Box.createVerticalStrut(5));
	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
    }

    void showDialog(MonitorData md) {

	this.monitorData = md; 

	queryPanel.setData(monitorData);
	requestPanel.setData(monitorData);
	cookiesPanel.setData(monitorData);
	serverPanel.setData(monitorData);
	headersPanel.setData(monitorData);

	Object[] options = {
	    //createSessionButtonPanel(),
	    sendButton,
	    cancelButton,
	};
	
	editDialog = new DialogDescriptor(this, 
					  NbBundle.getBundle(EditPanel.class).getString("MON_EditReplay_panel"),
					  false, 
					  options,
					  options[0],
					  DialogDescriptor.BOTTOM_ALIGN,
					  new HelpCtx("monitor_resend"), //NOI18N
					  this);
        
	dialog = DialogDisplayer.getDefault().createDialog(editDialog);
	dialog.pack();
	dialog.setVisible(true);
    }
    

    /**
     * Handle user input...
     */

    public void actionPerformed(ActionEvent e) {
	
	String str = new String();
        Object value = editDialog.getValue();
        if (value == null)
            return;
        if (value instanceof JButton)
            str = ((JButton)value).getText();
        else
            str = value.toString();
	if(str.equals(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_SendA11yDesc"))) {
	 
	    String method =
		monitorData.getRequestData().getAttributeValue(METHOD); 

	    if(method.equals(GET)) 
		Util.composeQueryString(monitorData.getRequestData());

	    try {
		MonitorAction.getController().replayTransaction(monitorData);
		dialog.dispose();
	    }
	    catch(UnknownHostException uhe) {
		// Notify the user that there is no host

		Object[] options = {
                    okButton
//		    NbBundle.getBundle(EditPanel.class).getString("MON_OK"),
		};

		NotifyDescriptor noServerDialog = 
		    new NotifyDescriptor
			(NbBundle.getMessage(EditPanel.class, "MON_Exec_server_wrong", monitorData.getServerName()),
			 NbBundle.getBundle(EditPanel.class).getString("MON_Exec_server"),
			 NotifyDescriptor.DEFAULT_OPTION,
			 NotifyDescriptor.INFORMATION_MESSAGE,
			 options,
			 options[0]);
		DialogDisplayer.getDefault().notify(noServerDialog);
		displayType = DISPLAY_TYPE_SERVER;
		showData();
	    }
	    catch(IOException ioe) {
		// Notify the user that the server is not running
		Object[] options = {
		    NbBundle.getBundle(EditPanel.class).getString("MON_OK"),
		};

		Object[] args = {
		    monitorData.getServerName(), 
		    monitorData.getServerPortAsString(), 
		};

		NotifyDescriptor noServerDialog = 
		    new NotifyDescriptor
			(NbBundle.getMessage(EditPanel.class, "MON_Exec_server_start", args),
			 NbBundle.getBundle(EditPanel.class).getString("MON_Exec_server"),
			 NotifyDescriptor.DEFAULT_OPTION,
			 NotifyDescriptor.INFORMATION_MESSAGE,
			 options,
			 options[0]);
		DialogDisplayer.getDefault().notify(noServerDialog);
	    }
	}
	else if(str.equals(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_CancelA11yDesc")))
	    dialog.dispose();
    }

    /**
     * Listens to events from the tab pane, displays different
     * categories of data accordingly. 
     */
    public void stateChanged(ChangeEvent e) {
	JTabbedPane p = (JTabbedPane)e.getSource();
	displayType = p.getSelectedIndex();

	showData();
    }
    

    void showData() {

	if (displayType == DISPLAY_TYPE_QUERY)
	    queryPanel.setData(monitorData);
	else if (displayType == DISPLAY_TYPE_REQUEST)
	    requestPanel.setData(monitorData);
	else if (displayType == DISPLAY_TYPE_COOKIES)
	    cookiesPanel.setData(monitorData);
	else if (displayType == DISPLAY_TYPE_SERVER)
	    serverPanel.setData(monitorData);
	else if (displayType == DISPLAY_TYPE_HEADERS)
	    headersPanel.setData(monitorData);
    }


    private void createDialogButtons() {

	// Button used by the dialog descriptor
	sendButton = new JButton();
        Mnemonics.setLocalizedText(sendButton, NbBundle.getBundle(EditPanel.class).getString("MON_Send"));
	sendButton.setToolTipText(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_SendA11yDesc"));

	okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getBundle(EditPanel.class).getString("MON_OK"));
	okButton.setToolTipText(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_OKA11yDesc"));

	cancelButton = new JButton(NbBundle.getBundle(EditPanel.class).getString("MON_Cancel"));
        Mnemonics.setLocalizedText(cancelButton, NbBundle.getBundle(EditPanel.class).getString("MON_Cancel"));
	cancelButton.setToolTipText(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_CancelA11yDesc"));
    }
} // EditPanel
