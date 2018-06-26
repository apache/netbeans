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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.j2ee.sun.validation.Failure;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;

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
