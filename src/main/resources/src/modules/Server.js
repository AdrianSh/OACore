import $ from "jquery";
import { serverHost, sampleToken } from './../config'

class Server {
    constructor(CSRFToken = '') {
        this.serverHost = serverHost;
        this.binderTypes = [];
        this.getCSRFToken();

        if(this.CSRFToken == undefined || this.CSRFToken == null || this.CSRFToken.length < 1){
            this.CSRFToken = sampleToken;
            this.CSRFTokenHeader = 'X-CSRF-TOKEN';
        }
    }

    beforeSend(jqXHR, settings) {
        // unsafe: jqXHR.setRequestHeader('Origin', serverHost);
        jqXHR.setRequestHeader('Content-Type', 'application/json');
        jqXHR.setRequestHeader('X-CSRF-HEADER', 'X-CSRF-TOKEN');
        jqXHR.setRequestHeader('X-CSRF-PARAM', this.CSRFTokenHeader);
        jqXHR.setRequestHeader(this.CSRFTokenHeader, this.CSRFToken);
    }

    getCSRFToken() {
        this.CSRFToken = $("meta[name='csrf']").attr("content");
        this.CSRFTokenHeader = $("meta[name='csrf_header']").attr("content");
    }

    getFromServer(successCallBack, url = 'admin/generic/x', data = undefined, thenFun = function () { }) {
        $.ajax({
            beforeSend: this.beforeSend.bind(this),
            url: `${serverHost}/${url}`,
            data: data,
            success: successCallBack,
        }).then(thenFun);
    }

    postToServer(data, url = 'admin/generic/x', successCallBack = function (data, textStatus, jqXHR){}, doneFun = function () { }) {
        $.ajax({
            beforeSend: this.beforeSend.bind(this),
            method: 'POST',
            url: `${serverHost}/${url}`,
            data: data,
            success: successCallBack
        }).done(doneFun);
    }
}

const server = new Server();
export default server;