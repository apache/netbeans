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

/*
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

}

