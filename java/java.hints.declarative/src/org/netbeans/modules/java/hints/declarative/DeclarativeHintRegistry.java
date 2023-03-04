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

    @Override
    public Map<HintMetadata, Collection<? extends HintDescription>> computeHints() {
        return readHints(findGlobalFiles());
    }

    @Override
    public Collection<? extends HintDescription> computeHints(final ClassPath cp, AtomicBoolean cancel) {
        final AtomicReference<Collection<? extends FileObject>> foundFiles = new AtomicReference<>();

        Task task = ASYNCHRONOUS.post(() -> foundFiles.set(findFiles(cp)));

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
        List<HintDescription> descs = new LinkedList<>();

        for (Collection<? extends HintDescription> c : hints.values()) {
            descs.addAll(c);
        }

        return descs;
    }

    private Map<HintMetadata, Collection<? extends HintDescription>> readHints(Iterable<? extends FileObject> files) {
        Map<HintMetadata, Collection<? extends HintDescription>> result = new HashMap<>();

        for (FileObject f : files) {
            result.putAll(parseHintFile(f));
        }

        return result;
    }

    public static Collection<? extends FileObject> findAllFiles() {
        List<FileObject> files = new ArrayList<>();

        files.addAll(findGlobalFiles());
        files.addAll(findFiles(GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT)));
        files.addAll(findFiles(GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE)));
        files.addAll(findFiles(GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)));

        return files;
    }

    private static Collection<? extends FileObject> findFiles(Iterable<? extends ClassPath> cps) {
        List<FileObject> result = new LinkedList<>();

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
        Collection<FileObject> result = new ArrayList<>();
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
        List<FileObject> result = new LinkedList<>();

        for (FileObject f : folder.getChildren()) {
            if (!"hint".equals(f.getExt())) {
                continue;
            }
            result.add(f);
        }

        return result;
    }

    private static Collection<? extends FileObject> findFilesRecursive(FileObject folder) {
        List<FileObject> todo = new LinkedList<>();
        List<FileObject> result = new LinkedList<>();

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

        return spec != null ? parseHints(file, spec) : Collections.emptyMap();
    }

    public static Map<HintMetadata, Collection<? extends HintDescription>> parseHints(@NullAllowed FileObject file, String spec) {
        ResourceBundle bundle;

        try {
            if (file != null) {
                ClassLoader l = new URLClassLoader(new URL[] {file.getParent().toURL()});

                bundle = NbBundle.getBundle("Bundle", Locale.getDefault(), l);
            } else {
                bundle = null;
            }
        } catch (MissingResourceException ex) {
            //TODO: log?
            bundle = null;
        }

        TokenHierarchy<?> h = TokenHierarchy.create(spec, DeclarativeHintTokenId.language());
        TokenSequence<DeclarativeHintTokenId> ts = h.tokenSequence(DeclarativeHintTokenId.language());
        Map<HintMetadata, Collection<HintDescription>> result = new LinkedHashMap<>();
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
            HintDescriptionFactory fac = HintDescriptionFactory.create();
            String displayName = resolveDisplayName(file, bundle, hint.displayName, true, "TODO: No display name");
            Map<String, String> constraints = Utilities.conditions2Constraints(hint.conditions);
            String imports = parsed.importsBlock != null ? spec.substring(parsed.importsBlock[0], parsed.importsBlock[1]) : "";
            String[] importsArray = parsed.importsBlock != null ? new String[] {spec.substring(parsed.importsBlock[0], parsed.importsBlock[1])} : new String[0];
            String pattern = spec.substring(hint.textStart, hint.textEnd);

            fac.setTrigger(PatternDescription.create(pattern, constraints, importsArray));

            List<DeclarativeFix> fixes = new LinkedList<>();

            for (FixTextDescription fix : hint.fixes) {
                int[] fixRange = fix.fixSpan;
                String fixDisplayName = resolveDisplayName(file, bundle, fix.displayName, false, null);
                Map<String, String> options = new HashMap<>(parsed.options);

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

            Map<String, String> options = new HashMap<>(parsed.options.size() + hint.options.size());
            options.putAll(parsed.options);
            options.putAll(hint.options);

            fac.setWorker(new DeclarativeHintsWorker(displayName, pattern, hint.conditions, imports, fixes, options));
            fac.setMetadata(currentMeta);
            fac.setAdditionalConstraints(new AdditionalQueryConstraints(new HashSet<>(constraints.values())));
            fac.setHintText(spec.substring(hint.textStart, hint.hintEnd));

            Collection<HintDescription> hints = result.get(currentMeta);

            if (hints == null) {
                result.put(currentMeta, hints = new LinkedList<>());
            }

            if (fixes.isEmpty()) {
                fac.addOptions(Options.QUERY);
            }
            
            hints.add(fac.produce());

            count++;
        }

        if (meta != null && result.isEmpty()) {
            result.put(meta, Collections.<HintDescription>emptyList());
        }

        return new LinkedHashMap<>(result);
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
