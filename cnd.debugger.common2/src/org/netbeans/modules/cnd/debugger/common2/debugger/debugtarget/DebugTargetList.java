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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget;

import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.AbstractRecordList;
import org.openide.ErrorManager;

public class DebugTargetList extends AbstractRecordList<DebugTarget> {

    private static DebugTargetList instance = null;

    /*package*/ static int debuglistMaxSize = 100; // FIXUP should be 500?

    /*package*/ static final String moduleFolderName = "DbxGui"; // NOI18N
    /*package*/ static final String folderName = "DebugTargets"; // NOI18N
    /*package*/ static final String filename = "debugtargets";           // NOI18N
    private static final UserdirFile userdirFile =
        new UserdirFile(moduleFolderName, folderName, filename);

    public DebugTargetList(int max) {
	super(max);
    }

    /**
     * Cloning constructor.
     */
    protected DebugTargetList(DebugTargetList that) {
	super(debuglistMaxSize, that);
    }

    public static DebugTargetList getInstance() {
	if (instance == null) {
	    try {
		instance = new DebugTargetList(debuglistMaxSize);
		instance.restore(userdirFile);
	    }
	    catch (Exception e) {
		System.out.println("DebugTargetList - getInstance - e " + e); //FIXUP //NOI18N
		System.out.println("Cannot restore debuglist ..."); //FIXUP //NOI18N
	    }
	}
	return instance;
    }

    public static void saveList() {
	if (instance == null) {
	    // No-one requested it so no need to save
	    return;
	}
	instance.save(userdirFile);
    }

    public void save(UserdirFile userdirFile) {
        DebugTargetsXMLWriter xw =
	    new DebugTargetsXMLWriter(userdirFile, this);
        try {
            xw.write();
//            clearDirty();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }

    public void restore(UserdirFile userdirFile) {
        DebugTargetsXMLReader xr = new DebugTargetsXMLReader(userdirFile, this);
        try {
            xr.read();
        } catch (Exception x) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, x);
        }
    }

    // implement AbstractRecordList
    @Override
    public DebugTargetList cloneList() {
	return new DebugTargetList(this);
    }
}
