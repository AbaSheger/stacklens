/**
 * Converts docs/demo.webm → docs/demo.gif using the bundled ffmpeg binary.
 * Run: node convert-gif.mjs
 */
import ffmpegInstaller from '@ffmpeg-installer/ffmpeg';
import ffmpeg from 'fluent-ffmpeg';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DOCS      = path.resolve(__dirname, '..', 'docs');
const INPUT     = path.join(DOCS, 'demo.webm');
const OUTPUT    = path.join(DOCS, 'demo.gif');

ffmpeg.setFfmpegPath(ffmpegInstaller.path);

console.log('Converting demo.webm → demo.gif ...');

ffmpeg(INPUT)
  .outputOptions([
    '-vf', 'fps=12,scale=900:-1:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=128[p];[s1][p]paletteuse=dither=bayer',
  ])
  .output(OUTPUT)
  .on('progress', p => {
    if (p.percent) process.stdout.write(`\r  ${Math.round(p.percent)}%   `);
  })
  .on('end', () => {
    console.log('\nSaved to', OUTPUT);
  })
  .on('error', err => {
    console.error('Error:', err.message);
    process.exit(1);
  })
  .run();
