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

package org.netbeans.modules.openide.explorer;

import java.util.Map;
import java.util.WeakHashMap;


/** A temporary workaround for issue 38132, getBeans returns null on
 * TreeTableView.  A better solution will be used for the TTV rewrite.
 * <P>
 * What it does:
 * TableSheetCell calls TTVEnvBridge.getInstance(this), and calls
 * setCurrentBeans with the node being rendered/edited.
 * Then it puts itself as a client property of the PropertyPanel it is
 * using to render.
 * <P>
 * On PropertyPanel.setProperty(), the PropertyPanel checks itself for this
 * client property, and if it's there, calls findInstance to fetch the
 * TTVEnvBridge instance, calls getCurrentBeans() and puts the result into
 * a private field accessible via a package private method.  It then calls
 * TTVEnvBridge.clear() to remove references to the node.
 * <P>
 * If an editor component embedded in a PropertyPanel (EditablePropertyDisplayer)
 * finds it is dealing with an ExPropertyEditor, it calls 
 * EditablePropertyDisplayer.findBeans(this).  That method first checks if the
 * property model is non-null, and if it is, checks a world of permutations of
 * PropertyModel that have various ways of locating their beans.  If there is
 * no property model, then it checks if its parent is a PropertyPanel.  If it
 * is, it calls getBeans() on that.
 * <P>
 * If a renderer component embedded in a PropertyPanel (RendererPropertyDisplayer)
 * finds it is dealing with an ExPropertyEditor, it calls 
 * EditablePropertyDisplayer.findBeans(this), as described above, from its
 * paintComponent() method, before it calls RendererFactory.getRenderer(getProperty()).
 * Then it sets the field ReusablePropertyEnv.NODE to the value returned by
 * findBeans(), since there is a shared instance of ReusablePropertyEnv which
 * is the PropertyEnv for all properties when they are renderered (for performance
 * reasons).
 * <P>
 * Note: As far as is known, the TaskList module is the only module that has 
 * ever used PropertyEnv.getBeans().  It should be deprecated, along with
 * PropertyEnv, in a future release.
 *
 * @author  Tim Boudreau
 */
public class TTVEnvBridge {
    private static Map<Object, TTVEnvBridge> bridges = new WeakHashMap<Object, TTVEnvBridge>();
    Object[] beans = null;
    /** Creates a new instance of TTVEnvBridge */
    private TTVEnvBridge() {
    }
    
    public static TTVEnvBridge getInstance(Object identifier) {
        TTVEnvBridge result = bridges.get(identifier);
        if (result == null) {
            result = new TTVEnvBridge();
            bridges.put (identifier, result);
        }
        return result;
    }
    
    public static TTVEnvBridge findInstance(Object identifier) {
        return bridges.get(identifier);
    }
    
    public void setCurrentBeans (Object[] o) {
        beans = o;
    }
    
    public void clear() {
        beans = null;
    }
    
    public Object[] getCurrentBeans() {
        if (beans == null) {
            return new Object[0];
        } else {
            return beans;
        }
    }
}
