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
            Class clazz = Class.forName("org.netbeans.progress.module.Controller");
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
