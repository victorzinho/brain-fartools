import Ajv from 'ajv';
import AjvStandalone from 'ajv/dist/standalone';

// to generate standalone code for validation; useful to avoid the whole Ajv dependency at runtime and reduce bundle size;
// output in ajv_validation.js

const ajv = new Ajv({ code: { source: true, esm: true } });
ajv.addSchema({
  $id: '/MusicNote',
  type: 'object',
  required: ['pitch', 'startSeconds', 'durationSeconds'],
  additionalProperties: false,
  properties: {
    pitch: { type: 'integer', minimum: 1, maximum: 127 },
    startSeconds: { type: 'number', minimum: 0 },
    durationSeconds: { type: 'number', minimum: 0 }
  }
});
ajv.addSchema({
  $id: '/MusicPart',
  type: 'object',
  required: ['notes'],
  additionalProperties: false,
  properties: {
    notes: { type: 'array', items: { $ref: '/MusicNote' } }
  }
});
const SCHEMA_MUSIC_PIECE_VERBOSE = {
  $id: '/MusicPieceVerbose',
  type: 'object',
  required: ['parts'],
  additionalProperties: false,
  properties: {
    parts: { type: 'array', items: { $ref: '/MusicPart' } }
  }
};

const SCHEMA_MUSIC_PIECE_ARRAY = {
  $id: '/MusicPieceArray',
  type: 'array', // array of parts
  items: {
    type: 'array', // array of notes
    items: {
      type: 'array', // array of [pitch, start, duration]
      items: [
        { type: 'integer', minimum: 1, maximum: 127 },
        { type: 'number', minimum: 0 },
        { type: 'number', minimum: 0 }
      ],
      minItems: 3,
      maxItems: 3
    }
  }
};
ajv.addSchema(SCHEMA_MUSIC_PIECE_VERBOSE);
ajv.addSchema(SCHEMA_MUSIC_PIECE_ARRAY);
// For ESM, the export name needs to be a valid export name, it can not be `export const #/definitions/Foo = ...;` so we
// need to provide a mapping between a valid name and the $id field. Below will generate
// `export const Foo = ...;export const Bar = ...;`
// This mapping would not have been needed if the `$ids` was just `Bar` and `Foo` instead of `#/definitions/Foo`
// and `#/definitions/Bar` respectfully
console.log(AjvStandalone(ajv, {
  ValidateMusicPieceArray: '/MusicPieceArray',
  ValidateMusicPieceVerbose: '/MusicPieceVerbose'
}));
