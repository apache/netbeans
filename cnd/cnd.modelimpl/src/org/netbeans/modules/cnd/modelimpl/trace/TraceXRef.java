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
package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Offsetable;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.impl.services.ReferenceRepositoryImpl;
import org.netbeans.modules.cnd.modelimpl.trace.XRefResultSet.ContextEntry;
import org.netbeans.modules.cnd.modelimpl.trace.XRefResultSet.DeclarationScope;
import org.netbeans.modules.cnd.modelimpl.trace.XRefResultSet.IncludeLevel;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmReferenceStorage;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.CharSequences;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 */
public class TraceXRef extends TraceModel {

    private String refFile = "";
    private String declarationName = "";
    private int line = 0;
    private int column = 0;

    public TraceXRef() {
        super(true);
    }

    public static void main(String[] args) {
        setUp();
        TraceXRef trace = new TraceXRef();
        trace.test(args);
    }

    private void test(String[] args) {
        try {
            processArguments(args);
            doTest();
            long time = 0;
            if (super.isShowTime()) {
                time = System.currentTimeMillis();
            }
            CsmObject object = null;
            if (declarationName.length() > 0) {
                System.err.println("looking for object with name: " + declarationName);
                object = super.getProject().findDeclaration(declarationName);
                if (object == null) {
                    System.err.println("No object with name " + declarationName + " in model");
                }
            } else if ((refFile.length() > 0) && (line > 0) && (column > 0)) {
                System.out.println("looking for object on position: line=" + line + " column=" + column); // NOI18N
                System.out.println("in file:" + refFile); // NOI18N
                CsmFile file = getCsmFile(refFile);
                if (!(file instanceof FileImpl)) {
                    System.err.println("No CsmFile was found with name: " + refFile);
                } else {
                    FileImpl implFile = (FileImpl) file;
                    int offset = implFile.getOffset(line, column);
                    if (offset < 0) {
                        System.err.println("incorrect offset for position line=" + line + " col=" + column);
                    } else {
                        CsmReference ref = CsmReferenceResolver.getDefault().findReference(implFile, null, offset);
                        if (ref == null) {
                            System.err.println("no any references were found on position line=" + line + " col=" + column);
                        } else {
                            object = ref.getReferencedObject();
                        }
                    }
                }
            } else {
                System.err.println("To run xref tests start script with parameter:");
                System.err.println("should be --xref#file_path#1_based_line#1_based_column or --xref#name");
            }
            if (object == null) {
                System.out.println("Nothing to search"); // NOI18N
            } else {
                System.out.println("TARGET OBJECT IS\n  " + CsmTracer.toString(object)); // NOI18N
                if (CsmKindUtilities.isNamedElement(object)) {
                    System.out.println("NAME IS: " + ((CsmNamedElement) object).getName()); // NOI18N
                }
                if (CsmKindUtilities.isDeclaration(object)) {
                    System.out.println("UNIQUE NAME IS: " + ((CsmDeclaration) object).getUniqueName()); // NOI18N
                }

                ReferenceRepositoryImpl xRefRepository = new ReferenceRepositoryImpl();
                CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(object, true);
                CsmObject decl = decDef[0];
                CsmObject def = decDef[1];
                Collection<CsmReference> refs = xRefRepository.getReferences(decl, getProject(), CsmReferenceKind.ALL, Interrupter.DUMMY);
                if (super.isShowTime()) {
                    time = System.currentTimeMillis() - time;
                }
                traceRefs(refs, decl, def, System.out);
                if (super.isShowTime()) {
                    System.out.println("search took " + time + "ms"); // NOI18N
                }
            }
        } finally {
            super.shutdown(true);
            APTDriver.close();
            ClankDriver.close();
            APTFileCacheManager.close();
        }
    }

    @SuppressWarnings("deprecation")
    private static void setUp() {
        // this is the only way to init extension-based recognizer
        FileUtil.setMIMEType("cc", MIMENames.CPLUSPLUS_MIME_TYPE); // NOI18N
        FileUtil.setMIMEType("h", MIMENames.HEADER_MIME_TYPE); // NOI18N
        FileUtil.setMIMEType("c", MIMENames.C_MIME_TYPE); // NOI18N

        JEditorPane.registerEditorKitForContentType(MIMENames.CPLUSPLUS_MIME_TYPE, "org.netbeans.modules.cnd.editor.cplusplus.CCKit"); // NOI18N
        JEditorPane.registerEditorKitForContentType(MIMENames.HEADER_MIME_TYPE, "org.netbeans.modules.cnd.editor.cplusplus.HKit"); // NOI18N
        JEditorPane.registerEditorKitForContentType(MIMENames.C_MIME_TYPE, "org.netbeans.modules.cnd.editor.cplusplus.CKit"); // NOI18N
    }

    private CsmFile getCsmFile(String path) {
        return super.getProject().findFile(new java.io.File(path).getAbsolutePath(), true, false);
    }

    @Override
    protected boolean processFlag(String flag) {
        String xRef = "xref"; // NOI18N
        if (flag.startsWith(xRef)) {
            String[] split = flag.split("#"); // NOI18N
            boolean error = false;
            if (split.length == 2) {
                declarationName = split[1];
                error = (declarationName == null) || (declarationName.length() == 0);
            } else if (split.length == 4) {
                refFile = split[1];
                try {
                    line = Integer.parseInt(split[2]);
                    column = Integer.parseInt(split[3]);
                } catch (NumberFormatException ex) {
                    DiagnosticExceptoins.register(ex);
                    line = 0;
                    column = 0;
                }
                error = (refFile == null) || (refFile.length() == 0) || line <= 0 || column <= 0;
            }
            if (error) {
                declarationName = "";
                refFile = "";
                System.err.println("unexpected parameter " + flag);
                System.err.println("should be --xref#file_path#1_based_line#1_based_column or --xref#name");
            }
            return true;
        }
        return false;
    }
    private static final int FACTOR = 1;

    public static void traceProjectRefsStatistics(CsmProject csmPrj, final Map<CharSequence, Long> times, final StatisticsParameters params, final PrintWriter printOut, final OutputWriter printErr, final CsmProgressListener callback, final AtomicBoolean canceled) {
        final XRefResultSet<XRefEntry> bag = new XRefResultSet<>();
        final boolean collect = times.isEmpty();
        List<CsmFile> allFiles = new ArrayList<>();
        int i = 0;
        for (CsmFile file : csmPrj.getAllFiles()) {
            i++;
            if (FACTOR > 1) {
                if (i % FACTOR != 0) {
                    continue;
                }
            }
            allFiles.add(file);
        }
        final AtomicLong time = new AtomicLong();
        Collections.sort(allFiles, new Comparator<CsmFile>(){
            @Override
            public int compare(CsmFile o1, CsmFile o2) {
                int res = 0;
                if ((o1 instanceof FileImpl) && (o2 instanceof FileImpl)) {
                    FileImpl f1 = (FileImpl) o1;
                    FileImpl f2 = (FileImpl) o2;
                    res = (int) (f2.getLastParseTime() - f1.getLastParseTime());
                }
                if (res == 0) {
                    return o1.getAbsolutePath().toString().compareTo(o2.getAbsolutePath().toString());
                }
                return res;
            }
        });
        if (callback != null) {
            callback.projectFilesCounted(csmPrj, allFiles.size());
        }
        RequestProcessor rp = new RequestProcessor("TraceXRef", params.numThreads); // NOI18N
        final CountDownLatch waitFinished = new CountDownLatch(allFiles.size());
        for (final CsmFile file : allFiles) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (canceled.get()) {
                            return;
                        }
                        if (callback != null) {
                            callback.fileParsingStarted(file);
                        }
                        String oldName = Thread.currentThread().getName();
                        try {
                            CharSequence absolutePath = file.getAbsolutePath();
                            if (!collect && !times.containsKey(absolutePath)) {
                                return;
                            }
                            Thread.currentThread().setName("Testing xRef " + absolutePath); //NOI18N
                            long aTime = analyzeFile(file, params, bag, printOut, printErr, canceled);
                            time.getAndAdd(aTime);
                            if (collect && (aTime > params.timeThreshold)) {
                                times.put(absolutePath, aTime);
                            }
                        } finally {
                            Thread.currentThread().setName(oldName);
                        }
                    } finally {
                        waitFinished.countDown();
                    }
                }
            };
            rp.post(task);
        }
        try {
            waitFinished.await();
            bag.setTime(time.get()/params.numThreads);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (callback != null) {
            callback.projectParsingFinished(csmPrj);
        }
        traceStatistics(bag, params, printOut, printErr);
    }

    public static void traceRefs(Collection<CsmReference> out, CsmObject target, PrintStream streamOut) {
        assert target != null;
        CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(target, true);
        CsmObject decl = decDef[0];
        CsmObject def = decDef[1];
        assert decl != null;
        traceRefs(out, decl, def, streamOut);
    }

    public static void traceRefs(Collection<CsmReference> out, CsmObject targetDecl, CsmObject targetDef, PrintStream streamOut) {
        if (out.isEmpty()) {
            streamOut.println("REFERENCES ARE NOT FOUND"); // NOI18N
        } else {
            streamOut.println("REFERENCES ARE:"); // NOI18N
            out = sortRefs(out);
            for (CsmReference ref : out) {
                streamOut.println(toString(ref, targetDecl, targetDef));
            }
        }
    }

    public static String toString(CsmReference ref, CsmObject targetDecl, CsmObject targetDef) {
        String out = CsmTracer.getOffsetString(ref, true);
        String postfix = "";
        if (CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DECLARATION))) {
            postfix = " (DECLARATION)"; // NOI18N
        } else if (CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DEFINITION))) {
            postfix = " (DEFINITION)"; // NOI18N
        } else if (CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.UNKNOWN))) {
            System.err.println("unknown reference kind " + ref);
        }
        return out + postfix;
    }

    public static Collection<CsmReference> sortRefs(Collection<CsmReference> refs) {
        List<CsmReference> out = new ArrayList<>(refs);
        Collections.sort(out, FILE_NAME_START_OFFSET_COMPARATOR);
        return out;
    }
    public static final Comparator<CsmOffsetable> FILE_NAME_START_OFFSET_COMPARATOR = new Comparator<CsmOffsetable>() {

        @Override
        public int compare(CsmOffsetable i1, CsmOffsetable i2) {
            if (i1 == i2) {
                return 0;
            }
            CharSequence path1 = i1.getContainingFile().getAbsolutePath();
            CharSequence path2 = i2.getContainingFile().getAbsolutePath();
            int res = CharSequences.comparator().compare(path1, path2);
            if (res == 0) {
                int ofs1 = i1.getStartOffset();
                int ofs2 = i2.getStartOffset();
                res = ofs1 - ofs2;
            }
            return res;
        }
    };

    private static long analyzeFile(final CsmFile file, final StatisticsParameters params,
            final XRefResultSet<XRefEntry> bag, final PrintWriter out, final OutputWriter printErr,
            final AtomicBoolean canceled) {
        long time = System.nanoTime();
        if (params.analyzeSmartAlgorith) {
            // for smart algorithm visit functions
            visitDeclarations(file.getDeclarations(), params, bag, out, printErr, canceled);
        } else if (params.reportIndex) {
            // otherwise visit active code in whole file
            CsmFileReferences.getDefault().accept(file, null, new LWReportIndexVisitor(bag, printErr, canceled, params.reportIndex), params.interestedReferences);
        } else {
            // otherwise visit active code in whole file
            CsmFileReferences.getDefault().accept(file, null, new LWCheckReferenceVisitor(bag, printErr, canceled, params.reportUnresolved), params.interestedReferences);
        }
        time = System.nanoTime() - time;
        // get line num
        CharSequence text = file.getText();
        int lineCount = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCount++;
            }
        }
        bag.incrementLineCounter(lineCount);
        if (params.printFileStatistic) {
            out.println(file.getAbsolutePath() + " has " + lineCount + " lines; took " + time/1000/1000 + "ms"); // NOI18N
        }
        return time;
    }

    private static void visitDeclarations(Collection<? extends CsmOffsetableDeclaration> decls, StatisticsParameters params, XRefResultSet<XRefEntry> bag,
            PrintWriter printOut, OutputWriter printErr, AtomicBoolean canceled) {
        for (CsmOffsetableDeclaration decl : decls) {
            if (CsmKindUtilities.isFunctionDefinition(decl)) {
                handleFunctionDefinition((CsmFunctionDefinition) decl, params, bag, printOut, printErr);
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                visitDeclarations(((CsmNamespaceDefinition) decl).getDeclarations(), params, bag, printOut, printErr, canceled);
            } else if (CsmKindUtilities.isClass(decl)) {
                visitDeclarations(((CsmClass) decl).getMembers(), params, bag, printOut, printErr, canceled);
            }
            if (canceled.get()) {
                break;
            }
        }
    }

    private static final class LWCheckReferenceVisitor implements CsmFileReferences.Visitor {

        private final XRefResultSet<XRefEntry> bag;
        private final OutputWriter printErr;
        private final AtomicBoolean canceled;
        private final boolean reportUnresolved;

        public LWCheckReferenceVisitor(XRefResultSet<XRefEntry> bag, OutputWriter printErr, AtomicBoolean canceled, boolean reportUnresolved) {
            this.bag = bag;
            this.printErr = printErr;
            this.canceled = canceled;
            this.reportUnresolved = reportUnresolved;
        }

        @Override
        public void visit(CsmReferenceContext context) {
            CsmReference ref = context.getReference();
            if (canceled.get()) {
                return;
            }
            XRefResultSet.ContextEntry entry = createLightWeightEntry(context, printErr, reportUnresolved);
            if (!reportUnresolved) {
                // if perf test => count all for statistics
                bag.incrementScopeCounter(XRefResultSet.ContextScope.CHECK_POINT);
            }
            if (reportUnresolved || entry != XRefResultSet.ContextEntry.RESOLVED) {
                bag.addEntry(XRefResultSet.ContextScope.UNRESOLVED, entry);
                // in perf test no need to spend extra memory
                if (reportUnresolved) {
                    if (entry == XRefResultSet.ContextEntry.UNRESOLVED || entry == XRefResultSet.ContextEntry.UNRESOLVED_MACRO_BASED || entry == XRefResultSet.ContextEntry.UNRESOLVED_BUILTIN_BASED) {
                        CharSequence text = ref.getText();
                        UnresolvedEntry unres = (UnresolvedEntry)bag.getUnresolvedEntry(text);
                        if (unres == null) {
                            unres = new UnresolvedEntry(text, new RefLink(ref));
                            unres = (UnresolvedEntry)bag.addUnresolvedEntry(text, unres);
                        }
                        unres.increment();
                    }
                }
            }
        }

        @Override
        public boolean cancelled() {
            return canceled.get();
        }
    }

    private static final class LWReportIndexVisitor implements CsmFileReferences.Visitor {

        private final XRefResultSet<XRefEntry> bag;
        private final OutputWriter printErr;
        private final AtomicBoolean canceled;

        public LWReportIndexVisitor(XRefResultSet<XRefEntry> bag, OutputWriter printErr, AtomicBoolean canceled, boolean reportUnresolved) {
            this.bag = bag;
            this.printErr = printErr;
            this.canceled = canceled;
        }

        @Override
        public void visit(CsmReferenceContext context) {
            CsmReference ref = context.getReference();
            if (canceled.get()) {
                return;
            }
            CsmReference refFromStorage = CsmReferenceStorage.getDefault().get(ref);
            boolean fromStorage = refFromStorage != null && refFromStorage.getReferencedObject() != null;
            CsmObject target = ref.getReferencedObject();
            if (target == null) {
                // skip all unresolved
                if (fromStorage) {
                    try {
                        printErr.println("INDEXED UNRESOLVED" + ":" + ref, new RefLink(ref), true); // NOI18N
                    } catch (IOException ioe) {
                        // skip it
                    }
                }
                return;
            }
            if (CsmKindUtilities.isParameter(target)) {
                // skip parameters
                return;
            }
            if (UIDProviderIml.isSelfUID(UIDs.get(target))) {
                // skip all locals
                return;
            }
            String skind;
            XRefResultSet.ContextEntry entry = XRefResultSet.ContextEntry.RESOLVED;
            if (CsmKindUtilities.isParameter(target)) {
                skind = "PARAMETER"; //NOI18N
            } else if(CsmKindUtilities.isDeclaration(target)) {
                skind = ((CsmDeclaration)target).getKind().toString();
            } else if(CsmKindUtilities.isNamespace(target)) {
                skind = "NAMESPACE"; //NOI18N
            } else {
                skind = "UNKNOWN"; //NOI18N
            }
            if(!fromStorage) {
                try {
                    printErr.println(skind + ":" + ref, new RefLink(ref), false); // NOI18N
                } catch (IOException ioe) {
                    // skip it
                }
            }
            if (entry != null) {
                RefLink refLink = new RefLink(ref);
                CharSequence text = ref.getText();
                IndexedEntry indexed = (IndexedEntry)bag.getIndexedEntry(refLink);
                if (indexed == null) {
                    indexed = new IndexedEntry(text, refLink, skind);
                    indexed = (IndexedEntry)bag.addIndexedEntry(refLink, indexed);
                }
                indexed.increment(fromStorage);
            }
        }

        @Override
        public boolean cancelled() {
            return canceled.get();
        }
    }

    private static void handleFunctionDefinition(final CsmFunctionDefinition fun, final StatisticsParameters params, final XRefResultSet<XRefEntry> bag,
            final PrintWriter printOut, final OutputWriter printErr) {
        final CsmScope scope = fun.getBody();
        if (scope != null) {
            final XRefResultSet.ContextScope funScope = classifyFunctionScope(fun, printOut);
            final ObjectContext<CsmFunctionDefinition> funContext = createContextObject(fun, printOut);
            final Set<CsmObject> objectsUsedInScope = new HashSet<>();
            bag.incrementScopeCounter(funScope);
            CsmFileReferences.getDefault().accept(
                    scope, null,
                    new CsmFileReferences.Visitor() {

                @Override
                        public void visit(CsmReferenceContext context) {
                            CsmReference ref = context.getReference();
                            XRefResultSet.ContextEntry entry = createEntry(objectsUsedInScope, params, ref, funContext, printOut, printErr);
                            if (entry != null) {
                                bag.addEntry(funScope, entry);
                                if (entry == XRefResultSet.ContextEntry.UNRESOLVED) {
                                    CharSequence text = ref.getText();
                                    UnresolvedEntry unres = (UnresolvedEntry)bag.getUnresolvedEntry(text);
                                    if (unres == null) {
                                        RefLink refLink;
                                        if (params.reportUnresolved) {
                                            refLink = new RefLink(ref);
                                        } else {
                                            refLink = null;
                                        }
                                        unres = new UnresolvedEntry(text, refLink);
                                        bag.addUnresolvedEntry(text, unres);
                                    }
                                    unres.increment();
                                }
                            }
                        }

                        @Override
                        public boolean cancelled() {
                            return false;
                        }
                    },
                    params.interestedReferences);
        } else {
            printOut.println("function definition without body " + fun); // NOI18N
        }
    }

    private static XRefResultSet.ContextEntry createLightWeightEntry(CsmReferenceContext context, OutputWriter printErr, boolean reportUnresolved) {
        XRefResultSet.ContextEntry entry;
        CsmReference ref = context.getReference();
        CsmObject target = ref.getReferencedObject();
        if (target == null) {
            String kind = "UNRESOLVED"; //NOI18N
            entry = XRefResultSet.ContextEntry.UNRESOLVED;
            boolean important = true;
            if (CsmFileReferences.isAfterUnresolved(context)) {
                entry = XRefResultSet.ContextEntry.UNRESOLVED_AFTER_UNRESOLVED;
                kind = "UNRESOLVED_AFTER_UNRESOLVED"; //NOI18N
                important = false;
            } else if (CsmFileReferences.isTemplateBased(context)) {
                entry = XRefResultSet.ContextEntry.UNRESOLVED_TEMPLATE_BASED;
                kind = "UNRESOLVED_TEMPLATE_BASED"; //NOI18N
                important = false;
            } else if (CsmFileReferences.isMacroBased(context)) {
                entry = XRefResultSet.ContextEntry.UNRESOLVED_MACRO_BASED;
                kind = "UNRESOLVED_MACRO_BASED"; //NOI18N
            } else if (CsmFileReferences.isBuiltInBased(ref)) {
                entry = XRefResultSet.ContextEntry.UNRESOLVED_BUILTIN_BASED;
                kind = "UNRESOLVED_BUILTIN_BASED"; //NOI18N
            }
            if (reportUnresolved) {
                try {
                    printErr.println(kind + ":" + ref, new RefLink(ref), important); // NOI18N
                } catch (IOException ioe) {
                    // skip it
                }
            }
        } else {
            entry = XRefResultSet.ContextEntry.RESOLVED;
        }
        return entry;
    }

    private static XRefResultSet.ContextEntry createEntry(Set<CsmObject> objectsUsedInScope, StatisticsParameters params, CsmReference ref, ObjectContext<CsmFunctionDefinition> fun,
            PrintWriter printOut, OutputWriter printErr) {
        XRefResultSet.ContextEntry entry;
        CsmObject target = ref.getReferencedObject();
        if (target == null) {
            entry = XRefResultSet.ContextEntry.UNRESOLVED;
            try {
                printErr.println("UNRESOLVED:" + ref, new RefLink(ref), true); // NOI18N
            } catch (IOException ioe) {
                // skip it
            }
        } else {
            if (CsmReferenceResolver.getDefault().isKindOf(ref, params.interestedReferences)) {
                XRefResultSet.DeclarationKind declaration = classifyDeclaration(target, printOut);
                XRefResultSet.DeclarationScope declarationScope = classifyDeclarationScopeForFunction(declaration, target, fun, printOut);
                XRefResultSet.IncludeLevel declarationIncludeLevel = classifyIncludeLevel(target, fun.objFile, printOut);
                XRefResultSet.UsageStatistics usageStat = XRefResultSet.UsageStatistics.FIRST_USAGE;
                if (objectsUsedInScope.contains(target)) {
                    usageStat = XRefResultSet.UsageStatistics.NEXT_USAGE;
                } else {
                    objectsUsedInScope.add(target);
                }
                entry = new XRefResultSet.ContextEntry(declaration, declarationScope, declarationIncludeLevel, usageStat);
            } else {
                entry = null;
            }
        }
        return entry;
    }

    private static XRefResultSet.ContextScope classifyFunctionScope(CsmFunction fun, PrintWriter printOut) {
        assert fun != null;
        XRefResultSet.ContextScope out = XRefResultSet.ContextScope.UNRESOLVED;
        CsmScope outScope = fun.getScope();
        if (outScope == null) {
            printOut.println("ERROR: no scope for function " + fun); // NOI18N
            return out;
        }
        if (CsmKindUtilities.isConstructor(fun)) {
            out = CsmBaseUtilities.isInlineFunction(fun) ? XRefResultSet.ContextScope.INLINED_CONSTRUCTOR : XRefResultSet.ContextScope.CONSTRUCTOR;
        } else if (CsmKindUtilities.isMethod(fun)) {
            out = CsmBaseUtilities.isInlineFunction(fun) ? XRefResultSet.ContextScope.INLINED_METHOD : XRefResultSet.ContextScope.METHOD;
        } else {
            if (CsmKindUtilities.isFile(outScope)) {
                out = XRefResultSet.ContextScope.FILE_LOCAL_FUNCTION;
            } else {
                CsmNamespace ns = CsmBaseUtilities.getFunctionNamespace(fun);
                if (ns != null) {
                    out = ns.isGlobal() ? XRefResultSet.ContextScope.GLOBAL_FUNCTION : XRefResultSet.ContextScope.NAMESPACE_FUNCTION;
                }
            }
        }
        if (out == XRefResultSet.ContextScope.UNRESOLVED) {
            printOut.println("ERROR: non classified function " + fun); // NOI18N
        }
        return out;
    }

    private static XRefResultSet.DeclarationKind classifyDeclaration(CsmObject obj, PrintWriter printOut) {
        XRefResultSet.DeclarationKind out = XRefResultSet.DeclarationKind.UNRESOLVED;
        if (CsmKindUtilities.isClassifier(obj)) {
            out = XRefResultSet.DeclarationKind.CLASSIFIER;
        } else if (CsmKindUtilities.isEnumerator(obj)) {
            out = XRefResultSet.DeclarationKind.ENUMERATOR;
        } else if (CsmKindUtilities.isParamVariable(obj)) {
            out = XRefResultSet.DeclarationKind.PARAMETER;
        } else if (CsmKindUtilities.isVariable(obj)) {
            out = XRefResultSet.DeclarationKind.VARIABLE;
        } else if (CsmKindUtilities.isFunction(obj)) {
            out = XRefResultSet.DeclarationKind.FUNCTION;
        } else if (CsmKindUtilities.isNamespace(obj)) {
            out = XRefResultSet.DeclarationKind.NAMESPACE;
        } else if (CsmKindUtilities.isMacro(obj)) {
            out = XRefResultSet.DeclarationKind.MACRO;
        } else if (CsmKindUtilities.isClassForwardDeclaration(obj)) {
            out = XRefResultSet.DeclarationKind.CLASS_FORWARD;
        } else if (obj != null) {
            printOut.println("ERROR: non classified declaration " + obj); // NOI18N
        }
        return out;
    }

    private static XRefResultSet.IncludeLevel classifyIncludeLevel(CsmObject obj, CsmFile file, PrintWriter printOut) {
        XRefResultSet.IncludeLevel out = XRefResultSet.IncludeLevel.UNRESOLVED;
        CsmInclude incl = null;
        CsmProject objPrj = null;
        if (CsmKindUtilities.isOffsetable(obj)) {
            CsmFile objFile = ((CsmOffsetable) obj).getContainingFile();
            if (file.equals(objFile)) {
                out = XRefResultSet.IncludeLevel.THIS_FILE;
            } else {
                objPrj = objFile.getProject();
                incl = findFirstLevelInclude(file, objFile);
            }
        } else if (CsmKindUtilities.isNamespace(obj)) {
            CsmNamespace ns = (CsmNamespace) obj;
            objPrj = ns.getProject();
            // check all namespace definitions
            for (CsmNamespaceDefinition nsDef : ns.getDefinitions()) {
                CsmFile defFile = nsDef.getContainingFile();
                if (file.equals(defFile)) {
                    out = XRefResultSet.IncludeLevel.THIS_FILE;
                    break;
                }
            }
            if (out != XRefResultSet.IncludeLevel.THIS_FILE) {
                for (CsmNamespaceDefinition nsDef : ns.getDefinitions()) {
                    CsmFile defFile = nsDef.getContainingFile();
                    CsmInclude curIncl = findFirstLevelInclude(file, defFile);
                    if (curIncl != null) {
                        incl = curIncl;
                        break;
                    }
                }
            }
        } else {
            printOut.println("ERROR: non classified declaration " + obj); // NOI18N
        }
        if (out != XRefResultSet.IncludeLevel.THIS_FILE) {
            if (incl != null) {
                out = incl.isSystem() ? XRefResultSet.IncludeLevel.LIBRARY_DIRECT : XRefResultSet.IncludeLevel.PROJECT_DIRECT;
            } else {
                out = file.getProject().equals(objPrj) ? XRefResultSet.IncludeLevel.PROJECT_DEEP : XRefResultSet.IncludeLevel.LIBRARY_DEEP;
            }
        }
        return out;
    }

    private static XRefResultSet.DeclarationScope classifyDeclarationScopeForFunction(XRefResultSet.DeclarationKind kind, CsmObject obj,
            ObjectContext<CsmFunctionDefinition> csmFunction, PrintWriter printOut) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        ObjectContext<CsmObject> objContext = createContextObject(obj, printOut);
        switch (kind) {
            case NAMESPACE: {
                out = checkNamespaceContainers(objContext, csmFunction);
                break;
            }
            case CLASSIFIER: {
                if (objContext.objClass != null) {
                    out = checkClassContainers(objContext, csmFunction);
                } else if (objContext.objNs != null) {
                    out = checkNamespaceContainers(objContext, csmFunction);
                } else if (CsmKindUtilities.isFunction(objContext.objScope) &&
                        csmFunction.csmObject.equals(objContext.objScope)) {
                    // function local classifier
                    out = XRefResultSet.DeclarationScope.FUNCTION_THIS;
                } else if (printOut != null) {
                    printOut.println("unknown classifier " + objContext.csmObject + " in context of " + csmFunction.csmObject); // NOI18N
                }
                break;
            }
            case FUNCTION: {
                out = checkFileClassNamespaceContainers(objContext, csmFunction, printOut);
                break;
            }
            case MACRO: {
                out = checkFileContainer(objContext, csmFunction);
                break;
            }
            case PARAMETER: {
                out = XRefResultSet.DeclarationScope.FUNCTION_THIS;
                break;
            }
            case ENUMERATOR:
            case VARIABLE: {
                int stOffset = ((CsmOffsetable) obj).getStartOffset();
                if (csmFunction.csmObject.getStartOffset() < stOffset &&
                        stOffset < csmFunction.csmObject.getEndOffset()) {
                    out = XRefResultSet.DeclarationScope.FUNCTION_THIS;
                } else {
                    out = checkFileClassNamespaceContainers(objContext, csmFunction, printOut);
                }
                break;
            }
            case UNRESOLVED:
                break;
            case CLASS_FORWARD:
            default:
                printOut.println("unhandled kind " + kind + " for object " + objContext.csmObject); // NOI18N
        }
        return out;
    }

    private static XRefResultSet.DeclarationScope checkFileContainer(
            ObjectContext<CsmObject> objContext,
            ObjectContext<CsmFunctionDefinition> csmFunction) {
        XRefResultSet.DeclarationScope out;
        if (csmFunction.objFile.equals(objContext.objFile)) {
            out = XRefResultSet.DeclarationScope.FILE_THIS;
        } else if (csmFunction.objPrj.equals(objContext.objPrj)) {
            out = XRefResultSet.DeclarationScope.PROJECT_FILE;
        } else {
            out = XRefResultSet.DeclarationScope.LIBRARY_FILE;
        }
        return out;
    }

    private static XRefResultSet.DeclarationScope checkNamespaceContainers(
            ObjectContext<CsmObject> objContext,
            ObjectContext<CsmFunctionDefinition> csmFunction) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        if (objContext.objNs != null) {
            boolean isNested = false;
            if (!objContext.objNs.isGlobal() && (csmFunction.objNs != null) &&
                    !csmFunction.objNs.isGlobal()) {
                CsmNamespace ns = csmFunction.objNs;
                if (ns.equals(objContext.objNs)) {
                    out = XRefResultSet.DeclarationScope.NAMESPACE_THIS;
                    isNested = true;
                } else {
                    while (ns != null && !ns.isGlobal()) {
                        if (ns.equals(objContext.objNs)) {
                            out = XRefResultSet.DeclarationScope.NAMESPACE_PARENT;
                            isNested = true;
                            break;
                        }
                        ns = ns.getParent();
                    }
                }
            }
            if (!isNested) {
                if (objContext.objNs.isGlobal()) {
                    out = csmFunction.objPrj.equals(objContext.objPrj) ? XRefResultSet.DeclarationScope.PROJECT_GLOBAL : XRefResultSet.DeclarationScope.LIBRARY_GLOBAL;
                } else {
                    out = csmFunction.objPrj.equals(objContext.objPrj) ? XRefResultSet.DeclarationScope.PROJECT_NAMESPACE : XRefResultSet.DeclarationScope.LIBRARY_NAMESPACE;
                }
            }
        }
        return out;
    }

    private static XRefResultSet.DeclarationScope checkClassContainers(
            ObjectContext<CsmObject> objContext,
            ObjectContext<CsmFunctionDefinition> csmFunction) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        if (objContext.objClass != null) {
            boolean isInherited = false;
            if (csmFunction.objClass != null) {
                // check inheritance
                if (csmFunction.objClass.equals(objContext.objClass)) {
                    out = XRefResultSet.DeclarationScope.CLASSIFIER_THIS;
                    isInherited = true;
                } else if (CsmInheritanceUtilities.isAssignableFrom(objContext.objClass, csmFunction.objClass)) {
                    out = XRefResultSet.DeclarationScope.CLASSIFIER_PARENT;
                    isInherited = true;
                }
            }
            if (!isInherited) {
                if (csmFunction.objPrj.equals(objContext.objPrj)) {
                    out = XRefResultSet.DeclarationScope.PROJECT_CLASSIFIER;
                } else {
                    out = XRefResultSet.DeclarationScope.LIBRARY_CLASSIFIER;
                }
            }
        }
        return out;
    }

    private static XRefResultSet.DeclarationScope checkFileClassNamespaceContainers(
            ObjectContext<CsmObject> objContext,
            ObjectContext<CsmFunctionDefinition> csmFunction,
            PrintWriter printOut) {
        XRefResultSet.DeclarationScope out = XRefResultSet.DeclarationScope.UNRESOLVED;
        if (CsmKindUtilities.isFile(objContext.objScope)) {
            out = checkFileContainer(objContext, csmFunction);
        } else if (objContext.objClass != null) {
            out = checkClassContainers(objContext, csmFunction);
        } else if (objContext.objNs != null) {
            out = checkNamespaceContainers(objContext, csmFunction);
        } else if (printOut != null) {
            printOut.println("unknown scope of " + objContext.csmObject + " in context of " + csmFunction.csmObject); // NOI18N
        }
        return out;
    }

    private static CsmInclude findFirstLevelInclude(CsmFile startFile, CsmFile searchFile) {
        assert startFile != null : "start file must be not null";
        assert searchFile != null : "search file must be not null";
        for (CsmInclude incl : startFile.getIncludes()) {
            CsmFile included = incl.getIncludeFile();
            if (searchFile.equals(included)) {
                return incl;
            } else if (included != null && included.getDeclarations().isEmpty()) {
                // this is a fake include only file
                return findFirstLevelInclude(included, searchFile);
            }
        }
        return null;
    }

    private static void traceStatistics(XRefResultSet<XRefEntry> bag, StatisticsParameters params, PrintWriter printOut, OutputWriter printErr) {
        printOut.println("Number of analyzed contexts " + bag.getNumberOfAllContexts()); // NOI18N
        Collection<XRefResultSet.ContextScope> sortedContextScopes = XRefResultSet.sortedContextScopes(bag, false);
        int numProjectProints = 0;
        int numUnresolvedPoints = 0;
        int numMacroBasedUnresolvedPoints = 0;
        int numBuiltinBasedUnresolvedPoints = 0;
        int numTemplateBasedUnresolvedPoints = 0;
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            numProjectProints += entries.size();
            for (ContextEntry contextEntry : entries) {
                if (contextEntry == ContextEntry.UNRESOLVED) {
                    numUnresolvedPoints++;
                } else if (contextEntry == ContextEntry.UNRESOLVED_MACRO_BASED) {
                    numMacroBasedUnresolvedPoints++;
                } else if (contextEntry == ContextEntry.UNRESOLVED_TEMPLATE_BASED) {
                    numTemplateBasedUnresolvedPoints++;
                } else if (contextEntry == ContextEntry.UNRESOLVED_BUILTIN_BASED) {
                    numBuiltinBasedUnresolvedPoints++;
                }
            }
        }
        if (bag.getNumberOfContexts(XRefResultSet.ContextScope.CHECK_POINT, false) > 0) {
            // in perf run all is counted separately
            numProjectProints = bag.getNumberOfContexts(XRefResultSet.ContextScope.CHECK_POINT, false);
        }
        int allUnresolvedPoints = numUnresolvedPoints + numMacroBasedUnresolvedPoints + numBuiltinBasedUnresolvedPoints;
        double unresolvedRatio = numProjectProints == 0 ? 0 : (100.0 * allUnresolvedPoints) / ((double) numProjectProints);
        double unresolvedMacroBasedRatio = numProjectProints == 0 ? 0 : (100.0 * numMacroBasedUnresolvedPoints) / ((double) numProjectProints);
        double unresolvedBuiltinBasedRatio = numProjectProints == 0 ? 0 : (100.0 * numBuiltinBasedUnresolvedPoints) / ((double) numProjectProints);
        double unresolvedTemplateBasedRatio = numProjectProints == 0 ? 0 : (100.0 * numTemplateBasedUnresolvedPoints) / ((double) numProjectProints);
        String unresolvedStatistics = String.format("Unresolved %d (%.2f%%) where MacroBased %d (%.2f%%) of %d checkpoints [TemplateBased warnings %d (%.2f%%), Builtin %d (%.2f%%)]", // NOI18N
                allUnresolvedPoints, unresolvedRatio, numMacroBasedUnresolvedPoints, unresolvedMacroBasedRatio,
                numProjectProints, numTemplateBasedUnresolvedPoints, unresolvedTemplateBasedRatio, numBuiltinBasedUnresolvedPoints, unresolvedBuiltinBasedRatio); // NOI18N
        printOut.println(unresolvedStatistics);
        String performanceStatistics = String.format("Line count: %d, time %.0f ms, %nspeed %.2f lines/sec, %.2f refs/sec", bag.getLineCount(), bag.getTimeMs(), bag.getLinesPerSec(), (double)numProjectProints / bag.getTimeSec()); // NOI18N
        printOut.println(performanceStatistics);

        if(params.reportIndex) {
            printOut.println("Index stats:"); // NOI18N
            Map<CharSequence, Integer> indexStats = new HashMap<>();
            Map<CharSequence, Integer> allStats = new HashMap<>();
            int totalAll = 0;
            int totalIndex = 0;

            Collection<XRefEntry> entries = bag.getIndexedEntries(new Comparator<XRefEntry>() {
                    @Override
                    public int compare(XRefEntry o1, XRefEntry o2) {
                        if(o1 instanceof IndexedEntry && o2 instanceof IndexedEntry) {
                            return ((IndexedEntry)o2).getNrIndexed() - ((IndexedEntry)o1).getNrIndexed();
                        }
                        else {
                            return 0;
                        }

                    }
                });
            for (XRefEntry entry : entries) {
                if(entry instanceof IndexedEntry) {
                    IndexedEntry indexed = (IndexedEntry) entry;
                    totalAll += indexed.getNrAll();
                    totalIndex += indexed.getNrIndexed();
                    if(indexStats.containsKey(indexed.getKind())) {
                        indexStats.put(indexed.getKind(), indexStats.get(indexed.getKind()) + indexed.getNrIndexed());
                    } else {
                        indexStats.put(indexed.getKind(), indexed.getNrIndexed());
                    }
                    if(allStats.containsKey(indexed.getKind())) {
                        allStats.put(indexed.getKind(), allStats.get(indexed.getKind()) + indexed.getNrAll());
                    } else {
                        allStats.put(indexed.getKind(), indexed.getNrAll());
                    }
                }
            }
            String header = String.format("%20s %10s %10s %5s", "Kind", "Indexed", "Checked", "%%"); // NOI18N
            printOut.println(header); // NOI18N
            for (CharSequence kind : indexStats.keySet()) {
                if(kind != null) {
                    String s2 = String.format("%20s %10d %10d %.2f%%", kind, indexStats.get(kind), allStats.get(kind), (double) indexStats.get(kind)*100/allStats.get(kind)); // NOI18N
                    printOut.println(s2); // NOI18N
                }
            }
            String s = String.format("%20s %10d %10d %.2f%%", "Total", totalIndex, totalAll, (double) totalIndex*100/totalAll); // NOI18N
            printOut.println(s); // NOI18N
        }

        if (!params.reportUnresolved) {
            return;
        }
        if (!params.analyzeSmartAlgorith) {
            // dump unresolved statistics
            if (allUnresolvedPoints > 0) {
                Collection<XRefEntry> entries = bag.getUnresolvedEntries(new Comparator<XRefEntry>() {

                    @Override
                    public int compare(XRefEntry o1, XRefEntry o2) {
                        if(o1 instanceof UnresolvedEntry && o2 instanceof UnresolvedEntry) {
                            return ((UnresolvedEntry)o2).getNrUnnamed() - ((UnresolvedEntry)o1).getNrUnnamed();
                        }
                        else {
                            return 0;
                        }

                    }
                });
                for (XRefEntry entry : entries) {
                    if(entry instanceof UnresolvedEntry) {
                        UnresolvedEntry unresolvedEntry = (UnresolvedEntry) entry;
                        double unresolvedEntryRatio = (100.0 * unresolvedEntry.getNrUnnamed()) / ((double) allUnresolvedPoints);
                        String msg = String.format("%20s\t|%6s\t| %.2f%% ", unresolvedEntry.getName(), unresolvedEntry.getNrUnnamed(), unresolvedEntryRatio); // NOI18N
                        try {
                            printErr.println(msg, unresolvedEntry.getLink(), false);
                        } catch (IOException ex) {
                            // skip exception
                        }
                    }
                }
            }
            return;
        }
        String contextFmt = "%20s\t|%6s\t| %2s |%n"; // NOI18N
        String msg = String.format(contextFmt, "Name", "Num", "%"); // NOI18N
        printOut.println(msg);
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
                if (entries.isEmpty()) {
                    continue;
                }
            }
            msg = String.format(contextFmt, scope, bag.getNumberOfContexts(scope, false), bag.getNumberOfContexts(scope, true));
            printOut.print(msg);
        }
        printOut.println("\nAnalyzed entries per scopes "); // NOI18N
        boolean printTitle = true;
        sortedContextScopes = XRefResultSet.sortedContextScopes(bag, true);
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            traceEntriesStatistics(scope, entries, printTitle, printOut);
            printTitle = false;
        }
        printOut.println("\nNumbers for \"first\" items approach"); // NOI18N
        printTitle = true;
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            traceFirstItemsStatistics(scope, entries, printTitle, printOut);
            printTitle = false;
        }
        printOut.println("\nDetails about file inclusion level"); // NOI18N
        printTitle = true;
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            traceFileBasedEntriesStatistics(scope, entries, printTitle, printOut);
            printTitle = false;
        }
        printOut.println("\nDetails about scope of referenced declarations"); // NOI18N
        printTitle = true;
        for (XRefResultSet.ContextScope scope : sortedContextScopes) {
            Collection<XRefResultSet.ContextEntry> entries = bag.getEntries(scope);
            traceUsedDeclarationScopeEntriesStatistics(scope, entries, printTitle, printOut);
            printTitle = false;
        }
    }

    private static void traceFirstItemsStatistics(XRefResultSet.ContextScope scope,
            Collection<XRefResultSet.ContextEntry> entries,
            boolean printTitle, PrintWriter printOut) {
        String entryFmtFileInfo = "%20s\t|%10s\t|%20s\t|%20s\t|%20s\t|%20s\t|%20s%n"; // NOI18N
        if (printTitle) {
            String title = String.format(entryFmtFileInfo, "scope name", "All", "local+cls+ns", "file+#incl-1", "local+cls+ns+#incl-1", // NOI18N
                    "was usages", "context+used"); // NOI18N
            printOut.print(title);
        }
        if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
            if (entries.isEmpty()) {
                return;
            }
        }

        Set<IncludeLevel> nearestIncludes = EnumSet.of(XRefResultSet.IncludeLevel.THIS_FILE, XRefResultSet.IncludeLevel.PROJECT_DIRECT, XRefResultSet.IncludeLevel.LIBRARY_DIRECT);
        Set<DeclarationScope> nearestScopes = EnumSet.of(
                XRefResultSet.DeclarationScope.FUNCTION_THIS,
                XRefResultSet.DeclarationScope.CLASSIFIER_THIS,
                XRefResultSet.DeclarationScope.CLASSIFIER_PARENT,
                XRefResultSet.DeclarationScope.FILE_THIS,
                XRefResultSet.DeclarationScope.NAMESPACE_THIS,
                XRefResultSet.DeclarationScope.NAMESPACE_PARENT);
        Set<DeclarationScope> nonScopes = EnumSet.noneOf(XRefResultSet.DeclarationScope.class);
        Set<IncludeLevel> nonIncludes = EnumSet.noneOf(XRefResultSet.IncludeLevel.class);
        Set<XRefResultSet.UsageStatistics> nonUsages = EnumSet.noneOf(XRefResultSet.UsageStatistics.class);
        Set<XRefResultSet.UsageStatistics> wasUsages = EnumSet.of(XRefResultSet.UsageStatistics.SECOND_USAGE, XRefResultSet.UsageStatistics.NEXT_USAGE);
        String msg = String.format(entryFmtFileInfo, scope,
                entries.size(),
                getDeclScopeAndIncludeLevelInfo(entries, nearestScopes, nonIncludes, nonUsages),
                getDeclScopeAndIncludeLevelInfo(entries, nonScopes, nearestIncludes, nonUsages),
                getDeclScopeAndIncludeLevelInfo(entries, nearestScopes, nearestIncludes, nonUsages),
                getDeclScopeAndIncludeLevelInfo(entries, nonScopes, nonIncludes, wasUsages),
                getDeclScopeAndIncludeLevelInfo(entries, nearestScopes, nearestIncludes, wasUsages));
        printOut.print(msg);
    }

    private static void traceFileBasedEntriesStatistics(XRefResultSet.ContextScope scope,
            Collection<XRefResultSet.ContextEntry> entries,
            boolean printTitle, PrintWriter printOut) {
        String entryFmtFileInfo = "%20s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s%n"; // NOI18N
        if (printTitle) {
            String title = String.format(entryFmtFileInfo, "scope name", "this file", "direct \"\"", "direct <>", "project", "library", "unresolved", "All"); // NOI18N
            printOut.print(title);
        }
        if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
            if (entries.isEmpty()) {
                return;
            }
        }
        String msg = String.format(entryFmtFileInfo, scope,
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.THIS_FILE),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.PROJECT_DIRECT),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.LIBRARY_DIRECT),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.PROJECT_DEEP),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.LIBRARY_DEEP),
                getIncludeLevelInfo(entries, XRefResultSet.IncludeLevel.UNRESOLVED),
                entries.size());
        printOut.print(msg);
    }

    private static void traceUsedDeclarationScopeEntriesStatistics(XRefResultSet.ContextScope scope,
            Collection<XRefResultSet.ContextEntry> entries,
            boolean printTitle, PrintWriter printOut) {
        String entryDeclScopeInfo = "%20s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s\t|%10s|%n"; // NOI18N
        if (printTitle) {
            String title = String.format(entryDeclScopeInfo,
                    "scope name", // NOI18N
                    //                    "All this", "All parent", "This+Parent", // NOI18N
                    "this fun", // NOI18N
                    "this class", "parent class", "prj class", "lib class", // NOI18N
                    "this ns", "parent ns", "prj ns", "lib ns", // NOI18N
                    "this file", "prj file", "lib file", // NOI18N
                    "project", "library", // NOI18N
                    "unresolved", "All"); // NOI18N
            printOut.print(title);
        }
        if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
            if (entries.isEmpty()) {
                return;
            }
        }
        String msg = String.format(entryDeclScopeInfo, scope,
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.FUNCTION_THIS),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.CLASSIFIER_THIS),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.CLASSIFIER_PARENT),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.PROJECT_CLASSIFIER),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.LIBRARY_CLASSIFIER),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.NAMESPACE_THIS),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.NAMESPACE_PARENT),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.PROJECT_NAMESPACE),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.LIBRARY_NAMESPACE),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.FILE_THIS),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.PROJECT_FILE),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.LIBRARY_FILE),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.PROJECT_GLOBAL),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.LIBRARY_GLOBAL),
                getDeclarationScopeInfo(entries, XRefResultSet.DeclarationScope.UNRESOLVED),
                entries.size());
        printOut.print(msg);
    }

    private static String getDeclScopeAndIncludeLevelInfo(Collection<ContextEntry> entries,
            Set<XRefResultSet.DeclarationScope> declScopes,
            Set<XRefResultSet.IncludeLevel> levels, Set<XRefResultSet.UsageStatistics> usages) {
        int num = 0;

        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (declScopes.contains(contextEntry.declarationScope) ||
                    levels.contains(contextEntry.declarationIncludeLevel) ||
                    usages.contains(contextEntry.usageStatistics)) {
                num++;
            }
        }
        return toRelString(num, entries.size());
    }

    private static String getIncludeLevelInfo(Collection<XRefResultSet.ContextEntry> entries, XRefResultSet.IncludeLevel level) {
        int num = 0;
        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (contextEntry.declarationIncludeLevel == level) {
                num++;
            }
        }
        return toRelString(num, entries.size());
    }

    private static String getDeclarationScopeInfo(Collection<XRefResultSet.ContextEntry> entries, XRefResultSet.DeclarationScope declScope) {
        int num = 0;
        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (contextEntry.declarationScope == declScope) {
                num++;
            }
        }
        return toRelString(num, entries.size());
    }

//    private static String getDeclarationKindInfo(Collection<XRefResultSet.ContextEntry> entries, XRefResultSet.DeclarationKind declKind) {
//        int num = 0;
//        for (XRefResultSet.ContextEntry contextEntry : entries) {
//            if (contextEntry.declaration == declKind) {
//                num++;
//            }
//        }
//        return toRelString(num, entries.size());
//    }
    private static String toRelString(int num, int size) {
        assert (size != 0) || (num == 0);
        int rel = (num == 0) ? 0 : (num * 100) / size;
        return rel + "%(" + num + ")"; // NOI18N
    }

    private static void traceEntriesStatistics(XRefResultSet.ContextScope scope,
            Collection<XRefResultSet.ContextEntry> entries,
            boolean printTitle, PrintWriter printOut) {
        String entryFmt = "%20s\t|%10s\t|%10s\t|%10s|%n"; // NOI18N
        if (printTitle) {
            String title = String.format(entryFmt, "Entries for scope", "Num", "Resolved", "Unresolved"); // NOI18N
            printOut.print(title);
        }
        if (scope == XRefResultSet.ContextScope.UNRESOLVED) {
            if (entries.isEmpty()) {
                return;
            }
        }
        int unresolved = 0;
        for (XRefResultSet.ContextEntry contextEntry : entries) {
            if (contextEntry.declaration == XRefResultSet.DeclarationKind.UNRESOLVED) {
                unresolved++;
            }
        }
        String msg = String.format(entryFmt, scope, entries.size(), (entries.size() - unresolved), unresolved);
        printOut.print(msg);
    }

    private static <T extends CsmObject> ObjectContext<T> createContextObject(T obj, PrintWriter printOut) {
        T csmObject = obj;
        CsmClass objClass = null;
        CsmFile objFile = null;
        CsmProject objPrj = null;
        CsmNamespace objNs = null;
        CsmScope objScope = null;
        // init project and file
        if (CsmKindUtilities.isOffsetable(obj)) {
            objFile = ((CsmOffsetable) obj).getContainingFile();
            assert objFile != null;
            objPrj = objFile.getProject();
        } else if (CsmKindUtilities.isNamespace(obj)) {
            objPrj = ((CsmNamespace) obj).getProject();
        } else {
            printOut.println("not handled object " + obj); // NOI18N
        }
        // init namespace
        objNs = CsmBaseUtilities.getObjectNamespace(obj);
        // init class
        objClass = CsmBaseUtilities.getObjectClass(obj);
        // init scope
        if (CsmKindUtilities.isEnumerator(obj)) {
            objScope = ((CsmEnumerator) obj).getEnumeration().getScope();
        } else if (CsmKindUtilities.isScopeElement(obj)) {
            objScope = ((CsmScopeElement) obj).getScope();
        }
        while (objScope != null) {
            if (CsmKindUtilities.isNamespaceDefinition(objScope) ||
                    CsmKindUtilities.isClass(objScope) ||
                    CsmKindUtilities.isFunction(objScope)) {
                break;
            } else if (CsmKindUtilities.isScopeElement(objScope)) {
                objScope = ((CsmScopeElement) objScope).getScope();
            } else {
                break;
            }
        }
        return new ObjectContext<>(csmObject, objClass, objFile, objPrj, objNs, objScope);
    }

    public static final class StatisticsParameters {

        public final Set<CsmReferenceKind> interestedReferences;
        public final boolean analyzeSmartAlgorith;
        public final boolean reportUnresolved;
        public final boolean reportIndex;
        public final int numThreads;
        public final long timeThreshold;
        public boolean printFileStatistic = true;
        public StatisticsParameters(Set<CsmReferenceKind> kinds, boolean analyzeSmartAlgorith, boolean reportUnresolved, boolean reportIndex, int numThreads, long timeThreshold) {
            this.analyzeSmartAlgorith = analyzeSmartAlgorith;
            this.interestedReferences = kinds;
            this.reportUnresolved = reportUnresolved;
            this.reportIndex = reportIndex;
            this.numThreads = numThreads;
            this.timeThreshold = timeThreshold;
        }

        public void printFileStatistic(boolean printFileStatistic) {
            this.printFileStatistic = printFileStatistic;
        }
    }

    private static final class ObjectContext<T extends CsmObject> {

        private final T csmObject;
        private final CsmClass objClass;
        private final CsmFile objFile;
        private final CsmProject objPrj;
        private final CsmNamespace objNs;
        private final CsmScope objScope;

        public ObjectContext(T csmObject, CsmClass objClass, CsmFile objFile, CsmProject objPrj, CsmNamespace objNs, CsmScope objScope) {
            this.csmObject = csmObject;
            this.objClass = objClass;
            this.objFile = objFile;
            this.objPrj = objPrj;
            this.objNs = objNs;
            this.objScope = objScope;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("Object: ").append(csmObject);//NOI18N
            buf.append("\nFile: ").append(objFile);//NOI18N
            buf.append("\nClass: ").append(objClass);//NOI18N
            buf.append("\nNS: ").append(objNs);//NOI18N
            buf.append("\nProject: ").append(objPrj);//NOI18N
            buf.append("\nScope: ").append(objScope);//NOI18N
            return buf.toString();
        }
    }

    final static class RefLink implements OutputListener {

        private final CsmUID<CsmFile> fileUID;
        private final int offset;

        RefLink(CsmReference ref) {
            this.fileUID = UIDs.get(ref.getContainingFile());
            this.offset = ref.getStartOffset();
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            CsmFile file = fileUID.getObject();
            if (file != null) {
                CsmUtilities.openSource(new Offsetable(file, offset, offset));
            }
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }

        @Override
        public boolean equals(Object o) {
            return fileUID.getObject().equals(fileUID.getObject()) && offset == offset;
        }

        @Override
        public int hashCode() {
            return offset;
        }
    }

    private static interface XRefEntry {
    }

    private final static class UnresolvedEntry implements XRefEntry {

        private final RefLink link;
        private final AtomicInteger nrUnnamed;
        private final CharSequence name;

        public UnresolvedEntry(CharSequence name, RefLink link) {
            this.link = link;
            this.name = name;
            this.nrUnnamed = new AtomicInteger(0);
        }

        public CharSequence getName() {
            return name;
        }

        public int getNrUnnamed() {
            return nrUnnamed.get();
        }

        public RefLink getLink() {
            return link;
        }

        private void increment() {
            nrUnnamed.incrementAndGet();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final UnresolvedEntry other = (UnresolvedEntry) obj;
            return this.name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
    }

    private final static class IndexedEntry implements XRefEntry {

        private final RefLink link;
        private final AtomicInteger nrIndexed;
        private final AtomicInteger nrAll;
        private final CharSequence name;
        private final CharSequence kind;

        public IndexedEntry(CharSequence name, RefLink link, CharSequence kind) {
            this.link = link;
            this.name = name;
            this.nrIndexed = new AtomicInteger(0);
            this.nrAll = new AtomicInteger(0);
            this.kind = kind;
        }

        public CharSequence getName() {
            return name;
        }

        public int getNrIndexed() {
            return nrIndexed.get();
        }

        public int getNrAll() {
            return nrAll.get();
        }

        public RefLink getLink() {
            return link;
        }

        public CharSequence getKind() {
            return kind;
        }

        private void increment(boolean indexed) {
            if(indexed) {
                nrIndexed.incrementAndGet();
            }
            nrAll.incrementAndGet();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IndexedEntry other = (IndexedEntry) obj;
            return this.name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
    }

}
