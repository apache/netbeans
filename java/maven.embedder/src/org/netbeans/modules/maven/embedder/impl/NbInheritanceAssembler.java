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
package org.netbeans.modules.maven.embedder.impl;

import java.io.File;
import java.util.ArrayList;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.inheritance.DefaultInheritanceAssembler;
import org.codehaus.plexus.util.StringUtils;
/**
 *
 * just a piece of experimental code, that allows one collecting the entire inheritance sequence
 * while loading the project itself, which is information otherwise inaccessable in maven
 * we collect the file locations and model coordinates
 * @author mkleint
 */
public class NbInheritanceAssembler extends DefaultInheritanceAssembler {
    public static final String NETBEANS_INHERITANCE = "Netbeans_inheritance";

    @Override
    public void assembleModelInheritance(Model child, Model parent, ModelBuildingRequest request, ModelProblemCollector problems) {
        InputLocation ploc = parent.getLocation("modelVersion");
        InputLocation parentLocation = parent.getLocation(NETBEANS_INHERITANCE);
        File file = parent.getPomFile();
        if (parentLocation == null) {
            InputSource source = new InputSource();
            if (ploc != null) {
                source.setLocation(ploc.getSource().getLocation());
                source.setModelId(ploc.getSource().getModelId());
            }
            parentLocation = new InputLocation(-1, -1, source);
            parent.setLocation(NETBEANS_INHERITANCE, parentLocation);
        }
        InputLocation cloc = child.getLocation("modelVersion");
        InputSource source = new InputSource();
        if (cloc != null) {
            source.setLocation((parentLocation.getSource().getLocation() != null ? parentLocation.getSource().getLocation() : "null")  + "||" + cloc.getSource().getLocation());
            source.setModelId((parentLocation.getSource().getModelId() != null ? parentLocation.getSource().getModelId() : "null") + "||" + cloc.getSource().getModelId());
        }
        InputLocation childLocation = new InputLocation(-1, -1, source);
        child.setLocation(NETBEANS_INHERITANCE, childLocation);
        
        super.assembleModelInheritance(child, parent, request, problems);
    }
    
    /**
     * retrieve the collected inheritance chain from the model. 
     * @param model null if the data is not present in the model, otherwise an array of InputSource instance, first one is the current model.
     * @return 
     */
    public static InputSource[] getInheritanceSource(Model model) {
        InputLocation location = model.getLocation(NETBEANS_INHERITANCE);
        if (location != null) {
            ArrayList<InputSource> toRet = new ArrayList<InputSource>();
            InputSource source = location.getSource();
            String locations = source.getLocation();
            String models = source.getModelId();
            String[] locs = StringUtils.split(locations, "||");
            String[] mdls = StringUtils.split(models, "||");
            assert locs.length == mdls.length;
            int i = 0;
            while (i < locs.length) {
                InputSource is = new InputSource();
                String loc = locs[i];
                String mdl = mdls[i];
                if (!"null".equals(loc)) {
                    is.setLocation(loc);
                }
                if (!"null".equals(mdl)) {
                    is.setModelId(mdl);
                }
                toRet.add(0, is);
            }
            return toRet.toArray(new InputSource[0]);
        }
        return null;
    }
    
}
