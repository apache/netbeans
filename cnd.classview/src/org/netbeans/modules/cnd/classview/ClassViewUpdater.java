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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.classview;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 * Deals with class view model updates
 */
public class ClassViewUpdater extends Thread {
    
    private static final boolean traceEvents = Boolean.getBoolean("cnd.classview.updater-events"); // NOI18N
    
    private static class BlockingQueue {
        
        private LinkedList<SmartChangeEvent> data = new LinkedList<SmartChangeEvent>();
        
        private final Object lock = new Object();
        
        public SmartChangeEvent get() throws InterruptedException {
            synchronized( lock ) {
                while( data.isEmpty() ) {
                    lock.wait();
                }
                return data.removeFirst();
            }
        }
        
        public void add(SmartChangeEvent event) {
            synchronized( lock ) {
                data.add(event);
                lock.notify();
            }
        }
        
        public SmartChangeEvent peek() throws InterruptedException {
            synchronized( lock ) {
                while( data.isEmpty() ) {
                    lock.wait();
                }
                return data.peek();
            }
        }
        
        public boolean isEmpty() throws InterruptedException {
            synchronized( lock ) {
                return data.isEmpty();
            }
        }
    }
    
    private ClassViewModel model;
    private BlockingQueue queue;
    private volatile boolean isStoped = false;
    
    /*package-local*/ ClassViewUpdater(ClassViewModel model) {
        super("Class View Updater"); // NOI18N
        this.model = model;
        queue = new BlockingQueue();
    }
    
    public void setStop(){
        isStoped = true;
        if (queue != null) {
            queue.add(null);
        }
    }
    
    /**
     * delay before class view update.
     */
    private static final int MINIMAL_DELAY = 500;
    
    /**
     * delay before checking queue in batch mode.
     */
    private static final int BATCH_MODE_DELAY = 1000;
    
    /**
     * stop collect events when batch contains:
     */
    private static final int MAXIMAL_BATCH_SIZE = 50;
    
    /**
     * stop collect events when batch consume time in second:
     */
    private static final int MAXIMAL_BATCH_TIME = 10;
    
    /**
     * delay on user activity.
     */
    private static final int USER_ACTIVITY_DELAY = 1000;
    
    @Override
    public void run() {
        long start = 0;
        try {
            while( true ) {
                if (isStoped) {
                    return;
                }
                SmartChangeEvent compose = queue.get();
                if (isStoped) {
                    return;
                }
                if (queue.isEmpty()) {
                    Thread.sleep(MINIMAL_DELAY);
                }
                int doWait = 0;
                while(true){
                    if (isStoped) {
                        return;
                    }
                    while(!queue.isEmpty()){
                        if (isStoped) {
                            return;
                        }
                        SmartChangeEvent e = queue.peek();
                        if (!compose.addChangeEvent(e)){
                            break;
                        }
                        queue.get();
                        if (queue.isEmpty() && compose.getCount() < MAXIMAL_BATCH_SIZE && doWait < MAXIMAL_BATCH_TIME) {
                            doWait++;
                            Thread.sleep(BATCH_MODE_DELAY);
                        }
                    }
                    if (model.isUserActivity()){
                        Thread.sleep(USER_ACTIVITY_DELAY);
                        continue;
                    }
                    break;
                }
                if (traceEvents) {
                    start = System.nanoTime();
                }
                if (isStoped) {
                    return;
                }
                model.update(compose);
                if (traceEvents) {
                    long end = System.nanoTime();
                    long time = (end-start)/1000000;
                    System.out.println("Compose change event contains "+compose.getCount()+ // NOI18N
                            " events. Time = "+((float)(time)/1000.)); // NOI18N
                    for(Map.Entry<CsmProject, SmartChangeEvent.Storage> entry : compose.getChangedProjects().entrySet()){
                        System.out.println("    Project "+entry.getKey().getName()+ // NOI18N
                                " Nd="+entry.getValue().getNewDeclarations().size()+ // NOI18N
                                ", Rd="+entry.getValue().getRemovedDeclarations().size()+ // NOI18N
                                ", Ud="+entry.getValue().getChangedDeclarations().size()+ // NOI18N
                                ", Nn="+entry.getValue().getNewNamespaces().size()+ // NOI18N
                                ", Rn="+entry.getValue().getRemovedNamespaces().size()); // NOI18N
                    }
                }
            }
        } catch( InterruptedException e ) {
            return;
        } finally {
            model = null;
            queue = null;
        }
    }
    
    public void scheduleUpdate(CsmChangeEvent e) {
        //model.update(e);
        if (queue != null) {
            queue.add(new SmartChangeEvent(e));
        }
    }
}
