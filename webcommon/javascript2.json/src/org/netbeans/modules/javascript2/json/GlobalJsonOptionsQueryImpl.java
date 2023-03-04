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
package org.netbeans.modules.javascript2.json;

import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Tomas Zezula
 */
@ServiceProvider(service = JsonOptionsQueryImplementation.class, position = 100_000)
public class GlobalJsonOptionsQueryImpl implements JsonOptionsQueryImplementation {
    private static final Logger LOG = Logger.getLogger(GlobalJsonOptionsQueryImpl.class.getName());
    private static final String PROP_ALLOW_COMMENTS="json.comments";  //NOI18N
    private static final Pattern FILES;
    static {
        Pattern p = null;
        final String propVal = System.getProperty(PROP_ALLOW_COMMENTS);
        if (propVal != null) {
            try {
                p = Pattern.compile(propVal);
            } catch (PatternSyntaxException e) {
                LOG.log(
                        Level.WARNING,
                        "Cannot compile: {0}, error: {1}",  //NOI18N
                        new Object[]{ propVal, e.getMessage()});
            }
        }
        FILES = p;
    }

    @Override
    @CheckForNull
    public Result getOptions(FileObject file) {
        if (FILES != null && FILES.matcher(file.getNameExt()).matches()) {
            return new JsonOptionsResult(true);
        }
        return null;
    }

    private static final class JsonOptionsResult implements JsonOptionsQueryImplementation.Result {
        private final boolean commentSupported;

        JsonOptionsResult(
            final boolean commentSupported) {
            this.commentSupported = commentSupported;
        }

        @Override
        @CheckForNull
        public Boolean isCommentSupported() {
            return commentSupported;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            //Immutable
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            //Immutable
        }
    }
}
