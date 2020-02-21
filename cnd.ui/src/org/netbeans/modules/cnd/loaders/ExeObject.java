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

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Superclass for Elf objects in the Repository.
 *
 */
public class ExeObject extends MultiDataObject {
    //private static final Logger LOG = Logger.getLogger(ExeObject.class.getName());

    /** Serial version number */
    static final long serialVersionUID = 5848558112012002127L;
    private InstanceContent ic;
    private Lookup myLookup;

    public ExeObject(FileObject pf, ExeLoader loader) throws DataObjectExistsException {
	super(pf, loader);
    }

    @Override
    public final synchronized Lookup getLookup() {
        if (myLookup == null) {
            ic = new InstanceContent();
            ic.add(this);
            ic.add(getPrimaryFile());
            if (needBinarySupport()) {
                ic.add(this, CndBinaryExecSupportProvider.staticFactory);
            }
            myLookup = new AbstractLookup(ic);
        }
        return myLookup;
    }

    @Override
    public final <T extends Cookie> T getCookie(Class<T> type) {
        if (!Cookie.class.isAssignableFrom(type)) {
            //Exception exception = new Exception("Class "+Cookie.class.getName()+" does not AssignableFrom "+type.getName()); //NOI18N
            //LOG.log(Level.INFO, exception.getMessage(), exception);
            return null;
        }
        Object lookupResult = getLookup().lookup(type);
        if (lookupResult != null) {
            if (!type.isInstance(lookupResult)) {
                //Exception exception = new Exception("Class "+lookupResult.getClass().getName()+" is not instance of "+type.getName()); //NOI18N
                //LOG.log(Level.INFO, exception.getMessage(), exception);
                return null;
            }
        }
        @SuppressWarnings("unchecked")
        T res = (T) lookupResult;
        return res;
    }

    protected boolean needBinarySupport() {
        return false;
    }
    
    @Override
    protected Node createNodeDelegate() {
	return new ExeNode(this);
    }    
  
    @Override
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
	// If you add context help, change to:
	// return new HelpCtx(ExeObject.class);
    }
}
