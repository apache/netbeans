/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.Collection;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Simple class completion: just insert the (right now fully qualified) classname
 * 
 * @author sdedic
 */
public class SimpleClassItem extends AbstractCompletionItem {
    private static final Logger LOG = Logger.getLogger(SimpleClassItem.class.getName()); // NOI18N
    
    private static final String ICON_CLASS = "org/netbeans/modules/javafx2/editor/resources/class.png"; // NOI18N
    
    private String  className;
    private  String  fullClassName;
    private String  leftText;
    private boolean deprecated;
    private int     priority;
    private static ImageIcon ICON;

    public SimpleClassItem(CompletionContext ctx, String text) {
        super(ctx, text);
    }

    void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }
    
    void setClassName(String n) {
        this.className = n;
    }
    
    void setFullClassName(String n) {
        fullClassName = n;
    }

    public String getClassName() {
        return className;
    }

    public String getFullClassName() {
        return fullClassName;
    }
    
    @Override
    public int getSortPriority() {
        return priority;
    }

    @Override
    public CharSequence getSortText() {
        return className;
    }

    @Override
    protected String getLeftHtmlText() {
        if (leftText != null) {
            return leftText;
        }
        String s;
        
        if (deprecated) {
            s = NbBundle.getMessage(SimpleClassItem.class, "FMT_Deprecated", className);
        } else {
            s = className;
        }
        
        s = NbBundle.getMessage(SimpleClassItem.class, 
                "FMT_AddPackage", s, 
                fullClassName.substring(0, fullClassName.length() - className.length() - 1));
        
        return this.leftText = s;
    }

    @Override
    protected ImageIcon getIcon() {
        if (ICON == null) {
            ICON = ImageUtilities.loadImageIcon(ICON_CLASS, false);
        }
        return ICON;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return className;
    }

    @Override
    protected String getSubstituteText() {
        // the opening < is a part of the replacement area
        return "<" + super.getSubstituteText() + (ctx.isReplaceExisting() ? "" : " "); // NOI18N
    }

    @MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=ClassItemFactory.class)
    public static class ItemFactory implements ClassItemFactory {

        @Override
        public CompletionItem convert(TypeElement elem, CompletionContext ctx, int priorityHint) {
            // ignore Strings, they are written as char content
            if (elem.getQualifiedName().contentEquals("java.lang.String")) { // NOI18N
                return null;
            }
            boolean ok = false;
            Collection<? extends ExecutableElement> execs = ElementFilter.constructorsIn(elem.getEnclosedElements());
            for (ExecutableElement e : execs) {
                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                    // ignore non-public ctors
                    continue;
                }
                if (e.getModifiers().contains(Modifier.ABSTRACT)) {
                    // ignore non-public ctors
                    continue;
                }
                if (e.getParameters().isEmpty()) {
                    ok = true;
                }
            }
            
            // non-public, no-arg ctor -> provide an item
            String fqn = elem.getQualifiedName().toString();

            if (!ok) {
                // last chance - try to find a Builder 
                FxBean bean = ctx.getBeanInfo(fqn);
                if (bean != null && !bean.isFxInstance() 
                        // not entirely correct, since Builder can create a non-abstract subclass,
                        // but eliminates abominations like Node and Parent. Next step will be a 
                        // blacklist for classes.
                        && !elem.getModifiers().contains(Modifier.ABSTRACT)
                        && bean.getBuilder() != null) {
                    ok = true;
                }
            }
            
            if (!ok) {
                return null;
            }

            String sn = ctx.getSimpleClassName(fqn);
            return setup(new SimpleClassItem(ctx, 
                    sn == null ? fqn : sn),
                    elem, ctx, priorityHint);
        }
    }

    static SimpleClassItem setup(
            SimpleClassItem item, TypeElement elem, CompletionContext ctx, int priority) {
        item.setFullClassName(elem.getQualifiedName().toString());
        item.setClassName(elem.getSimpleName().toString());
        item.setDeprecated(ctx.isBlackListed(elem));
        item.setPriority(priority);
        
        return item;
    }
    
    public String toString() {
        return "simple-class[" + getFullClassName() + "]";
    }
}
