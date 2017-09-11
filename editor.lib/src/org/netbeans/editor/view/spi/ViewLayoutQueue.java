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

package org.netbeans.editor.view.spi;

import javax.swing.text.View;

/**
 * This is a layout queue similar to the {@link javax.swing.text.LayoutQueue}
 * with certain improvements.
 *
 * <p>
 * Unlike the swing implementation this one uses a circular buffer
 * so it does no shifting of the runnables in the vector.
 *
 * <p>
 * The queue will shrink its size to 1/4 automatically if its size
 * would be lower than 1/8 of currently allocated array.
 *
 * <p>
 * Any view implementation wishing to perform asynchronous layout
 * may use {@link #getDefaultQueue()} or create its own queue instance.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class ViewLayoutQueue {
    
    private static ViewLayoutQueue defaultQueue;
    
    private static SynchronousQueue synchronousQueue;

    private Runnable[] taskArray;

    private int startIndex;
    
    private int endIndex;
    
    private Thread worker;
    
    /**
     * Construct a layout queue.
     */
    public ViewLayoutQueue() {
	taskArray = new Runnable[2]; // must be exactly power of 2
    }

    /**
     * Get the default layout queue.
     */
    public static ViewLayoutQueue getDefaultQueue() {
	if (defaultQueue == null) {
	    defaultQueue = new ViewLayoutQueue();
	}
	return defaultQueue;
    }

    public static ViewLayoutQueue getSynchronousQueue() {
	if (synchronousQueue == null) {
	    synchronousQueue = new SynchronousQueue();
	}
	return synchronousQueue;
    }

    public synchronized Thread getWorkerThread() {
        ensureWorkerInited();

        return worker;
    }
    
    private void ensureWorkerInited() {
        if (worker == null) {
            worker = new LayoutThread();
            worker.start();
        }
    }        

    /**
     * Add a task to the queue.
     */
    public synchronized void addTask(Runnable task) {
        if (task != null) {
            
            // Ensure that worker thread is started
            ensureWorkerInited();

            if (startIndex == endIndex && taskArray[startIndex] != null) { // full
                // new task array size must be exactly power of 2
                Runnable[] newTaskArray = new Runnable[(taskArray.length << 1)];
                
                // Copy the tasks into new array in the right order
                int afterStartIndexLength = taskArray.length - startIndex;
                System.arraycopy(taskArray, endIndex, newTaskArray, 0,
                    afterStartIndexLength);
                
                System.arraycopy(taskArray, 0, newTaskArray, afterStartIndexLength,
                    startIndex);
                
                startIndex = 0;
                endIndex = taskArray.length; // number of items in original array
                taskArray = newTaskArray;
            }
            
            taskArray[endIndex] = task;
            endIndex = (endIndex + 1) & (taskArray.length - 1);

            notify();
        }
    }

    /**
     * Used by the worker thread to get a new task to execute
     */
    synchronized Runnable waitForTask() {
	while (startIndex == endIndex && taskArray[startIndex] == null) { // empty
	    try {
		wait();
	    } catch (InterruptedException ie) {
		return null;
	    }
	}

        Runnable task = taskArray[startIndex];
        taskArray[startIndex] = null;
        int taskArrayLength = taskArray.length;
        startIndex = (startIndex + 1) & (taskArrayLength - 1);
        
        // Try to shrink to 1/4 if size < 1/8 of current size
        if (taskArrayLength >= 128) { // shrink to no less than 32 items (size / 4)
            int indexDiff = (endIndex - startIndex);
            if (indexDiff >= 0) { // endIndex >= startIndex
                if (indexDiff < (taskArrayLength / 8)) {
                    Runnable[] smallerTaskArray = new Runnable[(taskArrayLength / 4)];
                    System.arraycopy(taskArray, startIndex, smallerTaskArray, 0, indexDiff);
                    taskArray = smallerTaskArray;
                    startIndex = 0;
                    endIndex = indexDiff;
                }
                
            } else { // endIndex < startIndex
                indexDiff = taskArrayLength + indexDiff; // size without empty space
                if (indexDiff < (taskArrayLength / 8)) {
                    Runnable[] smallerTaskArray = new Runnable[(taskArrayLength / 4)];
                    System.arraycopy(taskArray, startIndex, smallerTaskArray, 0,
                        taskArrayLength - startIndex);
                    System.arraycopy(taskArray, 0, smallerTaskArray,
                        taskArrayLength - startIndex, endIndex);
                    taskArray = smallerTaskArray;
                    startIndex = 0;
                    endIndex = indexDiff;
                }
            }
        }           
                    
	return task;
    }

    /**
     * low priority thread to perform layout work forever
     */
    class LayoutThread extends Thread {
	
	LayoutThread() {
	    super("Text-Layout"); // NOI18N
	    setPriority(Thread.MIN_PRIORITY);
	}
	
        public void run() {
	    Runnable task;
	    do {
		task = waitForTask();
		if (task != null) {
		    task.run();
		}
	    } while (task != null);
	}


    }

    /**
     * Testing queue that executes the tasks synchronously
     * that should allow possible threading issues
     * to be discovered.
     */
    private static class SynchronousQueue extends ViewLayoutQueue {
        
        public void addTask(Runnable r) {
            r.run();
        }
        
    }

}
