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
package org.netbeans.modules.micronaut.hyperlink;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
 * CURRENTLY NOT ACTIVE - @MimeRegistration DISABLED to work around 
 * <a href="https://github.com/apache/netbeans/issues/3913">GITHUB-3913</a>
 *
 * @author Dusan Balek
 */
public class MicronautConfigHyperlinkProvider implements HyperlinkProviderExt {

    //@MimeRegistration(mimeType = "text/x-yaml", service = HyperlinkProviderExt.class, position = 1250)
    public static MicronautConfigHyperlinkProvider createYamlProvider() {
        return new MicronautConfigHyperlinkProvider();
    }

    //@MimeRegistration(mimeType = "text/x-properties", service = HyperlinkProviderExt.class, position = 1250)
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
        int[] span = new int[2];
        List<ConfigurationMetadataSource> sources = new ArrayList<>();
        ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, span, sources);
        return property != null || !sources.isEmpty() ? span : null;
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
                        ElementHandle handle = getElementHandle(cpInfo, source.getType(), property != null ? property.getName() : null, cancel);
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

    private static ElementHandle getElementHandle(ClasspathInfo cpInfo, String typeName, String propertyName, AtomicBoolean cancel) {
        ElementHandle[] handle = new ElementHandle[1];
        if (typeName != null) {
            handle[0] = ElementHandle.createTypeElementHandle(ElementKind.CLASS, typeName);
            if (cpInfo != null && propertyName != null) {
                try {
                    JavaSource.create(cpInfo).runUserActionTask(controller -> {
                        if (cancel != null && cancel.get()) {
                            return;
                        }
                        TypeElement te = (TypeElement) handle[0].resolve(controller);
                        if (te != null) {
                            ElementHandle found = null;
                            String name = "set" + propertyName.replace("-", "");
                            for (ExecutableElement executableElement : ElementFilter.methodsIn(te.getEnclosedElements())) {
                                if (name.equalsIgnoreCase(executableElement.getSimpleName().toString())) {
                                    found = ElementHandle.create(executableElement);
                                    break;
                                }
                            }
                            if (found == null) {
                                TypeElement typeElement = controller.getElements().getTypeElement("io.micronaut.context.annotation.Property");
                                for (VariableElement variableElement : ElementFilter.fieldsIn(te.getEnclosedElements())) {
                                    for (AnnotationMirror annotationMirror : variableElement.getAnnotationMirrors()) {
                                        if (typeElement == annotationMirror.getAnnotationType().asElement()) {
                                            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                                                if ("name".contentEquals(entry.getKey().getSimpleName()) && propertyName.equals(entry.getValue().getValue())) {
                                                    found = ElementHandle.create(variableElement);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (found != null) {
                                handle[0] = found;
                            }
                        }
                    }, true);
                } catch (IOException ex) {}
            }
        }
        return handle[0];
    }

    public static class LocationProvider implements HyperlinkLocationProvider {

        //@MimeRegistration(mimeType = "text/x-yaml", service = HyperlinkLocationProvider.class)
        public static LocationProvider createYamlProvider() {
            return new LocationProvider();
        }

        //@MimeRegistration(mimeType = "text/x-properties", service = HyperlinkLocationProvider.class)
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
                        ElementHandle handle = getElementHandle(cpInfo, typeName, property != null ? property.getName() : null, cancel);
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
