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

package org.netbeans.modules.terminal.api.ui;

import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * Capability of an InputOutput which controls whether it is visible
 * as a tab or not. Note that this is orthogonal to whether the
 * window/TopComponent containing this IO becomes visible or not.
 * <p>
 * Support for this capability not only depends on which IOProvider this IO
 * originated from but also which IOContainer it is contained in.
 * @author ivan
 */
public abstract class IOVisibility {

    public static final String PROP_VISIBILITY = "IOVisibility.PROP_VISIBILITY"; // NOI18N

    private static IOVisibility find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOVisibility.class);
        }
        return null;
    }

    /**
     * Control the visibility of this I/O.
     * setVisible(true) is roughly equivalent to {@link org.openide.windows.IOSelect#select}
     * with an empty <code>extraOps</code>.
     * setVisible(false) will unconditionally remove this IO from it's container.
     * I.e. it is not the same as X'ing the tab or Closing from the context menu,
     * operations which are tempered by isClosable() and vetoing.
     * @param visible
     */
    public static void setVisible(InputOutput io, boolean visible) {
	IOVisibility iov = find(io);
	if (iov != null)
	    iov.setVisible(visible);
    }

    /**
     * Control whether this IO is closable. When closable...
     * <ul>
     * <li>The X on the tab goes away.
     * <li>Close actions are disabled.
     * <li>Close all tabs actions will close only closable tabs.
     * <li>setVisible(false) is still effective!
     * <li>closeInputOutput() is still effective!
     * </ul>
     * @param io
     * @param closable
     */
    public static void setClosable(InputOutput io, boolean closable) {
	IOVisibility iov = find(io);
	if (iov != null)
	    iov.setClosable(closable);
    }

    public static boolean isClosable(InputOutput io) {
	IOVisibility iov = find(io);
	if (iov != null)
	    return iov.isClosable();
	else
	    return true;
    }

    /**
     * Checks whether this feature is supported for provided IO.
     * The availability of this capability also depends on which IOContainer
     * The IO belongs to.
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
	IOVisibility iov = find(io);
	if (iov == null)
	    return false;
	else
	    return iov.isSupported();
    }

    abstract protected void setVisible(boolean visible);
    abstract protected void setClosable(boolean closable);
    abstract protected boolean isClosable();
    abstract protected boolean isSupported();
}
