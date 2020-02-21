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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * Coordiates all change notifications.
 * Singleton.
 */
public class Notificator {

    private static final ThreadLocal<Notificator> instance = new ThreadLocal<Notificator>() {

        @Override
        protected Notificator initialValue() {
            return new Notificator();
        }
    };
    private int transactionLevel = 0;
    private ChangeEventImpl currEvent;

    private Notificator() {
    }

    public static Notificator instance() {
        return instance.get();
    }

    private String getCurrThreadString() {
        Thread thread = Thread.currentThread();
        return thread.getName() + ' ' + thread.hashCode();
    }

    public void startTransaction() {
        synchronized (this) {
            transactionLevel++;
            if (TraceFlags.DEBUG) {
                Diagnostic.trace("    > " + transactionLevel + ' ' + getCurrThreadString()); // NOI18N
            }
            resetEvent();
        }
    }

    public void endTransaction() {
        synchronized (this) {
            transactionLevel--;
            if (TraceFlags.DEBUG) {
                Diagnostic.trace("    < " + transactionLevel + ' ' + getCurrThreadString()); // NOI18N
            }
            if (transactionLevel <= 0) {
                flush();
            }
        }
    }

    private ChangeEventImpl getEvent() {
        if (currEvent == null) {
            //synchronized( this ) {
            //if( currEvent == null ) {
            // TODO: think over, whether this does not contain a well-known double-check problem
            ChangeEventImpl ev = new ChangeEventImpl(this);
            currEvent = ev;
        //}
        //}
        }
        return currEvent;
    }

    private void resetEvent() {
        currEvent = null;
    }

    // FIXUP: there should be a notificator per project instead!
    public void reset() {
        synchronized (this) {
            resetEvent();
        }
    }

    private boolean isEventEmpty() {
        return currEvent == null || currEvent.isEmpty();
    }

    public void registerNewFile(CsmFile file) {
        synchronized (this) {
            getEvent().addNewFile(file);
        }
    }

    public void registerRemovedFile(CsmFile file) {
        synchronized (this) {
            getEvent().addRemovedFile(file);
        }
    }

    public void registerChangedFile(CsmFile file) {
        synchronized (this) {
            getEvent().addChangedFile(file);
        }
    }

    public void registerNewDeclaration(CsmOffsetableDeclaration decl) {
        synchronized (this) {
            getEvent().addNewDeclaration(decl);
        }
    }

    public void registerRemovedDeclaration(CsmOffsetableDeclaration decl) {
        synchronized (this) {
            getEvent().addRemovedDeclaration(decl);
        }
    }

    public void registerChangedDeclaration(CsmOffsetableDeclaration oldDecl, CsmOffsetableDeclaration newDecl) {
        synchronized (this) {
            getEvent().addChangedDeclaration(oldDecl, newDecl);
        }
    }

    public void registerNewNamespace(CsmNamespace ns) {
        synchronized (this) {
            getEvent().addNewNamespace(ns);
        }
    }

    public void registerRemovedNamespace(CsmNamespace ns) {
        synchronized (this) {
            getEvent().addRemovedNamespace(ns);
        }
    }
    
    void registerChangedLibraryDependency(CsmProject project) {
        synchronized (this) {
            getEvent().addProjectThatChangedLibs(project);
        }
    }

    /**
     * Generally, we should rely on hashCode() and equals()
     * of the CsmFile && CsmDeclaration.
     *
     * But for now (last day of Deimos project) it's much easier
     * to ensure this here than to write/test/debug hashCode() and equals()
     *
     * TODO: ensure correct hashCode() and equals()
     * in CsmFile && CsmDeclaration, remove IdMaker and related
     */
    private interface IdMaker<T, S> {

        S id(T o);
    }

    public void flush() {

        ChangeEventImpl ev;

        synchronized (this) {
            transactionLevel = 0;
            if (isEventEmpty()) {
                return;
            }
            ev = getEvent();
            resetEvent();
        }

        IdMaker<CsmFile, CharSequence> idFileMaker;

        idFileMaker = new IdMaker<CsmFile, CharSequence>() {

            @Override
            public CharSequence id(CsmFile o) {
                return (o).getAbsolutePath();
            }
        };
        processFiles(idFileMaker, ev.getNewFiles(), ev.getRemovedFiles(), ev.getChangedFiles());

        IdMaker<CsmOffsetableDeclaration, PersistentKey> idDeclMaker = new IdMaker<CsmOffsetableDeclaration, PersistentKey>() {

            @Override
            public PersistentKey id(CsmOffsetableDeclaration o) {
                return PersistentKey.createKey(o);
            }
        };
        processDeclarations(idDeclMaker, ev.getNewDeclarations(), ev.getRemovedDeclarations(), ev.getChangedDeclarations());

        gatherProjects(ev);

        //TODO: thik over, probably it's worth keeping this "!="
        //if( model != null ) {
        ListenersImpl.getImpl().fireModelChanged(ev);
    //}
    }

    private static void gatherProjects(ChangeEventImpl ev) {
        Collection<CsmProject> projects = ev.getChangedProjects();
        Collection/*CsmFile*/[] files = new Collection/*CsmFile*/[]{
            ev.getNewFiles(),
            ev.getChangedFiles(),
            ev.getRemovedFiles()};
        for (int i = 0; i < files.length; i++) {
            for (Iterator iter = files[i].iterator(); iter.hasNext();) {
                projects.add(((CsmFile) iter.next()).getProject());
            }
        }
        Collection/*CsmOffsetableDeclaration*/[] decls = new Collection/*CsmOffsetableDeclaration*/[]{
            ev.getNewDeclarations(),
            ev.getChangedDeclarations().values(),
            ev.getRemovedDeclarations()};
        for (int i = 0; i < decls.length; i++) {
            for (Iterator iter = decls[i].iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o instanceof CsmOffsetableDeclaration) {
                    projects.add(((CsmOffsetableDeclaration) o).getContainingFile().getProject());
                }
            }
        }
    }

    private static void processFiles(IdMaker<CsmFile, CharSequence> idMaker, Collection<CsmFile> added, Collection<CsmFile> removed, Collection<CsmFile> changed) {


        Set<CharSequence> idsAdded = new HashSet<>();
        for (Iterator<CsmFile> iter = added.iterator(); iter.hasNext();) {
            idsAdded.add(idMaker.id(iter.next()));
        }

        Set<CharSequence> idsRemoved = new HashSet<>();
        for (Iterator<CsmFile> iter = removed.iterator(); iter.hasNext();) {
            idsRemoved.add(idMaker.id(iter.next()));
        }

        Set<CsmFile> rightAdded = new HashSet<>();
        Set<CsmFile> rightRemoved = new HashSet<>();

        for (Iterator<CsmFile> iter = removed.iterator(); iter.hasNext();) {
            CsmFile o = iter.next();
            CharSequence id = idMaker.id(o);
            if (idsAdded.contains(id)) {
                changed.add(o);
            } else {
                rightRemoved.add(o);
            }
        }

        for (Iterator<CsmFile> iter = added.iterator(); iter.hasNext();) {
            CsmFile o = iter.next();
            CharSequence id = idMaker.id(o);
            if (!idsRemoved.contains(id)) {
                rightAdded.add(o);
            }
        }

        added.clear();
        added.addAll(rightAdded);

        removed.clear();
        removed.addAll(rightRemoved);
    }

    private static void processDeclarations(IdMaker<CsmOffsetableDeclaration, PersistentKey> idMaker, Collection<CsmOffsetableDeclaration> added,
            Collection<CsmOffsetableDeclaration> removed, Map<CsmOffsetableDeclaration, CsmOffsetableDeclaration> changed) {

        Map<PersistentKey, CsmOffsetableDeclaration> idsAdded = new HashMap<>();
        for (CsmOffsetableDeclaration decl : added) {
            idsAdded.put(idMaker.id(decl), decl);
        }

        Map<PersistentKey, CsmOffsetableDeclaration> idsRemoved = new HashMap<>();
        for (CsmOffsetableDeclaration decl : removed) {
            idsRemoved.put(idMaker.id(decl), decl);
        }

        Set<CsmOffsetableDeclaration> rightAdded = new HashSet<>();
        Set<CsmOffsetableDeclaration> rightRemoved = new HashSet<>();

        for (CsmOffsetableDeclaration decl : removed) {
            Object id = idMaker.id(decl);
            if (idsAdded.containsKey(id)) {
                changed.put(decl, idsAdded.get(id));
            } else {
                rightRemoved.add(decl);
            }
        }

        for (CsmOffsetableDeclaration decl : added) {
            Object id = idMaker.id(decl);
            if (!idsRemoved.containsKey(id)) {
                rightAdded.add(decl);
            }
        }

        added.clear();
        added.addAll(rightAdded);

        removed.clear();
        //removed.addAll(rightRemoved);
        if (rightRemoved.size() > 0) {
            for (CsmOffsetableDeclaration decl : rightRemoved) {
                CharSequence uniqueName = decl.getUniqueName();
                CsmProject project = decl.getContainingFile().getProject();
                CsmOffsetableDeclaration duplicated = (CsmOffsetableDeclaration) project.findDeclaration(uniqueName);
                if (duplicated != null) {
                    changed.put(decl, duplicated);
                } else {
                    removed.add(decl);
                }
            }
        }
    }
}
