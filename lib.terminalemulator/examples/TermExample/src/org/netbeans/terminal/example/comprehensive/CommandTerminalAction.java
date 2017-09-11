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

package org.netbeans.terminal.example.comprehensive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import org.netbeans.terminal.example.Config;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.IOProvider;
import org.openide.windows.IOContainer;


import org.netbeans.terminal.example.TerminalIOProviderSupport;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

public final class CommandTerminalAction implements ActionListener {

    private final TerminalPanel terminalPanel = new TerminalPanel();

    public static InputOutput lastIO;

    public void actionPerformed(ActionEvent e) {

	DialogDescriptor dd = new DialogDescriptor(
		terminalPanel,
		"Start Terminal",
		true,
		new Object[] {
		    DialogDescriptor.OK_OPTION,
		    DialogDescriptor.CANCEL_OPTION,
		},
		DialogDescriptor.CANCEL_OPTION,
		DialogDescriptor.BOTTOM_ALIGN,
		null,		// HelpCtx
		null		// ActionListener
		);

	// null means all options close dialog:
        // 0-sized array means no option closes dialog
	dd.setClosingOptions(null);

	Object closer = DialogDisplayer.getDefault().notify(dd);
	if (closer == DialogDescriptor.CANCEL_OPTION) {
//	    System.out.printf("Dialog cancelled\n");
	    return;
	}
	if (closer == DialogDescriptor.CLOSED_OPTION) {
//	    System.out.printf("Dialog closed\n");
	    return;
	}


	final Config config = terminalPanel.getConfig();
	final String cmd = config.getCommand();
        if (cmd == null || cmd.trim().equals(""))
            return;

	final TerminalIOProviderSupport support = new TerminalIOProviderSupport(config);
	final IOContainer container;
	final IOProvider iop;

	switch (config.getContainerProvider()) {
	    case TERM:
		container = TerminalIOProviderSupport.getIOContainer(config);
		break;
	    case DEFAULT:
	    default:
		container = null;
		break;
	}

	switch (config.getIOProvider()) {
	    case TERM:
		iop = TerminalIOProviderSupport.getIOProvider();
		break;
	    case DEFAULT:
	    default:
		iop = IOProvider.getDefault();
		break;
	}

	final Runnable runnable = new Runnable() {
	    public void run() {
		final InputOutput io;
		switch (config.getExecution()) {
		    case RICH:
			io = support.executeRichCommand(iop, container);
			break;
		    case NATIVE:
			io = support.executeNativeCommand(iop, container);
			break;
		    default:
			io = null;
			break;
		}
		lastIO = io;
	    }
	};

	switch (config.getThread()) {
	    case EDT:
		SwingUtilities.invokeLater(runnable);
		break;
	    case RP:
		RequestProcessor.getDefault().execute(runnable);
		break;
	}
    }
}