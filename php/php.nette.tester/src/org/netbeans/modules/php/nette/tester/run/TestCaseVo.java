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
package org.netbeans.modules.php.nette.tester.run;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.spi.testing.run.TestCase;

public final class TestCaseVo {

    private final String name;

    private TestCase.Status status;
    private String file;
    private int line = -1;
    private long time = -1;
    private String message;
    private List<String> stackTrace = Collections.emptyList();
    private TestCase.Diff diff;


    public TestCaseVo(String name) {
        assert name != null;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public TestCase.Status getStatus() {
        assert status != null;
        return status;
    }

    public void setStatus(TestCase.Status status) {
        this.status = status;
    }

    @CheckForNull
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public long getTime() {
        assert time != -1;
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @CheckForNull
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }

    @CheckForNull
    public TestCase.Diff getDiff() {
        return diff;
    }

    public void setDiff(TestCase.Diff diff) {
        this.diff = diff;
    }

    @Override
    public String toString() {
        return "TestCaseVo{" + "name=" + name + ", status=" + status + '}'; // NOI18N
    }

}
