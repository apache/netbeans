<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fish.payara.micro</groupId>
    <artifactId>payara-micro-sample</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <version.payara>${payaraMicroVersion}</version.payara>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>fish.payara.maven.plugins</groupId>
                <artifactId>payara-micro-maven-plugin</artifactId>
                <version>1.0.5</version>
                <configuration>
                    <payaraVersion>${r"${version.payara}"}</payaraVersion>
                    <deployWar>false</deployWar>
                    <commandLineOptions>
<#if autoBindHttp == "true">
                        <option>
                            <key>--autoBindHttp</key>
                        </option>
</#if>
                        <option>
                            <key>--deploy</key>
                            <value>${r"${project.build.directory}"}/${r"${project.build.finalName}"}</value>
                        </option>
                    </commandLineOptions>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
