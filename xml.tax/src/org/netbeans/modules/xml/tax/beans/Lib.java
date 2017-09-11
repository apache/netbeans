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
package org.netbeans.modules.xml.tax.beans;

import java.beans.*;
import java.awt.Component;

import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.tax.*;
import javax.swing.JComponent;

import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public class Lib {

    private static String CREATE_ATTRIBUTE_NAME  = Util.THIS.getString ("TEXT_new_attribute_name");
    private static String CREATE_ATTRIBUTE_VALUE = Util.THIS.getString ("TEXT_new_attribute_value");
    


    /** Returns the customizer component for <CODE>object</CODE>.
     *
     * @param <CODE>object</CODE> bean to get its customizer
     * @return the component or <CODE>null</CODE> if there is no customizer
     */
    public static Component getCustomizer (Object object) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: object = " + object); // NOI18N

        if (object == null)
            return null;
        
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo (object.getClass());
        } catch (IntrospectionException e) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: exception = " + e); // NOI18N

            return null;
        }
        Class clazz = beanInfo.getBeanDescriptor().getCustomizerClass();
        if (clazz == null) {
            return null;
        }

        Object o;
        try {
            o = clazz.newInstance ();
        } catch (InstantiationException e) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: exception = " + e); // NOI18N

            return null;
        } catch (IllegalAccessException e) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: exception = " + e); // NOI18N

            return null;
        }

        if (!!! (o instanceof Customizer) ) {
            // no customizer => no fun
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: is NOT instanceof Customizer: " + o); // NOI18N

            return null;
        }

        Customizer cust = ((java.beans.Customizer)o);

        // looking for the component
        Component comp = null;
        if (o instanceof Component) {
            comp = (Component)o;
        } else {
            // no component provided
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: is NOT instanceof Component: " + o); // NOI18N

            return null;
        }

        cust.setObject (object);

        return comp;
    }

    /**
     */
    public static Component getCustomizer (Class classClass, Object property, String propertyName) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo (classClass);
        } catch (IntrospectionException e) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: exception = " + e); // NOI18N

            return null;
        }
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: beaninfo = " + beanInfo); // NOI18N

	PropertyDescriptor[] propDescrs = beanInfo.getPropertyDescriptors();
	PropertyDescriptor propertyDescriptor = null;
	for ( int i = 0; i < propDescrs.length; i++ ) {
	    if ( propertyName.equals (propDescrs[i].getName()) ) {
		propertyDescriptor = propDescrs[i];
		break;
	    }
	}
	if ( propertyDescriptor == null ) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: have NOT property: " + propertyName); // NOI18N

	    return null;
	}
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: propertyDescriptor: " + propertyDescriptor); // NOI18N

        Class clazz = propertyDescriptor.getPropertyEditorClass();

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: propertyEditorClass: " + clazz); // NOI18N

        if (clazz == null) {
            return null;
        }
        Object peo;
        try {
            peo = clazz.newInstance ();
        } catch (InstantiationException e) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: exception = " + e); // NOI18N

            return null;
        } catch (IllegalAccessException e) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: exception = " + e); // NOI18N

            return null;
        }
	
        if (!!! (peo instanceof PropertyEditor) ) {
            // no customizer => no fun
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: is NOT instanceof PropertyEditor: " + peo); // NOI18N

            return null;
        }

        PropertyEditor editor = ((PropertyEditor)peo);
	editor.setValue (property);
	Component comp = editor.getCustomEditor();
	if ( comp == null ) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: have NOT customizer: " + editor); // NOI18N

	    return null;
	}
	if (!!! (comp instanceof Customizer) ) {
	    // no customizer => no fun
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Lib::getCustomizer: is NOT instanceof Customizer: " + comp); // NOI18N

            return null;
        }
        Customizer cust = ((Customizer)comp);

//          cust.setObject (property); // done by editor.setValue (property);

        return comp;
    }

    /**
     */
    public static boolean confirmAction (String message) {
	NotifyDescriptor nd = new NotifyDescriptor.Confirmation (message, NotifyDescriptor.YES_NO_OPTION);

	Object option = DialogDisplayer.getDefault().notify (nd);

	return ( option == NotifyDescriptor.YES_OPTION );
    }


    /**
     * Create Attribute dialo gin edit mode.
     */
    public static TreeAttribute createAttributeDialog () {
        return createAttributeDialog(false);
    }
    
    /**
     * @param mone if true new mode is used instead of edit one
     */
    public static TreeAttribute createAttributeDialog (boolean mode) {
        try {
            TreeAttribute attr = new TreeAttribute (CREATE_ATTRIBUTE_NAME, CREATE_ATTRIBUTE_VALUE);
            Component customizer = getCustomizer (attr);

            if ( customizer == null ) {
                return null;
            }

	    return (TreeAttribute)customNode (attr, customizer, Util.THIS.getString ("TITLE_new_attribute"), mode);
	} catch (TreeException exc) {
	    TAXUtil.notifyTreeException (exc);
            return null;
        }
    }

    
    /**
     */
    private static TreeNode customNode (TreeNode treeNode, Component panel, String title, boolean mode) {
	DialogDescriptor dd = new DialogDescriptor
	    (panel, title, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
	     DialogDescriptor.BOTTOM_ALIGN, null, null);

        //??? Warning same code is also in tree.nodes.NodeFactory
        if (panel instanceof JComponent) {
            // set hints to the customizer component
            if (mode) {
                ((JComponent)panel).putClientProperty("xml-edit-mode", "new");  // NOI18N
            } else {
                ((JComponent)panel).putClientProperty("xml-edit-mode", "edit"); // NOI18N
            }
        }
        
	DialogDisplayer.getDefault ().createDialog (dd).setVisible(true);

	if (dd.getValue() != DialogDescriptor.OK_OPTION) {
	    return null;
        }
	return treeNode;
    }

}
