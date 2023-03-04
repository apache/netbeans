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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;
import org.openide.windows.OutputListener;

/**
 * Converted line prepared for output window. Wraps the line itself and
 * corresponding listener (if any).
 *
 * @author Petr Hejl
 */
public final class ConvertedLine {

    private final String text;

    private final OutputListener listener;

    private ConvertedLine(String text, OutputListener listener) {
        assert text != null;

        this.text = text;
        this.listener = listener;
    }

    /**
     * Returns the converted line presenting the given text and with the given
     * listener registered when it is diplayed.
     *
     * @param text line text
     * @param listener line listener to register, may be <code>null</code>
     * @return converted line
     */
    @NonNull
    public static ConvertedLine forText(@NonNull String text, @NullAllowed OutputListener listener) {
        Parameters.notNull("text", text);
        return new ConvertedLine(text, listener);
    }

    /**
     * Returns the text to display.
     *
     * @return the text to display
     */
    @NonNull
    public String getText() {
        return text;
    }

    /**
     * Returns the corresponding listener for actions taken on the line
     * or <code>null</code>.
     *
     * @return the corresponding listener for actions taken on the line
     *             or <code>null</code>
     */
    @CheckForNull
    public OutputListener getListener() {
        return listener;
    }

}
