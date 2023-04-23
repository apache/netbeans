package main

import (
	"fmt"
	"log"
	"errors"
	"net/http"
)

var Dir string

/*
var Users jsoncfgo.Obj
var AppContext *go_utils.Singleton
*/

func HtmlFileHandler(response http.ResponseWriter, request *http.Request, filename string){
	response.Header().Set("Content-type", "text/html")
	webpage, err := ioutil.ReadFile(Dir + filename)  // read whole the file
	if err != nil {
		http.Error(response, fmt.Sprintf("%s file error %v", filename, err), 500)
	}
	fmt.Fprint(response, string(webpage));
}
