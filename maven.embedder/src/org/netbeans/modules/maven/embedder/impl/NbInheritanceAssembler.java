/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
