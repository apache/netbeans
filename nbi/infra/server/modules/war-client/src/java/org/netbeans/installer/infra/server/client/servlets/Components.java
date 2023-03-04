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

package org.netbeans.installer.infra.server.client.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.Manager;
import org.netbeans.installer.infra.server.ejb.ManagerException;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.dependencies.Requirement;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Platform;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class Components extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final List<String> fixed = Arrays.asList(
                "nb-ide");
        final List<String> standard = Arrays.asList(
                "nb-ide",
                "glassfish");
        final List<String> selected = Arrays.asList(
                "nb-xml",
                "nb-soa",
                "nb-identity",
                "glassfish",
                "openesb",
                "sjsam");
        final Map<String, List<String>> desires =
                new HashMap<String, List<String>>();
        desires.put("nb-visualweb", Arrays.asList("glassfish"));
        
        final Map<String, String> comments =
                new HashMap<String, String>();
        comments.put("jdk", "WTF!T!&!&");
        
        try {
            response.setContentType("text/javascript; charset=UTF-8");
            final PrintWriter out = response.getWriter();
            
            final Registry registry = manager.loadRegistry("NetBeans 6.1");
            
            final List<Product> products = registry.getProducts();
            final List<Group> groups = registry.getGroups();
            
            final Map<Integer, Integer> productMapping =
                    new HashMap<Integer, Integer>();
            
            final List<String> productUids =
                    new LinkedList<String>();
            final List<String> productVersions =
                    new LinkedList<String>();
            final List<String> productDisplayNames =
                    new LinkedList<String>();
            final List<String> productDownloadSizes =
                    new LinkedList<String>();
            final List<List<Platform>> productPlatforms =
                    new LinkedList<List<Platform>>();
            final List<String> productProperties =
                    new LinkedList<String>();
            final List<List<List<Integer>>> productRequirements =
                    new LinkedList<List<List<Integer>>>();
            final List<List<Integer>> productDesires =
                    new LinkedList<List<Integer>>();
            final List<String> productComments =
                    new LinkedList<String>();
            
            final List<Integer> defaultGroupProducts =
                    new LinkedList<Integer>();
            final List<List<Integer>> groupProducts =
                    new LinkedList<List<Integer>>();
            final List<String> groupDisplayNames =
                    new LinkedList<String>();
            
            for (int i = 0; i < products.size(); i++) {
                final Product product = products.get(i);
                
                boolean existingFound = false;
                for (int j = 0; j < productUids.size(); j++) {
                    if (productUids.get(j).equals(product.getUid()) &&
                            productVersions.get(j).equals(product.getVersion().toString())) {
                        productPlatforms.get(j).addAll(product.getPlatforms());
                        productMapping.put(i, j);
                        existingFound = true;
                        break;
                    }
                }
                
                if (existingFound) {
                    continue;
                }
                
                long size = (long) Math.ceil(
                        ((double) product.getDownloadSize()) / 1024. );
                productUids.add(product.getUid());
                productVersions.add(product.getVersion().toString());
                productDisplayNames.add(product.getDisplayName());
                productDownloadSizes.add(Long.toString(size));
                productPlatforms.add(product.getPlatforms());
                
                String properties = "PROPERTY_NONE";
                if (fixed.contains(product.getUid())) {
                    properties += " | PROPERTY_FIXED";
                }
                if (standard.contains(product.getUid())) {
                    properties += " | PROPERTY_STANDARD";
                }
                if (selected.contains(product.getUid())) {
                    properties += " | PROPERTY_SELECTED";
                }
                productProperties.add(properties);
                
                List<List<Integer>> requirements = new LinkedList<List<Integer>>();
                productRequirements.add(requirements);
                
                List<Integer> currentDesires = new LinkedList<Integer>();
                productDesires.add(currentDesires);
                
                productComments.add("");
                
                productMapping.put(i, productUids.size() - 1);
            }
            
            for (int i = 0; i < products.size(); i++) {
                final int index = productMapping.get(i);
                final Product product = products.get(i);
                
                List<List<Integer>> requirements = productRequirements.get(index);
                for (Dependency requirement: product.getDependencies(Requirement.class)) {
                    List<Integer> requireeIds = new LinkedList<Integer>();
                    requirements.add(requireeIds);
                    for (Product requiree: registry.getProducts(requirement)) {
                        requireeIds.add(
                                productMapping.get(products.indexOf(requiree)));
                    }
                }
                
                List<Integer> currentDesires = productDesires.get(index);
                productDesires.set(index, currentDesires);
                if (desires.get(product.getUid()) != null) {
                    for (String uid: desires.get(product.getUid())) {
                        for (Product desiree: products) {
                            if (desiree.getUid().equals(uid)) {
                                int indexOf = productMapping.get(products.indexOf(desiree));
                                if (!currentDesires.contains(indexOf)) {
                                    currentDesires.add(indexOf);
                                }
                            }
                        }
                    }
                }
                
                if (comments.get(product.getUid()) != null) {
                    productComments.set(index, comments.get(product.getUid()));
                }
            }
            
            out.println("product_uids = new Array();");
            for (int i = 0; i < productUids.size(); i++) {
                out.println("    product_uids[" + i + "] = \"" + productUids.get(i) + "\";");
            }
            out.println();
            
            out.println("product_versions = new Array();");
            for (int i = 0; i < productVersions.size(); i++) {
                out.println("    product_versions[" + i + "] = \"" + productVersions.get(i) + "\";");
            }
            out.println();
            
            out.println("product_display_names = new Array();");
            for (int i = 0; i < productDisplayNames.size(); i++) {
                out.println("    product_display_names[" + i + "] = \"" + productDisplayNames.get(i) + "\";");
            }
            out.println();
            
            out.println("product_download_sizes = new Array();");
            for (int i = 0; i < productDownloadSizes.size(); i++) {
                out.println("    product_download_sizes[" + i + "] = " + productDownloadSizes.get(i) + ";");
            }
            out.println();
            
            out.println("product_platforms = new Array();");
            for (int i = 0; i < productPlatforms.size(); i++) {
                out.println("    product_platforms[" + i + "] = new Array();");
                for (int j = 0; j < productPlatforms.get(i).size(); j++) {
                    out.println("        product_platforms[" + i + "][" + j + "] = \"" + productPlatforms.get(i).get(j) + "\";");
                }
            }
            out.println();
            
            out.println("product_properties = new Array();");
            for (int i = 0; i < productProperties.size(); i++) {
                out.println("    product_properties[" + i + "] = " + productProperties.get(i) + ";");
            }
            out.println();
            
            out.println("product_requirements = new Array();");
            for (int i = 0; i < productRequirements.size(); i++) {
                out.println("    product_requirements[" + i + "] = new Array();");
                for (int j = 0; j < productRequirements.get(i).size(); j++) {
                    out.println("        product_requirements[" + i + "][" + j + "] = new Array();");
                    for (int k = 0; k < productRequirements.get(i).get(j).size(); k++) {
                        out.println("            product_requirements[" + i + "][" + j + "][" + k + "] = " + productRequirements.get(i).get(j).get(k) + ";");
                    }
                }
            }
            
            out.println("product_desires = new Array();");
            for (int i = 0; i < productDesires.size(); i++) {
                out.println("    product_desires[" + i + "] = new Array();");
                for (int j = 0; j < productDesires.get(i).size(); j++) {
                    out.println("        product_desires[" + i + "][" + j + "] = " + productDesires.get(i).get(j) + ";");
                }
            }
            out.println();
            
            out.println("product_comments = new Array();");
            for (int i = 0; i < productComments.size(); i++) {
                out.println("    product_comments[" + i + "] = \"" + productComments.get(i) + "\";");
            }
            out.println();
            
            
            for (int i = 0; i < productUids.size(); i++) {
                defaultGroupProducts.add(Integer.valueOf(i));
            }
            
            for (Group group: groups) {
                // skip the registry root
                if (group.getUid().equals("")) {
                    continue;
                }
                
                List<Integer> components = new LinkedList<Integer>();
                for (int i = 0; i < products.size(); i++) {
                    if (group.isAncestor(products.get(i))) {
                        Integer index = Integer.valueOf(productMapping.get(i));
                        if (!components.contains(index)) {
                            components.add(index);
                            defaultGroupProducts.remove(index);
                        }
                    }
                }
                
                groupProducts.add(components);
                groupDisplayNames.add(group.getDisplayName());
            }
            
            if (groupProducts.size() > 0) {
                out.println("group_products = new Array();");
                out.println("    group_products[0] = new Array();");
                for (int j = 0; j < defaultGroupProducts.size(); j++) {
                    out.println("        group_products[0][" + j + "] = " + defaultGroupProducts.get(j) + ";");
                }
                for (int i = 0; i < groupProducts.size(); i++) {
                    out.println("    group_products[" + (i + 1) + "] = new Array();");
                    for (int j = 0; j < groupProducts.get(i).size(); j++) {
                        out.println("        group_products[" + (i + 1) + "][" + j + "] = " + groupProducts.get(i).get(j) + ";");
                    }
                }
                out.println();
                
                out.println("group_display_names = new Array();");
                out.println("    group_display_names[0] = \"\";");
                for (int i = 0; i < groupDisplayNames.size(); i++) {
                    out.println("    group_display_names[" + (i + 1) + "] = \"" + groupDisplayNames.get(i) + "\";");
                }
                out.println();
            } else {
                out.println("group_products = new Array();");
                out.println("    group_products[0] = new Array();");
                for (int j = 0; j < defaultGroupProducts.size(); j++) {
                    out.println("        group_products[0][" + j + "] = " + defaultGroupProducts.get(j) + ";");
                }
                out.println();
                
                out.println("group_display_names = new Array();");
                out.println("    group_display_names[0] = \"\";");
                out.println();
            }
            
            out.close();
        } catch (ManagerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
