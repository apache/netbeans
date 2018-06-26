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
 * @author Ana von Klopp
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.openide.util.NbBundle;


class SortButton extends JButton {

    private int state = DisplayTable.NEUTRAL;

    private Icon[] icon = new Icon[3];
    
    public SortButton(final DisplayTable dt) {    
	super();
	icon[0] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/unsorted.gif")); // NOI18N)
	icon[1] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/a2z.gif")); // NOI18N
	icon[2] = new ImageIcon(TransactionView.class.getResource
				("/org/netbeans/modules/web/monitor/client/icons/z2a.gif")); // NOI18N
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
