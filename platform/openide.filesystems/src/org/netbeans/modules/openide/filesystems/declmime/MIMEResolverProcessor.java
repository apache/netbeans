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
package org.netbeans.modules.openide.filesystems.declmime;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.MIMEResolver.ExtensionRegistration;
import org.openide.filesystems.MIMEResolver.NamespaceRegistration;
import org.openide.filesystems.MIMEResolver.Registration;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
public class MIMEResolverProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(MIMEResolver.Registration.class.getCanonicalName());
        set.add(MIMEResolver.ExtensionRegistration.class.getCanonicalName());
        set.add(MIMEResolver.NamespaceRegistration.class.getCanonicalName());
        return set;
    }
    
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(MIMEResolver.Registration.class)) {
            MIMEResolver.Registration r = e.getAnnotation(MIMEResolver.Registration.class);
            registerDefinition(e, r.resource(), r);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(MIMEResolver.ExtensionRegistration.class)) {
            MIMEResolver.ExtensionRegistration r = e.getAnnotation(MIMEResolver.ExtensionRegistration.class);
            registerExt(e, r);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(MIMEResolver.NamespaceRegistration.class)) {
            MIMEResolver.NamespaceRegistration r = e.getAnnotation(MIMEResolver.NamespaceRegistration.class);
            registerNamespace(e, r);
        }
        return true;
    }
    private static final String SUFFIX = ".xml";
    private void registerExt(Element e, ExtensionRegistration r) throws LayerGenerationException {
        final LayerBuilder b = layer(e);
        File f = b.file("Services/MIMEResolver/" + getName(e).replace('.', '-') + "-Extension" + SUFFIX); // NOI18N
        f.methodvalue("instanceCreate", MIMEResolver.class.getName(), "create"); // NOI18N
        f.stringvalue("instanceClass", MIMEResolver.class.getName()); // NOI18N
        f.stringvalue("mimeType", r.mimeType()); // NOI18N
        int cnt = 0;
        for (String ext : r.extension()) {
            f.stringvalue("ext." + (cnt++), ext); // NOI18N
        }
        f.position(r.position());
        int ccnt = 0;
        for (String chooser : r.showInFileChooser()) {
            f.bundlevalue("fileChooser." + (ccnt++), chooser);
        }
        f.bundlevalue("displayName", r.displayName()); // NOI18N
        f.write();
    }

    private void registerDefinition(Element e, String relRes, Registration r) throws LayerGenerationException {
        String absRes = LayerBuilder.absolutizeResource(e, relRes);
        final LayerBuilder b = layer(e);
        FileObject fo = b.validateResource(absRes, e, r, null, false);
        File f = b.file("Services/MIMEResolver/" + getName(e).replace('.', '-') + "-Registration" + SUFFIX); // NOI18N
        f.methodvalue("instanceCreate", MIMEResolver.class.getName(), "create"); // NOI18N
        f.stringvalue("instanceClass", MIMEResolver.class.getName()); // NOI18N
        f.serialvalue("bytes", generateInstanceResolver(fo, e, f, r)); // NOI18N
        f.position(r.position());
        int ccnt = 0;
        for (String chooser : r.showInFileChooser()) {
            f.bundlevalue("fileChooser." + (ccnt++), chooser);
        }
        f.bundlevalue("displayName", r.displayName()); // NOI18N
        f.write();
    }

    private byte[] generateInstanceResolver(FileObject fo, Element e, File f,
            Registration r) throws LayerGenerationException {
        try {
            InputStream is = fo.openInputStream();
            org.openide.filesystems.FileObject tmp = FileUtil.createMemoryFileSystem().getRoot().createData("resolver.xml");
            OutputStream os = tmp.getOutputStream();
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
            os.close();
            is.close();
            MIMEResolver resolver = MIMEResolverImpl.forDescriptor(tmp, false);
            setFileChooserRelatedAttributes(r, resolver, f);
            final byte[] almostResult = MIMEResolverImpl.toStream(resolver);
            // XXX: it would be slightly shorter to return the array directly,
            // but the XMLFileSystem insist on deserializing the value, it does
            // not support returning plain byte[]
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(almostResult);
            oos.close();
            return out.toByteArray();
        } catch (IOException ex) {
            final LayerGenerationException le = new LayerGenerationException("Cannot process " + fo, e);
            le.initCause(ex);
            throw le;
        }
    }
    private void registerNamespace(Element e, NamespaceRegistration r) throws LayerGenerationException {
        final LayerBuilder b = layer(e);
        File f = b.file("Services/MIMEResolver/" + getName(e).replace('.', '-') + "-Namespace" + SUFFIX); // NOI18N
        f.methodvalue("instanceCreate", MIMEResolver.class.getName(), "create"); // NOI18N
        f.stringvalue("instanceClass", MIMEResolver.class.getName()); // NOI18N
        f.stringvalue("mimeType", r.mimeType()); // NOI18N
        int cnt = 0;
        for (String ext : r.doctypePublicId()) {
            f.stringvalue("doctype." + (cnt++), ext); // NOI18N
        }
        cnt = 0;
        for (String ext : r.checkedExtension()) {
            f.stringvalue("ext." + (cnt++), ext); // NOI18N
        }
        cnt = 0;
        for (String ext : r.acceptedExtension()) {
            f.stringvalue("accept." + (cnt++), ext); // NOI18N
        }
        cnt = 0;
        f.stringvalue("element", r.elementName()); // NOI18N
        for (String ext : r.elementNS()) {
            f.stringvalue("ns." + (cnt++), ext); // NOI18N
        }
        f.position(r.position());
        f.bundlevalue("displayName", r.displayName()); // NOI18N
        f.write();
    }

    private String getName(Element e) {
        if (e.getKind().isClass() || e.getKind().isInterface()) {
            return processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
        } else if (e.getKind() == ElementKind.PACKAGE) {
            return e.getSimpleName().toString();
        } else {
            return getName(e.getEnclosingElement()) + '.' + e.getSimpleName();
        }
    }

    private Set<String> unq(String[] array) {
        return unq(Arrays.asList(array));
    }

    private Set<String> unq(Collection collection) {
        Set<String> s = new TreeSet<>();
        s.addAll(collection);
        return s;
    }

    private void setFileChooserRelatedAttributes(Registration r,
            MIMEResolver resolver, File f) {

        if (r.showInFileChooser().length > 0) {
            String[] types = MIMEResolverImpl.getMIMETypes(resolver);
            List<String> exts = ((FilterInfo) resolver).getExtensions();
            int cnt = 0;
            for (String ext : unq(exts)) {
                f.stringvalue("ext." + (cnt++), ext);              // NOI18N
            }
            int tcnt = 0;
            for (String type : unq(types)) {
                f.stringvalue("mimeType." + (tcnt++), type);       // NOI18N
            }
            int ncnt = 0;
            for (String name : ((FilterInfo) resolver).getFileNames()) {
                f.stringvalue("fileName." + (ncnt++), name);        //NOI18N
            }
        }
    }

    /**
     * Interface of objects that provide information for construction of
     * FileFilters.
     */
    interface FilterInfo {

        List<String> getExtensions();

        List<String> getFileNames();
    }
}
