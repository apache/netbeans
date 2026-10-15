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

package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Mutex;

/**
 * Copied from PythonSharabilityQuery.
 * @author Tomas Zezula
 */
public class PhpSharabilityQuery implements SharabilityQueryImplementation2, PropertyChangeListener {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sources;
    private final SourceRoots tests;
    private final SourceRoots selenium;
    private volatile SharabilityQueryImplementation2 delegate;

    private PhpSharabilityQuery(final AntProjectHelper helper, final PropertyEvaluator evaluator,
            final SourceRoots sources, final SourceRoots tests, final SourceRoots selenium) {
        assert helper != null;
        assert evaluator != null;
        assert sources != null;
        assert tests != null;
        assert selenium != null;

        this.helper = helper;
        this.evaluator = evaluator;
        this.sources = sources;
        this.tests = tests;
        this.selenium = selenium;

    }

    public static PhpSharabilityQuery create(final AntProjectHelper helper, final PropertyEvaluator evaluator,
            final SourceRoots sources, final SourceRoots tests, final SourceRoots selenium) {
        PhpSharabilityQuery query = new PhpSharabilityQuery(helper, evaluator, sources, tests, selenium);
        sources.addPropertyChangeListener(query);
        tests.addPropertyChangeListener(query);
        selenium.addPropertyChangeListener(query);
        return query;
    }

    @Override
    public Sharability getSharability(final URI uri) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Sharability>() {
            @Override
            public Sharability run() {
                synchronized (PhpSharabilityQuery.this) {
                    if (delegate == null) {
                        delegate = createDelegate();
                    }
                    return delegate.getSharability(uri);
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
            delegate = null;
        }
    }

    private SharabilityQueryImplementation2 createDelegate() {
        String[] srcProps = sources.getRootProperties();
        String[] testProps = tests.getRootProperties();
        String[] seleniumProps = selenium.getRootProperties();

        int size = srcProps.length;
        size += testProps.length;
        size += seleniumProps.length;

        List<String> props = new ArrayList<>(size);

        for (String src : srcProps) {
            props.add("${" + src + "}"); // NOI18N
        }
        for (String test : testProps) {
            props.add("${" + test + "}"); // NOI18N
        }
        for (String test : seleniumProps) {
            props.add("${" + test + "}"); // NOI18N
        }

        return helper.createSharabilityQuery2(evaluator, props.toArray(new String[0]), new String[0]);
    }

}
