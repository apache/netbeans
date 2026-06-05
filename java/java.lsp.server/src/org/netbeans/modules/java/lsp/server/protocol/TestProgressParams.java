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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.util.Preconditions;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 *
 * @author Dusan Balek
 */
@SuppressWarnings("all")
public class TestProgressParams {

    /**
     * The test suite or the workspace folder the test suite belongs to.
     */
    @NonNull
    private String uri;

    /**
     * Information about a test suite being loaded, started, completed or skipped
     * during a test run.
     */
    @NonNull
    private TestSuiteInfo suite;

    public TestProgressParams() {
        this("", new TestSuiteInfo());
    }

    public TestProgressParams(@NonNull final String uri, @NonNull final TestSuiteInfo suite) {
        this.uri = Preconditions.checkNotNull(uri, "uri");
        this.suite = Preconditions.checkNotNull(suite, "suite");
    }

    /**
     * The test suite or the workspace folder the test suite belongs to.
     */
    @Pure
    @NonNull
    public String getUri() {
        return uri;
    }

    /**
     * The test suite or the workspace folder the test suite belongs to.
     */
    public void setUri(@NonNull final String uri) {
        this.uri = Preconditions.checkNotNull(uri, "uri");
    }

    /**
     * Information about a test suite being loaded, started, completed or skipped
     * during a test run.
     */
    @Pure
    @NonNull
    public TestSuiteInfo getSuite() {
        return suite;
    }

    /**
     * Information about a test suite being loaded, started, completed or skipped
     * during a test run.
     */
    public void setSuite(@NonNull final TestSuiteInfo suite) {
        this.suite = Preconditions.checkNotNull(suite, "suite");
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("uri", uri);
        b.add("suite", suite);
        return b.toString();
    }

    @Override
    @Pure
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.uri);
        hash = 59 * hash + Objects.hashCode(this.suite);
        return hash;
    }

    @Override
    @Pure
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestProgressParams other = (TestProgressParams) obj;
        if (!Objects.equals(this.uri, other.uri)) {
            return false;
        }
        if (!Objects.equals(this.suite, other.suite)) {
            return false;
        }
        return true;
    }
}
