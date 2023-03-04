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

package org.netbeans.modules.refactoring.spi.ui;

/**
 * Register your own TreeElementFactoryImplementation using {@link org.openide.util.lookup.ServiceProvider}
 * if you want to build your own RefactoringPreview tree.
 * 
 * For instance Java Refactoring understand Java - specific objects e.g. 
 * Projects, Groups, Methods etc.
 * 
 * <pre>
 * public TreeElement getTreeElement(Object o) {
 * .
 * .
 * if (o instanceof SourceGroup) {
 *   return new SourceGroupTreeElement((SourceGroup)o);
 *  } else if (o instanceof SomethingFromJava) {
 *    return new SomethingFromJavaTreeElement((SomethingFromJava) o);
 *  }
 * </pre>
 * 
 * Important note. It is expected from mathematical point of view, that this method
 * is function, or even better bijection.
 * @author Jan Becicka
 */
public interface TreeElementFactoryImplementation {
   /**
     * returns TreeElement for given object if possible. Otherwise returns null.
     * @param o 
     * @return 
     */
    public TreeElement getTreeElement(Object o);

    /**
     * clears internal structures
     */
    public void cleanUp();
}
