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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.source.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.openide.util.Parameters;

/** 
 * 
 * @author Petr Hrebejk, Tomas Zezula
 */
public final class Iterators {

    private static final String NULL_AS_PARAMETER_MESSAGE = "Iterator(s) passed in as parameter must NOT be null."; // NOI18N
    
    /** Singleton */
    private Iterators() {}
        
    /**
     * Creates an chained {@link Iterable}.
     * PRE: This method doesn't do a defensive copy, by other words the caller 
     * is responsible for encuring that the passed {@link Iterable}s are unmodifiable.
     * The returned {@link Iterable} is thread save when the above precondition is fulfilled.
     * PRE: The iterables may not contain null references.
     * @param iterables to create chained {@link Iterable} for
     * @return {@param Iterable}
     */
    public static <T> Iterable<T> chained( Iterable<? extends Iterable<T>> iterables ) {
        return new ChainedIterable<T> (iterables);
    }
    
    public static <T> Iterable<T> filter ( final Iterable<T> it, final Comparable<? super T> c) {
        Parameters.notNull("it",it);   //NOI18N
        Parameters.notNull("c", c);    //NOI18N
        return new FilterIterable (it,c);
    }
               
    // Innerclasses ------------------------------------------------------------
    
    private static class FilterIterable<T> implements Iterable<T> {
        
        private final Iterable<T> it;
        private final Comparable<? super T> c;
        
        public FilterIterable (final Iterable<T> it, final Comparable<? super T> c) {
            this.it = it;
            this.c = c;
        }

        public Iterator<T> iterator() {
            return new FilterIterator (it.iterator(),c);
        }
        
    }
    
    //@NotThreadSafe
    private static class FilterIterator<T> implements Iterator<T> {
        
        private final Iterator<T> it;
        private final Comparable<? super T> c;
        private T nextValue;
        
        public FilterIterator (final Iterator<T> it, final Comparable<? super T> c) {
            this.it = it;
            this.c = c;
        }

        public boolean hasNext() {
            if (nextValue != null) {
                return true;
            }
            while (it.hasNext()) {
                T val = it.next();
                if (c.compareTo(val)!=0) {
                    nextValue = val;
                    return true;
                }
            }
            return false;
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException ();
            }
            T ret = this.nextValue;
            this.nextValue = null;
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported operation.");
        }
    }
            
       
    private static class ChainedIterable<T> implements Iterable<T> {
        
        final Iterable<? extends Iterable<T>> iterables;
        
        ChainedIterable (final Iterable<? extends Iterable<T>> iterables) {
            assert iterables != null;
            this.iterables = iterables;
        }
        
        public Iterator<T> iterator() {
            return new ChainedIterator (this.iterables);
        }        
    }

        
    private static class ChainedIterator<E> implements Iterator<E> {

        /** The chain of iterators */
        protected final Iterator< ? extends Iterable<E>> iteratorChain;

        /** The current iterator */
        protected Iterator<E> currentIterator = null;
                              
        /**
         * Constructs a new <code>IteratorChain</code> over the collection of
         * iterators.
         *
         * @param iterators the collection of iterators
         * 
         * @throws NullPointerException if iterators collection is or contains
         *  <code>null</code>
         */
        public ChainedIterator(final Iterable<? extends Iterable<E>> iterators) {
            this.iteratorChain = iterators.iterator();
        }
        
        /**
         * Updates the current iterator field to ensure that the current
         * <code>Iterator</code> is not exhausted
         */
        protected void updateCurrentIterator() {
            if (currentIterator == null) {
                if (!iteratorChain.hasNext()) {
                    // @todo How to manage the EmptyIterator.INSTANCE warning?
                    currentIterator = Collections.<E>emptyList().iterator();
                } else {
                    currentIterator = iteratorChain.next().iterator();
                }
            }

            while (currentIterator.hasNext() == false && iteratorChain.hasNext()) {
                currentIterator = iteratorChain.next().iterator();
            }
        }

        // Iterator interface methods
        // -------------------------------------------------------------------------

        /**
         * @inheritDoc
         */
        public boolean hasNext() {
            updateCurrentIterator();
            return currentIterator.hasNext();
        }

        /**
         * @inheritDoc         
         */
        public E next() {
            updateCurrentIterator();
            return currentIterator.next();
        }

        /**
         * Not supported operation. Throws {@link UnsupportedOperationException}
         * @throws UnsupportedOperationException
         */
        public void remove() throws UnsupportedOperationException {            
            throw new UnsupportedOperationException ();
        }


    }           
}
