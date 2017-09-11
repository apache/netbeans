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

package org.netbeans.modules.apisupport.project;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import junit.framework.Assert;
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
