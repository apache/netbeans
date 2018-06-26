<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
