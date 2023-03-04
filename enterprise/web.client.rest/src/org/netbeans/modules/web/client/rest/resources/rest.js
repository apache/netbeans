<#--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

var app = {
 // Create this closure to contain the cached modules
 module: function() {
    // Internal module cache.
    var modules = {};

    // Create a new module reference scaffold or load an
    // existing module.
    return function(name) {
      // If this module has already been created, return it.
      if (modules[name]) {
        return modules[name];
      }

      // Create a module and save it under this name
      return modules[name] = { Views: {} };
    };
  }()
};

(function(models) {
   ${models} 
})(app.module("models"));

(function(views) {
    
    views.ListView = Backbone.View.extend({
    <#if ui??>
        tagName:'tbody',
    <#else>
        tagName:'ul',
    </#if>
        initialize:function (options) {
            this.options = options || {};
            this.model.bind("reset", this.render, this);
            var self = this;
            this.model.bind("add", function (modelName) {
		var row = new views.ListItemView({
                    model:modelName,
                    templateName: self.options.templateName
                }).render().el;
                $(self.el).append($(row));
	    <#if ui??>
		$(self.el).parent().trigger('addRows', [$(row)]);
	    </#if>
            });
        },
 
        render:function (eventName) {
            var self = this;
            _.each(this.model.models, function (modelName) {
                $(this.el).append(new views.ListItemView({
                    model:modelName,
                    templateName: self.options.templateName
                }).render().el);
            }, this);
            return this;
        }
    });

    views.ListItemView = Backbone.View.extend({  
    <#if ui??>
        tagName:'tr',
    <#else>
        tagName:"li",
    </#if>
        
        initialize:function (options) {
            this.options = options || {};
            this.model.bind("change", this.render, this);
            this.model.bind("destroy", this.close, this);
        },
        
        template: function(json){
            /*
             *  templateName is element identifier in HTML
             *  $(this.options.templateName) is element access to the element
             *  using jQuery 
             */ 
            return _.template($(this.options.templateName).html())(json);
        },
        
        render:function (eventName) {
        <#if ui??>
            $(this.el).html(this.template(this.model.toJSON()));
        <#else>
            $(this.el).html(this.template(this.model.toViewJson()));
        </#if>
            return this;
        },
        
        close:function () {
        <#if ui??>
	    var table = $(this.el).parent().parent();
            table.trigger('disable.pager');
        </#if>
            $(this.el).unbind();
            $(this.el).remove();
        <#if ui??>
  	    table.trigger('enable.pager');
        </#if>
        }
        
    });
    
    views.ModelView = Backbone.View.extend({
 
        initialize:function (options) {
            this.options = options || {};
            this.model.bind("change", this.render, this);
        },
 
        render:function (eventName) {
        <#if ui??>
            $(this.el).html(this.template(this.model.toJSON()));
        <#else>
            $(this.el).html(this.template(this.model.toViewJson()));
        </#if>
            return this;
        },
        
        template: function(json){
            /*
             *  templateName is element identifier in HTML
             *  $(this.options.templateName) is element access to the element
             *  using jQuery 
             */
            return _.template($(this.options.templateName).html())(json);
        },
 
        /*
         *  Classes "save"  and "delete" are used on the HTML controls to listen events.
         *  So it is supposed that HTML has controls with these classes.
         */
        events:{
            "change input":"change",
            "click .save":"save",
            "click .delete":"drop"
        },
 
        change:function (event) {
            var target = event.target;
            console.log('changing ' + target.id + ' from: ' + target.defaultValue + ' to: ' + target.value);
        },
 
        save:function () {
            // TODO : put save code here
            var hash = this.options.getHashObject();
            this.model.set(hash);
            if ( this.model.isNew() && this.collection) {
                var self = this;
                this.collection.create(this.model,{
                    success: function(){
                        // see isNew() method implementation in the model
                        self.model.notSynced = false;
                        self.options.navigate(self.model.id);
                    }
                });
            } else {
                this.model.save();
            <#if ui??>
		this.model.el.parent().parent().trigger("update");
            </#if>
            }
            return false;
        },
 
        drop:function () {
            this.model.destroy({
                success:function () {
                    /*
                     *  TODO : put your code here
                     *  f.e. alert("Model is successfully deleted");
                     */  
                    window.history.back();
                }
            });
            return false;
        },
 
        close:function () {
            $(this.el).unbind();
            $(this.el).empty();
        }
    });
    
    // This view is used to create new model element
    views.CreateView = Backbone.View.extend({
 
        initialize:function(options) {
            this.options = options || {};
            this.render();  
        },
 
        render:function (eventName) {
            $(this.el).html(this.template());
            return this;
        },
        
        template: function(json){
            /*
             *  templateName is element identifier in HTML
             *  $(this.options.templateName) is element access to the element
             *  using jQuery 
             */
            return _.template($(this.options.templateName).html())(json);
        },
 
        /*
         *  Class "new" is used on the control to listen events.
         *  So it is supposed that HTML has a control with "new" class.
         */
        events:{
            "click .new":"create"
        },
 
        create:function (event) {
            this.options.navigate();
            return false;
        }
    });
    
})(app.module("views"));


$(function(){
    var models = app.module("models");
    var views = app.module("views");

    ${routers}

    Backbone.history.start();
});
