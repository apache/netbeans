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
package org.netbeans.api.intent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.intent.CallbackResult;
import org.netbeans.modules.intent.IntentHandler;
import org.netbeans.modules.intent.SettableResult;
import org.netbeans.spi.intent.Result;

/**
 * Actual action for an Intent. Pair of an Intent and one of its handlers.
 *
 * @see Intent#getIntentActions()
 * @author jhavlin
 */
public final class IntentAction {

    private final Intent intent;
    private final IntentHandler delegate;

    IntentAction(Intent intent, IntentHandler delegate) {
        this.intent = intent;
        this.delegate = delegate;
    }

    int getPosition() {
        return delegate.getPosition();
    }

    /**
     * Execute the intent action. The operation will be run asynchronously.
     *
     * @param callback Callback object that will be notified when the execution
     * completes. If callback is null, the result will be ignored.
     */
    public void execute(@NullAllowed final Callback callback) {
        IntentHandler.RP.post(new Runnable() {
            @Override
            public void run() {
                Result result = callback == null
                        ? null
                        : new CallbackResult(callback);
                delegate.handle(intent, result);
            }
        });
    }

    /**
     * Execute the intent action. The operation will be run asynchronously.
     * <p>
     * If the result is ignored, it's recommended to use
     * {@code intentAction.execute(null);}
     * </p>
     *
     * @return Future for result of the action. The type of result depends on
     * implementation of chosen intent handler, it can be null.
     */
    public @NonNull Future<Object> execute() {

        return IntentHandler.RP.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                SettableResult result = new SettableResult();
                delegate.handle(intent, result);
                if (result.getException() != null) {
                    throw result.getException();
                }
                return result.getResult();
            }
        });
    }

    /**
     * Get display name of this action.
     *
     * @return The localized display name.
     */
    public @NonNull String getDisplayName() {
        return delegate.getDisplayName();
    }

    /**
     * Get icon of this action.
     *
     * @return Some resource identifier, e.g. icon id, path or URI.
     * Depends on the platform. If not available, empty string is returned.
     */
    public @NonNull String getIcon() {
        return delegate.getIcon();
    }
}
