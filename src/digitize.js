export const BLACK_PIXEL_THRESHOLD = 230;

/**
 * Detects a line in an image and converts it into an array of numbers representing it.
 *
 * The numbers represent the pixel index in a column, scaled exponentially to a given range.
 *
 * Each element of the array corresponds to a pixel column in the image, from left to right (or NaN if there's no line).
 *
 * There are several simplifications that make it work only with a single, relatively thin, line
 * on an image with a white background:
 * - The line is detected by obtaining a grayscale value for the given pixel and compare against the threshold.
 * - If the grayscale value is below (darker) than the threshold, the pixel is detected as part of the line.
 * - If multiple dark pixels are detected together in a pixel column (a thick line), the middle one is used.
 * - Multiple lines are currently ignored; only the first dark pixels (starting from the bottom of the image) will
 * be taken into account.
 *
 * @param imageData An ImageData containing the image with the line to detect.
 * @param min The value representing the bottom of the image.
 * @param max The value representing the top of the image.
 * @param threshold The threshold for which to consider a pixel dark enough (between 0 and 255).
 * @returns {[]} An array of numbers, exponentially scaled between min and max to represent the line in the image.
 */
export function digitizeLines (
  imageData,
  min = 25,
  max = 4500,
  threshold = BLACK_PIXEL_THRESHOLD,
  decimals = 2
) {
  const roundFactor = Math.pow(10, decimals);
  // we want the y axis to represent an exponential scale between min and max
  // min ^ (1 + imageHeight * x) = max, where x is the exponential constant
  const exponentialStep = Math.log(max) / (imageData.height * Math.log(min)) - 1 / imageData.height;
  const frequencies = [];
  for (let x = 0; x < imageData.width; x++) {
    let minFreqForColumn;
    let maxFreqForColumn;
    for (let y = imageData.height - 1; y >= 0; y--) {
      const dataIndex = 4 * (y * imageData.width + x);
      const gray = (imageData.data[dataIndex] + imageData.data[dataIndex + 1] + imageData.data[dataIndex + 2]) / 3;
      if (gray < threshold) {
        maxFreqForColumn = maxFreqForColumn || y;
        minFreqForColumn = y;
      } else if (maxFreqForColumn && minFreqForColumn) {
        // already detected a single line, assume there's only one line on the image
        // and skip the rest of the pixels above; so nice when you do your own requirements :)
        break;
      }
    }

    let freq = NaN;
    if (maxFreqForColumn && minFreqForColumn) {
      const freqColumn = imageData.height - (maxFreqForColumn + minFreqForColumn) / 2.0;
      freq = Math.pow(min, 1 + freqColumn * exponentialStep);
      freq = Math.round((freq + Number.EPSILON) * roundFactor) / roundFactor;
    }
    frequencies.push(freq);
  }

  return frequencies;
}
