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

package org.netbeans.modules.terminal.api;

import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * Capability of an InputOutput which allows ...
 * <ul>
 * <li>
 * Querying and setting of
 * emulation type using termcap/terminfo terminal types.
 * <br>
 * The default implementation of InputOutput has a terminal type of
 * "dumb".
 * <li>
 * Controlling whether the IO depends on an external agent, typically a pty,
 * to implement a "line discipline" or emulates it itself.
 * <br>
 * Line discipline is the functionality which handles things like
 *   <ul>
 *   <li>Line buffering.
 *   <li>CR/LF conversions.
 *   <li>Backspace.
 *   <li>Tabs.
 *   </ul>
 * The default implementation of InputOutput has disciplined set to true.
 * </ul>
 * @author ivan
 */
public abstract class IOEmulation {

    private static IOEmulation find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOEmulation.class);
        }
        return null;
    }

    /**
     * Return the terminal type supported by this IO.
     * @param io IO to operate on.
     * @return terminal type.
     */
    public static String getEmulation(InputOutput io) {
	IOEmulation ior = find(io);
	if (ior != null)
	    return ior.getEmulation();
	else
	    return "dumb";		// NOI18N

    }

    /**
     * Return whether this IO implements it's own line discipline.
     * @param io IO to operate on.
     * @return If true this IO implements it's own line discipline.
     */
    public static boolean isDisciplined(InputOutput io) {
	IOEmulation ior = find(io);
	if (ior != null)
	    return ior.isDisciplined();
	else
	    return true;
    }

    /**
     * Return whether this IO implements it's own line discipline.
     * @param io IO to operate on.
     */
    public static void setDisciplined(InputOutput io) {
	IOEmulation ior = find(io);
	if (ior != null)
	    ior.setDisciplined();
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    /**
     * Return the terminal type supported by this IO.
     * @return terminal type.
     */
    abstract protected String getEmulation();

    /**
     * Return whether this IO implements it's own line discipline.
     * @return If true this IO implements it's own line discipline.
     */
    abstract protected boolean isDisciplined();

    /**
     * Declare that this IO should implement it's own line discipline.
     */
    abstract protected void setDisciplined();
}
