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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 */
/*package-local*/ class FileStateCache {
    private static final boolean TRACE = false;
    private static final boolean cacheStates = TraceFlags.CACHE_FILE_STATE;
    private static final int CACHE_SIZE = 10;
    private static int stateCacheAttempt = 0;
    private static int stateCacheSuccessAttempt = 0;
    private final Map<PreprocHandler.StateKey, Value> stateCache = new LinkedHashMap<>();
    private final ReadWriteLock stateCacheLock = new ReentrantReadWriteLock();
    private final FileImpl file;


    /*package-local*/ FileStateCache(FileImpl file){
        this.file = file;
    }
    void cacheVisitedState(PreprocHandler.State inputState, PreprocHandler outputHandler, FilePreprocessorConditionState pcState) {
        if (cacheStates && inputState.isCompileContext()) {
            stateCacheLock.writeLock().lock();
            try {
                if ((stateCache.isEmpty() || APTHandlersSupport.getIncludeStackDepth(inputState) == 1) && isCacheableState(inputState)) {
                    if (stateCache.size() == CACHE_SIZE) {
                        int min = Integer.MAX_VALUE;
                        PreprocHandler.StateKey key = null;
                        for (Map.Entry<PreprocHandler.StateKey, Value> entry : stateCache.entrySet()){
                            if (entry.getValue().value.get() == null) {
                                key = entry.getKey();
                                break;
                            }
                            if (entry.getValue().count < min){
                                key = entry.getKey();
                                min = entry.getValue().count;
                            }
                        }
                        stateCache.remove(key);
                    }
                    stateCache.put(createKey(inputState), new Value(new PreprocessorStatePair(outputHandler.getState(), pcState)));
                }
            } finally {
                stateCacheLock.writeLock().unlock();
            }
        }
    }

    /*package-local*/ PreprocessorStatePair getCachedVisitedState(PreprocHandler.State inputState) {
        PreprocessorStatePair res = null;
        if (cacheStates && inputState.isCompileContext()) {
            if (TRACE) {stateCacheAttempt++;}
            int count = 0;
            stateCacheLock.readLock().lock();
            PreprocHandler.StateKey key = null;
            try {
                if (isCacheableState(inputState)) {
                    key = createKey(inputState);
                    Value value = stateCache.get(key);
                    if (value != null) {
                        res = value.value.get();
                        value.count++;
                        count = value.count;
                    }
                }
            } finally {
                stateCacheLock.readLock().unlock();
            }
            if (TRACE && res != null) {
                stateCacheSuccessAttempt++;
                System.err.println("State Cache Attempt="+stateCacheAttempt+" successful="+stateCacheSuccessAttempt+" cache size="+stateCache.size()+" in file "+file.getName()+" hits "+count);
                System.err.println("    Key="+key);
                System.err.println("    Res="+createKey(res.state));
            }
        }
        return res;
    }

    /*package-local*/ void clearStateCache() {
        if (cacheStates) {
            try {
                stateCacheLock.writeLock().lock();
                stateCache.clear();
                if (TRACE) {
                   System.err.println("Clear State Cache in file "+file.getName());
                }
            } finally {
                stateCacheLock.writeLock().unlock();
            }
        }
    }

    private static PreprocHandler.StateKey createKey(PreprocHandler.State inputState){
        return APTHandlersSupport.getStateKey(inputState);
    }

    private boolean isCacheableState(PreprocHandler.State inputState) {
        //return !APTHandlersSupport.isEmptyActiveMacroMap(inputState);
        return true;//APTHandlersSupport.getMacroSize(inputState) < MAX_KEY_SIZE;
    }

    private static class Value {
        private final Reference<PreprocessorStatePair> value;
        private int count;
        private Value(PreprocessorStatePair value){
            if (CndTraceFlags.WEAK_REFS_HOLDERS_FILE_STATE) {
                this.value = new WeakReference<>(value);
            } else {
                this.value = new SoftReference<>(value);
            }
        }
    }
}
