<?php
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

/**
 * Zend Tool Framework Provider which lists all available commands.
 *
 * <b>WARNING: User changes to this file should be avoided.</b>
 *
 * @package NetBeans
 */
class NetBeansCommandsProvider extends Zend_Tool_Framework_Provider_Abstract {

    public function getName() {
        return "NbCommands";
    }

    /**
     * @see Zend_Tool_Framework_Client_Console_HelpSystem
     */
    public function show($separator = " ", $includeAllSpecialties = true) {
        $manifest = $this->_registry->getManifestRepository();

        $providerMetadatasSearch = array(
                'type'       => 'Tool',
                'name'       => 'providerName',
                'clientName' => 'console'
        );

        $actionMetadatasSearch = array(
                'type'       => 'Tool',
                'name'       => 'actionName',
                'clientName' => 'console'
        );

        // get the metadata's for the things to display
        $displayProviderMetadatas = $manifest->getMetadatas($providerMetadatasSearch);
        $displayActionMetadatas = $manifest->getMetadatas($actionMetadatasSearch);

        // create index of actionNames
        for ($i = 0; $i < count($displayActionMetadatas); $i++) {
            $displayActionNames[] = $displayActionMetadatas[$i]->getActionName();
        }

        foreach ($displayProviderMetadatas as $providerMetadata) {

            $providerName = $providerMetadata->getProviderName();

            // ignore itself
            if ($providerName == "NbCommands") {
                continue;
            }

            $providerSignature = $providerMetadata->getReference();

            foreach ($providerSignature->getActions() as $actionInfo) {

                $actionName = $actionInfo->getName();

                // check to see if this action name is valid
                if (($foundActionIndex = array_search($actionName, $displayActionNames)) === false) {
                    continue;
                } else {
                    $actionMetadata = $displayActionMetadatas[$foundActionIndex];
                }

                $specialtyMetadata = $manifest->getMetadata(array(
                        'type'          => 'Tool',
                        'name'          => 'specialtyName',
                        'providerName'  => $providerName,
                        'specialtyName' => '_Global',
                        'clientName'    => 'console'
                ));

                // lets do the main _Global action first
                $actionableGlobalLongParamMetadata = $manifest->getMetadata(array(
                        'type'          => 'Tool',
                        'name'          => 'actionableMethodLongParams',
                        'providerName'  => $providerName,
                        'specialtyName' => '_Global',
                        'actionName'    => $actionName,
                        'clientName'    => 'console'
                ));

                if ($actionableGlobalLongParamMetadata) {

                    $this->respondWithCommand($separator, $providerMetadata, $actionMetadata, $specialtyMetadata, $actionableGlobalLongParamMetadata);

                    $actionIsGlobal = true;
                } else {
                    $actionIsGlobal = false;
                }

                $actionableGlobalMetadatas = $manifest->getMetadatas(array(
                        'type'          => 'Tool',
                        'name'          => 'actionableMethodLongParams',
                        'providerName'  => $providerName,
                        'actionName'    => $actionName,
                        'clientName'    => 'console'
                ));

                if (!$actionIsGlobal && count($actionableGlobalMetadatas) == 1) {
                    $this->_registry->getResponse()->appendContent('single special action/provider');
                }

                if ($includeAllSpecialties) {

                    foreach ($providerSignature->getSpecialties() as $specialtyName) {

                        if ($specialtyName == '_Global') {
                            continue;
                        }

                        $specialtyMetadata = $manifest->getMetadata(array(
                                'type'          => 'Tool',
                                'name'          => 'specialtyName',
                                'providerName'  => $providerMetadata->getProviderName(),
                                'specialtyName' => $specialtyName,
                                'clientName'    => 'console'
                        ));

                        $actionableSpecialtyLongMetadata = $manifest->getMetadata(array(
                                'type'          => 'Tool',
                                'name'          => 'actionableMethodLongParams',
                                'providerName'  => $providerMetadata->getProviderName(),
                                'specialtyName' => $specialtyName,
                                'actionName'    => $actionName,
                                'clientName'    => 'console'
                        ));

                        if ($actionableSpecialtyLongMetadata) {
                            $this->respondWithCommand($separator, $providerMetadata, $actionMetadata, $specialtyMetadata, $actionableSpecialtyLongMetadata);
                        }

                    }
                }
            }
        }
        return $this;

    }

    private function respondWithCommand($separator, Zend_Tool_Framework_Metadata_Tool $providerMetadata, Zend_Tool_Framework_Metadata_Tool $actionMetadata,
            Zend_Tool_Framework_Metadata_Tool $specialtyMetadata, Zend_Tool_Framework_Metadata_Tool $parameterLongMetadata) {
        $response = $this->_registry->getResponse();
        $response->appendContent(
                $actionMetadata->getValue().' '.$providerMetadata->getValue(),
                array('separator' => false)
        );

        if ($specialtyMetadata->getSpecialtyName() != '_Global') {
            $response->appendContent('.'.$specialtyMetadata->getValue(), array('separator' => false));
        }

        $response->appendContent($separator, array('separator' => false));
        $params = "";
        foreach ($parameterLongMetadata->getValue() as $paramName => $consoleParamName) {
            $methodInfo = $parameterLongMetadata->getReference();
            $paramString = $consoleParamName;
            if ( ($defaultValue = $methodInfo['parameterInfo'][$paramName]['default']) != null) {
                $paramString .= '[='.$defaultValue.']';
            }
            $params .= $paramString.' ';
        }

        $response->appendContent(trim($params), array('separator' => true));
        return $this;
    }
}

?>
