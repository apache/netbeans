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
package org.netbeans.api.io;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.intent.Intent;
import org.netbeans.modules.io.HyperlinkAccessor;
import org.openide.util.Parameters;

/**
 * Hyperlink in output window. It can be specified by a {@link Runnable} to
 * invoke when the hyperlink is clicked.
 *
 * @author jhavlin
 */
public abstract class Hyperlink {

    private final boolean important;

    private Hyperlink(boolean important) {
        this.important = important;
    }

    static {
        HyperlinkAccessor.setDefault(new HyperlinkAccessorImpl());
    }

    /**
     * @return True if the hyperlink has been marked as important, false if it
     * is a standard link.
     */
    boolean isImportant() {
        return important;
    }

    /**
     * Create a new hyperlink for specified {@link Runnable}, which will be
     * invoked when the line is clicked.
     *
     * @param runnable The runnable to run on click.
     * @return The new hyperlink.
     */
    @NonNull
    public static Hyperlink from(@NonNull Runnable runnable) {
        return from(runnable, false);
    }

    /**
     * Create a new hyperlink for specified {@link Runnable}, which will be
     * invoked when the line is clicked.
     *
     * <div class="nonnormative">
     * <p>
     * Important hyperlinks can be printed in different color, or can have some
     * special behavior, e.g. automatic scrolling can be switched off to keep
     * the important hyperlink visible.
     * </p>
     * </div>
     *
     * @param runnable The runnable to run on click.
     * @param important True if the hyperlink should be handled as an important
     * one, false if it is a standard one.
     * @return The new hyperlink.
     */
    @NonNull
    public static Hyperlink from(@NonNull Runnable runnable,
            boolean important) {
        Parameters.notNull("runnable", runnable);                       //NOI18N
        return new OnClickHyperlink(runnable, important);
    }

    /**
     * Create a new hyperlink for specified {@link Intent}, which will be
     * executed when the line is clicked.
     *
     * @param intent The intent to execute on click.
     * @return The new hyperlink.
     */
    public static Hyperlink from(@NonNull Intent intent) {
        return from(intent, false);
    }

    /**
     * Create a new hyperlink for specified {@link Intent}, which will be
     * executed when the line is clicked.
     *
     * <div class="nonnormative">
     * <p>
     * Important hyperlinks can be printed in different color, or can have some
     * special behavior, e.g. automatic scrolling can be switched off to keep
     * the important hyperlink visible.
     * </p>
     * </div>
     *
     * @param intent The intent to execute on click.
     * @param important True if the hyperlink should be handled as an important
     * one, false if it is a standard one.
     * @return The new hyperlink.
     */
    public static Hyperlink from(@NonNull Intent intent, boolean important) {
        Parameters.notNull("intent", intent);                           //NOI18N
        return new IntentHyperlink(intent, important);
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    static class OnClickHyperlink extends Hyperlink {

        private final Runnable runnable;

        public OnClickHyperlink(Runnable runnable, boolean important) {
            super(important);
            this.runnable = runnable;
        }

        public Runnable getRunnable() {
            return runnable;
        }
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    static class IntentHyperlink extends Hyperlink {

        private final Intent intent;

        public IntentHyperlink(Intent intent, boolean important) {
            super(important);
            this.intent = intent;
        }

        public Intent getIntent() {
            return intent;
        }
    }
}
