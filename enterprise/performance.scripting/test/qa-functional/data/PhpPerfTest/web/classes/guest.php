<?php
/*
This is a Guest helper class. Instance of
this class can store Guest information.
*/

class Guest{
    private $GID;
    private $firstName;
    private $lastName;

    function set_GID($GID){
            $this->GID = $GID;
        }
    function set_firstName($firstName){
            $this->firstName = $firstName;
        }
    function set_lastName($lastName){
            $this->lastName = $lastName;
        }

    function get_GID(){
            return $this->GID;
        }
    function get_firstName(){
            return $this->firstName;
        }
    function get_lastName(){
            return $this->lastName;
        } 
}
?>