function Test(param) {
    this.util = {
        setCookie: function(name, value) {
                // here
            var date = new Date();
            
            date.setTime(date.getTime() + (synergy.defaultCookiesExpiration));
            var expires = "; expires=" + date.toGMTString();
            document.cookie = name + "=" + value + expires + "; path=/";
        }
    };
}