/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
