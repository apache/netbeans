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
package org.netbeans.test.j2ee.multiview;

/**
 * @author pfiala
 */
public abstract class StepIterator {

    private Exception error = null;
    private long startTime;
    private long duration;
    private boolean success = false;

    public StepIterator() {
        this(1000, 20000);
    }

    public StepIterator(int stepDuration, int timeout) {
        iterate(stepDuration, timeout);
    }

    public abstract boolean step() throws Exception;

    public void finalCheck() {
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getError() {
        return error;
    }

    public long getDuration() {
        return duration;
    }

    private void iterate(long stepDuration, long timeout) {
        startTime = System.currentTimeMillis();
        for (;;) {
            try {
                error = null;
                success = step();
                if (success) {
                    break;
                }
            } catch (Exception e) {
                error = e;
            }
            duration = System.currentTimeMillis() - startTime;
            if (duration > timeout) {
                break;
            }
            try {
                Thread.sleep(stepDuration);
            } catch (InterruptedException ex) {
            }
        }
        finalCheck();
    }
}
