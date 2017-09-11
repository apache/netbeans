/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.masterfs.watcher;

import org.netbeans.modules.masterfs.providers.Notifier;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class NotifierKeyRef<KEY> extends WeakReference<FileObject> {
    private final KEY key;
    private final int hash;
    private final Notifier<KEY> outer;

    public NotifierKeyRef(FileObject fo, KEY key, ReferenceQueue<FileObject> queue, final Notifier<KEY> outer) {
        super(fo, queue);
        this.outer = outer;
        this.key = key;
        this.hash = fo.hashCode();
        if (key != null) {
            Watcher.LOG.log(Level.FINE, "Adding watch for {0}", key);
        }
    }

    @Override
    public FileObject get() {
        return super.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        try {
            NotifierKeyRef kr = (NotifierKeyRef) obj;
            FileObject mine = get();
            FileObject theirs = kr.get();
            if (mine == null) {
                return theirs == null;
            } else {
                return mine.equals(theirs);
            }
        } catch (ClassCastException ex) {
            return false;
        }
    }

    final void removeWatch() throws IOException {
        Watcher.LOG.log(Level.FINE, "Removing watch for {0}", key);
        NotifierAccessor.getDefault().removeWatch(outer, key);
    }

    @Override
    public int hashCode() {
        return hash;
    }
    
}
