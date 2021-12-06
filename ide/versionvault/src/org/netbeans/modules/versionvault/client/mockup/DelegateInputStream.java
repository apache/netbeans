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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.client.mockup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka
 */
public class DelegateInputStream extends InputStream {
    private List<InputStream> delegates = new ArrayList<InputStream>();
    private Object LOCK = new Object();

    public void setDelegate(InputStream inputStream) {            
        synchronized(LOCK) {
            delegates.add(inputStream);    
        }                        
    }    

    private InputStream getCurrent() throws IOException {
        synchronized(LOCK) { 
            if(delegates.size() == 0) {
                return null;
            }
            return delegates.get(0);
        }
    }

    @Override
    public int read() throws IOException {
        InputStream is = null;
        while(is == null) {
            is = getCurrent();
            try {
                Thread.sleep(10);                    
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }    
        int i = is.read();
        if(i == -1) {
            synchronized(LOCK) { 
                delegates.remove(is);
                is = getCurrent();                    
            }
            if(is != null) {
                i = is.read();
                return is.read();
            }
        }
        return i;
    }

    @Override
    public int available() throws IOException {
        synchronized(LOCK) {  
            InputStream is = getCurrent();
            if(is != null) {                    
                int i = is.available();
                if(i == 0) {
                    delegates.remove(is);
                    return available();
                }
                return i;
            } else {
                return 0;
            }
        }                 
    }
    @Override
    public void close() throws IOException {
        InputStream is = getCurrent();
        if(is != null) {
            is.close();            
        }
        super.close();
    }
    @Override
    public synchronized void mark(int readlimit) {
        try {
            InputStream is = getCurrent();
            if (is != null) {
                is.mark(readlimit);
            }
            super.mark(readlimit);
        } catch (IOException ex) {
            CleartoolMockup.LOG.log(Level.WARNING, null, ex);
        } 
    }
    @Override
    public boolean markSupported() {
        try {
            InputStream is = getCurrent();
            if (is != null) {
                return is.markSupported();
            }
        } catch (IOException ex) {
            CleartoolMockup.LOG.log(Level.WARNING, null, ex);
        }
        return super.markSupported();
    }
    @Override
    public int read(byte[] b) throws IOException {
        InputStream is = getCurrent();
        if(is != null) {
            return is.read(b);
        }    
        return super.read(b);
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        InputStream is = getCurrent();
        if(is != null) {
            return is.read(b, off, len);
        }            
        return super.read(b, off, len);
    }
    @Override
    public synchronized void reset() throws IOException {
        InputStream is = getCurrent();
        if(is != null) {
            is.reset();
        }            
        super.reset();
    }
    @Override
    public long skip(long n) throws IOException {
        InputStream is = getCurrent();
        if(is != null) {                    
            return is.skip(n);
        }            
        return super.skip(n);                        
    }
}
