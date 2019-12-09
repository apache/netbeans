/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.htmlui.jfx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import net.java.html.boot.fx.FXBrowsers;
import net.java.html.js.JavaScriptBody;
import org.openide.util.NbBundle;

final class NbBrowsers {
    static {
        Platform.setImplicitExit(false);
    }
    static void load(WebView view, URL page, final Runnable onPageLoad, ClassLoader loader, Object... args) {
        class ApplySkin implements Runnable {
            @Override
            public void run() {
                applyNbSkin();
                onPageLoad.run();
            }
        }
        load0(view, page, new ApplySkin(), loader, args);
    }

    static void applyNbSkin() {
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        String name = findLafName(lookAndFeel.getName());
        if (name == null) {
            return;
        }
        String resource = "nbres:/org/netbeans/modules/htmlui/css/wizard-" + name + ".css";
        loadCss(resource);
    }

    private static String findLafName(String name) {
        switch (name) {
            case "Mac OS X":
                return "mac";
            case "Metal":
                return "metal";
            case "GTK look and feel":
                return "gtk";
            case "Nimbus":
                return "nimbus";
            case "Windows":
                return "win";
            case "Darcula":
                return "darcula";
        }
        return null;
    }

    @JavaScriptBody(args = { "css" }, body =
"  if (!document.head || document.head.getAttribute(\"data-netbeans-css\") == \"false\") {\n" +
"     return;\n" +
"  }\n" +
"  var link = document.createElement(\"link\");\n" +
"  link.rel = \"stylesheet\";\n" +
"  link.type = \"text/css\";\n" +
"  link.href = css;\n" +
"  document.head.appendChild(link);"
    )
    private static native void loadCss(String css);

    private static void load0(WebView view, URL page, Runnable onPageLoad, ClassLoader loader, Object... args) {
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
