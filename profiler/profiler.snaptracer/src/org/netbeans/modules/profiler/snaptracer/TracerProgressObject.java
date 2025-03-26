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

package org.netbeans.modules.profiler.snaptracer;

import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;

/**
 * TracerProgressObject describes progress of the TracerPackage/TracerProbe
 * initialization when starting a Tracer session.
 *
 * @author Jiri Sedlacek
 */
public final class TracerProgressObject {

    private final int steps;
    private String text;
    private int currentStep;
    private int lastStep;

    private final Set<Listener> listeners;


    /**
     * Creates new instance of TracerProgressObject with a defined number of
     * steps.
     *
     * @param steps number of steps to finish the initialization
     */
    public TracerProgressObject(int steps) {
        this(steps, null);
    }

    /**
     * Creates new instance of TracerProgressObject with a defined number of
     * steps and text describing the initial state.
     *
     * @param steps number of steps to finish the initialization
     * @param text text describing the initial state
     */
    public TracerProgressObject(int steps, String text) {
        if (steps < 1)
            throw new IllegalArgumentException("steps value must be >= 1: " + steps); // NOI18N

        this.steps = steps;
        this.text = text;
        currentStep = 0;
        lastStep = 0;
        listeners = new HashSet<>();
    }


    /**
     * Returns number of steps to finish the initialization.
     *
     * @return number of steps to finish the initialization
     */
    public synchronized int getSteps() { return steps; }

    /**
     * Returns current step of the initialization progress.
     *
     * @return current step of the initialization progress
     */
    public synchronized int getCurrentStep() { return currentStep; }

    /**
     * Returns text describing the current state or null.
     *
     * @return text describing the current state or null
     */
    public synchronized String getText() { return text; }


    /**
     * Adds a single step to the current initialization progress.
     */
    public void addStep() { addSteps(1); }

    /**
     * Adds a single step to the current initialization progress and changes
     * the text describing the current state.
     *
     * @param text text describing the current state
     */
    public void addStep(String text)  { addSteps(1, text); }

    /**
     * Adds a number of steps to the current initialization progress.
     *
     * @param steps number of steps to be addded to the current initialization progress
     */
    public void addSteps(int steps) { addSteps(steps, text); }

    /**
     * Adds a number of steps to the current initialization progress and changes
     * the text describing the current state.
     *
     * @param steps number of steps to be addded to the current initialization progress
     * @param text text describing the current state
     */
    public synchronized void addSteps(int steps, String text) {
        if (steps < 0)
            throw new IllegalArgumentException("steps value must be >= 0: " + steps); // NOI18N
        if (currentStep + steps > this.steps)
            throw new IllegalArgumentException("Total steps exceeded: " + // NOI18N
                                               (currentStep + steps) + ">" + this.steps); // NOI18N

        currentStep += steps;
        this.text = text;
        fireChange();
    }

    /**
     * Updates text describing the current state without adding any steps to the
     * current initialization progress.
     *
     * @param text text describing the current state
     */
    public synchronized void setText(String text) {
        this.text = text;
        fireChange();
    }

    /**
     * Adds all remaining steps to finish the initialization progress.
     */
    public synchronized void finish() {
        if (isFinished()) return;
        currentStep = steps;
        fireChange();
    }

    /**
     * Returns true for a finished TracerProgressObject, false otherwise.
     *
     * @return true for a finished TracerProgressObject, false otherwise.
     */
    public synchronized boolean isFinished() {
        return currentStep == steps;
    }


    /**
     * Adds a listener to receive progress notifications.
     *
     * @param l listener to be added
     */
    public synchronized void addListener(Listener l) { listeners.add(l); }

    /**
     * Removes a listener receiving progress notifications.
     *
     * @param l listener to be removed.
     */
    public synchronized void removeListener(Listener l) { listeners.remove(l); }

    private void fireChange() {
        final int currentStepF = currentStep;
        final int addedStepsF = currentStep - lastStep;
        final String textF = text;
        final Set<Listener> toNotify = new HashSet<>(listeners);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Listener listener : toNotify)
                    listener.progressChanged(addedStepsF, currentStepF, textF);
            }
        });
        lastStep = currentStep;
    }


    /**
     * Listener to receive notifications about the initialization progress.
     */
    public static interface Listener {

        /**
         * Invoked when the progress and/or text describing the current state
         * changes.
         *
         * @param addedSteps new steps added by the change
         * @param currentStep current step of the initialization progress
         * @param text text describing the current state
         */
        public void progressChanged(int addedSteps, int currentStep, String text);

    }

}
