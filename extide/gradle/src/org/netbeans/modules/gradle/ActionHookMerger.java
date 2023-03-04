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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.actions.AfterBuildActionHook;
import org.netbeans.modules.gradle.spi.actions.BeforeBuildActionHook;
import org.netbeans.modules.gradle.spi.actions.BeforeReloadActionHook;
import java.io.PrintWriter;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
final class ActionHookMerger implements BeforeBuildActionHook, BeforeReloadActionHook, AfterBuildActionHook {

    final Lookup lookup;

    public ActionHookMerger(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public Lookup beforeAction(String action, Lookup context, PrintWriter out) {
        Lookup ctx = context;
        for (BeforeBuildActionHook hook : lookup.lookupAll(BeforeBuildActionHook.class)) {
            ctx = hook.beforeAction(action, ctx, out);
        }
        return ctx;
    }

    @Override
    public boolean beforeReload(String action, Lookup context, int result, PrintWriter out) {
        boolean ret = true;
        for (BeforeReloadActionHook hook : lookup.lookupAll(BeforeReloadActionHook.class)) {
            ret &= hook.beforeReload(action, context, result, out);
        }
        return ret;
    }

    @Override
    public void afterAction(String action, Lookup context, int result, PrintWriter out) {
        for (AfterBuildActionHook hook : lookup.lookupAll(AfterBuildActionHook.class)) {
            hook.afterAction(action, context, result, out);
        }
    }

    @LookupMerger.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
    public static class BeforeBuildActionHookMerger implements LookupMerger<BeforeBuildActionHook> {

        @Override
        public Class<BeforeBuildActionHook> getMergeableClass() {
            return BeforeBuildActionHook.class;
        }

        @Override
        public BeforeBuildActionHook merge(Lookup lookup) {
            return new ActionHookMerger(lookup);
        }

    }

    @LookupMerger.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
    public static class BeforeReloadActionHookMerger implements LookupMerger<BeforeReloadActionHook> {

        @Override
        public Class<BeforeReloadActionHook> getMergeableClass() {
            return BeforeReloadActionHook.class;
        }

        @Override
        public BeforeReloadActionHook merge(Lookup lookup) {
            return new ActionHookMerger(lookup);
        }

    }

    @LookupMerger.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
    public static class AfterBuildActionHookMerger implements LookupMerger<AfterBuildActionHook> {

        @Override
        public Class<AfterBuildActionHook> getMergeableClass() {
            return AfterBuildActionHook.class;
        }

        @Override
        public AfterBuildActionHook merge(Lookup lookup) {
            return new ActionHookMerger(lookup);
        }

    }
}
