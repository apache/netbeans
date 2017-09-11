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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
    abstract protected void print(CharSequence text, OutputListener listener, boolean important, Color color) throws IOException;
}
