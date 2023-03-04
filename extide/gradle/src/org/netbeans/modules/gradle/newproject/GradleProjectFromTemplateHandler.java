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
package org.netbeans.modules.gradle.newproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator;
import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = CreateFromTemplateHandler.class)
public final class GradleProjectFromTemplateHandler extends CreateFromTemplateHandler {
    private static BiFunction<BaseGradleWizardIterator, Map<String, Object>, TemplateOperation> COLLECT_OPERATIONS;

    public static void register(BiFunction<BaseGradleWizardIterator, Map<String, Object>, TemplateOperation> c) {
        if (COLLECT_OPERATIONS != null) {
            throw new IllegalStateException();
        }
        COLLECT_OPERATIONS = c;
    }

    @Override
    protected boolean accept(CreateDescriptor desc) {
        return extractGradleIterator(desc) != null;
    }

    @Override
    protected List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
        BaseGradleWizardIterator p = extractGradleIterator(desc);

        final Map<String, Object> copyParams = new HashMap<>(desc.getParameters());
        fillIfMissing(copyParams, CommonProjectActions.PROJECT_PARENT_FOLDER, FileUtil.toFile(desc.getTarget()));
        fillIfMissing(copyParams, "template", desc.getTemplate()); // NOI18N

        TemplateOperation ops = COLLECT_OPERATIONS.apply(p, copyParams);
        ops.run();
        return sortByParentship(ops.getImportantFiles());
    }

    private static List<FileObject> sortByParentship(Collection<FileObject> fo) {
        ArrayList<FileObject> files = new ArrayList<>(fo);
        files.sort((a, b) -> {
            if (a.equals(b)) {
                return 0;
            }
            if (FileUtil.isParentOf(a, b)) {
                return -1;
            }
            if (FileUtil.isParentOf(b, a)) {
                return 1;
            }
            return 0;
        });
        return files;
    }

    private static BaseGradleWizardIterator extractGradleIterator(CreateDescriptor desc) {
        Object it = desc.getTemplate().getAttribute("instantiatingIterator"); // NOI18N;
        return it instanceof BaseGradleWizardIterator ? (BaseGradleWizardIterator) it : null;
    }

    private static void fillIfMissing(Map<String, Object> map, String prop, Object obj) {
        if (!map.containsKey(prop)) {
            if (obj != null) {
                map.put(prop, obj);
            }
        }
    }

}
