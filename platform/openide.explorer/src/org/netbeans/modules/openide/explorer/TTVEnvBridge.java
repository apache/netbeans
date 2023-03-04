/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
