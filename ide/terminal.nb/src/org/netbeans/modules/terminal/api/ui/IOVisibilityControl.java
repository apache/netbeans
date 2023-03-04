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

    protected abstract boolean isClosable();
    protected abstract boolean okToClose();

}
