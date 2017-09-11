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

package org.netbeans.modules.parsing.lucene;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.BaseUtilities;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class IndexCacheFactory {
    
    private static final Logger LOG = Logger.getLogger(IndexCacheFactory.class.getName());
    private static final String PROP_CACHE_SIZE = "java.index.size";    //NOI18N
    private static final IndexCacheFactory instance = new IndexCacheFactory();
    private final RAMController ramController;
    private final LRUCache<URI, Evictable> nioCache;
    private final LRUCache<URI, Evictable> ramCache;

    private IndexCacheFactory() {
        this.ramController = new RAMController();
        this.nioCache = new LRUCache<>(new NIOPolicy());
        this.ramCache = new LRUCache<>(new RAMPolicy(ramController));
    }

    @NonNull
    LRUCache<URI,Evictable> getNIOCache() {
        return nioCache;
    }

    @NonNull
    LRUCache<URI,Evictable> getRAMCache() {
        return ramCache;
    }

    @NonNull
    RAMController getRAMController() {
        return ramController;
    }

    public static IndexCacheFactory getDefault() {
        return instance;
    }

    static final class RAMController {
        private static final float DEFAULT_CACHE_SIZE = 0.05f;
        private static final long maxCacheSize = getCacheSize();
        private final AtomicLong currentCacheSize = new AtomicLong();


        public long acquire(final long bytes) {
            return currentCacheSize.addAndGet(bytes);
        }

        public long release(final long bytes) {
            return currentCacheSize.addAndGet(~bytes + 1);
        }

        public boolean isFull() {
            return currentCacheSize.get() > maxCacheSize;
        }

        public boolean shouldLoad(final long bytes) {
            //Todo: Preffer NMAP to RAM
            return bytes < maxCacheSize;
        }

        private static long getCacheSize() {
            float per = -1.0f;
            final String propVal = System.getProperty(PROP_CACHE_SIZE);
            if (propVal != null) {
                try {
                    per = Float.parseFloat(propVal);
                } catch (NumberFormatException nfe) {
                    //Handled below
                }
            }
            if (per<0) {
                per = DEFAULT_CACHE_SIZE;
            }
            return (long) (per * Runtime.getRuntime().maxMemory());
        }
    }

    private static final class NIOPolicy implements EvictionPolicy<URI,Evictable> {
        private static final int DEFAULT_SIZE = 400;
        private static final boolean NEEDS_REMOVE =  Boolean.getBoolean("IndexCache.force") || (BaseUtilities.isUnix() && !BaseUtilities.isMac());  //NOI18N
        private static final int MAX_SIZE;
        static {
            int value = DEFAULT_SIZE;
            final String sizeStr = System.getProperty("IndexCache.size");   //NOI18N
            if (sizeStr != null) {
                try {
                    value = Integer.parseInt(sizeStr);
                } catch (NumberFormatException nfe) {
                    LOG.warning("Wrong (non integer) cache size: " + sizeStr);  //NOI18N
                }
            }            
            MAX_SIZE = value;
            LOG.fine("NEEDS_REMOVE: " + NEEDS_REMOVE +" MAX_SIZE: " + MAX_SIZE);    //NOI18N
        }

        @Override
        public boolean shouldEvict(int size, URI key, Evictable value) {
            return NEEDS_REMOVE && size>MAX_SIZE;
        }
    }

    private static final class RAMPolicy implements EvictionPolicy<URI, Evictable> {

        private final RAMController controller;

        RAMPolicy(@NonNull final RAMController controller) {
            Parameters.notNull("controller", controller);   //NOI18N
            this.controller = controller;
        }

        @Override
        public boolean shouldEvict(int size, URI key, Evictable value) {
            return controller.isFull();
        }


        
    }
}
