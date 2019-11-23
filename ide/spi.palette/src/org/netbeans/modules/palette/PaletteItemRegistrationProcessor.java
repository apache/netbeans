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
package org.netbeans.modules.palette;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.netbeans.spi.palette.PaletteItemRegistrations;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.lookup.ServiceProvider;

/**
 * Processor for PaletteItemRegistration.
 *
 * @author Eric Barboni <skygo@netbeans.org>
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes({"org.netbeans.spi.palette.PaletteItemRegistration", "org.netbeans.spi.palette.PaletteItemRegistrations"})
public final class PaletteItemRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {

        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element e : roundEnv.getElementsAnnotatedWith(PaletteItemRegistration.class)) {
            PaletteItemRegistration pir = e.getAnnotation(PaletteItemRegistration.class);
            if (pir == null) {
                continue;
            }
            process(e, pir);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(PaletteItemRegistrations.class)) {
            PaletteItemRegistrations dfrr = e.getAnnotation(PaletteItemRegistrations.class);
            if (dfrr == null) {
                continue;
            }
            for (PaletteItemRegistration t : dfrr.value()) {
                process(e, t);
            }
        }
        return true;
    }

    private void process(Element e, PaletteItemRegistration pir) throws LayerGenerationException {
        LayerBuilder builder = layer(e);
        TypeMirror activeEditorDrop = type(ActiveEditorDrop.class);

        LayerBuilder.File f = builder.file(pir.paletteid() + "/" + pir.category() + "/" + pir.itemid() + ".xml");
        StringBuilder paletteFile = new StringBuilder();

        paletteFile.append("<!DOCTYPE editor_palette_item PUBLIC '-//NetBeans//Editor Palette Item 1.1//EN' 'http://www.netbeans.org/dtds/editor-palette-item-1_1.dtd'>\n");
        paletteFile.append("<editor_palette_item version=\"1.1\">\n");

        if (pir.body().isEmpty()) {
            // body empty we need a activeEditorDrop
            if (e.getKind() == ElementKind.CLASS && isAssignable(e.asType(), activeEditorDrop)) {
                String className = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
                paletteFile.append(" <class name=\"").append(className).append("\"/>\n");
            } else {
                throw new LayerGenerationException("Class annotated with @PaletteItemRegistration has to implements ActiveEditorDrop", e);
            }
        } else {
            // body not empty put info 
            paletteFile.append("<body>  <![CDATA[");
            paletteFile.append(pir.body());
            paletteFile.append("]]> </body>\n");
        }

// icon shoud be in classpath
        if (!pir.icon16().isEmpty()) {
            builder.validateResource(pir.icon16(), e, pir, "icon16", true);
            paletteFile.append("<icon16 urlvalue=\"").append(pir.icon16()).append("\" />\n");
        } else {
            throw new LayerGenerationException("Icon 16 must be set ", e);
        }
        if (!pir.icon32().isEmpty()) {
            builder.validateResource(pir.icon32(), e, pir, "icon32", true);
            paletteFile.append("<icon32 urlvalue=\"").append(pir.icon32()).append("\" />\n");
        } else {
            throw new LayerGenerationException("Icon 32 must be set ", e);
        }

        paletteFile.append("<inline-description>");
        paletteFile.append("<display-name>");
        paletteFile.append(pir.name());
        paletteFile.append("</display-name>");
        paletteFile.append("<tooltip> <![CDATA[ ");
        paletteFile.append(pir.tooltip());
        paletteFile.append("]]></tooltip>");
        paletteFile.append("</inline-description>");

        paletteFile.append("</editor_palette_item>");

        f.contents(paletteFile.toString());
        f.write();
    }

    // XXX come from other processor in openide
    private boolean isAssignable(TypeMirror first, TypeMirror snd) {
        if (snd == null) {
            return false;
        } else {
            return processingEnv.getTypeUtils().isAssignable(first, snd);
        }
    }
    // XXX come from other processor in openide
    private TypeMirror type(Class<?> type) {
        final TypeElement e = processingEnv.getElementUtils().getTypeElement(type.getCanonicalName());
        return e == null ? null : e.asType();
    }
}
