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

package org.netbeans.modules.editor.options;

import java.awt.Image;
import java.awt.Toolkit;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.netbeans.modules.editor.options.AnnotationTypesFolder;
import org.netbeans.editor.AnnotationType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import java.util.Iterator;
import java.beans.IntrospectionException;
import org.openide.util.NbBundle;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.netbeans.editor.AnnotationTypes;
import java.lang.Boolean;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URL;

/** Node representing the Annotation Types in Options window.
 *
 * @author  David Konecny
 * @since 07/2001
 */
public class AnnotationTypesNode extends AbstractNode {

    private static final String HELP_ID = "editing.configuring.annotations"; // !!! NOI18N
    private static final String ICON_BASE = "org/netbeans/modules/editor/resources/annotationtypes"; // NOI18N
    
    
    /** Creates new AnnotationTypesNode */
    public AnnotationTypesNode() {
        super(new AnnotationTypesSubnodes ());
        setName("annotationtypes"); // NOI18N
        setDisplayName(getBundleString("ATN_AnnotationTypesNode_Name")); // NOI18N
        setShortDescription (getBundleString("ATN_AnnotationTypesNode_Description")); // NOI18N
        setIconBase (ICON_BASE);
    }
    
    private String getBundleString(String s) {
        return NbBundle.getMessage(AnnotationTypesNode.class, s);
    }        

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }
    
    protected SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (PropertiesAction.class),
               };
    }

    /** Create properties sheet */
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        
	Sheet.Set ps = sheet.get (Sheet.PROPERTIES);
	if (ps == null) {
	    ps = Sheet.createPropertiesSet ();
	}
        
        ps.put(createProperty(AnnotationTypes.PROP_BACKGROUND_DRAWING, boolean.class)); //NOI18N
        ps.put(createProperty(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA, int.class)); //NOI18N
        ps.put(createProperty(AnnotationTypes.PROP_COMBINE_GLYPHS, boolean.class));    //NOI18N
        ps.put(createProperty(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS, boolean.class));    //NOI18N
        ps.put(createProperty(AnnotationTypes.PROP_SHOW_GLYPH_GUTTER, boolean.class));    //NOI18N
        sheet.put(ps);
        
        return sheet;
    }
    
    /** Create PropertySupport for given property name and class */
    private PropertySupport createProperty(final String name, final Class clazz) {
        return new PropertySupport.ReadWrite(name, clazz,
            getBundleString("PROP_" + name),    //NOI18N
            getBundleString("HINT_" + name)) {  //NOI18N
            public Object getValue() {
                return getProperty(name);
            }
            public void setValue(Object value) {
                setProperty(name, value);
            }
            public boolean supportsDefaultValue() {
                return false;
            }
        };
    }

    /** General setter */
    private void setProperty(String property, Object value) {
        if (property.equals(AnnotationTypes.PROP_BACKGROUND_DRAWING))
            AnnotationTypes.getTypes().setBackgroundDrawing((Boolean)value);
        if (property.equals(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA))
            AnnotationTypes.getTypes().setBackgroundGlyphAlpha(((Integer)value).intValue());
        if (property.equals(AnnotationTypes.PROP_COMBINE_GLYPHS))
            AnnotationTypes.getTypes().setCombineGlyphs((Boolean)value);
        if (property.equals(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS))
            AnnotationTypes.getTypes().setGlyphsOverLineNumbers((Boolean)value);
        if (property.equals(AnnotationTypes.PROP_SHOW_GLYPH_GUTTER))
            AnnotationTypes.getTypes().setShowGlyphGutter((Boolean)value);
    }

    /** General getter*/
    private Object getProperty(String property) {
        if (property.equals(AnnotationTypes.PROP_BACKGROUND_DRAWING))
            return AnnotationTypes.getTypes().isBackgroundDrawing();
        if (property.equals(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA))
            return AnnotationTypes.getTypes().getBackgroundGlyphAlpha();
        if (property.equals(AnnotationTypes.PROP_COMBINE_GLYPHS))
            return AnnotationTypes.getTypes().isCombineGlyphs();
        if (property.equals(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS))
            return AnnotationTypes.getTypes().isGlyphsOverLineNumbers();
        if (property.equals(AnnotationTypes.PROP_SHOW_GLYPH_GUTTER))
            return AnnotationTypes.getTypes().isShowGlyphGutter();
        
        return null;
    }
    
    /** Class representing subnodes of AnnotationType node.*/
    private static class AnnotationTypesSubnodes extends Children.Array {

        /** Listener on add/remove of annotation type. */
        private PropertyChangeListener listener;
        
        public AnnotationTypesSubnodes() {
            super();
            AnnotationTypes.getTypes().addPropertyChangeListener( listener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == AnnotationTypes.PROP_ANNOTATION_TYPES) {
                        AnnotationTypesSubnodes.this.nodes = initCollection();
                        refresh();
                    }
                }
            });
        }
        
        /** Initialize the collection with results of parsing of "Editors/AnnotationTypes" directory */
        protected java.util.Collection initCollection() {
            
            AnnotationTypesFolder folder = AnnotationTypesFolder.getAnnotationTypesFolder();

            Iterator types = AnnotationTypes.getTypes().getAnnotationTypeNames();

            java.util.List list = new java.util.LinkedList();

            for( ; types.hasNext(); ) {
                String name = (String)types.next();
                AnnotationType type = AnnotationTypes.getTypes().getType(name);
                if (type == null || !type.isVisible())
                    continue;
                try {
                    list.add(new AnnotationTypesSubnode(type));
                } catch (IntrospectionException e) {
                    Exceptions.printStackTrace(e);
                    continue;
                }
            }

            return list;
        }
        
        // Cf. #7925, though not quite the same.
        private static final class AnnotationTypesSubnode extends BeanNode {
            private final URL iconURL;

            public AnnotationTypesSubnode(AnnotationType type) throws IntrospectionException {
                super(new AnnotationTypeOptions(type));
                setName(type.getDescription());
                iconURL = type.getGlyph();
            }
            
            public Image getIcon(int type) {
                // Utilities.loadImage does not handle URLs.
                // Toolkit.getImage would work, but U.lI does nicer caching.
                if (iconURL.getProtocol().equals("nbresloc")) { // NOI18N
                    return ImageUtilities.loadImage(iconURL.getPath().substring(1));
                } else {
                    return Toolkit.getDefaultToolkit().getImage(iconURL);
                }
            }
            
            public boolean canDestroy() {
                return false;
            }
            public HelpCtx getHelpCtx() {
                return new HelpCtx(HELP_ID);
            }
            
        }
    }

}
