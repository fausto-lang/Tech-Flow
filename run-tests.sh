#!/usr/bin/env bash
# Menú interactivo de tests de Tech-Flow.
# Toda la lógica (gum, tabla por clase, resumen) vive en techflow-tests.sh
set -u

exec "$HOME/.config/scripts/techflow-tests.sh" -l
