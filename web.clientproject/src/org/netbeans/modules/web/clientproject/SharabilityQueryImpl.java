/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        return helper.createSharabilityQuery2(evaluator, props.toArray(new String[props.size()]), new String[0]);
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
