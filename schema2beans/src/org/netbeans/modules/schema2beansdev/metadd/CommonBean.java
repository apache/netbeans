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
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/**
 * This interface is the intersection of all generated methods.
 * 
 * @Generated
 */

package org.netbeans.modules.schema2beansdev.metadd;

public interface CommonBean {
	public void changePropertyByName(String name, Object value);

	public org.netbeans.modules.schema2beansdev.metadd.CommonBean[] childBeans(boolean recursive);

	public void childBeans(boolean recursive, java.util.List beans);

	public boolean equals(Object o);

	public Object fetchPropertyByName(String name);

	public int hashCode();

	public boolean isVetoable();

	public String nameChild(Object childObj);

	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName);

	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName);

	public String nameSelf();

	public void readNode(org.w3c.dom.Node node);

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes);

	public void setVetoable(boolean value);

	public String toString();

	public void validate() throws org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException;

	public void writeNode(java.io.Writer out) throws java.io.IOException;

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException;

}
