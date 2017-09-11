/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    abstract protected void println(CharSequence text, OutputListener listener, boolean important, Color color) throws IOException;
}
