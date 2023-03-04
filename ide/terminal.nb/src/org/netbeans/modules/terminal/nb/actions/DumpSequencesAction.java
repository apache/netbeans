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
package org.netbeans.modules.terminal.nb.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;

/**
 *
 * @author igromov
 */
@ActionID(id = ActionFactory.DUMP_SEQUENCE_ACTION_ID, category = ActionFactory.CATEGORY)
@ActionRegistration(displayName = "#CTL_DumpSequences", lazy = true) //NOI18N
@ActionReferences({
    @ActionReference(path = ActionFactory.ACTIONS_PATH, name = "DumpSequencesAction") //NOI18N
})
public class DumpSequencesAction extends TerminalAction {

    public DumpSequencesAction(Terminal context) {
	super(context);

	putValue(NAME, getMessage("CTL_DumpSequences")); //NOI18N
    }

    private void dump(String title, Set<String> set) {
	File file = new File(String.format("/tmp/term-sequences-%s", title)); // NOI18N
	PrintStream ps;
	try {
	    ps = new PrintStream(file);
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(Terminal.class.getName()).log(Level.SEVERE, null, ex);
	    return;
	}

	if (set != null) {
	    for (String s : set) {
		ps.printf("%s\n", s); // NOI18N
	    }
	}

	ps.close();
    }

    @Override
    public void performAction() {
	Terminal terminal = getTerminal();
	Term term = terminal.term();

	if (!terminal.isEnabled()) {
	    return;
	}
	dump("completed", term.getCompletedSequences()); // NOI18N
	dump("unrecognized", term.getUnrecognizedSequences()); // NOI18N
    }

    // --------------------------------------------- 

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
	return new DumpSequencesAction(actionContext.lookup(Terminal.class));
    }
}
