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

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

/**
 * Data structure (model) of results of test results.
 * The data are built by the {@link TestRecognizer}.
 * 
 * <i>This is a modified version for <code>o.n.m.junit.output.Report</code>.</i>
 *
 * @author  Marian Petras, Erno Mononen
 */
public final class Report {

    private String suiteClassName;
    /**
     * number of recognized (by the parser) passed test reports
     */
    private int totalTests;
    private int passed;
    private int passedWithErrors;
    private int failures;
    private int errors;
    private int pending;
    private int skippedNum;
    private int abortedNum;
    private long elapsedTimeMillis;
    private int detectedPassedTests;
    private Collection<Testcase> tests;
    private FileLocator fileLocator;
    private WeakReference<Project> project;
    private final URI projectURI;
    private boolean aborted;
    private boolean skipped;

    private boolean completed;

    /**
     * @param suiteClassName name of the suite class this report is build for
     * @param project project this report is build for
     */
    public Report(String suiteClassName, Project project) {
        this.suiteClassName = suiteClassName;
        this.project = new WeakReference<Project>(project);
        this.projectURI = project.getProjectDirectory().toURI();
        this.fileLocator = project.getLookup().lookup(FileLocator.class);
        this.tests = new ArrayList<Testcase>(10);
        this.completed = true;
        this.aborted = false;
        this.skipped = false;
    }

    public boolean isCompleted() {
      return completed;
    }

    public void setCompleted(boolean completed) {
      this.completed = completed;
    }

    public FileLocator getFileLocator() {
        return fileLocator;
    }

    public Project getProject() {
        Project prj = project.get();
        if (prj == null) {
            prj = FileOwnerQuery.getOwner(projectURI);
	    assert prj != null : "Project was null for projectURI: " + projectURI; //NOI18N
            project = new WeakReference<Project>(prj);
        }
        return prj;
    }

    /**
     * @param test the {@link Testcase} that will be added to this report
     */
    public void reportTest(Testcase test) {
        
        //PENDING - should be synchronized
        tests.add(test);
        
        if (!Status.isFailureOrError(test.getStatus()) && !Status.isSkipped(test.getStatus()) && !Status.isAborted(test.getStatus())) {
            detectedPassedTests++;
        }
    }
    
    /**
     * @param report update this {@link Report}
     */
    public void update(Report report) {
        synchronized(this){
            this.suiteClassName = report.suiteClassName;
            this.totalTests = report.totalTests;
            this.passed = report.passed;
            this.passedWithErrors = report.passedWithErrors;
            this.failures = report.failures;
            this.errors = report.errors;
            this.pending = report.pending;
            this.elapsedTimeMillis = report.elapsedTimeMillis;
            this.detectedPassedTests = report.detectedPassedTests;
            this.tests = report.tests;
            this.completed = report.completed;
            this.skipped = report.skipped;
            this.skippedNum = report.skippedNum;
            this.abortedNum = report.abortedNum;
        }
    }
    
    public Status getStatus() {
        if (abortedNum > 0){
            return Status.ABORTED;
        } else if (errors > 0) {
            return Status.ERROR;
        } else if (failures > 0) {
            return Status.FAILED;
        } else if (skippedNum > 0) {
            return Status.SKIPPED;
        } else if (pending > 0) {
            return Status.PENDING;
        } else if (passedWithErrors > 0) {
            return Status.PASSEDWITHERRORS;
        }
        return Status.PASSED;
    }
    
    /**
     * @return all {@link Testcase}s already added to this report
     */
    public Collection<Testcase> getTests() {
        
        /*
         * May be called both from the EventDispatch thread and
         * from other threads!
         *
         * TestSuiteNodeChildren.setFiltered() ... EventDispatch thread
         * TestSuiteNodeChildren.addNotify() ... EventDispatch thread or else
         */
        
        //PENDING - should be synchronized
        if (tests.isEmpty()) {
            final Collection<Testcase> emptyList = Collections.emptyList();
            return emptyList;
        } else {
            return new ArrayList<Testcase>(tests);
        }
    }
    
    /**
     * @return {@code true} if this report contains any failures or errors, {@code false} otherwise
     */
    public boolean containsFailed() {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        return (failures + errors) != 0;
    }

    /**
     * @return the suiteClassName
     */
    public String getSuiteClassName() {
        return suiteClassName;
    }

    /**
     * @param suiteClassName the suiteClassName to set
     */
    public void setSuiteClassName(String suiteClassName) {
        this.suiteClassName = suiteClassName;
    }

    /**
     * @return the totalTests
     */
    public int getTotalTests() {
        return totalTests;
    }

    /**
     * @param totalTests the totalTests to set
     */
    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    /**
     * @return the passed
     */
    public int getPassed() {
        return passed;
    }

    /**
     * @param passed the passed to set
     */
    public void setPassed(int passed) {
        this.passed = passed;
    }

    /**
     * @return the passedWithErrors
     */
    public int getPassedWithErrors() {
        return passedWithErrors;
    }

    /**
     * @param passedWithErrors the passedWithErrors to set
     */
    public void setPassedWithErrors(int passedWithErrors) {
        this.passedWithErrors = passedWithErrors;
    }

    /**
     * @return the the number of skipped tests
     */
    public int getSkipped() {
        return skippedNum;
    }

    /**
     * @param skipped the number of skipped tests to set
     */
    public void setSkipped(int skipped) {
        this.skippedNum = skipped;
    }

    /**
     * @return the the number of aborted tests
     */
    public int getAborted() {
        return abortedNum;
    }

    /**
     * @param aborted the number of aborted tests to set
     */
    public void setAborted(int aborted) {
        this.abortedNum = aborted;
    }

    /**
     * @return the failures
     */
    public int getFailures() {
        return failures;
    }

    /**
     * @param failures the failures to set
     */
    public void setFailures(int failures) {
        this.failures = failures;
    }

    /**
     * @return the errors
     */
    public int getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(int errors) {
        this.errors = errors;
    }

    /**
     * @return the pending
     */
    public int getPending() {
        return pending;
    }

    /**
     * @param pending the pending to set
     */
    public void setPending(int pending) {
        this.pending = pending;
    }

    /**
     * @return the elapsedTimeMillis
     */
    public long getElapsedTimeMillis() {
        return elapsedTimeMillis;
    }

    /**
     * @param elapsedTimeMillis the elapsedTimeMillis to set
     */
    public void setElapsedTimeMillis(long elapsedTimeMillis) {
        this.elapsedTimeMillis = elapsedTimeMillis;
    }

    /**
     * @return the detectedPassedTests
     */
    public int getDetectedPassedTests() {
        return detectedPassedTests;
    }

    /**
     * @param detectedPassedTests the detectedPassedTests to set
     */
    public void setDetectedPassedTests(int detectedPassedTests) {
        this.detectedPassedTests = detectedPassedTests;
    }

    /**
     * @param tests the tests to set
     */
    public void setTests(Collection<Testcase> tests) {
        this.tests = tests;
    }

    /**
     * @param fileLocator the fileLocator to set
     */
    public void setFileLocator(FileLocator fileLocator) {
        this.fileLocator = fileLocator;
    }

    /**
     * @param project the project to set
     */
    public void setProject(Project project) {
        this.project = new WeakReference<Project>(project);
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    public int getStatusMask(){
        int statusMask = 0;
        statusMask |= getPassed() > 0 ? Status.PASSED.getBitMask() : 0;
        statusMask |= getPassedWithErrors() > 0 ? Status.PASSEDWITHERRORS.getBitMask() : 0;
        statusMask |= getFailures() > 0 ? Status.FAILED.getBitMask() : 0;
        statusMask |= getErrors() > 0 ? Status.ERROR.getBitMask() : 0;
        statusMask |= getSkipped() > 0 ? Status.SKIPPED.getBitMask() : 0;
        statusMask |= getAborted() > 0 ? Status.ABORTED.getBitMask() : 0;
        return statusMask;
    }
}
