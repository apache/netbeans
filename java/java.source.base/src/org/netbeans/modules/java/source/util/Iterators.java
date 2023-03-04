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
