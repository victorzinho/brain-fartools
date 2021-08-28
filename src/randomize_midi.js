// this one doesn't work properly but gives psychedelic results ¯\_(ツ)_/¯

function randomizeEvent (event) {
  const random = Math.round(200 * (Math.random() - 0.5));
  event.ticks += random;
}

export function modify (midi, modifyFunction = randomizeEvent) {
  midi.tracks.forEach(track => track.notes.forEach(modifyFunction));
  return midi;
}
