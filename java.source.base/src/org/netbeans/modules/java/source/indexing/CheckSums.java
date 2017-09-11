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
