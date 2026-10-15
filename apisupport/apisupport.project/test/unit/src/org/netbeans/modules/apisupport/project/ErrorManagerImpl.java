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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;

/**
 * NbTestCase logging error manager.
 * @author Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.ErrorManager.class)
public class ErrorManagerImpl extends ErrorManager {

    static NbTestCase running;

    private String prefix;

    /** Creates a new instance of ErrorManagerImpl */
    public ErrorManagerImpl() {
        this("[em]");
    }

    private ErrorManagerImpl(String p) {
        this.prefix = p;
    }
    
    public static void registerCase(NbTestCase r) {
        running = r;
    }
    
    public Throwable attachAnnotations(Throwable t, Annotation[] arr) {
        return t;
    }
    
    public Annotation[] findAnnotations(Throwable t) {
        return null;
    }
    
    public Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
        return t;
    }
    
    public void notify(int severity, Throwable t) {
        StringWriter w = new StringWriter();
        w.write(prefix);
        w.write(' ');
        t.printStackTrace(new PrintWriter(w));
        
        System.err.println(w.toString());
        
        if (running == null) {
            return;
        }
        running.getLog().println(w.toString());
    }
    
    public void log(int severity, String s) {
        String msg = prefix + ' ' + s;
        if (severity != INFORMATIONAL) {
            System.err.println(msg);
        }
        
        if (running == null) {
            return;
        }
        running.getLog().println(msg);
    }
    
    public ErrorManager getInstance(String name) {
        return new ErrorManagerImpl(name);
    }
    
}
