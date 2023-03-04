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
package org.netbeans.modules.java.openjdk.editor;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.nio.charset.Charset;
import java.util.Enumeration;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.openjdk.project.SourcesImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType="text/x-java", position=0, service=HyperlinkProviderExt.class)
public class JavaToCHyperlinkProvider implements HyperlinkProviderExt {

    private static final SourceGroup[] NO_GROUPS = new SourceGroup[0];

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return findNext().isHyperlinkPoint(doc, offset, type);
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return findNext().getHyperlinkSpan(doc, offset, type);
    }

    @Override
    public void performClickAction(Document doc, final int offset, HyperlinkType type) {
        FileObject file = NbEditorUtilities.getFileObject(doc);
        Project prj = file != null ? FileOwnerQuery.getOwner(file) : null;
        SourceGroup[] nativeGroups = prj != null ? ProjectUtils.getSources(prj).getSourceGroups(SourcesImpl.SOURCES_TYPE_JDK_PROJECT_NATIVE)
                                                 : NO_GROUPS;
        if (nativeGroups.length == 0) {
            findNext().performClickAction(doc, offset, type);
            return ;
        }

        final String[][] lookFor = new String[1][];

        try {
            final JavaSource source = JavaSource.forDocument(doc);
            if (source != null) {
                source.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController parameter) throws Exception {
                        parameter.toPhase(Phase.PARSED);

                        TreePath tp = parameter.getTreeUtilities().pathFor(offset);

                        if (tp.getLeaf().getKind() != Kind.METHOD || !((MethodTree) tp.getLeaf()).getModifiers().getFlags().contains(Modifier.NATIVE))
                            return ;

                        int[] nameSpan = parameter.getTreeUtilities().findNameSpan((MethodTree) tp.getLeaf());

                        if (nameSpan[0] <= offset && offset <= nameSpan[1]) {
                            parameter.toPhase(Phase.RESOLVED);
                            Element el = parameter.getTrees().getElement(tp);
                            if (el != null && el.getKind() == ElementKind.METHOD) {
                                lookFor[0] = SourceUtils.getJVMSignature(ElementHandle.create(el));
                            }
                        }
                    }
                }, true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (lookFor[0] == null) {
            findNext().performClickAction(doc, offset, type);
            return ;
        }

        String mangledBase = "Java_" + mangle(lookFor[0][0]) + "_" + mangle(lookFor[0][1]);
        String mangledWithParameters = mangledBase + "__" + mangle(lookFor[0][2].replaceAll("\\((.*)\\).*", "$1"));

        FileObject fileToOpen = null;
        int targetOffset = -1;

        OUTER: for (SourceGroup root : nativeGroups) {
            Charset encoding = FileEncodingQuery.getEncoding(root.getRootFolder());
            Enumeration<? extends FileObject> en = root.getRootFolder().getChildren(true);
            while (en.hasMoreElements()) {
                FileObject current = en.nextElement();
                if (current.hasExt("c") || current.hasExt("cc") || current.hasExt("cpp")) { //c++??
                    try {
                        String content = new String(current.asBytes(), encoding);
                        int pos = content.indexOf(mangledWithParameters);
                        if (pos == (-1)) {
                            pos = content.indexOf(mangledBase);
                        }
                        if (pos != (-1)) {
                            fileToOpen = current;
                            targetOffset = pos;
                            break OUTER;
                        }
                    } catch (IOException ex) {
                        //TODO:
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        if (fileToOpen != null) {
            UiUtils.open(fileToOpen, targetOffset);
        }
    }

    private String mangle(String original) {
        //no unicode mangling - is that needed?
        StringBuilder mangled = new StringBuilder();

        for (char c : original.toCharArray()) {
            switch (c) {
                case '.':
                case '/': mangled.append("_"); break;
                case '_': mangled.append("_1"); break;
                case ';': mangled.append("_2"); break;
                case '[': mangled.append("_3"); break;
                default: mangled.append(c); break;
            }
        }

        return mangled.toString();
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return findNext().getTooltipText(doc, offset, type);
    }

    private HyperlinkProviderExt findNext() {
        boolean seenMe = false;

        for (HyperlinkProviderExt p : MimeLookup.getLookup("text/x-java").lookupAll(HyperlinkProviderExt.class)) {
            if (seenMe) return p;
            seenMe = p == this;
        }

        return new HyperlinkProviderExt() {
            @Override public Set<HyperlinkType> getSupportedHyperlinkTypes() {
                return EnumSet.noneOf(HyperlinkType.class);
            }
            @Override public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
                return false;
            }
            @Override public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
                return new int[] {0, 0};
            }
            @Override public void performClickAction(Document doc, int offset, HyperlinkType type) {
            }
            @Override public String getTooltipText(Document doc, int offset, HyperlinkType type) {
                return null;
            }
        };
    }
}
