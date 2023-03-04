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
import java.io.IOException;
import org.openide.util.Lookup;

/**
 * Line printing with custom color.
 * <p>
 * Client usage:
 * <pre>
 *  // print green line
 *  InputOutput io = ...;
 *  IOColorLines.println(io, "Green line", Color.GREEN);
 * </pre>
 * How to support {@link IOColorLines} in own {@link IOProvider} implementation:
 * <ul>
 *   <li> {@link InputOutput} provided by {@link IOProvider} has to implement {@link org.openide.util.Lookup.Provider}
 *   <li> Extend {@link IOColorLines} and implement its abstract methods
 *   <li> Place instance of {@link IOColorLines} to {@link Lookup} provided by {@link InputOutput}
 * </ul>
 * @see IOColors
 * @see IOColorPrint
 * @since 1.16
 * @author Tomas Holy
 */
public abstract class IOColorLines {

    private static IOColorLines find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOColorLines.class);
        }
        return null;
    }

    /**
     * Prints line with selected color
     * @param io IO to print to
     * @param text a string to print to the tab
     * @param color a color for the line of text (null allowed). If null is passed default color (see {@link IOColors}) is used.
     */
    public static void println(InputOutput io, CharSequence text, Color color) throws IOException {
        IOColorLines iocl = find(io);
        if (iocl != null) {
            iocl.println(text, null, false, color);
        }
    }

    /**
     * Prints line with selected color
     * @param io IO to print to
     * @param text a string to print to the tab
     * @param listener a listener that will receive events about this line
     * @param important  important mark the line as important.
     *        Makes the UI respond appropriately, eg. stop the automatic scrolling
     *        or highlight the hyperlink.
     * @param color a color for the line of text (null allowed). If null is passed default color (see {@link IOColors}) is used.
     */
    public static void println(InputOutput io, CharSequence text, OutputListener listener, boolean important, Color color) throws IOException {
        IOColorLines iocl = find(io);
        if (iocl != null) {
            iocl.println(text, listener, important, color);
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
     * Prints line with selected color
     * @param text a string to print to the tab
     * @param listener a listener that will receive events about this line (null allowed)
     * @param important  important mark the line as important.
     *        Makes the UI respond appropriately, eg. stop the automatic scrolling
     *        or highlight the hyperlink.
     * @param color a color for the line of text (null allowed). If null is passed default color (see {@link IOColors}) is used.
     */
    protected abstract void println(CharSequence text, OutputListener listener, boolean important, Color color) throws IOException;
}
