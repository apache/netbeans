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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextComponentPeer;
import java.awt.peer.TextFieldPeer;
import java.lang.reflect.*;


/**
 *
 * @author Tran Duc Trung
 */

public class FakePeerSupport
{
    private FakePeerSupport() {
    }
    
    public static boolean attachFakePeer(Component comp) {
        if (comp == null || comp.isDisplayable()
              || comp instanceof javax.swing.JComponent
              || comp instanceof javax.swing.RootPaneContainer)
            return false;

        FakePeer peer = null;

        if (comp instanceof Label)
            peer = getFakePeer(LabelPeer.class, new FakeLabelPeer((Label) comp));
        else if (comp instanceof Button)
            peer = getFakePeer(ButtonPeer.class, new FakeButtonPeer((Button) comp));                   
        else if (comp instanceof Panel)
            peer = getFakePeer(new Class[] {ContainerPeer.class, PanelPeer.class}, new FakePanelPeer((Panel) comp));
        else if (comp instanceof TextField)
            peer = getFakePeer(new Class[] {TextFieldPeer.class, TextComponentPeer.class}, new FakeTextFieldPeer((TextField) comp));
        else if (comp instanceof TextArea)
            peer = getFakePeer(new Class[] {TextAreaPeer.class, TextComponentPeer.class}, new FakeTextAreaPeer((TextArea) comp));
        else if (comp instanceof TextComponent)
            peer = getFakePeer(TextComponentPeer.class, new FakeTextComponentPeer((TextComponent) comp));
        else if (comp instanceof Checkbox)
            peer = getFakePeer(CheckboxPeer.class, new FakeCheckboxPeer((Checkbox) comp));
        else if (comp instanceof Choice)
            peer = getFakePeer(ChoicePeer.class, new FakeChoicePeer((Choice) comp));
        else if (comp instanceof List)
            peer = getFakePeer(ListPeer.class, new FakeListPeer((List) comp));
        else if (comp instanceof Scrollbar)
            peer = getFakePeer(ScrollbarPeer.class, new FakeScrollbarPeer((Scrollbar) comp));
        else if (comp instanceof ScrollPane)
            peer = getFakePeer(new Class[] {ContainerPeer.class, ScrollPanePeer.class}, new FakeScrollPanePeer((ScrollPane) comp));
        else if (comp instanceof Canvas)
            peer = getFakePeer(CanvasPeer.class, new FakeCanvasPeer((Canvas) comp));
        else
            return false;

        attachFakePeer(comp, peer);
        return true;
    }
    
    private static FakePeer getFakePeer(Class fakePeerInterfaces, FakeComponentPeer compPeer) {                
        return getFakePeer(new Class[] {fakePeerInterfaces}, compPeer);
    }
    
    private static FakePeer getFakePeer(Class[] fakePeerInterfaces, FakeComponentPeer compPeer) {        
        
        // FakePeer.class and java.awt.peer.LightweightPeer.class interfaces
        // should be implemented for each FakeComponentPeer
        Class[] interfaces = new Class[fakePeerInterfaces.length + 2];
        System.arraycopy(fakePeerInterfaces, 0, interfaces, 0,  fakePeerInterfaces.length);
        interfaces[fakePeerInterfaces.length] = FakePeer.class;
        interfaces[fakePeerInterfaces.length+1] = java.awt.peer.LightweightPeer.class;
        
        Class proxyClass = Proxy.getProxyClass(compPeer.getClass().getClassLoader(), interfaces);        
        FakePeerInvocationHandler handler = new FakePeerInvocationHandler(compPeer); 
        try {
           return (FakePeer) proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { handler });                   
        } catch (Exception e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        return null;
    }

    public static ComponentPeer getPeer(Component comp) {
        ComponentPeer peer = null;
        try {
            Field f = Component.class.getDeclaredField("peer"); // NOI18N
            f.setAccessible(true);
            peer = (ComponentPeer)f.get(comp);
        } catch (IllegalAccessException iaex) {
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, iaex);
        } catch (NoSuchFieldException nsfex) {
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, nsfex);
        }
        return peer;
    }
    
    public static void attachFakePeer(Component comp, ComponentPeer peer) {
        try {
            Field f = Component.class.getDeclaredField("peer"); // NOI18N
            f.setAccessible(true);
            f.set(comp, peer);
        }
        catch (Exception ex) {
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }

    public static void attachFakePeerRecursively(Container container) {
        Component components[] = getComponents(container);
        for (int i=0; i < components.length; i++) {
            Component comp = components[i];
            attachFakePeer(comp);
            if (comp instanceof Container)
                attachFakePeerRecursively((Container) comp);
        }
    }

    static Component[] getComponents(Container container) {
        // hack for the case some "smart" containers delegate getComponents()
        // to some subcontainer (which becomes inaccessible then)
        try {
            Field f = Container.class.getDeclaredField("component"); // NOI18N
            f.setAccessible(true);
            Object value = f.get(container);
            Component[] components;
            if (value instanceof Component[]) { 
                components = (Component[])value;
            } else {
                // The type of the component field changed to List<Component>
                // in JDK 6 update 10 build 23
                java.util.List<Component> list = (java.util.List<Component>)value;
                components = list.toArray(new Component[list.size()]);
            }
            return components;
        }
        catch (Exception ex) {
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return container.getComponents();
    }

    public static ComponentPeer detachFakePeer(Component comp) {
        try {
            Field f = Component.class.getDeclaredField("peer"); // NOI18N
            f.setAccessible(true);
            Object peer = (ComponentPeer) f.get(comp);
            if (peer instanceof FakePeer) {
                f.set(comp, null);
                return (FakePeer) peer;
            }
        }
        catch (Exception ex) {
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
    
    public static Font getDefaultAWTFont() {
        if (defaultFont == null) {
            defaultFont = org.openide.windows.WindowManager.getDefault()
                                               .getMainWindow().getFont();
            if (defaultFont == null)
                defaultFont = new Font("Dialog", Font.PLAIN, 12); // NOI18N
        }
        return defaultFont;
    }

    private static Font defaultFont;
}
