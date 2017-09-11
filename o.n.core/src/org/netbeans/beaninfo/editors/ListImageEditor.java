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

package org.netbeans.beaninfo.editors;

import java.awt.Image;
import java.beans.PropertyEditorSupport;
import java.beans.FeatureDescriptor;
import org.netbeans.core.UIExceptions;

import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/** Editor for property of enumerated integers, each integer should
 * have associated image displayed as a property value. It's possible
 * to associate descriptions for each value which is then shown in combobox
 * when property is edited.
 *
 * @author  Vitezslav Stejskal
 */
public class ListImageEditor extends PropertyEditorSupport implements ExPropertyEditor {

    public static final String PROP_IMAGES = "images"; //NOI18N
    public static final String PROP_VALUES = "values"; //NOI18N
    public static final String PROP_DESCRIPTIONS = "descriptions"; //NOI18N
    
    private boolean canWrite = true;

    private Image [] images = null;
    private Integer [] values = null;
    private String [] descriptions = null;

    /** Creates new ListEditor */
    public ListImageEditor () {
        super ();
    }

    public void attachEnv (PropertyEnv env) {
        FeatureDescriptor d = env.getFeatureDescriptor ();
        if (d instanceof Node.Property) {
            canWrite = ((Node.Property)d).canWrite ();
        }
        
        Object o;
        Image imgs [] = null;
        Integer vals [] = null;
        String descs [] = null;
        
        o = d.getValue (PROP_IMAGES);
        if (o instanceof Image []) {
            imgs = (Image [])o;
        }
        o = d.getValue (PROP_VALUES);
        if (o instanceof Integer []) {
            vals = (Integer [])o;
        }
        o = d.getValue (PROP_DESCRIPTIONS);
        if (o instanceof String []) {
            descs = (String [])o;
        }
        
        if (imgs != null && vals != null) {
            int length = length = imgs.length;

            if(vals.length < length)  {
                length = vals.length;
            }

            if (descs != null && descs.length < length) {
                length = descs.length;
            }

            images = new Image [length];
            values = new Integer [length];
            descriptions = new String [length];

            for (int i = 0; i < length; i++) {
                images [i] = imgs [i];
                values [i] = vals [i];
                descriptions [i] = descs == null ? vals [i].toString () : descs [i];
            }
        }
    }
    
    public boolean isEditable () {
        return canWrite;
    }
    
    public String getAsText () {
        int i = findIndex (values, getValue ());
        return (String) findObject (descriptions, i);
    }
    
    public void setAsText (String str) throws java.lang.IllegalArgumentException {
        int i = findIndex (descriptions, str);
        if (i == -1) {
            IllegalArgumentException iae = new IllegalArgumentException (
                "negative: " + str); //NOI18N
            String msg = NbBundle.getMessage(ListImageEditor.class, 
                "CTL_NegativeSize"); //NOI18N
            UIExceptions.annotateUser(iae, iae.getMessage(), msg, null,
                                     new java.util.Date());
            throw iae;
        }
        setValue (findObject (values, i));
    }
    
    public String[] getTags () {
        return descriptions;
    }

    public boolean isPaintable () {
        return true;
    }
    
    public void paintValue (java.awt.Graphics g, java.awt.Rectangle rectangle) {
        Image img = (Image) findObject (images, findIndex (values, getValue ()));
    
        if (img != null) {
            g.drawImage (img,
                rectangle.x + (rectangle.width - img.getWidth (null))/ 2,
                rectangle.y + (rectangle.height - img.getHeight (null))/ 2, 
                img.getWidth (null),
                img.getHeight (null),
                null);
        }
    }
    
    public String getJavaInitializationString () {
        return "new Integer(" + getValue () + ")"; // NOI18N
    }
    
    private Object findObject (Object [] objs, int i) {
        if (objs == null || i < 0 || i >= objs.length)
            return null;
        
        return objs[i];
    }
    
    private int findIndex (Object [] objs, Object obj) {
        if (objs != null) {
            for ( int i = 0; i < objs.length; i++) {
                if (objs[i].equals (obj))
                    return i;
            }
        }
        return -1;
    }
}
