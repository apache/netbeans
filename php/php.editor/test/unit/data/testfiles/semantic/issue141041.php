<?php

    class TwitterWhatAreYouDoingServiceAuthenticatorProfile {

        //Specify the usern here
        private static $username = "";

        //Specify the password here
        private static $password = "";

        public static function getUsername() {
            return self::$username;
        }

        public static function getPassword() {
            return self::$password;
        }

        public function hello() {
            return this;
        }


    }
?>
