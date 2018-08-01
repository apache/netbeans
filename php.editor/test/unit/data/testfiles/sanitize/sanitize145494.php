<?php

    class WizardStepFormExtender extends G_StepFormExtender {

        protected function onEachStep($ident, &$step) {
            if ($this->getStepIdent() == $ident) {
                $this->getParser()->registerIf("no_forward");
            }
        }

    }

    class WizardCheckboxFormExtender extends G_ExtenderAdaptor {

        public function onManageSentValues(&$sentValues) {
            parent::onManageSentValues($sentValues);

            $formItems = $this->getCaller()->getFormItems();
            foreach ($formItems as $ident => $formItem) {
                if ($formItem['_type'] == "checkbox" && !isset($sentValues[$ident])) {
                    $sentValues[$ident] = false;
                }
            }
        }

        public function onParseCaller($array) {
            parent::onParseCaller($array);

            // TODO: this should do FreeFormExtender with all form items (not only checkboxes)
            // TODO: general CheckboxFormExtender should do this in onEachFormItem and register _active
            $formItems = $this->getCaller()->getFormItems();
            foreach ($formItems as $ident => $formItem) {
                if ($formItem['_type'] == "checkbox") {
                    if ($this->getCaller()->getValue($ident)) {
                        $this->getParser()->registerIf("active_".$ident);
                    } else {
                        $this->getParser()->unregisterIf("active_".$ident);
                    }
                }
            }
        }

    }

    class WizardForm extends G_Form {

        const FINISHED = "FINISHED";

        private $finished;
        private $showSchool = false;

        protected function onPreConstruct() {
            parent::onPreConstruct();

            $this->finished = $this->getLinkVar(self::FINISHED);
            $this->setLinkVar(self::FINISHED, null);
        }

        protected function onConstruct() {
            parent::onConstruct();

            $this->setParam(self::PARSE_KEYS, array(
                'FORM_TITLE' => new LS("titleOnlineIssuingWizard", "Online Issuing Wizard"),
                'LINK_LOGIN' => $this->getLinker()->buildLink(array(
                    'mainpager' => array(G_Pager::IDENT => "login"),
                )),
            ));

            $this->setParam(G_LookupFormExtender::LOOKUP_FORM_ITEMS, array(
                'order_school_city_id' => array(
                    '_data_source_name' => "cityDataSource",
                    '_constraints' => array(
                        CityDataSource::PREFIX_CHOOSE_ROW => true,
                    ),
                    '_value_key' => "id",
                    '_title_key' => "city_name",
                ),
                'order_university_id' => array(
                    '_data_source_name' => "universityDataSource",
                    '_constraints' => array(),
                    '_value_key' => "id",
                    '_title_key' => "university_name",
                ),
            ));

            $ajaxConfirmationFileUrl = $this->getLinker()->buildLink(self::buildLinkVarsByName("mainpager", array(
                G_Pager::IDENT => "confirmationFile",
            )));
            $ajaxPhotoFileUrl = $this->getLinker()->buildLink(self::buildLinkVarsByName("mainpager", array(
                G_Pager::IDENT => "photoFile",
            )));
            $ajaxIdentityCardUrl = $this->getLinker()->buildLink(self::buildLinkVarsByName("mainpager", array(
                G_Pager::IDENT => "identityCard",
            )));
            $settings = $this->getHolder()->get("templateParametersDataSource")->getTemplateParameters("settings", 1);
            $formItems =  array(
                'order_card_type' => array(
                    '_type'  => "input_hidden",
                    '_default' => "", // isic || itic || alive
                ),
                'submit_alive' => array(
                    '_type'	 => "input_image",
                    'type'   => "alive",
                    'price'  => (int) $settings['PriceAlive'],
                    'params' => "",
                ),
                'submit_isic' => array(
                    '_type'	 => "input_image",
                    'type'   => "isic",
                    'price'  => (int) $settings['PriceIsic'],
                    'params' => "",
                ),
                'submit_itic' => array(
                    '_type'	 => "input_image",
                    'type'   => "itic",
                    'price'  => (int) $settings['PriceItic'],
                    'params' => "",
                ),
                /*'order_school_info' => array(
                    '_title' => "",
                    '_type'  => "pure_text",
                    '_default' => new LS("infoWarnUniversity", "You have to contact your school to gain ISIC/ITIC card."),
                ),*/
                // insertion point of lookups
                'own_card' => array(
                    '_title' => "",
                    '_type'  => "pure_text",
                    '_default' => new LS("infoOwnCard", "Own Card Info"),
                ),
                'elementary_school' => array(
                    '_title' => "",
                    '_type'  => "pure_text",
                    '_default' => new LS("infoElementarySchool", "Elementary School Info"),
                ),
                'university' => array(
                    '_title' => "",
                    '_type'  => "pure_text",
                    '_default' => new LS("infoUniversity", "University Info"),
                ),
                'space1' => array(
                    '_title' => "",
                    '_type'  => "space",
                ),

                'order_name' => array(
                    '_title' => new LS("titleName", "Name"),
                    '_data'  => "text",
                    'subfieldset'          => "begin",
                    'subfieldset_id'       => "main_part",
                    'subfieldset_title'    => "",
                ),
                'order_surname' => array(
                    '_title' => new LS("titleSurname", "Surname"),
                    '_data'  => "text",
                ),
                'order_birth_date' => array(
                    '_title' => new LS("titleBirthDate", "BirthDate"),
                    '_data'  => "date_select",
                ),
                'order_street' => array(
                    '_title' => new LS("titleStreet", "Street"),
                    '_data'  => "text",
                ),
                'order_city' => array(
                    '_title' => new LS("titleCity", "City"),
                    '_data'  => "text",
                ),
                'order_zip' => array(
                    '_title' => new LS("titleZip", "Zip"),
                    '_data'  => "text",
                    '_preg'   => "/^\d{5}$/",
                    'message' => new LS("infoDataTypeZip", "exactly 5 numbers"),
                ),
                'order_email' => array(
                    '_title' => new LS("titleEmail", "Email"),
                    '_data'  => "email",
                    '_default'  => "@",
                ),
                'order_fixed_phone' => array(
                    '_title' => new LS("titleFixedPhone", "Fixed Phone"),
                    '_type'  => "input_hidden",
                    '_default'  => "",
                ),
                'order_cell_phone' => array(
                    '_title' => new LS("titleCellPhone", "Cell Phone"),
                    '_data'  => "telephone_optional",
                ),
                'space2' => array(
                    '_title' => "",
                    '_type'  => "space",
                ),

                'order_photo_file' => array(
                    '_title' => new LS("titlePhotoFile", "Photo File"),
                    '_type'                => "input_file_ajax",
                    '_default'             => new LS("actionUploadImage", "Upload Image"),
                    'ajax_upload_function' => "ajaxFileUpload('".$ajaxPhotoFileUrl."', '".$this->buildFieldId("order_photo_file")."', finalizePhotoFile)",
                ),
                'order_identity_card' => array(
                    '_title' => new LS("titleIdentityCard", "Identity Card"),
                    '_type'                => "input_file_ajax",
                    '_default'             => new LS("actionUploadImage", "Upload Image"),
                    'ajax_upload_function' => "ajaxFileUpload('".$ajaxIdentityCardUrl."', '".$this->buildFieldId("order_identity_card")."', finalizePhotoFile)",
                ),
                'order_confirmation_file' => array(
                    '_title'               => new LS("titleConfirmationFile", "Confirmation File"),
                    '_type'                => "input_file_ajax",
                    '_default'             => new LS("actionUploadFile", "Upload File"),
                    'ajax_upload_function' => "ajaxFileUpload('".$ajaxConfirmationFileUrl."', '".$this->buildFieldId("order_confirmation_file")."', finalizeConfirmationFile)",
                ),
                'space3' => array(
                    '_title' => "",
                    '_type'  => "space",
                ),

                'order_sms_club' => array(
                    '_title' => new LS("titleSmsClub", "Sms Club"),
                    '_type'  => "checkbox",
                    '_default' => true,
                    'free'   => true,
                ),
                'order_email_club' => array(
                    '_title' => new LS("titleEmailClub", "Email Club"),
                    '_type'  => "checkbox",
                    '_default' => true,
                    'free'   => true,
                ),
                'order_comment' => array(
                    '_title' => new LS("titleComment", "Comment"),
                    '_data'  => "long_text_optional",
                    'free'   => true,
                ),
                'order_conditions_agreement' => array(
                    '_title' => new LS("titleConditionsAgreement", "Conditions Agreement"),
                    '_type'  => "checkbox",
                    '_default' => 0,
                    'free'   => true,
                ),
                'order_price' => array(
                    '_title' => new LS("titlePrice", "Price"),
                    '_data'  => "currency",
                ),
            );

            // files are compulsory now
            /*$settings = $this->getHolder()->get("templateParametersDataSource")->getTemplateParameters("settings", 1);
            if (array_safe_is_not($settings, "StudyConfirmationFile")) {
                unset($formItems['order_confirmation_file']);
            }
            if (array_safe_is_not($settings, "UserPhoto")) {
                unset($formItems['order_photo_file']);
            }*/

            $this->setParam(self::FORM_ITEMS, $formItems);

            $cardTypes = array();
            if (Config::CARD_ALIVE) {
                $cardTypes[] = "submit_alive";
            }
            if (Config::CARD_ISIC) {
                $cardTypes[] = "submit_isic";
            }
            if (Config::CARD_ITIC) {
                $cardTypes[] = "submit_itic";
            }

            $this->setParam(G_StepFormExtender::STEPS, array(
                "cardAndSchoolType" => array(
                    '_title' => new LS("titleCardAndSchoolType", "Card and School Type"),
                    '_fields' => array_merge(array("order_card_type"), $cardTypes),
                    'step_number' => 1,
                ),
                "info" => array(
                    '_title' => new LS("titleInfo", "Info"),
                    '_fields' => array("space1", "space2", "space3", "order_card_type", "order_school_city_id", "order_school_type", "order_high_school_id", "own_card", "elementary_school", "university", "order_confirmation_file",
                            "order_name", "order_surname", "order_birth_date", "order_street",
                            "order_city", "order_zip", "order_email", "order_fixed_phone",
                            "order_cell_phone", "order_sms_club", "order_email_club", "order_photo_file", "order_identity_card", "order_comment", "order_conditions_agreement", ),
                    'step_number' => 2,
                ),
                "finalize" => array(
                    '_title' => new LS("titleFinalize", "Finalize"),
                    '_fields' => array("space1", "space2", "space3", "order_confirmation_file",
                            "order_name", "order_surname", "order_birth_date", "order_street",
                            "order_city", "order_zip", "order_email",
                            "order_cell_phone", "order_sms_club", "order_email_club", "order_photo_file", "order_identity_card", "order_price", "order_comment"),
                    'step_number' => 3,
                ),
                "validationEmail" => array(
                    '_title' => new LS("titleValidationEmail", "Validation Email"),
                    '_fields' => array(),
                    'step_number' => 4,
                ),
            ));

            $this->setParam(G_StepFormExtender::STEP_BUTTONS, array(
                G_StepFormExtender::BUTTON_PREV => array(
                    '_type'  => "input_submit",
                    '_default' => new LS("actionPrevious", "Previous"),
                    'params' => "",
                ),
                G_StepFormExtender::BUTTON_NEXT => array(
                    '_type'  => "input_submit",
                    '_default'  => new LS("actionNext", "Next"),
                    'params' => "",
                ),
                G_StepFormExtender::BUTTON_FINISH => array(
                    '_type'  => "input_submit",
                    '_default' => new LS("actionFinish", "Finish"),
                    'params' => "",
                ),
            ));
        }

        protected function onManageConstruct() {
            parent::onManageConstruct();

            $form = $this->getParam(self::FORM_ITEMS);

            if ($this->getExtender("step")->getStepIdent() == "info" || $this->getExtender("step")->getStepIdent() == "finalize") {
                $index = 0;
                $ajaxSchoolUrl = $this->getLinker()->buildLink(self::buildLinkVarsByName("mainpager", array(
                    G_Pager::IDENT => "highSchool",
                )));
                $schoolTypes = HighSchoolDataSource::$SCHOOL_TYPES;
                switch ($this->getExtender("step")->getValue("order_card_type")) {
                    case "isic":
                        unset($schoolTypes['kindergarten']);
                        break;
                }
                array_insert($form, $index, array(
                    'order_school_city_id' => array(
                        '_title'  => new LS("titleCity", "MÄ›sto"),
                        '_data'   => "lookup",
                        'params'  => "onchange=\"ajaxSchool('".$ajaxSchoolUrl."');\"",
                    ),
                    'order_school_type' => array(
                        '_title' => new LS("titleSchoolType", "School Type"),
                        '_type'  => "select",
                        '_data'  => "drop_down",
                        '_options' => $schoolTypes,
                        'params'  => "onchange=\"ajaxSchool('".$ajaxSchoolUrl."');\"",
                    ),
                    'order_high_school_id' => array(
                        '_title'     => new LS("titleSchool", "School"),
                        '_data'      => "lookup2",
                        'params'     => "",
                        'help_title' => new LS("titleCantFindSchool", "Can't find your school?"),
                        'help_href'  => $this->getLinker()->buildLink(array(
                            'mainpager'	  => array(G_Pager::IDENT => "portal"),
                            'portalPager' => array(G_Pager::IDENT => "schoolRequest"),
                        )),
                    ),
//                    'order_university_id' => array(
//                        '_title' => new LS("titleUniversity", "University"),
//                        '_data'  => "lookup",
//                    ),
                ));

                $this->setParam(self::FORM_ITEMS, $form);
                $this->call($this->getExtender("lookup"), "onManageConstructCaller");
                $form = $this->getParam(self::FORM_ITEMS);
            }

            $removeFields = array();
            switch ($this->getExtender("step")->getValue("order_card_type")) {
                case "alive":
                    $removeFields = array(""order_school_city_id", "order_school_type", "order_high_school_id", "order_university_id", "order_confirmation_file");
                    if ($this->getExtender("step")->getStepIdent() == "finalize") {
                        $removeFields[] = "order_school_type";
                    }
                    break;
                case "isic";
                    $removeFields = array("order_university_id");
                    break;
                case "itic";
                    $removeFields = array("order_university_id");
                    if (isset($form['order_confirmation_file'])) {
                        $form['order_confirmation_file']['_title'] = new LS("titleConfirmationFileTeacher", "titleConfirmationFileTeacher");
                    }
                    break;
            }

            $form = array_diff_key($form, array_flip($removeFields));

            if ($this->getExtender("step")->getStepIdent() == "finalize") {
                foreach ($form as $fieldIdent => $field) {
                    if ($fieldIdent == "order_birth_date") {
                        $form[$fieldIdent]['_type'] = "date_output";
                    } elseif ($fieldIdent == "order_price") {
                        $form[$fieldIdent]['_type'] = "currency_output";
                    } elseif ($fieldIdent == "order_sms_club" || $fieldIdent == "order_email_club") {
                        unset($form[$fieldIdent]['free']);
                        $form[$fieldIdent]['_type'] = "pure_text";
                        $form[$fieldIdent]['_data'] = "yes_no_optional";
                    } elseif ($fieldIdent == "order_comment") {
                        unset($form[$fieldIdent]['free']);
                        $form[$fieldIdent]['_type'] = "pure_text";
                    } elseif (!isset($field['_type']) || $field['_type'] != "input_submit") {
                        $form[$fieldIdent]['_type'] = "pure_text";
                    }
                }
            }

            $this->setParam(self::FORM_ITEMS, $form);
        }

        protected function onPostConstruct() {
            parent::onPostConstruct();
        }

        protected function onNeedExtenders() {
            return array(
                new G_HighlightFormExtender("highlight", $this),
                new LookupFormExtender("lookup", $this),
                new G_MessageExtender("message", $this),
                new G_FreeFormExtender("free", $this),
                new G_DataFormExtender("data", $this),
                new SubFiledSetFormExtender("subfield", $this),
                new WizardStepFormExtender("step", $this),
                new DataType("dataType", $this),
                new WizardCheckboxFormExtender("checkbox", $this),
            );
        }

        protected function onNeedDataCheck() {
            return $this->getExtender("dataType");
        }

        protected function onNeedDataSink() {
            return $this->getHolder()->get("cardOrderDataSink");
        }

        protected function onManageInitialValues(&$initialValues) {
            parent::onManageInitialValues(&$initialValues);

            $this->getHolder()->get("wizardLogDataSink")->logWizardStep(
                    $this->getExtender("step")->getStepIdent()
            );
        }

        protected function onManageSentValues(&$sentValues) {
            parent::onManageSentValues($sentValues);

            if (isset($sentValues['order_high_school_id']) && $sentValues['order_high_school_id'] == "na") {
                $sentValues['order_high_school_id'] = "";
            }
        }

        protected function onManageCheckPass(&$pass, &$values) {
            parent::onManageCheckPass($pass, $values);

            $_SESSION['order_card_type'] = $this->getValue("order_card_type");

            if ($this->getExtender("step")->isChecking()) {
                if (!$pass) {
                    // remove all the messages with key 'infoFieldFilled'
                    $messages = $this->getMessages();
                    $empty = array();
                    foreach ($messages as $key => $val) {
                        if (isset($val['_message'])
                                && $val['_message'] instanceof LS
                                && $val['_message']->ident == "infoFieldFilled") {
                            unset($messages[$key]);
                            $empty[] = $key;
                        }
                    }
                    if (count($empty)) {
                        $this->cleanMessages();
                        switch ($this->getValue("order_card_type")) {
                            case "itic":
                                $ls = new LS("infoErrorItic", "Itic Error");
                                break;
                            default:
                                $ls = new LS("infoErrorAliveIsic", "Isic/Alive Error");
                                break;
                        }
                        $this->setMessage('missing_field', array(
                            '_fields'  => $empty,
                            '_type'    => "error",
                            '_message' => $ls,
                        ));
                        foreach ($messages as $key => $val) {
                            $this->setMessage($key, $val);
                        }
                    }
                }

                if ($this->getFormItem("order_birth_date") && $this->getValue("order_birth_date")) {
                    $birthDate = $this->getValue("order_birth_date");
                    if (!checkdate($birthDate['month'], $birthDate['day'], $birthDate['year'])) {
                        $this->setMessage('order_birth_date', array(
                            '_fields'  => "order_birth_date",
                            '_type'    => "error",
                            '_message' => new LS("titleErrorDate", "Invalid date!"),
                        ));
                        $pass = false;
                    } elseif ($this->getValue("order_card_type") == "alive") {
                        $check = strtotime("26 years 1 day ago");
                        $date = strtotime($birthDate['year']."-".$birthDate['month']."-".$birthDate['day']);
                        if ($check > $date) {
                            $this->setMessage('order_birth_date', array(
                                '_fields'  => "order_birth_date",
                                '_type'    => "error",
                                '_message' => new LS("titleErrorBirthDate", "You have to be younger than 26 years!"),
                            ));
                            $pass = false;
                        }
                    }
                }
                if ($this->getFormItem("order_photo_file") && !isset($_SESSION['wizardForm']['photoFile'])) {
                    $this->setMessage("order_photo_file", array(
                        '_fields'  => "order_photo_file",
                        '_type'    => "error",
                        '_message' => new LS("titleErrorPhotoFile", "No photo file provided!"),
                    ));

                    $pass = false;
                }
                if ($this->getFormItem("order_identity_card") && !isset($_SESSION['wizardForm']['identityCard'])) {
                    $this->setMessage("order_identity_card", array(
                        '_fields'  => "order_identity_card",
                        '_type'    => "error",
                        '_message' => new LS("titleErrorIdentityCard", "No identity card provided!"),
                    ));

                    $pass = false;
                }
                if ($this->getFormItem("order_confirmation_file") && !isset($_SESSION['wizardForm']['confirmationFile'])) {
                    $error = new LS("titleErrorConfirmationFile", "No confirmation file provided!");
                    if ($this->getValue("order_card_type") == "itic") {
                        $error = new LS("titleErrorConfirmationFileTeacher", "titleErrorConfirmationFileTeacher");
                    }
                    $this->setMessage("order_confirmation_file", array(
                        '_fields'  => "order_confirmation_file",
                        '_type'    => "error",
                        '_message' => $error,
                    ));

                    $pass = false;
                }
                if ($this->getFormItem("order_conditions_agreement") && !$this->getValue("order_conditions_agreement")) {
                    $this->setMessage('order_conditions_agreement', array(
                        '_fields'  => "order_conditions_agreement",
                        '_type'    => "error",
                        '_message' => new LS("titleErrorConditionsAgreement", "You must agree with conditions!"),
                    ));
                    $this->getParser()->registerIf("conditions_error");

                    $pass = false;
                }

                if ($this->getExtender("step")->isFinaling()
                        && ($this->getExtender("step")->getNotPassedStepIdents())) {
                    $this->setMessage('form', array(
                        '_type'    => "error",
                        '_message' => new LS("titleErrorNotPassedSteps", "There are not passed steps!"),
                    ));

                    $pass = false;
                }
            }
        }

        protected function onManageFinalPass(&$pass, &$values) {
            if ($this->getExtender("step")->isFinaling()) {
                $tmp = $values['order_birth_date']['year'];
                $tmp .= "-".$values['order_birth_date']['month'];
                $tmp .= "-".$values['order_birth_date']['day'];
                $values['order_birth_date'] = $tmp;
                $values['order_state'] = CardOrderDataSource::STATE_SETUP;

                // manage schools for alive
                switch ($this->getValue("order_card_type")) {
                    case "alive":
                        // just to be sure that no school is stored anywhere
                        $values['order_school_city_id'] = 0;
                        $values['order_school_type'] = "none";
                        $values['order_high_school_id'] = 0;
                        break;
                }

                // prefinalize files
                if (isset($_SESSION['wizardForm']['confirmationFile'])) {
                    $values['order_confirmation_file'] = $_SESSION['wizardForm']['confirmationFile'];
                } else {
                    $values['order_confirmation_file'] = "";
                }
                if (isset($_SESSION['wizardForm']['photoFile'])) {
                    $values['order_photo_file'] = $_SESSION['wizardForm']['photoFile'];
                } else {
                    $values['order_photo_file'] = "";
                }
                if (isset($_SESSION['wizardForm']['identityCard'])) {
                    $values['order_identity_card'] = $_SESSION['wizardForm']['identityCard'];
                } else {
                    $values['order_identity_card'] = "";
                }
                unset($_SESSION['wizardForm']);
            }

            if ($this->getExtender("step")->isChecking()) {
                // set price
                if (isset($values['order_card_type']) && $this->getExtender("step")->getStepIdent() == "cardAndSchoolType") {
                    $settings = $this->getHolder()->get("templateParametersDataSource")->getTemplateParameters("settings", 1);
                    $values['order_price'] = $settings['Price'.ucfirst($values['order_card_type'])];
                }

                // set card chip
                $settings = $this->getHolder()->get("templateParametersDataSource")->getTemplateParameters("settings", 1);
                $values['order_chip'] = $settings['OrderCardChip'];

            }

            parent::onManageFinalPass($pass, $values);

            if ($this->getExtender("step")->isFinaling()) {
                // finalize files
                $orderId = $this->getDataSink()->getInsertId();

                $row = array();
                $tmpFile = $values['order_confirmation_file'];
                if ($tmpFile) {
                    $confirmationFile = str_replace(session_id(), Config::getUploadedHash($orderId), $tmpFile);
                    rename($tmpFile, $confirmationFile);

                    $row['order_confirmation_file'] = str_replace(Config::UPLOADED_DIR, "", $confirmationFile);
                }

                foreach (array("order_photo_file", "order_identity_card") as $field) {
                    $tmpFile = $values[$field];
                    if ($tmpFile) {
                        $photoFile = str_replace(session_id(), Config::getUploadedHash($orderId), $tmpFile);
                        rename($tmpFile, $photoFile);

                        $row[$field] = str_replace(Config::UPLOADED_DIR, "", $photoFile);

                        // rename thumbnail file
                        $tmpFile = preg_replace("/(\.[^.]+)$/", "_thumb.jpg", $tmpFile);
                        $photoFile = preg_replace("/(\.[^.]+)$/", "_thumb.jpg", $photoFile);
                        rename($tmpFile, $photoFile);
                    }
                }

                if (count($row)) {
                    $this->getDataSink()->sink(G_IDataSinkMode::EDIT, $row, $orderId);
                }
            }

            if ($this->getExtender("step")->isFinaling()) {
                $this->getHolder()->get("wizardLogDataSink")->logWizardStep(
                        $this->getExtender("step")->getStepIdent(),
                        $this->getDataSink()->getInsertId()
                );
            }
        }

        protected function onCollectSucessHeaderVars(&$headerVars) {
            parent::onCollectSucessHeaderVars($headerVars);

            if ($this->getExtender("step")->isFinaling()) {
                $headerVars['wizardForm'][self::FINISHED] = true;
            }
       }

        protected function onManageParseFormItems(&$formItems) {
            parent::onManageParseFormItems($formItems);

            if (isset($formItems['order_school_city_id'])) {
                $lookupExtender = $this->getExtender("lookup");

                $cityId = $this->getValue("order_school_city_id");
                if (!$cityId) {
                    $cityOptions = $formItems['order_school_city_id']['_options'];
                    $firstKey = array_value(array_keys($cityOptions), 0);
                    $cityId = $cityOptions[$firstKey]['_value'];
                }

                $lookupExtender->forceParam(G_LookupFormExtender::LOOKUP_FORM_ITEMS, array(
                    'order_high_school_id' => array(
                        '_data_source_name' => "highSchoolDataSource",
                        '_constraints' => array(
                            HighSchoolDataSource::PREFIX_CHOOSE_ROW => true,
                            HighSchoolDataSource::ONLY_CITY_ID => (int) $cityId),
                        '_value_key' => "id",
                        '_title_key' => "school_name",
                    ),
                ));

                $lookupExtender->callOnPostConstruct();
                $this->call($lookupExtender, "onManageConstructCaller");
                $formItems = $this->getParam(self::FORM_ITEMS);
            }

            if ($this->getExtender("step")->getStepIdent() == "finalize") {
                // confirmation file
                if (isset($formItems['order_confirmation_file']) && $formItems['order_confirmation_file']) {
                    if (isset($_SESSION['wizardForm']['confirmationFile'])) {
                        $formItems['order_confirmation_file']['_type'] = "file_link";
                        $formItems['order_confirmation_file']['link_file'] = $_SESSION['wizardForm']['confirmationFile'];
                    }
                }

                // photo files
                $fields = array(
                    'order_photo_file'     => "photoFile",
                    'order_identity_card'  => "identityCard",
                );
                foreach ($fields as $field => $sessionVarible) {
                    if (isset($formItems[$field]) && isset($_SESSION['wizardForm'])) {
                        $formItems[$field]['_type'] = "thumbnail_image";
                        $formItems[$field]['link_image'] = $_SESSION['wizardForm'][$sessionVarible];
                        $formItems[$field]['thumbnail'] = $_SESSION['wizardForm'][$sessionVarible.'Thumbnail'];
                    }
                }
            }
        }

        protected function onManageParseValues(&$values) {
            parent::onManageParseValues($values);

            if ($this->getExtender("step")->getStepIdent() == "finalize") {
                $formItems = $this->getFormItems();
                foreach ($formItems as $ident => $formItem) {
                    if (isset($values[$ident]) && $formItem['_type'] == "pure_text"
                            && isset($formItem['_options'][$values[$ident]]['_title'])) {
                        $values[$ident] = $formItem['_options'][$values[$ident]]['_title'];
                    }
                }

                $values['order_confirmation_file'] = I18n::trans("titleNA", "N/A");
            }
        }

        protected function onParse($array) {
            parent::onParse($array);

            $this->getParser()->registerBlock("GROUP", array(&$this, "parseBlockDateGroup"));
            $this->getParser()->registerIf($this->getName());

            if ($this->getExtender("step")->getStepIdent() == "validationEmail") {
                $this->getParser()->registerKeys(array(
                    'INFO_VALIDATION_EMAIL' => I18n::trans("infoValidationEmail", "<p>Validation e-mail will be sent.</p>", $this->getValue("order_price")),
                ));
            }

            if ($this->finished) {
                $this->getParser()->registerIf("finished");
            }

            // confirmation file
            if (isset($_SESSION['wizardForm']['confirmationFile'])) {
                $this->getParser()->registerIf("confirmationFile");
                $this->getParser()->registerKeys(array(
                    'CONFIRMATION_FILE_ID'  => $this->buildFieldId("order_confirmation_file"),
                    'LINK'                  => $_SESSION['wizardForm']['confirmationFile'],
                    'MESSAGE'               => new LS("actionDownload", "Download"),
                ));
            }
            // photo files
            if (isset($_SESSION['wizardForm']['photoFile'])
                    && isset($_SESSION['wizardForm']['photoFileThumbnail'])) {
                $this->getParser()->registerIf("photoFile");
                $this->getParser()->registerKeys(array(
                    'PHOTO_FILE_ID'          => $this->buildFieldId("order_photo_file"),
                    'PHOTO_FILE_IMAGE'       => $_SESSION['wizardForm']['photoFile'],
                    'PHOTO_FILE_THUMBNAIL'   => $_SESSION['wizardForm']['photoFileThumbnail'],
                ));
            }
            if (isset($_SESSION['wizardForm']['identityCard'])
                    && isset($_SESSION['wizardForm']['identityCardThumbnail'])) {
                $this->getParser()->registerIf("identityCard");
                $this->getParser()->registerKeys(array(
                    'IDENTITY_CARD_ID'          => $this->buildFieldId("order_identity_card"),
                    'IDENTITY_CARD_IMAGE'       => $_SESSION['wizardForm']['identityCard'],
                    'IDENTITY_CARD_THUMBNAIL'   => $_SESSION['wizardForm']['identityCardThumbnail'],
                ));
            }

            // birth date
            $birthDate = $this->getValue("order_birth_date");
            if (is_array($birthDate)) {
                $this->getParser()->registerKeys($birthDate);
            }

            if (isset($_SESSION['order_card_type'])) {
                $this->getParser()->registerIf("card_type", $_SESSION['order_card_type']);
            }

            if (Config::CARD_ALIVE) {
                $this->getParser()->registerIf("CARD_ALIVE");
            }
            if (Config::CARD_ISIC) {
                $this->getParser()->registerIf("CARD_ISIC");
            }
            if (Config::CARD_ITIC) {
                $this->getParser()->registerIf("CARD_ITIC");
            }
        }

        protected function onEachFormItem($ident, &$item) {
            parent::onEachFormItem($ident, $item);

            if (array_safe_is($item, "free")) {
                $this->getParser()->registerIf("free");
            } else {
                $this->getParser()->unregisterIf("free");
            }
        }

        public function parseBlockDateGroup($array) {
            $birthDate = $this->getValue("order_birth_date");
            $return = "";
            if (!isset($array['params']['name'])) {
                return $return;
            }
            switch ($array['params']['name']) {
            case "day":
                for ($i = 1; $i <= 31; ++$i) {
                    $this->getParser()->unregisterIf("_active");
                    if (isset($birthDate['day']) && $birthDate['day'] == $i) {
                        $this->getParser()->registerIf("_active");
                    }
                    $this->getParser()->registerKeys(array(
                        '_TITLE'   => ($i < 10 ? "0".$i : $i),
                        '_VALUE'   => $i,
                    ));
                    $return .= $this->getParser()->parse($array[G_Parser::BODY]);
                }
                break;
            case "month":
                for ($i = 1; $i <= 12; ++$i) {
                    $this->getParser()->unregisterIf("_active");
                    if (isset($birthDate['month']) && $birthDate['month'] == $i) {
                        $this->getParser()->registerIf("_active");
                    }
                    $this->getParser()->registerKeys(array(
                        '_TITLE'   => ($i < 10 ? "0".$i : $i),
                        '_VALUE'   => $i,
                    ));
                    $return .= $this->getParser()->parse($array[G_Parser::BODY]);
                }
                break;
            case "year":
                $end = date("Y");
                $start = 1920;
                for ($i = $start; $i <= $end; ++$i) {
                    $this->getParser()->unregisterIf("_active");
                    if (isset($birthDate['year']) && $birthDate['year'] == $i) {
                        $this->getParser()->registerIf("_active");
                    }
                    $this->getParser()->registerKeys(array(
                        '_TITLE'   => $i,
                        '_VALUE'   => $i,
                    ));
                    $return .= $this->getParser()->parse($array[G_Parser::BODY]);
                }
                break;
            }

            return $return;
        }

    }

?>
