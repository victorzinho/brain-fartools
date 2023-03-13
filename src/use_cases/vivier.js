import UI from '../UI';
import MusicPiece from '../MusicPiece';
import './vivier.css';
import { isArray, NOTE_ARRAY_DURATION, NOTE_ARRAY_PITCH, NOTE_ARRAY_TIME } from '../formats';
import { HAENDEL_VOLUNTARY_III } from './music_samples';

const INFO = {
  title: 'Any colour you like',
  html: `
<p>This tool is some humble attempt at Claude Vivier's point of view for harmony and colors, in a way related
to spectralism and electronic procedures.</p>
<p>It allows you to automatically build chords that share the same "color" from:</p>
<ul>
<li>A two part music piece (they have to match in the number of notes and their attack time and duration;
you can think of it as parallel bass/melody lines).</li>
<li>A set of "colors". Each "color" is defined as a pair of numbers: factors for both (bass/melody) lines.</li>
</ul>
By using that, the tool generates a new note <code>fn = a * fm + b * fb</code> where <code>a/b</code>
are the part factors and <code>fm/fb</code> are the part frequencies in hertz. The tool rounds the resulting
frequencies to the nearest semitone in order to be able to export to MIDI, but it is also possible to export the 
specific frequencies (as a JSON array, sorry!). 
`
};

// We convert from midi to hz to do calculations and back to midi;
// the minor differences in tuning shouldn't impact the calculations and eventual discretization to midi;
// also, please think about singers :(
const A_HZ = 440.0;
const A_MIDI = 69;

function roundHz (hz) {
  return Math.round(hz * 100.0) / 100.0;
}

/**
 * https://www.music.mcgill.ca/~gary/307/week1/node28.html
 */
function toHz (midiNote) {
  return A_HZ * Math.pow(2, (midiNote - A_MIDI) / 12);
}

function toMidiNote (hz) {
  return Math.round(12 * (Math.log(hz / A_HZ) / Math.log(2.0)) + A_MIDI);
}

function setError (errorsDomElement, text) {
  errorsDomElement.classList.add('errorBorder');
  errorsDomElement.innerText = text;
  return null;
}

function getValidMusicPiece (musicPieceArrayString, errorsContainer) {
  let musicPiece;
  try {
    musicPiece = JSON.parse(musicPieceArrayString);
  } catch (e) {
    // ignore, error message already there from MusicPiece
    return null;
  }

  if (!isArray(musicPiece)) return false; // error message is already there

  if (musicPiece.length !== 2) return setError('Exactly two parts required');

  const part1 = musicPiece[0];
  const part2 = musicPiece[1];
  if (part1.length !== part2.length) return setError('The two parts must have the same number of notes');

  const anyNoteDiffersTime = part1.some((note, i) => note[NOTE_ARRAY_TIME] !== part2[i][NOTE_ARRAY_TIME]);
  const anyNoteDiffersDuration = part1.some((note, i) => note[NOTE_ARRAY_DURATION] !== part2[i][NOTE_ARRAY_DURATION]);
  if (anyNoteDiffersTime || anyNoteDiffersDuration) {
    return setError('The notes in the two parts must have the same attack time and duration');
  }

  return musicPiece;
}

/**
 * Obtain the frequencies in a format similar to the music piece array.
 *
 * @param musicPieceArray The original music piece containing the two parts in array format.
 * @param vivierColors Array of VivierColors
 * @returns {*[]} An array of music parts; each part is an array of frequencies (number).
 * It will have num colors +2 parts (including the provided input parts as well as the first two parts).
 */
function modulate (musicPieceArray, vivierColors) {
  const musicPart1 = musicPieceArray[0];
  const musicPart2 = musicPieceArray[1];

  const output = [];
  output.push([]); // music part 1
  output.push([]); // music part 2
  vivierColors.forEach(() => output.push([]));

  for (let i = 0; i < musicPart2.length; i++) {
    const part1hz = toHz(musicPart1[i][NOTE_ARRAY_PITCH]);
    const part2hz = toHz(musicPart2[i][NOTE_ARRAY_PITCH]);
    output[0].push(roundHz(part1hz));
    output[1].push(roundHz(part2hz));
    for (let color = 0; color < vivierColors.length; color++) {
      const hz = roundHz(vivierColors[color].modulatePair(part1hz, part2hz));
      output[color + 2].push(hz);
    }
  }

  return output;
}

function toMidiPiece (musicPieceArray, frequencies) {
  const anyPart = musicPieceArray[0];
  return frequencies.map(part => part
    .map((hz, i) => [toMidiNote(hz), anyPart[i][NOTE_ARRAY_TIME], anyPart[i][NOTE_ARRAY_DURATION]]));
}

class VivierColor {
  constructor (parent, initialPart1Factor, initialPart2Factor) {
    this.changeListeners = [];
    this.deleteListeners = [];
    const onChange = () => this.changeListeners.forEach(l => l(this.part1Slider.value, this.part2Slider.value));

    this.container = UI.Div(parent).withClassName('vivierColorInputContainer');
    this.part1Slider = UI.Slider(this.container, 'Line 1', 0.01, 4.0, [initialPart1Factor], 0.01)
      .withEventListener('update', onChange);
    this.part2Slider = UI.Slider(this.container, 'Line 2', 0.01, 4.0, [initialPart2Factor], 0.01)
      .withEventListener('update', onChange);
    // delete me
    UI.Div(this.container).withClassName('shinyButton vivierColorButton').withText('-').withEventListener('click', () => {
      this.deleteListeners.forEach(l => l());
      this.container.domElement.remove();
    });
    this.container.domElement.scrollIntoView();
  }

  modulatePair (part1hz, part2hz) {
    return this.part1Slider.value * part1hz + this.part2Slider.value * part2hz;
  }

  withChangeListener (listener) {
    this.changeListeners.push(listener);
    return this;
  }

  withDeleteListener (listener) {
    this.deleteListeners.push(listener);
    return this;
  }
}

/**
 * Full-blown component to play around with Vivier colors (a * hz(part1) + b * hz(part2)).
 *
 * @param parent The DOM element to put all the controls (pitch, time sliders, etc.).
 */
function create (parent) {
  const vivierContainer = UI.Div(parent).withClassName('vivierContainer');
  const label = UI.Label(vivierContainer).withClassName('vivierColorsLabel').withText('Colors');
  const vivierColorsContainer = UI.Div(vivierContainer).withClassName('vivierColorsContainer');
  const addButton = UI.Div(parent).withClassName('shinyButton vivierColorButton').withText('+');

  const musicPieceContainer = UI.Div(parent).withClassName('vivierMusicPieceContainer');
  const inputContainer = UI.Div(musicPieceContainer).withClassName('vivierInputContainer');
  const inputPiece = MusicPiece(inputContainer, true);
  const outputContainer = UI.Div(musicPieceContainer).withClassName('vivierOutputContainer');
  const outputPiece = MusicPiece(outputContainer, true);
  const frequenciesContainer = UI.Div(musicPieceContainer).withClassName('vivierFrequenciesContainer');
  const frequenciesArea = UI.TextArea(frequenciesContainer).withClassName('musicPieceInputArray');
  UI.Div(frequenciesContainer).withText('Shiny hertz!').withClassName('vivierFrequenciesText');

  const vivierColors = [];

  function update () {
    // TODO reuse errors from input music piece; ugly but don't want to build some generic error thingy
    const errorMessages = inputContainer.domElement.querySelector('div.musicPieceInputMessages');
    const musicPiece = getValidMusicPiece(inputPiece.getArray(), errorMessages);
    if (!musicPiece) return;

    const frequencies = modulate(musicPiece, vivierColors);
    outputPiece.setArray(toMidiPiece(musicPiece, frequencies));
    frequenciesArea.value = JSON.stringify(frequencies, null, 2);
  }

  function addColorInput (part1Factor, part2Factor) {
    const colorInput = new VivierColor(vivierColorsContainer, part1Factor, part2Factor);
    vivierColors.push(colorInput);
    colorInput
      .withChangeListener(update)
      .withDeleteListener(() => {
        vivierColors.splice(vivierColors.indexOf(colorInput), 1);
        update();
      });
    label.domElement.scrollIntoView();
    update();
  }

  addButton.withEventListener('click', () => addColorInput(0.8, 0.4));
  inputPiece.withEventListener(update);
  inputPiece.setArray(HAENDEL_VOLUNTARY_III);

  addColorInput(0.85, 0.4);
  addColorInput(0.4, 0.70);
}

export default { create, INFO };
