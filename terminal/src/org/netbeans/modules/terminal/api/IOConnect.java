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
 * Capability of InputOutput to help manage and track Stream connections
 * to an InputOutput.
 * @author ivan
 */
public abstract class IOConnect {

    public static final String PROP_CONNECTED = "IOConnect.PROP_CONNECTED"; // NOI18N

    private static IOConnect find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOConnect.class);
        }
        return null;
    }

    /**
     * Return whether any streams are connected to this IO.
     * <b>
     * An IO is "disconnected" in it's default state, before any of getOut(),
     * getErr() or IOTerm.connect() are called.
     * <b>
     * An IO is "connected" if any of getOut(), getErr() or
     * IOTerm.connect() are called.
     * <b>
     * An IO is "disconnected" after all of getIn().close(), getErr().close() and
     * IOTerm.disconnect() or disconnectAll() are called.
     * <b>
     * Only a "disconnected" IO is eligible for reuse via
     * {@link org.openide.windows.IOProvider#getIO(String, boolean)}
     * @param io
     */
    public static boolean isConnected(InputOutput io) {
	IOConnect ioc = find(io);
	if (ioc != null)
	    return ioc.isConnected();
	else
	    return false;
    }

    /**
     * Disconnects all of getIn() and getOut() and any streams connected
     * via IOTerm.connect().
     * @param io
     * @param continuation See {@link IOTerm#disconnect}.
     */
    public static void disconnectAll(InputOutput io, Runnable continuation) {
	IOConnect ioc = find(io);
	if (ioc != null)
	    ioc.disconnectAll(continuation);
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    abstract protected boolean isConnected();

    abstract protected void disconnectAll(Runnable continuation);
}
