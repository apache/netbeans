/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.spi.io.support;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.intent.Intent;
import org.netbeans.api.io.Hyperlink;
import org.netbeans.modules.io.HyperlinkAccessor;

/**
 * Helper class for accessing information from {@link Hyperlink} objects.
 *
 * @author jhavlin
 */
public final class Hyperlinks {

    private Hyperlinks() {
    }

    /**
     * Get hyperlink type.
     *
     * @param hyperlink The hyperlink to get type of.
     * @return The type of the hyperlink.
     */
    @NonNull
    public static HyperlinkType getType(@NonNull Hyperlink hyperlink) {
        return HyperlinkAccessor.getDefault().getType(hyperlink);
    }

    /**
     * Check whether a hyperlink is important.
     *
     * <div class="nonnormative">
     * <p>
     * Important hyperlinks can be printed in different color, or can have some
     * special behavior, e.g. automatic scrolling can be switched off to keep
     * the important hyperlink visible.
     * </p>
     * </div>
     *
     * @param hyperlink The hyperlink to check.
     *
     * @return True if the hyperlink has been marked as important, false if it
     * is a standard link.
     */
    public static boolean isImportant(@NonNull Hyperlink hyperlink) {
        return HyperlinkAccessor.getDefault().isImportant(hyperlink);
    }

    /**
     * Get runnable associated with a hyperlink of type
     * {@link HyperlinkType#FROM_RUNNABLE}.
     *
     * @param hyperlink The hyperlink to get runnable from.
     *
     * @return A runnable.
     * @throws IllegalArgumentException if type of the hyperlink is not
     * {@link HyperlinkType#FROM_RUNNABLE}.
     * @see #getType(org.netbeans.api.io.Hyperlink)
     * @see HyperlinkType
     */
    @NonNull
    public static Runnable getRunnable(@NonNull Hyperlink hyperlink) {
        return HyperlinkAccessor.getDefault().getRunnable(hyperlink);
    }

    /**
     * Get intent associated with a hyperlink of type
     * {@link HyperlinkType#FROM_INTENT}.
     *
     * @param hyperlink  The hyperlink to get intent from.
     *
     * @return An intent.
     * @throws IllegalArgumentException if type of the hyperlink is not
     * {@link HyperlinkType#FROM_INTENT}.
     * @see #getType(org.netbeans.api.io.Hyperlink)
     * @see HyperlinkType
     */
    @NonNull
    public static Intent getIntent(@NonNull Hyperlink hyperlink) {
        return HyperlinkAccessor.getDefault().getIntent(hyperlink);
    }

    /**
     * Invoke appropriate action for the hyperlink.
     *
     * @param hyperlink Hyperlink to invoke.
     */
    public static void invoke(Hyperlink hyperlink) {
        switch (getType(hyperlink)) {
            case FROM_RUNNABLE:
                getRunnable(hyperlink).run();
                break;
            case FROM_INTENT:
                getIntent(hyperlink).execute(null);
                break;
            default:
                break;
        }
    }
}
