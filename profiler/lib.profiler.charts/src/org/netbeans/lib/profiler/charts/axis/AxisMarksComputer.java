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

package org.netbeans.lib.profiler.charts.axis;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.SwingConstants;
import org.netbeans.lib.profiler.charts.ChartContext;

/**
 *
 * @author Jiri Sedlacek
 */
public interface AxisMarksComputer {

    public static final Iterator<AxisMark> EMPTY_ITERATOR =
        new Iterator<AxisMark>() {
            public boolean hasNext() { return false; }
            public AxisMark next() { throw new NoSuchElementException(); }
            public void remove() { throw new IllegalStateException(); }
        };


    public Iterator<AxisMark> marksIterator(int start, int end);


    public abstract static class Abstract implements AxisMarksComputer {

        protected final ChartContext context;

        protected final int orientation;

        protected final boolean horizontal;
        protected final boolean reverse;


        public Abstract(ChartContext context, int orientation) {
            
            this.context = context;
            
            this.orientation = orientation;

            horizontal = orientation == SwingConstants.HORIZONTAL;
            reverse = horizontal ? context.isRightBased() :
                                   context.isBottomBased();
        }

        // Return minimum distance between two axis marks
        protected int getMinMarksDistance() {
            return 50;
        }

        // Returns true if the configuration changed and axis should be repainted
        protected boolean refreshConfiguration() {
            return true;
        }

    }


    public abstract static class AbstractIterator implements Iterator<AxisMark> {
        public void remove() {
            throw new UnsupportedOperationException(
                      "AxisMarksComputer does not support remove()"); // NOI18N
        }
    }

}
