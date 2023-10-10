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
package org.netbeans.modules.css.editor.module.main.properties;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
import org.netbeans.modules.css.editor.module.main.CssModuleTestBase;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class PropertiesATest extends CssModuleTestBase {

//    private static final String[][] TEST_SETS = new String[][]{
//        {"http://www.w3.org/2008/site/css/minimum", "Min"},
//        {"http://www.w3.org/2008/site/css/advanced", "Adv"}
//    };

    public PropertiesATest(String name) {
        super(name);
    }

//    public void testGenerateTests() throws MalformedURLException, IOException {
//
//        StringBuilder code = new StringBuilder();
//
//        for (String[] set : TEST_SETS) {
//            URLConnection con = new URL(set[0]).openConnection();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
//            StringBuilder sb = new StringBuilder();
//            String line;
//            int i = 0;
//            while ((line = reader.readLine()) != null) {
//                i++;
//                String escapedLine = escape(line);
//
//                code.append("public void ");
//                code.append("testCase");
//                code.append(set[1]);
//                code.append(i);
//                code.append("() throws ParseException {\n");
//                code.append("\tString code = \"");
//                code.append(escapedLine);
//                code.append("\";\n");
//                code.append("\tassertCssCode(code);\n");
//                code.append("}\n");
//                code.append("\n");
//            }
//            reader.close();
//
//
//
//
//        }
//
//        System.out.println(code);
//
//    }
//
//    private static String escape(String s) {
//        s = s.replace("\\", "\\\\");
//        s = s.replace("\"", "\\\"");
//        return s;
//    }

    public void testCaseMin1() throws ParseException {
        String code = "@charset \"UTF-8\";/*!* Source in minimum-src.css */ html{color:#000;background:#FFF;}";
        assertCssCode(code);
    }

    public void testCaseMin2() throws ParseException {
        String code = "body,div,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,code,form,fieldset,legend,input,textarea,p,blockquote,th,td,address{margin:0;padding:0;}";
        assertCssCode(code);
    }

    public void testCaseMin3() throws ParseException {
        String code = "table{border-collapse:collapse;border-spacing:0;font-size:inherit;}";
        assertCssCode(code);
    }

    public void testCaseMin4() throws ParseException {
        String code = "fieldset,img{border:0;}";
        assertCssCode(code);
    }

    public void testCaseMin5() throws ParseException {
        String code = "address,caption,cite,code,dfn,em,strong,th,var{font-style:normal;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseMin6() throws ParseException {
        String code = "li{list-style:none;}";
        assertCssCode(code);
    }

    public void testCaseMin7() throws ParseException {
        String code = "caption,th{text-align:left;}";
        assertCssCode(code);
    }

    public void testCaseMin8() throws ParseException {
        String code = "h1,h2,h3,h4,h5,h6{font-size:100%;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseMin9() throws ParseException {
        String code = "q:before,q:after{content:'';}";
        assertCssCode(code);
    }

    public void testCaseMin10() throws ParseException {
        String code = "abbr,acronym{border:0;font-variant:normal;}";
        assertCssCode(code);
    }

    public void testCaseMin11() throws ParseException {
        String code = "sup{vertical-align:text-top;}";
        assertCssCode(code);
    }

    public void testCaseMin12() throws ParseException {
        String code = "sub{vertical-align:text-bottom;}";
        assertCssCode(code);
    }

    public void testCaseMin13() throws ParseException {
        String code = "input,textarea,select{font-family:inherit;font-size:inherit;font-weight:inherit;}";
        assertCssCode(code);
    }

    public void testCaseMin14() throws ParseException {
        String code = "legend{color:#000;}";
        assertCssCode(code);
    }

    public void testCaseMin15() throws ParseException {
        String code = "body{font-family:sans-serif;}";
        assertCssCode(code);
    }

    public void testCaseMin16() throws ParseException {
        String code = "pre,code,kbd,samp,tt{font-family:monospace;line-height:100%;}";
        assertCssCode(code);
    }

    public void testCaseMin17() throws ParseException {
        String code = "body{font-family:\"Helvetica Neue\",Helvetica,Arial,Verdana,Geneva,sans-serif;color:#333;}";
        assertCssCode(code);
    }

    public void testCaseMin18() throws ParseException {
        String code = "em{font-style:italic;}";
        assertCssCode(code);
    }

    public void testCaseMin19() throws ParseException {
        String code = "strong{font-weight:bold;}";
        assertCssCode(code);
    }

    public void testCaseMin20() throws ParseException {
        String code = "#w3c_mast h1{background-color:#005A9C;width:auto;position:static;text-align:left;float:left;padding:0;margin:0;height:auto;}";
        assertCssCode(code);
    }

    public void testCaseMin21() throws ParseException {
        String code = ".w3c_member #w3c_mast h1{background-color:#6D3792;}";
        assertCssCode(code);
    }

    public void testCaseMin22() throws ParseException {
        String code = ".w3c_team #w3c_mast h1{background-color:#A34425;}";
        assertCssCode(code);
    }

    public void testCaseMin23() throws ParseException {
        String code = "#w3c_nav{background-color:#eee;clear:both;}";
        assertCssCode(code);
    }

    public void testCaseMin24() throws ParseException {
        String code = "#w3c_mast{overflow:visible;}";
        assertCssCode(code);
    }

    public void testCaseMin25() throws ParseException {
        String code = "#w3c_mast img{display:inline;}";
        assertCssCode(code);
    }

    public void testCaseMin26() throws ParseException {
        String code = "#w3c_mast h1 a{background:none;display:inherit;float:none;height:auto;position:static;width:auto;}";
        assertCssCode(code);
    }

    public void testCaseMin27() throws ParseException {
        String code = "#w3c_mast h1 a img{padding:0;margin:0;float:left;display:block;}";
        assertCssCode(code);
    }

    public void testCaseMin28() throws ParseException {
        String code = ".w3c_member #w3c_mast h1 a{background:url('../images/logo_member_mobile') no-repeat top left;}";
        assertCssCode(code);
    }

    public void testCaseMin29() throws ParseException {
        String code = ".w3c_team #w3c_mast h1 a{background:url('../images/logo_team_mobile') no-repeat top left;}";
        assertCssCode(code);
    }

//    //Bug 206035
//    public void testCaseMin30() throws ParseException {
//        String code = "#search-form .text{background:#fff url(../images/google) no-repeat center left;min-width:2em;}";
//        assertCssCode(code);
//    }

    public void testCaseMin31() throws ParseException {
        String code = "#search-form .text:focus{background:#fff;}";
        assertCssCode(code);
    }

    public void testCaseMin32() throws ParseException {
        String code = "@media screen{.secondary_nav a{display:block;color:#000;text-decoration:none;} }";
        assertCssCode(code);
    }

    public void testCaseMin33() throws ParseException {
        String code = ".secondary_nav a:link,.secondary_nav a:visited{color:#888;}";
        assertCssCode(code);
    }

    public void testCaseMin34() throws ParseException {
        String code = ".secondary_nav{padding:5px 20px 0 0;font-size:88%;margin-bottom:10px;}";
        assertCssCode(code);
    }

    public void testCaseMin35() throws ParseException {
        String code = ".secondary_nav li{padding-left:5px;margin:0;}";
        assertCssCode(code);
    }

    public void testCaseMin36() throws ParseException {
        String code = ".secondary_nav li{display:-moz-inline-stack;display:inline-block;vertical-align:top;float:left;}";
        assertCssCode(code);
    }

    public void testCaseMin37() throws ParseException {
        String code = ".main_nav li{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin38() throws ParseException {
        String code = ".main_nav li.search-item{display:list-item;}";
        assertCssCode(code);
    }

    public void testCaseMin39() throws ParseException {
        String code = ".w3c_home .main_nav{display:block;}";
        assertCssCode(code);
    }

    public void testCaseMin40() throws ParseException {
        String code = ".main_nav a{padding:0;padding-right:1em;display:inline;}";
        assertCssCode(code);
    }

    public void testCaseMin41() throws ParseException {
        String code = ".main_nav{padding-top:0;padding-left:5px;float:none;clear:left;}";
        assertCssCode(code);
    }

    public void testCaseMin42() throws ParseException {
        String code = ".main_nav li{display:inline;float:none;margin:0;}";
        assertCssCode(code);
    }

    public void testCaseMin43() throws ParseException {
        String code = "#w3c_main{clear:both;background-image:none;}";
        assertCssCode(code);
    }

    public void testCaseMin44() throws ParseException {
        String code = "#w3c_logo_shadow,#w3c_crumbs br{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin45() throws ParseException {
        String code = ".bct{padding:5px;}";
        assertCssCode(code);
    }

    public void testCaseMin46() throws ParseException {
        String code = ".bct li{display:inline;}";
        assertCssCode(code);
    }

    public void testCaseMin47() throws ParseException {
        String code = ".bct li .cr{padding:0;}";
        assertCssCode(code);
    }

    public void testCaseMin48() throws ParseException {
        String code = ".bct .skip{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin49() throws ParseException {
        String code = "#w3c_footer{text-align:left;}";
        assertCssCode(code);
    }

    public void testCaseMin50() throws ParseException {
        String code = "#w3c_footer-inner{padding:5px;}";
        assertCssCode(code);
    }

    public void testCaseMin51() throws ParseException {
        String code = ".alt-logo{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin52() throws ParseException {
        String code = ".bar{padding-left:.25em;padding-right:.25em;}";
        assertCssCode(code);
    }

    public void testCaseMin53() throws ParseException {
        String code = ".w3c_footer-nav h3{font-weight:bold;font-size:119%;color:#17445F;text-transform:uppercase;}";
        assertCssCode(code);
    }

    public void testCaseMin54() throws ParseException {
        String code = ".w3c_footer-nav{margin-top:20px;}";
        assertCssCode(code);
    }

    public void testCaseMin55() throws ParseException {
        String code = ".w3c_leftCol{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin56() throws ParseException {
        String code = ".w3c_home .w3c_leftCol{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin57() throws ParseException {
        String code = ".w3c_member #w3c_footer-inner{background-image:none;}";
        assertCssCode(code);
    }

    public void testCaseMin58() throws ParseException {
        String code = ".w3c_member #w3c_acl{text-transform:uppercase;letter-spacing:.2em;width:auto;text-align:right;padding:5px 10px 0 5px;float:left;font-size:81%;}";
        assertCssCode(code);
    }

    public void testCaseMin59() throws ParseException {
        String code = ".w3c_member_only{margin-top:-60px;}";
        assertCssCode(code);
    }

    public void testCaseMin60() throws ParseException {
        String code = "#w3c_crumbs{background:none;}";
        assertCssCode(code);
    }

    public void testCaseMin61() throws ParseException {
        String code = ".w3c_member #w3c_crumbs{background:none;}";
        assertCssCode(code);
    }

    public void testCaseMin62() throws ParseException {
        String code = "#w3c_footer{padding-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin63() throws ParseException {
        String code = ".w3c_mainCol,#w3c_footer-inner,#w3c_crumbs_frame{border-left:none;}";
        assertCssCode(code);
    }

    public void testCaseMin64() throws ParseException {
        String code = "#w3c_crumbs_frame{margin-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin65() throws ParseException {
        String code = ".line{margin-bottom:10px;}";
        assertCssCode(code);
    }

    public void testCaseMin66() throws ParseException {
        String code = ".w3c_javascript.w3c_handheld .line .size2on3{margin:0;}";
        assertCssCode(code);
    }

    public void testCaseMin67() throws ParseException {
        String code = ".size1on1,.size1on2,.size1on3,.size2on3,.size1on4,.size3on4{float:none;width:auto;}";
        assertCssCode(code);
    }

    public void testCaseMin68() throws ParseException {
        String code = ".lastUnit{float:none;}";
        assertCssCode(code);
    }

    public void testCaseMin69() throws ParseException {
        String code = ".intro{font-size:100%;line-height:1.2em;}";
        assertCssCode(code);
    }

    public void testCaseMin70() throws ParseException {
        String code = ".offscreen{position:absolute;left:-1000em;}";
        assertCssCode(code);
    }

    public void testCaseMin71() throws ParseException {
        String code = ".tPadding0{padding-top:0!important;}";
        assertCssCode(code);
    }

    public void testCaseMin72() throws ParseException {
        String code = ".rPadding0{padding-right:0!important;}";
        assertCssCode(code);
    }

    public void testCaseMin73() throws ParseException {
        String code = ".bPadding0{padding-bottom:0!important;}";
        assertCssCode(code);
    }

    public void testCaseMin74() throws ParseException {
        String code = ".lPadding0{padding-left:0!important;}";
        assertCssCode(code);
    }

    public void testCaseMin75() throws ParseException {
        String code = ".padding{padding:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin76() throws ParseException {
        String code = ".tPadding{padding-top:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin77() throws ParseException {
        String code = ".rPadding{padding-right:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin78() throws ParseException {
        String code = ".bPadding{padding-bottom:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin79() throws ParseException {
        String code = ".lPadding{padding-left:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin80() throws ParseException {
        String code = ".lPaddingLg{padding-left:40px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin81() throws ParseException {
        String code = ".tPaddingLg{padding-top:40px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin82() throws ParseException {
        String code = ".rPaddingLg{padding-right:40px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin83() throws ParseException {
        String code = ".bPaddingLg{padding-bottom:40px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin84() throws ParseException {
        String code = ".tMargin0{margin-top:0!important;}";
        assertCssCode(code);
    }

    public void testCaseMin85() throws ParseException {
        String code = ".rMargin0{margin-right:0!important;}";
        assertCssCode(code);
    }

    public void testCaseMin86() throws ParseException {
        String code = ".bMargin0{margin-bottom:0!important;}";
        assertCssCode(code);
    }

    public void testCaseMin87() throws ParseException {
        String code = ".lMargin0{margin-left:0!important;}";
        assertCssCode(code);
    }

    public void testCaseMin88() throws ParseException {
        String code = ".tMargin{margin-top:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin89() throws ParseException {
        String code = ".rMargin{margin-right:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin90() throws ParseException {
        String code = ".bMargin{margin-bottom:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin91() throws ParseException {
        String code = ".lMargin{margin-left:20px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin92() throws ParseException {
        String code = ".tMarginLg{margin-top:40px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin93() throws ParseException {
        String code = ".rMarginLg{margin-right:40px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin94() throws ParseException {
        String code = ".bMarginLg{margin-bottom:40px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin95() throws ParseException {
        String code = ".lMarginLg{margin-left:40px!important;}";
        assertCssCode(code);
    }

    public void testCaseMin96() throws ParseException {
        String code = "h2,.h2{margin-top:20px;}";
        assertCssCode(code);
    }

    public void testCaseMin97() throws ParseException {
        String code = "h1,h2,h3,h4,h5,ul,ol,dl,p,pre,blockquote{padding:5px 5px 0;}";
        assertCssCode(code);
    }

    public void testCaseMin98() throws ParseException {
        String code = "table h4,table p,table ul,table ol{padding:0;}";
        assertCssCode(code);
    }

    public void testCaseMin99() throws ParseException {
        String code = "ul ul,ol ol{padding:0 20px;}";
        assertCssCode(code);
    }

    public void testCaseMin100() throws ParseException {
        String code = "dt{padding:0 20px;}";
        assertCssCode(code);
    }

    public void testCaseMin101() throws ParseException {
        String code = "dd{padding:0 20px 10px;}";
        assertCssCode(code);
    }

    public void testCaseMin102() throws ParseException {
        String code = ".block{margin:20px;}";
        assertCssCode(code);
    }

    public void testCaseMin103() throws ParseException {
        String code = "hr{border:5px solid #BCBCBC;border-width:0 0 5px;margin:20px 20px 0;}";
        assertCssCode(code);
    }

    public void testCaseMin104() throws ParseException {
        String code = "h1,.h1{font-size:131%;font-weight:bold;font-style:normal;color:#025A9A;}";
        assertCssCode(code);
    }

    public void testCaseMin105() throws ParseException {
        String code = "h2,.h2{font-size:125%;font-weight:bold;font-style:normal;color:#036;}";
        assertCssCode(code);
    }

    public void testCaseMin106() throws ParseException {
        String code = "h3,.h3{font-size:113%;font-weight:normal;font-style:normal;color:#025A9A;}";
        assertCssCode(code);
    }

    public void testCaseMin107() throws ParseException {
        String code = "h4,.h4{font-size:106%;font-weight:bold;font-style:normal;color:#036;}";
        assertCssCode(code);
    }

    public void testCaseMin108() throws ParseException {
        String code = "h5,.h5{font-size:106%;font-weight:bold;font-style:normal;color:#930;}";
        assertCssCode(code);
    }

    public void testCaseMin109() throws ParseException {
        String code = ".title{padding:0 5px;background:#DBE7F0;font-weight:normal;letter-spacing:-0.05em;text-transform:uppercase;color:#000;}";
        assertCssCode(code);
    }

    public void testCaseMin110() throws ParseException {
        String code = ".w3c_member .title{background:#F8EFFF;border-bottom:1px solid #E2D1EF;}";
        assertCssCode(code);
    }

    public void testCaseMin111() throws ParseException {
        String code = ".w3c_team .title{background:#FFEADF;border-bottom:1px solid #DFA999;}";
        assertCssCode(code);
    }

    public void testCaseMin112() throws ParseException {
        String code = "h1.title{letter-spacing:.01em;}";
        assertCssCode(code);
    }

    public void testCaseMin113() throws ParseException {
        String code = "h1.title img{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin114() throws ParseException {
        String code = ".category a{color:#333;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseMin115() throws ParseException {
        String code = "a{color:#036;}";
        assertCssCode(code);
    }

    public void testCaseMin116() throws ParseException {
        String code = "a img{cursor:pointer;}";
        assertCssCode(code);
    }

    public void testCaseMin117() throws ParseException {
        String code = "a:visited{color:#006EC6;}";
        assertCssCode(code);
    }

    public void testCaseMin118() throws ParseException {
        String code = "a:link,a:visited,a:hover{text-decoration:none;}";
        assertCssCode(code);
    }

    public void testCaseMin119() throws ParseException {
        String code = "a:active{outline:0 none;position:relative;top:1px;}";
        assertCssCode(code);
    }

    public void testCaseMin120() throws ParseException {
        String code = ".logo a:active{top:0;}";
        assertCssCode(code);
    }

    public void testCaseMin121() throws ParseException {
        String code = ".theme_ext h2.w3c_topic a:active{position:static;}";
        assertCssCode(code);
    }

    public void testCaseMin122() throws ParseException {
        String code = "a:link,a:visited{border-bottom:2px solid #A8BFCF;padding-bottom:1px;}";
        assertCssCode(code);
    }

    public void testCaseMin123() throws ParseException {
        String code = "a.no-border:link,a.no-border:visited,a.no-border:hover{border:none;}";
        assertCssCode(code);
    }

    public void testCaseMin124() throws ParseException {
        String code = ".menu a:link,.menu a:visited{color:#036;border-bottom:transparent 2px solid;}";
        assertCssCode(code);
    }

    public void testCaseMin125() throws ParseException {
        String code = ".w3c_leftCol a:link,.w3c_leftCol a:visited,ul.theme li a:link,ul.theme li a:visited{border-bottom:none;}";
        assertCssCode(code);
    }

    public void testCaseMin126() throws ParseException {
        String code = ".bct a:link,.bct a:visited{border-bottom:none;padding-bottom:0;}";
        assertCssCode(code);
    }

    public void testCaseMin127() throws ParseException {
        String code = "h1 a:link,.h1 a:link,h2 a:link,.h2 a:link,h3 a:link,.h3 a:link,h4 a:link,.h4 a:link{font-weight:normal;border-bottom:transparent 2px solid;}";
        assertCssCode(code);
    }

    public void testCaseMin128() throws ParseException {
        String code = "h5 a:link,.h5 a:link{color:#930;font-weight:normal;border-bottom:transparent 2px solid;}";
        assertCssCode(code);
    }

    public void testCaseMin129() throws ParseException {
        String code = ".h1 a:visited,h1 a:visited,.h2 a:visited,h2 a:visited,.h3 a:visited,h3 a:visited,.h4 a:visited,h4 a:visited{font-weight:normal;color:#036;border-bottom:transparent 2px solid;}";
        assertCssCode(code);
    }

    public void testCaseMin130() throws ParseException {
        String code = ".h5 a:visited,h5 a:visited{font-weight:normal;color:#930;border-bottom:transparent 2px solid;}";
        assertCssCode(code);
    }

    public void testCaseMin131() throws ParseException {
        String code = ".w3c_toc a:link,.w3c_toc a:visited,.data a:link,.data a:visited{border-bottom:transparent 2px solid;}";
        assertCssCode(code);
    }

    public void testCaseMin132() throws ParseException {
        String code = ".w3c_leftCol a:link:after,.w3c_leftCol a:visited:after,table a:link:after,table a:visited:after,.expand_section a:link:after,.expand_section a:visited:after{content:none;}";
        assertCssCode(code);
    }

    public void testCaseMin133() throws ParseException {
        String code = "a:hover,.main_nav a:hover,.menu a:hover,h1 a:hover,.h1 a:hover,h2 a:hover,.h2 a:hover,h3 a:hover,.h3 a:hover,h4 a:hover,.h4 a:hover,h5 a:hover,.h5 a:hover,.w3c_toc a:hover,.data a:hover,.bct a:hover{border-bottom:#005A9C 2px solid;}";
        assertCssCode(code);
    }

    public void testCaseMin134() throws ParseException {
        String code = ".w3c_leftCol a:hover,.w3c_leftCol li a:focus{background-color:#fafafa;-moz-transition-property:background-color;-webkit-transition-property:background-color;-o-transition-property:background-color;-moz-transition-duration:.3s;-webkit-transition-duration:.3s;-o-transition-duration:.3s;}";
        assertCssCode(code);
    }

    public void testCaseMin135() throws ParseException {
        String code = ".w3c_javascript.w3c_handheld .headline,.w3c_javascript.w3c_handheld .headline h3.h4,.w3c_javascript.w3c_handheld #w3c_most-recently h3 a{border:none;margin:0;background-color:#F1F7FB;font-weight:bold;}";
        assertCssCode(code);
    }

    public void testCaseMin136() throws ParseException {
        String code = "ol.show_items li,.entry ol li{list-style-type:decimal;}";
        assertCssCode(code);
    }

    public void testCaseMin137() throws ParseException {
        String code = "ul.show_items li,.entry ul li{list-style-type:disc;}";
        assertCssCode(code);
    }

    public void testCaseMin138() throws ParseException {
        String code = "ol.show_items li,.entry ol li,ul.show_items li,.entry ul li{margin-left:20px;}";
        assertCssCode(code);
    }

    public void testCaseMin139() throws ParseException {
        String code = ".vevent_list,.hentry_list{padding:0;}";
        assertCssCode(code);
    }

    public void testCaseMin140() throws ParseException {
        String code = ".vevent_list .location,.vevent_list .eventtitle,.vevent_list .person,.hentry_list .entry-title{text-align:left;padding:0 5px;margin-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin141() throws ParseException {
        String code = ".vevent_list .summary{padding-bottom:0;}";
        assertCssCode(code);
    }

    public void testCaseMin142() throws ParseException {
        String code = ".w3c_events_talks .vevent_list li{padding-bottom:30px;}";
        assertCssCode(code);
    }

    public void testCaseMin143() throws ParseException {
        String code = ".theme_ext{padding:0;}";
        assertCssCode(code);
    }

    public void testCaseMin144() throws ParseException {
        String code = ".theme_ext li.theme_ext_item{position:relative;padding-left:0;min-height:0;}";
        assertCssCode(code);
    }

    public void testCaseMin145() throws ParseException {
        String code = ".theme_ext .icon{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin146() throws ParseException {
        String code = "h4.w3c_status_title{padding-bottom:0;}";
        assertCssCode(code);
    }

    public void testCaseMin147() throws ParseException {
        String code = "p.rec_support_data{font-size:75%;padding-bottom:5px;padding-top:0;}";
        assertCssCode(code);
    }

    public void testCaseMin148() throws ParseException {
        String code = "p.rec_support_data a{display:inline;}";
        assertCssCode(code);
    }

    public void testCaseMin149() throws ParseException {
        String code = ".date{margin:0 20px 0 5px;padding:2px 0 0;}";
        assertCssCode(code);
    }

    public void testCaseMin150() throws ParseException {
        String code = ".w3c_events_talks .date{padding:0;float:left;text-align:right;}";
        assertCssCode(code);
    }

    public void testCaseMin151() throws ParseException {
        String code = ".w3c_events_talks .info-wrap{clear:both;}";
        assertCssCode(code);
    }

    public void testCaseMin152() throws ParseException {
        String code = ".w3c_events_talks .date .dtstart .year{float:right;margin-left:10px;}";
        assertCssCode(code);
    }

    public void testCaseMin153() throws ParseException {
        String code = ".date .mm-dd,.date .dtend .year,.date .paren{display:none;}";
        assertCssCode(code);
    }

    public void testCaseMin154() throws ParseException {
        String code = ".w3c_javascript.w3c_handheld .date{font-weight:bold;}";
        assertCssCode(code);
    }

    public void testCaseMin155() throws ParseException {
        String code = ".w3c_events_talks .date .date-separator{display:inline;}";
        assertCssCode(code);
    }

    public void testCaseMin156() throws ParseException {
        String code = ".entry .summary,.vevent .summary{padding-top:0;}";
        assertCssCode(code);
    }

    public void testCaseMin157() throws ParseException {
        String code = ".data{padding:20px 0;position:relative;vertical-align:top;border-right:solid 1px transparent;}";
        assertCssCode(code);
    }

    public void testCaseMin158() throws ParseException {
        String code = ".data table{width:100%;border-top:3px solid #BCBCBC;}";
        assertCssCode(code);
    }

    public void testCaseMin159() throws ParseException {
        String code = "th,td{vertical-align:top;}";
        assertCssCode(code);
    }

    public void testCaseMin160() throws ParseException {
        String code = ".data th,.data td{border-right:1px solid #FFF;border-bottom:1px solid #FFF;padding:5px 20px;}";
        assertCssCode(code);
    }

    public void testCaseMin161() throws ParseException {
        String code = ".data .lastColumn{border-right:none;}";
        assertCssCode(code);
    }

    public void testCaseMin162() throws ParseException {
        String code = ".data .lastRow td{border-bottom:none;}";
        assertCssCode(code);
    }

    public void testCaseMin163() throws ParseException {
        String code = ".data tr.even{background-color:#f8f8f8;}";
        assertCssCode(code);
    }

    public void testCaseMin164() throws ParseException {
        String code = ".data tr.odd{background-color:#E2E2E2;}";
        assertCssCode(code);
    }

    public void testCaseMin165() throws ParseException {
        String code = ".data tbody tr:nth-child(even){background-color:#f8f8f8;}";
        assertCssCode(code);
    }

    public void testCaseMin166() throws ParseException {
        String code = ".data tbody tr:nth-child(odd){background-color:#E2E2E2;}";
        assertCssCode(code);
    }

    public void testCaseMin167() throws ParseException {
        String code = ".data th{color:#000;font-weight:bold;}";
        assertCssCode(code);
    }

    public void testCaseMin168() throws ParseException {
        String code = ".spec{padding:20px;}";
        assertCssCode(code);
    }

    public void testCaseMin169() throws ParseException {
        String code = ".spec th,.spec td{border:1px solid #aaa;border-width:1px 0;padding-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin170() throws ParseException {
        String code = ".spec tbody tr:nth-child(even),.spec tbody tr:nth-child(odd){background-color:#fff;}";
        assertCssCode(code);
    }

    public void testCaseMin171() throws ParseException {
        String code = ".data .table_datecol{width:12%;}";
        assertCssCode(code);
    }

    public void testCaseMin172() throws ParseException {
        String code = ".data .table_labelcol{width:20%;}";
        assertCssCode(code);
    }

    public void testCaseMin173() throws ParseException {
        String code = ".data .table_titlecol{width:60%;}";
        assertCssCode(code);
    }

    public void testCaseMin174() throws ParseException {
        String code = ".w3c_spec_summary_table td{padding:3px 1em 0;}";
        assertCssCode(code);
    }

    public void testCaseMin175() throws ParseException {
        String code = ".w3c_spec_summary_table td .expand_description{padding-bottom:1em;}";
        assertCssCode(code);
    }

    public void testCaseMin176() throws ParseException {
        String code = ".w3c_spec_summary_table .table_datecol{width:5.5em;}";
        assertCssCode(code);
    }

    public void testCaseMin177() throws ParseException {
        String code = ".w3c_more_recent_status{font-size:75%;margin-top:0;margin-bottom:5px;padding-top:0;}";
        assertCssCode(code);
    }

    public void testCaseMin178() throws ParseException {
        String code = ".w3c_more_recent_status a{background-color:#FFEA6F;color:#000;}";
        assertCssCode(code);
    }

    public void testCaseMin179() throws ParseException {
        String code = ".tr_view_nav{margin-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin180() throws ParseException {
        String code = "input{font-size:inherit;font-family:inherit;}";
        assertCssCode(code);
    }

    public void testCaseMin181() throws ParseException {
        String code = ".button{border:0;-moz-border-radius:3px;-moz-box-shadow:0 1px 3px rgba(0,0,0,0.5);-webkit-border-radius:3px;-webkit-box-shadow:0 1px 3px rgba(0,0,0,0.5);border-bottom:1px solid rgba(0,0,0,0.25);background-color:#555;color:#FFF;cursor:pointer;font-size:81%;font-weight:bold;left:5px;padding:2px 5px;position:relative;text-shadow:0 -1px 1px rgba(0,0,0,0.25);}";
        assertCssCode(code);
    }

    public void testCaseMin182() throws ParseException {
        String code = ".button:hover{background-color:#3A80B3;color:#fff;}";
        assertCssCode(code);
    }

    public void testCaseMin183() throws ParseException {
        String code = ".button:active{top:1px;}";
        assertCssCode(code);
    }

    public void testCaseMin184() throws ParseException {
        String code = "textarea,select{max-width:100%;}";
        assertCssCode(code);
    }

    public void testCaseMin185() throws ParseException {
        String code = ".w3c_javascript.w3c_handheld input.text{width:80%;}";
        assertCssCode(code);
    }

    public void testCaseMin186() throws ParseException {
        String code = ".w3c_javascript.w3c_handheld input.submit{font-size:88%;padding:.2em .2em .2em .4em;position:relative;right:35px;top:5px;}";
        assertCssCode(code);
    }

    public void testCaseMin187() throws ParseException {
        String code = "#search-form button{font-size:88%;padding:0;position:relative;right:45px;top:2px;border-style:none;background:none;}";
        assertCssCode(code);
    }

    public void testCaseMin188() throws ParseException {
        String code = "#region_form select{width:80%;font-size:94%;}";
        assertCssCode(code);
    }

    public void testCaseMin189() throws ParseException {
        String code = ".w3c_toggle_form{width:15em;border:1px #DBE7F0 solid;padding:5px;}";
        assertCssCode(code);
    }

    public void testCaseMin190() throws ParseException {
        String code = ".w3c_javascript .expand_block h3{padding-left:5px;}";
        assertCssCode(code);
    }

    public void testCaseMin191() throws ParseException {
        String code = ".w3c_javascript.w3c_handheld .expand_block h3{border-top:1px #e2e2e2 solid;}";
        assertCssCode(code);
    }

    public void testCaseMin192() throws ParseException {
        String code = ".w3c_javascript .expand_block h4{padding-left:5px;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseMin193() throws ParseException {
        String code = ".w3c_javascript .expand_block .expand_description{padding-bottom:5px;margin-bottom:20px;background:none;border:none;display:block;}";
        assertCssCode(code);
    }

    public void testCaseMin194() throws ParseException {
        String code = ".w3c_javascript.w3c_handheld .expand_block .expand_section{background:none;padding-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin195() throws ParseException {
        String code = ".hierarchy{list-style:none;}";
        assertCssCode(code);
    }

    public void testCaseMin196() throws ParseException {
        String code = "li.top{clear:both;margin-left:1em;}";
        assertCssCode(code);
    }

    public void testCaseMin197() throws ParseException {
        String code = ".menu{padding:0 20px;}";
        assertCssCode(code);
    }

    public void testCaseMin198() throws ParseException {
        String code = ".menu.expand_block{padding-left:20px;}";
        assertCssCode(code);
    }

    public void testCaseMin199() throws ParseException {
        String code = ".menu h2.h4{font-size:100%;color:#666;background:#fff;letter-spacing:1.1px;padding-left:0;border-bottom:1px solid #ddd;}";
        assertCssCode(code);
    }

    public void testCaseMin200() throws ParseException {
        String code = ".menu.expand_block h2{padding-top:0;padding-left:20px;margin-top:20px;margin-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin201() throws ParseException {
        String code = ".w3c_javascript.w3c_handheld .expand_block.menu .expand_description,.expand_block.menu .expand_description{margin-left:0;padding-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin202() throws ParseException {
        String code = ".menu ul{padding-top:.2em;padding-left:0;padding-right:0;}";
        assertCssCode(code);
    }

    public void testCaseMin204() throws ParseException {
        String code = "#request_form fieldset p{padding:0;color:#666;}";
        assertCssCode(code);
    }

    public void testCaseMin205() throws ParseException {
        String code = "#request_form fieldset textarea,.errors{width:85%;}";
        assertCssCode(code);
    }

    public void testCaseMin206() throws ParseException {
        String code = ".errors h3{background-image:none;padding:0;}";
        assertCssCode(code);
    }

    public void testCaseMin207() throws ParseException {
        String code = ".errors ul{margin-left:0;}";
        assertCssCode(code);
    }

    public void testCaseMin208() throws ParseException {
        String code = "/*!* Many thanks to Sorin Stefan and Nicole Sullivan!*(Refers to section A. Libraries) * Copyright(c) 2007,Yahoo!Inc. All rights reserved. * Code licensed under the BSD License:* http://developer.yahoo.net/yui/license.txt * version:2.2.1 */";
        assertCssCode(code);
    }

//    //Bug 206035
//    public void testCaseAdv1() throws ParseException {
//        String code = "/*!* Source in advanced-src.css */ body{font-size:.82em;background:#fff url(../images/page/page_bkg.jpg) repeat-x top left;}";
//        assertCssCode(code);
//    }

//    //Bug 206035
//    public void testCaseAdv2() throws ParseException {
//        String code = "body.w3c_member{background-image:url(../images/page/page_bkg_member.jpg);}";
//        assertCssCode(code);
//    }

//    //Bug 206035
//    public void testCaseAdv3() throws ParseException {
//        String code = "body.w3c_team{background-image:url(../images/page/page_bkg_team.jpg);}";
//        assertCssCode(code);
//    }

    public void testCaseAdv4() throws ParseException {
        String code = "#w3c_container{margin-right:2%;font-size:108%;line-height:1.41667;}";
        assertCssCode(code);
    }

    public void testCaseAdv5() throws ParseException {
        String code = "#w3c_mast{overflow:hidden;}";
        assertCssCode(code);
    }

    public void testCaseAdv6() throws ParseException {
        String code = "#w3c_mast p{font-style:italic;color:#424242;padding:20px 30px;}";
        assertCssCode(code);
    }

    public void testCaseAdv7() throws ParseException {
        String code = "#w3c_mast h1{float:left;padding:0;text-align:right;height:107px;}";
        assertCssCode(code);
    }

    public void testCaseAdv8() throws ParseException {
        String code = "#w3c_mast h1 a{display:block;float:left;background:url('../images/logo-w3c-screen-lg') no-repeat top left;width:100%;height:107px;position:relative;z-index:1;}";
        assertCssCode(code);
    }

    public void testCaseAdv9() throws ParseException {
        String code = "#w3c_mast h1 a:hover{border:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv10() throws ParseException {
        String code = "#w3c_mast h1 a img{display:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv11() throws ParseException {
        String code = ".w3c_member #w3c_mast h1 a,.w3c_member #w3c_mast h1 a:hover,.w3c_team #w3c_mast h1 a,.w3c_team #w3c_mast h1 a:hover{border:0;text-decoration:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv12() throws ParseException {
        String code = ".w3c_member #w3c_mast h1 a{background-image:url('../images/logo-w3c-member-lg');}";
        assertCssCode(code);
    }

    public void testCaseAdv13() throws ParseException {
        String code = ".w3c_team #w3c_mast h1 a{background-image:url('../images/logo-w3c-team-lg');}";
        assertCssCode(code);
    }

    public void testCaseAdv14() throws ParseException {
        String code = ".alt-logo{display:block;position:absolute;left:20px;z-index:0;background-color:#fff;}";
        assertCssCode(code);
    }

    public void testCaseAdv15() throws ParseException {
        String code = "#w3c_mast img{display:block;vertical-align:top;}";
        assertCssCode(code);
    }

    public void testCaseAdv16() throws ParseException {
        String code = "#w3c_nav{clear:none;overflow:hidden;}";
        assertCssCode(code);
    }

    public void testCaseAdv17() throws ParseException {
        String code = "#w3c_nav form#region_form{float:right;margin-right:20px;margin-top:8px;}";
        assertCssCode(code);
    }

    public void testCaseAdv18() throws ParseException {
        String code = "#w3c_nav form#region_form select{display:block;width:14.3em;max-width:14.3em;color:#333;border:1px solid #d1d1d1;float:left;}";
        assertCssCode(code);
    }

    public void testCaseAdv19() throws ParseException {
        String code = "#w3c_nav form#region_form select option{max-height:19px;overflow:hidden;}";
        assertCssCode(code);
    }

    public void testCaseAdv20() throws ParseException {
        String code = "#region_form input.button{display:inline;}";
        assertCssCode(code);
    }

    public void testCaseAdv21() throws ParseException {
        String code = ".main_nav{display:block;width:98%;margin-left:2.4%;float:left;padding:27px 0 0;text-shadow:0 1px 1px #FFF;}";
        assertCssCode(code);
    }

    public void testCaseAdv22() throws ParseException {
        String code = ".main_nav a,.main_nav a:link,.main_nav span{font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseAdv23() throws ParseException {
        String code = ".main_nav a,.main_nav span{display:block;}";
        assertCssCode(code);
    }

    public void testCaseAdv24() throws ParseException {
        String code = ".main_nav a,.main_nav span{padding:10px 10px 12px;border-left:1px solid #ddd;border-right:1px solid #fff;color:#0C3D5F;border-bottom:none;text-decoration:none;text-transform:uppercase;}";
        assertCssCode(code);
    }

    public void testCaseAdv25() throws ParseException {
        String code = ".main_nav a:hover,.main_nav a:focus,.main_nav a.current{color:#333;background-color:#fafafa;border-bottom:none;-webkit-transition:all .3s ease-out;-moz-transition:all .3s ease-out;-o-transition:all .3s ease-out;transition:all .3s ease-out;}";
        assertCssCode(code);
    }

    public void testCaseAdv26() throws ParseException {
        String code = ".main_nav a,.main_nav a:link,.main_nav a:hover,.main_nav a:active,.main_nav a:visited{padding:10px 10px 12px;}";
        assertCssCode(code);
    }

    public void testCaseAdv27() throws ParseException {
        String code = ".main_nav li{float:left;text-align:center;}";
        assertCssCode(code);
    }

    public void testCaseAdv28() throws ParseException {
        String code = ".main_nav li.last-item a{border-right:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv29() throws ParseException {
        String code = ".main_nav li.first-item a{border-left:0 none;text-align:left;}";
        assertCssCode(code);
    }

    public void testCaseAdv30() throws ParseException {
        String code = ".main_nav li.search-item{width:20%;float:right;margin-right:20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv31() throws ParseException {
        String code = ".secondary_nav{margin-top:10px;margin-bottom:0;margin-left:3.2%;padding:5px 7px;float:left;font-size:88%;background:#e8e7e7;-webkit-box-shadow:0 1px 2px #fff;-moz-box-shadow:0 1px 2px #fff;-webkit-border-radius:5px;-moz-border-radius:5px;border-top:1px solid #bbb;border-left:1px solid #bbb;}";
        assertCssCode(code);
    }

    public void testCaseAdv32() throws ParseException {
        String code = ".secondary_nav li{display:inline;display:inline-block;padding-left:10px;float:left;}";
        assertCssCode(code);
    }

    public void testCaseAdv33() throws ParseException {
        String code = ".secondary_nav li.label{padding-left:5px;padding-right:3px;}";
        assertCssCode(code);
    }

    public void testCaseAdv34() throws ParseException {
        String code = ".secondary_nav li a:hover{text-decoration:none;border:0;color:#000;}";
        assertCssCode(code);
    }

    public void testCaseAdv35() throws ParseException {
        String code = "#search-form{border-bottom:1px solid #d1d1d1;border-left:1px solid #d1d1d1;border-right:1px solid #d1d1d1;border-top:1px solid #b6b6b6;background:url('../images/search-bg.png') repeat-x top left;height:28px;width:100%;float:right;clear:both;position:relative;-webkit-border-radius:5px;-moz-border-radius:5px;}";
        assertCssCode(code);
    }

    public void testCaseAdv36() throws ParseException {
        String code = "#search-form input.text{border:none;color:#333;float:left;font-size:131%;margin-left:2px;margin-top:4px;width:70%;}";
        assertCssCode(code);
    }

    public void testCaseAdv37() throws ParseException {
        String code = "#search-form button{position:absolute;right:3px;top:6px;vertical-align:middle;}";
        assertCssCode(code);
    }

    public void testCaseAdv38() throws ParseException {
        String code = "#search-form button img.submit{float:right;}";
        assertCssCode(code);
    }

    public void testCaseAdv39() throws ParseException {
        String code = "#w3c_main{background:#eee;overflow:hidden;}";
        assertCssCode(code);
    }

    public void testCaseAdv40() throws ParseException {
        String code = "#w3c_logo_shadow{overflow:hidden;display:block;}";
        assertCssCode(code);
    }

    public void testCaseAdv41() throws ParseException {
        String code = "#w3c_logo_shadow img{width:100%;display:block;}";
        assertCssCode(code);
    }

    public void testCaseAdv42() throws ParseException {
        String code = "#w3c_main p,#w3c_main li{line-height:1.5;}";
        assertCssCode(code);
    }

    public void testCaseAdv43() throws ParseException {
        String code = "#w3c_main li.vevent .date,#w3c_main li.hentry .date{border-bottom:1px solid #E2E2E2;margin-bottom:18px;padding-bottom:4px;}";
        assertCssCode(code);
    }

    public void testCaseAdv44() throws ParseException {
        String code = "#w3c_main li.vevent p.summary a,#w3c_main li.vevent p.summary a:link,#w3c_main li.vevent p.summary a:visited,#w3c_main li.hentry p.entry-title a,#w3c_main li.hentry p.entry-title a:link,#w3c_main li.hentry p.entry-title a:visited{border:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv45() throws ParseException {
        String code = "#w3c_main li.vevent p.summary a:hover,#w3c_main li.hentry p.entry-title a:hover{border-bottom:2px solid #005A9C;}";
        assertCssCode(code);
    }

    public void testCaseAdv46() throws ParseException {
        String code = "#w3c_main p.about{color:#23668F;margin-top:.7%;padding-top:0;font-size:91%;}";
        assertCssCode(code);
    }

    public void testCaseAdv47() throws ParseException {
        String code = "#w3c_main .bct{max-width:none;font-size:94%;}";
        assertCssCode(code);
    }

    public void testCaseAdv48() throws ParseException {
        String code = "#w3c_main .bct li.current{padding:0 0 9px;}";
        assertCssCode(code);
    }

    public void testCaseAdv49() throws ParseException {
        String code = ".w3c_public #w3c_main .bct li.current{background:#fff url('../images/bct.png') no-repeat bottom center;}";
        assertCssCode(code);
    }

    public void testCaseAdv50() throws ParseException {
        String code = ".w3c_member #w3c_main .bct li.current{background:#fff url('../images/bct-member.png') no-repeat bottom center;}";
        assertCssCode(code);
    }

    public void testCaseAdv51() throws ParseException {
        String code = ".w3c_team #w3c_main .bct li.current{background:#fff url('../images/bct-team.png') no-repeat bottom center;}";
        assertCssCode(code);
    }

    public void testCaseAdv52() throws ParseException {
        String code = ".bct{padding:4px 20px 7px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv53() throws ParseException {
        String code = ".bct li .cr{padding:0 .83em;}";
        assertCssCode(code);
    }

    public void testCaseAdv54() throws ParseException {
        String code = "#w3c_crumbs br{display:inline;}";
        assertCssCode(code);
    }

    public void testCaseAdv55() throws ParseException {
        String code = "#w3c_crumbs_frame{background-color:#fff;}";
        assertCssCode(code);
    }

    public void testCaseAdv56() throws ParseException {
        String code = ".bct .skip{display:inline;background:#fff url('../images/skip.png') no-repeat center;text-align:center;width:55px;float:right;position:relative;left:10px;bottom:3px;}";
        assertCssCode(code);
    }

    public void testCaseAdv57() throws ParseException {
        String code = ".bct .skip a{color:#333;font-size:85%;}";
        assertCssCode(code);
    }

    public void testCaseAdv58() throws ParseException {
        String code = "#w3c_footer{text-align:center;background:#fff url('../images/footer-shadow.png') no-repeat top center;width:100%;}";
        assertCssCode(code);
    }

    public void testCaseAdv59() throws ParseException {
        String code = "#w3c_footer a:hover{text-decoration:underline;}";
        assertCssCode(code);
    }

    public void testCaseAdv60() throws ParseException {
        String code = "#w3c_footer-inner{padding:30px 0 20px;max-width:600px;margin:0 auto;}";
        assertCssCode(code);
    }

    public void testCaseAdv61() throws ParseException {
        String code = "#w3c_footer-inner ul{text-align:left;}";
        assertCssCode(code);
    }

    public void testCaseAdv62() throws ParseException {
        String code = ".w3c_footer-nav{float:left;margin-left:23px;margin-top:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv63() throws ParseException {
        String code = ".w3c_footer-nav h3{text-align:left;}";
        assertCssCode(code);
    }

    public void testCaseAdv64() throws ParseException {
        String code = ".w3c_footer-nav ul{padding:10px 20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv65() throws ParseException {
        String code = ".w3c_footer-nav a:hover img.social-icon{-moz-opacity:.6;-khtml-opacity:.6;opacity:.6;}";
        assertCssCode(code);
    }

    public void testCaseAdv66() throws ParseException {
        String code = ".copyright{clear:both;color:#333;font-size:94%;padding-top:30px;}";
        assertCssCode(code);
    }

    public void testCaseAdv67() throws ParseException {
        String code = "#w3c_footer a:link,#w3c_footer a:visited{border-bottom:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv68() throws ParseException {
        String code = ".w3c_home .w3c_leftCol{display:block;float:left;padding-top:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv69() throws ParseException {
        String code = ".w3c_leftCol{display:block;float:left;padding-top:0;clear:left;}";
        assertCssCode(code);
    }

    public void testCaseAdv70() throws ParseException {
        String code = ".w3c_leftCol h3 a:hover,h2.category a:hover{background:none;color:#2673AB;text-decoration:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv71() throws ParseException {
        String code = ".w3c_mainCol{overflow:hidden;background-color:#fff;padding-left:3%;padding-bottom:3%;padding-top:1.1%;}";
        assertCssCode(code);
    }

    public void testCaseAdv72() throws ParseException {
        String code = "#w3c_mast h1,.w3c_leftCol{width:20%;}";
        assertCssCode(code);
    }

    public void testCaseAdv73() throws ParseException {
        String code = "#w3c_crumbs_frame,.line .size2on3{margin-right:1.3%;}";
        assertCssCode(code);
    }

    public void testCaseAdv74() throws ParseException {
        String code = ".w3c_member #w3c_acl{width:185px;padding-right:0;padding-left:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv75() throws ParseException {
        String code = ".w3c_alt #w3c_acl,.w3c_member_alt #w3c_acl,.w3c_team #w3c_acl{margin:0;width:210px;text-align:right;padding:5px 0 0;float:left;color:#fff;font-weight:bold;}";
        assertCssCode(code);
    }

    public void testCaseAdv76() throws ParseException {
        String code = ".line,.lastUnit{overflow:hidden;}";
        assertCssCode(code);
    }

    public void testCaseAdv77() throws ParseException {
        String code = ".unit{float:left;}";
        assertCssCode(code);
    }

    public void testCaseAdv78() throws ParseException {
        String code = ".size1on2{width:50%;}";
        assertCssCode(code);
    }

    public void testCaseAdv79() throws ParseException {
        String code = ".size1on3{width:33.33333%;}";
        assertCssCode(code);
    }

    public void testCaseAdv80() throws ParseException {
        String code = ".size2on3{width:66.66666%;}";
        assertCssCode(code);
    }

    public void testCaseAdv81() throws ParseException {
        String code = ".size1on4{width:25%;}";
        assertCssCode(code);
    }

    public void testCaseAdv82() throws ParseException {
        String code = ".size3on4{width:75%;}";
        assertCssCode(code);
    }

    public void testCaseAdv83() throws ParseException {
        String code = ".size1on5{width:18%;}";
        assertCssCode(code);
    }

    public void testCaseAdv84() throws ParseException {
        String code = ".lastUnit{float:none;width:auto;}";
        assertCssCode(code);
    }

    public void testCaseAdv85() throws ParseException {
        String code = ".w3c_home .lastUnit h2.category{margin-bottom:15px;}";
        assertCssCode(code);
    }

    public void testCaseAdv86() throws ParseException {
        String code = ".intro{line-height:1.8em;}";
        assertCssCode(code);
    }

    public void testCaseAdv87() throws ParseException {
        String code = "#w3c_content_body .intro p,#w3c_content_body p.intro{color:#333;font-size:94%;}";
        assertCssCode(code);
    }

    public void testCaseAdv88() throws ParseException {
        String code = "ul.w3c_toc{background-color:#f2f7fb;padding:5px 20px;border-top:1px solid #fff;font-size:94%;}";
        assertCssCode(code);
    }

    public void testCaseAdv89() throws ParseException {
        String code = "ul.w3c_toc li{display:inline;}";
        assertCssCode(code);
    }

    public void testCaseAdv90() throws ParseException {
        String code = "ul.w3c_toc li.toc_prefix{margin-right:10px;padding:2px 4px;background-color:#fff;}";
        assertCssCode(code);
    }

    public void testCaseAdv91() throws ParseException {
        String code = "ul.w3c_toc li .bullet{font-size:144%;color:#cbd9e4;vertical-align:middle;padding:0 10px;}";
        assertCssCode(code);
    }

    public void testCaseAdv92() throws ParseException {
        String code = ".w3c_events_talks .date .dtstart .year{color:#FFF;font-size:88%;font-weight:bold;float:none;margin:0;vertical-align:top;}";
        assertCssCode(code);
    }

    public void testCaseAdv93() throws ParseException {
        String code = ".w3c_events_talks .info-wrap{clear:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv94() throws ParseException {
        String code = ".w3c_events_talks .date .dd-mmm{color:#333;font-size:81%;display:block;font-weight:bold;line-height:11px;text-transform:uppercase;}";
        assertCssCode(code);
    }

    public void testCaseAdv95() throws ParseException {
        String code = ".w3c_events_talks .date .dtstart{padding-top:2px;}";
        assertCssCode(code);
    }

    public void testCaseAdv96() throws ParseException {
        String code = ".w3c_events_talks .date .date-separator{display:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv97() throws ParseException {
        String code = ".w3c_events_talks .date.single .dd-mmm{padding:2px 9px;}";
        assertCssCode(code);
    }

    public void testCaseAdv98() throws ParseException {
        String code = ".w3c_image{border:1px #999 solid;padding:2px;}";
        assertCssCode(code);
    }

    public void testCaseAdv99() throws ParseException {
        String code = "h2,.h2{margin-top:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv100() throws ParseException {
        String code = "h1,h2,h3,h4,h5,h6,ul,ol,dl,p,pre,blockquote{padding:20px 20px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv101() throws ParseException {
        String code = "code{color:#000;}";
        assertCssCode(code);
    }

    public void testCaseAdv102() throws ParseException {
        String code = "h1+p,h2+p,h3+p,h4+p,h5+p{padding-top:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv103() throws ParseException {
        String code = "h1,#w3c_mast h1,.h1{font-size:136%;font-weight:normal;overflow:hidden;}";
        assertCssCode(code);
    }

    public void testCaseAdv104() throws ParseException {
        String code = "h2,.h2{font-size:167%;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseAdv105() throws ParseException {
        String code = "h3,.h3{font-size:131%;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseAdv106() throws ParseException {
        String code = "h4,.h4{font-size:131%;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseAdv107() throws ParseException {
        String code = "h5,.h5{font-size:100%;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseAdv108() throws ParseException {
        String code = "h6,.h6{font-size:85%;font-weight:normal;}";
        assertCssCode(code);
    }

    public void testCaseAdv109() throws ParseException {
        String code = ".category{font-size:108%;font-weight:normal;font-style:normal;text-transform:uppercase;color:#333;}";
        assertCssCode(code);
    }

    public void testCaseAdv110() throws ParseException {
        String code = "h3.category{background:#E7E6E6 url('../images/category-bg-fold.png') no-repeat bottom right;text-shadow:1px 1px 0 #fff;color:#347cb0;padding:0 6px 0 0;width:100%;position:relative;margin-top:13px;}";
        assertCssCode(code);
    }

    public void testCaseAdv111() throws ParseException {
        String code = "h2.category,.h2.category{background:#FCFBFB url('../images/category-bg-right.png') repeat-x bottom right;text-shadow:1px 1px 0 #fff;color:#347cb0;padding:5px 11px 15px 10px;margin:20px 0 3px 10px;font-weight:bold;}";
        assertCssCode(code);
    }

    public void testCaseAdv112() throws ParseException {
        String code = "#w3c_home_member_testimonials h3{padding:0 20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv113() throws ParseException {
        String code = "#w3c_home_sponsors p,#w3c_home_sponsors h3{text-align:center;}";
        assertCssCode(code);
    }

    public void testCaseAdv114() throws ParseException {
        String code = "h3.category .ribbon{background:#E7E6E6 url('../images/category-bg.png') repeat-x bottom right;display:block;padding:8px 5px 13px 20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv115() throws ParseException {
        String code = "h3.category a,h2.category a,.h2.category a{color:#17445F;font-weight:bold;border:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv116() throws ParseException {
        String code = "h1.title{padding:10px 20px;font-size:200%;border-bottom:1px solid #C6D4E0;text-shadow:1px 1px 0 #fff;}";
        assertCssCode(code);
    }

    public void testCaseAdv117() throws ParseException {
        String code = "h1.title img{float:right;margin-top:-2px;display:inline;}";
        assertCssCode(code);
    }

    public void testCaseAdv118() throws ParseException {
        String code = ".media{width:auto;}";
        assertCssCode(code);
    }

    public void testCaseAdv119() throws ParseException {
        String code = ".headline h2,.headline h3,.headline h4{padding:5px 20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv120() throws ParseException {
        String code = "ol.show_items li,.entry ol li,ul.show_items li,.entry ul li{margin-left:40px;}";
        assertCssCode(code);
    }

    public void testCaseAdv121() throws ParseException {
        String code = ".theme{padding-top:10px;}";
        assertCssCode(code);
    }

    public void testCaseAdv122() throws ParseException {
        String code = ".theme ul{display:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv123() throws ParseException {
        String code = ".theme li.subhead ul{display:block;}";
        assertCssCode(code);
    }

    public void testCaseAdv124() throws ParseException {
        String code = ".theme li{border-bottom:1px solid #ddd;}";
        assertCssCode(code);
    }

    public void testCaseAdv125() throws ParseException {
        String code = ".theme li.subhead{border-bottom:none;padding:5px 0 5px 5px;}";
        assertCssCode(code);
    }

    public void testCaseAdv126() throws ParseException {
        String code = ".theme a{display:block;overflow:hidden;font-weight:normal;padding:5px 0 5px 5px;}";
        assertCssCode(code);
    }

    public void testCaseAdv127() throws ParseException {
        String code = ".theme a.current{background-color:#fff;border-bottom:none;}";
        assertCssCode(code);
    }

//    //Bug 206035
//    public void testCaseAdv128() throws ParseException {
//        String code = ".theme .icon{background:url(../images/theme-all.png) no-repeat left top;height:22px;width:22px;display:block;float:left;margin-right:10px;-moz-border-radius:5px;}";
//        assertCssCode(code);
//    }

    public void testCaseAdv129() throws ParseException {
        String code = ".theme .devices .icon{background-position:-22px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv130() throws ParseException {
        String code = ".theme .arch .icon{background-position:-44px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv131() throws ParseException {
        String code = ".theme .design .icon{background-position:-66px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv132() throws ParseException {
        String code = ".theme .semantics .icon{background-position:-88px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv133() throws ParseException {
        String code = ".theme .services .icon{background-position:-110px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv134() throws ParseException {
        String code = ".theme .xml .icon{background-position:-132px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv135() throws ParseException {
        String code = ".theme .allspecs .icon{background-image:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv136() throws ParseException {
        String code = ".theme_ext{padding:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv137() throws ParseException {
        String code = ".theme_ext li.theme_ext_item{position:relative;padding-left:64px;}";
        assertCssCode(code);
    }

    public void testCaseAdv138() throws ParseException {
        String code = ".theme_ext li.theme_ext_item ul{font-size:85%;padding-left:20px;}";
        assertCssCode(code);
    }

//    //Bug 206035
//    public void testCaseAdv139() throws ParseException {
//        String code = ".theme_ext .icon{background:#FFC0CB url(../images/icon_sprite.png) no-repeat 0 0;height:44px;width:44px;display:block;position:absolute;top:28px;left:20px;}";
//        assertCssCode(code);
//    }

    public void testCaseAdv140() throws ParseException {
        String code = ".theme_ext .about_donations .icon{background-position:-1144px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv141() throws ParseException {
        String code = ".theme_ext .about_facts .icon{background-position:-1100px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv142() throws ParseException {
        String code = ".theme_ext .about_jobs .icon{background-position:-1056px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv143() throws ParseException {
        String code = ".theme_ext .about_locations .icon{background-position:-1012px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv144() throws ParseException {
        String code = ".theme_ext .about_mission .icon{background-position:-968px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv145() throws ParseException {
        String code = ".theme_ext .about_press .icon{background-position:-924px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv146() throws ParseException {
        String code = ".theme_ext .comingsoon .icon{background-position:-880px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv147() throws ParseException {
        String code = ".theme_ext .membership_policies .icon{background-position:-660px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv148() throws ParseException {
        String code = ".theme_ext .membership_admin .icon{background-position:-704px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv149() throws ParseException {
        String code = ".theme_ext .membership_join .icon{background-position:-748px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv150() throws ParseException {
        String code = ".theme_ext .membership_fees .icon{background-position:-792px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv151() throws ParseException {
        String code = ".theme_ext .membership_benefits .icon{background-position:-836px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv152() throws ParseException {
        String code = ".theme_ext .participate_calendar .icon{background-position:-616px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv153() throws ParseException {
        String code = ".theme_ext .participate_groups .icon{background-position:-572px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv154() throws ParseException {
        String code = ".theme_ext .participate_implementation .icon{background-position:-528px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv155() throws ParseException {
        String code = ".theme_ext .participate_liaisons .icon{background-position:-484px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv156() throws ParseException {
        String code = ".theme_ext .participate_news .icon{background-position:-440px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv157() throws ParseException {
        String code = ".theme_ext .participate_promotion .icon{background-position:-396px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv158() throws ParseException {
        String code = ".theme_ext .participate_rss .icon{background-position:-352px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv159() throws ParseException {
        String code = ".theme_ext .participate_specifications .icon{background-position:-308px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv160() throws ParseException {
        String code = ".theme_ext .standards_agents .icon{background-position:-264px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv161() throws ParseException {
        String code = ".theme_ext .standards_webofdevices .icon{background-position:-220px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv162() throws ParseException {
        String code = ".theme_ext .standards_webarch .icon{background-position:-176px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv163() throws ParseException {
        String code = ".theme_ext .standards_webdesign .icon{background-position:-132px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv164() throws ParseException {
        String code = ".theme_ext .standards_semanticweb .icon{background-position:-88px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv165() throws ParseException {
        String code = ".theme_ext .standards_webofservices .icon{background-position:-44px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv166() throws ParseException {
        String code = ".theme_ext .standards_xml .icon{background-position:0 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv167() throws ParseException {
        String code = "ul.theme.right-list{padding-top:5px;}";
        assertCssCode(code);
    }

    public void testCaseAdv168() throws ParseException {
        String code = ".theme.right-list li{border-bottom:1px solid #eee;}";
        assertCssCode(code);
    }

    public void testCaseAdv169() throws ParseException {
        String code = ".theme.right-list li a:hover{background-color:#eee;}";
        assertCssCode(code);
    }

    public void testCaseAdv170() throws ParseException {
        String code = ".w3c_home #w3c_most-recently{margin-top:0!important;padding-top:0!important;}";
        assertCssCode(code);
    }

    public void testCaseAdv171() throws ParseException {
        String code = ".date{margin-left:20px;font-size:88%;}";
        assertCssCode(code);
    }

    public void testCaseAdv172() throws ParseException {
        String code = "#w3c_most-recently .date{margin-left:10px;}";
        assertCssCode(code);
    }

    public void testCaseAdv173() throws ParseException {
        String code = ".vevent_list .location,.vevent_list .source,.vevent_list .eventtitle,.vevent_list .person,.hentry_list .entry-title,.hentry_list p.author{padding:0 20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv174() throws ParseException {
        String code = "#workshopslist .date,#workshopslist .location,#workshopslist .host{padding:0;margin-left:20px;font-size:88%;color:#333;}";
        assertCssCode(code);
    }

    public void testCaseAdv175() throws ParseException {
        String code = "#workshopslist p.view_report{padding-top:5px;}";
        assertCssCode(code);
    }

    public void testCaseAdv176() throws ParseException {
        String code = "#workshopslist p.view_report a{background:url('../images/icons/view-report.png') no-repeat top left;border:0;height:35px;padding:10px 0 0 45px;display:block;}";
        assertCssCode(code);
    }

    public void testCaseAdv177() throws ParseException {
        String code = "#workshopslist p.view_report a:hover{text-decoration:underline;}";
        assertCssCode(code);
    }

    public void testCaseAdv178() throws ParseException {
        String code = "#workshopslist .description p{padding:5px 20px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv179() throws ParseException {
        String code = ".main-content{position:relative;}";
        assertCssCode(code);
    }

    public void testCaseAdv180() throws ParseException {
        String code = ".w3c_events_talks{margin-top:20px;background-color:#eee;}";
        assertCssCode(code);
    }

    public void testCaseAdv181() throws ParseException {
        String code = ".w3c_events_talks .unit.size1on2,.w3c_events_talks .unit.size1on1{background:url('../images/talks-bg-left.png') no-repeat top left;}";
        assertCssCode(code);
    }

    public void testCaseAdv182() throws ParseException {
        String code = ".w3c_events_talks .w3c_upcoming_events,.w3c_events_talks .w3c_upcoming_talks{background:url('../images/talks-bg-right.png') no-repeat top right;}";
        assertCssCode(code);
    }

    public void testCaseAdv183() throws ParseException {
        String code = ".w3c_events_talks h2.category{margin:0 20px 20px;background:#F9F9F9 url('../images/talks-bg.png') repeat-x bottom left;}";
        assertCssCode(code);
    }

    public void testCaseAdv184() throws ParseException {
        String code = ".w3c_events_talks .date{background:#aaa url('../images/calendar-sprite.png') no-repeat top left;border:none;height:41px;width:42px;border:none;float:left;margin-right:10px;text-align:center;}";
        assertCssCode(code);
    }

    public void testCaseAdv185() throws ParseException {
        String code = ".w3c_events_talks ul.vevent_list li{float:left;width:100%;}";
        assertCssCode(code);
    }

    public void testCaseAdv186() throws ParseException {
        String code = ".w3c_events_talks ul.vevent_list .info-wrap{margin-left:75px;}";
        assertCssCode(code);
    }

    public void testCaseAdv187() throws ParseException {
        String code = ".w3c_events_talks ul.vevent_list .info-wrap p{padding:0 10px 0 0;font-size:88%;}";
        assertCssCode(code);
    }

    public void testCaseAdv188() throws ParseException {
        String code = ".w3c_events_talks ul.vevent_list .info-wrap p.summary{font-size:113%;}";
        assertCssCode(code);
    }

    public void testCaseAdv189() throws ParseException {
        String code = ".w3c_events_talks ul.vevent_list .info-wrap p.source{color:#9A1724;}";
        assertCssCode(code);
    }

    public void testCaseAdv190() throws ParseException {
        String code = ".w3c_events_talks .w3c_upcoming_events .date{background-position:0 0;margin-bottom:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv191() throws ParseException {
        String code = ".w3c_events_talks .w3c_upcoming_talks .date{background-position:0 -49px;margin-bottom:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv192() throws ParseException {
        String code = "#w3c_main .w3c_upcoming_events ul.vevent_list .location,#w3c_main .w3c_upcoming_talks ul.vevent_list .location,.w3c_events_talks ul.vevent_list .info-wrap .source{font-size:100%;font-style:italic;line-height:1.3;}";
        assertCssCode(code);
    }

    public void testCaseAdv193() throws ParseException {
        String code = "#w3c_main .w3c_upcoming_events ul.vevent_list .location{color:green;}";
        assertCssCode(code);
    }

    public void testCaseAdv194() throws ParseException {
        String code = ".w3c_events_talks ul.vevent_list .info-wrap .source{color:#9A1724;}";
        assertCssCode(code);
    }

    public void testCaseAdv195() throws ParseException {
        String code = "#region_form select{width:76%;}";
        assertCssCode(code);
    }

    public void testCaseAdv196() throws ParseException {
        String code = ".w3c_home #w3c_most-recently h3,#w3c_most-recently h3{margin-left:9px;padding-left:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv197() throws ParseException {
        String code = ".w3c_home #w3c_most-recently h3 span a:hover{border:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv198() throws ParseException {
        String code = ".w3c_home #w3c_most-recently h3 .expand_section:hover{cursor:pointer;}";
        assertCssCode(code);
    }

    public void testCaseAdv199() throws ParseException {
        String code = ".newsImage{overflow:hidden;}";
        assertCssCode(code);
    }

    public void testCaseAdv200() throws ParseException {
        String code = "a.imageLink.no-border img{border:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv201() throws ParseException {
        String code = "a.imageLink img{float:left;margin-right:1em;display:block;text-decoration:none;border:1px solid #CDDCED;padding:2px;}";
        assertCssCode(code);
    }

    public void testCaseAdv202() throws ParseException {
        String code = "a.imageLink:hover img{border:1px solid #3A80B3;}";
        assertCssCode(code);
    }

    public void testCaseAdv203() throws ParseException {
        String code = "a.imageLink:hover.no-border img{opacity:.6;border:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv204() throws ParseException {
        String code = ".expand_section a img{vertical-align:middle;padding-right:.4em;}";
        assertCssCode(code);
    }

    public void testCaseAdv205() throws ParseException {
        String code = ".w3c_javascript .expand_block .expand_description,.expand_block .expand_description{display:block;padding-bottom:20px;margin-bottom:10px;background:#EEE;border-top:1px solid #fff;}";
        assertCssCode(code);
    }

    public void testCaseAdv206() throws ParseException {
        String code = "p.more-content{font-size:81%;font-weight:bold;}";
        assertCssCode(code);
    }

    public void testCaseAdv207() throws ParseException {
        String code = "p.more-content a{border-bottom:0;color:#106F0D;}";
        assertCssCode(code);
    }

    public void testCaseAdv208() throws ParseException {
        String code = "p.more-content a:hover{border-bottom:2px solid #106F0D;}";
        assertCssCode(code);
    }

    public void testCaseAdv209() throws ParseException {
        String code = ".more.expand_section{margin:3px 0 5px 19px;-moz-box-shadow:1px 1px 0 #bbb;-webkit-box-shadow:1px 1px 0 #bbb;box-shadow:1px 1px 0 #bbb;background:#eee;padding:3px 4px;-moz-border-radius:3px;-webkit-border-radius:3px;border-radius:3px;font-size:80%;display:table;}";
        assertCssCode(code);
    }

    public void testCaseAdv210() throws ParseException {
        String code = ".more.expand_section a:link,.more.expand_section a:visited,.more.expand_section a:hover{text-decoration:none;border:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv211() throws ParseException {
        String code = ".more.expand_section+div.expand_description{background:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv212() throws ParseException {
        String code = ".w3c_javascript .expand_block h3{padding-top:0;padding-left:20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv213() throws ParseException {
        String code = ".w3c_javascript .expand_block h4{padding-left:20px;color:#333;}";
        assertCssCode(code);
    }

    public void testCaseAdv214() throws ParseException {
        String code = ".w3c_javascript .expand_block.closed .expand_description,.w3c_javascript .closed .expand_description{display:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv215() throws ParseException {
        String code = ".w3c_javascript .expand_block .headline,.expand_block .headline,.w3c_javascript .expand_block.closed .headline,.expand_block.closed .headline{background:#f1f7fb;border-bottom:1px solid #e2e2e2;padding:10px;}";
        assertCssCode(code);
    }

    public void testCaseAdv216() throws ParseException {
        String code = "#recentnews .expand_block .headline{padding:10px 10px 15px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv217() throws ParseException {
        String code = "#recentnews h2#recent{margin-bottom:10px;}";
        assertCssCode(code);
    }

    public void testCaseAdv218() throws ParseException {
        String code = ".w3c_javascript .expand_block.closed .headline,.expand_block.closed .headline{background:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv219() throws ParseException {
        String code = ".w3c_javascript .expand_block.closed .headline:hover,.expand_block.closed .headline:hover{background-color:#f1f7fb;}";
        assertCssCode(code);
    }

    public void testCaseAdv220() throws ParseException {
        String code = ".hierarchy .expand_block .expand_description{margin-bottom:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv221() throws ParseException {
        String code = ".w3c_screen .trviewcat h3,.trviewcat h3{font-size:108%;}";
        assertCssCode(code);
    }

    public void testCaseAdv222() throws ParseException {
        String code = ".w3c_screen .trviewcat h4,.trviewcat h4{font-size:100%;}";
        assertCssCode(code);
    }

    public void testCaseAdv223() throws ParseException {
        String code = ".menu.expand_block{padding-left:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv224() throws ParseException {
        String code = ".w3c_javascript .expand_block.menu .expand_description{padding-left:20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv225() throws ParseException {
        String code = ".more-news{font-size:131%;}";
        assertCssCode(code);
    }

    public void testCaseAdv226() throws ParseException {
        String code = ".more-news a{color:#036;}";
        assertCssCode(code);
    }

    public void testCaseAdv227() throws ParseException {
        String code = ".feedlink img{margin:0 0 0 6px;vertical-align:top;}";
        assertCssCode(code);
    }

    public void testCaseAdv228() throws ParseException {
        String code = "a.feedlink:link,a.feedlink:visited{border-bottom:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv229() throws ParseException {
        String code = "#request_form{width:50%;}";
        assertCssCode(code);
    }

    public void testCaseAdv230() throws ParseException {
        String code = "#request_form fieldset label{display:block;margin-top:10px;}";
        assertCssCode(code);
    }

    public void testCaseAdv231() throws ParseException {
        String code = "#request_form fieldset input,#request_form fieldset textarea{border:1px solid #777;padding:5px;color:#005A9C;}";
        assertCssCode(code);
    }

    public void testCaseAdv232() throws ParseException {
        String code = "#request_form fieldset input.error,#request_form fieldset textarea.error{border:2px solid #DC4747;}";
        assertCssCode(code);
    }

    public void testCaseAdv233() throws ParseException {
        String code = "#request_form fieldset input:focus,#request_form fieldset textarea:focus{outline:none;box-shadow:0 0 6px #99C0E0;-moz-box-shadow:0 0 7px #99C0E0;-webkit-box-shadow:0 0 7px #99C0E0;-webkit-transition:border .2s linear,-webkit-box-shadow .2s linear;-moz-transition:border .2s linear,-moz-box-shadow .2s linear;border-color:#628EAF;}";
        assertCssCode(code);
    }

    public void testCaseAdv234() throws ParseException {
        String code = "#request_form fieldset p{padding:0;font-size:90%;color:#666;}";
        assertCssCode(code);
    }

    public void testCaseAdv235() throws ParseException {
        String code = ".testimonial{padding:15px;margin:20px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv236() throws ParseException {
        String code = ".testimonial ul{padding:0;margin-left:50px;}";
        assertCssCode(code);
    }

    public void testCaseAdv237() throws ParseException {
        String code = ".w3c_message.errors{background-color:#FFEBE6;border:1px solid #D5A79C;}";
        assertCssCode(code);
    }

    public void testCaseAdv238() throws ParseException {
        String code = ".w3c_message.warnings{background-color:#FCFBE4;border:1px solid #D6D49E;}";
        assertCssCode(code);
    }

    public void testCaseAdv239() throws ParseException {
        String code = ".w3c_message.messages{background-color:#F1FBEB;border:1px solid #AAC28E;}";
        assertCssCode(code);
    }

    public void testCaseAdv240() throws ParseException {
        String code = ".w3c_message h3{display:block;height:40px;font-weight:600;letter-spacing:.02em;font-size:180%;text-shadow:0 1px 1px #fff;padding:3px 50px 0;background:url(\"../images/icons/messages_sprite.png\") no-repeat top left;}";
        assertCssCode(code);
    }

    public void testCaseAdv241() throws ParseException {
        String code = ".w3c_message.errors h3{background-position:0 0;color:#CF3C3C;}";
        assertCssCode(code);
    }

    public void testCaseAdv242() throws ParseException {
        String code = ".w3c_message.warnings h3{background-position:0 -80px;color:#C6A712;}";
        assertCssCode(code);
    }

    public void testCaseAdv243() throws ParseException {
        String code = ".w3c_message.messages h3{background-position:0 -40px;color:#65A020;}";
        assertCssCode(code);
    }

    public void testCaseAdv244() throws ParseException {
        String code = "#endorsed,#workshopsupcoming{background:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv245() throws ParseException {
        String code = "#twitter_update_list li{background:#eee;-moz-border-radius:5px;border-radius:5px;-webkit-border-radius:5px;padding:5px 8px;width:80%;margin-bottom:1em;border-right:1px solid #D6D6D6;border-bottom:1px solid #D6D6D6;}";
        assertCssCode(code);
    }

    public void testCaseAdv246() throws ParseException {
        String code = "a.twit-time:link,a.twit-time:visited{font-size:85%;display:block;color:#878787;margin-top:7px;text-shadow:1px 1px 0 #fff;border-bottom:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv247() throws ParseException {
        String code = "a.twit-time:hover{color:#A6A5A5;}";
        assertCssCode(code);
    }

    public void testCaseAdv248() throws ParseException {
        String code = ".rhs-logo{text-align:center;display:block;margin-top:20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv249() throws ParseException {
        String code = "header,nav,section,aside,footer,article,hgroup{display:block;}";
        assertCssCode(code);
    }

    public void testCaseAdv250() throws ParseException {
        String code = ".team-photo{width:94%;-moz-box-shadow:0 2px 10px #bbb;-webkit-box-shadow:0 2px 10px #bbb;box-shadow:0 2px 10px #bbb;margin:25px 0 20px 7px;padding:10px;text-align:center;}";
        assertCssCode(code);
    }

    public void testCaseAdv251() throws ParseException {
        String code = ".team-photo img{width:99.8%;height:auto;margin-bottom:5px;}";
        assertCssCode(code);
    }

    public void testCaseAdv252() throws ParseException {
        String code = ".team-photo span{font-family:Georgia,Times,serif;font-style:italic;}";
        assertCssCode(code);
    }

    public void testCaseAdv253() throws ParseException {
        String code = ".team-photo img+span{text-transform:uppercase;color:#999;border-bottom:1px solid #eee;font-style:normal;}";
        assertCssCode(code);
    }

    public void testCaseAdv254() throws ParseException {
        String code = ".team-photo+.hierarchy .more.expand_section{margin:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv255() throws ParseException {
        String code = ".w3c_javascript .expand_block .expand_description.people{background:#eee;margin:10px 0;font-size:100%;color:#036;text-shadow:1px 1px 0 #FFF;-moz-border-radius:5px;-webkit-border-radius:5px;border-radius:5px;}";
        assertCssCode(code);
    }

    public void testCaseAdv256() throws ParseException {
        String code = ".w3c_javascript .expand_block .expand_description.people p{padding:0 20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv257() throws ParseException {
        String code = ".w3c_javascript .expand_block .expand_description.people p.people_row{color:#777;font-family:Georgia,Times,serif;font-style:italic;padding-top:12px;}";
        assertCssCode(code);
    }

    public void testCaseAdv258() throws ParseException {
        String code = ".plans{padding:0;overflow:hidden;margin:30px 20px 20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv259() throws ParseException {
        String code = ".plans li p,.plans li h2{padding:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv260() throws ParseException {
        String code = ".plans li{font-family:Georgia,Times,serif;padding:5px 5px 30px 5px;text-align:center;border-left:1px solid #eee;text-shadow:1px 1px 0 #fff;background:url(\"../images/icons/sponsors_shape.png\") no-repeat bottom center;min-height:350px;}";
        assertCssCode(code);
    }

    public void testCaseAdv261() throws ParseException {
        String code = ".plans li:first-child{border:none;}";
        assertCssCode(code);
    }

    public void testCaseAdv262() throws ParseException {
        String code = ".plans li:hover{background:#eee url(\"../images/icons/sponsors_shape.png\") no-repeat bottom center;color:#333;-webkit-transition:all .2s ease-out;-moz-transition:all .2s ease-out;-o-transition:all .2s ease-out;transition:all .2s ease-out;}";
        assertCssCode(code);
    }

    public void testCaseAdv263() throws ParseException {
        String code = ".plans li h2,.w3c_plans .intro h2{font-size:120%;color:#333;letter-spacing:.5px;line-height:1.2em;margin:0 0 10px;text-transform:uppercase;}";
        assertCssCode(code);
    }

    public void testCaseAdv264() throws ParseException {
        String code = ".w3c_plans .intro h2{font-size:140%;}";
        assertCssCode(code);
    }

    public void testCaseAdv265() throws ParseException {
        String code = ".w3c_plans .show_items,.w3c_plans p,.w3c_plans h3{padding:5px 0;clear:both;}";
        assertCssCode(code);
    }

    public void testCaseAdv266() throws ParseException {
        String code = ".w3c_plans h3{font-family:Georgia,Times,serif;font-size:120%;}";
        assertCssCode(code);
    }

    public void testCaseAdv267() throws ParseException {
        String code = "#w3c_main .plans li h2+p{font-style:italic;font-size:90%;letter-spacing:.4px;color:#333;line-height:1.15em;min-height:115px;}";
        assertCssCode(code);
    }

//    //Bug 206035
//    public void testCaseAdv268() throws ParseException {
//        String code = "#w3c_main .plans li a.call-to-action{font-family:\"Helvetica Neue\",Helvetica,Arial,Verdana,Geneva,sans-serif;font-size:80%;letter-spacing:.5px;-moz-border-radius:23px;-webkit-border-radius:23px;border-radius:23px;-moz-box-shadow:0 1px 2px rgba(0,0,0,0.5);-webkit-box-shadow:0 1px 2px rgba(0,0,0,0.5);box-shadow:0 1px 2px rgba(0,0,0,0.5);background-color:#bbb;background-image:-webkit-gradient(linear,0% 0,0% 100%,from(#fff),to(#bbb));background-image:-moz-linear-gradient(0% 100% 90deg,#bbb,#fff);border:none;color:#555;font-weight:bold;padding:6px 15px;text-shadow:0 1px 1px rgba(255,255,255,0.85);}";
//        assertCssCode(code);
//    }

//    //Bug 206035
//    public void testCaseAdv269() throws ParseException {
//        String code = "#w3c_main .plans li a.call-to-action.external{background-color:#95BFD2;background-image:-webkit-gradient(linear,0% 0,0% 100%,from(#fff),to(#95BFD2));background-image:-moz-linear-gradient(0% 100% 90deg,#95BFD2,#fff);}";
//        assertCssCode(code);
//    }

    public void testCaseAdv270() throws ParseException {
        String code = "#w3c_main .plans li a.call-to-action:hover{-moz-box-shadow:none;-webkit-box-shadow:none;box-shadow:none;color:#333;}";
        assertCssCode(code);
    }

    public void testCaseAdv271() throws ParseException {
        String code = "#w3c_main .plans li a.call-to-action+p{font-family:\"Helvetica Neue\",Helvetica,Arial,Verdana,Geneva,sans-serif;font-size:85%;letter-spacing:.4px;color:#333;line-height:1.15em;margin:20px 0 0;height:90px;}";
        assertCssCode(code);
    }

    public void testCaseAdv272() throws ParseException {
        String code = ".plans li span{background:url(\"../images/icons/sponsors_sprite.png\") no-repeat center;height:100px;display:block;margin:10px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv273() throws ParseException {
        String code = ".plans span.corporate{background-position:center -15px;}";
        assertCssCode(code);
    }

    public void testCaseAdv274() throws ParseException {
        String code = ".plans span.events{background-position:center -120px;}";
        assertCssCode(code);
    }

    public void testCaseAdv275() throws ParseException {
        String code = ".plans span.initiatives{background-position:center -235px;}";
        assertCssCode(code);
    }

    public void testCaseAdv276() throws ParseException {
        String code = ".plans span.validator{background-position:center -340px;}";
        assertCssCode(code);
    }

    public void testCaseAdv277() throws ParseException {
        String code = ".plans span.supporters{background-position:center -442px;}";
        assertCssCode(code);
    }

    public void testCaseAdv278() throws ParseException {
        String code = "@media screen and /*!space fix */(max-width:1000px){.plans li h2{font-size:100%;} }";
        assertCssCode(code);
    }

    public void testCaseAdv279() throws ParseException {
        String code = "#w3c_main .plans li h2+p,#w3c_main .plans li a.call-to-action+p{font-size:80%;}";
        assertCssCode(code);
    }

    public void testCaseAdv280() throws ParseException {
        String code = "#w3c_main .plans li a.call-to-action{padding:3px 10px;}";
        assertCssCode(code);
    }

    public void testCaseAdv281() throws ParseException {
        String code = ".plans li span{background:url(\"../images/icons/sponsors_sprite_small.png\") no-repeat center;}";
        assertCssCode(code);
    }

    public void testCaseAdv282() throws ParseException {
        String code = ".plans li span{height:65px;}";
        assertCssCode(code);
    }

    public void testCaseAdv283() throws ParseException {
        String code = ".plans span.corporate{background-position:center 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv284() throws ParseException {
        String code = ".plans span.events{background-position:center -65px;}";
        assertCssCode(code);
    }

    public void testCaseAdv285() throws ParseException {
        String code = ".plans span.initiatives{background-position:center -135px;}";
        assertCssCode(code);
    }

    public void testCaseAdv286() throws ParseException {
        String code = ".plans span.validator{background-position:center -200px;}";
        assertCssCode(code);
    }

    public void testCaseAdv287() throws ParseException {
        String code = ".plans span.supporters{background-position:center -260px;}";
        assertCssCode(code);
    }

    public void testCaseAdv289() throws ParseException {
        String code = ".w3c_plans{padding:0 20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv290() throws ParseException {
        String code = ".w3c_plans .intro.corporate,.w3c_plans .intro.events,.w3c_plans .intro.strategic,.w3c_plans .intro.developer{padding:0 100px 0 150px;margin:25px 0 10px 0;min-height:160px;}";
        assertCssCode(code);
    }

    public void testCaseAdv291() throws ParseException {
        String code = ".w3c_plans .intro.corporate{background:url(\"../images/icons/sponsors_folder.png\") no-repeat top left;}";
        assertCssCode(code);
    }

    public void testCaseAdv292() throws ParseException {
        String code = ".w3c_plans .intro.events{background:url(\"../images/icons/sponsors_calendar.png\") no-repeat top left;}";
        assertCssCode(code);
    }

    public void testCaseAdv293() throws ParseException {
        String code = ".w3c_plans .intro.strategic{background:url(\"../images/icons/sponsors_globe.png\") no-repeat top left;}";
        assertCssCode(code);
    }

    public void testCaseAdv294() throws ParseException {
        String code = ".w3c_plans .intro.developer{background:url(\"../images/icons/sponsors_check.png\") no-repeat top left;}";
        assertCssCode(code);
    }

    public void testCaseAdv295() throws ParseException {
        String code = ".w3c_plans .intro p,.w3c_plans .intro h2,.w3c_plans p{padding:0;}";
        assertCssCode(code);
    }

    public void testCaseAdv296() throws ParseException {
        String code = "#w3c_content_body .w3c_plans .intro p{color:#333;font-family:\"Helvetica Neue\",Helvetica,Arial,Verdana,Geneva,sans-serif;font-style:normal;}";
        assertCssCode(code);
    }

    public void testCaseAdv297() throws ParseException {
        String code = ".w3c_home_plans{overflow:hidden;position:relative;width:285px;margin:1em 0 0 5em;display:inline-block;}";
        assertCssCode(code);
    }

    public void testCaseAdv298() throws ParseException {
        String code = ".w3c_home_plans em{float:left;}";
        assertCssCode(code);
    }

    public void testCaseAdv299() throws ParseException {
        String code = ".w3c_home_plans img{position:relative;left:25px;top:20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv300() throws ParseException {
        String code = ".w3c_plans .intro h2,.w3c_plans h2[id],.w3c_home_plans h2{font-family:Georgia,Times,serif;text-transform:uppercase;margin:0 0 7px 0;}";
        assertCssCode(code);
    }

    public void testCaseAdv301() throws ParseException {
        String code = ".w3c_plans h2[id],.w3c_home_plans h2{background:url(\"../images/underline.png\") repeat-x 0 25px;float:left;margin:15px 0 0 0;padding:0 0 5px 0;font-size:130%;color:#333;}";
        assertCssCode(code);
    }

    public void testCaseAdv302() throws ParseException {
        String code = ".w3c_plans p,.w3c_plans ul{font-family:Georgia,Times,serif;padding-bottom:20px;}";
        assertCssCode(code);
    }

    public void testCaseAdv303() throws ParseException {
        String code = ".w3c_plans ul{font-style:italic;}";
        assertCssCode(code);
    }

    public void testCaseAdv304() throws ParseException {
        String code = "#w3c_main .w3c_plans h2[id]+ul li{line-height:1.35em;}";
        assertCssCode(code);
    }

    public void testIssue241560_01() throws ParseException {
        String code = "#foo {height: inherit;}";
        assertCssCode(code);
    }

    public void testIssue241560_02() throws ParseException {
        String code = "#foo {width: inherit;}";
        assertCssCode(code);
    }
    
    public void testInheritInMaxMinWidthHeight() throws ParseException {
        String code = "#foo {"
                + "    max-width: inherit;\n"
                + "    min-width: inherit;\n"
                + "    max-height: inherit;\n"
                + "    min-height: inherit;"
                + "}";
        assertCssCode(code);
    }
    
    public void testCursorNewProperties() throws ParseException {
        String code = "span.wait {\n"
                + "    cursor:url(smiley.gif),url(myBall.cur),auto;\n"
                + "    cursor: zoom-in;\n"
                + "    cursor: zoom-out;\n"
                + "    cursor: grab;\n"
                + "    cursor: grabbing;\n"
                + "}";
        assertCssCode(code);
    }
    
    public void testListInherit()  throws ParseException {
        String code = "ul {\n"
                + "    list-style-position: inherit;\n"
                + "    list-style: inherit\n"
                + "}";
        assertCssCode(code);
    }
    
    public void testLinerGradient() throws ParseException {
        String code = "#grad {\n"
                + "  background: -webkit-linear-gradient(left top, red , blue); /* For Safari 5.1 to 6.0 */\n"
                + "  background: -o-linear-gradient(bottom right, red, blue); /* For Opera 11.1 to 12.0 */\n"
                + "  background: -moz-linear-gradient(bottom right, red, blue); /* For Firefox 3.6 to 15 */\n"
                + "  background: linear-gradient(to bottom right, red , blue); /* Standard syntax */\n"
                + "}";
        assertCssCode(code);
    }
    
    public void testFirefoxAppearance() throws ParseException {
        String code = ".test {\n"
                + "  -moz-appearance: none"
                + "}";
        assertCssCode(code);
    }
    
    public void testMozBorderImageStandard() throws ParseException {
        String code = ".test {\n"
                + "  -moz-border-image: url('test.png') 8 fill;"
                + "}";
        assertCssCode(code);
    }
    
    public void testTransition()  throws ParseException {
        String code = ".test {\n"
                + "-o-transition: .6s ease-in-out left;\n"
                + "transition: .6s ease-in-out left;\n"
                + "}";
        assertCssCode(code);
    }

    public void testUnicodeRange() throws ParseException {
        String code = "xxx {\n"
            + "    unicode-range: U+0D01, U+0A01-00ff, U+0A??;\n"
            + "}";
        assertCssCode(code);
    }

    public void testDisplay() throws ParseException {
        assertCssCode(".demo {display: block}");
        assertCssCode(".demo {display: inline-block}");
        assertCssCode(".demo {display: inline flow}");
        assertCssCode(".demo {display: block table}");
        assertCssCode(".demo {display: block flex}");
        assertCssCode(".demo {display: block flow list-item}");
        assertCssCode(".demo {display: flow list-item}");
        assertCssCode(".demo {display: list-item flow}");
    }

    public void testGrid() throws ParseException {
        assertCssCode(".demo {grid-template-columns: 150px 1fr;}");
        assertCssCode(".demo {grid-template-columns: 150px [item1-start] 1fr [item1-end];}");
        assertCssCode(".demo {grid-template-rows: [item1-start] 50px 1fr 50px [item1-end]}");
        assertCssCode(".demo {grid-template-rows: 1fr minmax(min-content, 1fr)}");
        assertCssCode(".demo {grid-template-areas: \"head head\"\n\"nav main\"}");
        assertCssCode(".demo {grid-template: auto 1fr / auto 1fr auto;}");
        assertCssCode(".demo {grid-template: [header-top] \"a   a   a\"     [header-bottom]\n"
            + "[main-top] \"b   b   b\" 1fr [main-bottom]\n"
            + "/ auto 1fr auto;}");
        assertCssCode(".demo {grid-column-start: 4}");
        assertCssCode(".demo {grid-column-end: auto}");
        assertCssCode(".demo {grid-row-start: C}");
        assertCssCode(".demo {grid-row-start: -4}");
        assertCssCode(".demo {grid-row-end: B -1}");
        assertCssCode(".demo {grid-row: A / C}");
        assertCssCode(".demo {grid-column: 1 / 3}");
        assertCssCode(".demo {grid-row: B}");
        assertCssCode(".demo {grid-column: 4}");
        assertCssCode(".demo {grid-auto-columns: 20px min-content fit-content(30px)}");
        assertCssCode(".demo {grid-auto-rows: 40px minmax(max-content, auto)}");
        assertCssCode(".demo {grid-auto-flow: row dense;}");
        assertCssCode(".demo {grid-area: dummy-area}");
        assertCssCode(".demo {grid-area: auto}");
        assertCssCode(".demo {grid-area: 2 / 4}");
        assertCssCode(".demo {grid-area: 1 / 3 / -1}");
        assertCssCode(".demo {grid-area: header-start / sidebar-start / footer-end / sidebar-start}");
        assertCssCode(".demo {grid: auto-flow 1fr / 100px;}");
        assertCssCode(".demo {grid: \"H  H\"\n"
            + "\" A B\"\n"
            + "\" F F\" 30px\n"
            + "/ auto 1fr\n"
            + "}");
        assertCssCode(".demo {grid: repeat(auto-fill, 5em) / auto-flow 1fr;}");
        assertCssCode(".demo {grid: auto-flow 1fr / repeat(auto-fill, 5em);}");
        assertCssCode(".demo {grid: auto 1fr auto / repeat(5, 1fr);}");
    }

    public void testCaseSensitivity() throws Exception {
        assertCssCode("h1 { border-style: dashed; border-width: 1rem; }");
        assertCssCode("h1 { border-style: dAsHeD; border-width: 1ReM; }");
        assertCssCode("h1 { BORDER-STYLE: dashed }");
        assertCssCode("h1 { bOrDeR-sTyLe: dashed }");
    }

    public void testContainProperties() throws Exception {
        assertCssCode("main {container-type: normal}");
        assertCssCode("main {container-type: size}");
        assertCssCode("main {container-type: inline-size}");
        assertCssCode("main {container-name: demo}");
        assertCssCode("main {container: demo}");
        assertCssCode("main {container: demo / size}");
        assertCssCode("main {container: demo / inline-size}");
    }
}