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

package org.netbeans.modules.cnd.debugger.common2.capture;

import java.util.prefs.Preferences;
import javax.swing.JSeparator;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.Action;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.openide.util.actions.Presenter;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.SharedClassObject;

import org.openide.awt.Actions;
import org.openide.util.NbPreferences;

import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.MasterView;
import org.netbeans.modules.cnd.debugger.common2.utils.InfoPanel;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CustomizableHostList;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CndRemote;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.EditHostListAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CustomizableHost;

public class CaptureListenAction extends SystemAction implements Presenter.Menu  {

    @Override
    public String getName () {
	return Catalog.get("LBL_ListenToCaptures"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(CaptureListenAction.class); // FIXUP ???
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public JMenuItem getMenuPresenter() {
	JMenu mi = new JMenu();
	Actions.connect(mi, (Action)this, false);
	mi.addMenuListener(new MainItemListener());
	return mi;
    }

    private class MainItemListener implements MenuListener {
        @Override
	public void menuCanceled(MenuEvent e) {
	}

        @Override
        public void menuDeselected(MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
            menu.removeAll();
        }

        @Override
        public void menuSelected (MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
	    String [] hostChoices = null;
	    String hostName = null;
	    CustomizableHostList hostlist = null;
	    hostlist = CustomizableHostList.getInstance();

	    if (NativeDebuggerManager.isStandalone()) {
		hostChoices = hostlist.getRecordsName();
	    } else {
                hostChoices = CndRemote.getServerListIDs();
	    }
	    if ( hostChoices.length > 0 ) {
		for (int i = 0; i < hostChoices.length ; i++) {
		    if (i >= MasterView.MAX_VISIBLE_IN_MENU)
			break;

		    String hostDispName = hostName = hostChoices[i];
		    if (NativeDebuggerManager.isStandalone()) {
			CustomizableHost host = hostlist.getHostByName(hostName);
			hostDispName = host.displayName();
		    }

		    // 6798371
		    ExternalStart xstart = ExternalStartManager.getXstart(hostName);
		    boolean allowed;
		    if (xstart == null) {
			allowed = false;
		    } else {
			allowed = xstart.isRunning();
		    }

		    // don't need to create xstart yet if xstart is null
		    // delay it until ss_attach is enabled 
		    // (PopupItemTarget.actionPerformed)
		    menu.add(new PopupItemTarget(xstart, hostName, hostDispName, allowed ));
		}
	    }

	    EditHostListAction editHostListAction = SharedClassObject.findObject(EditHostListAction.class, true);
	    if (editHostListAction != null) {
		editHostListAction.setEnabled( hostChoices.length > 0 );
		if (NativeDebuggerManager.isStandalone()) {
		    menu.add(new JSeparator());
		    menu.add(editHostListAction.getMenuPresenter());
		}
	    }
	}
    }

    protected class PopupItemTarget extends JCheckBoxMenuItem implements ActionListener {
	private ExternalStart xstart;
	private String hostName;

	public PopupItemTarget(ExternalStart xs, String hostName, String hostDispName, boolean allowed) {
	    super(hostDispName, allowed); 
	    this.xstart = xs; // could be null
	    this.hostName = hostName; // hostName is different between IDE and dbxtool
	    addActionListener(this);
	}
        
        @Override
	public void actionPerformed(ActionEvent e) {
	    final String hn = hostName;
	    CndRemote.validate(hn, new Runnable() {
                @Override
		public void run() {
		    toggleXstart();
		}
	    });
	}

    	private void toggleXstart() {
	    if (!ExternalStartManager.isSupported()) {
		DialogDisplayer.getDefault().notify(
			new NotifyDescriptor.Message(
			Catalog.get("MSG_ss_attach_not_supported"),	// NOI18N
			NotifyDescriptor.ERROR_MESSAGE));
		return;
	    }

	    Host host = Host.byName(hostName);

	    if (xstart == null) {
		xstart = ExternalStartManager.createExternalStart(host);
		ExternalStartManager.addXstart(host, xstart);
	    }

	    // toggle ss_attach enable
	    if (xstart.isRunning())
		xstart.stop();
	    else {
		if (!isDoNotShowAgain()) {
		    String msg;
		    if (NativeDebuggerManager.isStandalone())
			msg = Catalog.get("MSG_dbxtool_ss_attach_info"); // NOI18N
		    else
			msg = Catalog.get("MSG_ss_attach_info"); // NOI18N
		    InfoPanel panel = new InfoPanel(msg);
		    NotifyDescriptor dlg = new NotifyDescriptor.Confirmation(
			panel,
			CaptureListenAction.this.getName(),
			NotifyDescriptor.OK_CANCEL_OPTION);
		    Object answer = DialogDisplayer.getDefault().notify(dlg);
		    // ---------- we block here -------------

		    // remember whether to show dialog again
		    setDoNotShowAgain(panel.dontShowAgain());

		    if (answer == NotifyDescriptor.CANCEL_OPTION ||
			answer == NotifyDescriptor.CLOSED_OPTION) {
			return;
		    }
		}
		xstart.start();
	    }
	}
    }

    // settings persistence
    // Saved in:
    // config/Preferences/org/netbeans/modules/cnd/debugger/common2.properties

    private static final Preferences prefs =
	NbPreferences.forModule(ExternalStart.class);

    private static final String PREFIX = "xstart.";       // NOI18N

    private static final String PREF_DONOTSHOWAGAIN =
	PREFIX + "doNotShowAgain";	// NOI18N

    private static boolean isDoNotShowAgain() {
	return prefs.getBoolean(PREF_DONOTSHOWAGAIN, false);
    }

    private static void setDoNotShowAgain(boolean doNotShowAgain) {
	prefs.putBoolean(PREF_DONOTSHOWAGAIN, doNotShowAgain);
    }

}
