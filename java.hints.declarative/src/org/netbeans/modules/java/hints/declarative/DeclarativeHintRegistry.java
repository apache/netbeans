/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.declarative;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result2;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.FixTextDescription;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.HintTextDescription;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.Result;
import org.netbeans.modules.java.hints.providers.spi.ClassPathBasedHintProvider;
import org.netbeans.modules.java.hints.providers.spi.HintProvider;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.AdditionalQueryConstraints;
import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Jan Lahoda
 */
@ServiceProviders({
    @ServiceProvider(service=HintProvider.class),
    @ServiceProvider(service=ClassPathBasedHintProvider.class)
})
public class DeclarativeHintRegistry implements HintProvider, ClassPathBasedHintProvider {

    private static final RequestProcessor ASYNCHRONOUS = new RequestProcessor(DeclarativeHintRegistry.class.getName(), 10, false, false);
    private static final Logger LOG = Logger.getLogger(DeclarativeHintRegistry.class.getName());

    public Map<HintMetadata, Collection<? extends HintDescription>> computeHints() {
        return readHints(findGlobalFiles());
    }

    @Override
    public Collection<? extends HintDescription> computeHints(final ClassPath cp, AtomicBoolean cancel) {
        final AtomicReference<Collection<? extends FileObject>> foundFiles = new AtomicReference<>();

        Task task = ASYNCHRONOUS.post(new Runnable() {
            @Override public void run() {
                foundFiles.set(findFiles(cp));
            }
        });

        while ((cancel == null || !cancel.get()) && !task.isFinished()) {
            try {
                task.waitFinished(1);
            } catch (InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }

        Collection<? extends FileObject> files = foundFiles.get();

        if (files == null || (cancel != null && cancel.get())) return null;

        return join(readHints(files));
    }

    public static Collection<? extends HintDescription> join(Map<HintMetadata, Collection<? extends HintDescription>> hints) {
        List<HintDescription> descs = new LinkedList<HintDescription>();

        for (Collection<? extends HintDescription> c : hints.values()) {
            descs.addAll(c);
        }

        return descs;
    }

    private Map<HintMetadata, Collection<? extends HintDescription>> readHints(Iterable<? extends FileObject> files) {
        Map<HintMetadata, Collection<? extends HintDescription>> result = new HashMap<HintMetadata, Collection<? extends HintDescription>>();

        for (FileObject f : files) {
            result.putAll(parseHintFile(f));
        }

        return result;
    }

    public static Collection<? extends FileObject> findAllFiles() {
        List<FileObject> files = new LinkedList<FileObject>();

        files.addAll(findGlobalFiles());
        files.addAll(findFiles(GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT)));
        files.addAll(findFiles(GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE)));
        files.addAll(findFiles(GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)));

        return files;
    }

    private static Collection<? extends FileObject> findFiles(Iterable<? extends ClassPath> cps) {
        List<FileObject> result = new LinkedList<FileObject>();

        for (ClassPath cp : cps) {
            result.addAll(findFiles(cp));
        }

        return result;
    }

    private static Collection<? extends FileObject> findFiles(ClassPath cp) {
        List<FileObject> roots = new ArrayList<>();

        for (Entry binaryEntry : cp.entries()) {
            Result2 sources = SourceForBinaryQuery.findSourceRoots2(binaryEntry.getURL());

            if (sources.preferSources()) {
                roots.addAll(Arrays.asList(sources.getRoots()));
            } else {
                FileObject binaryRoot = binaryEntry.getRoot();

                if (binaryRoot != null)
                    roots.add(binaryRoot);
            }
        }

        List<FileObject> result = new LinkedList<>();

        for (FileObject root : roots) {
            FileObject folder = root.getFileObject("META-INF/upgrade");

            if (folder != null) {
                result.addAll(findFiles(folder));
            }
        }

        return result;
    }

    private static Collection<? extends FileObject> findGlobalFiles() {
        Collection<FileObject> result = new ArrayList<FileObject>();
        FileObject folder = FileUtil.getConfigFile("org-netbeans-modules-java-hints/declarative");

        if (folder != null) {
            result.addAll(findFilesRecursive(folder));
        }
        
        folder = FileUtil.getConfigFile("rules");
        
        if (folder != null) {
            result.addAll(findFilesRecursive(folder));
        }
        
        return result;
    }

    private static Collection<? extends FileObject> findFiles(FileObject folder) {
        List<FileObject> result = new LinkedList<FileObject>();

        for (FileObject f : folder.getChildren()) {
            if (!"hint".equals(f.getExt())) {
                continue;
            }
            result.add(f);
        }

        return result;
    }

    private static Collection<? extends FileObject> findFilesRecursive(FileObject folder) {
        List<FileObject> todo = new LinkedList<FileObject>();
        List<FileObject> result = new LinkedList<FileObject>();

        todo.add(folder);

        while (!todo.isEmpty()) {
            FileObject f = todo.remove(0);

            if (f.isFolder()) {
                todo.addAll(Arrays.asList(f.getChildren()));
                continue;
            }
            if (!"hint".equals(f.getExt())) {
                continue;
            }
            result.add(f);
        }

        return result;
    }

    public static Map<HintMetadata, Collection<? extends HintDescription>> parseHintFile(@NonNull FileObject file) {
        String spec = Utilities.readFile(file);

        return spec != null ? parseHints(file, spec) : Collections.<HintMetadata, Collection<? extends HintDescription>>emptyMap();
    }

    public static Map<HintMetadata, Collection<? extends HintDescription>> parseHints(@NullAllowed FileObject file, String spec) {
        ResourceBundle bundle;

        try {
            if (file != null) {
                ClassLoader l = new URLClassLoader(new URL[] {file.getParent().getURL()});

                bundle = NbBundle.getBundle("Bundle", Locale.getDefault(), l);
            } else {
                bundle = null;
            }
        } catch (FileStateInvalidException ex) {
            bundle = null;
        } catch (MissingResourceException ex) {
            //TODO: log?
            bundle = null;
        }

        TokenHierarchy<?> h = TokenHierarchy.create(spec, DeclarativeHintTokenId.language());
        TokenSequence<DeclarativeHintTokenId> ts = h.tokenSequence(DeclarativeHintTokenId.language());
        Map<HintMetadata, Collection<HintDescription>> result = new LinkedHashMap<HintMetadata, Collection<HintDescription>>();
        Result parsed = new DeclarativeHintsParser().parse(file, spec, ts);

        HintMetadata meta;
        String id = parsed.options.get("hint");
        String fallbackDisplayName = file != null ? file.getName() : null;
        String description = parsed.options.get("description");
        String srcVersion = parsed.options.get("minSourceVersion");
        if (description == null) {
            description = fallbackDisplayName;
        }
        if (id == null && file != null) {
            id = file.getNameExt();
        }
        
        if (id != null) {
            String cat = parsed.options.get("hint-category");

            if (cat == null) {
                if ("rules".equals(file.getParent().getName())) {
                    cat = "custom";
                } else {
                    cat = "classpathbased";
                }
            }

            String[] w = suppressWarnings(parsed.options);

            meta = HintMetadata.Builder.
                    create(id).
                    setBundle(bundle, fallbackDisplayName, description).
                    setCategory(cat).
                    addSuppressWarnings(w).
                    setSourceVersion(srcVersion).
                    build();
        } else {
            meta = null;
        }

        int count = 0;

        for (HintTextDescription hint : parsed.hints) {
            HintDescriptionFactory f = HintDescriptionFactory.create();
            String displayName = resolveDisplayName(file, bundle, hint.displayName, true, "TODO: No display name");
            Map<String, String> constraints = Utilities.conditions2Constraints(hint.conditions);
            String imports = parsed.importsBlock != null ? spec.substring(parsed.importsBlock[0], parsed.importsBlock[1]) : "";
            String[] importsArray = parsed.importsBlock != null ? new String[] {spec.substring(parsed.importsBlock[0], parsed.importsBlock[1])} : new String[0];
            String pattern = spec.substring(hint.textStart, hint.textEnd);

            f = f.setTrigger(PatternDescription.create(pattern, constraints, importsArray));

            List<DeclarativeFix> fixes = new LinkedList<DeclarativeFix>();

            for (FixTextDescription fix : hint.fixes) {
                int[] fixRange = fix.fixSpan;
                String fixDisplayName = resolveDisplayName(file, bundle, fix.displayName, false, null);
                Map<String, String> options = new HashMap<String, String>(parsed.options);

                options.putAll(fix.options);

                fixes.add(DeclarativeFix.create(fixDisplayName, spec.substring(fixRange[0], fixRange[1]), fix.conditions, options));
            }

            HintMetadata currentMeta = meta;

            if (currentMeta == null || hint.options.get("hint") != null) {
                String[] w = suppressWarnings(hint.options);
                String currentId = hint.options.get("hint");
                String cat = parsed.options.get("hint-category");

                if (cat == null) {
                    if (file != null && "rules".equals(file.getParent().getName())) {
                        cat = "custom";
                    } else {
                        cat = "general";
                    }
                }

                if (currentId != null) {
                    currentMeta = HintMetadata.Builder.
                            create(currentId).setBundle(bundle).
                            setCategory(cat).
                            addSuppressWarnings(w).
                            setSourceVersion(srcVersion).
                            build();
                } else {
                    currentId = file != null ? file.getNameExt() + "-" + count : String.valueOf(count);
                    currentMeta = HintMetadata.Builder.
                            create(currentId).
                            setDescription(displayName, "No Description").
                            setCategory(cat).
                            addSuppressWarnings(w).
                            setSourceVersion(srcVersion).
                            build();
                }
            }

            Map<String, String> options = new HashMap<String, String>(parsed.options);

            options.putAll(hint.options);

            f = f.setWorker(new DeclarativeHintsWorker(displayName, pattern, hint.conditions, imports, fixes, options));
            f = f.setMetadata(currentMeta);
            f = f.setAdditionalConstraints(new AdditionalQueryConstraints(new HashSet<String>(constraints.values())));
            f = f.setHintText(spec.substring(hint.textStart, hint.hintEnd));

            Collection<HintDescription> hints = result.get(currentMeta);

            if (hints == null) {
                result.put(currentMeta, hints = new LinkedList<HintDescription>());
            }

            if (fixes.isEmpty()) {
                f.addOptions(Options.QUERY);
            }
            
            hints.add(f.produce());

            count++;
        }

        if (meta != null && result.isEmpty()) {
            result.put(meta, Collections.<HintDescription>emptyList());
        }

        return new LinkedHashMap<HintMetadata, Collection<? extends HintDescription>>(result);
    }

    private static String[] suppressWarnings(Map<String, String> options) {
        String suppressWarnings = options.get("suppress-warnings");

        if (suppressWarnings != null) {
            return suppressWarnings.split(",");
        } else {
            return new String[0];
        }
    }

    private static @NonNull String resolveDisplayName(@NonNull FileObject hintFile, @NullAllowed ResourceBundle bundle, String displayNameSpec, boolean fallbackToFileName, String def) {
        if (bundle != null) {
            if (displayNameSpec == null) {
                if (!fallbackToFileName) {
                    return def;
                }

                String dnKey = "DN_" + hintFile.getName();
                try {
                    return bundle.getString(dnKey);
                } catch (MissingResourceException e) {
                    Logger.getLogger(DeclarativeHintRegistry.class.getName()).log(Level.FINE, null, e);
                    return fileDefaultDisplayName(hintFile, def);
                }
            }

            if (displayNameSpec.startsWith("#")) {
                String dnKey = "DN_" + displayNameSpec.substring(1);
                try {
                    return bundle.getString(dnKey);
                } catch (MissingResourceException e) {
                    Logger.getLogger(DeclarativeHintRegistry.class.getName()).log(Level.FINE, null, e);
                    return "XXX: missing display name key in the bundle (key=" + dnKey + ")";
                }
            }
        }

        return displayNameSpec != null ? displayNameSpec
                                       : fallbackToFileName ? fileDefaultDisplayName(hintFile, def)
                                                            : def;
    }

    private static @NonNull String fileDefaultDisplayName(@NullAllowed FileObject hintFile, String def) {
        if (hintFile == null) {
            return def;
        }

        return hintFile.getName();
    }

}
