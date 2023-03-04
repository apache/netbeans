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

package org.netbeans.modules.parsing.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.implspi.EnvironmentFactory;
import org.netbeans.modules.parsing.implspi.SourceControl;
import org.netbeans.modules.parsing.implspi.SourceEnvironment;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 * Temporary helper functions needed by the java.source
 * @author Tomas Zezula
 */
public class Utilities {

    private static final ThreadLocal<Parser.CancelReason> cancelReason = new ThreadLocal<Parser.CancelReason>();
    
    private static final int DEFAULT_MAX_FILE_SIZE = 50*(1<<20);

    public static final int getMaxFileSize() {
        return Integer.getInteger(
            "parse.max.file.size",  //NOI18N
            DEFAULT_MAX_FILE_SIZE);
    }

    private Utilities () {}

    //MasterFS bridge
    public static <T> T runPriorityIO (final Callable<T> r) throws Exception {
        return getEnvFactory().runPriorityIO(r);
    }

    //Helpers for java reformatter, may be removed when new reformat api will be done
    public static void acquireParserLock () {
        TaskProcessor.acquireParserLock();
    }

    public static void releaseParserLock () {
        TaskProcessor.releaseParserLock();
    }

    //Helpers for asserts in java.source    
    public static boolean holdsParserLock () {
        return TaskProcessor.holdsParserLock();
    }

    /**
     * Returns true if given thread is a TaskProcessor dispatch thread.
     * @param Thread thread
     * @return boolean
     */
    public static boolean isTaskProcessorThread () {
        return TaskProcessor.WORKER.isRequestProcessorThread();
    }

    //Helpers for indexing in java.source, will be removed when indexing will be part of parsing api

    public static void scheduleSpecialTask (
            @NonNull final Runnable runnable,
            @NonNull final Lookup context,
            final int priority) {
        TaskProcessor.scheduleSpecialTask(runnable, context, priority);
    }

    //Helpers to bridge java.source factories into parsing.api
    public static void revalidate (@NonNull final Source source) {
        final SourceControl ctl = SourceAccessor.getINSTANCE().getEnvControl(source);
        ctl.sourceChanged(false);
        ctl.revalidate(SourceEnvironment.getReparseDelay(false));
    }
    
    public static void revalidate (@NonNull final FileObject fo) {
        final Source source = SourceAccessor.getINSTANCE().get(fo);
        if (source != null) {
            revalidate(source);
        }
    }

    public static void invalidate (@NonNull final FileObject fo) {
        final Source source = SourceAccessor.getINSTANCE().get(fo);
        if (source != null) {
            final SourceAccessor sa = SourceAccessor.getINSTANCE();
            sa.setFlags(source, EnumSet.of(SourceFlags.INVALID));
        }
    }

    public static void addParserResultTask (final ParserResultTask<?> task, final Source source) {
        Parameters.notNull ("task", task);
        Parameters.notNull ("source", source);
        final SourceCache cache = SourceAccessor.getINSTANCE ().getCache (source);
        TaskProcessor.addPhaseCompletionTasks (
            Collections.<Pair<SchedulerTask,Class<? extends Scheduler>>>singleton(Pair.<SchedulerTask,Class<? extends Scheduler>>of(task,null)),
            cache,
            true);
    }
    
    public static void removeParserResultTask (final ParserResultTask<?> task, final Source source) {
        Parameters.notNull ("task", task);
        Parameters.notNull ("source", source);
        TaskProcessor.removePhaseCompletionTasks(Collections.singleton(task), source);
    }
    
    public static void rescheduleTask (final ParserResultTask<?> task, final Source source) {
        Parameters.notNull ("task", task);
        Parameters.notNull ("source", source);
        TaskProcessor.rescheduleTasks (Collections.<SchedulerTask>singleton (task), source, null);
    }
    

    //Internal API among TaskProcessor and RepositoryUpdater
    //If SchedulerTask will need the information about cancel
    //add the CancelReason parameter into cancel like it's in Parser
    public static Parser.CancelReason getTaskCancelReason() {
        return cancelReason.get();
    }

    static void setTaskCancelReason(final @NullAllowed Parser.CancelReason reason) {
        if (reason == null) {
            cancelReason.remove();
        } else {
            cancelReason.set(reason);
        }
    }

    public static FileObject getFileObject(Document doc) {
        return EditorDocumentUtils.getFileObject(doc);
    }

    /**
     * Finds the nearest caller outside the parsing API.
     * Some additional classes can be also excluded.
     * 
     * Note: this method is copied from Indexing API (parsing.impl.indexing.IndexingUtils). Perhaps
     * a common implementation should be created
     * 
     * @param elements
     * @param classesToFilterOut
     * @return 
     */
    public static StackTraceElement findCaller(StackTraceElement[] elements, Object... classesToFilterOut) {
        loop: for (StackTraceElement e : elements) {
            if (e.getClassName().equals(Utilities.class.getName()) || e.getClassName().startsWith("java.lang.")) { //NOI18N
                continue;
            }

            if (classesToFilterOut != null && classesToFilterOut.length > 0) {
                for(Object c : classesToFilterOut) {
                    if (c instanceof Class && e.getClassName().startsWith(((Class) c).getName())) {
                        continue loop;
                    } else if (c instanceof String && e.getClassName().startsWith((String) c)) {
                        continue loop;
                    }
                }
            } else {
                if (e.getClassName().startsWith("org.netbeans.modules.parsing.")) { //NOI18N
                    continue;
                }
            }

            return e;
        }
        return null;
    }

    private static volatile EnvironmentFactory   envFactory;
    
    public static synchronized EnvironmentFactory getEnvFactory() {
        EnvironmentFactory f = envFactory;
        if (f == null) {
            f = envFactory = Lookup.getDefault().lookup(EnvironmentFactory.class);
            if (f == null) {
                throw new UnsupportedOperationException("EnvironmentFactory missing");
            }
        }
        return f;
    }
    
    public static SourceEnvironment createEnvironment(Source src, SourceControl ctrl) {
        return getEnvFactory().createEnvironment(src, ctrl);
    }

    public static Class<? extends Scheduler> findDefaultScheduler(String type) {
        EnvironmentFactory f = getEnvFactory();
        return f.findStandardScheduler(type);
    }
    
    public static final class NopScheduler extends Scheduler {
        @Override
        protected SchedulerEvent createSchedulerEvent(SourceModificationEvent event) {
            return null;
        }
    }
}
