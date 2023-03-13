import { Midi } from '@tonejs/midi';
import { instrument } from 'soundfont-player';
import MidiPlayer from 'midi-player-js';
import downloadBlob from './download';
import { toToneJs } from './formats';

const AudioContext = window.AudioContext || window.webkitAudioContext || false;
const INSTRUMENT = 'marimba';
let midiPlayer;
let soundfontPlayer;
let playing = false;

/**
 * Reads a MIDI from an ArrayBuffer into a @tonejs Midi object.
 *
 * @param arrayBuffer the MIDI bytes.
 * @returns {Midi} The MIDI object.
 */
function read (arrayBuffer) {
  return new Midi(arrayBuffer);
}

/**
 * Triggers the donwload of the MIDI piece.
 *
 * @param musicPiece Can be any of the formats supported by format.js
 * @throws Error if the music piece has not any recognized format.
 */
function download (musicPiece, bpm) {
  const toneJsMidi = toToneJs(musicPiece, bpm);
  downloadBlob(toneJsMidi.toArray(), 'yeah.mid', 'application/octet-stream');
}

/**
 * Initializes the MIDI player.
 *
 * @returns {Promise<Player>}
 */
function initPlayer () {
  if (midiPlayer) return Promise.resolve(midiPlayer);

  midiPlayer = new MidiPlayer.Player();
  const audioContext = new AudioContext();
  return instrument(audioContext, INSTRUMENT).then(function (p) {
    soundfontPlayer = p;
    midiPlayer.on('midiEvent', event => {
      if (event.name === 'Note on' && event.velocity > 0) {
        soundfontPlayer.play(event.noteName, audioContext.currentTime, { gain: event.velocity / 100 });
      }
    });
    midiPlayer.on('endOfFile', stop);
    return midiPlayer;
  });
}

const stopListeners = [];
const playListeners = [];

/**
 * Starts the playback. Does nothing if already playing.
 *
 * @param musicPiece Can be any of the formats supported by format.js
 * @throws Error if the music piece has not any recognized format.
 */
function play (musicPiece) {
  const midi = toToneJs(musicPiece);
  const base64 = btoa(String.fromCharCode(...new Uint8Array(midi.toArray())));
  if (playing) {
    console.log('Hold your horses, mate!');
    return;
  }

  initPlayer().then(player => {
    playing = true;
    player.loadDataUri('data:audio/midi;base64,' + base64);
    player.play();
    playListeners.forEach(listener => listener());
  });
}

/**
 * Stops the playback. Does nothing if not playing.
 */
function stop () {
  midiPlayer.stop();
  soundfontPlayer.stop();
  playing = false;
  stopListeners.forEach(listener => listener());
}

const onPlay = listener => playListeners.push(listener);
const onStop = listener => stopListeners.push(listener);
const isPlaying = () => playing;

export default { play, stop, read, download, onStop, onPlay, isPlaying };
