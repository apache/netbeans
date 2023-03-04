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

package org.netbeans.modules.bugzilla.kenai;

import java.util.Collection;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.query.QueryController;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiQuery extends BugzillaQuery {
    private final String product;
    private boolean predefinedQuery = false;

    public KenaiQuery(String name, BugzillaRepository repository, String urlParameters, String product, boolean saved, boolean predefined) {
        super(name, repository, urlParameters, saved, false, false);
        this.product = product;
        this.predefinedQuery = predefined;
        this.lastRefresh = BugzillaConfig.getInstance().getLastQueryRefresh(repository, getStoredQueryName());
        controller = createControler(repository, this, urlParameters);
    }

    @Override
    protected QueryController createControler(BugzillaRepository r, BugzillaQuery q, String parameters) {
        KenaiQueryController c = new KenaiQueryController(r, q, parameters, product, predefinedQuery);
        return c;
    }

    void setUrlParameters(String urlParameters) {
        super.urlParameters = urlParameters;
    }

    @Override
    protected void logQueryEvent(int count, boolean autoRefresh) {
        LogUtils.logQueryEvent(
            BugzillaConnector.getConnectorName(),
            getDisplayName(),
            count,
            true,
            autoRefresh);
    }

    @Override
    public String getStoredQueryName() {
        return super.getStoredQueryName() + "-" + product;
    }

    @Override
    public boolean canRemove() {
        return predefinedQuery ? false : super.canRemove();
    }
}
