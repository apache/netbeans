<?xml version="1.0" encoding="UTF-8"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="apichanges.xsl" />

    <xsl:output method="xml" omit-xml-declaration="yes"/>

    <!-- Sep 1, 1997 is the start of the NetBeans epoch  -->
    <xsl:param name="changes-since-year" select="'1997'" />
    <xsl:param name="changes-since-month" select="'09'" />
    <xsl:param name="changes-since-day" select="'01'" />
    <!-- relative path to the api changes document -->
    <xsl:param name="changes-since-url" select="'.'" />
    <!-- amount of changes to print -->
    <xsl:param name="changes-since-amount" select="'65535'" />

    <!-- Main document structure: -->
    <xsl:template match="/" name="api-changes" >
        <!-- amount of changes to print -->
        <xsl:param name="changes-since-amount" select="$changes-since-amount" />
         <xsl:text>

         
</xsl:text>
        <xsl:comment>Search for dates that are later or equal to <xsl:value-of select="$changes-since-year" 
          />-<xsl:value-of select="$changes-since-month" />-<xsl:value-of select="$changes-since-day" /> in
          <xsl:value-of select="$changes-since-url" />
        </xsl:comment>
        <xsl:apply-templates select="//change" mode="changes-since" >
            <xsl:with-param name="changes-since-amount" select="$changes-since-amount" />
            <xsl:sort data-type="number" order="descending" select="date/@year"/>
            <xsl:sort data-type="number" order="descending" select="date/@month"/>
            <xsl:sort data-type="number" order="descending" select="date/@day"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- Summarizing links to changes: -->
    <xsl:template match="change" mode="changes-since" >
        <xsl:param name="changes-since-amount" select="$changes-since-amount" />
        
        <xsl:variable name="day" select="date/@day" />
        <xsl:variable name="month" select="date/@month" />
        <xsl:variable name="year" select="date/@year" />

        <xsl:variable name="number-of-newer" select="count(
            //change[ 
              (number(date/@year) > number($year)) or
              (number(date/@year) = number($year) and number(date/@month) > number($month)) or
              (number(date/@year) = number($year) and number(date/@month) = number($month) and number(date/@day) > number($day))
            ]
        )" />
         
         <xsl:text>
</xsl:text>
        <xsl:comment>Checking date <xsl:value-of select="$year" 
          />-<xsl:value-of select="$month" />-<xsl:value-of select="$day" 
          /> with count of newer <xsl:value-of select="$number-of-newer" />
        </xsl:comment>
       <xsl:choose>
            <xsl:when test="number($number-of-newer) >= number($changes-since-amount)" >
                <xsl:comment>Skipped as the amount of changes is too big</xsl:comment>
            </xsl:when>
            <xsl:when test="number(date/@year) > number($changes-since-year)">
                <xsl:comment>year ok</xsl:comment>
                <xsl:call-template name="print-change" />
            </xsl:when>
            <xsl:when test="number($changes-since-year) = number(date/@year)">
                <xsl:comment>year equal</xsl:comment>
                <xsl:choose>
                    <xsl:when test="number(date/@month) > number($changes-since-month)">
                        <xsl:comment>month ok</xsl:comment>
                        <xsl:call-template name="print-change" />
                    </xsl:when>
                    <xsl:when test="number($changes-since-month) = number(date/@month)">
                        <xsl:comment>month equal</xsl:comment>
                        <xsl:if test="number(date/@day) >= number($changes-since-day) ">
                            <xsl:comment>day ok</xsl:comment>
                            <xsl:call-template name="print-change" />
                        </xsl:if>
                    </xsl:when>
                 </xsl:choose>
            </xsl:when>
         </xsl:choose>
         
    </xsl:template>
        
    <xsl:template name="print-change" >
        <xsl:text>
</xsl:text>
        <change>
            <xsl:attribute name="id"><xsl:call-template name="change-id"/></xsl:attribute>
            <xsl:attribute name="url"><xsl:value-of select="$changes-since-url" /></xsl:attribute>
            <xsl:copy-of select="*" />
        </change>
    </xsl:template>

</xsl:stylesheet>
