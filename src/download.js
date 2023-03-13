import { saveAs } from 'file-saver';

export default function (data, fileName, mimeType) {
  saveAs(new Blob([data], { type: mimeType }), fileName); // eslint-disable-line no-undef
}
