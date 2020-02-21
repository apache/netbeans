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

package org.netbeans.modules.cnd.completion.csm;

import org.netbeans.modules.cnd.completion.csm.CsmContext.CsmContextEntry;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 * context - ordered collection of scope entries passed till language context
 */
public class CsmContext {
    private final CsmFile file;
    // offset for which the context is looking for or last off
    private final int offset;

    // path of context as ordered list of context entries
    private ArrayList<CsmContextEntry> context;
    
    // possible not null
    // an object who's part is csmLastObject, but it is not a scope
    private CsmObject csmLastOwner;

    // possible not null
    // when context was found for exact inner object under offset
    // csmLastObject is subelement the last context entry's scope
    private CsmObject   csmLastObject;

    /** Creates a new instance of CsmContext */
    public CsmContext(CsmFile file, int offset) {
        this.file = file;
        this.offset = offset;
        context = new ArrayList<CsmContextEntry>();
    }

    /** Copy constructor */
    public CsmContext(CsmContext other) {
        this.file = other.file;
        this.offset = other.offset;
        this.context = new ArrayList<CsmContextEntry>(other.context);
        this.csmLastObject = other.csmLastObject;
    }

    public CsmContextEntry get(int index) {
        return context.get(index);
    }

    public CsmContextEntry create(CsmScope scope) {
        return new CsmContextEntry(scope);
    }

    public CsmContextEntry create(CsmScope scope, int offset) {
        return new CsmContextEntry(scope, offset);
    }

    protected void add(CsmContextEntry entry) {
        context.add(entry);
    }

    public void add(CsmScope scope) {
        add(create(scope));
    }

    public void add(CsmScope scope, int offset) {
        add(create(scope, offset));
    }

    public void remove(CsmContextEntry entry) {
        context.remove(entry);
    }

    public CsmContextEntry getLastEntry() {
        if (isEmpty()) {
            return null;
        } else {
            return get(size() - 1);
        }
    }

    public CsmScope getLastScope() {
        if (getLastEntry() != null) {
            return getLastEntry().getScope();
        } else {
            return null;
        }
    }
    
    public CsmObject getLastOwner() {
        return csmLastOwner;
    }

    public void setLastOwner(CsmObject obj) {
        this.csmLastOwner = obj;
    }

    public CsmObject getLastObject() {
        return csmLastObject;
    }

    public void setLastObject(CsmObject obj) {
        this.csmLastObject = obj;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return context.size();
    }

    public Iterator<CsmContextEntry> iterator() {
        return context.iterator();
    }

    public ListIterator<CsmContextEntry> reverseIterator() {
        return context.listIterator(context.size());
    }

    public int getOffset() {
        return this.offset;
    }

    public boolean isCpp() {
        switch (file.getFileType()) {
            case SOURCE_C_FILE:
            case SOURCE_FORTRAN_FILE:
                return false;
        }
        return true;
    }

    CsmFile getFile() {
        return this.file;
    }

    /**
     * Returns a string representation of the object.
     * @return  a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\nlast element is ").append(csmLastObject); // NOI18N
        buf.append("\ncontext for offset ").append(offset); //NOI18N
        if (isEmpty()) {
            buf.append(" empty"); //NOI18N
        } else {
            buf.append(" with ").append(size()).append(" elements:\n"); //NOI18N
            for (Iterator<CsmContextEntry> it = context.iterator(); it.hasNext();) {
                CsmContextEntry elem = it.next();
                buf.append(elem);
                buf.append("\n"); //NOI18N
            }
        }
        return buf.toString();
    }

    // help structure to store one context object and offset where was jump in
    // inner scope
    public static class CsmContextEntry {
        // scope element
        private CsmScope    scope;

        // offset in scope to stop processing scopeElements
        private final int         offset;

        public static final int WHOLE_SCOPE = -1;

        public CsmContextEntry(CsmScope scope) {
            this(scope, WHOLE_SCOPE);
        }

        public CsmContextEntry(CsmScope scope, int offset) {
            this.scope = scope;
            this.offset = offset;
        }

        public CsmScope getScope() {
            return scope;
        }

        public int getOffset() {
            return offset;
        }

        public boolean isWholeScope() {
            return getOffset() == WHOLE_SCOPE;
        }

        /**
         * Returns a string representation of the object.
         * @return  a string representation of the object.
         */
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("["); //NOI18N
            if (isWholeScope()) {
                buf.append("whole scope"); //NOI18N
            } else {
                buf.append("jump in ").append(getOffset()); //NOI18N
            }
            CsmOffsetable offs = (CsmKindUtilities.isOffsetable(scope)) ? (CsmOffsetable)scope : null;
            if (offs != null) {
                // add range of scope
                buf.append(" ("); //NOI18N
                // start as line:col,offset
                CsmOffsetable.Position pos=offs.getStartPosition();
                buf.append(pos.getLine()).append(":").append(pos.getColumn()).append(",").append(pos.getOffset()); //NOI18N
                buf.append(";"); //NOI18N
                // end as line:col,offset
                pos=offs.getEndPosition();
                buf.append(pos.getLine()).append(":").append(pos.getColumn()).append(",").append(pos.getOffset()); //NOI18N
                buf.append(")"); //NOI18N
            }
            // add name
            buf.append(CsmUtilities.getCsmName(scope));
            // add scope info
            buf.append(" scope - "); //NOI18N
            if (CsmKindUtilities.isScope(scope)) {
                buf.append(" [scope object] "); //NOI18N
            }
            if (CsmKindUtilities.isScopeElement(scope)) {
                buf.append(" [scope element] "); //NOI18N
            }
            buf.append("]"); //NOI18N
            return buf.toString();
        }

    }
}
