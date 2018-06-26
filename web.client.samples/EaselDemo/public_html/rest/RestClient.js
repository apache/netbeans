/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */

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
            return modules[name] = {Views: {}};
        };
    }()
};

(function(models) {

// Model for Manufacturer entity
    models.Manufacturer = Backbone.Model.extend({
        urlRoot: "rest/resources/manufacturer.json",
        idAttribute: 'manufacturerId',
        defaults: {
            addressline2: "",
            zip: "",
            phone: "",
            fax: "",
            addressline1: "",
            email: "",
            name: "",
            state: "",
            rep: "",
            city: ""
        },
        toViewJson: function() {
            var result = this.toJSON(); // displayName property is used to render item in the list
            result.displayName = this.get('name');
            return result;
        },
        isNew: function() {
            // default isNew() method imlementation is
            // based on the 'id' initialization which
            // sometimes is required to be initialized.
            // So isNew() is rediefined here
            return this.notSynced;
        },
        sync: function(method, model, options) {
            options || (options = {});
            var errorHandler = {
                error: function(jqXHR, textStatus, errorThrown) {
                    // TODO: put your error handling code here
                    // If you use the JS client from the different domain
                    // (f.e. locally) then Cross-origin resource sharing 
                    // headers has to be set on the REST server side.
                    // Otherwise the JS client has to be copied into the
                    // some (f.e. the same) Web project on the same domain
                    alert('Unable to fulfil the request. This is a sample application without server backend.');
                }}
            
            if (method == 'create') {
                options.url = 'rest/resources/manufacturer.json';
            }
            if (method == 'update') {
                options.url = 'rest/resources/manufacturer.json';
            }
            var result = Backbone.sync(method, model, _.extend(options, errorHandler));
            return result;
        }
        
        
    });
    
    
    // Collection class for Manufacturer entities
    models.ManufacturerCollection = Backbone.Collection.extend({
        model: models.Manufacturer,
        url: "rest/resources/manufacturer.json",
        sync: function(method, model, options) {
            options || (options = {});
            var errorHandler = {
                error: function(jqXHR, textStatus, errorThrown) {
                    // TODO: put your error handling code here
                    // If you use the JS client from the different domain
                    // (f.e. locally) then Cross-origin resource sharing 
                    // headers has to be set on the REST server side.
                    // Otherwise the JS client has to be copied into the
                    // some (f.e. the same) Web project on the same domain
                    alert('Unable to fulfil the request');
                }}
            
            var result = Backbone.sync(method, model, _.extend(options, errorHandler));
            return result;
        }
    });
    
    
})(app.module("models"));

(function(views) {
    
    views.ListView = Backbone.View.extend({
        tagName: 'tbody',
        initialize: function() {
            
            this.model.bind("reset", this.render, this);
            var self = this;
            this.model.bind("add", function(modelName) {
                var row = new views.ListItemView({
                    model: modelName,
                    templateName: self.options.templateName
                }).render().el;
                $(self.el).append($(row));
                $(self.el).parent().trigger('addRows', [$(row)]);
            });
        },
        render: function(eventName) {
            var self = this;
            _.each(this.model.models, function(modelName) {
                $(this.el).append(new views.ListItemView({
                    model: modelName,
                    templateName: self.options.templateName
                }).render().el);
            }, this);
            return this;
        }
    });
    
    views.ListItemView = Backbone.View.extend({
        tagName: 'tr',
        initialize: function() {
            this.model.bind("change", this.render, this);
            this.model.bind("destroy", this.close, this);
        },
        template: function(json) {
            /*
             *  templateName is element identifier in HTML
             *  $(this.options.templateName) is element access to the element
             *  using jQuery 
             */ 
            return _.template($(this.options.templateName).html())(json);
        },
        render: function(eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        close: function() {
            var table = $(this.el).parent().parent();
            table.trigger('disable.pager');
            $(this.el).unbind();
            $(this.el).remove();
            table.trigger('enable.pager');
        }
        
    });
    
    views.ModelView = Backbone.View.extend({
        initialize: function() {
            this.model.bind("change", this.render, this);
        },
        render: function(eventName) {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        template: function(json) {
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
        events: {
            "change input": "change",
            "click .save": "save",
            "click .delete": "drop"
        },
        change: function(event) {
            var target = event.target;
            console.log('changing ' + target.id + ' from: ' + target.defaultValue + ' to: ' + target.value);
        },
        save: function() {
            // TODO : put save code here
            var hash = this.options.getHashObject();
            this.model.set(hash);
            if (this.model.isNew() && this.collection) {
                var self = this;
                this.collection.create(this.model, {
                    success: function() {
                        // see isNew() method implementation in the model
                        self.model.notSynced = false;
                        self.options.navigate(self.model.id);
                    }
                });
            } else {
                this.model.save();
                this.model.el.parent().parent().trigger("update");
            }
            return false;
        },
        drop: function() {
            this.model.destroy({
                success: function() {
                    /*
                     *  TODO : put your code here
                     *  f.e. alert("Model is successfully deleted");
                     */  
                    window.history.back();
                }
            });
            return false;
        },
        close: function() {
            $(this.el).unbind();
            $(this.el).empty();
        }
    });
    
    // This view is used to create new model element
    views.CreateView = Backbone.View.extend({
        initialize: function() {
            this.render();  
        },
        render: function(eventName) {
            $(this.el).html(this.template());
            return this;
        },
        template: function(json) {
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
        events: {
            "click .new": "create"
        },
        create: function(event) {
            this.options.navigate();
            return false;
        }
    });
    
})(app.module("views"));


$(function() {
    var models = app.module("models");
    var views = app.module("views");
    
    var AppRouter = Backbone.Router.extend({
        routes: {
            '': 'list',
            'new': 'create'
                    ,
            ':id': 'details'
        },
        initialize: function() {
            var self = this;
            $('#create').html(new views.CreateView({
                // tpl-create is template identifier for 'create' block
                templateName: '#tpl-create',
                navigate: function() {
                    self.navigate('new', true);
                }
            }).render().el);
        },
        list: function() {
            this.collection = new models.ManufacturerCollection();
            var self = this;
            this.collection.fetch({
                success: function() {
                    self.listView = new views.ListView({
                        model: self.collection,
                        // tpl-manufacturer-list-itemis template identifier for item
                        templateName: '#tpl-manufacturer-list-item'
                    });
                    $('#datatable').html(self.listView.render().el).append(_.template($('#thead').html())());
                    if (self.requestedId) {
                        self.details(self.requestedId);
                    }
                    var pagerOptions = {
                        // target the pager markup 
                        container: $('.pager'),
                        // output string - default is '{page}/{totalPages}'; possiblevariables: {page}, {totalPages},{startRow}, {endRow} and {totalRows}
                        output: '{startRow} to {endRow} ({totalRows})',
                        // starting page of the pager (zero based index)
                        page: 0,
                        // Number of visible rows - default is 10
                        size: 10
                    };
                    $('#datatable').tablesorter({widthFixed: true,
                        widgets: ['zebra']}).
                            tablesorterPager(pagerOptions);
                }
            });
        },
        details: function(id) {
            if (this.collection) {
                this.manufacturer = this.collection.get(id);
                if (this.view) {
                    this.view.close();
                }
                var self = this;
                this.view = new views.ModelView({
                    model: this.manufacturer,
                    // tpl-manufacturer-details is template identifier for chosen model element
                    templateName: '#tpl-manufacturer-details',
                    getHashObject: function() {
                        return self.getData();
                    }
                });
                $('#details').html(this.view.render().el);
            } else {
                this.requestedId = id;
                this.list();
            }
        },
        create: function() {
            if (this.view) {
                this.view.close();
            }
            var self = this;
            var dataModel = new models.Manufacturer();
            // see isNew() method implementation in the model
            dataModel.notSynced = true;
            this.view = new views.ModelView({
                model: dataModel,
                collection: this.collection,
                // tpl-manufacturer-details is a template identifier for chosen model element
                templateName: '#tpl-manufacturer-details',
                navigate: function(id) {
                    self.navigate(id, false);
                },
                getHashObject: function() {
                    return self.getData();
                }
            });
            $('#details').html(this.view.render().el);
        },
        getData: function() {
            return {
                manufacturerId: $('#manufacturerId').val(),
                addressline2: $('#addressline2').val(),
                zip: $('#zip').val(),
                phone: $('#phone').val(),
                addressline1: $('#addressline1').val(),
                fax: $('#fax').val(),
                email: $('#email').val(),
                name: $('#name').val(),
                state: $('#state').val(),
                city: $('#city').val(),
                rep: $('#rep').val()
            };
        }
    });
    new AppRouter();
    
    
    Backbone.history.start();
});
