import * as PIXI from 'pixi.js'
import { JoiningArrow } from './modules/PIXI/JoiningArrow';
import { Process } from './modules/PIXI/Process';
import { mainContainer, app } from './index';
import { binderProperties } from './modules/html/BinderProperties';
import { processProperties } from './modules/html/ProcessProperties';


const menu = [
    {
        button: ['Bind', 'btn-primary'], action: function (e) {
            if (this.lastProcess != undefined && this.lastProcess instanceof Process) {
                if (this.lastProcess == this.process)
                    console.warn(`You should bind two different processes...`);
                else {
                    binderProperties.process1 = this.lastProcess;
                    binderProperties.process2 = this.process;

                    let b = new JoiningArrow(this.lastProcess, this.process);
                    mainContainer.addChild(b);

                    binderProperties.joiningArrow = b;
                    binderProperties.show();
                    this.process.addInputBinder(b);
                    this.lastProcess.addOutputBinder(b);
                }
            }
            this.lastProcess = this.process;
        }
    },
    {
        button: ['Properties', 'btn-info'], action: function () {
            if (this.process == undefined && this.joiningArrow != undefined) {
                console.log(`Process undefined... this navbar is not used for Joining Arrows (deprecated)`);
            } else if (this.process != undefined) {
                processProperties.process = this.process;
                processProperties.show();
            }
        }
    },
    {
        button: ['Remove', 'btn-danger'], action: function () {
            if (this.process != undefined) this.process.destroy();
            if (this.joiningArrow != undefined) this.joiningArrow.destroy();
        }
    },
    {
        button: ['X', 'btn-light'], action: function () {
            this.nav.hide();
        }
    }
];

export { menu };