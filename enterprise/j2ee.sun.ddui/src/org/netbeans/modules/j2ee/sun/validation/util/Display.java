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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.j2ee.sun.validation.Failure;

/**
 * Display is a class that provides utiltiy methods for displaying
 * validation failures.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class Display {

    /** Creates a new instance of <code>Display</code>. */
    public Display() {
    }


    /**
     * Displays validation failures in a command mode.
     * It systems out the failure messages.
     */
    public void text(Collection collection) {
        Object object = null;
        Failure failure = null;

        if(collection != null){
            Iterator iterator = collection.iterator();
            while(iterator.hasNext()){
                object = iterator.next();
                boolean failureObect = isFailureObject(object);
                if(failureObect){
                    failure = (Failure) object;
                    reportFailure(failure.failureMessage());
                } else {
                    reportError(object);
                }
            }
        }
    }


    /** 
     * Displays validation failures in a GUI mode.
     */
    public void gui(Collection collection){
        assert false : 
                (BundleReader.getValue("MSG_not_yet_implemented"));     //NOI18N
    }


    /** 
     * Systems out the failure message.
     * @param message the failure message to report.
     */
    protected void reportFailure(String message){
        System.out.println(message);
    }


    /** 
     * Reports an error message.
     * @param object the given object which is not of type {@link Failure}
     */
    protected void reportError(Object object){
        String format = BundleReader.getValue(
            "MSG_does_not_support_displaying_of");                      //NOI18N
        Class classObject = object.getClass();
        Object[] arguments = new Object[]{"Display",                    //NOI18N
            classObject.getName()};

        String message = 
            MessageFormat.format(format, arguments);

        assert false : message;
    }


    /** 
     * Determines whether the given <code>object</code> is of
     * type  {@link Failure}
     * @param object the given object to determine the type of
     * @return <code>true</code> only if the given 
     * <code>object</code> is of type <code>Failure</code>
     */
    protected boolean isFailureObject(Object object){
        return (object instanceof Failure);
    }
}
