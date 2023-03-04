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
package org.netbeans.api.intent;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.intent.CallbackResult;
import org.netbeans.modules.intent.IntentHandler;
import org.netbeans.modules.intent.SettableResult;
import org.netbeans.spi.intent.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Description of some intended operation. The operation is described by an
 * action and a URI.
 *
 * <p>
 * If the intent is executed, proper registered handler is chosen to perform the
 * actual operation.
 * </p>
 * <p>
 * For example, the following code can be used to open a file in editor (if the
 * environment is suitable for such operation).
 * </p>
 * <code>
 * new Intent(Intent.ACTION_VIEW, new URI("file://path/file.txt")).execute();
 * </code>
 *
 * @author jhavlin
 */
public final class Intent {

    private static final Logger LOG = Logger.getLogger(Intent.class.getName());

    /**
     * Standard VIEW action type.
     */
    public static final String ACTION_VIEW = "VIEW";                    //NOI18N

    /**
     * Standard EDIT action type.
     */
    public static final String ACTION_EDIT = "EDIT";                    //NOI18N

    private final String action;
    private final URI uri;

    /**
     * Constructor for an intended operation.
     *
     * @param action Action type to perform. It is recommended to use either
     * standard actions predefined in Intent class (see {@link #ACTION_EDIT},
     * {@link #ACTION_VIEW}), or strings similar to fully qualified field names
     * (e.g. "org.some.package.ClassName.ACTION_CUSTOM").
     *
     * @param uri URI specifying the operation.
     */
    public Intent(@NonNull String action, @NonNull URI uri) {
        Parameters.notNull("action", action);
        Parameters.notNull("uri", uri);
        this.action = action;
        this.uri = uri;
    }

    /**
     * Get action type.
     *
     * @return The action type.
     */
    public @NonNull String getAction() {
        return action;
    }

    /**
     * Get URI specifying this intent.
     *
     * @return The URI.
     */
    public @NonNull URI getUri() {
        return uri;
    }

    /**
     * Execute the intent. The operation will be run asynchronously.
     * <p>
     * If the result is ignored, it's recommended to use
     * {@code intent.execute(null);}
     * </p>
     *
     * @return {@link Future} Future for result of the action. The type of
     * result depends on implementation of chosen intent handler, it can be
     * null.
     */
    public @NonNull Future<Object> execute() {
        return IntentHandler.RP.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                SettableResult sr = new SettableResult();
                invoke(Intent.this, sr);
                if (sr.getException() != null) {
                    throw sr.getException();
                }
                return sr.getResult();
            }
        });
    }

    /**
     * Execute the intent. The operation will be run asynchronously.
     *
     * @param callback Callback object that will be notified when the execution
     * completes. If callback is null, the result will be ignored.
     */
    public void execute(@NullAllowed final Callback callback) {
        IntentHandler.RP.post(new Runnable() {

            @Override
            public void run() {
                invoke(Intent.this, callback == null
                        ? null
                        : new CallbackResult(callback));
            }
        });
    }

    /**
     * Get available actions for the intent.
     *
     * @return Immutable set of available actions, sorted by priority.
     */
    public @NonNull SortedSet<? extends IntentAction> getIntentActions() {
        SortedSet<IntentHandler> intentHandlers = getIntentHandlers(this);
        SortedSet<IntentAction> actions = new TreeSet<>(
                new Comparator<IntentAction>() {

                    @Override
                    public int compare(IntentAction o1, IntentAction o2) {
                        return o1.getPosition() - o2.getPosition();
                    }
                });
        for (IntentHandler ih : intentHandlers) {
            actions.add(new IntentAction(this, ih));
        }
        return Collections.unmodifiableSortedSet(actions);
    }

    private static SortedSet<IntentHandler> getIntentHandlers(
            Intent intent) {

        FileObject f = FileUtil.getConfigFile("Services/Intent/Handlers");
        if (f == null) {
            return null;
        }
        SortedSet<IntentHandler> candidates = new TreeSet<>();
        for (FileObject fo : f.getChildren()) {
            if ("instance".equals(fo.getExt())) {
                Object pattern = fo.getAttribute("uriPattern");
                Object displayName = fo.getAttribute("displayName");
                Object position = fo.getAttribute("position");
                Object actions = fo.getAttribute("actions");
                if (pattern instanceof String && displayName instanceof String
                        && position instanceof Integer
                        && actions instanceof String) {
                    if (canSupport((String) pattern, (String) actions, intent)) {
                        try {
                            IntentHandler ih = FileUtil.getConfigObject(
                                    fo.getPath(), IntentHandler.class);
                            candidates.add(ih);
                        } catch (Exception e) {
                            LOG.log(Level.INFO,
                                    "Cannot instantiate handler for " //NOI18N
                                    + fo.getPath(), e);
                        }
                    }
                } else {
                    LOG.log(Level.FINE, "Invalid URI handler {0}", fo.getPath());
                }
            }
        }
        return candidates;
    }

    /**
     * Check whether an intent is supported by a handler specified by a URI
     * pattern and action list.
     *
     * @param uriPattern Pattern for the URI.
     * @param actions Comma-separated actions.
     * @param intent Intent to check.
     * @return True if the intent matches the URI pattern and action list.
     */
    private static boolean canSupport(String uriPattern, String actions,
            Intent intent) {
        Pattern p = Pattern.compile(uriPattern);
        if (p.matcher(intent.getUri().toString()).matches()) {
            if ("*".equals(actions)) {
                return true;
            } else {
                List<String> actionList = Arrays.asList(actions.split(","));
                return actionList.contains(intent.getAction());
            }
        } else {
            return false;
        }
    }

    private static void invoke(Intent intent, Result resultOrNull) {

        Throwable lastException = null;
        boolean handled = false;

        for (IntentHandler h : getIntentHandlers(intent)) {
            try {
                h.handle(intent, resultOrNull);
                handled = true;
                break;
            } catch (Exception e) {
                lastException = e;
                LOG.log(Level.WARNING, null, e);
            }
        }
        if (!handled) {
            if (resultOrNull != null) {
                resultOrNull.setException(lastException == null
                        ? new NoAvailableHandlerException(intent)
                        : new NoAvailableHandlerException(intent, lastException));
            }
            LOG.log(Level.INFO, "Intent {0} cannot be handled", intent);//NOI18N
        }
    }

    @Override
    public String toString() {
        return "Intent{" + "action=" + action + ", uri=" + uri + '}';
    }
}
