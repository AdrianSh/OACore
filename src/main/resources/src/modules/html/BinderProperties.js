import $ from "jquery";
import Server from './../Server'

class BinderProperties {
    constructor() {
        this.id = 'binderProperties';
        this.buildHtmlElement();
        this.modal.modal('handleUpdate');
        this.process1 = undefined;
        this.process2 = undefined;
        this.binder = undefined;
    }

    show() {
        this.modal.find('.modal-body').replaceWith(this.buildModalBodyHtml());
        this.modal.modal('show');

        if (this.binder.binderData != undefined) {
            let d = this.binder.binderData;
            console.log(`Showing binder from data: ${JSON.stringify(d)}`);
            this.modal.find(`#${this.id}binderTypeSelector`).val(d.binderType);
            this.buildBinderInputs(d.binderType);
            this.modal.find(`#${this.id}ProcessId`).val(d.processId['$oid']);

            if (d.runnableType != undefined)
                this.modal.find(`#${this.id}RunnableType`).val(d.runnableType);
            if (d.runnableId != undefined)
                this.modal.find(`#${this.id}RunnableId`).val(d.runnableId);

            if (d.subscriberId != undefined) {
                Server.getFromServer(sd => {
                    d.subscriber = sd;
                    this.modal.find(`#${this.id}SubscriberTypeSelector`).val(sd.subscriberType);
                    this.buildSubscriberInputs(sd.subscriberType);
                    if (sd.socket_port != undefined)
                        this.modal.find(`#${this.id}SubscriberSocketPort`).val(sd.socket_port);
                    if (sd.socket_host != undefined)
                        this.modal.find(`#${this.id}SubscriberSocketHost`).val(sd.socket_host);
                }, `admin/generic/ASubscriber/${d.subscriberId['$oid']}`);
            }

            if (d.publisherId != undefined) {
                Server.getFromServer(sd => {
                    d.publisher = sd;
                    this.modal.find(`#${this.id}PublisherTypeSelector1`).val(sd.publisherType);
                    this.buildPublisherInputs(sd.publisherType);
                    if (sd.socket_port != undefined)
                        this.modal.find(`#${this.id}PublisherSocketPort1`).val(sd.socket_port);
                    if (sd.socket_host != undefined)
                        this.modal.find(`#${this.id}PublisherSocketHost1`).val(sd.socket_host);
                }, `admin/generic/APublisher/${d.publisherId['$oid']}`);
            }

            if (d.stdInPublisherId != undefined) {
                Server.getFromServer(sd => {
                    d.stdInPublisherId = sd;
                    this.modal.find(`#${this.id}PublisherTypeSelector1`).val(sd.publisherType);
                    this.buildPublisherInputs(sd.publisherType, 1);
                    if (sd.socket_port != undefined)
                        this.modal.find(`#${this.id}PublisherSocketPort1`).val(sd.socket_port);
                    if (sd.socket_host != undefined)
                        this.modal.find(`#${this.id}PublisherSocketHost1`).val(sd.socket_host);
                }, `admin/generic/APublisher/${d.stdInPublisherId['$oid']}`);
            }

            if (d.stdInErrorPublisherId != undefined) {
                Server.getFromServer(sd => {
                    d.stdInErrorPublisherId = sd;
                    this.modal.find(`#${this.id}PublisherTypeSelector2`).val(sd.publisherType);
                    this.buildPublisherInputs(sd.publisherType, 2);
                    if (sd.socket_port != undefined)
                        this.modal.find(`#${this.id}PublisherSocketPort2`).val(sd.socket_port);
                    if (sd.socket_host != undefined)
                        this.modal.find(`#${this.id}PublisherSocketHost2`).val(sd.socket_host);
                }, `admin/generic/APublisher/${d.stdInErrorPublisherId['$oid']}`);
            }
        }
    }

    collectValues(cb) {
        let binder = {}
        binder['binderType'] = $(`#${this.id}binderTypeSelector`).val();

        switch (binder['binderType']) {
            case 'es.jovenesadventistas.arnion.process.binders.DirectStdInBinder':
                binder['processId'] = $(`#${this.id}ProcessId`).val();
                cb(binder);
                break;
            case 'es.jovenesadventistas.arnion.process.binders.ExitCodeBinder':
                binder['processId'] = $(`#${this.id}ProcessId`).val();
                try {
                    Server.postToServer(this.collectSubscriber(), 'admin/generic/ASubscriber', (data, status, jqXHR) => {
                        binder['subscriberId'] = data._id['$oid'];

                        Server.postToServer(this.collectPublisher(), 'admin/generic/APublisher', (data, status, jqXHR) => {
                            binder['publisherId'] = data._id['$oid'];
                            cb(binder);
                        });
                    });
                } catch (e) {
                    this.alert(`An error ocurred while saving Publishers... ${e}`, 'danger');
                }
                break;
            case 'es.jovenesadventistas.arnion.process.binders.RunnableBinder':
                binder['runnableType'] = $(`#${this.id}RunnableType`).val();
                this.alert(`Pending to check if exists...`);
                binder['runnableId'] = $(`#${this.id}RunnableId`).val();
                cb(binder);
                break;
            case 'es.jovenesadventistas.arnion.process.binders.StdInBinder':
                binder['processId'] = $(`#${this.id}ProcessId`).val();
                try {
                    Server.postToServer(this.collectPublisher(1), 'admin/generic/APublisher', (data, status, jqXHR) => {
                        binder['stdInPublisherId'] = data._id['$oid'];

                        Server.postToServer(this.collectPublisher(2), 'admin/generic/APublisher', (data, status, jqXHR) => {
                            binder['stdInErrorPublisherId'] = data._id['$oid'];
                            cb(binder);
                        }, (jqXHR, textStatus, errorThrown) => {
                            this.alert(`Couldn't save the Publisher, ${errorThrown}`);
                            binder = undefined;
                        });
                    }, (jqXHR, textStatus, errorThrown) => {
                        this.alert(`Couldn't save the Publisher, ${errorThrown}`);
                        binder = undefined;
                    });
                } catch (e) {
                    this.alert(`An error ocurred while saving Publishers... ${e}`, 'danger');
                }
                break;
            case 'es.jovenesadventistas.arnion.process.binders.StdOutBinder':
                binder['processId'] = $(`#${this.id}ProcessId`).val();
                cb(binder);
                break;
            default:
                this.alert(`Invalid binder type ${binder.binderType}`, 'danger');
                break;
        }
    }

    collectPublisher(num = '1') {
        let publisher = {};
        publisher['publisherType'] = $(`#${this.id}PublisherTypeSelector${num}`).val()
        switch (publisher['publisherType']) {
            case 'es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher':
                break;
            case 'es.jovenesadventistas.arnion.process.binders.Publishers.SocketListenerPublisher':
                publisher['socket_port'] = parseInt($(`#${this.id}PublisherSocketPort${num}`).val());
                publisher['socket_host'] = $(`#${this.id}PublisherSocketHost${num}`).val();
                break;
            case 'es.jovenesadventistas.arnion.process.binders.Publishers.SocketServerPublisher':
                publisher['socket_port'] = parseInt($(`#${this.id}PublisherSocketPort${num}`).val());
                break;
            default:
                this.alert(`Invalid publisher type ${publisher.publisherType}`, 'danger');
                throw `Cannot collect publisher ${num}`;
        }
        return publisher;
    }

    collectSubscriber(num = '') {
        let subscriber = {};
        subscriber['subscriberType'] = $(`#${this.id}SubscriberTypeSelector${num}`).val()
        switch (subscriber['subscriberType']) {
            case 'es.jovenesadventistas.arnion.process.binders.Subscribers.ConcurrentLinkedQueueSubscriber':
                break;
            case 'es.jovenesadventistas.arnion.process.binders.Subscribers.SocketSubscriber':
                subscriber['socket_port'] = parseInt($(`#${this.id}SubscriberSocketPort${num}`).val());
                subscriber['socket_host'] = $(`#${this.id}SubscriberSocketHost${num}`).val();
                break;
            case 'es.jovenesadventistas.arnion.process.binders.Subscribers.SocketServerSubscriber':
                subscriber['socket_port'] = parseInt($(`#${this.id}SubscriberSocketPort${num}`).val());
                break;
            default:
                this.alert(`Invalid subscriber type ${subscriber.subscriberType}`, 'danger');
                throw `Cannot collect subscriber ${num}`;
        }
        return subscriber;
    }

    alert(msg, type = 'primary') {
        let alert = $(`<div class="alert alert-${type}" role="alert">${msg}</div>`);
        this.modal.find(`#${this.id}Alerts`).append(alert);
        setTimeout(function () {
            alert.remove();
        }, 10000);
    }

    buildModalBodyHtml() {
        return `    <div class="modal-body">
                        ${ /* <div class="progress">
                            <div class="progress-bar progress-bar-striped bg-success" role="progressbar" style="width: 25%"
                                aria-valuenow="25" aria-valuemin="0" aria-valuemax="100"></div>
                        </div> */ ''}

                        <h6 class="mt-3">${this.process1 == undefined ? undefined : this.process1.processData.command} <!-- <small class="text-muted">-t a s</small> -->
                            <span class="badge badge-primary">Origin</span></h6>
                        <h6>${this.process2 == undefined ? undefined : this.process2.processData.command} <!-- <small class="text-muted">-t a s</small> -->
                            <span class="badge badge-secondary">End</span></h6>

                        <form class="mt-3">
                            <div class="form-group">
                                <label for="${this.id}binderTypeSelector">Binder type</label>
                                ${this.binderTypeSelector()}
                                <small id="emailHelp" class="form-text text-muted">Defines the link between the processes</small>
                            </div>
                            <div class="form-group" id="${this.id}BinderInputs">
                                
                            </div>
                        </form>
                    </div>`
    }

    buildHtml() {
        return `<div class="modal fade" id="${this.id}" tabindex="-1" role="dialog" aria-labelledby="${this.id}CenterTitle" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div id="${this.id}Alerts"></div>
                    <div class="modal-header">
                        <h5 class="modal-title">Binder properties</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">×</span>
                        </button>
                    </div>
                    ${this.buildModalBodyHtml()}
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary" id="${this.id}Save">Save changes</button>
                    </div>
                </div>
            </div>
        </div>`
    }

    buildBinderInputs(type) {
        let html = ``;
        switch (type) {
            case 'es.jovenesadventistas.arnion.process.binders.DirectStdInBinder':
                html += `
                    <div class="form-group">
                        <label for="${this.id}ProcessId">Process </label>
                        <select class="form-control" id="${this.id}ProcessId">
                            <option value="${this.process1.processData._id['$oid']}">1: ${this.process1.processData.command}</option>
                            <option value="${this.process2.processData._id['$oid']}">2: ${this.process2.processData.command}</option>
                        </select>
                        <small id="${this.id}ProcessIdHelp" class="form-text text-muted">Process to read</small>
                    </div>`;
                break;
            case 'es.jovenesadventistas.arnion.process.binders.ExitCodeBinder':
                html += `
                    <div class="form-group">
                        <label for="${this.id}ProcessId">Process </label>
                        <select class="form-control" id="${this.id}ProcessId">
                            <option value="${this.process1.processData._id['$oid']}">1: ${this.process1.processData.command}</option>
                            <option value="${this.process2.processData._id['$oid']}">2: ${this.process2.processData.command}</option>
                        </select>
                        <small id="${this.id}ProcessIdHelp" class="form-text text-muted">Process to read</small>
                    </div>
                    ${this.subscriberTypeSelector()}
                    ${this.publisherTypeSelector()}`;
                break;
            case 'es.jovenesadventistas.arnion.process.binders.RunnableBinder':
                html += `
                    <div class="form-group">
                        <label for="${this.id}RunnableType">Runnable type</label>
                        <select class="form-control" id="${this.id}RunnableType">
                            <option value="Binder">Binder</option>
                            <option value="APublisher">Publisher</option>
                            <option value="ASubscriber">Subscriber</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="${this.id}RunnableId">Runnable Id </label>
                        <input id="${this.id}RunnableId" type="text" class="form-control" placeholder="Binder ID, Publisher ID, Subscriber ID, ...">
                        <small id="${this.id}RunnableIdHelp" class="form-text text-muted">To be executed</small>
                    </div>`;
                break;
            case 'es.jovenesadventistas.arnion.process.binders.StdInBinder':
                html += `
                    <div class="form-group">
                        <label for="${this.id}ProcessId">Process </label>
                        <select class="form-control" id="${this.id}ProcessId">
                            <option value="${this.process1.processData._id['$oid']}">1: ${this.process1.processData.command}</option>
                            <option value="${this.process2.processData._id['$oid']}">2: ${this.process2.processData.command}</option>
                        </select>
                        <small id="${this.id}ProcessIdHelp" class="form-text text-muted">Process to read</small>
                    </div>
                    ${this.publisherTypeSelector(1, 'Std in publisher')}
                    ${this.publisherTypeSelector(2, 'Std in error publisher')}`
                break;
            case 'es.jovenesadventistas.arnion.process.binders.StdOutBinder':
                html += `
                    <div class="form-group">
                        <label for="${this.id}ProcessId">Process </label>
                        <select class="form-control" id="${this.id}ProcessId">
                            <option value="${this.process1.processData._id['$oid']}">1: ${this.process1.processData.command}</option>
                            <option value="${this.process2.processData._id['$oid']}">2: ${this.process2.processData.command}</option>
                        </select>
                        <small id="${this.id}ProcessIdHelp" class="form-text text-muted">Process to read</small>
                    </div>`
                break;
            default:
                return;
        }
        $(`#${this.id}BinderInputs`).html(html);
    }

    buildSubscriberInputs(type) {
        let html = ``;
        switch (type) {
            case 'es.jovenesadventistas.arnion.process.binders.Subscribers.ConcurrentLinkedQueueSubscriber':
                break;
            case 'es.jovenesadventistas.arnion.process.binders.Subscribers.SocketServerSubscriber':
                html = `<div class="form-group">
                        <label for="${this.id}SubscriberSocketPort">Socket Port</label>
                        <input id="${this.id}SubscriberSocketPort" type="number" class="form-control" value="21" placeholder="port">
                    </div>`;
                break;
            case 'es.jovenesadventistas.arnion.process.binders.Subscribers.SocketSubscriber':
                html += `<div class="form-group">
                        <label for="${this.id}SubscriberSocketHost">Socket Host</label>
                        <input id="${this.id}SubscriberSocketHost" type="text" class="form-control" value="127.0.0.1" placeholder="x.x.x.x">
                    </div>
                    <div class="form-group">
                        <label for="${this.id}SubscriberSocketPort">Socket Port</label>
                        <input id="${this.id}SubscriberSocketPort" type="number" class="form-control" value="21" placeholder="port">
                    </div>`;
                break;
            default:
                return;
        }
        $(`#${this.id}SubscriberTypeInputs`).html(html);
    }

    buildPublisherInputs(type, num = 1) {
        let html = ``;
        switch (type) {
            case 'es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher':
                break;
            case 'es.jovenesadventistas.arnion.process.binders.Publishers.SocketListenerPublisher':
                html += `<div class="form-group">
                    <label for="${this.id}PublisherSocketHost${num}">Socket Host</label>
                    <input id="${this.id}PublisherSocketHost${num}" type="text" class="form-control" value="127.0.0.1" placeholder="x.x.x.x">
                </div>
                <div class="form-group">
                    <label for="${this.id}PublisherSocketPort${num}">Socket Port</label>
                    <input id="${this.id}PublisherSocketPort${num}" type="number" class="form-control" value="21" placeholder="port">
                </div>`;
                break;
            case 'es.jovenesadventistas.arnion.process.binders.Publishers.SocketServerPublisher':
                html += `<div class="form-group">
                    <label for="${this.id}PublisherSocketPort${num}">Socket Port</label>
                    <input id="${this.id}PublisherSocketPort${num}" type="number" class="form-control" value="21" placeholder="port">
                </div>`;
                break;
            default:
                return;
        }
        $(`#${this.id}PublisherTypeInputs${num}`).html(html);
    }
    binderTypeSelector() {
        return `<select class="form-control" id="${this.id}binderTypeSelector">
            <option value="">Select</option>
            <option value="es.jovenesadventistas.arnion.process.binders.DirectStdInBinder">DirectStdInBinder</option>
            <option value="es.jovenesadventistas.arnion.process.binders.ExitCodeBinder">ExitCodeBinder</option>
            <option value="es.jovenesadventistas.arnion.process.binders.RunnableBinder">RunnableBinder</option>
            <option value="es.jovenesadventistas.arnion.process.binders.StdInBinder">StdInBinder</option>
            <option value="es.jovenesadventistas.arnion.process.binders.StdOutBinder">StdOutBinder</option>
        </select>`;
    }
    subscriberTypeSelector() {
        return `<div class="form-group">
            <label for="${this.id}SubscriberTypeSelector">Subscriber</label>
            <select class="form-control" id="${this.id}SubscriberTypeSelector">
                <option value="">Select</option>
                <option value="es.jovenesadventistas.arnion.process.binders.Subscribers.ConcurrentLinkedQueueSubscriber">ConcurrentLinkedQueueSubscriber</option>
                <option value="es.jovenesadventistas.arnion.process.binders.Subscribers.SocketServerSubscriber">SocketServerSubscriber</option>
                <option value="es.jovenesadventistas.arnion.process.binders.Subscribers.SocketSubscriber">SocketSubscriber</option>
                <!-- <option value="es.jovenesadventistas.arnion.process.binders.Subscribers.TransferStoreSubscriber">TransferStoreSubscriber</option> -->
            </select></div><div class="form-group" id="${this.id}SubscriberTypeInputs"></div>`;
    }
    publisherTypeSelector(num = '1', helpText = '') {
        let html = `<div class="form-group">
            <label for="${this.id}PublisherTypeSelector${num}">Publisher</label>
            <select class="form-control" id="${this.id}PublisherTypeSelector${num}">
                <option value="">Select</option>
                <option value="es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher">ConcurrentLinkedQueuePublisher</option>
                <option value="es.jovenesadventistas.arnion.process.binders.Publishers.SocketListenerPublisher">SocketListenerPublisher</option>
                <option value="es.jovenesadventistas.arnion.process.binders.Publishers.SocketServerPublisher">SocketServerPublisher</option>
            </select>`;
        if (helpText.length > 0)
            html += `<small id="${this.id}PublisherTypeSelector${num}Help" class="form-text text-muted">${helpText}</small>`;
        html += `</div><div class="form-group" id="${this.id}PublisherTypeInputs${num}"></div>`;
        return html;
    }

    buildHtmlElement() {
        $('body').on('change', `#${this.id}binderTypeSelector`, e => {
            e.preventDefault();
            this.buildBinderInputs(e.target.value);
            console.log(` Binder type selected... ${e.target.value}`);
        })

        $('body').on('change', `#${this.id}SubscriberTypeSelector`, e => {
            e.preventDefault();
            this.buildSubscriberInputs(e.target.value);
            console.log(` Subscriber type selected... ${e.target.value}`);
        })

        $('body').on('change', `#${this.id}PublisherTypeSelector1`, e => {
            e.preventDefault();
            this.buildPublisherInputs(e.target.value, 1);
            console.log(` Publisher type selected... ${e.target.value}`);
        })

        $('body').on('change', `#${this.id}PublisherTypeSelector2`, e => {
            e.preventDefault();
            this.buildPublisherInputs(e.target.value, 2);
            console.log(` Publisher type selected... ${e.target.value}`);
        })

        this.modal = $(this.buildHtml());
        document.body.appendChild(this.modal[0]);

        this.modal.on('click', `#${this.id}Save`, e => {
            e.preventDefault();
            this.collectValues(b => {
                if (b != undefined) {
                    this.binder.binderData = b;
                    Server.postToServer(b, 'admin/generic/binder', (data) => {
                        this.binder.binderData = data;
                        this.alert(`Binder saved!`);
                    }, (jqXHR, status, e) => {
                        this.alert(`Couldn't save the binder, ${e}`, 'danger');
                    });
                }
            })
        });

        return this.modal;
    }
}

const binderProperties = new BinderProperties();
// Object.freeze(binderProperties);
export { binderProperties };