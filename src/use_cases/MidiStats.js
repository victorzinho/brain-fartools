// hello! I'm a Java developer playing with js :)
class MidiStat {
  constructor (deltaRelations, min, max) {
    this.deltaRelations = deltaRelations;
    this.min = min;
    this.max = max;

    this.values = [];
    this.delta = [];
    this.deltaNormalized = [];
  }

  value (value) {
    if ((this.min && value < this.min) || (this.max && value > this.max)) return;

    this.values.push(value);

    if (this.values.length === 1) return;

    const previousValue = this.values[this.values.length - 2];
    if (!this.deltaRelations) {
      this.delta.push(value - previousValue);
      this.deltaNormalized.push((value - previousValue) / Math.max(value, previousValue));
      return;
    }

    const minDelta = this.deltaRelations.map(deltaRelation => value * deltaRelation - previousValue)
      .reduce((previous, current) => Math.abs(previous) < Math.abs(current) ? previous : current);
    this.delta.push(minDelta);
    this.deltaNormalized.push(minDelta / Math.max(value, previousValue));
  }
}

export class MidiStats {
  constructor (aggregated = true, minAttack, maxAttack, minDuration, maxDuration, deltaRelations) {
    this.aggregated = aggregated;
    this.minAttack = minAttack;
    this.maxAttack = maxAttack;
    this.minDuration = minDuration;
    this.maxDuration = maxDuration;
    this.deltaRelations = deltaRelations;

    this.aggregatedAttack = [];
    this.aggregatedVelocity = [];
    this.aggregatedDuration = [];
  }

  newMidi (midi) {
    // no, I'm not searching for 'js multithreading' on google
    this.currentMidi = midi;
    this.attack = new MidiStat(this.deltaRelations, this.minAttack, this.maxAttack);
    this.velocity = new MidiStat();
    this.duration = new MidiStat(this.deltaRelations, this.minDuration, this.maxDuration);

    if (!this.aggregated) {
      this.aggregatedAttack = [];
      this.aggregatedVelocity = [];
      this.aggregatedDuration = [];
    }

    this.aggregatedAttack.push(this.attack);
    this.aggregatedVelocity.push(this.velocity);
    this.aggregatedDuration.push(this.duration);
    this.previousNote = null;
  }

  note (note) {
    this.duration.value(this.toMillis(note.durationTicks));
    this.velocity.value(note.velocity);
    if (this.previousNote) {
      this.attack.value(this.toMillis(note.ticks) - this.toMillis(this.previousNote.ticks));
    }

    this.previousNote = note;
  }

  toMillis (durationTicks) {
    const bpm = this.currentMidi.header.tempos.length > 0 ? this.currentMidi.header.tempos[0].bpm : 60;
    return Math.round(60000 / (bpm * this.currentMidi.header.ppq) * durationTicks);
  }

  getAttackValues (filterFunc = () => true) {
    return this.get(this.aggregatedAttack, this.attack, filterFunc, series => series.values);
  }

  getAttackDelta (filterFunc = () => true) {
    return this.get(this.aggregatedAttack, this.attack, filterFunc, series => series.delta);
  }

  getAttackDeltaNormalized (filterFunc = () => true) {
    return this.get(this.aggregatedAttack, this.attack, filterFunc, series => series.deltaNormalized);
  }

  getVelocityValues (filterFunc = () => true) {
    return this.get(this.aggregatedVelocity, this.velocity, filterFunc, series => series.values);
  }

  getVelocityDelta (filterFunc = () => true) {
    return this.get(this.aggregatedVelocity, this.velocity, filterFunc, series => series.delta);
  }

  getVelocityDeltaNormalized (filterFunc = () => true) {
    return this.get(this.aggregatedVelocity, this.velocity, filterFunc, series => series.deltaNormalized);
  }

  getDurationValues (filterFunc = () => true) {
    return this.get(this.aggregatedDuration, this.duration, filterFunc, series => series.values);
  }

  getDurationDelta (filterFunc = () => true) {
    return this.get(this.aggregatedDuration, this.duration, filterFunc, series => series.delta);
  }

  getDurationDeltaNormalized (filterFunc = () => true) {
    return this.get(this.aggregatedDuration, this.duration, filterFunc, series => series.deltaNormalized);
  }

  get (aggregated, nonAggregated, filterFunc = () => true, mapFunc) {
    return this.aggregated
      ? aggregated.map(series => mapFunc(series).filter(filterFunc))
      : mapFunc(nonAggregated).filter(filterFunc);
  }
}
