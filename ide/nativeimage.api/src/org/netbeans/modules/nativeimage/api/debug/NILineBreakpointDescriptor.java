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
package org.netbeans.modules.nativeimage.api.debug;

import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Description of a line native breakpoint.
 *
 * @since 0.1
 */
public final class NILineBreakpointDescriptor {

    private final String filePath;
    private final int line;
    private final boolean enabled;
    private final String condition;
    private final boolean hidden;

    private NILineBreakpointDescriptor(String filePath, int line, boolean enabled, String condition, boolean hidden) {
        this.filePath = filePath;
        this.line = line;
        this.enabled = enabled;
        this.condition = condition;
        this.hidden = hidden;
    }

    /**
     * Create a new line native breakpoint builder.
     *
     * @param filePath file path of the breakpoint
     * @param lineNumber 1-based line number
     * @since 0.1
     */
    public static Builder newBuilder(String filePath, int lineNumber) {
        return new Builder(filePath, lineNumber);
    }

    /**
     * Get path of the file.
     *
     * @since 0.1
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Get 1-based line number.
     *
     * @since 0.1
     */
    public int getLine() {
        return line;
    }

    /**
     * Check if the breakpoint is to be enabled.
     *
     * @since 0.1
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the breakpoint condition.
     *
     * @return the condition, or <code>null</code> when the breakpoint does not have any condition.
     * @since 0.1
     */
    @CheckForNull
    public String getCondition() {
        return condition;
    }

    /**
     * Check if the breakpoint is to be hidden (not user-visible).
     *
     * @since 0.1
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Builder of a line native breakpoint descriptor. The builder is reusable
     * and the built breakpoint descriptor can be used to update existing breakpoints.
     *
     * @since 0.1
     */
    public static final class Builder {

        private String filePath;
        private int line;
        private boolean enabled = true;
        private String condition;
        private boolean hidden = false;

        Builder(String filePath, int lineNumber) {
            this.filePath = filePath;
            this.line = lineNumber;
        }

        /**
         * Set a file path.
         *
         * @since 0.1
         */
        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        /**
         * Set a 1-based line number.
         *
         * @since 0.1
         */
        public Builder line(int lineNumber) {
            this.line = lineNumber;
            return this;
        }

        /**
         * Set a condition.
         *
         * @since 0.1
         */
        public Builder condition(String condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Set an enabled state of the breakpoint. The breakpoint is enabled by default.
         *
         * @since 0.1
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Set a hidden state of the breakpoint. Hidden breakpoints are not visible
         * to users. The breakpoint is not hidden by default.
         *
         * @since 0.1
         */
        public Builder hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        /**
         * Build the line breakpoint descriptor.
         */
        public NILineBreakpointDescriptor build() {
            return new NILineBreakpointDescriptor(filePath, line, enabled, condition, hidden);
        }
    }
}
