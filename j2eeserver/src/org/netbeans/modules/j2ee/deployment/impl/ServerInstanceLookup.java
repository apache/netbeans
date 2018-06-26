/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.j2ee.deployment.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class ServerInstanceLookup extends Lookup {

    private final ServerInstance instance;

    private final DeploymentFactory factory;

    private final Target target;

    public ServerInstanceLookup(ServerInstance instance,
            DeploymentFactory factory, Target target) {

        assert instance != null;
        assert factory != null;

        this.instance = instance;
        this.factory = factory;
        this.target = target;
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        if (DeploymentFactory.class.isAssignableFrom(clazz)) {
            return clazz.cast(factory);
        } else if (DeploymentManager.class.isAssignableFrom(clazz)) {
            if (instance.isConnected()) {
                return clazz.cast(instance.getDeploymentManager());
            }
            try {
                return clazz.cast(instance.getDisconnectedDeploymentManager());
            } catch (DeploymentManagerCreationException dmce) {
                Exceptions.printStackTrace(dmce);
            }
        } else if (target != null && Target.class.isAssignableFrom(clazz)) {
            return clazz.cast(target);
        }
        return null;
    }

    @Override
    public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
        if (DeploymentFactory.class.isAssignableFrom(template.getType())) {
            return new SimpleResult(factory);
        } else if (DeploymentManager.class.isAssignableFrom(template.getType())) {
            return (Lookup.Result<T>) new DeploymentManagerResult(instance);
        } else if (Target.class.isAssignableFrom(template.getType())) {
            return new SimpleResult(target);
        }

        return new EmptyResult();
    }

    private static final class EmptyResult<T> extends Lookup.Result<T> {

        @Override
        public void addLookupListener(LookupListener l) {
        }

        @Override
        public void removeLookupListener(LookupListener l) {
        }

        @Override
        public Collection<? extends T> allInstances() {
            return Collections.emptyList();
        }
    }

    private abstract static class AbstractResult<T> extends Lookup.Result<T> {

        public AbstractResult() {
            super();
        }

        public abstract T getInstance();

        @Override
        public synchronized Collection<? extends T> allInstances() {
            T instance = getInstance();
            if (instance == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(instance);
        }

        @Override
        public synchronized Set<Class<? extends T>> allClasses() {
            T instance = getInstance();
            if (instance == null) {
                return Collections.emptySet();
            }
            Class<T> clazz = (Class<T>) instance.getClass();
            return Collections.<Class<? extends T>>singleton(clazz);
        }

        @Override
        public synchronized Collection<? extends Item<T>> allItems() {
            T instance = getInstance();
            if (instance == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(Lookups.lookupItem(instance, null));
        }
    }

    private static class SimpleResult<T> extends AbstractResult<T> {

        private final T instance;

        public SimpleResult(T instance) {
            this.instance = instance;
        }

        @Override
        public void addLookupListener(LookupListener l) {
        }

        @Override
        public void removeLookupListener(LookupListener l) {
        }

        @Override
        public T getInstance() {
            return instance;
        }
    }

    private static class DeploymentManagerResult extends AbstractResult<DeploymentManager> implements ChangeListener {

        private final ServerInstance instance;

        private final List<LookupListener> listeners = new ArrayList<LookupListener>();

        public DeploymentManagerResult(ServerInstance instance) {
            this.instance = instance;
        }

        @Override
        public void addLookupListener(LookupListener l) {
            if (l == null) {
                return;
            }
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    instance.addManagerChangeListener(this);
                }
                listeners.add(l);
            }
        }

        @Override
        public void removeLookupListener(LookupListener l) {
            if (l == null) {
                return;
            }
            synchronized (listeners) {
                listeners.remove(l);
                if (listeners.isEmpty()) {
                    instance.removeManagerChangeListener(this);
                }
            }
        }

        // this can fire fake events in rare cases
        public void stateChanged(ChangeEvent e) {
            List<LookupListener> toFire = new ArrayList<LookupListener>();
            synchronized (listeners) {
                toFire.addAll(listeners);
            }

            for (LookupListener listener : toFire) {
                listener.resultChanged(new LookupEvent(this));
            }

        }

        @Override
        public DeploymentManager getInstance() {
            if (instance.isConnected()) {
                return instance.getDeploymentManager();
            }
            try {
                return instance.getDisconnectedDeploymentManager();
            } catch (DeploymentManagerCreationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
}
