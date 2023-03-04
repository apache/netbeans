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
