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

    @Override
    public String getAsText () {
        int i = findIndex (values, getValue ());
        return (String) findObject (descriptions, i);
    }

    @Override
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

    @Override
    public String[] getTags () {
        return descriptions;
    }

    @Override
    public boolean isPaintable () {
        return true;
    }

    @Override
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

    @Override
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
