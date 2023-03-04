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
package org.netbeans.spi.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;

/**Inner stub action class which delegates to the parent action's methods.
* Used both for context aware instances, and for internal state for
* ContextAction instances
* @author Tim Boudreau
*/
class ActionStub<T> extends NbAction implements LookupListener, ContextAwareAction {

    private final Lookup.Result<T> lkpResult;
    private final Lookup context;
    protected final ContextAction<T> parent;
    protected boolean enabled;

    ActionStub(Lookup context, ContextAction<T> parent) {
        assert context != null;
        this.context = context;
        this.parent = parent;
        lkpResult = context.lookupResult(parent.type);
        lkpResult.addLookupListener(this);
        if (getClass() == ActionStub.class) {
            //avoid superclass call to Retained.collection() before
            //it has initialized
            enabled = isEnabled();
        }
    }

    @Override
    public Object getValue(String key) {
        Object result = _getValue(key);
        if (result == null) {
            result = parent.getValue(key);
        }
        return result;
    }

    Object _getValue(String key) {
        if (!attached()) {
            //Make sure any code that updates the name runs - we are not
            //listening to the lookup
            resultChanged(null);
        }
        return pairs.get(key);
    }

    Collection<? extends T> collection() {
        return lkpResult.allInstances();
    }
    
    public boolean isEnabled() {
        Collection<? extends T> targets = collection();
        assert targets != null;
        assert parent != null;
        return targets.isEmpty() ? false : parent.checkQuantity(targets) &&
                parent.isEnabled(targets);
    }

    public void actionPerformed(ActionEvent e) {
        assert isEnabled() : "Not enabled: " + this + " collection " +
                collection() + " of " + parent.type.getName();
        Collection<? extends T> targets = collection();
        parent.actionPerformed(targets);
    }

    void enabledChanged(final boolean enabled) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                firePropertyChange(PROP_ENABLED, !enabled, enabled); //NOI18N
                if (ContextAction.unitTest) {
                    synchronized (parent) {
                        parent.notifyAll();
                    }
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }
        });
    }

    public void resultChanged(LookupEvent ev) {
        if (ContextAction.unitTest) {
            synchronized (parent) {
                parent.notifyAll();
            }
        }
        synchronized(parent.lock()) {
            parent.change (collection(), this == parent.stub ? parent : this);
        }
        boolean old = enabled;
        enabled = isEnabled();
        if (old != enabled) {
            enabledChanged(enabled);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[name=" + getValue(NAME) + //NOI18N
                "delegating={" + parent + "} context=" + //NOI18N
                context + "]"; //NOI18N
    }

    @Override
    protected NbAction internalCreateContextAwareInstance(Lookup actionContext) {
        return parent.createStub(actionContext);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass() && parent.equals(((ActionStub<?>) o).parent);
    }

    @Override
    public int hashCode() {
        return parent.hashCode() * 37;
    }
}
