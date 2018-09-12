MySpace.extractTimeFromDateObject = function(dateObject)
{
    var time = 10 + dateObject.getMinutes() + ":" + dateObject.getSeconds();
    return time;
 };