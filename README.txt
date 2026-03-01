PDF Viewer - VR 1.0
====================

A simple PDF viewer with sticky note support!

Features:
---------
- View and navigate PDF files
- Zoom controls (Zoom In/Out, Fit Width, Fit Page)
- Search text in PDFs (shows all results with page numbers)
- Add sticky notes that are only visible in this app
- Sticky notes are saved separately (not embedded in the PDF)
- Edit or delete sticky notes by clicking on them
- Auto-update feature

How to Run:
-----------
Linux/Mac:
  ./run.sh

Windows:
  run.bat

The script will automatically download required libraries on first run.

Using Sticky Notes:
-------------------
1. Open a PDF file
2. Click "Add Sticky Note" button or Tools > Add Sticky Note
3. Click anywhere on the PDF page where you want the note
4. Enter your note text
5. The sticky note appears as a yellow box on the page

To view/edit/delete a sticky note:
- Click on any existing sticky note
- Choose "View/Edit" to change the text
- Choose "Delete" to remove it

IMPORTANT: Sticky notes are saved in a .notes file next to your PDF
           (example: document.pdf.notes)
           These notes are ONLY visible in this app, not in other PDF viewers!

Search Feature:
---------------
1. Click Tools > Search
2. Enter your search text
3. View ALL results with:
   - Page numbers
   - Number of occurrences per page
   - Text context around each match
4. Double-click any result to jump to that page

Auto-Update:
------------
The app automatically checks for updates on startup.
If a new version is available, you'll be prompted to update.
Your files are backed up before updating.
