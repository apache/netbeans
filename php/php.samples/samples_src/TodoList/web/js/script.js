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

$(document).ready(function() {
    initDatepicker();
    initFlashes();
    initErrorFields();
    initChangeStatusDialog();
    initDeleteDialog();
});

function initDatepicker() {
    $('.datepicker')
        .attr('readonly', 'readonly')
        .datepicker({
            dateFormat: 'yy-m-d'
        });
}

function initFlashes() {
    var flashes = $("#flashes");
    if (!flashes.length) {
        return;
    }
    setTimeout(function() {
        flashes.slideUp("slow");
    }, 2000);
}

function initErrorFields() {
    $('.error-field').first().focus();
}

function initDeleteDialog() {
    var deleteDialog = $('#delete-dialog');
    var deleteLink = $('#delete-link');
    deleteDialog.dialog({
        autoOpen: false,
        modal: true,
        width: 476,
        buttons: {
            'OK': function() {
                $(this).dialog('close');
                location.href = deleteLink.attr('href');
            },
            'Cancel': function() {
                $(this).dialog('close');
            }
        }
    });
    deleteLink.click(function() {
        deleteDialog.dialog('open');
        return false;
    });
}

function initChangeStatusDialog() {
    var changeStatusDialog = $('#change-status-dialog');
    var changeStatusLink = $('.change-status-link');
    var changeStatusForm = $('#change-status-form');
    changeStatusDialog.dialog({
        autoOpen: false,
        modal: true,
        width: 476,
        buttons: {
            'OK': function() {
                changeStatusForm.submit();
                $(this).dialog('close');
            },
            'Cancel': function() {
                $(this).dialog('close');
            }
        }
    });
    changeStatusLink.click(function() {
        changeStatusForm.attr('action', $(this).attr('href'));
        changeStatusDialog.dialog('option', 'title', $(this).attr('title'));
        changeStatusDialog.dialog('open');
        return false;
    });
}
