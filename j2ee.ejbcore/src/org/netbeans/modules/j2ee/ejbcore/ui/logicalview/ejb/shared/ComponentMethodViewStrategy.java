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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.openide.filesystems.FileObject;

/**
 * This interfaces provides a strategy for working with component methods.
 * 
 * @author  rico
 * @author Chris Webster
 */
public interface ComponentMethodViewStrategy {
    
  /**
   * Get the badge for the method (if any) depending on its presence in the interfaces
   * 
   * @param me method from one of the interfaces in the collection
   * @return Image of the appropriate badge or null 
   */  
  public Image getBadge(MethodModel me);
  
  /**
   * Get the icon for the method. The general implementation of this
   * method determines the icon based on the interface the method is present in
   * along with the signature.
   * 
   * @param me method from one of the collection intefaces
   * @return Image, this method should not return null
   */
  public Image getIcon(MethodModel me);
  
  /**
   * Delete the method from the implementation class
   * 
   * @param me method from one of the interfaces in the collection
   * @param implClass Implementation class where the corresponding method will be deleted
   * @param implClassFO file object conataing implementation class; can be null (e.g. EJB from library)
   */
  public void deleteImplMethod(MethodModel me, String implClass, FileObject implClassFO) throws IOException;
  
  /**
   * Open MethodElement in the implementation class
   * 
   * @param me method from one of the interfaces in the collection
   * @param implClass Implementation class where the corresponding method will be opened
   * @param implClassFO file object conataing implementation class; can be null (e.g. EJB from library)
   * @return The OpenCookie of the corresponding method in the implementation class implClass.
   */
  void openMethod(MethodModel me, String implClass, FileObject implClassFO); 
  
}
