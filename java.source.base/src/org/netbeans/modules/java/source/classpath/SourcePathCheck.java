/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
