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

package org.netbeans.api.extexecution.base.input;

/**
 *
 * This class is <i>NotThreadSafe</i>.
 * @author Petr Hejl
 */
public class TestInputProcessor implements InputProcessor {

    private final boolean cleanBytesOnReset;

    private StringBuilder charsProcessed = new StringBuilder();

    private int resetCount = 0;

    private boolean closed;

    public TestInputProcessor(boolean cleanBytesOnReset) {
        this.cleanBytesOnReset = cleanBytesOnReset;
    }

    public void processInput(char[] chars) {
        charsProcessed.append(chars);
    }

    public void reset() {
        resetCount++;
        if (cleanBytesOnReset) {
            charsProcessed.setLength(0);
        }
    }

    public void close() {
        closed = true;
    }

    public char[] getCharsProcessed() {
        return charsProcessed.toString().toCharArray();
    }

    public int getResetCount() {
        return resetCount;
    }

    public boolean isClosed() {
        return closed;
    }

}
