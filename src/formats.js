import { Midi } from '@tonejs/midi';
import { ValidateMusicPieceArray, ValidateMusicPieceVerbose } from './ajv_validation';

/**
 * Index for pitch value in a note in array format ([pitch, time, duration]).
 * @type {number}
 */
export const NOTE_ARRAY_PITCH = 0;
/**
 * Index for time value in a note in array format ([pitch, time, duration]).
 * @type {number}
 */
export const NOTE_ARRAY_TIME = 1;
/**
 * Index for duration value in a note in array format ([pitch, time, duration]).
 * @type {number}
 */
export const NOTE_ARRAY_DURATION = 2;

function toObject (musicPiece) {
  return (typeof musicPiece === 'string') ? JSON.parse(musicPiece) : musicPiece;
}

/**
 * Determines whether a music piece is in a valid verbose format or not.
 *
 * @param musicPieceObj The music piece to validate. It's assumed to be at least an object, string parsing is not performed.
 * @returns {boolean} true if it's a valid music piece in verbose format, false otherwise
 */
export function isVerbose (musicPieceObj) {
  return ValidateMusicPieceVerbose(musicPieceObj);
}

/**
 * Determines whether a music piece is in a valid verbose format or not.
 *
 * @param musicPieceObj The music piece to validate. It's assumed to be at least an object, string parsing is not performed.
 * @returns {boolean} true if it's a valid music piece in verbose format, false otherwise
 */
export function isArray (musicPieceObj) {
  return ValidateMusicPieceArray(musicPieceObj);
}

export function fromArrayToVerbose (musicPiece) {
  const musicPieceObj = toObject(musicPiece);
  ValidateMusicPieceArray(musicPieceObj);
  if (ValidateMusicPieceArray.errors) throw new JsonValidationError(ValidateMusicPieceArray.errors);
  return {
    parts: musicPieceObj.map(part => ({
      notes: part.map(note => ({
        pitch: note[NOTE_ARRAY_PITCH],
        startSeconds: note[NOTE_ARRAY_TIME],
        durationSeconds: note[NOTE_ARRAY_DURATION]
      }))
    }))
  };
}

/**
 * Converts a music piece from verbose into array format (see schemas above).
 *
 * @param musicPiece A music piece in verbose format, either as object or string.
 * @returns {[]} The music piece in array format.
 */
export function fromVerboseToArray (musicPiece) {
  const musicPieceObj = toObject(musicPiece);
  ValidateMusicPieceVerbose(musicPieceObj);
  if (ValidateMusicPieceVerbose.errors) throw new JsonValidationError(ValidateMusicPieceVerbose.errors);
  return musicPieceObj.parts.map(part => part.notes.map(note => [note.pitch, note.startSeconds, note.durationSeconds]));
}

/**
 * Converts a music piece from any supported format into a @tonejs Midi object.
 *
 * @param musicPiece A music piece in array or verbose format (see schemas above), either as object or string.
 * @param bpm The bpm to define for the @tonejs object, useful to produce a correct MIDI file that can be imported elsewhere.
 * @returns {Midi} The @tonejs MIDI object.
 */
export function toToneJs (musicPiece, bpm = 100) {
  if (bpm < 30 || bpm > 300) throw new Error('Insane tempo!');

  const musicPieceObj = toObject(musicPiece);
  let musicPieceArray;
  if (isArray(musicPieceObj)) {
    musicPieceArray = musicPieceObj;
  } else if (isVerbose(musicPieceObj)) {
    musicPieceArray = fromVerboseToArray(musicPieceObj);
  } else {
    throw new Error('Unrecognized format: ' + musicPiece);
  }

  const midi = new Midi();
  midi.header.setTempo(bpm);
  for (const part of musicPieceArray) {
    const track = midi.addTrack();
    for (const note of part) {
      track.addNote({
        midi: note[NOTE_ARRAY_PITCH],
        time: note[NOTE_ARRAY_TIME],
        duration: note[NOTE_ARRAY_DURATION]
      });
    }
  }

  return midi;
}

/**
 * Error containing the AJV validation errors on schema validation.
 */
export class JsonValidationError extends Error {
  constructor (errors) {
    super('Error parsing JSON');
    this.errors = errors;
  }
}
