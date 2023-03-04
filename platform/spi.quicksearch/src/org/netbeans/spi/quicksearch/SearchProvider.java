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


package org.netbeans.spi.quicksearch;

/**
 * Main interface of Quick Search API. Implement this interface
 * to provide new group of results for quick search.
 * 
 * In order to plug into Quick Search UI and show quick search results for your
 * providers, implementations of SearchProvider must be registered through xml
 * layer in following way:
 * 
 * <pre>
 *  &lt;folder name="QuickSearch"&gt;
 *      &lt;folder name="MyCategoryID"&gt;
 *          &lt;!--Attribute for localization - provide localized display name of category!--&gt;
 *          &lt;attr name="SystemFileSystem.localizingBundle" stringvalue="org.netbeans.modules.yourmodule.YourBundle"/>
 *          &lt;!--Attribute for command prefix - used to narrow search to this category only!--&gt;
 *          &lt;attr name="command" stringvalue="p"/>
 *          &lt;!--Attribute for category ordering!--&gt;
 *          &lt;attr name="position" intvalue="200"/&gt;
 *          &lt;!--Note that multiple providers can contribute to one category!--&gt;
 *          &lt;file name="org-netbeans-module2-package2-MySearchProviderImpll.instance"/&gt;
 *          &lt;file name="org-netbeans-module2-package3-MySearchProviderImpl2.instance"/&gt;
 *      &lt;/folder&gt;
 *  &lt;/folder&gt;
 * </pre>
 * 
 * @author  Jan Becicka, Dafe Simonek
 */
public interface SearchProvider {
    
    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with appropriate results.
     * 
     * Typical implementation would look like follows:
     * 
     * <pre>
     *  for (SearchedItem item : getAllItemsToSearchIn()) {
     *      if (isConditionSatisfied(item, request)) {
     *          if (!response.addResult(item.getRunnable(), item.getDisplayName(),
     *              item.getShortcut(), item.getDisplayHint())) {
     *               break;
     *          }
     *      }
     *  }
     * </pre>
     * 
     * It may happen that the Provider searches for some considerable time, or searches
     * a considerable number of items without any results. It may check use 
     * {@link SearchResponse#isObsolete()} to determine if the search was cancelled 
     * or obsoleted without adding any items.
     * <p>
     * Threading: This method can be called outside EQ thread by infrastructure.
     * 
     * @param request Search request object that contains information what to
     * search for.
     * @param response Search response object that stores search results. Note
     * that it's important to react to return value of SearchResponse.addResult(...)
     * method and stop computation if false value is returned.
     * 
     */
    public void evaluate (SearchRequest request, SearchResponse response);
    
    
}
