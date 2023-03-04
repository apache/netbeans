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
package org.netbeans.modules.javafx2.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.text.StyledDocument;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.javafx2.editor.fxml.FXMLDataObject;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

import static org.netbeans.modules.javafx2.editor.actions.Bundle.*;

@ActionID(
    category = "Source",
    id = "org.netbeans.modules.javafx2.editor.actions.GenerateControllerAction")

@ActionRegistration(
    iconBase = "org/netbeans/modules/javafx2/editor/resources/generate_controller.png",
    displayName = "#CTL_GenerateControllerAction")

@ActionReferences({
    @ActionReference(path = "Menu/Source", position = 3050, separatorAfter = 3075),
    @ActionReference(path = "Loaders/text/x-fxml+xml/Actions", position = 2150, separatorAfter = 2175)
})
@Messages("CTL_GenerateControllerAction=Make Controller")
public final class GenerateControllerAction implements ActionListener {

    private final FXMLDataObject context;

    public GenerateControllerAction(FXMLDataObject context) {
        this.context = context;
    }
    
    @NbBundle.Messages({
        "# {0} - fxml source file name",
        "FMT_controllerClassName={0}Controller",
        "ERR_CannotCreateController=Could not create controller source: {0}"
    })
    @Override
    public void actionPerformed(ActionEvent ev) {
        // parse the source file:
        Source s = Source.create(context.getPrimaryFile());
        
        
        final DataObject controllerFile;
        final ControllerFileMaker fileMaker = new ControllerFileMaker(context);
        try {
            controllerFile = fileMaker.getControllerFile();
            if (controllerFile == null) {
                // handled error
                return;
            }
        } catch (IOException ex) {
            Exceptions.attachSeverity(ex, Level.WARNING);
            Exceptions.printStackTrace(
                Exceptions.attachMessage(ex, ERR_CannotCreateController(ex.getLocalizedMessage()))
            );
            return;
        }

        final ModificationResult[] result = new ModificationResult[1];
        try {
            ParserManager.parse(Collections.singletonList(s), new UserTask() {
                public void run(ResultIterator iter) throws Exception {
                    FxmlParserResult fxmlResult = (FxmlParserResult)iter.getParserResult();
                    
                    FxModel model = fxmlResult.getSourceModel();
                    
                    ControllerGenerator gen = new ControllerGenerator(fxmlResult, controllerFile);
                    //gen.setControllerClassName(fileMaker.getControllerClassName())
                    result[0] = JavaSource.forFileObject(controllerFile.getPrimaryFile()).runModificationTask(gen);
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        try {
            ModificationResult res = result[0];
            res.commit();

            if (!fileMaker.hasControllerName()) {
                EditorCookie edit = context.getLookup().lookup(EditorCookie.class);
                boolean mod = edit.isModified();
                StyledDocument sd = edit.openDocument();
                s = Source.create(sd);
                ControllerGenerator.generateControllerAttribute(s, 
                        fileMaker.getControllerClassName());
                // save
                if (!mod) {
                    edit.saveDocument();
                }
            }
            Savable save = controllerFile.getLookup().lookup(Savable.class);
            if (save != null) {
                save.save();
            }
            if (res.getDifferences(controllerFile.getPrimaryFile()) != null) {
                Editable ed = controllerFile.getLookup().lookup(Editable.class);
                ed.edit();
            }
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
