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

package org.netbeans.modules.apisupport.project;

import java.awt.Dialog;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.lookup.ServiceProvider;

/** Test ready implementation of DialogDisplayer.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=DialogDisplayer.class)
public class DialogDisplayerImpl extends DialogDisplayer {
    
    private static Object toReturn;
    
    private NotifyDescriptor lastNotifyDescriptor;
    
    public static void returnFromNotify(Object value) {
        Object o = DialogDisplayer.getDefault();
        assertEquals("My class", DialogDisplayerImpl.class, o.getClass());
        toReturn = value;
    }
    
    @Override
    public Object notify(NotifyDescriptor descriptor) {
        lastNotifyDescriptor = descriptor;
        Object r = toReturn;
        toReturn = null;
        
        assertNotNull("We are supposed to return a value", r);
        return r;
    }
    
    @Override
    public Dialog createDialog(DialogDescriptor descriptor) {
        throw new UnsupportedOperationException();
    }
    
    public NotifyDescriptor getLastNotifyDescriptor() {
        return lastNotifyDescriptor;
    }
    
    public void reset() {
        this.lastNotifyDescriptor = null;
    }

}
