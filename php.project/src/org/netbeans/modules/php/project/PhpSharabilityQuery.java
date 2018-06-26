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

        return helper.createSharabilityQuery2(evaluator, props.toArray(new String[props.size()]), new String[0]);
    }

}
