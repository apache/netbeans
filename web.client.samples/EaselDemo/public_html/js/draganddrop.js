/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var orig_draganddrop="";
var orig_div1 = "";
var orig_rabbit1 = "";

function allowDrop(ev)
{
    ev.preventDefault();
}

function drag(ev)
{
    ev.dataTransfer.setData("pets", ev.target.id);
}

function drop(ev)
{
    orig_draganddrop = document.getElementById('DandD_div').innerHTML;

    ev.preventDefault();
    var data = ev.dataTransfer.getData("pets");
    ev.target.appendChild(document.getElementById(data));
    document.getElementById('drop1').setAttribute("src","img/cute_rabbit1.jpg");
    document.getElementById('drop1').setAttribute("width","300");
    document.getElementById('drop1').setAttribute("height","200");

    document.getElementById("div1").setAttribute('width',"300");
    document.getElementById("div1").setAttribute('height',"200");

    document.getElementById('draganddrop').innerHTML="Well, Hello There! <br/><a id='resetBtn' class='btn' onclick='reset()' href='#'>Reset</a>";
    document.getElementById('drag1').style.display='none';
}

function reset(){
    document.getElementById("DandD_div").innerHTML = orig_draganddrop;
}

