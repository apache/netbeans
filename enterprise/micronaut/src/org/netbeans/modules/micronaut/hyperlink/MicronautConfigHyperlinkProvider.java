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
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lsp.HyperlinkLocation;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.spi.lsp.HyperlinkLocationProvider;
import org.openide.util.NbBundle;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataSource;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-yaml", service = HyperlinkProviderExt.class, position = 1250)
public class MicronautConfigHyperlinkProvider implements HyperlinkProviderExt {

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
        int[] span = new int[2];
        ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, span, null);
        return property != null ? span : null;
    }

    @Override
    @NbBundle.Messages("LBL_GoToDeclaration=Go to Declaration")
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        AtomicBoolean cancel = new AtomicBoolean();
        BaseProgressUtils.runOffEventDispatchThread(() -> {
            List<ConfigurationMetadataSource> sources = new ArrayList<>();
            ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, null, sources);
            if (property != null && !sources.isEmpty()) {
                ClasspathInfo cpInfo = ClasspathInfo.create(doc);
                ElementHandle handle = getElementHandle(cpInfo, sources.get(0).getType(), property.getName(), cancel);
                if (handle == null || !ElementOpen.open(cpInfo, handle)) {
                    Toolkit.getDefaultToolkit().beep();
                }
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

    private static ElementHandle getElementHandle(ClasspathInfo cpInfo, String typeName, String propertyName, AtomicBoolean cancel) {
        ElementHandle[] handle = new ElementHandle[1];
        if (cpInfo != null) {
            try {
                JavaSource.create(cpInfo).runUserActionTask(controller -> {
                    if (cancel != null && cancel.get()) {
                        return;
                    }
                    handle[0] = ElementHandle.createTypeElementHandle(ElementKind.CLASS, typeName);
                    TypeElement te = (TypeElement) handle[0].resolve(controller);
                    if (te != null) {
                        String name = "set" + propertyName.replaceAll("-", "");
                        for (ExecutableElement executableElement : ElementFilter.methodsIn(te.getEnclosedElements())) {
                            if (name.equalsIgnoreCase(executableElement.getSimpleName().toString())) {
                                handle[0] = ElementHandle.create(executableElement);
                                break;
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {}
        }
        return handle[0];
    }

    @MimeRegistration(mimeType = "text/x-yaml", service = HyperlinkLocationProvider.class)
    public static class LocationProvider implements HyperlinkLocationProvider {

        @Override
        public CompletableFuture<HyperlinkLocation> getHyperlinkLocation(Document doc, int offset) {
            final AtomicBoolean cancel = new AtomicBoolean();
            List<ConfigurationMetadataSource> sources = new ArrayList<>();
            ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, null, sources);
            if (property != null && !sources.isEmpty()) {
                ClasspathInfo cpInfo = ClasspathInfo.create(doc);
                String typeName = sources.get(0).getType();
                ElementHandle handle = getElementHandle(cpInfo, typeName, property.getName(), cancel);
                if (handle != null) {
                    CompletableFuture<ElementOpen.Location> future = ElementOpen.getLocation(cpInfo, handle, typeName.replace('.', '/') + ".class");
                    return future.thenApply(location -> {
                        return location != null ? HyperlinkLocationProvider.createHyperlinkLocation(location.getFileObject(), location.getStartOffset(), location.getEndOffset()) : null;
                    });
                }
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
