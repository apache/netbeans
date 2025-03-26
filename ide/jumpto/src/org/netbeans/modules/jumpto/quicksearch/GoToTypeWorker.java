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

package org.netbeans.modules.jumpto.quicksearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.modules.jumpto.type.TypeComparator;
import org.netbeans.modules.jumpto.type.TypeProviderAccessor;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * xxx: Copy/paste from GoToTypeAction
 * @author  Jan Becicka
 * @author  Tomas Zezula
 */
public class GoToTypeWorker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(GoToTypeWorker.class.getName());

    private volatile boolean isCanceled = false;
    private final String text;
    private final long createTime;
    private List<? extends TypeDescriptor> types = Collections.<TypeDescriptor>emptyList();

    public GoToTypeWorker( String text ) {
        this.text = text;
        this.createTime = System.currentTimeMillis();
    }

    public List<? extends TypeDescriptor> getTypes() {
        return types;
    }

    @Override
    public void run() {
        LOGGER.log(
                Level.FINE,
                "Worker for {0} - started {1} ms.", //NOI18N
                new Object[]{
                    text,
                    System.currentTimeMillis() - createTime
                });
        types = getTypeNames( text );
    }

    public void cancel() {
        isCanceled = true;
    }

    private List<? extends TypeDescriptor> getTypeNames(final String text) {
        // Multiple providers: merge results
        List<TypeDescriptor> items = new ArrayList<TypeDescriptor>(128);
        List<TypeDescriptor> ccItems = new ArrayList<TypeDescriptor>(128);
        String[] message = new String[1];

        final TypeProvider.Context context =
              TypeProviderAccessor.DEFAULT.createContext(null, text, SearchType.CASE_INSENSITIVE_PREFIX);
        final TypeProvider.Result result = TypeProviderAccessor.DEFAULT.createResult(items, message, context);
        final TypeProvider.Context ccContext =
              TypeProviderAccessor.DEFAULT.createContext(null, text, SearchType.CAMEL_CASE);
        final TypeProvider.Result ccResult = TypeProviderAccessor.DEFAULT.createResult(ccItems, message, context);

        final Collection<? extends TypeProvider> providers = Lookup.getDefault().lookupAll(TypeProvider.class);
        try {
            computeTypeNames(providers, context, result);
            computeTypeNames(providers, ccContext, ccResult);
            if (isCanceled) {
                throw new InterruptedException();
            }
        } catch(InterruptedException ie) {
            return Collections.<TypeDescriptor>emptyList();
        } finally {
            cleanUp(providers);
        }

        TreeSet<TypeDescriptor> ts =
                new TreeSet<TypeDescriptor>(new TypeComparatorFO());
        ts.addAll(ccItems);
        ts.addAll(items);
        items.clear();
        items.addAll(ts); //eliminate duplicates
        items.sort(TypeComparator.create(GoToSettings.SortingType.LEXICOGRAPHIC, text, false, true));
        return items;
    }

    /**
     * Computes type names via specified collection of the {@code providers}.
     * @param providers the providers.
     * @param context the search context.
     * @param result the search result.
     * @throws InterruptedException if operation is canceled.
     */
    private void computeTypeNames(
            final Collection<? extends TypeProvider> providers,
            final TypeProvider.Context context,
            final TypeProvider.Result result) throws InterruptedException {
        for (TypeProvider provider : providers) {
            if (isCanceled) {
                throw new InterruptedException();
            }
            provider.computeTypeNames(context, result);
        }
    }

    private void cleanUp (final Collection<? extends TypeProvider> providers) {
        for (TypeProvider tp : providers) {
            try {
                tp.cleanup();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                } else {
                    Exceptions.printStackTrace(t);
                }
            }
        }
    }

    private class TypeComparatorFO extends EntityComparator<TypeDescriptor> {

        @Override
        public int compare(TypeDescriptor t1, TypeDescriptor t2) {
            int cmpr = compare(t1.getTypeName(), t2.getTypeName());
            if (cmpr != 0) {
                return cmpr;
            }
            cmpr = compare(t1.getOuterName(), t2.getOuterName());
            if (cmpr != 0) {
                return cmpr;
            }            
            //FileObject does not have to be available
            //if t1 fo is not null and t2 not null => -1
            //t1 fo null => no check
            final String fdp1 = t1.getFileDisplayPath();
            if (!fdp1.isEmpty() && !fdp1.equals(t2.getFileDisplayPath())) {
                return -1;
            }
            return compare(t1.getContextName(), t2.getContextName());
        }

    } // TypeComparatorFO

}
