#!/bin/bash

# Clean and run all tests
./gradlew clean test

# Display test results summary
echo "Test results are available at: build/reports/tests/test/index.html" 