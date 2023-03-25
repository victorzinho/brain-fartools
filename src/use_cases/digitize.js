import UI from '../UI';
import './digitize.css';
import MusicPiece from '../MusicPiece';
import { NOTE_ARRAY_DURATION } from '../formats';

const INFO = {
  title: 'Drawing lines in the sand',
  html: `
<p>Turn your "paintings" into music! This one is about getting some MIDI or frequencies by drawing a single horizontal
line and choosing the pitch range and duration. Is it useful? Probably not, but doesn't it make it more fun?</p>

<p>It just traverses the image from left to right, trying to find the first non-white pixel from the bottom.
That means you can still draw a knob, but the music is going to be disappointing.</p>
`
};

const NUM_DECIMALS = 2;

/**
 * Detects a line in an image and converts it into an array of numbers representing it.
 *
 * The numbers represent the pixel index in a column, scaled exponentially to a given range.
 *
 * Each element of the array corresponds to a pixel column in the image, from left to right (or NaN if there's no line).
 *
 * There are several simplifications that make it work only with a single, relatively thin, line
 * on an image with a white background:
 * - The line is detected by obtaining a grayscale value for the given pixel and compare against the threshold.
 * - If the grayscale value is below (darker) than the threshold, the pixel is detected as part of the line.
 * - If multiple dark pixels are detected together in a pixel column (a thick line), the middle one is used.
 * - Multiple lines are currently ignored; only the first dark pixels (starting from the bottom of the image) will
 * be taken into account.
 *
 * @param imageData An ImageData containing the image with the line to detect.
 * @param min The value representing the bottom of the image, in hertz.
 * @param max The value representing the top of the image, in hertz.
 * @returns {[]} An array of numbers, exponentially scaled between min and max to represent the line in the image.
 */
export function digitizeLines (imageData, min = 25, max = 4500) {
  const roundFactor = Math.pow(10, NUM_DECIMALS);
  // we want the y axis to represent an exponential scale between min and max
  // min ^ (1 + imageHeight * x) = max, where x is the exponential constant
  const exponentialStep = Math.log(max) / (imageData.height * Math.log(min)) - 1 / imageData.height;
  const frequencies = [];
  for (let x = 0; x < imageData.width; x++) {
    let minFreqForColumn;
    let maxFreqForColumn;
    for (let y = imageData.height - 1; y >= 0; y--) {
      const dataIndex = 4 * (y * imageData.width + x);
      const colorAmount = (imageData.data[dataIndex] + imageData.data[dataIndex + 1] + imageData.data[dataIndex + 2]) / 3;
      if (colorAmount > 0) {
        maxFreqForColumn = maxFreqForColumn || y;
        minFreqForColumn = y;
      } else if (maxFreqForColumn && minFreqForColumn) {
        // already detected a single line, assume there's only one line on the image
        // and skip the rest of the pixels above; so nice when you do your own requirements :)
        break;
      }
    }

    let freq = NaN;
    if (maxFreqForColumn && minFreqForColumn) {
      const freqColumn = imageData.height - (maxFreqForColumn + minFreqForColumn) / 2.0;
      freq = Math.pow(min, 1 + freqColumn * exponentialStep);
      freq = Math.round((freq + Number.EPSILON) * roundFactor) / roundFactor;
    }
    if (freq) frequencies.push(freq);
  }

  return frequencies;
}

class LineCanvas {
  constructor (parent) {
    this.position = {};
    this.listeners = [];

    this.parent = parent;
    this.canvas = UI.Canvas(parent).withClassName('digitizeCanvas');
    this.context = this.canvas.domElement.getContext('2d', { willReadFrequently: true });

    window.addEventListener('resize', e => this.resize(e));
    parent.withEventListener('mousemove', e => this.draw(e));
    parent.withEventListener('touchmove', e => this.draw(e));
    window.addEventListener('mouseup', e => this.endEvent(e));
    window.addEventListener('touchend', e => this.endEvent(e));
  }

  endEvent (event) {
    // event.preventDefault();
    this.reset();
    this.listeners.forEach(l => l());
  }

  onFinishLine (listener) {
    this.listeners.push(listener);
  }

  reset () {
    this.position = {};
  }

  resize () {
    this.canvas.domElement.width = this.parent.domElement.offsetWidth;
    this.canvas.domElement.height = this.parent.domElement.offsetHeight;
  }

  setPosition (e) {
    const rect = this.canvas.domElement.getBoundingClientRect();
    this.position.x = (e.clientX || e.targetTouches[0].clientX) - rect.left - window.scrollX;
    this.position.y = (e.clientY || e.targetTouches[0].clientY) - rect.top - window.scrollY;
  }

  draw (e) {
    if (e.touches) {
      e.preventDefault();
    } else if (e.buttons !== 1) {
      return;
    }

    if (!this.canvas.domElement.width || !this.canvas.domElement.height) this.resize();
    this.context.beginPath();
    this.context.lineWidth = 3;
    this.context.lineCap = 'round';
    this.context.strokeStyle = '#F00';

    if (this.position.x && this.position.y) {
      this.context.moveTo(this.position.x, this.position.y);
      this.setPosition(e);
      this.context.lineTo(this.position.x, this.position.y);
      this.context.stroke(); // draw it!
    } else {
      this.context.clearRect(0, 0, this.canvas.domElement.width, this.canvas.domElement.height);
      this.setPosition(e);
    }
  }

  getImageData () {
    if (!this.canvas.domElement.width || !this.canvas.domElement.height) return null;
    return this.context.getImageData(0, 0, this.canvas.domElement.width, this.canvas.domElement.height);
  }
}

function round (time) {
  return Math.round(time * 100.0) / 100.0;
}

function updateMusicPiece (musicPiece, freqs, totalTime) {
  let time = 0.0;
  let lastNote = null;
  const notes = [];
  const duration = totalTime / freqs.length;
  for (const hz of freqs) {
    const currentNote = toMidiNote(hz);
    if (currentNote !== lastNote) {
      notes.push([currentNote, round(time), round(duration)]);
    } else {
      const lastNote = notes[notes.length - 1];
      lastNote[NOTE_ARRAY_DURATION] = round(lastNote[NOTE_ARRAY_DURATION] + duration);
    }
    time += duration;
    lastNote = currentNote;
  }
  musicPiece.setArray([notes]);
}

function create (parent) {
  const container = UI.Div(parent).withClassName('digitizeContainer');

  const paramsContainer = UI.Div(container).withClassName('digitizeParamsContainer');
  const pitchRangeInput = UI.Slider(paramsContainer, 'Pitch range', 30, 4500, [100, 1000], 1);
  const timeInput = UI.Slider(paramsContainer, 'Time (s)', 1, 60, [11], 1);

  const canvasContainer = UI.Div(container).withClassName('digitizeCanvasContainer');
  UI.Label(canvasContainer).withClassName('digitizeCanvasLabel').withText('Draw a line on me!');
  const canvas = new LineCanvas(canvasContainer);

  const outputContainer = UI.Div(container).withClassName('digitizeOutputContainer');
  const musicPieceContainer = UI.Div(outputContainer).withClassName('digitizeMusicPieceContainer');
  const musicPiece = MusicPiece(musicPieceContainer, true);

  const frequenciesContainer = UI.Div(outputContainer).withClassName('digitizeFrequenciesContainer');
  const frequenciesArea = UI.TextArea(frequenciesContainer).withClassName('musicPieceInputArray');
  UI.Div(frequenciesContainer).withText('Shiny hertz!').withClassName('digitizeFrequenciesText');

  function update () {
    const imageData = canvas.getImageData();
    if (!imageData) return;

    const freqs = digitizeLines(imageData, pitchRangeInput.value[0], pitchRangeInput.value[1]);
    updateMusicPiece(musicPiece, freqs, timeInput.value);
    frequenciesArea.value = JSON.stringify([freqs], null, 2);
  }

  canvas.onFinishLine(update);
  pitchRangeInput.withEventListener('update', update);
  timeInput.withEventListener('update', update);
  canvas.resize();
}

function toMidiNote (hz) {
  return Math.round(12 * (Math.log(hz / 440.0) / Math.log(2.0)) + 69);
}

export default { create, INFO };
