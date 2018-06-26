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
package org.netbeans.modules.web.beans.navigation;

import java.io.IOException;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

class InjectableTreeNode<T extends Element> extends DefaultMutableTreeNode 
            implements JavaElement 
{
    private static final long serialVersionUID = -6398205566811265151L;
    
    InjectableTreeNode(FileObject fileObject,
        T element, DeclaredType parentType, boolean disabled , 
        CompilationInfo compilationInfo) 
    {
        myFileObject = fileObject;
        myElementHandle = ElementHandle.create(element);
        myElementKind = element.getKind();
        myModifiers = element.getModifiers();
        myCpInfo = compilationInfo.getClasspathInfo();
        isDisabled = disabled;

        setName(element.getSimpleName().toString());
        setIcon(ElementIcons.getElementIcon(element.getKind(), element.getModifiers()));
        setLabel(Utils.format(element, parentType, compilationInfo));
        setFQNLabel(Utils.format(element, parentType, compilationInfo , false, true));
        setToolTip(Utils.format(element, parentType, compilationInfo, true, 
                WebBeansNavigationOptions.isShowFQN()));            
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.JavaElement#isDisabled()
     */
    @Override
    public boolean isDisabled() {
        return isDisabled;
    }

    @Override
    public FileObject getFileObject() {
        return myFileObject;
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return myModifiers;
    }
    
    @Override
    public ElementKind getElementKind() {
        return myElementKind;
    }
    
    protected void setName(String name) {
        myName = name;
    }

    @Override
    public String getLabel() {
        return myLabel;
    }

    @Override
    public String getFQNLabel() {
        return myFQNlabel;
    }

    @Override
    public String getTooltip() {
        return myTooltip;
    }

    @Override
    public Icon getIcon() {
        return myIcon;
    }

    protected void setIcon(Icon icon) {
        myIcon = icon;
    }
    
    protected void setLabel(String label) {
        myLabel = label;
    }
    
    protected void setFQNLabel(String FQNlabel) {
        myFQNlabel = FQNlabel;
    }
    
    protected void setToolTip(String tooltip) {
        myTooltip = tooltip;
    }

    public ElementJavadoc getJavaDoc() {
        if (myJavaDoc == null) {
            if (myFileObject == null) {
                // Probably no source file - so cannot get Javadoc
                return null;
            }
            
            JavaSource javaSource = JavaSource.forFileObject(myFileObject);

            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                            public void run(
                                CompilationController compilationController)
                                throws Exception {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                Element element = myElementHandle.resolve(compilationController);
                                setJavaDoc(ElementJavadoc.create(compilationController, element));
                            }
                        }, true);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        return myJavaDoc;
    }

    protected void setJavaDoc(ElementJavadoc javaDoc) {
        myJavaDoc = javaDoc;
    }

    @Override
    public ElementHandle<T> getElementHandle() {
        return myElementHandle;
    }

    @Override
    public void gotoElement() {
        openElementHandle();
    }
    
    @Override
    public String toString() {
        return (WebBeansNavigationOptions.isShowFQN()? getFQNLabel() : getLabel());
    }

    protected void openElementHandle() {
    	if (myFileObject == null) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(InjectablesModel.class, 
                            "MSG_CouldNotOpenElement", getFQNLabel())); // NOI18N
            return;
        }
    	
        if (myElementHandle == null) {
            return;
        }

        if (!ElementOpen.open(myCpInfo, myElementHandle)) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(InjectablesModel.class, 
                            "MSG_CouldNotOpenElement", getFQNLabel()));// NOI18N
        }
    }
    
    private FileObject myFileObject;
    private ElementHandle<T> myElementHandle;
    private ElementKind myElementKind;
    private Set<Modifier> myModifiers;
    private String myName = "";
    private String myLabel = "";
    private String myFQNlabel = "";
    private String myTooltip ;
    private Icon myIcon ;
    private ElementJavadoc myJavaDoc;
    private final ClasspathInfo myCpInfo;
    private boolean isDisabled;

}
