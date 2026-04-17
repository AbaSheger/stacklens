/**
 * Generates demo assets for StackLens:
 *   docs/demo.png   — static screenshot for GitHub README
 *   docs/demo.webm  — recorded video (convert to GIF: ffmpeg -i demo.webm -vf "fps=10,scale=900:-1" demo.gif)
 *
 * Usage:
 *   cd scripts && npm install && node generate-demo.mjs
 */

import { chromium } from 'playwright';
import AnsiToHtml   from 'ansi-to-html';
import ffmpegInstaller from '@ffmpeg-installer/ffmpeg';
import ffmpeg from 'fluent-ffmpeg';
import { execSync }  from 'child_process';
import { fileURLToPath } from 'url';
import path from 'path';
import fs   from 'fs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT      = path.resolve(__dirname, '..');
const DOCS_DIR  = path.join(ROOT, 'docs');
const JAR       = path.join(ROOT, 'target', 'stacklens.jar');

if (!fs.existsSync(JAR)) {
  console.log('Building JAR first...');
  execSync('mvn clean package -q', { cwd: ROOT, stdio: 'inherit' });
}

fs.mkdirSync(DOCS_DIR, { recursive: true });

// ── Run the actual tool and capture ANSI output ───────────────────────────────

function run(args) {
  try {
    return execSync(`java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar "${JAR}" ${args}`, {
      cwd: ROOT, encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe']
    });
  } catch (e) {
    return (e.stdout ?? '') + (e.stderr ?? '');
  }
}

const converter = new AnsiToHtml({
  fg: '#d4d4d4', bg: '#1e1e1e', newline: true,
  escapeXML: true, stream: false
});

const scenes = [
  {
    cmd:    'java -jar stacklens.jar analyze samples/sample-npe.log',
    output: run('analyze samples/sample-npe.log'),
  },
  {
    cmd:    'java -jar stacklens.jar analyze samples/sample-mixed-errors.log --summary',
    output: run('analyze samples/sample-mixed-errors.log --summary'),
  },
  {
    cmd:    'cat samples/sample-db-failure.log | java -jar stacklens.jar analyze -',
    output: run('analyze samples/sample-db-failure.log'),
  },
];

const scenesJson = JSON.stringify(
  scenes.map(s => ({ cmd: s.cmd, html: converter.toHtml(s.output) }))
);

// ── Build the terminal HTML page ─────────────────────────────────────────────

const html = /* html */ `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }

  body {
    background: #1e1e1e;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding: 32px;
    min-height: 100vh;
    font-family: 'Menlo', 'Consolas', 'Monaco', monospace;
  }

  .window {
    width: 860px;
    background: #1e1e1e;
    border-radius: 10px;
    box-shadow: 0 24px 64px rgba(0,0,0,0.7);
    overflow: hidden;
    border: 1px solid #3a3a3a;
  }

  .titlebar {
    background: #2d2d2d;
    padding: 10px 14px;
    display: flex;
    align-items: center;
    gap: 8px;
    border-bottom: 1px solid #3a3a3a;
  }

  .dot { width: 12px; height: 12px; border-radius: 50%; }
  .dot.red    { background: #ff5f57; }
  .dot.yellow { background: #febc2e; }
  .dot.green  { background: #28c840; }

  .title {
    flex: 1;
    text-align: center;
    font-size: 12px;
    color: #888;
    letter-spacing: 0.5px;
  }

  .terminal {
    padding: 18px 20px;
    font-size: 13px;
    line-height: 1.55;
    color: #d4d4d4;
    min-height: 420px;
    white-space: pre-wrap;
    word-break: break-all;
  }

  .prompt { color: #4ec9b0; }
  .prompt::before { content: "$ "; color: #569cd6; }

  .cursor {
    display: inline-block;
    width: 8px;
    height: 14px;
    background: #d4d4d4;
    animation: blink 1s step-end infinite;
    vertical-align: text-bottom;
    margin-left: 1px;
  }
  @keyframes blink { 50% { opacity: 0; } }

  .output { opacity: 0; transition: opacity 0.15s ease-in; }
  .output.visible { opacity: 1; }
</style>
</head>
<body>
<div class="window">
  <div class="titlebar">
    <div class="dot red"></div>
    <div class="dot yellow"></div>
    <div class="dot green"></div>
    <div class="title">stacklens — bash</div>
  </div>
  <div class="terminal" id="term"></div>
</div>

<script>
const scenes = ${scenesJson};
const term   = document.getElementById('term');

let sceneIndex   = 0;
let charIndex    = 0;
let phase        = 'typing'; // 'typing' | 'output' | 'pause'
let currentCmd   = null;
let cmdEl        = null;
let outputEl     = null;
let cursorEl     = null;
let animDone     = false;

function appendPrompt() {
  const promptEl = document.createElement('span');
  promptEl.className = 'prompt';
  cmdEl = document.createElement('span');
  cursorEl = document.createElement('span');
  cursorEl.className = 'cursor';
  term.appendChild(promptEl);
  term.appendChild(cmdEl);
  term.appendChild(cursorEl);
}

function startScene(i) {
  if (i >= scenes.length) {
    animDone = true;
    if (cursorEl) cursorEl.remove();
    return;
  }
  sceneIndex = i;
  charIndex  = 0;
  phase      = 'typing';
  currentCmd = scenes[i].cmd;
  appendPrompt();
}

function tick() {
  if (animDone) return;

  if (phase === 'typing') {
    if (charIndex < currentCmd.length) {
      cmdEl.textContent += currentCmd[charIndex++];
      setTimeout(tick, 38);
    } else {
      // Done typing — show output
      phase = 'output';
      cursorEl.remove();
      cursorEl = null;

      const nl = document.createTextNode('\\n');
      term.appendChild(nl);

      outputEl = document.createElement('div');
      outputEl.className = 'output';
      outputEl.innerHTML = scenes[sceneIndex].html;
      term.appendChild(outputEl);

      requestAnimationFrame(() => {
        outputEl.classList.add('visible');
      });

      setTimeout(tick, 200);
    }
  } else if (phase === 'output') {
    // Pause before next command
    phase = 'pause';
    const gap = sceneIndex === scenes.length - 1 ? 2000 : 1600;
    setTimeout(tick, gap);
  } else if (phase === 'pause') {
    const sep = document.createTextNode('\\n');
    term.appendChild(sep);
    startScene(sceneIndex + 1);
    setTimeout(tick, 120);
  }
}

// Start after a short delay so Playwright can find the page
setTimeout(() => startScene(0), 400);
setTimeout(tick, 500);
</script>
</body>
</html>`;

const htmlPath = path.join(DOCS_DIR, 'demo.html');
fs.writeFileSync(htmlPath, html);
console.log('HTML written to', htmlPath);

// ── Playwright — screenshot + video ──────────────────────────────────────────

const totalDuration = 14_000; // ms — adjust if you add more scenes

console.log('Launching Playwright...');
const browser = await chromium.launch();

// ── 1. Static screenshot (end state) ─────────────────────────────────────────
{
  const page = await browser.newPage({ viewport: { width: 924, height: 780 } });
  await page.goto(`file://${htmlPath}`);
  await page.waitForTimeout(totalDuration);
  const screenshotPath = path.join(DOCS_DIR, 'demo.png');
  await page.screenshot({ path: screenshotPath, fullPage: false });
  console.log('Screenshot saved to', screenshotPath);
  await page.close();
}

// ── 2. Animated video ─────────────────────────────────────────────────────────
{
  const videoDir = path.join(DOCS_DIR, 'video_tmp');
  fs.mkdirSync(videoDir, { recursive: true });

  const context = await browser.newContext({
    viewport: { width: 924, height: 780 },
    recordVideo: { dir: videoDir, size: { width: 924, height: 780 } },
  });

  const page = await context.newPage();
  await page.goto(`file://${htmlPath}`);
  await page.waitForTimeout(totalDuration + 1000);

  await context.close(); // flushes the video

  // Move the recorded video to docs/demo.webm
  const [videoFile] = fs.readdirSync(videoDir).map(f => path.join(videoDir, f));
  const webmPath = path.join(DOCS_DIR, 'demo.webm');
  if (videoFile) {
    fs.renameSync(videoFile, webmPath);
    console.log('Video saved to', webmPath);
    console.log('');
    console.log('Convert to GIF (requires ffmpeg):');
    console.log('  ffmpeg -i docs/demo.webm -vf "fps=10,scale=900:-1:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" docs/demo.gif');
  }

  fs.rmSync(videoDir, { recursive: true, force: true });
}

await browser.close();

// ── Convert WebM → GIF ────────────────────────────────────────────────────────
const gifPath = path.join(DOCS_DIR, 'demo.gif');
ffmpeg.setFfmpegPath(ffmpegInstaller.path);

console.log('\nConverting to GIF...');
await new Promise((resolve, reject) => {
  ffmpeg(path.join(DOCS_DIR, 'demo.webm'))
    .outputOptions([
      '-vf', 'fps=12,scale=900:-1:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=128[p];[s1][p]paletteuse=dither=bayer',
    ])
    .output(gifPath)
    .on('end', resolve)
    .on('error', reject)
    .run();
});
console.log('GIF saved to', gifPath);

// Clean up intermediates
fs.rmSync(path.join(DOCS_DIR, 'demo.webm'), { force: true });
fs.rmSync(path.join(DOCS_DIR, 'demo.html'), { force: true });

console.log('\nDone. docs/demo.gif and docs/demo.png are ready.');
