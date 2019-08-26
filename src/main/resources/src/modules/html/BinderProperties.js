import $ from "jquery";
import Server from './../Server'

class BinderProperties {
    constructor() {
        this.id = 'binderProperties';
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
                        <h5 class="modal-title">Binder properties</h5>
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
                                <label for="exampleInputEmail1">Binder type</label>
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
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary">Save changes</button>
                    </div>
                </div>
            </div>
        </div>`);

        let binderTypeSelector = this.modal.find(`#binderTypeSelector`);

        Server.getFromServer(function (data, textStatus, jqXHR) {
            data.forEach(bName => {
                binderTypeSelector.append($(`<option value="${bName}">${bName}</option>`)[0]);
            });
        }, 'admin/binders');

        binderTypeSelector.on('change', e => {
            e.preventDefault();

            console.log(` Binder type selected... ${e.target.value}`);
        })


        document.body.appendChild(this.modal[0]);

        return this.modal;
    }
}

const binderProperties = new BinderProperties();
// Object.freeze(binderProperties);
export { binderProperties };