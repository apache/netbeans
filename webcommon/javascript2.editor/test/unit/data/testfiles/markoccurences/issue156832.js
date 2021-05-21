CSSClass.remove = function(param, c)
{
    if (typeof param == "string")
        param = document.getElementById(param);
    param.className = param.className.replace(new RegExp("\\b" + c + "\\b\\s*", "g"), "");
}