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

package org.netbeans.modules.gradle;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleProject implements Serializable, Lookup.Provider {

    final Set<String> problems;
    final Quality quality;
    final long evaluationTime = System.currentTimeMillis();
    final Lookup lookup;
    final GradleBaseProject baseProject;

    @SuppressWarnings("rawtypes")
    public GradleProject(Quality quality, Set<String> problems, Collection infos) {
        this.quality = quality;
        Set<String> probs = new LinkedHashSet<>();
        for (String prob : problems) {
            if (prob != null) probs.add(prob);
        }
        this.problems = probs;
        InstanceContent ic = new InstanceContent();
        for (Object i : infos) {
            ic.add(i);
        }
        lookup = new AbstractLookup(ic);
        baseProject = lookup.lookup(GradleBaseProject.class);
        assert baseProject != null : "GradleProject always shall have a GradleBaseProject in it's lookup!";
    }

    private GradleProject(Quality quality, Set<String> problems, GradleProject origin) {
        this.quality = quality;
        Set<String> probs = new LinkedHashSet<>();
        for (String prob : problems) {
            if (prob != null) probs.add(prob);
        }
        this.problems = probs;
        lookup = origin.lookup;
        baseProject = lookup.lookup(GradleBaseProject.class);
        assert baseProject != null : "GradleProject always shall have a GradleBaseProject in it's lookup!";
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public Set<String> getProblems() {
        return Collections.unmodifiableSet(problems);
    }

    public Quality getQuality() {
        return quality;
    }

    public long getEvaluationTime() {
        return evaluationTime;
    }

    @NonNull
    public GradleBaseProject getBaseProject() {
        return baseProject;
    }

    @Override
    public String toString() {
        return "GradleProject{" + "quality=" + quality + ", baseProject=" + baseProject + '}';
    }

    public final GradleProject invalidate(String... reasons) {
        Set<String> p = new LinkedHashSet<>(Arrays.asList(reasons));
        return new GradleProject(Quality.EVALUATED, p, this);
    }

}
