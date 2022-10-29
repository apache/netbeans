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

package org.netbeans.modules.gradle.tooling;

import java.io.Serializable;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.gradle.internal.exceptions.LocationAwareException;
import org.gradle.internal.exceptions.MultiCauseException;
import org.gradle.internal.resolve.ModuleVersionResolveException;

/**
 *
 * @author Laszlo Kishalmi
 */
public class NbProjectInfoModel extends BaseModel implements NbProjectInfo {

    private final Map<String, Object> info = new HashMap<>();
    private final Map<String, Object> ext = new HashMap<>();
    private final Set<String> problems = new LinkedHashSet<>();
    private final Set<Report> reports = new LinkedHashSet<>();
    private boolean miscOnly = false;

    public NbProjectInfoModel() {
        ext.put("perf", new LinkedHashMap());
    }

    @Override
    public Map<String, Object> getInfo() {
        return info;
    }

    @Override
    public Map<String, Object> getExt() {
        return ext;
    }

    @Override
    public Set<String> getProblems() {
        return problems;
    }

    @Override
    public Set<Report> getReports() {
        return reports;
    }

    void noteProblem(String s) {
        problems.add(s);
    }
    
    /**
     * Notes a problem reported as an throwable. Unlike String problems, throwables may contain annotation or even wrap multiple
     * error causes. This may be useful for the IDE to process, i.e. in a form of hints. The Report structure does not contain stacktrace 
     * yet, but contains exception class as a data for possible matching.
     * @param e reported issue as a Throwable.
     */
    void noteProblem(Throwable e) {
        if (e instanceof MultiCauseException && !(e instanceof ModuleVersionResolveException)) {
            // only handle wrapper multi-causes. They may appear in the middle of the cause chain, too, but
            // it's not yet obvious if the multi-cause errors are actually useful.
            MultiCauseException mce = (MultiCauseException)e;
            for (Throwable t : mce.getCauses()) {
                Report r = createReport(t, false);
                if (r != null) {
                    ReportImpl outer = createReport(e, true);
                    outer.addCause(r);
                    reports.add(outer);
                }
            }
        } else {
            Report r = createReport(e, false);
            if (r != null) {
                reports.add(r);
            }
        }
    }
    
    /**
     * Replicates the Throwable into a Report. If shallow is false, replicates
     * the whole cause chain into a chain of Reports.
     * @param e throwable to encode
     * @param shallow if true, will encode just the throwable, not its chain
     * @return created Report
     */
    private ReportImpl createReport(Throwable e, boolean shallow) {
        if (e == null) {
            return null;
        }

        ReportImpl report;
        Throwable reported = e;
        
        if (e instanceof LocationAwareException) {
            LocationAwareException lae = (LocationAwareException)e;
            reported = lae.getCause();
            report = new ReportImpl(reported.getClass().getName(), lae.getLocation(), lae.getLineNumber(), reported.getMessage());
        } else {
            report = new ReportImpl(reported.getClass().getName(), null, -1, e.getMessage());
            reported = e;
        }
        if (shallow) {
            return report;
        }
        if (e.getCause() != null && e.getCause() != reported) {
            Report nested = createReport(e.getCause(), false);
            if (nested != null) {
                report.addCause(nested);
            }
        }
        return report;
    }

    public void setMiscOnly(boolean miscOnly) {
        this.miscOnly = miscOnly;
    }

    @Override
    public boolean getMiscOnly() {
        return miscOnly;
    }

    public void registerPerf(String name, Object runtime) {
        ((LinkedHashMap<String,Object>) ext.get("perf")).put(name, runtime);
    }
    
    static class ReportImpl implements Serializable, Report {
        private final String errorClass;
        private final String scriptLocation;
        private final int lineNumber;
        private final String message;
        private Report cause;

        public ReportImpl(String errorClass, String scriptLocation, int lineNumber, String message) {
            this.errorClass = errorClass;
            this.scriptLocation = scriptLocation;
            this.lineNumber = lineNumber;
            this.message = message;
        }
        
        public String getErrorClass() {
            return errorClass;
        }

        public String getScriptLocation() {
            return scriptLocation;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getMessage() {
            return message;
        }

        public void addCause(Report cause) {
            this.cause = cause;
        }

        public Report getCause() {
            return cause;
        }
    }
}
