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
import org.openide.windows.IOContainer;

/**
 * Capability of an IOProvider.CallBacks which provides access to state
 * governing tab visibility.
 */
public abstract class IOVisibilityControl {

    private static IOVisibilityControl find(IOContainer.CallBacks cbs) {
        if (cbs instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) cbs;
            return p.getLookup().lookup(IOVisibilityControl.class);
        }
        return null;
    }

    /**
     * This allows satisfying IOContainer.isClosable().
     * If the component is not closable okToHide will not get called.
     * @param cbs
     * @return Returns true if component should be closable.
     */

    /* LATER
     * @deprecated
     * This implementation is a pull style implementation. So when
     * IOVisibility.setClosable() is called someone needs to trigger IOContainer
     * to re-pull this information. There is no API for triggering pulls.
     * <p>
     * Ironically we can make it work w/o a triggering API. The reason is that
     * whether the Tab has an X is controlled by
     * <code>
	putClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON, ! closable);
     * </code>
     * which doesn't need IOContainers involvement.
     * <br>
     * But that assumes that the container is implemented using NB TabedPaneFactory.
     * Alternative implementations will not benefit from this.
     * <p>
     * A better approach might be a "push" style where we introduce
     * IOContainer.setClosable() (using the usual capability enhancement technique).
     * <br>
     * However, Terminal implementation isn't very good yet at managing the
     * multiplexing of Terminal (aka IOTab) attributes (tooltip, actions etc).
     * So I'm postponing the push style until attribute multiplexing is under
     * control.
     * <p>
     * NOTE: once we switch to push style revisit
     * TerminalInputOutput.MyIOVisibility.isSupported()
     * <p>
     * NOTE: once we switch to push style revisit
     *
     */
    // LATER @Deprecated
    public static boolean isClosable(IOContainer.CallBacks cbs) {
	IOVisibilityControl iov = find(cbs);
	if (iov != null)
	    return iov.isClosable();
	else
	    return true;
    }

    /**
     * This should cause a vetoableChange() call for
     * IOVisibility.PROP_VISIBILITY() with a new value of false.
     * If it gets vetoed, false should be returned. Otherwise true should
     * be returned.
     * @param cbs
     * @return True if OK to close component.
     */
    public static boolean okToClose(IOContainer.CallBacks cbs) {
	IOVisibilityControl iov = find(cbs);
	if (iov != null)
	    return iov.okToClose();
	else
	    return true;
    }

    /**
     * Checks whether this feature is supported for provided IOContainer.CallBacks
     */
    public static boolean isSupported(IOContainer.CallBacks cbs) {
        return find(cbs) != null;
    }

    abstract protected boolean isClosable();
    abstract protected boolean okToClose();

}
