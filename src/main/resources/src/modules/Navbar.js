import $ from "jquery";
import {menu} from '../config'

class Navbar {
    constructor(){
        this.buildHtmlElement();
    }

    buildButton(value, className = 'btn-primary', extraAttributes = ''){
        return `<button type="button" class="btn btn-sm ${className}" ${extraAttributes}>${value}</button>`;
    }

    buildHtmlElement(){
        this.nav = $(`<div class="position-absolute">
        <nav class="navbar navbar-expand-lg">
            <div class="collapse navbar-collapse" id="navbarText">
                <form class="form-inline">
                </form>
            </div>
        </nav>`);

        let form = this.nav.find(`form.form-inline`);
        menu.forEach(b => {
            let button = $(this.buildButton.apply(this, b.button));
            button.on('pointerdown', function(){
                b.action.apply(this, arguments);
            }.bind(this))
            form.append(button);
        });
        document.body.appendChild(this.nav[0]);

        this.nav.hide();
        return this.nav;
    }
}

const navbar = new Navbar();
// Object.freeze(navbar);
export {navbar};