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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
