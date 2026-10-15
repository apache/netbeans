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

package org.netbeans.modules.java.api.common.queries;

import org.netbeans.modules.java.api.common.SourceRoots;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openide.util.Mutex;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;

/**
 * Default implementation of {@link SharabilityQueryImplementation} which is capable to take more sources.
 * It listens to the changes in particular property values.
 * @author Tomas Zezula, Tomas Mysik
 */
class SharabilityQueryImpl implements SharabilityQueryImplementation2, PropertyChangeListener {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots srcRoots;
    private final SourceRoots testRoots;
    private final List<String> additionalSourceRoots;
    private SharabilityQueryImplementation2 delegate;

    @SuppressWarnings("LeakingThisInConstructor")
    public SharabilityQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots srcRoots,
            SourceRoots testRoots, String... additionalSourceRoots) {
        assert helper != null;
        assert evaluator != null;
        assert srcRoots != null;

        this.helper = helper;
        this.evaluator = evaluator;
        this.srcRoots = srcRoots;
        this.testRoots = testRoots;
        if (additionalSourceRoots != null) {
            this.additionalSourceRoots = Collections.unmodifiableList(Arrays.asList(additionalSourceRoots));
        } else {
            this.additionalSourceRoots = Collections.<String>emptyList();
        }
        this.srcRoots.addPropertyChangeListener(this);
        if (this.testRoots != null) {
            this.testRoots.addPropertyChangeListener(this);
        }
    }

    @Override public SharabilityQuery.Sharability getSharability(final URI file) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SharabilityQuery.Sharability>() {
            @Override public SharabilityQuery.Sharability run() {
                synchronized (SharabilityQueryImpl.this) {
                    if (delegate == null) {
                        delegate = createDelegate();
                    }
                    return delegate.getSharability(file);
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(evt.getPropertyName())) {
            synchronized (this) {
                delegate = null;
            }
        }
    }

    private SharabilityQueryImplementation2 createDelegate() {
        String[] srcProps = srcRoots.getRootProperties();
        String[] testProps = testRoots == null ? new String[0] : testRoots.getRootProperties();
        String[] buildDirectories = new String[] {"${dist.dir}", "${build.dir}"}; // NOI18N

        int size = srcProps.length;
        size += testProps.length;
        size += additionalSourceRoots.size();
        List<String> props = new ArrayList<String>(size);

        for (String src : srcProps) {
            props.add("${" + src + "}"); // NOI18N
        }
        for (String test : testProps) {
            props.add("${" + test + "}"); // NOI18N
        }
        props.addAll(additionalSourceRoots);

        return helper.createSharabilityQuery2(evaluator, props.toArray(new String[0]), buildDirectories);
    }
}
