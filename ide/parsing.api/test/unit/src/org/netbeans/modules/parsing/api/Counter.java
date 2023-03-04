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
package org.netbeans.modules.parsing.api;

class Counter {

    private int count = 1;
    private int maxCount;
    private String errorMessage = null;

    public Counter (int maxCount) {
        this.maxCount = maxCount;
    }
    
    public void check (int c) {
        check (c, false);
    }

    public void wait (int c) {
        check (c, true);
    }
    
    public synchronized void check (final int c, final boolean wait) {
        while (true) {
            if (errorMessage != null) {
                return;
            }
            if (c == maxCount) {
                notifyAll ();
                return;
            }
            if (wait && count < c)
                try {
                    wait ();
                    continue;
                } catch (InterruptedException ex) {
                    ex.printStackTrace ();
                }
            if (c != count) {
                errorMessage = "expected " + c + ", but was " + count;
                notifyAll ();
                return;
            }
            count ++;
            notifyAll ();
            return;
        }
    }

    public synchronized void check (String expected, String current) {
        if (errorMessage != null)
            return;
        if (!expected.equals (current)) {
            errorMessage = "expected " + expected + ", but was " + current;
            notify ();
            return;
        }
    }

    public synchronized String errorMessage (boolean wait) throws InterruptedException {
        while (true) {
            if (errorMessage != null) 
                return errorMessage;
            if (count == maxCount) return null;
            if (wait)
                wait ();
            else
                return errorMessage;
        }
    }

    public int count () {
        return count;
    }
}



