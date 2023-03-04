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
