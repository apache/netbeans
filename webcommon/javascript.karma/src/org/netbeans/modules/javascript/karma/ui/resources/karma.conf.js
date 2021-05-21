<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

module.exports = function(config) {
    config.set({
        basePath : '../',

        files : [
        ],

        exclude : [
        ],

        autoWatch : true,

        frameworks: [
        ],

        browsers : [
        ],

        plugins : [
        ]
    });
};
