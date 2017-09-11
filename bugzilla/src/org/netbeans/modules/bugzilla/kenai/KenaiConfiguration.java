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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
