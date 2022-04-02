import { Midi } from '@tonejs/midi';

function nextExponential (lambda) {
  return -Math.log(Math.random()) / lambda;
}

const NOTE_DURATION_4TH = 1100.0 / 2;
const NOTE_DURATION_8TH = 700.0 / 2;

export function sectionA1FirstChord () {
  const midi = new Midi();
  // implement :)
  return midi;
}

export function weird8thNotes2 (notes, durations) {
  const midi = new Midi();
  const track = midi.addTrack();

  let currentTime = 0;
  let noteIndex = 0;
  let durationIndex = 0;
  for (let i = 0; i < 500; i++) {
    const shouldPlay = Math.random();
    if (shouldPlay < 0.0) continue;

    if (noteIndex >= notes.length) noteIndex = 0;
    if (durationIndex >= durations.length) durationIndex = 0;
    track.addNote({
      midi: notes[noteIndex],
      time: currentTime,
      duration: durations[durationIndex] / 30.0,
      velocity: 1
    });

    currentTime += durations[durationIndex] / 30.0;
    noteIndex++;
    durationIndex++;
  }

  return midi;
}

export function weird8thNotes (notes) {
  const midi = new Midi();
  const track = midi.addTrack();

  let allValidTimestamps = [];
  let t = 0;
  while (t < 50000) {
    allValidTimestamps.push(t);
    t += NOTE_DURATION_4TH;
  }

  t = 0;
  while (t < 50000) {
    // allValidTimestamps.push(t);
    t += NOTE_DURATION_8TH;
  }

  allValidTimestamps = [...new Set(allValidTimestamps)];
  for (let i = 0; i < allValidTimestamps.length; i++) {
    const shouldPlay = Math.random();
    if (shouldPlay < 0.0) continue;

    const velocity = Math.random();
    const noteRnd = Math.random();
    const duration = 0.25;
    let note;
    if (noteRnd < 0.3) {
      note = notes[0];
    } else if (noteRnd < 0.6) {
      note = notes[1];
    } else {
      note = notes[2];
    }

    track.addNote({
      midi: 60 + note,
      time: allValidTimestamps[i] / 2000.0,
      duration,
      velocity
    });
  }

  return midi;
}

export function randomMidi (numNotes = 500) {
  const midi = new Midi();
  const track = midi.addTrack();

  let currentTime = 0;
  const noteOffTimeByNote = [];
  for (let i = 0; i < numNotes; i++) {
    const numChordNotesRnd = Math.random();
    let numChordNotes;
    if (numChordNotesRnd < 0.75) {
      numChordNotes = 1;
    } else if (numChordNotesRnd < 0.87) {
      numChordNotes = 2;
    } else {
      numChordNotes = 3;
    }

    let numAddedNotes = 0;
    const velocity = Math.random();
    const duration = 0.25;

    while (numAddedNotes < numChordNotes) {
      let note = Math.round(nextExponential(0.1)) % 50;
      // still playing
      while (noteOffTimeByNote[note] && noteOffTimeByNote[note] > currentTime) {
        note = Math.round(nextExponential(0.1)) % 50;
      }

      noteOffTimeByNote[note] = currentTime + duration;
      track.addNote({
        midi: 60 + note,
        time: currentTime,
        duration,
        velocity
      });
      numAddedNotes++;
    }

    const time = nextExponential(8);
    currentTime += time;
  }

  return midi;
}
