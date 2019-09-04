import $ from "jquery";
import { menu } from '../../config'
import { serverHost } from '../../serverConfig';
import Server from '../Server'
import {app, saveWorkflow} from './../../index'

class ProcessProperties {
    constructor() {
        this.id = 'ProcessProperties';
        this.buildHtmlElement();
        this.modal.modal('handleUpdate');
    }

    hide() {
        this.modal.modal('hide');
    }

    show() {
        this.modal.modal('show');

        if (this.process != undefined && this.process.processData != undefined) {
            let pData = this.process.processData;
            $(`#${this.id}Command`).val(pData.command);
            $(`#${this.id}WorkingDir`).val(pData.workingdirectory);
            this.buildExecutorServicesOptions();
            let execServ = app.workflowData.executorAssigned[this.process._getId()];
            if(execServ != undefined)
                $(`#${this.id}ExecutorService`).val(execServ);

            if (pData.modifiedEnvironment != "null" && pData.modifiedEnvironment != null)
                for (const key in pData.modifiedEnvironment) {
                    if (pData.modifiedEnvironment.hasOwnProperty(key)) {
                        const value = pData.modifiedEnvironment[key];
                        let container = $(`.${this.id}ModifiedVar:not(.updated)`).first();
                        if (container == null || container == undefined || container.length < 1) {
                            $(`#${this.id}ModifiedVar`).append(
                                `<div class="form-row form-group ${this.id}ModifiedVar updated">
                                    <div class="col">
                                        <input type="text" class="form-control form-control-sm" value="${key}" placeholder="Variable">
                                    </div>
                                    <div class="col">
                                        <input type="text" class="form-control form-control-sm" value="${value}" placeholder="Value">
                                    </div>
                                </div>`);
                        } else {
                            container.find('input[placeholder="Variable"]').val(key);
                            container.find('input[placeholder="Value"]').val(value);
                            container.addClass('updated');
                        }
                    }
                }

            $(`#${this.id}InheritIO`).prop('checked', pData.inheritIO);
        }
    }

    buildExecutorServicesOptions(){
        $(`#${this.id}ExecutorService`).empty();
        for (let i = 0; i < app.workflowData.executorServices.length; i++) {
            $(`#${this.id}ExecutorService`).append(`<option value="${i}">${i}: ${app.workflowData.executorServices[i]}</option>`);
        }
    }

    buildHtmlElement() {
        this.modal = $(`
        <div class="modal fade" id="${this.id}" tabindex="-1" role="dialog" aria-labelledby="${this.id}CenterTitle" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Process properties</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <form class="mt-3">
                            <div class="form-group">
                                <label for="${this.id}Command">Command line</label>
                                <input id="${this.id}Command" type="text" class="form-control" placeholder="Command line">
                                <small id="${this.id}CommandHelp" class="form-text text-muted">Process to be build.</small>
                            </div>
                            <div class="form-group">
                                <label for="${this.id}WorkingDir">Working Directory</label>
                                <input id="${this.id}WorkingDir" type="text" class="form-control" placeholder="C:\\Program Files\\nodejs\\">
                                <small id="${this.id}WorkingDirHelp" class="form-text text-muted">A folder with resources.</small>
                            </div>
                            <div class="form-group">
                                <label for="${this.id}ExecutorService">Executor Service</lavel>
                                <select class="form-control" id="${this.id}ExecutorService"></select>
                            </div>
                            <div class="form-group" id="${this.id}ModifiedVar">
                                <label for="${this.id}ModifiedVar">Modified Environment</label>
                                <div class="form-row form-group ${this.id}ModifiedVar">
                                    <div class="col">
                                        <input type="text" class="form-control form-control-sm" placeholder="Variable">
                                    </div>
                                    <div class="col">
                                        <input type="text" class="form-control form-control-sm" placeholder="Value">
                                    </div>
                                </div>
                            </div>
                            <div class="form-group mb-2">
                                <button class="btn btn-sm btn-primary" id="${this.id}AddModifiedVar">Add var</button>
                            </div>

                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" value="" id="${this.id}InheritIO">
                                <label class="form-check-label" for="${this.id}InheritIO">Inherit IO</label>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <!-- <button type="button" id="${this.id}Close" class="btn btn-secondary" data-dismiss="modal">Close</button> -->
                        <button type="button" id="${this.id}Cancel" class="btn btn-warning" data-dismiss="modal">Cancel</button>
                        <button type="button" id="${this.id}Save" class="btn btn-primary">Save changes</button>
                    </div>
                </div>
            </div>
        </div>`);

        this.modal.find(`#${this.id}Cancel`).on('click', function (e) {
            e.preventDefault();
            if (this.process.processData == undefined)
                this.process.destroy();
        }.bind(this));

        this.modal.on('hidden.bs.modal', function () {
            if (this.process.processData == undefined)
                this.process.destroy();

            $(`#${this.id}ModifiedVar`).html(`
            <div class="form-group" id="${this.id}ModifiedVar">
                <label for="${this.id}ModifiedVar">Modified Environment</label>
                <div class="form-row form-group ${this.id}ModifiedVar">
                    <div class="col">
                        <input type="text" class="form-control form-control-sm" placeholder="Variable">
                    </div>
                    <div class="col">
                        <input type="text" class="form-control form-control-sm" placeholder="Value">
                    </div>
                </div>
            </div>`);

        }.bind(this));

        this.modal.find(`#${this.id}AddModifiedVar`).on('click', function (e) {
            e.preventDefault();
            $(`.${this.id}ModifiedVar`).last().clone().appendTo($(`#${this.id}ModifiedVar`));
        }.bind(this));

        this.modal.find(`#${this.id}Save`).on('click', function (e) {
            let modifiedEnvironment = {};
            let modifiedEnvironmentVarsInput = $(`.${this.id}ModifiedVar`).toArray();
            for (const i in modifiedEnvironmentVarsInput) {
                const e = $(modifiedEnvironmentVarsInput[i]);
                modifiedEnvironment[e.find('input[placeholder="Variable"]').val()] = e.find('input[placeholder="Value"]').val();
            }

            let processDef = {
                "command": $(`#${this.id}Command`).val().trim(),
                "workingdirectory": $(`#${this.id}WorkingDir`).val(),
                "modifiedEnvironment": Object.getOwnPropertyNames(modifiedEnvironment).length > 0 ? modifiedEnvironment : "null",
                "inheritIO": $(`#${this.id}InheritIO`).prop('checked') == true
            }

            if (this.process.processData != undefined && this.process.processData._id != undefined && this.process.processData._id['$oid'] != undefined) {
                processDef['_id'] = {};
                processDef._id['$oid'] = this.process.processData._id['$oid'];
                console.log(`Updating process... ${JSON.stringify(processDef)}`);
                Server.putToServer(processDef, 'admin/generic/aprocess', function (data, textStatus, jqXHR) {
                    console.log('Process updated.');
                    console.log(data);
                    this.process.processData = data;
                    this.process.updateCommandLineText(data.command.trim());
                    app.workflowData.executorAssigned[this.process.processData._id['$oid']] = $(`#${this.id}ExecutorService`).val();
                    saveWorkflow();
                    this.hide();
                }.bind(this))
            } else {
                console.log(`Saving new process... ${JSON.stringify(processDef)}`);
                Server.postToServer(processDef, 'admin/generic/aprocess', function (data, textStatus, jqXHR) {
                    this.process.processData = data;
                    this.process.updateCommandLineText(data.command);

                    app.workflowData.processes[data['_id']['$oid']] = this.process;
                    app.workflowData.executorAssigned[data['_id']['$oid']] = $(`#${this.id}ExecutorService`).val();
                    saveWorkflow();

                    console.log('Process saved.');
                    console.log(data);
                    this.hide();
                }.bind(this))
            }

        }.bind(this));

        document.body.appendChild(this.modal[0]);

        return this.modal;
    }
}

const processProperties = new ProcessProperties();
// Object.freeze(ProcessProperties);
export { processProperties };