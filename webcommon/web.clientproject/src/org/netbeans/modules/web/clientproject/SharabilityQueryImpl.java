/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.env.Values;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 * Inspired from PhpSharabilityQuery.
 */
public final class SharabilityQueryImpl implements SharabilityQueryImplementation2, PropertyChangeListener {

    private final CommonProjectHelper helper;
    private final Values evaluator;
    private final Set<String> sourceRootProperties;

    // @GuardedBy("this")
    private SharabilityQueryImplementation2 delegate;


    private SharabilityQueryImpl(CommonProjectHelper helper, Values evaluator, String... sourceRootProperties) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRootProperties = new CopyOnWriteArraySet<String>(Arrays.asList(sourceRootProperties));
    }

    public static SharabilityQueryImpl create(CommonProjectHelper helper, Values evaluator, String... sourceRootProperties) {
        SharabilityQueryImpl query = new SharabilityQueryImpl(helper, evaluator, sourceRootProperties);
        query.addSourceRootsListener();
        return query;
    }

    @Override
    public Sharability getSharability(final URI uri) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Sharability>() {
            @Override
            public Sharability run() {
                return getDelegate().getSharability(uri);
            }
        });
    }

    private SharabilityQueryImplementation2 createDelegate() {
        List<String> props = new ArrayList<String>(sourceRootProperties.size());
        for (String src : sourceRootProperties) {
            props.add("${" + src + "}"); // NOI18N
        }
        return helper.createSharabilityQuery2(evaluator, props.toArray(new String[0]), new String[0]);
    }

    private void addSourceRootsListener() {
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (sourceRootProperties.contains(evt.getPropertyName())) {
            resetDelegate();
        }
    }

    synchronized SharabilityQueryImplementation2 getDelegate() {
        assert Thread.holdsLock(this);
        if (delegate == null) {
            delegate = createDelegate();
        }
        return delegate;
    }

    private synchronized void resetDelegate() {
        assert Thread.holdsLock(this);
        delegate = null;
    }

}
