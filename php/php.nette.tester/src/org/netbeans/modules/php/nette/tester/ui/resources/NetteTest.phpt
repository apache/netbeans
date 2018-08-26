<?php
<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

require 'Tester/bootstrap.php';

use Tester\Assert;

$object = new Greeting();

// use an assertion function to test say()
Assert::same('Hello John', $object->say('John'));
