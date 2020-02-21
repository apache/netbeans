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

package  org.netbeans.modules.cnd.makefile.wizard;

import java.awt.Component;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class MakefileWizardDescriptorPanel implements WizardDescriptor.Panel {

    /** Serial version number */
    static final long serialVersionUID = -7154324322016837684L;

    private Vector<ChangeListener> listvec;

    private MakefileWizardPanel panel = null;
    private String helpString = "Construct_make_create"; // NOI18N

    MakefileWizardDescriptorPanel(MakefileWizardPanel panel, String helpString) {
        this.panel = panel;
        this.helpString = helpString;
    }


    /** Get the component for this panel */
    public Component getComponent() {
        return panel;
    }


    /**
     *  Default help for those panels which do not currently have a help topic.
     */
    public HelpCtx getHelp() {
        return new HelpCtx(helpString);
    }


    /**
     *  The default validation method. Most panels don't do validation so don't
     *  need to override this.
     */
    public boolean isValid() {
        return panel.isPanelValid();
    }

    public void addChangeListener(ChangeListener listener) {
        if (listvec == null) {
            listvec = new Vector<ChangeListener>(1);
        }
        listvec.add(listener);
    }
  

    public void removeChangeListener(ChangeListener listener) {
        if (listvec != null) {
            listvec.remove(listener);
        }
    }


    public void readSettings(Object settings) {
    }


    public void storeSettings(Object settings) {
    }    

    public void putClientProperty(Object key, Object value) {
        panel.putClientProperty(key, value);
    }
}
