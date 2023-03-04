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

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.reflect.Method;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.windows.OutputWriter;

/**
 *
 * @author  Marian Petras
 */
public final class PrintDetailsTask implements Runnable {

    /** */
    private static final int BUFFER_SIZE = 8;
    /** */
    private final List<MatchingObject> objects;
    /** */
    private final BasicSearchCriteria basicSearchCriteria;
    /** */
    private final Node[] buffer = new Node[BUFFER_SIZE];
    /** position of the first free item in the buffer */
    private int bufPos = 0;
    /** */
    private SearchDisplayer displayer;
    /** */
    private volatile boolean interrupted = false;

    /** Creates a new instance of PrintDetailsTask */
    public PrintDetailsTask(final List<MatchingObject> matchingObjects,
                     final BasicSearchCriteria basicCriteria) {
        this.objects = matchingObjects;
        this.basicSearchCriteria = basicCriteria;
    }

    /** */
    @Override
    public void run() {
        displayer = new SearchDisplayer();
        callDisplayerFromAWT("prepareOutput");                    //NOI18N
        
        int freeBufSpace = 0;
        for (MatchingObject obj : objects) {

            /* Collect details about the found node: */
            Node[] allDetails = null;
            if (basicSearchCriteria != null) {
                Node[] details = obj.getDetails();
                if (details != null && details.length != 0) {
                    allDetails = details;
                }
            }
            if (allDetails == null) {
                continue;
            }

            /* Print the collected details: */
            freeBufSpace = addToBuffer(allDetails, 0);
            while (freeBufSpace < 0) {
                printBuffer();

                int remainderIndex = allDetails.length + freeBufSpace;
                freeBufSpace = addToBuffer(allDetails, remainderIndex);
            }
            if (freeBufSpace == 0) {
                printBuffer();
            }
            
            if (interrupted) {
                break;
            }
        }
        if ((freeBufSpace != 0) && !interrupted) {
            int smallBufSize = BUFFER_SIZE - freeBufSpace;
            Node[] smallBuffer = new Node[smallBufSize];
            System.arraycopy(buffer, 0, smallBuffer, 0, smallBufSize);
            displayer.displayNodes(smallBuffer);
        }
        
        /*
         * We must call this even if this task is interrupted. We must close
         * the output window.
         */
        callDisplayerFromAWT("finishDisplaying");
    }

    /**
     * Stops this search task.
     *
     * @see  #stop(boolean)
     */
    void stop() {
        interrupted = true;
    }
    
    /**
     */
    Reference<OutputWriter> getOutputWriterRef() {
        return displayer.getOutputWriterRef();
    }

    /**
     * Adds some or all of the given nodes to the buffer.
     * Nodes at position lesser than the given start index are ignored.
     * If the nodes to be added do not fit all into the buffer, the remaining
     * nodes are ignored.
     *
     * @param  detailNodes  array containing nodes to be added
     * @param  firstIndex  index of the first node to be added to the buffer
     * @return  positive number expressing number of free items in the buffer;
     *          negative number expressing number of remaining nodes that
     *                  did not fit into the buffer;
     *          or <code>0</code> if the nodes exactly filled the buffer
     */
    private int addToBuffer(Node[] detailNodes, int firstIndex) {
        assert firstIndex >=0 && firstIndex <= detailNodes.length;

        int nodesToAddCount = detailNodes.length - firstIndex;
        int newBufPos = bufPos + nodesToAddCount;
        int remainingSpace = BUFFER_SIZE - newBufPos;
        if (remainingSpace <= 0) {
            nodesToAddCount += remainingSpace;
            newBufPos = 0;
        }
        System.arraycopy(detailNodes, firstIndex, buffer, bufPos, nodesToAddCount);
        bufPos = newBufPos;
        return remainingSpace;
    }

    /**
     */
    private void printBuffer() {
        displayer.displayNodes(buffer);
    }

    /**
     */
    private Node[] concatNodeArrays(Node[] arrA, Node[] arrB) {
        Node[] result = new Node[arrA.length + arrB.length];

        System.arraycopy(arrA,   0,
                         result, 0,
                         arrA.length);
        System.arraycopy(arrB,   0,
                         result, arrA.length,
                         arrB.length);
        return result;
    }
    
    /**
     */
    private void callDisplayerFromAWT(final String methodName) {
        try {
            final Method method = SearchDisplayer.class
                    .getDeclaredMethod(methodName, new Class<?>[0]);
            Runnable runnable = () -> {
                try {
                    method.invoke(displayer, (Object[]) null);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            };
            if (EventQueue.isDispatchThread()) {
                runnable.run();
            } else {
                EventQueue.invokeAndWait(runnable);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
}
