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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class AnnotationProcessingQueryImpl implements AnnotationProcessingQueryImplementation {

    private final NbModuleProject project;

    public AnnotationProcessingQueryImpl(NbModuleProject project) {
        this.project = project;
    }
    
    public @Override Result getAnnotationProcessingOptions(FileObject file) {
        if (inside(project.getSourceDirectory(), file)) {
            return new ResultImpl(FileUtil.urlForArchiveOrDir(project.getGeneratedClassesDirectory()));
        } else if (inside(project.getTestSourceDirectory("unit"), file)) {
            return new ResultImpl(FileUtil.urlForArchiveOrDir(project.getTestGeneratedClassesDirectory("unit")));
        } else if (inside(project.getTestSourceDirectory("qa-functional"), file)) {
            return new ResultImpl(FileUtil.urlForArchiveOrDir(project.getTestGeneratedClassesDirectory("qa-functional")));
        } else {
            return null;
        }
    }

    private static boolean inside(FileObject root, FileObject file) {
        return root != null && (file == root || FileUtil.isParentOf(root, file));
    }

    private static final class ResultImpl implements Result {

        private final URL dashS;

        ResultImpl(URL dashS) {
            this.dashS = dashS;
        }

        @Override
        public Set<? extends Trigger> annotationProcessingEnabled() {
            return EnumSet.allOf(Trigger.class);
        }

        @Override
        public Iterable<? extends String> annotationProcessorsToRun() {
            return null;
        }

        @Override
        public URL sourceOutputDirectory() {
            return dashS;
        }

        @Override
        public Map<? extends String, ? extends String> processorOptions() {
            return Collections.emptyMap();
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}

    }

}
