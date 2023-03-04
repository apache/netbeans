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
package org.netbeans.modules.java.api.common.project.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleNode extends AbstractNode implements ChangeListener {
    @StaticResource
    private static final String MODULE_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/module.png"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ModuleNode.class);

    private final URI uri;
    private final AtomicReference<SourceForBinaryQuery.Result2> resCache;
    private final AtomicReference<String> descCache;

    ModuleNode(
            @NonNull final String moduleName,
            @NonNull final URI uri,
            @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
            @NullAllowed final Consumer<Pair<String,String>> postRemoveAction) {
        super(Children.LEAF, createLookup(moduleName, uri, preRemoveAction, postRemoveAction));
        this.uri = uri;
        this.resCache = new AtomicReference<>();
        this.descCache = new AtomicReference<>();
        setName(moduleName);
        setIconBaseWithExtension(MODULE_ICON);
    }

    @Override
    public String getShortDescription() {
        String res = descCache.get();
        if (res == null) {
            RP.execute(() -> {
                try {
                    SourceForBinaryQuery.Result2 sfbq = resCache.get();
                    if (sfbq == null) {
                        sfbq  = SourceForBinaryQuery.findSourceRoots2(uri.toURL());
                        if (resCache.compareAndSet(null, sfbq)) {
                            sfbq.addChangeListener(WeakListeners.change(this, sfbq));
                        } else {
                            sfbq = resCache.get();
                        }
                    }
                    descCache.set(Arrays.stream(sfbq.getRoots())
                            .map(ModuleNode::getModuleFolder)
                            .map(FileUtil::getFileDisplayName)
                            .collect(Collectors.joining(File.pathSeparator)));
                    fireShortDescriptionChange(null, null);
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
        return res;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context) {
            return super.getActions(context);
        } else {
            return new Action[] {
                SystemAction.get (ShowJavadocAction.class),
                SystemAction.get (RemoveClassPathRootAction.class)
            };
        }
    }

    @Override
    public Action getPreferredAction () {
        return null;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        descCache.set(null);
    }

    private static Lookup createLookup(
            @NonNull final String moduleName,
            @NonNull final URI binUri,
            @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
            @NullAllowed final Consumer<Pair<String,String>> postRemoveAction) {
        return Lookups.fixed(
                new JavadocProvider(moduleName, binUri),
                new Remove(preRemoveAction, postRemoveAction));
    }

    @NonNull
    private static FileObject getModuleFolder(@NonNull FileObject srcRoot) {
        final ClassPath msp = ClassPath.getClassPath(srcRoot, JavaClassPathConstants.MODULE_SOURCE_PATH);
        if (msp == null) {
            return srcRoot;
        }
        final FileObject owner = msp.findOwnerRoot(srcRoot);
        if (owner == null) {
            return srcRoot;
        }
        FileObject prev = srcRoot;
        while (!srcRoot.equals(owner)) {
            prev = srcRoot;
            srcRoot = srcRoot.getParent();
        }
        return prev;
    }

    private static final class Remove implements RemoveClassPathRootAction.Removable {
        private final Consumer<Pair<String,String>> preRemoveAction;
        private final Consumer<Pair<String,String>> postRemoveAction;

        Remove(
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction) {
            this.preRemoveAction = preRemoveAction;
            this.postRemoveAction = postRemoveAction;
        }

        @Override
        public boolean canRemove() {
            return preRemoveAction != null || postRemoveAction != null;
        }

        @Override
        public Project remove() {
            return null;
        }

        @Override
        public void beforeRemove() {
            if (preRemoveAction != null) {
                preRemoveAction.accept(null);
            }
        }

        @Override
        public void afterRemove() {
            if (postRemoveAction != null) {
                postRemoveAction.accept(null);
            }
        }
    }

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider, ChangeListener {

        private final String moduleName;
        private final URI uri;
        private final AtomicReference<JavadocForBinaryQuery.Result> resCache;
        private final AtomicReference<URL[]> rootsCache;

        JavadocProvider (
                @NonNull final String moduleName,
                @NonNull final URI binUri) {
            this.moduleName = moduleName;
            this.uri = binUri;
            this.resCache = new AtomicReference<>();
            this.rootsCache = new AtomicReference<>();
        }

        @Override
        public boolean hasJavadoc() {
            return findJavadoc().length > 0;
        }

        @Override
        public void showJavadoc() {
            final URL[] urls = findJavadoc();
            URL pageURL = ShowJavadocAction.findJavadoc("overview-summary.html",urls);  //NOI18N
            if (pageURL == null) {
                pageURL = ShowJavadocAction.findJavadoc("index.html",urls);             //NOI18N
            }
            final String message = Optional.ofNullable(FileOwnerQuery.getOwner(uri))
                    .map(ProjectUtils::getInformation)
                    .map(ProjectInformation::getDisplayName)
                    .map((pn) -> NbBundle.getMessage(ModuleNode.class, "TXT_ModuleInProject", moduleName, pn))
                    .orElse(NbBundle.getMessage(ModuleNode.class, "TXT_Module",  moduleName));
            ShowJavadocAction.showJavaDoc (pageURL, message);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            rootsCache.set(null);
        }

        private URL[] findJavadoc() {
            URL[] roots = rootsCache.get();
            if (roots == null) {
                JavadocForBinaryQuery.Result res = resCache.get();
                if (res == null) {
                    try {
                        res = JavadocForBinaryQuery.findJavadoc(uri.toURL());
                        if (resCache.compareAndSet(null, res)) {
                            res.addChangeListener(WeakListeners.change(this, res));
                        } else {
                            res = resCache.get();
                        }
                    } catch (MalformedURLException e) {
                        //pass with res null
                    }
                }
                roots = res == null ?
                    new URL[0] :
                    res.getRoots();
                rootsCache.set(roots);
            }
            return roots;
        }
    }
}
