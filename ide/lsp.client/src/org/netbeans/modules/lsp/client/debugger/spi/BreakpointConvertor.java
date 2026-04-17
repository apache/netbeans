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
package org.netbeans.modules.lsp.client.debugger.spi;

import java.net.URI;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.lsp.client.debugger.LineBreakpointData;
import org.netbeans.modules.lsp.client.debugger.SPIAccessor;

/**Convert language-specific breakpoints to format usable by the DAP debugger.
 *
 * Implementations should inspect the provided {@code Breakpoint}, and if they
 * recognize it, and there's a corresponding method in the provided
 * {@code ConvertedBreakpointConsumer} instance, the method should be called.
 *
 * The implementations should be registered in the global {@code Lookup}.
 *
 * @since 1.29
 */
public interface BreakpointConvertor {
    /**
     * Inspect the provided {@code Breakpoint}, and call an appropriate method
     * on the provided {@code ConvertedBreakpointConsumer} if possible.
     *
     * @param b the breakpoint to inspect
     * @param breakpointConsumer the consumer of which the appropriate method
     *                           should be invoked
     */
    public void convert(org.netbeans.api.debugger.Breakpoint b,
                       ConvertedBreakpointConsumer breakpointConsumer);

    /**
     * Set of callbacks for converted breakpoints.
     */
    public static class ConvertedBreakpointConsumer {
        private final List<LineBreakpointData> lineBreakpoints;

        ConvertedBreakpointConsumer(List<LineBreakpointData> lineBreakpoints) {
            this.lineBreakpoints = lineBreakpoints;
        }

        /**Report a line-based breakpoint, with the given properties
         *
         * @param uri the location of the file where the breakpoint is set
         * @param lineNumber the line number on which the breakpoint is set
         * @param condition an optional condition expression - the the debugger
         *                  will only stop if this evaluates to a language-specific
         *                  {@code true} representation; may be {@code null}
         */
        public void lineBreakpoint(@NonNull URI uri, int lineNumber, @NullAllowed String condition) {
            lineBreakpoints.add(new LineBreakpointData(uri, lineNumber, condition));
        }

        static {
            SPIAccessor.setInstance(new SPIAccessor() {
                @Override
                public ConvertedBreakpointConsumer createConvertedBreakpointConsumer(List<LineBreakpointData> lineBreakpoints) {
                    return new ConvertedBreakpointConsumer(lineBreakpoints);
                }
            });
        }
    }
}
