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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiConfiguration extends BugzillaConfiguration {
    private List<String> products;
    private KenaiRepository repository;

    /** one instance for all kenai repositories on on each kenai site */
    private static Map<String, RepositoryConfiguration> rcs;

    public KenaiConfiguration(KenaiRepository repository, String product) {
        this.repository = repository;
        ArrayList<String> l = new ArrayList<String>();
        l.add(product);
        this.products = Collections.unmodifiableList(l);
    }

    @Override
    public List<String> getProducts() {
        if(!BugzillaUtil.isNbRepository(repository)) {
            return products;
        } else {
            return super.getProducts();
        }
    }

    @Override
    public List<String> getComponents(String product) {
        return super.getComponents(product);
    }

    @Override
    public List<String> getVersions(String product) {
        return super.getVersions(product);
    }

    void reset() {
        if(rcs  != null) {
            rcs.remove(repository.getUrl());
        }
    }

    @Override
    protected RepositoryConfiguration getRepositoryConfiguration(BugzillaRepository repository, boolean forceRefresh) {
        if(rcs == null) {
            rcs = new HashMap<String, RepositoryConfiguration>(1);
        }
        RepositoryConfiguration rc = rcs.get(repository.getUrl());
        if(rc == null || forceRefresh) {
            rc = super.getRepositoryConfiguration(repository, forceRefresh);
            rcs.put(repository.getUrl(), rc);
        }
        if(rc != null && (!forceRefresh && !hasProduct(rc))) {
            // mylyn is cashing the configuration for us so in case
            // forceRefresh=false and it doesn't contain the given project
            // we have to force refresh it one more time to get the needed
            // project data from the server
            forceRefresh = true;
            rc = super.getRepositoryConfiguration(repository, forceRefresh);
        }
        return rc;
    }

    private boolean hasProduct(RepositoryConfiguration rc) {
        List<String> knownProducts = rc.getProducts();
        for (String product : products) {
            if(!knownProducts.contains(product)) {
                return false;
            }
        }
        return true;
    }
}
