import UI from './UI';
import Swal from 'sweetalert2';
import Tabs from './Tabs';

import BallBounce from './use_cases/ball_bounce';
import RandomNotes from './use_cases/random';
import TimeSteps from './use_cases/time_steps';
import VivierColors from './use_cases/vivier';
import SetTheory from './use_cases/set_theory';
import MidiAnalysis from './use_cases/midi_analysis';
import DigitizeLines from './use_cases/digitize';
import ExternalLinks from './use_cases/external_links';

import './css/main.css';

const tabs = new Tabs(document.body);

function addTab (title, id, useCase) {
  const tab = tabs.add(title).withAttribute('id', id);
  if (useCase.INFO) {
    UI.Div(tab).withClassName('popup-question-mark').withText('?').withEventListener('click', () => {
      Swal.fire({
        title: useCase.INFO.title,
        html: useCase.INFO.html,
        icon: 'info',
        confirmButtonText: 'Noice!'
      });
    });
  }
  useCase.create(tab);
}

addTab('Bouncing', 'ballBounce', BallBounce);
addTab('Time steps', 'timeSteps', TimeSteps);
addTab('Random', 'randomNotes', RandomNotes);
addTab('Chord colours', 'vivierColors', VivierColors);
addTab('Set theory', 'setTheory', SetTheory);
addTab('Midi analysis', 'midiAnalysis', MidiAnalysis);
addTab('Paint!', 'digitizeLines', DigitizeLines);
addTab('Links', 'externalLinks', ExternalLinks);
