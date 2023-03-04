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

package org.netbeans.modules.java.source.classpath;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class SourcePathCheck extends JavaParserResultTask {

    private final Factory factory;

    public SourcePathCheck (final Factory factory) {
        super (JavaSource.Phase.PARSED);
        this.factory = factory;
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")    //NOI18N
    public void run(final Result result, final SchedulerEvent event) {
        final CompilationInfo info = CompilationInfo.get(result);
        final ClasspathInfo cpInfo = info.getClasspathInfo();
        if (cpInfo != null) {
            final ClassPath src = cpInfo.getClassPath(PathKind.SOURCE);
            final ClassPath boot = cpInfo.getClassPath(PathKind.BOOT);
            final ClassPath compile = cpInfo.getClassPath(PathKind.COMPILE);
            if (!isIncomplete(src, boot, compile)) {
                final ClassPath cachedSrc = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(cpInfo, PathKind.SOURCE);
                try {
                    final Set<URL> unknown = new HashSet<URL>();
                    if (cachedSrc.entries().isEmpty() && !src.entries().isEmpty()) {
                        for (ClassPath.Entry entry : src.entries()) {
                            final URL url = entry.getURL();
                            if (!this.factory.firedFor.contains(url) &&
                                    !JavaIndex.hasSourceCache(url,false) &&
                                    FileOwnerQuery.getOwner(url.toURI()) != null) {
                                unknown.add(url);
                                this.factory.firedFor.add(url);
                            }
                        }
                    }
                    if (!unknown.isEmpty()) {
                        PathRegistry.getDefault().registerUnknownSourceRoots(src, unknown);
                    }
                } catch (URISyntaxException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {        
    }

    private static boolean isIncomplete(ClassPath... cps) {
        for (ClassPath cp : cps) {
            if (cp.getFlags().contains(ClassPath.Flag.INCOMPLETE)) {
                return true;
            }
        }
        return false;
    }

    public static final class Factory extends TaskFactory {

        @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")    //NOI18N
        private final Set<URL> firedFor = new HashSet<URL>();

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.<SchedulerTask>singleton(new SourcePathCheck(this));
        }
    }

}
