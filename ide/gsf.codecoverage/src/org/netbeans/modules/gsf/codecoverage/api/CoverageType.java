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
package org.netbeans.modules.gsf.codecoverage.api;

/**
 * Information about the type of coverage for a given line.
 *
 * @author Tor Norbye
 */
public enum CoverageType {
    /**
     * The line was touched 1 or more times.
     */
    COVERED("Covered"),
    /**
     * The line was never touched.
     */
    NOT_COVERED("Not Covered"),
    /**
     * The line may have been touched, not sure. Typically, comments and whitespace between executed
     * statements fall into this category.
     */
    INFERRED("Inferred"),
    /**
     * Parts of the line were touched, and other parts were not. This happens for example when you
     * have conditional statements or multiple statements on the line and not all parts were
     * executed.
     */
    PARTIAL("Partial"),
    /**
     * We have no information about this line
     */
    UNKNOWN("Unknown");

    private final String desc;

    private CoverageType(String desc) {
        this.desc = desc;
    }

    public String getDescription() {
        return desc;
    }
}
