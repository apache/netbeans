define('knockout.global', ['knockout'], function(kno)
{
    window.ko = kno; // Initialize a global 'ko' variable
    return kno;
});