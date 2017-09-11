/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.htmlui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.java.html.boot.fx.FXBrowsers;
import org.openide.util.NbBundle;

final class NbBrowsers {
    public static void load(WebView view, URL page, Runnable onPageLoad, ClassLoader loader, Object... args) {
        final Stage stage = null;
        view.setContextMenuEnabled(false);
        view.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> t) {
                final Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(stage);
                ResourceBundle r = NbBundle.getBundle(NbBrowsers.class); // NOI18N
                dialogStage.setTitle(r.getString("AlertTitle")); // NOI18N
                final Button button = new Button(r.getString("AlertCloseButton")); // NOI18N
                final Text text = new Text(t.getData());
                VBox box = new VBox();
                box.setAlignment(Pos.CENTER);
                box.setSpacing(10);
                box.setPadding(new Insets(10));
                box.getChildren().addAll(text, button);
                dialogStage.setScene(new Scene(box));
                button.setCancelButton(true);
                button.setOnAction(new CloseDialogHandler(dialogStage, null));
                dialogStage.centerOnScreen();
                dialogStage.showAndWait();
            }
        });
        view.getEngine().setConfirmHandler(new Callback<String, Boolean>() {
            @Override
            public Boolean call(String question) {
                final Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(stage);
                ResourceBundle r = NbBundle.getBundle(NbBrowsers.class); // NOI18N
                dialogStage.setTitle(r.getString("ConfirmTitle")); // NOI18N
                final Button ok = new Button(r.getString("ConfirmOKButton")); // NOI18N
                final Button cancel = new Button(r.getString("ConfirmCancelButton")); // NOI18N
                final Text text = new Text(question);
                final Insets ins = new Insets(10);
                final VBox box = new VBox();
                box.setAlignment(Pos.CENTER);
                box.setSpacing(10);
                box.setPadding(ins);
                final HBox buttons = new HBox(10);
                buttons.getChildren().addAll(ok, cancel);
                buttons.setAlignment(Pos.CENTER);
                buttons.setPadding(ins);
                box.getChildren().addAll(text, buttons);
                dialogStage.setScene(new Scene(box));
                ok.setCancelButton(false);

                final boolean[] res = new boolean[1];
                ok.setOnAction(new CloseDialogHandler(dialogStage, res));
                cancel.setCancelButton(true);
                cancel.setOnAction(new CloseDialogHandler(dialogStage, null));
                dialogStage.centerOnScreen();
                dialogStage.showAndWait();
                return res[0];
            }
        });
        view.getEngine().setPromptHandler(new Callback<PromptData, String>() {
            @Override
            public String call(PromptData prompt) {
                final Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(stage);
                ResourceBundle r = NbBundle.getBundle(NbBrowsers.class); // NOI18N
                dialogStage.setTitle(r.getString("PromptTitle")); // NOI18N
                final Button ok = new Button(r.getString("PromptOKButton")); // NOI18N
                final Button cancel = new Button(r.getString("PromptCancelButton")); // NOI18N
                final Text text = new Text(prompt.getMessage());
                final TextField line = new TextField();
                if (prompt.getDefaultValue() != null) {
                    line.setText(prompt.getDefaultValue());
                }
                final Insets ins = new Insets(10);
                final VBox box = new VBox();
                box.setAlignment(Pos.CENTER);
                box.setSpacing(10);
                box.setPadding(ins);
                final HBox buttons = new HBox(10);
                buttons.getChildren().addAll(ok, cancel);
                buttons.setAlignment(Pos.CENTER);
                buttons.setPadding(ins);
                box.getChildren().addAll(text, line, buttons);
                dialogStage.setScene(new Scene(box));
                ok.setCancelButton(false);

                final boolean[] res = new boolean[1];
                ok.setOnAction(new CloseDialogHandler(dialogStage, res));
                cancel.setCancelButton(true);
                cancel.setOnAction(new CloseDialogHandler(dialogStage, null));
                dialogStage.centerOnScreen();
                dialogStage.showAndWait();
                return res[0] ? line.getText() : null;
            }
        });

        FXBrowsers.load(view, page, onPageLoad, loader, args);
    }
    private static final class CloseDialogHandler implements EventHandler<ActionEvent> {

        private final Stage dialogStage;
        private final boolean[] res;

        public CloseDialogHandler(Stage dialogStage, boolean[] res) {
            this.dialogStage = dialogStage;
            this.res = res;
        }

        @Override
        public void handle(ActionEvent t) {
            dialogStage.close();
            if (res != null) {
                res[0] = true;
            }
        }
    }
}
