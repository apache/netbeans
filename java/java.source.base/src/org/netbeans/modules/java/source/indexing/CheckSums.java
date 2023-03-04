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

package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
public final class CheckSums {

    private static final String CHECK_SUMS_FILE = "checksums.properties"; //NOI18N
    private static final String DEPRECATED = "DEPRECATED"; //NOI18N

    public static CheckSums forContext(final Context context) throws IOException, NoSuchAlgorithmException {
        return new CheckSums(context);
    }

    private final Context context;
    private final Properties props = new Properties();
    private final MessageDigest md;

    private CheckSums(final Context context) throws IOException, NoSuchAlgorithmException {
        assert context != null;
        this.context = context;
        md = MessageDigest.getInstance("MD5"); //NOI18N
        load();
    }

    public boolean checkAndSet(final URL file, final Iterable<? extends TypeElement> topLevelElements, final Elements elements) {
        String fileId = file.toExternalForm();
        String sum = computeCheckSum(md, topLevelElements, elements);
        String value = (String) props.setProperty(fileId, sum);
        return value == null || value.equals(sum);
    }

    public void remove(URL file) {
        String fileId = file.toExternalForm();
        props.remove(fileId);
    }

    public void store () throws IOException {
        final File indexDir = FileUtil.toFile(context.getIndexFolder());
        final File f = new File (indexDir, CHECK_SUMS_FILE);
        try(final OutputStream out = new FileOutputStream(f)) {
            props.store(out, ""); //NOI18N
        }
    }

    private void load() throws IOException {
        final File indexDir = FileUtil.toFile(context.getIndexFolder());
        final File f = new File (indexDir, CHECK_SUMS_FILE);
        if (f.canRead()) {
            try (final InputStream in = new FileInputStream(f)) {
                props.load(in);
            } catch (IllegalArgumentException iae) {
                props.clear();
            }
        }
    }

    static String computeCheckSum(MessageDigest md, Iterable<? extends TypeElement> topLevelElements, Elements elements) {
        Queue<TypeElement> toHandle = new LinkedList<>();
        for (TypeElement te : topLevelElements) {
            toHandle.offer(te);
        }
        List<String> sigs = new ArrayList<String>();
        while (!toHandle.isEmpty()) {
            Element te = toHandle.poll();
            if (te == null) {
                //workaround for 6443073
                //see Symbol.java:601
                //see JavacTaskImpl.java:367
                continue;
            }
            sigs.add(String.valueOf(te.asType()) + getExtendedModifiers(elements, te));
            for (Element e : te.getEnclosedElements()) {
                switch (e.getKind()) {
                    case CLASS:
                    case INTERFACE:
                    case ENUM:
                    case ANNOTATION_TYPE:
                        if (!e.getModifiers().contains(Modifier.PRIVATE))
                            toHandle.offer((TypeElement) e);
                        break;
                    case CONSTRUCTOR:
                    case METHOD:
                    case FIELD:
                    case ENUM_CONSTANT:
                        if (!e.getModifiers().contains(Modifier.PRIVATE)) {
                            final StringBuilder sb = new StringBuilder();
                            sb.append(e.getSimpleName());
                            sb.append(String.valueOf(e.asType()));
                            sb.append(getExtendedModifiers(elements, e));
                            sigs.add(sb.toString());
                        }
                        break;                    
                }
            }
        }
        Collections.sort(sigs);
        StringBuilder sb = new StringBuilder();
        for (String s : sigs)
            sb.append(s);
        byte[] bytes = md.digest(sb.toString().getBytes());
        return new String(bytes);
    }

    private static String getExtendedModifiers(Elements elements, Element el) {
        StringBuilder sb = new StringBuilder();
        for (Modifier m : el.getModifiers())
            sb.append(m.name());
        if (elements.isDeprecated(el))
            sb.append(DEPRECATED);
        if (el.getKind() == ElementKind.FIELD) {
            Object v = ((VariableElement) el).getConstantValue();
            if (v != null) {
                sb.append(v.getClass().getName());
                sb.append(String.valueOf(v));
            }
        }
        return sb.toString();
    }
}
