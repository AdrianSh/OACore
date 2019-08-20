import { Binder } from './modules/Binder';
import { Program } from './modules/Program';
import { binderProperties } from './modules/BinderProperties';
import { mainContainer } from './index';

const menu = [
    {
        button: ['Bind', 'btn-primary'], action: function (e) {
            if (this.lastProgram != undefined && this.lastProgram instanceof Program) {
                if (this.lastProgram == this.program)
                    console.warn(`You should bind two different programs...`);
                else {
                    binderProperties.show();

                    let b = new Binder(this.lastProgram, this.program);
                    mainContainer.addChild(b);
                    this.program.addInputBinder(b);
                    this.lastProgram.addOutputBinder(b);
                }
            }
            this.lastProgram = this.program;
        }
    },
    {
        button: ['Properties', 'btn-info'], action: function(){
            
        }
    },
    {
        button: ['Remove', 'btn-danger'], action: function(){
            this.program.destroy();
        }
    },
    {
        button: ['X', 'btn-light'], action: function(){
            this.nav.hide();
        }
    }
];

export { menu }