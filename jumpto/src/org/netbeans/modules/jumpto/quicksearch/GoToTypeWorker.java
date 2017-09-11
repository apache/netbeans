/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    private List<? extends TypeDescriptor> types = Collections.<TypeDescriptor>emptyList();;

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

    private List<? extends TypeDescriptor> getTypeNames(String text) {
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
        Collections.sort(items, new TypeComparator());
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
