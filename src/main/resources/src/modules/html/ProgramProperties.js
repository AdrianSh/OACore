import $ from "jquery";
import { menu } from '../../config'
import { serverHost } from '../../serverConfig';
import Server from './../Server'

class ProgramProperties {
    constructor() {
        this.id = 'ProgramProperties';
        this.buildHtmlElement();
        this.modal.modal('handleUpdate');
    }

    show() {
        this.modal.modal('show');
    }

    buildHtmlElement() {
        this.modal = $(`
        <div class="modal fade" id="${this.id}" tabindex="-1" role="dialog" aria-labelledby="${this.id}CenterTitle" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Program properties</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div class="progress">
                            <div class="progress-bar progress-bar-striped bg-success" role="progressbar" style="width: 25%"
                                aria-valuenow="25" aria-valuemin="0" aria-valuemax="100"></div>
                        </div>

                        <h6 class="mt-3">c:/Program Files/lolamento <small class="text-muted">-t a s</small>
                            <span class="badge badge-primary">Origin</span></h6>
                        <h6>c:/Program Files/lolamento <small class="text-muted">-t a s</small>
                            <span class="badge badge-secondary">End</span></h6>

                        <form class="mt-3">
                            <div class="form-group">
                                <label for="exampleInputEmail1">Program type</label>
                                <select class="form-control" id="binderTypeSelector">
                                </select>
                                <small id="emailHelp" class="form-text text-muted">Defines the link between the programs</small>
                            </div>
                            <div class="form-group">
                                <label for="exampleInputPassword1">Runnable</label>
                                <select class="form-control">
                                    <option>SocketListenerPublisher</option>
                                </select>
                            </div>
                            <div class="form-row">
                                <div class="col">
                                    <input type="text" class="form-control" placeholder="Hostname">
                                </div>
                                <div class="col">
                                    <input type="text" class="form-control" placeholder="Port">
                                </div>
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
            this.program.destroy();
        }.bind(this));

        this.modal.on('hidden.bs.modal', function () {
            console.log('Closing modal....');
            console.log(this.program);
        }.bind(this));

        this.modal.find(`#${this.id}Save`).on('click', function (e) {
            Server.postToServer({
                "command": "C:\\\\Program Files\\\\nodejs\\\\node.exe index.js write output1.txt",
                "workingdirectory": "C:\\Privado\\TFG\\Arnion-Processes\\File",
                "modifiedEnvironment": "null",
                "inheritIO": false
            }, 'admin/generic/aprocess', function (data, textStatus, jqXHR){
                console.log('Process saved.');
                console.log(data);
            })
        }.bind(this));

        let binderTypeSelector = this.modal.find(`#binderTypeSelector`);

        // 

        document.body.appendChild(this.modal[0]);

        return this.modal;
    }
}

const programProperties = new ProgramProperties();
// Object.freeze(ProgramProperties);
export { programProperties };