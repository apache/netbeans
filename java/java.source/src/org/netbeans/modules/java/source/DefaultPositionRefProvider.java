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
     * Notes for compatibility: PositionRef *happens* to implement Position. The
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
