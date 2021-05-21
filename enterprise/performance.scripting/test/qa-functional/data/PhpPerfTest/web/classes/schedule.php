<?php
/*
This is a Schedule helper class. Instance of
this class can store Schedule information.
*/

class Schedule{
    private $SID;
    private $SDate;
    private $flights;
    
    function set_SID($SID){
            $this->SID = $SID;
        }
    function set_SDate($SDate){
            $this->SDate = $SDate;
        }
    function set_flights($flights){
            $this->flights = $flights;
        }
   

    function get_SID(){
            return $this->SID;
        }
    function get_SDate(){
            return $this->SDate;
        }
    function get_flights(){
            return $this->fligths;
        }     
}
?>