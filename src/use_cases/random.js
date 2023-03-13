import UI from '../UI';
import './random.css';
import MusicPiece from '../MusicPiece';

const INFO = {
  title: 'Random, the new fugue',
  html: `
<p>They say whenever a composer doesn't know what to write, they start a fugue. If you're too bored with fugues but
don't want to wait until AI is able to produce some garbage that saves you some time, you can just try random stuff
and export it to MIDI.</p>
<p>With this tool you can choose the amount of notes, the pitch range, and the randomness for both pitch and "time"
(inter-onset interval). Both random distributions are meant to be exponential, so the "randomness" is λ for
<code>log(random()) / λ</code>.</p>
`
};

const DURATION = 0.25;

function nextExponential (lambda) {
  return -Math.log(Math.random()) / lambda;
}

function updateMusicPiece (musicPiece, minPitch, maxPitch, numNotes, durationLambda = 4, pitchLambda = 0.1) {
  let currentTime = 0;
  const musicPart = [];
  for (let i = 0; i < numNotes; i++) {
    const time = nextExponential(durationLambda);
    const note = minPitch + Math.round(nextExponential(pitchLambda)) % (maxPitch - minPitch);
    currentTime += time;
    musicPart.push([note, parseFloat(currentTime.toFixed(3)), DURATION]);
  }

  return musicPiece.setArray([musicPart]);
}

/**
 * Generates random notes (in both pitch and time) with an exponential distribution.
 *
 * @param parent The DOM element to put all the controls (pitch, time sliders, etc.).
 */
function create (parent) {
  const randomParamsContainer = UI.Div(parent).withClassName('randomParamsContainer');
  const row1 = UI.Div(randomParamsContainer).withClassName('randomParamsRow');
  const pitchRangeInput = UI.Slider(row1, 'Pitch range', 0, 127, [30, 60], 1);
  const numNotesInput = UI.Slider(row1, 'Num. notes', 10, 500, [100], 1);
  const row2 = UI.Div(randomParamsContainer).withClassName('randomParamsRow');
  const durationLambdaInput = UI.Slider(row2, 'Time λ', 0.1, 10, [4], 0.1);
  const pitchLambdaInput = UI.Slider(row2, 'Pitch λ', 0.01, 1, [0.1], 0.01);
  const again = UI.Div(randomParamsContainer).withClassName('randomAgainButton').addClassName('shinyButton').withText('Again!');

  const musicPieceContainer = UI.Div(parent).withClassName('randomMusicPieceContainer');
  const musicPiece = MusicPiece(musicPieceContainer);

  function onChange () {
    updateMusicPiece(musicPiece, pitchRangeInput.value[0], pitchRangeInput.value[1], numNotesInput.value, durationLambdaInput.value, pitchLambdaInput.value);
  }

  pitchRangeInput.withEventListener('update', onChange);
  numNotesInput.withEventListener('update', onChange);
  durationLambdaInput.withEventListener('update', onChange);
  pitchLambdaInput.withEventListener('update', onChange);
  again.withEventListener('click', onChange);
}

export default { create, INFO };
