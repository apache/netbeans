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

package org.netbeans.modules.java.source.parsing;

import java.net.URL;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public final class SiblingSupport implements SiblingSource {

    private static final Logger LOG = Logger.getLogger(SiblingSupport.class.getName());

    private final Stack<Pair<URL,Boolean>> siblings = new Stack<Pair<URL,Boolean>>();
    private final SiblingProvider provider = new Provider();

    private SiblingSupport() {
    }

    @Override
    public void push(
            @NonNull final URL sibling,
            final boolean inSourceRoot) {
        assert sibling != null;
        siblings.push(Pair.<URL,Boolean>of(sibling,inSourceRoot));
        LOG.log(Level.FINE, "Pushed sibling: {0} size: {1}", new Object[]{sibling, siblings.size()});    //NOI18N
    }

    @Override
    public URL pop() {
        final Pair<URL, Boolean> removed = siblings.pop();
        if (LOG.isLoggable(Level.FINEST)) {
            StackTraceElement[] td = Thread.currentThread().getStackTrace();
            LOG.log(Level.FINEST, "Poped sibling: {0} size: {1} caller:\n{2}", new Object[] {removed, siblings.size(), formatCaller(td)});     //NOI18N
        } else {
            LOG.log(Level.FINE, "Poped sibling: {0} size: {1}", new Object[] {removed, siblings.size()});     //NOI18N
        }
        return removed.first();
    }

    @Override
    public SiblingProvider getProvider() {
        return provider;
    }

    public static SiblingSource create() {
        return new SiblingSupport();
    }

    private final class Provider implements SiblingProvider {
        private Provider() {
        }

        @Override
        public URL getSibling() {
            final Pair<URL,Boolean> result = siblings.peek();
            LOG.log(
                Level.FINER,
                "Returns sibling: {0} in source root? {1}",    //NOI18N
                new Object[] {
                    result.first(),
                    result.second()
                });
            return result.first();
        }

        @Override
        public boolean hasSibling() {
            boolean result = !siblings.isEmpty();
            LOG.log(Level.FINER, "Has sibling: {0}", new Object[] {result});  //NOI18N
            return result;
        }

        @Override
        public boolean isInSourceRoot() {
            return siblings.peek().second();
        }
    }

    private static String formatCaller (final StackTraceElement[] elements) {
        final StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : elements) {
            sb.append(String.format("%s.%s (%s:%d)\n",
                    element.getClassName(),
                    element.getMethodName(),
                    element.getFileName(),
                    element.getLineNumber()));
        }
        return sb.toString();
    }

}
