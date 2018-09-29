<?php
<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

/**
 * @author ${user}
 */

// DO NOT REMOVE "%INCLUDE_PATH%" FROM TEMPLATE!
// TODO: check include path
ini_set('include_path', ini_get('include_path')%INCLUDE_PATH%);

// put your code here
