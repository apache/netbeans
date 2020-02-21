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
package org.netbeans.modules.cnd.navigation.docview;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@TopComponent.Description(preferredID = DocViewTopComponent.preferredID, persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false, position=1250)
@Messages({"CTL_DocViewTopComponent=C/C++ Documentation","HINT_DocViewTopComponent=C/C++ documentation window shows a documentation of element under the caret."})
public final class DocViewTopComponent extends TopComponent {
    private static transient DocViewTopComponent instance;
    private final transient DocViewPanel documentationPane;
    private final transient AtomicBoolean activated = new AtomicBoolean(false);
    static final String preferredID = "DocViewTopComponent"; //NOI18N

    private DocViewTopComponent() {
        initComponents();
        setName(Bundle.CTL_DocViewTopComponent());
        setToolTipText(Bundle.HINT_DocViewTopComponent());
        documentationPane = new DocViewPanel();
        add( documentationPane, BorderLayout.CENTER );
    }
    
    void setDoc( CharSequence doc ){    
        documentationPane.setData( doc );
    }
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized DocViewTopComponent getDefault() {
        if (instance == null) {
            instance = new DocViewTopComponent();
        }
        return instance;
    }
    
    public static synchronized DocViewTopComponent getInstance() {
        return instance;
    }
    
    /**
     * Obtain the JavadocTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DocViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(DocViewTopComponent.preferredID);
        if (win == null) {
            return getDefault();
        }
        if (win instanceof DocViewTopComponent) {
            if (instance == null) {
                instance = (DocViewTopComponent) win;
            }
            return (DocViewTopComponent)win;
        }
        return getDefault();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("CppDocumentation"); //NOI18N
    }
    
    /** replaces this in object stream */
    public @Override Object writeReplace() {
        return new ResolvableHelper();
    }

    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return DocViewTopComponent.getDefault();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        activated.set(true);
    }

    @Override
    public void componentClosed() {
        activated.set(false);
    }
    
    public boolean isActivated() {
        return activated.get();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
}
