#!/bin/bash

# Create lib directory if it doesn't exist
mkdir -p lib

# Download PDFBox and dependencies if not already present
if [ ! -f lib/pdfbox-2.0.30.jar ]; then
    echo "Downloading PDFBox..."
    curl -L -o lib/pdfbox-2.0.30.jar https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.30/pdfbox-2.0.30.jar
fi

if [ ! -f lib/fontbox-2.0.30.jar ]; then
    echo "Downloading FontBox..."
    curl -L -o lib/fontbox-2.0.30.jar https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/2.0.30/fontbox-2.0.30.jar
fi

if [ ! -f lib/commons-logging-1.2.jar ]; then
    echo "Downloading Commons Logging..."
    curl -L -o lib/commons-logging-1.2.jar https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar
fi

# Create out directory if it doesn't exist
mkdir -p out

# Compile
echo "Compiling..."
javac -cp "lib/*" -d out src/PDFViewerApp.java src/PDFViewerPanel.java src/AutoUpdater.java

# Run
echo "Running PDF Viewer..."
java -cp "out:lib/*" PDFViewerApp
