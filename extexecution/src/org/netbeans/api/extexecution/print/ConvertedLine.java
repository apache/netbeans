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
