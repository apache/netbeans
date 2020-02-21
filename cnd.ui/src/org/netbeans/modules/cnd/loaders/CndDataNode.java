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

package org.netbeans.modules.cnd.loaders;

import java.util.ResourceBundle;
import javax.swing.Action;

import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *  A base class for CND binary nodes. The functionality from
 *  this base class is the renaming of the PROP_name property to show an extension.
 */
public class CndDataNode extends DataNode {

    /** The name property */
    //private static final String PROP_NAME = "name"; // NOI18N

    /** Cache the bundle */
    private static final ResourceBundle bundle = NbBundle.getBundle(CndDataNode.class);

    /** Primary File */
    private final FileObject primary;


    /** Constructor for this class */
    public CndDataNode(DataObject obj, Children ch, Lookup lookup) {
	super(obj, ch, lookup);
	primary = getDataObject().getPrimaryFile();
    }

    /**
     *  Overrides default action from DataNode.
     *  Instantiate a template, if isTemplate() returns true.
     *  Opens otherwise.
     */
    @Override
    public Action getPreferredAction() {
	Action result = super.getPreferredAction();
	return result == null ? SystemAction.get(OpenAction.class) : result;
    }

    /** Getter for bundle strings */
    protected static String getString(String prop) {
	return bundle.getString(prop);
    }

    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("Welcome_cpp_home"); // NOI18N
    }
    
}
