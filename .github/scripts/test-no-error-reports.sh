if [[ -d "run/crash-reports" ]]; then
  echo "Crash reports detected:"
  cat $directory/*
  exit 1
fi

if grep --quiet "Fatal errors were detected" client.log; then
  echo "Fatal errors detected:"
  cat client.log
  exit 1
fi

if grep --quiet "---- Minecraft Crash Report ----" client.log; then
  echo "Client force stopped:"
  cat client.log
  exit 1
fi

echo "No crash reports detected"
exit 0

