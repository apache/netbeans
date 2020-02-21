/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
