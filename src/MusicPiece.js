import UI from './UI';
import { fromVerboseToArray, fromArrayToVerbose, JsonValidationError } from './formats';
import MidiIO from './MidiIO';
import './css/music_piece.css';

const CLASS_MESSAGES_ERROR = 'errorBorder';
const CLASS_BUTTON_DISABLED = 'disabled';

function handlingErrors (messagesDiv, downloadButton, playbackButton, func) {
  return () => {
    try {
      func();
      messagesDiv.withText('');
      messagesDiv.domElement.classList.remove(CLASS_MESSAGES_ERROR);
      downloadButton.domElement.classList.remove(CLASS_BUTTON_DISABLED);
      playbackButton.domElement.classList.remove(CLASS_BUTTON_DISABLED);
    } catch (e) {
      if (e instanceof JsonValidationError) {
        messagesDiv.withText('Nope, that\'s not valid: ' + e.errors[0].instancePath + ' ' + e.errors[0].message);
      } else if (e instanceof SyntaxError) {
        messagesDiv.withText('Ew, that\'s not even JSON: ' + e.message);
      } else {
        messagesDiv.withText('I don\'t even know what happened: ' + e);
      }
      messagesDiv.domElement.classList.add(CLASS_MESSAGES_ERROR);
      downloadButton.domElement.classList.add(CLASS_BUTTON_DISABLED);
      if (!MidiIO.isPlaying()) {
        playbackButton.domElement.classList.add(CLASS_BUTTON_DISABLED);
      }
    }
  };
}

function prettyPrintArray (musicPiece) {
  return '[\n ' + musicPiece.map(part => ' [\n' + part.map(note => '    [' + note.join(', ') + ']').join(',\n') + '\n  ]').join(',') + '\n]';
}

function prettyPrintVerbose (musicPiece) {
  return '{\n  "parts": [' +
    musicPiece.parts.map(part => ('{\n    "notes": [\n' +
        part.notes.map(note => '      { "pitch": ' + note.pitch + ', "startSeconds": ' + note.startSeconds + ', "durationSeconds": ' + note.durationSeconds + '}').join(',\n') + '\n    ]') +
      '\n  }').join(', ') +
    ']\n}';
}

export default function (parent, arrayOnly = false) {
  const textContainer = UI.Div(parent).withClassName('musicPieceInputTextContainer');
  const buttonContainer = UI.Div(parent).withClassName('musicPieceInputButtonContainer');
  const listeners = [];

  let verboseText;
  if (!arrayOnly) {
    verboseText = UI.TextArea(textContainer).withClassName('musicPieceInputVerbose');
  }

  const arrayText = UI.TextArea(textContainer).withClassName('musicPieceInputArray');
  const playback = UI.Div(buttonContainer).withClassName('musicPieceInputPlayback').addClassName('shinyButton');
  const download = UI.Div(buttonContainer).withClassName('musicPieceInputDownload').addClassName('shinyButton')
    .withText('Download');
  const midiTempo = UI.Slider(buttonContainer, 'Export MIDI BPM', 30, 300, 60, 1).addClassName('midiTempoSlider');
  const messages = UI.Div(parent).withClassName('musicPieceInputMessages');

  playback.withText(MidiIO.isPlaying() ? 'Stop' : 'Play');
  MidiIO.onPlay(() => playback.withText('Stop'));
  MidiIO.onStop(() => {
    playback.withText('Play');
    if (messages.domElement.classList.contains(CLASS_MESSAGES_ERROR)) {
      playback.domElement.classList.add(CLASS_BUTTON_DISABLED);
    }
  });

  if (!arrayOnly) {
    verboseText.withEventListener('input', handlingErrors(messages, download, playback, () => {
      const musicPieceArray = fromVerboseToArray(verboseText.value);
      arrayText.value = prettyPrintArray(musicPieceArray);
      listeners.forEach(l => l(arrayText.value));
    }));
  }
  arrayText.withEventListener('input', handlingErrors(messages, download, playback, () => {
    const musicPieceVerbose = fromArrayToVerbose(arrayText.value); // needed at least for validation
    if (!arrayOnly) verboseText.value = prettyPrintVerbose(musicPieceVerbose);
  }));

  playback.withEventListener('click', () => {
    if (MidiIO.isPlaying()) {
      MidiIO.stop();
    } else if (!download.domElement.classList.contains(CLASS_BUTTON_DISABLED)) {
      MidiIO.play(arrayText.value);
    }
  });
  download.withEventListener('click', () => {
    if (!download.domElement.classList.contains(CLASS_BUTTON_DISABLED)) {
      MidiIO.download(arrayText.value, midiTempo.value);
    }
  });

  arrayText.withEventListener('input', () => listeners.forEach(l => l(arrayText.value)));

  return {
    setArray: musicPieceArray => {
      arrayText.value = prettyPrintArray(musicPieceArray);
      if (verboseText) verboseText.value = prettyPrintVerbose(fromArrayToVerbose(musicPieceArray));
    },
    withEventListener: listener => listeners.push(listener),
    getArray: () => arrayText.value
  };
}
