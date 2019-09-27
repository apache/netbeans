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

package org.netbeans.test.javaee.lib;

import java.lang.reflect.Method;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.Operator;

/**
 * Handle Progress bars at the main window of NetBeans.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ProgressOperator {

    /** Wait process started.
     */
    public static void waitStarted(final String name, long timeout) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object anObject) {
                    return processInProgress(name) ? Boolean.TRUE : null;
                }
                public String getDescription() {
                    return("Wait process "+name+" is started.");
                }
            });
            waiter.getTimeouts().setTimeout("Waiter.WaitingTime", timeout);
            waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
        
    }
    
    /** Wait process with given name finished.
     */
    public static void waitFinished(final String name, long timeout) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object anObject) {
                    return processInProgress(name) ? null : Boolean.TRUE;
                }
                public String getDescription() {
                    return("Wait process "+name+" is finished.");
                }
            });
            waiter.getTimeouts().setTimeout("Waiter.WaitingTime", timeout);
            waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
        
    }
    
    /** Wait all processes finished.
     */
    public static void waitFinished(long timeout) {
        waitFinished("", timeout); // NOI18N
    }
    
    private static boolean processInProgress(String name) {
        try {
            Class<?> clazz = Class.forName("org.netbeans.progress.module.Controller");
            Method getDefaultMethod = clazz.getDeclaredMethod("getDefault", (Class[])null);
            getDefaultMethod.setAccessible(true);
            Object controllerInstance = getDefaultMethod.invoke(null, (Object[])null);
            
            Method getModelMethod = clazz.getDeclaredMethod("getModel", (Class[])null);
            getModelMethod.setAccessible(true);
            Object taskModelInstance = getModelMethod.invoke(controllerInstance, (Object[])null);
            
            //Method getSizeMethod = taskModelInstance.getClass().getDeclaredMethod("getSize", (Class[])null);
            //Object size = getSizeMethod.invoke(taskModelInstance, (Object[])null);
            //System.out.println("SIZE="+((Integer)size));
            
            Method getHandlesMethod = taskModelInstance.getClass().getDeclaredMethod("getHandles", (Class[])null);
            Object[] handles = (Object[])getHandlesMethod.invoke(taskModelInstance, (Object[])null);
            
            for(int i=0;i<handles.length;i++) {
                Method getDisplayNameMethod = handles[i].getClass().getDeclaredMethod("getDisplayName", (Class[])null);
                String displayName = (String)getDisplayNameMethod.invoke(handles[i], (Object[])null);
                //System.out.println("DISPLAY_NAME="+displayName);
                if(Operator.getDefaultStringComparator().equals(displayName, name)) {
                    return true;
                }
            }
            return false;
            
            //Method addListDataListenerMethod = taskModelInstance.getClass().getDeclaredMethod("addListDataListener", ListDataListener.class);
            //addListDataListenerMethod.invoke(taskModelInstance, new TestProgressBar());
            
            
        } catch (Exception e) {
            throw new JemmyException("Reflection operation failed.", e);
        }
    }
}
