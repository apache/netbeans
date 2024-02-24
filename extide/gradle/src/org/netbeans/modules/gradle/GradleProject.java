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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleProject implements Serializable, Lookup.Provider {

    final Quality quality;
    final long evaluationTime = System.currentTimeMillis();
    final Lookup lookup;
    final GradleBaseProject baseProject;

    @SuppressWarnings("rawtypes")
    public GradleProject(Quality quality, Set<GradleReport> problems, Collection infos) {
        this.quality = quality;
        Set<GradleReport> probs = new LinkedHashSet<>();
        for (GradleReport prob : problems) {
            if (prob != null) probs.add(prob);
        }
        InstanceContent ic = new InstanceContent();
        for (Object i : infos) {
            ic.add(i);
        }
        lookup = new AbstractLookup(ic);

        baseProject = lookup.lookup(GradleBaseProject.class);
        assert baseProject != null : "GradleProject always shall have a GradleBaseProject in it's lookup!";
        setProblems(baseProject, probs);
    }

    private GradleProject(Quality quality, GradleReport[] problems, GradleProject origin) {
        this.quality = quality;
        Set<GradleReport> probs = new LinkedHashSet<>();
        for (GradleReport prob : problems) {
            if (prob != null) probs.add(prob);
        }
        lookup = origin.lookup;
        baseProject = lookup.lookup(GradleBaseProject.class);
        assert baseProject != null : "GradleProject always shall have a GradleBaseProject in it's lookup!";

        setProblems(baseProject, probs);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    Set<GradleReport> getProblems() {
        return baseProject.getProblems();
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
    
    /**
     * 
     * @since 2.23
     */
    public final GradleProject invalidate(GradleReport... problems) {
        if (getQuality().worseThan(Quality.EVALUATED)) {
            if (problems != null && problems.length > 0) {
                return new GradleProject(getQuality(), problems, this);
            } else {
                return this;
            }
        } else {
            return new GradleProject(Quality.EVALUATED, problems, this);
        }
    }

    public final GradleProject invalidate(String... reason) {
        GradleFiles gf = new GradleFiles(baseProject.getProjectDir(), true);
        Path scriptPath = gf.getBuildScript() != null ? gf.getBuildScript().toPath() : null;
        List<GradleReport> reports = new ArrayList<>();
        for (String s : reason) {
            reports.add(createGradleReport(scriptPath, s));
        }
        return invalidate(reports.toArray(new GradleReport[0]));
    }

    private static void setProblems(GradleBaseProject baseProject, Set<GradleReport> problems) {
        NbGradleProjectImpl.ACCESSOR.setProblems(baseProject, problems);
    }

    public static GradleReport createGradleReport(GradleReport.Severity severity, String errorClass, String location, int line, String message, 
            GradleReport causedBy, String... traceLines) {
        return NbGradleProjectImpl.ACCESSOR.createReport(severity, errorClass, location, line, message, causedBy, traceLines);
    }

    public static GradleReport createGradleReport(Path script, String message, String... detail) {
        return createGradleReport(GradleReport.Severity.ERROR, null, Objects.toString(script), -1, message, null, detail);
    }
}
