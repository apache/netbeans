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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * MonitorAction.java
 *
 *
 * Created: Wed Feb  2 15:37:25 2000
 *
 * @author Ana von Klopp
 * @version
 */

package  org.netbeans.modules.web.monitor.client;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;


public class MonitorAction extends CallableSystemAction {

    static transient Controller controller = null;
    private static final boolean debug = false;
     
    public MonitorAction() {
    }

    protected static Controller getController() {
	return Controller.getInstance();
    }
    
    public String getName () {
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
	return NbBundle.getBundle(MonitorAction.class).getString("MON_HTTP_Transaction_13");
    }
  
    public HelpCtx getHelpCtx () {
	return new HelpCtx (MonitorAction.class);
    }

    protected String iconResource () {
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
	return "org/netbeans/modules/web/monitor/client/icons/menuitem.gif"; //NOI18N

    }

    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * Starts a monitor window. This method is used by the menu item,
     * so it should verify that the execution server is running, and
     * if it isn't, restart it. 
     */
  
    public void performAction() {
	openTransactionView(); 
    }
   
    static void addTransaction(String id) { 
	if(!TransactionView.getInstance().isOpened()) {
            boolean initialized = TransactionView.getInstance().isInitialized();
            // If not initialized yet, this will cause the record to be loaded 
            // from disk, so we don't need to add it in this case
	    openTransactionView(); 
            if (!initialized) {
                return;
            }
	} 
	// Otherwise we add it to the current records
	Controller.getInstance().addTransaction(id); 
    } 

    private static void openTransactionView() {

	TransactionView tv = TransactionView.getInstance(); 
        WindowManager wm = WindowManager.getDefault();
	Mode mode = wm.findMode(tv);
        
        if(mode == null) {
            mode = wm.findMode("output"); // NOI18N
            if(mode != null) {
                mode.dockInto(tv);
            }
        }
	tv.open();
        tv.requestVisible();
        tv.requestActive();        
    }

    public static void log(String s) {
	log("MonitorAction::" + s); //NOI18N
    }
}

