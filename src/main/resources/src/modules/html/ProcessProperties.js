import $ from "jquery";
import { menu } from '../../config'
import { serverHost } from '../../serverConfig';
import Server from '../Server'

class ProcessProperties {
    constructor() {
        this.id = 'ProcessProperties';
        this.buildHtmlElement();
        this.modal.modal('handleUpdate');
    }

    show() {
        this.modal.modal('show');

        if (this.process != undefined && this.process.processData != undefined) {
            let pData = this.process.processData;
            $(`#${this.id}Command`).val(pData.command);
            $(`#${this.id}WorkingDir`).val(pData.workingdirectory);

            if (pData.modifiedEnvironment != "null" && pData.modifiedEnvironment != null)
                for (const key in pData.modifiedEnvironment) {
                    if (pData.modifiedEnvironment.hasOwnProperty(key)) {
                        const value = pData.modifiedEnvironment[key];
                        let container = $(`.${this.id}ModifiedVar:not(.updated)`).first();
                        if(container == null || container == undefined || container.length < 1){
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
                                <input id="${this.id}WorkingDir" type="text" class="form-control" placeholder="C:\\Program Files\\nodejs\\node.exe">
                                <small id="${this.id}WorkingDirHelp" class="form-text text-muted">A folder with resources.</small>
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
                "command": $(`#${this.id}Command`).val(),
                "workingdirectory": $(`#${this.id}WorkingDir`).val(),
                "modifiedEnvironment": Object.getOwnPropertyNames(modifiedEnvironment).length > 0 ? modifiedEnvironment : "null",
                "inheritIO": $(`#${this.id}InheritIO`).prop('checked') == true
            }

            console.log(`Saving new process... ${JSON.stringify(processDef)}`);

            this.process.processData = processDef;
            this.process.updateCommandLineText(processDef.command);

            Server.postToServer(processDef, 'admin/generic/aprocess', function (data, textStatus, jqXHR) {
                console.log('Process saved.');
                console.log(data);
                this.process.processData = data
            }.bind(this))
        }.bind(this));

        document.body.appendChild(this.modal[0]);

        return this.modal;
    }
}

const processProperties = new ProcessProperties();
// Object.freeze(ProcessProperties);
export { processProperties };