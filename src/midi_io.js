import { Midi } from '@tonejs/midi';

export function readMidi (arrayBuffer) {
  return new Midi(arrayBuffer);
}

export function writeMidi (midi) {
  throw new Error('Not implemented');
}
