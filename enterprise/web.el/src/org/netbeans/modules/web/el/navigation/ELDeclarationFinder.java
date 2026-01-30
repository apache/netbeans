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
package org.netbeans.modules.web.el.navigation;

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.Node;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.modules.web.el.AstPath;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.ResourceBundles;
import org.netbeans.modules.web.el.completion.ELCompletionUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 * Simple DeclarationFinder based on the ELHyperlinkProvider code.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ELDeclarationFinder implements DeclarationFinder {

    private static final org.netbeans.modules.csl.api.ElementHandle DEFAULT_RESOURCE_BUNDLE_HANDLE = new ResourceBundleElementHandle();

    @Override
    public DeclarationLocation findDeclaration(final ParserResult info, int offset) {
        final Pair<Node, ELElement> nodeElem = ELHyperlinkProvider.resolveNodeAndElement(info.getSnapshot().getSource(), offset, new AtomicBoolean());
        if (nodeElem == null) {
            return DeclarationLocation.NONE;
        }
        final FileObject file = info.getSnapshot().getSource().getFileObject();
        final ClasspathInfo cp = ELTypeUtilities.getElimplExtendedCPI(file);
        final RefsHolder refs = new RefsHolder();
        final List<AlternativeLocation> alternatives = new ArrayList<>();
        try {
            JavaSource.create(cp).runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationContext context = CompilationContext.create(file, cc);

                    // resolve beans
                    Element javaElement = ELTypeUtilities.resolveElement(context, nodeElem.second(), nodeElem.first());
                    if (javaElement != null) {
                        refs.handle = ElementHandle.<Element>create(javaElement);
                        refs.fo = SourceUtils.getFile(refs.handle, cp);
                    }

                    // resolve resource bundles
                    ResourceBundles resourceBundles = ResourceBundles.get(file);
                    if (resourceBundles.canHaveBundles()) {
                        List<RefsHolder> bundleLocations = getBundleLocations(context, resourceBundles, nodeElem);
                        if (!bundleLocations.isEmpty()) {
                            refs.fo = bundleLocations.get(0).fo;
                            refs.offset = bundleLocations.get(0).offset;
                            refs.elementHandle = bundleLocations.get(0).elementHandle;
                            for (RefsHolder location : bundleLocations) {
                                alternatives.add(new ResourceBundleAlternative(location.fo, location.offset, location.lineNumber));
                            }
                        }
                    }
                }
            }, true);
            if (refs.fo != null) {
                JavaSource javaSource = JavaSource.forFileObject(refs.fo);
                if (javaSource != null) {
                    // java bean
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                        @Override
                        public void run(CompilationController controller) throws Exception {
                            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element element = refs.handle.resolve(controller);
                            Trees trees = controller.getTrees();
                            Tree tree = trees.getTree(element);
                            SourcePositions sourcePositions = trees.getSourcePositions();
                            refs.offset = (int) sourcePositions.getStartPosition(controller.getCompilationUnit(), tree);
                        }
                    }, true);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (refs.fo != null && refs.offset != -1) {
            DeclarationLocation declarationLocation = new DeclarationLocation(refs.fo, refs.offset, refs.elementHandle);
            for (AlternativeLocation alternativeLocation : alternatives) {
                declarationLocation.addAlternative(alternativeLocation);
            }
            return declarationLocation;
        }
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final AtomicReference<OffsetRange> ret = new AtomicReference<>(OffsetRange.NONE);
        doc.render(new Runnable() {
            @Override
            public void run() {
                int[] offsets = ELHyperlinkProvider.getELIdentifierSpan(doc, caretOffset);
                if (offsets != null) {
                    ret.set(new OffsetRange(offsets[0], offsets[1]));
                }
            }
        });
        return ret.get();
    }

    private static List<RefsHolder> getBundleLocations(CompilationContext ccontext, ResourceBundles resourceBundles, Pair<Node, ELElement> nodeElem) {
        if (nodeElem.first() instanceof AstIdentifier) {
            List<ResourceBundles.Location> locations = resourceBundles.getLocationsForBundleIdent(ccontext.context(), nodeElem.first().getImage());
            return locations.stream().map(location -> new RefsHolder(location.getFile(), location.getOffset())).toList();
        } else {
            AstPath astPath = new AstPath(nodeElem.second().getNode());
            for (Node node : astPath.rootToLeaf()) {
                String image = nodeElem.first().getImage();
                if (node instanceof AstIdentifier && node.getImage() != null && image != null) {
                    if (image.length() > 2 && (image.startsWith("'") && image.endsWith("'")) //NOI18N
                            || (image.startsWith("\"") && image.endsWith("\""))) { //NOI18N
                        // bundle['key']
                        image = image.substring(1, image.length() - 1);
                    }
                    final String key = image;
                    List<ResourceBundles.Location> locations = resourceBundles.getLocationsForBundleKey(ccontext.context(), node.getImage(), key);
                    final String value;
                    if (!locations.isEmpty()) {
                        value = resourceBundles.getValueWithVarName(ccontext.context(), node.getImage(), key);
                    } else {
                        value = null;
                    }
                    return locations.stream().map(location -> new RefsHolder(location.getFile(), location.getOffset(),
                            ELCompletionUtil.getResourceBundleElementHandle(key, value, nodeElem.second(), location.getFile()), location.getLineNumber())).toList();
                }
            }
        }
        return Collections.emptyList();
    }

    private static class ResourceBundleAlternative implements AlternativeLocation {

        private final FileObject file;
        private final int offset;
        private final int lineNumber;

        public ResourceBundleAlternative(FileObject file, int offset, int lineNumber) {
            this.offset = offset;
            this.file = file;
            this.lineNumber = lineNumber;
        }

        @Override
        public org.netbeans.modules.csl.api.ElementHandle getElement() {
            return DEFAULT_RESOURCE_BUNDLE_HANDLE;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.emphasis(true);
            formatter.appendText(file.getName());
            formatter.emphasis(false);
            formatter.appendText(" in ");

            //add a link to the file relative to the web root
            FileObject pathRoot = ProjectWebRootQuery.getWebRoot(file);
            String path = null;
            if (pathRoot != null) {
                path = FileUtil.getRelativePath(pathRoot, file); //this may also return null
            }
            if (path == null) {
                Project project = FileOwnerQuery.getOwner(file);
                if (project != null) {
                    pathRoot = project.getProjectDirectory();
                    path = FileUtil.getRelativePath(pathRoot, file); //this may also return null
                }
            }
            if (path == null) {
                //if everything fails, just use the absolute path
                path = file.getPath();
            }

            formatter.appendHtml("<i>");
            formatter.appendText(path);
            formatter.appendHtml("</i>");
            if (lineNumber > -1) {
                formatter.appendText(":"); //NOI18N
                formatter.appendText(String.valueOf(lineNumber + 1)); //line numbers are counted from zero, but in editor lines starts with one.
            }
            return formatter.getText();
        }

        @Override
        public DeclarationLocation getLocation() {
            return new DeclarationLocation(file, offset);
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            return getComparableString(this).compareTo(getComparableString(o));
        }

        private static String getComparableString(AlternativeLocation loc) {
            DeclarationLocation location = loc.getLocation();
            FileObject fileObject = location.getFileObject();
            if (fileObject != null) {
                return String.valueOf(location.getOffset()) + fileObject.getPath();
            } else {
                return String.valueOf(location.getOffset());
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.file);
            hash = 89 * hash + this.offset;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ResourceBundleAlternative other = (ResourceBundleAlternative) obj;
            if (!Objects.equals(this.file, other.file)) {
                return false;
            }
            if (this.offset != other.offset) {
                return false;
            }
            return true;
        }
    }

    private static class RefsHolder {

        ElementHandle<Element> handle;
        FileObject fo;
        int offset = -1;
        int lineNumber = -1;
        org.netbeans.modules.csl.api.ElementHandle elementHandle;

        RefsHolder() {
        }

        RefsHolder(FileObject fo, int offset) {
            this.fo = fo;
            this.offset = offset;
        }

        public RefsHolder(FileObject fo, int offset, org.netbeans.modules.csl.api.ElementHandle elementHandle, int lineNumber) {
            this.fo = fo;
            this.offset = offset;
            this.elementHandle = elementHandle;
            this.lineNumber = lineNumber;
        }

    }

    private static class ResourceBundleElementHandle implements org.netbeans.modules.csl.api.ElementHandle {

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return "";
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FILE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
    }
}
