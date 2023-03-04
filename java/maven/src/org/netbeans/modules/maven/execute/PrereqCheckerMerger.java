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

package org.netbeans.modules.maven.execute;

import java.util.Collection;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.cos.CosChecker;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;

/**
 * a PrerequisitesChecker lookupMerger, for now will just put the CoS implementation at the end
 * of the list.
 * @author mkleint
 */
@LookupMerger.Registration(projectType="org-netbeans-modules-maven")
public class PrereqCheckerMerger implements LookupMerger<PrerequisitesChecker> {

    @Override
    public Class<PrerequisitesChecker> getMergeableClass() {
        return PrerequisitesChecker.class;
    }

    @Override
    public PrerequisitesChecker merge(Lookup lookup) {
        Lookup.Result<PrerequisitesChecker> res = lookup.lookupResult(PrerequisitesChecker.class);
        return new Impl(res);
    }

    private static class Impl implements PrerequisitesChecker {

        Lookup.Result<PrerequisitesChecker> checkers;
        public Impl(Lookup.Result<PrerequisitesChecker> res) {
            checkers = res;
        }

        @Override
        public boolean checkRunConfig(RunConfig config) {
            Collection<? extends PrerequisitesChecker> all = checkers.allInstances();
            PrerequisitesChecker cos = null;
            for (PrerequisitesChecker check : all) {
                if (check instanceof CosChecker) {
                    cos = check;
                    continue;
                }
                if (!check.checkRunConfig(config)) {
                    return false;
                }
            }
            if (cos != null && !cos.checkRunConfig(config)) {
                return false;
            }
            return true;
        }

    }

}
