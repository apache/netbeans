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

package org.netbeans.modules.cnd.classview.model;

import java.awt.Image;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import  org.netbeans.modules.cnd.api.model.*;
import static org.netbeans.modules.cnd.classview.model.BaseNode.RP;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;

/**
 */
public class EnumNode extends ClassifierNode {
    private CharSequence name;
    private CharSequence qname;
    private static Image enumImage = null;

    public EnumNode(CsmEnum enumeration,Children.Array key) {
        super(enumeration,key);
        init(enumeration);
    }
    
    private void init(CsmEnum enumeration){
        final CharSequence old = name;
        name = enumeration.getName();
        final CharSequence oldQ = qname;
        qname = enumeration.getQualifiedName();
        if( enumImage == null ) {
            enumImage = CsmImageLoader.getImage(enumeration);
        }
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                if ((old == null) || !old.equals(name)) {
                    fireNameChange(old == null ? null : old.toString(),
                            name == null ? null : name.toString());
                    fireDisplayNameChange(old == null ? null : old.toString(),
                            name == null ? null : name.toString());
                }
                if ((oldQ == null) || !oldQ.equals(qname)) {
                    fireShortDescriptionChange(oldQ == null ? null : oldQ.toString(),
                            qname == null ? null : qname.toString());
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public String getDisplayName() {
        return name.toString();
    }

    @Override
    public String getShortDescription() {
        return qname.toString();
    }

    @Override
    public Image getIcon(int param) {
        return enumImage;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmEnum){
            CsmEnum cls = (CsmEnum)o;
            setObject(cls);
            init(cls);
        } else if (o != null) {
            System.err.println("Expected CsmEnum. Actually event contains "+o.toString());
        }
    }
}
