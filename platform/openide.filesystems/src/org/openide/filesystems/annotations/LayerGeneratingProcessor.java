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

package org.openide.filesystems.annotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.WeakSet;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Convenience base class for an annotation processor which creates XML layer entries.
 * From version 9.17, it is not necessary (and is not recommended) to declare @{@link SupportedSourceVersion}
 * on subclasses: the default implementation declares support for {@link SourceVersion#latest()}. Declare
 * specific {@link SourceVersion} limits only when necessary.
 * @see XMLFileSystem
 * @since org.openide.filesystems 7.15
 */
public abstract class LayerGeneratingProcessor extends AbstractProcessor {

    private static final String GENERATED_LAYER = "META-INF/generated-layer.xml";
    private static final String PUBLIC_DTD_ID = "-//NetBeans//DTD Filesystem 1.2//EN";
    private static final String NETWORK_DTD_URL = "http://www.netbeans.org/dtds/filesystem-1_2.dtd";
    private static final String LOCAL_DTD_RESOURCE = "/org/openide/filesystems/filesystem1_2.dtd";

    private static final ErrorHandler ERROR_HANDLER = new ErrorHandler() {
        @Override public void warning(SAXParseException exception) throws SAXException {throw exception;}
        @Override public void error(SAXParseException exception) throws SAXException {throw exception;}
        @Override public void fatalError(SAXParseException exception) throws SAXException {throw exception;}
    };

    private static final EntityResolver ENTITY_RESOLVER = new EntityResolver() {
        @Override public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (PUBLIC_DTD_ID.equals(publicId)) {
                return new InputSource(LayerGeneratingProcessor.class.getResource(LOCAL_DTD_RESOURCE).toString());
            } else {
                return null;
            }
        }
    };

    private static final Map<Filer,Document> generatedLayerByProcessor = new WeakHashMap<Filer,Document>();
    private static final Map<Filer,Collection<Element>> originatingElementsByProcessor = new WeakHashMap<Filer,Collection<Element>>();

    /** For access by subclasses. */
    protected LayerGeneratingProcessor() {}

    private final List<LayerBuilder> createdBuilders = new LinkedList<LayerBuilder>();

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        boolean ret;
        try {
            ret = handleProcess(annotations, roundEnv);
        } catch (LayerGenerationException x) {
            if (x.erroneousAnnotationValue != null) {
                messager.printMessage(Kind.ERROR, x.getLocalizedMessage(), x.erroneousElement, x.erroneousAnnotation, x.erroneousAnnotationValue);
            } else if (x.erroneousAnnotation != null) {
                messager.printMessage(Kind.ERROR, x.getLocalizedMessage(), x.erroneousElement, x.erroneousAnnotation);
            } else if (x.erroneousElement != null) {
                messager.printMessage(Kind.ERROR, x.getLocalizedMessage(), x.erroneousElement);
            } else {
                messager.printMessage(Kind.ERROR, x.getLocalizedMessage());
            }
            return false;
        }
        if (roundEnv.processingOver()) {
            Filer filer = processingEnv.getFiler();
            Document doc = generatedLayerByProcessor.remove(filer);
            Collection<Element> originatingElementsL = originatingElementsByProcessor.remove(filer);
            if (doc != null && !roundEnv.errorRaised()) {
                Element[] originatingElementsA = new Element[0];
                if (originatingElementsL != null) {
                    originatingElementsA = originatingElementsL.toArray(originatingElementsA);
                }
                try {
                    // Write to memory and reparse to make sure it is valid according to DTD before writing to disk.
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    // #184714: XMLUtil.write has too much overhead.
                    DOMImplementationLS ls = (DOMImplementationLS) doc.getImplementation().getFeature("LS", "3.0"); // NOI18N
                    assert ls != null : "No DOM 3 LS supported in " + doc.getClass().getName();
                    LSOutput output = ls.createLSOutput();
                    output.setEncoding("UTF-8");
                    output.setByteStream(baos);
                    LSSerializer ser = ls.createLSSerializer();
                    String fpp = "format-pretty-print"; // NOI18N
                    if (ser.getDomConfig().canSetParameter(fpp, true)) {
                        ser.getDomConfig().setParameter(fpp, true);
                    }
                    ser.write(doc, output);
                    byte[] data = baos.toByteArray();
                    XMLUtil.parse(new InputSource(new ByteArrayInputStream(data)), true, true, ERROR_HANDLER, ENTITY_RESOLVER);
                    FileObject layer = filer.createResource(StandardLocation.CLASS_OUTPUT, "", GENERATED_LAYER, originatingElementsA);
                    OutputStream os = layer.openOutputStream();
                    try {
                        os.write(data);
                    } finally {
                        os.close();
                    }
                    /*
                    {
                        SortedSet<String> files = new TreeSet<String>();
                        NodeList nl = doc.getElementsByTagName("file");
                        for (int i = 0; i < nl.getLength(); i++) {
                            org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(i);
                            String name = e.getAttribute("name");
                            while ((e = (org.w3c.dom.Element) e.getParentNode()).getTagName().equals("folder")) {
                                name = e.getAttribute("name") + "/" + name;
                            }
                            files.add(name);
                        }
                        for (String file : files) {
                            messager.printMessage(Kind.NOTE, "generated layer entry: " + file);
                        }
                    }
                    */
                } catch (IOException x) {
                    messager.printMessage(Kind.ERROR, "Failed to write generated-layer.xml: " + x.toString());
                } catch (SAXException x) {
                    messager.printMessage(Kind.ERROR, "Refused to write invalid generated-layer.xml: " + x.toString());
                }
            }
            for (LayerBuilder b : createdBuilders) {
                b.close();
            }
            createdBuilders.clear();
        }
        return ret;
    }

    /**
     * The regular body of {@link #process}.
     * In the last round, one of the layer-generating processors will write out generated-layer.xml.
     * <p>Do not attempt to read or write the layer file directly; just use {@link #layer}.
     * You may however wish to create other resource files yourself: see {@link LayerBuilder.File#url} for syntax.
     * @param annotations as in {@link #process}
     * @param roundEnv as in {@link #process}
     * @return as in {@link #process}
     * @throws LayerGenerationException in case some layer fragment cannot be generated (a user-level error will be reported for you)
     */
    protected abstract boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException;

    /**
     * Access the generated XML layer.
     * May already have content from a previous compilation run which should be overwritten.
     * May also have content from other layer-generated processors which should be appended to.
     * Simply make changes to the layer and they will be written to disk at the end of the job.
     * @param originatingElements as in {@link Filer#createResource};
     *                            optional but (if exactly one is passed) may be used for error reporting as well as identification of Java instances
     * @return a builder permitting you to add layer entries
     */
    protected final LayerBuilder layer(Element... originatingElements) {
        LayerBuilder b = new LayerBuilder(layerDocument(originatingElements), originatingElements.length == 1 ? originatingElements[0] : null, processingEnv);
        createdBuilders.add(b);
        return b;
    }

    private Document layerDocument(Element... originatingElements) {
        Filer filer = processingEnv.getFiler();
        Collection<Element> originatingElementsL = originatingElementsByProcessor.get(filer);
        if (originatingElementsL == null) {
            originatingElementsL = new WeakSet<Element>();
            originatingElementsByProcessor.put(filer, originatingElementsL);
        }
        originatingElementsL.addAll(Arrays.asList(originatingElements));
        Document doc = generatedLayerByProcessor.get(filer);
        if (doc == null) {
            try {
                FileObject layer = filer.getResource(StandardLocation.CLASS_OUTPUT, "", GENERATED_LAYER);
                InputStream is = layer.openInputStream();
                try {
                    doc = XMLUtil.parse(new InputSource(is), true, true, ERROR_HANDLER, ENTITY_RESOLVER);
                } finally {
                    is.close();
                }
            } catch (FileNotFoundException | NoSuchFileException fnfe) {
                // Fine, not yet created.
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to read generated-layer.xml: " + x.toString());
            } catch (SAXException x) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to parse generated-layer.xml: " + x.toString());
            }
            if (doc == null) {
                doc = XMLUtil.createDocument("filesystem", null, PUBLIC_DTD_ID, NETWORK_DTD_URL);
            }
            generatedLayerByProcessor.put(filer, doc);
        }
        return doc;
    }

    /**
     * If the subclass itself does not define SupportedSourceVersion, assume latest(). If it does
     * (was recommended prior to 9.17), returns the subclass' value for compatibility.
     * @return max supported source version.
     * @since 9.17
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        SourceVersion sv;
        if (ssv == null) {
            sv = SourceVersion.latest();
        } else
            sv = ssv.value();
        return sv;
    }
}
