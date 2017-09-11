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
    abstract protected Color getColor(OutputType type);

    /**
     * Sets specified color for output
     * @param type output type to set color for
     * @param color new color for specified output type
     */
    abstract protected void setColor(OutputType type, Color color);
}
