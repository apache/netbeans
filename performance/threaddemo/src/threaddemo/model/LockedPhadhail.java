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
import threaddemo.locking.PrivilegedLock;

/**
 * A phadhail in which all model methods are locked with a plain lock.
 * In this variant, the impl acquires locks automatically, though another
 * style would be to require the client to do this.
 * @author Jesse Glick
 */
final class LockedPhadhail extends AbstractPhadhail {

    private static final PrivilegedLock PLOCK = new PrivilegedLock();
    static {
        Locks.readWrite(PLOCK);
    }
    
    private static final AbstractPhadhail.Factory FACTORY = new AbstractPhadhail.Factory() {
        public AbstractPhadhail create(File f) {
            return new LockedPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private LockedPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public List<Phadhail> getChildren() {
        PLOCK.enterRead();
        try {
            return super.getChildren();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public String getName() {
        PLOCK.enterRead();
        try {
            return super.getName();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public String getPath() {
        PLOCK.enterRead();
        try {
            return super.getPath();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public boolean hasChildren() {
        PLOCK.enterRead();
        try {
            return super.hasChildren();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public void rename(String nue) throws IOException {
        PLOCK.enterWrite();
        try {
            super.rename(nue);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        PLOCK.enterWrite();
        try {
            return super.createContainerPhadhail(name);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        PLOCK.enterWrite();
        try {
            return super.createLeafPhadhail(name);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public void delete() throws IOException {
        PLOCK.enterWrite();
        try {
            super.delete();
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        PLOCK.enterRead();
        try {
            return super.getInputStream();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comment in AbstractPhadhail re. use of read access.
        PLOCK.enterRead();
        try {
            return super.getOutputStream();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public RWLock lock() {
        return PLOCK.getLock();
    }
    
}
