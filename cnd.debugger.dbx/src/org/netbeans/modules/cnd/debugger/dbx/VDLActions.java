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


package org.netbeans.modules.cnd.debugger.dbx;

/**
 * Interface of actions that the VDL parser will call back.
 * Watches and Variables wil prebably customize the actions to their own
 * taste.
 */
interface VDLActions {

    /**
     * Set expandability of  Variable
     */
    public void setLeaf(boolean l);
    
    /**
     * Set type of  Variable
     */
    public void setType(String type, String atype);

    /**
     * Indicate current var is a Java var
     */
    public void setJava(boolean e);
    
    /**
     * Indicate current value of var is modified
     */
    public void setDelta(boolean e);

    /**
     * Declare the begining of a new smplval
     */
    public void newSmplval(String name, String deref_name,
			   String type, String atype, boolean stat,
			   String value, String set_str, String deref_action,
			   String hint, boolean delta);

    /**
     * Declare the beginning of a new Aggregate
     */
    public void startAggregate(String name, String deref_name,
			       String type, String atype, boolean stat,
			       boolean delta, boolean isopen);

    /**
     * Declare the beginning of a new Aggregate
     */
    public void endAggregate();
};
