import UI from '../UI';
import './set_theory.css';

const INFO = {
  title: 'Stealing from the shoulders of giants',
  html: `
<p>There are already some tools doing a great job for playing around with set theory:</p>
<ul>
<li><a href="https://www.mta.ca/pc-set/calculator/pc_calculate.html" target="_blank">David Walters' PC Set Calculator</a>. Just excellent.</li>
<li><a href="https://github.com/remy/jsconsole" target="_blank">@rem's</a>
 <a href="https://jsconsole.com/" target="_blank">JS console</a> <a href="https://github.com/remy/jsconsole" target="_blank">(github)</a>. 
 A generic JS console, in case you want to do more weird stuff with your sets programatically,
 or convert your sets to the music formats used in other tabs.</li>
</ul>
<p>Kudos to them :)</p>
`
};

/**
 * Gathers together some nice tools for set theory from another websites.
 *
 * @param parent The DOM element to put all the embedded frames.
 */
function create (parent) {
  const container = UI.Div(parent).withClassName('setTheoryContainer');
  UI.Frame(container, 'https://www.mta.ca/pc-set/calculator/pc_calculate.html').withClassName('pcSetCalculatorFrame');
  UI.Frame(container, 'https://jsconsole.com/').withClassName('jsConsoleFrame');
}

export default { create, INFO };
