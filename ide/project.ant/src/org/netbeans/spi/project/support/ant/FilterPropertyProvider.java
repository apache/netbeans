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

package org.netbeans.spi.project.support.ant;

import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 * Property provider that delegates to another source.
 * Useful, for example, when conditionally loading from one or another properties file.
 * @since org.netbeans.modules.project.ant/1 1.14
 */
public abstract class FilterPropertyProvider implements PropertyProvider {

    private PropertyProvider delegate;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final ChangeListener strongListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            //System.err.println("DPP: change from current provider " + delegate);
            cs.fireChange();
        }
    };
    private ChangeListener weakListener = null; // #50572: must be weak

    /**
     * Initialize the proxy.
     * @param delegate the initial delegate to use
     */
    protected FilterPropertyProvider(PropertyProvider delegate) {
        assert delegate != null;
        setDelegate(delegate);
    }

    /**
     * Change the current delegate (firing changes as well).
     * @param delegate the initial delegate to use
     */
    protected final void setDelegate(PropertyProvider delegate) {
        if (delegate == this.delegate) {
            return;
        }
        if (this.delegate != null) {
            assert weakListener != null;
            this.delegate.removeChangeListener(weakListener);
        }
        this.delegate = delegate;
        weakListener = WeakListeners.change(strongListener, delegate);
        delegate.addChangeListener(weakListener);
        cs.fireChange();
    }

    public final Map<String, String> getProperties() {
        return delegate.getProperties();
    }

    public final synchronized void addChangeListener(ChangeListener listener) {
        // XXX could listen to delegate only when this has listeners
        cs.addChangeListener(listener);
    }

    public final synchronized void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

}
