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
/*
 * ResourceConfigHelper.java
 *
 * Created on October 17, 2002, 12:11 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;


/**
 *
 * @author  shirleyc
 */
public class ResourceConfigHelper {

    private ResourceConfigData datas[] = null;
    private int index;
    private boolean forEdit = false;

    /** Creates a new instance of ResourceConfigHelper */
    public ResourceConfigHelper(int size) {
        this(size, 0);
    }

    public ResourceConfigHelper(int size, int index) {
        datas = new ResourceConfigData[size];
        this.index = index;
    }
    
    public ResourceConfigHelper(ResourceConfigData data, int size, int index) {
        this(size, index);
        datas[index] = data;
    }
    
    public ResourceConfigHelper(ResourceConfigData data) {
        this(data, 1, 0);
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public boolean getForEdit() {
        return forEdit;
    }
    
    public ResourceConfigHelper setForEdit(boolean forEdit) {
        this.forEdit = forEdit;
        return this;
    }    
        
    public ResourceConfigData getData() {
        ResourceConfigData data = datas[index];
        if (data == null) {
            data = new ResourceConfigData();
            datas[index] = data;
        }
        return data;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("index is " + index + "\n");  //NOI18N
        for (int i = 0; i < datas.length; i++) {
            if (datas[i] == null)
                str.append("datas[ " + i + " ] is null"); //NOI18N
            else
                str.append("datas[ " + i + " ] is:\n" + datas[i].toString()); //NOI18N
        }
        return str.toString();
    }
           
}
