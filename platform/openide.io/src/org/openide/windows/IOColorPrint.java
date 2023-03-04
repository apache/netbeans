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
 * Text printing with custom color.
 * <p>
 * Client usage:
 * <pre>
 *  InputOutput io = ...;
 *  OutputListener l = ...;
 *  OutputListener l2 = ...;
 *  IOColorPrint.print(io, "Green text", Color.GREEN);
 *  IOColorPrint.print(io, " orange hyperlink ", l, false, Color.ORANGE);
 *  IOColorPrint.print(io, " green hyperlink\n", l2, false, Color.GREEN);
 * </pre>
 * How to support {@link IOColorPrint} in own {@link IOProvider} implementation:
 * <ul>
 *   <li> {@link InputOutput} provided by {@link IOProvider} has to implement {@link org.openide.util.Lookup.Provider}
 *   <li> Extend {@link IOColorPrint} and implement its abstract methods
 *   <li> Place instance of {@link IOColorPrint} to {@link Lookup} provided by {@link InputOutput}
 * </ul>
 * @see IOColors
 * @see IOColorLines
 * @since 1.18
 * @author Tomas Holy
 */
public abstract class IOColorPrint {

    private static IOColorPrint find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOColorPrint.class);
        }
        return null;
    }

    /**
     * Prints text with selected color
     * @param io IO to print to
     * @param text a string to print to the tab
     * @param color a color for the text (null allowed). If null is passed default color (see {@link IOColors}) is used.
     */
    public static void print(InputOutput io, CharSequence text, Color color) throws IOException {
        IOColorPrint iocl = find(io);
        if (iocl != null) {
            iocl.print(text, null, false, color);
        }
    }

    /**
     * Prints text with selected color and add listener for it
     * @param io IO to print to
     * @param text a string to print to the tab
     * @param listener a listener that will receive events about this text (null allowed)
     * @param important  important mark the line as important.
     *        Makes the UI respond appropriately, eg. stop the automatic scrolling
     *        or highlight the hyperlink.
     * @param color a color for the text (null allowed). If null is passed default color (see {@link IOColors}) is used.
     */
    public static void print(InputOutput io, CharSequence text, OutputListener listener, boolean important, Color color) throws IOException {
        IOColorPrint iocl = find(io);
        if (iocl != null) {
            iocl.print(text, listener, important, color);
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
     * Prints text with selected color and optionaly add listener for it
     * @param text a string to print to the tab
     * @param listener a listener that will receive events about this text (null allowed)
     * @param important  important mark the line as important.
     *        Makes the UI respond appropriately, eg. stop the automatic scrolling
     *        or highlight the hyperlink.
     * @param color a color for the text (null allowed). If null is passed default color (see {@link IOColors}) is used.
     */
    protected abstract void print(CharSequence text, OutputListener listener, boolean important, Color color) throws IOException;
}
