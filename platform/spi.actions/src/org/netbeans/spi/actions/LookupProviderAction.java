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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 * LookupProviderAction is an action subclass which looks up some
 * type which implements Lookup.Provider in the global selection, and then
 * actually delegates to some other type in the lookups of the Lookup.Providers
 * within.
 * <p/>
 * The canonical example of this is NodeContextAction, which looks for one or more Nodes
 * in the global action context, but what it really needs to operate on
 * is some objects that may or may not be in those Nodes' lookups.
 * <p/>
 * This also handles the case of exclusive multi-selection, where the action
 * should only be enabled if like objects are selected.  An example of this
 * is OpenAction, which should be enabled if all of the selected files
 * own an OpenCookie, but should not be enabled otherwise.
 * <p/>
 * To do more than one level of indirection of Lookup.Providers, or to
 * impose restrictions such as how many instances of a type can be in the
 * lookup, use IndirectAction and pass it a subclass of
 * ContextAction (such as ContextAction.Single) that does what you want.
 * <p/>
 * Anything that can be done with this class can be done with
 * DelegateAction;  this class just simplifies the common case of one
 * layer of indirection.
 *
 * @param <T> The type of Lookup.Provider to look for in the selection - for
 * example, Node.class or Project.class
 * @param <R> The type of object this action will actually operate on - for
 * example, an OpenCookie instance
 * @author Tim Boudreau
 */
public abstract class LookupProviderAction <T extends Lookup.Provider, R> extends ContextAction <T>{
        private final Class<R> delegateType;
        private final boolean all;
        final ContextAction<R> overDelegateType;
        private boolean inDelegate;
        /**
         * Create a new LookupProviderAction.
         * @param type The type of Lookup.Provider to find in the global selection
         * @param delegateType The type of object to look for in the lookups of the instances
         * of <code>type</code> in the global selection
         * @param all If true, all T's in the global selection must contain at least one R
         * for this action to be enabled.  If false, it is enabled if there are any R's in
         * any of the T's lookups.
         */
        protected LookupProviderAction (Class<T> type, Class<R> delegateType, boolean all) {
            super (type);
            assert delegateType != null;
            this.delegateType = delegateType;
            this.all = all;
            overDelegateType = new InternalDelegateAction(delegateType);
        }

        /**
         * Create a new LookupProviderAction.
         * @param type The type of Lookup.Provider to find in the global selection
         * @param delegateType The type of object to look for in the lookups of the instances
         * of <code>type</code> in the global selection
         * @param all If true, all T's in the global selection must contain at least one R
         * for this action to be enabled.  If false, it is enabled if there are any R's in
         * any of the T's lookups.
         * @param displayName The localized display name
         * @param icon The icon
         */
        protected LookupProviderAction (Class<T> type, Class<R> delegateType, boolean all, String displayName, Image icon) {
            super (type, displayName, icon);
            assert delegateType != null;
            this.delegateType = delegateType;
            this.all = all;
            overDelegateType = new InternalDelegateAction(delegateType);
        }

        protected final void actionPerformed(Collection<? extends T> targets) {
            if (all) {
                Collection<? extends R> delegates = delegates(targets);
                assert delegates != null && !delegates.isEmpty();
            }
            Action contextDelegate = overDelegateType.createContextAwareInstance(
                    delegateLookup());
            assert contextDelegate.isEnabled();
            contextDelegate.actionPerformed((ActionEvent) null);
        }

        /**
         * Determine if this action should be enabled in the case of
         * multi-selection.  By default this method simply returns true
         * if lookupProviderCount > 0.  To have an action which only
         * is enabled if there is exactly one selected item, override
         * and test <code>lookupProviderCount == 1</code>.
         * <p/>
         * Note that the number passed to this method is the number of
         * <i><code>Lookup.Provider</code></i>s in the selection, <b>not</b>
         * the number of objects of type R in those Lookup.Providers.
         * If you want to control for that, override <code>isEnabled(Collection)</code>.
         *
         * @param lookupProviderCount The number of Lookup.Providers of type T
         * in the selection
         * @return true if the action should be enabled
         */
        @Override
        protected boolean checkQuantity(int lookupProviderCount) {
            return lookupProviderCount > 0;
        }

        /**
         * Creates a ProxyLookup over all selected Lookup.Providers of type T
         * @return A lookup that proxies all the lookups of all T's in the
         * current global selection
         */
        private synchronized Lookup delegateLookup() {
            inDelegate = true;
            try {
                List <Lookup> lookups = new LinkedList<Lookup>();
                Collection <? extends T> sc = super.getStub().collection();
                assert sc != null;
                for (Lookup.Provider provider : sc) {
                    lookups.add (provider.getLookup());
                }
                Lookup[] lkps = lookups.toArray (new Lookup[0]);
                return new ProxyLookup (lkps);
            } finally {
                inDelegate = false;
            }
        }

        /**
         * Get the objects of type R that we really want to operate on.
         * @param targets The collection from the lookup of the current selection (which
         * is a Lookup.Provider)
         * @return A collection of R's.  If all Lookup.Providers in the selection
         * must contain an R and one or more do not, returns null.
         */
        private Set <? extends R> delegates(Collection<? extends T> targets) {
            Set <R> realTargets = new HashSet<R>();
            for (Lookup.Provider provider : targets) {
                Collection <? extends R> delegateCookies =
                        provider.getLookup().lookupAll (delegateType);
                if (all && delegateCookies.isEmpty()) {
                    return null;
                } else {
                    realTargets.addAll (delegateCookies);
                }
            }
            return realTargets;
        }

        /**
         * Actually perform the action on the collection of objects found
         * in the Lookup.Provider of type <code>T</code> in the lookup.
         * @param targets The collection of objects
         */
        protected abstract void perform(Collection <? extends R> targets);

        /**
         * Determine if the action should be enabled.  This method is only
         * called if the collection is non-empty.  The default implementation
         * simply returns true.  Only override this if you really need to
         * check the state of the objects in the collection to determine
         * if the action can really be performed.
         * @param targets The collection of objects found in the
         * Lookup.Provider of type <code>T</code> in the selection.
         * @return whether or not this action should be enabled
         */
        protected boolean enabled (Collection<? extends R> targets) {
            return true;
        }

        @Override
        protected final boolean isEnabled(Collection<? extends T> targets) {
            if (inDelegate) {
                //Avoid endless loop when context aware instance also checks
                //its enablement to initialize its state
                return true;
            }
            boolean result;
            if (all) {
                //If all the Lookups must contain at least one R, make
                //sure they do
                Collection<? extends R> delegates = delegates(targets);
                result = delegates != null && !delegates.isEmpty();
            } else {
                result = true;
            }
            //Okay, we know we have a selection we can use.  Get an actual
            //instance
            if (result) {
                result = overDelegateType.createContextAwareInstance(delegateLookup()).isEnabled();
            }
            return result;
        }

        private class InternalDelegateAction extends ContextAction<R> {

            public InternalDelegateAction(Class<R> type) {
                super(type);
            }

            @Override
            protected void actionPerformed(Collection<? extends R> targets) {
                perform(targets);
            }

            @Override
            protected boolean isEnabled(Collection<? extends R> targets) {
                return LookupProviderAction.this.enabled(targets);
            }

            @Override
            public Object getValue(String key) {
                Object result = super.getValue(key);
                if (result == null) {
                    result = LookupProviderAction.this.getValue(key);
                }
                return result;
            }
        }

}
