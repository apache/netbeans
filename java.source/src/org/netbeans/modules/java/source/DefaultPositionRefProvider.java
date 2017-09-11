/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source;

import java.io.IOException;
import javax.swing.text.Position;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.EditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
public final class DefaultPositionRefProvider extends PositionRefProvider {

    private final CloneableEditorSupport ces;
    private final EditorSupport es;
    private final Lookup.Provider lkpProv;

    private DefaultPositionRefProvider(CloneableEditorSupport ces, Lookup.Provider lkpProv) {
        this.ces = ces;
        this.es = null;
        this.lkpProv = lkpProv;
    }
    
    private DefaultPositionRefProvider(EditorSupport es, Lookup.Provider lkpProv) {
        this.es = es;
        this.ces = null;
        this.lkpProv = lkpProv;
    }
    
    /**
     * Notes for compatibility: PositonRef *happens* to implement Position. The
     * compatibility bridge typecasts this Position instance into back into PositionRef.
     * Another solution would be to wrap PositionRef into a Position-like type known to the
     * compatible bridge, so the PositionRef could be unwrapped.
     */
    @Override
    public Position createPosition(int position, Position.Bias bias) {
        return  ces != null ? ces.createPositionRef(position, bias) : es.createPositionRef(position, bias);
    }
 
    @ServiceProvider(service = PositionRefProvider.Factory.class)
    public static final class FactoryImpl implements PositionRefProvider.Factory {

        @Override
        public PositionRefProvider create(FileObject fo) throws IOException {
            DataObject dob = DataObject.find(fo);
            Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
            if (obj instanceof CloneableEditorSupport) {
                return new DefaultPositionRefProvider((CloneableEditorSupport) obj, dob);
            }
            obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
            if (obj instanceof CloneableEditorSupport) {
                return new DefaultPositionRefProvider((CloneableEditorSupport) obj, dob);
            }
            @SuppressWarnings("deprecation")
            EditorSupport es = dob.getCookie(EditorSupport.class);
            return es != null ? new DefaultPositionRefProvider(es, dob) : null;
        }
    }
}
