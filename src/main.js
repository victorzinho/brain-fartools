import { digitizeLines } from './digitize';
import { readMidi } from './midi_io';
import { modify } from './randomize_midi';
import { analyze } from './midi_analysis';

const canvas = document.createElement('canvas');
const context = canvas.getContext('2d');

document.getElementById('image').addEventListener('change', event => {
  const files = event.target.files;
  if (FileReader && files && files.length) { // eslint-disable-line no-undef
    const reader = new FileReader(); // eslint-disable-line no-undef
    reader.onload = () => readBytes(reader, files[0].name);
    reader.readAsArrayBuffer(files[0]);
  }
});

function readBytes (fileReader, filename) {
  const array = new Uint8Array(fileReader.result);
  const midi = readMidi(array.buffer);
  analyze(midi, filename);
  // const modified = modify(midi);
  // downloadBlob(modified.toArray(), 'test.mid', 'application/octet-stream');
  // const writer = writeMidi(modified);
}

const downloadURL = (data, fileName) => {
  const a = document.createElement('a');
  a.href = data;
  a.download = fileName;
  document.body.appendChild(a);
  a.style.display = 'none';
  a.click();
  a.remove();
};

function downloadBlob (data, fileName, mimeType) {
  const blob = new Blob([data], { // eslint-disable-line no-undef
    type: mimeType
  });

  const url = window.URL.createObjectURL(blob);
  downloadURL(url, fileName);
  setTimeout(() => window.URL.revokeObjectURL(url), 10000);
}

function showImage (fileReader) {
  const img = document.getElementById('output_image');
  img.onload = () => getImageData(img);
  img.src = fileReader.result;
}

function getImageData (img) {
  canvas.width = img.width;
  canvas.height = img.height;
  context.drawImage(img, 0, 0);
  const frequencies = digitizeLines(context.getImageData(0, 0, canvas.width, canvas.height));
  document.getElementById('output_freqs').innerHTML = '' + frequencies;
}
