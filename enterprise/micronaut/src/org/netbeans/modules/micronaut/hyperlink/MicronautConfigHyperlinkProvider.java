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
package org.netbeans.modules.micronaut.hyperlink;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lsp.HyperlinkLocation;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.lsp.HyperlinkLocationProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataSource;

/**
 * 
 * @author Dusan Balek
 */
public class MicronautConfigHyperlinkProvider implements HyperlinkProviderExt {

    private static final String SPANS_PROPERTY_NAME = "MicronautConfigHyperlinkSpans";

    @MimeRegistration(mimeType = MicronautConfigUtilities.YAML_MIME, service = HyperlinkProviderExt.class, position = 1250)
    public static MicronautConfigHyperlinkProvider createYamlProvider() {
        return new MicronautConfigHyperlinkProvider();
    }

    @MimeRegistration(mimeType = MicronautConfigUtilities.PROPERTIES_MIME, service = HyperlinkProviderExt.class, position = 1250)
    public static MicronautConfigHyperlinkProvider createPropertiesProvider() {
        return new MicronautConfigHyperlinkProvider();
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        String mimeType = DocumentUtilities.getMimeType(doc);
        if (MicronautConfigUtilities.YAML_MIME.equals(mimeType)) {
            List<int[]> spans = null;
            synchronized (doc) {
                spans = (List<int[]>) doc.getProperty(SPANS_PROPERTY_NAME);
            }
            if (spans != null) {
                for (int[] span : spans) {
                    if (span.length == 2 && span[0] <= offset && offset <= span[1]) {
                        return span;
                    }
                }
            }
            return null;
        } else {
            int[] span = new int[2];
            List<ConfigurationMetadataSource> sources = new ArrayList<>();
            ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, span, sources);
            return property != null || !sources.isEmpty() ? span : null;
        }
    }

    @Override
    @NbBundle.Messages("LBL_GoToDeclaration=Go to Declaration")
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        AtomicBoolean cancel = new AtomicBoolean();
        BaseProgressUtils.runOffEventDispatchThread(() -> {
            List<ConfigurationMetadataSource> sources = new ArrayList<>();
            ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, null, sources);
            if (!sources.isEmpty()) {
                ClasspathInfo cpInfo = ClasspathInfo.create(doc);
                for (ConfigurationMetadataSource source : sources) {
                    if (property == null || source.getProperties().get(property.getId()) == property) {
                        ElementHandle handle = MicronautConfigUtilities.getElementHandle(cpInfo, source.getType(), property != null ? property.getName() : null, cancel);
                        if (handle != null && ElementOpen.open(cpInfo, handle)) {
                            return;
                        }
                    }
                }
                Toolkit.getDefaultToolkit().beep();
            }
        }, Bundle.LBL_GoToDeclaration(), cancel, false);
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, null, null);
        if (property != null) {
            StringBuilder sb = new StringBuilder("<html><body>");
            sb.append("<b>").append(property.getId().replace(".", /* ZERO WIDTH SPACE */".&#x200B;")).append("</b>");
            String propertyType = property.getType();
            if (propertyType != null) {
                sb.append("<pre>").append(propertyType).append("</pre>");
            }
            return sb.toString();
        }
        return null;
    }

    public static final class Task extends IndexingAwareParserResultTask<Parser.Result> {
    
        private final AtomicBoolean cancel = new AtomicBoolean();
        private final Project project;

        public Task(Project project) {
            super(TaskIndexingMode.ALLOWED_DURING_SCAN);
            this.project = project;
        }

        @Override
        public void run(Parser.Result result, SchedulerEvent event) {
            if (cancel.get()) {
                return;
            }
            Document doc = result.getSnapshot().getSource().getDocument(false);
            if (doc != null) {
                List<int[]> spans = MicronautConfigUtilities.getPropertySpans(project, result);
                synchronized (doc) {
                    doc.putProperty(SPANS_PROPERTY_NAME, spans);
                }
            }
        }

        @Override
        public int getPriority() {
            return 200;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
            cancel.set(true);
        }

        @MimeRegistration(mimeType = MicronautConfigUtilities.YAML_MIME, service = TaskFactory.class)
        public static final class Factory extends TaskFactory {

            @Override
            public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                FileObject fo = snapshot.getSource().getFileObject();
                if (MicronautConfigUtilities.isMicronautConfigFile(fo)) {
                    Project project = FileOwnerQuery.getOwner(fo);
                    if (project != null && MicronautConfigProperties.hasConfigMetadata(project)) {
                        return Collections.singleton(new Task(project));
                    }
                }
                return Collections.emptySet();
            }
        }
    }

    public static class LocationProvider implements HyperlinkLocationProvider {

        @MimeRegistration(mimeType = MicronautConfigUtilities.YAML_MIME, service = HyperlinkLocationProvider.class)
        public static LocationProvider createYamlProvider() {
            return new LocationProvider();
        }

        @MimeRegistration(mimeType = MicronautConfigUtilities.PROPERTIES_MIME, service = HyperlinkLocationProvider.class)
        public static LocationProvider createPropertiesProvider() {
            return new LocationProvider();
        }

        @Override
        public CompletableFuture<HyperlinkLocation> getHyperlinkLocation(Document doc, int offset) {
            final AtomicBoolean cancel = new AtomicBoolean();
            List<ConfigurationMetadataSource> sources = new ArrayList<>();
            ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, null, sources);
            if (!sources.isEmpty()) {
                ClasspathInfo cpInfo = ClasspathInfo.create(doc);
                for (ConfigurationMetadataSource source : sources) {
                    if (property == null || source.getProperties().get(property.getId()) == property) {
                        String typeName = source.getType();
                        ElementHandle handle = MicronautConfigUtilities.getElementHandle(cpInfo, typeName, property != null ? property.getName() : null, cancel);
                        if (handle != null) {
                            CompletableFuture<ElementOpen.Location> future = ElementOpen.getLocation(cpInfo, handle, typeName.replace('.', '/') + ".class");
                            return future.thenApply(location -> {
                                return location != null ? HyperlinkLocationProvider.createHyperlinkLocation(location.getFileObject(), location.getStartOffset(), location.getEndOffset()) : null;
                            });
                        }
                    }
                }
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
