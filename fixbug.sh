
#!/bin/bash
# fail if any commands fails
set -e
# debug log
set -x

# For newer Android SDK:
sdkmanager "extras;android;m2repository"
sdkmanager "extras;google;m2repository"

# For older Android SDK:
echo y | android update sdk --no-ui --all --filter extra-android-m2repository | grep 'package installed'
echo y | android update sdk --no-ui --all --filter extra-google-m2repository | grep 'package installed'
echo yes | sdkmanager "extras;m2repository;com;android;support;constraint;constraint-layout;1.0.2"
echo yes | sdkmanager "extras;m2repository;com;android;support;constraint;constraint-layout-solver;1.0.2"
