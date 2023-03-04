/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
