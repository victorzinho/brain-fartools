import assert from 'assert';
import fs from 'fs';
import { describe, it } from 'mocha';
import { PNG } from 'pngjs';
import { digitizeLines } from '../src/digitize';

const RESOURCES_LINE = 'test/resources/line_lowres.png';

describe('digitize', function () {
  it('digitizes single line', function () {
    // GIVEN
    const png = PNG.sync.read(fs.readFileSync(RESOURCES_LINE));
    const min = 20;
    const max = 100;

    // WHEN
    const frequencies = digitizeLines(png, min, max);

    // THEN
    assert.strictEqual(frequencies.length, png.width);
    assert.strictEqual(frequencies.filter(isNaN).length, 19);
    assert.strictEqual(frequencies.filter(x => (x < min || x > max)).length, 0);
  });
});
