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

/**
 * @author Ana von Klopp
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;
import javax.swing.JButton;

import org.openide.util.NbBundle;


class SortButton extends JButton {

    private int state = DisplayTable.NEUTRAL;

    private Icon[] icon = new Icon[3];
    
    public SortButton(final DisplayTable dt) {    
	super();
	icon[0] = ImageUtilities.loadIcon("org/netbeans/modules/web/monitor/client/icons/unsorted.gif"); // NOI18N)
	icon[1] = ImageUtilities.loadIcon("org/netbeans/modules/web/monitor/client/icons/a2z.gif"); // NOI18N
	icon[2] = ImageUtilities.loadIcon("org/netbeans/modules/web/monitor/client/icons/z2a.gif"); // NOI18N
	setIcon(icon[state]); 
	setBorder(null);
	setBorderPainted(false);
        setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonUnsortedA11yDesc"));
	
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
	
                    Logger.getLogger(SortButton.class.getName()).info("Sort requested");
		    
		    state++;
		    state=state%3;

                    Logger.getLogger(SortButton.class.getName()).info("State is: " + String.valueOf(state));
		    JButton b = (JButton)e.getSource();
		    b.setIcon(icon[state]); 
		    
		    if(state == DisplayTable.NEUTRAL)
                    {
			// PENDING
                        SortButton.this.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonUnsortedA11yDesc"));
                    }
		    else if(state == DisplayTable.A2Z) {
                        SortButton.this.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonSortAZA11yDesc"));
		    } else if(state == DisplayTable.Z2A) {
                        SortButton.this.setToolTipText(NbBundle.getBundle(TransactionView.class).getString("ACS_SortButtonSortZAA11yDesc"));
                    }
		    dt.setSorting(state);
		}
	    });
    }

    int getMode() { 
	return state;
    }
} // SortButton
