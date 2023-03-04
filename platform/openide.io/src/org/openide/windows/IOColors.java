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

package org.openide.windows;

import java.awt.Color;
import org.openide.util.Lookup;

/**
 * Settings of colors for normal, error, hyperlink, important hyperlink lines.
 * Change is global for text past and future.
 * <p>
 * Client usage:
 * <pre>
 *  // set important hyperlink color to red
 *  InputOutput io = ...;
 *  IOColors.setColor(io, IOColors.OutputType.HYPERLINK_IMPORTANT, Color.RED);
 * </pre>
 * How to support {@link IOColors} in own {@link IOProvider} implementation:
 * <ul>
 *   <li> {@link InputOutput} provided by {@link IOProvider} has to implement {@link org.openide.util.Lookup.Provider}
 *   <li> Extend {@link IOColors} and implement its abstract methods
 *   <li> Place instance of {@link IOColors} to {@link Lookup} provided by {@link InputOutput}
 * </ul>
 * @see IOColorLines
 * @see IOColorPrint
 * @since 1.16
 * @author Tomas Holy
 */
public abstract class IOColors {

    private static IOColors find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOColors.class);
        }
        return null;
    }

    /**
     * output types
     */
    public enum OutputType {
        /** default output */
        OUTPUT,
        /** error output */
        ERROR,
        /** hyperlink */
        HYPERLINK,
        /** important hyperlink */
        HYPERLINK_IMPORTANT,
        /** input text
         * @since 1.39 */
        INPUT,
        /** Info about success. Change is not guaranteed to affect colored
         * output written in the past.
         * @since 1.40 */
        LOG_SUCCESS,
        /** Info about failure. Change is not guaranteed to affect colored
         * output written in the past.
         * @since 1.40 */
        LOG_FAILURE,
        /**Info about warning. Change is not guaranteed to affect colored
         * output written in the past.
         * @since 1.40 */
        LOG_WARNING,
        /** Debugging info. Change is not guaranteed to affect colored
         * output written in the past.
         * @since 1.40 */
        LOG_DEBUG
    }

    /**
     * Gets current color for output
     * @param io InputOutput to operate on
     * @param type output type to get color for
     * @return current color for specified output type or null if not supported
     */
    public static Color getColor(InputOutput io, OutputType type) {
        IOColors ioc = find(io);
        return ioc != null ? ioc.getColor(type) : null;
    }

    /**
     * Sets specified color for output
     * @param io InputOutput to operate on
     * @param type output type to set color for
     * @param color new color for specified output type
     */
    public static void setColor(InputOutput io, OutputType type, Color color) {
        IOColors ioc = find(io);
        if (ioc != null) {
            ioc.setColor(type, color);
        }
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    /**
     * Gets current color for output
     * @param type output type to get color for
     * @return current color for specified output
     */
    protected abstract Color getColor(OutputType type);

    /**
     * Sets specified color for output
     * @param type output type to set color for
     * @param color new color for specified output type
     */
    protected abstract void setColor(OutputType type, Color color);
}
