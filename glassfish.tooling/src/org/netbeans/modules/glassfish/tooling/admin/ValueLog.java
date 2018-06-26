/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.admin;

import java.util.List;
import org.netbeans.modules.glassfish.tooling.utils.Utils;

/**
 * GlassFish server log.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ValueLog {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server log lines. */
    final List<String> lines;

    /**
     * URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     * <p/>
     * <code>X-Text-Append-Next</code> header contains the entire URL to pass
     * to the GET method to return the changes since the last call.
     * You can use those URL parameters to construct URL to get all log entries
     * that were added in particular interval starting from call that returned
     * this result.
     */
    final String paramsAppendNext;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish server log entity.
     * <p/>
     * Entity is initialized with values stored in
     * <code>Runner</code> internal attributes in <code>processResponse</code>
     * method.
     * <p/>
     * @param lines GlassFish server log lines.
     * @param paramsAppendNext URL parameters from HTTP header
     *        <code>X-Text-Append-Next</code>
     */
    ValueLog(List<String> lines, String paramsAppendNext) {
        this.lines = lines;
        this.paramsAppendNext = paramsAppendNext;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server log lines.
     * <p/>
     * @return GlassFish server log lines.
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * Get URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     * <p/>
     * @return URL parameters from HTTP header <code>X-Text-Append-Next</code>.
     */
    public String getParamsAppendNext() {
        return paramsAppendNext;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert stored server log lines to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        String lineSeparator = Utils.lineSeparator();
        int lineSeparatorLength = lineSeparator.length();
        if (lines != null) {
            // Calculate total log length to avoid StringBuffer resizing.
            int length = 0;
            for (String line : lines) {
                length += line != null
                        ? line.length() + lineSeparatorLength
                        : lineSeparatorLength;
            }
            StringBuilder sb = new StringBuilder(length);
            for (String line : lines) {
                if (line != null) {
                    sb.append(line);
                }
                sb.append(lineSeparator);
            }
            return sb.toString();
        }
        else {
            return null;
        }
    }

}
