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
