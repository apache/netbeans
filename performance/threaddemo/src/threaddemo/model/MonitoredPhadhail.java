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

package threaddemo.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import threaddemo.locking.RWLock;
import threaddemo.locking.Locks;

/**
 * A phadhail in which all model methods are locked with a simple monitor.
 * In this variant, the impl acquires locks automatically, though another
 * style would be to require the client to do this.
 * @author Jesse Glick
 */
final class MonitoredPhadhail extends AbstractPhadhail {

    private static final class LOCK {}
    private static final Object LOCK = new LOCK();
    private static final RWLock MLOCK = Locks.monitor(LOCK);
    
    private static final AbstractPhadhail.Factory FACTORY = new AbstractPhadhail.Factory() {
        public AbstractPhadhail create(File f) {
            return new MonitoredPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private MonitoredPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public List<Phadhail> getChildren() {
        synchronized (LOCK) {
            return super.getChildren();
        }
    }
    
    public String getName() {
        synchronized (LOCK) {
            return super.getName();
        }
    }
    
    public String getPath() {
        synchronized (LOCK) {
            return super.getPath();
        }
    }
    
    public boolean hasChildren() {
        synchronized (LOCK) {
            return super.hasChildren();
        }
    }
    
    public void rename(String nue) throws IOException {
        synchronized (LOCK) {
            super.rename(nue);
        }
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        synchronized (LOCK) {
            return super.createContainerPhadhail(name);
        }
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        synchronized (LOCK) {
            return super.createLeafPhadhail(name);
        }
    }
    
    public void delete() throws IOException {
        synchronized (LOCK) {
            super.delete();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        synchronized (LOCK) {
            return super.getInputStream();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        synchronized (LOCK) {
            return super.getOutputStream();
        }
    }
    
    public RWLock lock() {
        return MLOCK;
    }
    
}
