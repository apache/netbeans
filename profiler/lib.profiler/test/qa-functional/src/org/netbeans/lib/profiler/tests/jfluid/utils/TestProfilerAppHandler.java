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

package org.netbeans.lib.profiler.tests.jfluid.utils;

import org.netbeans.lib.profiler.client.AppStatusHandler;
import org.netbeans.lib.profiler.tests.jfluid.CommonProfilerTestCase;


public class TestProfilerAppHandler implements AppStatusHandler {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    CommonProfilerTestCase test;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public TestProfilerAppHandler(CommonProfilerTestCase t) {
        test = t;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public AsyncDialog getAsyncDialogInstance(String message, boolean showProgress, Runnable cancelHandler) {
        return new TestAsyncDialog();
    }

    public boolean confirmWaitForConnectionReply() {
        return false;
    }

    public void displayError(String msg) {
        test.log("\n!!!error");
        test.log("mesage=" + msg);
        test.getLog().flush();
        System.err.println("Error: " + msg);
        new Exception().printStackTrace();
        test.setStatus(CommonProfilerTestCase.STATUS_ERROR);
    }

    public void displayErrorAndWaitForConfirm(String msg) {
        test.getLog().flush();
        test.log("error");
        test.log("mesg=" + msg);
        System.err.println("Error: " + msg);
        new Exception().printStackTrace();
        test.setStatus(CommonProfilerTestCase.STATUS_ERROR);
    }

    public void displayErrorWithDetailsAndWaitForConfirm(String shortMsg, String detailsMsg) {
        test.getLog().flush();
        test.log("error");
        test.log("mesg=" + shortMsg);
        test.log("details=" + detailsMsg);
        System.err.println("Error: " + shortMsg + "; Details: " + detailsMsg);
        new Exception().printStackTrace();
        test.setStatus(CommonProfilerTestCase.STATUS_ERROR);
    }

    public void displayNotification(String msg) {
        test.log("notification: " + msg);
    }

    public void displayNotificationAndWaitForConfirm(String msg) {
        test.log("notification: " + msg);
    }

    public void displayNotificationWithDetailsAndWaitForConfirm(String shortMsg, String detailsMsg) {
        test.log("notification: " + shortMsg + ", detail: " + detailsMsg);
    }

    public void displayWarning(String msg) {
        test.log("warning: " + msg);
    }

    public void displayWarningAndWaitForConfirm(String msg) {
        test.log("warning: " + msg);
    }

    public void handleShutdown() {
        test.log("Handler shutdown");
        test.setStatus(CommonProfilerTestCase.STATUS_APP_FINISHED);
        test.waitForStatus(CommonProfilerTestCase.STATUS_MEASURED);
        test.log("Handled shutdown");
    }

    public void pauseLiveUpdates() {
        test.unsetStatus(CommonProfilerTestCase.STATUS_LIVERESULTS_AVAILABLE);
    }

    public void resultsAvailable() {
        test.log("Result Available");
        test.setStatus(CommonProfilerTestCase.STATUS_RESULTS_AVAILABLE);
    }

    public void resumeLiveUpdates() {
        test.setStatus(CommonProfilerTestCase.STATUS_LIVERESULTS_AVAILABLE);
    }

    public void takeSnapshot() {
        test.log("take snapshot");
    }
}
