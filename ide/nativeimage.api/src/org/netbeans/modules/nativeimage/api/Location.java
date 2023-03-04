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
package org.netbeans.modules.nativeimage.api;

/**
 * A location in the debuggee program.
 *
 * @since 0.2
 */
public final class Location {

    private final long pc;
    private final int line;
    private final int column;

    private Location(long pc, int line, int column) {
        this.pc = pc;
        this.line = line;
        this.column = column;
    }

    /**
     * Get the program counter position.
     *
     * @since 0.2
     */
    public long getPC() {
        return pc;
    }

    /**
     * Get 1-based line number. Returns 0 when the line is not defined.
     *
     * @since 0.2
     */
    public int getLine() {
        return line;
    }

    /**
     * Get 1-based column number. Returns 0 when the column is not defined.
     *
     * @since 0.2
     */
    public int getColumn() {
        return column;
    }

    /**
     * Creates a builder to build a new {@link Location}.
     *
     * @since 0.2
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Location{" + "pc=" + pc + ", line=" + line + ", column=" + column + '}';
    }

    /**
     * Location's builder.
     *
     * @since 0.2
     */
    public static final class Builder {

        private long pc;
        private int line;
        private int column;

        Builder() {}

        /**
         * Set a program counter location in the native binary.
         *
         * @since 0.2
         */
        public void pc(long pc) {
            this.pc = pc;
        }

        /**
         * Set an 1-based line. The line is treated as unknown when 0.
         *
         * @since 0.2
         */
        public void line(int line) {
            if (line < 0) {
                throw new IllegalArgumentException("Line must not be negative");
            }
            this.line = line;
        }

        /**
         * Set an 1-based column. The column is treated as unknown when 0.
         *
         * @since 0.2
         */
        public void column(int column) {
            if (column < 0) {
                throw new IllegalArgumentException("Column must not be negative");
            }
            this.column = column;
        }

        /**
         * Build the {@link Location} object.
         *
         * @since 0.2
         */
        public Location build() {
            if (column > 0 && line == 0) {
                throw new IllegalStateException("Column can not be defined without a line.");
            }
            if (line == 0 && pc == 0) {
                throw new IllegalStateException("No location information is defined.");
            }
            return new Location(pc, line, column);
        }
    }
}
