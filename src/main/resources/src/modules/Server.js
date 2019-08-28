import $ from "jquery";
import { serverHost, sampleToken } from './../config'

class Server {
    constructor(CSRFToken = '') {
        this.serverHost = serverHost;
        this.binderTypes = [];
        this.getCSRFToken();

        if (this.CSRFToken == undefined || this.CSRFToken == null || this.CSRFToken.length < 1) {
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

    async getFromServer(successCallBack = function (data, textStatus, jqXHR) { }, url = 'admin/generic/x', data = undefined, errorCallBack = function (jqXHR, textStatus, errorThrown) {
        console.error(errorThrown);
    }, thenFun = function () { }) {
        try {
            await $.ajax({
                beforeSend: this.beforeSend.bind(this),
                url: `${serverHost}/${url}`,
                data: data,
                success: function (data, textStatus, jqXHR) {
                    try {
                        let pData = JSON.parse(data);
                        successCallBack(pData, textStatus, jqXHR);
                    } catch (e) {
                        console.error(`A response from the server doesn´t contains a valid JSON document: \n${data}`);
                    }
                },
                error: errorCallBack
            }).then(thenFun);
        } catch (e) {
            console.error(e);
        }
    }

    async postToServer(data, url = 'admin/generic/x', successCallBack = function (data, textStatus, jqXHR) { }, errorCallBack = function (jqXHR, textStatus, errorThrown) {
        console.error(errorThrown);
    }, doneFun = function () { }) {
        try {
            await $.ajax({
                beforeSend: this.beforeSend.bind(this),
                method: 'POST',
                url: `${serverHost}/${url}`,
                dataType: 'json',
                contentType: 'application/json',
                data: JSON.stringify(data),
                success: successCallBack,
                error: errorCallBack
            }).done(doneFun);
        } catch (e) {
            console.error(e);
        }
    }

    async putToServer(data, url = 'admin/generic/x', successCallBack = function (data, textStatus, jqXHR) { }, errorCallBack = function (jqXHR, textStatus, errorThrown) {
        console.error(errorThrown);
    }, doneFun = function () { }) {
        try {
            await $.ajax({
                beforeSend: this.beforeSend.bind(this),
                method: 'PUT',
                url: `${serverHost}/${url}`,
                dataType: 'json',
                contentType: 'application/json',
                data: JSON.stringify(data),
                success: successCallBack,
                error: errorCallBack
            }).done(doneFun);
        } catch (e) {
            console.error(e);
        }
    }

    async deleteToServer(url = 'admin/generic/x', successCallBack = function (data, textStatus, jqXHR) { }, errorCallBack = function (jqXHR, textStatus, errorThrown) {
        console.error(errorThrown);
    }, doneFun = function () { }) {
        try {
            await $.ajax({
                beforeSend: this.beforeSend.bind(this),
                method: 'DELETE',
                url: `${serverHost}/${url}`,
                dataType: 'json',
                contentType: 'application/json',
                success: successCallBack,
                error: errorCallBack
            }).done(doneFun);
        } catch (e) {
            console.error(e);
        }
    }
}

const server = new Server();
export default server;