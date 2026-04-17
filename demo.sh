#!/usr/bin/env bash
# StackLens demo script
# Records well when used with asciinema: asciinema rec demo.cast --command ./demo.sh
# Convert to GIF: agg demo.cast demo.gif  (https://github.com/asciinema/agg)

JAR="target/stacklens.jar"

if [ ! -f "$JAR" ]; then
  echo "Building StackLens..."
  mvn clean package -q
fi

pause() { sleep "$1"; }

type_cmd() {
  echo -n "$ "
  for char in $(echo "$1" | grep -o .); do
    echo -n "$char"
    sleep 0.04
  done
  echo
  sleep 0.3
}

clear
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  StackLens — Java log analyzer demo"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
pause 1

# ── Scene 1: The raw log ─────────────────────────────
echo ""
type_cmd "cat samples/sample-npe.log"
pause 0.3
cat samples/sample-npe.log
pause 2

# ── Scene 2: Full analysis ───────────────────────────
echo ""
type_cmd "java -jar $JAR analyze samples/sample-npe.log"
pause 0.3
java -jar "$JAR" analyze samples/sample-npe.log || true
pause 3

# ── Scene 3: Mixed errors ────────────────────────────
echo ""
type_cmd "java -jar $JAR analyze samples/sample-mixed-errors.log --summary"
pause 0.3
java -jar "$JAR" analyze samples/sample-mixed-errors.log --summary || true
pause 3

# ── Scene 4: JSON output ─────────────────────────────
echo ""
type_cmd "java -jar $JAR analyze samples/sample-oom.log --output json"
pause 0.3
java -jar "$JAR" analyze samples/sample-oom.log --output json || true
pause 3

# ── Scene 5: Stdin (kubectl / docker workflow) ───────
echo ""
type_cmd "cat samples/sample-db-failure.log | java -jar $JAR analyze -"
pause 0.3
cat samples/sample-db-failure.log | java -jar "$JAR" analyze - || true
pause 2

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  github.com/AbaSheger/stacklens"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
pause 2
