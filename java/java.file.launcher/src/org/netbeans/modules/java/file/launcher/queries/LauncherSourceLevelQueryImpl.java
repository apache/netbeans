/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.file.launcher.queries;

import java.util.List;
import java.util.Objects;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=SourceLevelQueryImplementation2.class, position=9_999)
public class LauncherSourceLevelQueryImpl implements SourceLevelQueryImplementation2 {

    @Override
    public Result getSourceLevel(FileObject javaFile) {
        SingleFileOptionsQueryImplementation.Result delegate = SingleSourceFileUtil.getOptionsFor(javaFile);

        if (delegate != null) {
            return new ResultImpl(delegate);
        } else {
            return null;
        }
    }

    private static final class ResultImpl implements ChangeListener, Result {

        private static final String DEFAULT_SOURCE_LEVEL =
                JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();

        private final ChangeSupport cs = new ChangeSupport(this);
        private final SingleFileOptionsQueryImplementation.Result delegate;
        private String sourceLevel;

        public ResultImpl(SingleFileOptionsQueryImplementation.Result delegate) {
            this.delegate = delegate;
            this.delegate.addChangeListener(this);
            updateDelegate();
        }

        private void updateDelegate() {
            List<String> parsed = SingleSourceFileUtil.parseLine(delegate.getOptions());
            String sourceLevel = DEFAULT_SOURCE_LEVEL;

            for (int i = 0; i < parsed.size(); i++) {
                if ("--source".equals(parsed.get(i)) && i + 1 < parsed.size()) {
                    sourceLevel = parsed.get(i + 1);
                }
            }

            boolean modified;

            synchronized (this) {
                modified = !Objects.equals(this.sourceLevel, sourceLevel);

                if (modified) {
                    this.sourceLevel = sourceLevel;
                }
            }

            if (modified) {
                cs.fireChange();
            }
        }

        @Override
        public void stateChanged(ChangeEvent ce) {
            updateDelegate();
        }

        @Override
        public synchronized String getSourceLevel() {
            return sourceLevel;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

    }
}
