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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 * A ContextAction which looks for a specific subclass of Lookup.Provider
 * in the selection, and then delegates to another ContextAction for that
 * type.
 * <p/>
 * This class can be used to do multiple levels of indirection, simply
 * by passing another instance of IndirectAction to the constructor.
 * Say that you want to write an action that operates on the
 * ClassPathProvider of the Project which the selected Node belongs to.
 * This can be written quite simply:
 * <pre>
 * ContextAction<ClassPathProvider> theRealAction = new MyContextAction();
 * Action theGlobalAction = new IndirectAction<Node>(Project.class, new IndirectAction(ClassPathProvider.class, theRealAction));
 * </pre>
 * <p/>
 *
 * @param <T> The type of Lookup.Provider to look for in the lookup.
 * @param <R> The type that the passed ContextAction is interested in
 * @author Tim Boudreau
 */
final class IndirectAction<T extends Lookup.Provider, R> extends ContextAction<T> {
    final ContextAction<R> delegate;
    private final PCL pcl = new PCL();
    private final boolean all;

    /**
     * Create a new IndirectAction.
     *
     * @param toLookupType The type of Lookup.Provider to find in the
     * selection
     * @param delegate The action that will look in those Lookup.Providers
     * lookups
     */
    public IndirectAction(Class<T> toLookupType, ContextAction<R> delegate, boolean all) {
        super(toLookupType);
        this.delegate = delegate;
        this.all = all;
    }

    @Override
    void internalAddNotify() {
        super.internalAddNotify();
        delegate.addPropertyChangeListener(pcl);
    }

    @Override
    void internalRemoveNotify() {
        super.internalRemoveNotify();
        delegate.removePropertyChangeListener(pcl);
    }

    @Override
    public Object getValue(String key) {
        Object result = super.getValue(key);
        if (result == null) {
            result = delegate.getValue(key);
        }
        return result;
    }

    private Lookup delegateLookup(Collection<? extends T> targets) {
        List<Lookup> lookups = new LinkedList<Lookup>();
        for (Lookup.Provider provider : targets) {
            Lookup lkp = provider.getLookup();
            if (all && lkp.lookupResult(delegate.type).allItems().size() == 0) {
                return Lookup.EMPTY;
            }
            lookups.add(provider.getLookup());
        }
        Lookup proxy = new ProxyLookup(lookups.toArray(new Lookup[0]));
        return proxy;
    }

    @Override
    protected void actionPerformed(Collection<? extends T> targets) {
        Action delegateStub = delegate.createContextAwareInstance(delegateLookup(targets));
        delegateStub.actionPerformed(null);
    }

    @Override
    protected boolean isEnabled(Collection<? extends T> targets) {
        Action delegateStub = delegate.createContextAwareInstance(delegateLookup(targets));
        return delegateStub.isEnabled();
    }

    @Override
    public boolean equals (Object o) {
        return o != null && o.getClass() == IndirectAction.class && delegate.equals(((IndirectAction) o).delegate);
    }

    /**
     * Returns getClass().hashCode();
     * @return The hash code of this type.
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    private final class PCL implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
                    evt.getNewValue());
        }
    }
}
