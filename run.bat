@echo off

REM Create lib directory if it doesn't exist
if not exist lib mkdir lib

REM Download PDFBox and dependencies if not already present
if not exist lib\pdfbox-2.0.30.jar (
    echo Downloading PDFBox...
    curl -L -o lib\pdfbox-2.0.30.jar https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.30/pdfbox-2.0.30.jar
)

if not exist lib\fontbox-2.0.30.jar (
    echo Downloading FontBox...
    curl -L -o lib\fontbox-2.0.30.jar https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/2.0.30/fontbox-2.0.30.jar
)

if not exist lib\commons-logging-1.2.jar (
    echo Downloading Commons Logging...
    curl -L -o lib\commons-logging-1.2.jar https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar
)

REM Create out directory
if not exist out mkdir out

REM Compile
echo Compiling...
javac -cp "lib\*" -d out src\PDFViewerApp.java src\PDFViewerPanel.java src\AutoUpdater.java

REM Run
echo Running PDF Viewer...
java -cp "out;lib\*" PDFViewerApp
