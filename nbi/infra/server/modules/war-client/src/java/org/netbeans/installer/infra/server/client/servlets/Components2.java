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
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.helper.Platform;

public class Components2 extends HttpServlet {
    @EJB
    private Manager manager;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final List<String> java = Arrays.asList(
                "nb-platform",
                "nb-base",
                "nb-javase");
        final List<String> javaee = Arrays.asList(
                "nb-platform",
                "nb-base",
                "nb-javase",
                "nb-javaee",                
                "nb-groovy",
                "glassfish",
                "tomcat",
                "sjsas");
        final List<String> javame = Arrays.asList(
                "nb-platform",
                "nb-base",
                "nb-javase",
                "nb-javame");
        final List<String> ruby = Arrays.asList(
                "nb-platform",
                "nb-base",
                "nb-ruby");
        final List<String> cnd = Arrays.asList(
                "nb-platform",
                "nb-base",
                "nb-cnd");
        final List<String> php = Arrays.asList(
                "nb-platform",
                "nb-base",
                "nb-php");
        
        final List<String> full = Arrays.asList(
                "nb-platform",
                "nb-base",
                "nb-javase",
                "nb-javaee",
                "nb-javame",
                "nb-cnd",
                "nb-soa",
                "nb-uml",
                "nb-ruby",
                "nb-groovy",
                "nb-php",
                "glassfish",
                "openesb",
                "sjsam",
                "tomcat",
                "sjsas");
        final List<String> hidden = Arrays.asList(
                "nb-platform",
                //"nb-base",
                //"nb-php",
                "openesb",
                "sjsam");
        final Map<String, String> notes = new HashMap<String, String>();
        //notes.put("nb-javase", "for Java SE, includes GUI Builder, Profiler");
        
        try {
            response.setContentType("text/javascript; charset=UTF-8");
            final PrintWriter out = response.getWriter();
            
            final Registry registry = manager.loadRegistry("NetBeans");
            
            final List<Product> products = getProducts(registry.getRegistryRoot());
            final List<Group> groups = getGroups(registry.getRegistryRoot());
            
            final Map<Integer, Integer> productMapping =
                    new HashMap<Integer, Integer>();
            
            final List<String> productUids =
                    new LinkedList<String>();
            final List<String> productVersions =
                    new LinkedList<String>();
            final List<String> productDisplayNames =
                    new LinkedList<String>();
            final List<String> productNotes =
                    new LinkedList<String>();
            final List<String> productDescriptions =
                    new LinkedList<String>();
            final List<String> productDownloadSizes =
                    new LinkedList<String>();
            final List<List<Platform>> productPlatforms =
                    new LinkedList<List<Platform>>();
            final List<String> productProperties =
                    new LinkedList<String>();
            
            final List<Integer> defaultGroupProducts =
                    new LinkedList<Integer>();
            final List<List<Integer>> groupProducts =
                    new LinkedList<List<Integer>>();
            final List<String> groupDisplayNames =
                    new LinkedList<String>();
            final List<String> groupDescriptions =
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
                productDisplayNames.add(product.getDisplayName().replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
                productDescriptions.add(product.getDescription().replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
                productDownloadSizes.add(Long.toString(size));
                productPlatforms.add(product.getPlatforms());
                
                if (notes.get(product.getUid()) != null) {
                    productNotes.add(notes.get(product.getUid()).replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
                } else {
                    productNotes.add("");
                }
                
                String properties = "PROPERTY_NONE";
                if (java.contains(product.getUid())) {
                    properties += " | PROPERTY_JAVA";
                }
                if (javaee.contains(product.getUid())) {
                    properties += " | PROPERTY_JAVAEE";
                }
                if (javame.contains(product.getUid())) {
                    properties += " | PROPERTY_JAVAME";
                }
                if (ruby.contains(product.getUid())) {
                    properties += " | PROPERTY_RUBY";
                }
		if (cnd.contains(product.getUid())) {
                    properties += " | PROPERTY_CND";
                }
		if (php.contains(product.getUid())) {
                    properties += " | PROPERTY_PHP";
                }
                if (full.contains(product.getUid())) {
                    properties += " | PROPERTY_FULL";
                }
                if (hidden.contains(product.getUid())) {
                    properties += " | PROPERTY_HIDDEN";
                }
                productProperties.add(properties);
                
                productMapping.put(i, productUids.size() - 1);
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
            
            out.println("product_notes = new Array();");
            for (int i = 0; i < productNotes.size(); i++) {
                out.println("    product_notes[" + i + "] = \"" + productNotes.get(i) + "\";");
            }
            out.println();
            
            out.println("product_descriptions = new Array();");
            for (int i = 0; i < productDescriptions.size(); i++) {
                out.println("    product_descriptions[" + i + "] = \"" + productDescriptions.get(i) + "\";");
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
            
            for (int i = 0; i < productUids.size(); i++) {
                defaultGroupProducts.add(Integer.valueOf(i));
            }
            
            for (Group group: groups) {
                List<Integer> components = new LinkedList<Integer>();
                for (int i = 0; i < products.size(); i++) {
                    if (products.get(i).getParent().equals(group)) {
                        Integer index = Integer.valueOf(productMapping.get(i));
                        if (!components.contains(index)) {
                            components.add(index);
                            defaultGroupProducts.remove(index);
                        }
                    }
                }
                
                groupProducts.add(components);
                groupDisplayNames.add(group.getDisplayName().replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
                groupDescriptions.add(group.getDescription().replace("\"", "\\\"").replaceAll("\r\n|\r|\n", "\\\n"));
            }
            
            if (defaultGroupProducts.size() > 0) {
                groupProducts.add(0, defaultGroupProducts);
                groupDisplayNames.add(0, "");
                groupDescriptions.add(0, "");
            }
            
            out.println("group_products = new Array();");
            for (int i = 0; i < groupProducts.size(); i++) {
                out.println("    group_products[" + i + "] = new Array();");
                for (int j = 0; j < groupProducts.get(i).size(); j++) {
                    out.println("        group_products[" + i + "][" + j + "] = " + groupProducts.get(i).get(j) + ";");
                }
            }
            out.println();
            
            out.println("group_display_names = new Array();");
            for (int i = 0; i < groupDisplayNames.size(); i++) {
                out.println("    group_display_names[" + i + "] = \"" + groupDisplayNames.get(i) + "\";");
            }
            out.println();
            
            out.println("group_descriptions = new Array();");
            for (int i = 0; i < groupDescriptions.size(); i++) {
                out.println("    group_descriptions[" + i + "] = \"" + groupDescriptions.get(i) + "\";");
            }
            out.println();
            
            out.close();
        } catch (ManagerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private List<Product> getProducts(RegistryNode root) {
        final List<Product> list = new LinkedList<Product>();
        
        for (RegistryNode node: root.getChildren()) {
            if (node instanceof Product) {
                list.add((Product) node);
            }
            
            list.addAll(getProducts(node));
        }
        
        return list;
    }
    
    private List<Group> getGroups(RegistryNode root) {
        final List<Group> list = new LinkedList<Group>();
        
        for (RegistryNode node: root.getChildren()) {
            if (node instanceof Group) {
                list.add((Group) node);
            }
            
            list.addAll(getGroups(node));
        }
        
        return list;
    }
}
