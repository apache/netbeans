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

package org.netbeans.api.extexecution.print;

import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Converts the line to lines for output window. Convertor may change number
 * of lines and add listener to line (invoked when the line is clicked).
 *
 * @author Petr Hejl
 * @see org.netbeans.api.extexecution.input.InputProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)
 * @see org.netbeans.api.extexecution.input.LineProcessors#printing(org.openide.windows.OutputWriter, org.netbeans.api.extexecution.print.LineConvertor, boolean)
 */
public interface LineConvertor {

    /**
     * Converts the line to lines for output window. Method may return
     * no line or any other number of lines for single input line. If
     * the method returns <code>null</code> the line was not handled by the
     * convertor.
     *
     * @param line input line to convert
     * @return converted lines for output window or <code>null</code>
     *             if the convertor does not handle line at all
     * @see ConvertedLine
     */
    @CheckForNull
    List<ConvertedLine> convert(@NonNull String line);

}
