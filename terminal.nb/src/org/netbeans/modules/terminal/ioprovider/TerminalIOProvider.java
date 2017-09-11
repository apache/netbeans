/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.terminal.ioprovider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;

import org.openide.util.lookup.ServiceProvider;

import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * An implementation of {@link IOProvider} based on
 * {@link org.netbeans.lib.terminalemulator.Term}.
 * Lookup id is "Terminal".
 * <p>
 * <pre>
	IOProvider iop = IOProvider.get("Terminal");
        if (iop == null)
            iop = IOProvider.getDefault();
 * </pre>
 * @author ivan
 */

@ServiceProvider(service = IOProvider.class, position=200)

public final class TerminalIOProvider extends IOProvider {

    private static final List<TerminalInputOutput> list =
	    new ArrayList<TerminalInputOutput>();

    @Override
    public String getName() {
        return "Terminal";      // NOI18N
    }

    @Override
    public InputOutput getIO(String name, Action[] additionalActions) {
	// FIXUP: to try from CND
	return getIO(name, true, additionalActions, null);
    }

    @Override
    public InputOutput getIO(String name, boolean newIO) {
	return getIO(name, newIO, null, null);
    }

    @Override
    public InputOutput getIO(String name, Action[] actions, IOContainer ioContainer) {
	return getIO(name, true, actions, ioContainer);
    }

    @Override
    public InputOutput getIO(String name,
	                      boolean newIO,
			      Action[] actions,
			      IOContainer ioContainer) {

	synchronized (list) {
	    Set<TerminalInputOutput> candidates = new HashSet<TerminalInputOutput>();

	    if (!newIO && name != null) {
		// find candidates for reuse
		for (TerminalInputOutput tio : list) {
		    if (name.equals(tio.name()) && !tio.terminal().isConnected())
			candidates.add(tio);
		}
	    }

	    TerminalInputOutput tio = null;
	    if (candidates.isEmpty()) {
		// newIO == true || nothing found to reuse
		if (ioContainer == null)
		    ioContainer = IOContainer.getDefault();
		tio = new TerminalInputOutput(name, actions, ioContainer);
		list.add(tio);
	    } else {
		for (TerminalInputOutput candidate : candidates) {
		    if (tio == null)
			tio = candidate;
		    else
			candidate.closeInputOutput();
		}
	    }
	    return tio;
	}
    }

    /**
     * This operation is not supported because standard NetBeans output are
     * is not Term based.
     * For that matter, neither Tim nor anyone else remembers what's it for.
     * @return nothing. Always throws UnsupportedOperationException.
     */
    @Override
    public OutputWriter getStdOut() {
        throw new UnsupportedOperationException("Not supported yet.");	// NOI18N
    }

    static void remove(TerminalInputOutput io) {
	synchronized (list) {
	    list.remove(io);
	}
    }

}
