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
package org.netbeans.modules.xml.wsdl.model.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Import;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.10.22
 */
public class WSDLUtilities {

  private WSDLUtilities() {}

  public static void visitRecursively(WSDLModel model, WSDLModelVisitor visitor) {
    visitRecursively(model, visitor, new ArrayList<WSDLModel>());
  }

  private static void visitRecursively(WSDLModel model, WSDLModelVisitor visitor, List<WSDLModel> visited) {
    if (model == null) {
      return;
    }
    if (visited.contains(model)) {
      return;
    }
    visited.add(model);
    visitor.visit(model);

    Definitions definitions = model.getDefinitions();

    if (definitions == null) {
      return;
    }
    Collection<Import> imports = definitions.getImports();

    if (imports == null) {
      return;
    }
    for (Import _import : imports) {
      try {
        visitRecursively(_import.getImportedWSDLModel(), visitor, visited);
      }
      catch (CatalogModelException e) {
        continue;
      }
    }
  }
}
