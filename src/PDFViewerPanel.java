import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PDFViewerPanel extends JPanel {
    private PDDocument document;
    private PDFRenderer renderer;
    private BufferedImage currentPageImage;
    private int currentPage = 0;
    private float zoomLevel = 1.0f;
    private JScrollPane scrollPane;
    private ImagePanel imagePanel;
    private JLabel statusLabel;
    private File currentPDFFile;

    private boolean stickyNoteMode = false;
    private List<StickyNote> stickyNotes = new ArrayList<>();

    public PDFViewerPanel() {
        setLayout(new BorderLayout());

        imagePanel = new ImagePanel();
        scrollPane = new JScrollPane(imagePanel);
        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("No PDF loaded");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (stickyNoteMode) {
                    addStickyNoteAtPosition(e.getX(), e.getY());
                } else {
                    // Check if clicked on existing sticky note
                    checkStickyNoteClick(e.getX(), e.getY());
                }
            }
        });
    }

    public void loadPDF(File file) {
        try {
            if (document != null) {
                saveStickyNotes();
                document.close();
            }

            currentPDFFile = file;
            document = PDDocument.load(file);
            renderer = new PDFRenderer(document);
            currentPage = 0;
            loadStickyNotes();
            renderCurrentPage();
            updateStatus();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void closePDF() {
        try {
            if (document != null) {
                saveStickyNotes();
                document.close();
                document = null;
                renderer = null;
                currentPageImage = null;
                currentPDFFile = null;
                stickyNotes.clear();
                imagePanel.repaint();
                statusLabel.setText("No PDF loaded");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error closing PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderCurrentPage() {
        if (document == null) return;

        try {
            currentPageImage = renderer.renderImageWithDPI(currentPage, 72 * zoomLevel);
            imagePanel.setImage(currentPageImage);
            imagePanel.revalidate();
            imagePanel.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error rendering page: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void nextPage() {
        if (document != null && currentPage < document.getNumberOfPages() - 1) {
            currentPage++;
            renderCurrentPage();
            updateStatus();
        }
    }

    public void previousPage() {
        if (document != null && currentPage > 0) {
            currentPage--;
            renderCurrentPage();
            updateStatus();
        }
    }

    public void zoomIn() {
        zoomLevel += 0.25f;
        if (zoomLevel > 5.0f) zoomLevel = 5.0f;
        renderCurrentPage();
        updateStatus();
    }

    public void zoomOut() {
        zoomLevel -= 0.25f;
        if (zoomLevel < 0.25f) zoomLevel = 0.25f;
        renderCurrentPage();
        updateStatus();
    }

    public void fitWidth() {
        if (document == null) return;
        int viewportWidth = scrollPane.getViewport().getWidth();
        try {
            BufferedImage testImage = renderer.renderImageWithDPI(currentPage, 72);
            zoomLevel = (float) viewportWidth / testImage.getWidth();
            renderCurrentPage();
            updateStatus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fitPage() {
        if (document == null) return;
        int viewportWidth = scrollPane.getViewport().getWidth();
        int viewportHeight = scrollPane.getViewport().getHeight();
        try {
            BufferedImage testImage = renderer.renderImageWithDPI(currentPage, 72);
            float widthRatio = (float) viewportWidth / testImage.getWidth();
            float heightRatio = (float) viewportHeight / testImage.getHeight();
            zoomLevel = Math.min(widthRatio, heightRatio) * 0.95f;
            renderCurrentPage();
            updateStatus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enableStickyNoteMode() {
        if (document == null) {
            JOptionPane.showMessageDialog(this, "Please open a PDF first.", "No PDF Open", JOptionPane.WARNING_MESSAGE);
            return;
        }
        stickyNoteMode = true;
        JOptionPane.showMessageDialog(this, "Click anywhere on the page to add a sticky note.", "Sticky Note Mode", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addStickyNoteAtPosition(int x, int y) {
        String text = JOptionPane.showInputDialog(this, "Enter sticky note text:");
        if (text != null && !text.trim().isEmpty()) {
            StickyNote note = new StickyNote(currentPage, x, y, text, Color.YELLOW);
            stickyNotes.add(note);
            saveStickyNotes();
            imagePanel.repaint();
        }
        stickyNoteMode = false;
    }

    private void checkStickyNoteClick(int x, int y) {
        for (int i = stickyNotes.size() - 1; i >= 0; i--) {
            StickyNote note = stickyNotes.get(i);
            if (note.pageNumber == currentPage && note.contains(x, y)) {
                showStickyNoteOptions(note);
                break;
            }
        }
    }

    private void showStickyNoteOptions(StickyNote note) {
        String[] options = {"View/Edit", "Delete", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Sticky Note:\n" + note.text,
                "Sticky Note",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) { // View/Edit
            String newText = JOptionPane.showInputDialog(this, "Edit sticky note:", note.text);
            if (newText != null && !newText.trim().isEmpty()) {
                note.text = newText;
                saveStickyNotes();
                imagePanel.repaint();
            }
        } else if (choice == 1) { // Delete
            stickyNotes.remove(note);
            saveStickyNotes();
            imagePanel.repaint();
        }
    }

    private void saveStickyNotes() {
        if (currentPDFFile == null) return;

        String notesFilePath = currentPDFFile.getAbsolutePath() + ".notes";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(notesFilePath))) {
            oos.writeObject(stickyNotes);
        } catch (IOException e) {
            System.err.println("Error saving sticky notes: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadStickyNotes() {
        stickyNotes.clear();
        if (currentPDFFile == null) return;

        String notesFilePath = currentPDFFile.getAbsolutePath() + ".notes";
        File notesFile = new File(notesFilePath);

        if (notesFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(notesFile))) {
                stickyNotes = (List<StickyNote>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading sticky notes: " + e.getMessage());
                stickyNotes = new ArrayList<>();
            }
        }
    }

    public void showSearchDialog() {
        if (document == null) {
            JOptionPane.showMessageDialog(this, "Please open a PDF first.", "No PDF Open", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String searchText = JOptionPane.showInputDialog(this, "Enter text to search:");
        if (searchText != null && !searchText.trim().isEmpty()) {
            searchInPDF(searchText);
        }
    }

    private void searchInPDF(String searchText) {
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            List<SearchResult> results = new ArrayList<>();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String pageText = stripper.getText(document);

                String lowerPageText = pageText.toLowerCase();
                String lowerSearchText = searchText.toLowerCase();
                int count = 0;
                int index = 0;

                while ((index = lowerPageText.indexOf(lowerSearchText, index)) != -1) {
                    count++;
                    index += lowerSearchText.length();
                }

                if (count > 0) {
                    int firstIndex = lowerPageText.indexOf(lowerSearchText);
                    int contextStart = Math.max(0, firstIndex - 30);
                    int contextEnd = Math.min(pageText.length(), firstIndex + searchText.length() + 30);
                    String context = pageText.substring(contextStart, contextEnd).trim();
                    if (contextStart > 0) context = "..." + context;
                    if (contextEnd < pageText.length()) context = context + "...";

                    results.add(new SearchResult(i, count, context));
                }
            }

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Text not found in PDF.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showSearchResults(searchText, results);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error searching PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSearchResults(String searchText, List<SearchResult> results) {
        SearchResultsDialog dialog = new SearchResultsDialog(searchText, results, this);
        dialog.setVisible(true);
    }

    public void goToPage(int pageNumber) {
        if (pageNumber >= 0 && pageNumber < document.getNumberOfPages()) {
            currentPage = pageNumber;
            renderCurrentPage();
            updateStatus();
        }
    }

    private void updateStatus() {
        if (document != null) {
            statusLabel.setText(String.format("Page %d of %d | Zoom: %.0f%% | Sticky Notes: %d on this page",
                    currentPage + 1, document.getNumberOfPages(), zoomLevel * 100, countNotesOnCurrentPage()));
        }
    }

    private int countNotesOnCurrentPage() {
        int count = 0;
        for (StickyNote note : stickyNotes) {
            if (note.pageNumber == currentPage) {
                count++;
            }
        }
        return count;
    }

    public boolean hasDocument() {
        return document != null;
    }

    public PDDocument getCurrentDocument() {
        return document;
    }

    private static class StickyNote implements Serializable {
        private static final long serialVersionUID = 1L;
        int pageNumber;
        int x, y;
        String text;
        Color color;
        static final int WIDTH = 150;
        static final int HEIGHT = 100;

        StickyNote(int pageNumber, int x, int y, String text, Color color) {
            this.pageNumber = pageNumber;
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
        }

        boolean contains(int px, int py) {
            return px >= x && px <= x + WIDTH && py >= y && py <= y + HEIGHT;
        }
    }

    private static class SearchResult {
        int pageNumber;
        int occurrences;
        String context;

        SearchResult(int pageNumber, int occurrences, String context) {
            this.pageNumber = pageNumber;
            this.occurrences = occurrences;
            this.context = context;
        }
    }

    private class SearchResultsDialog extends JDialog {
        public SearchResultsDialog(String searchText, List<SearchResult> results, PDFViewerPanel parent) {
            super((JFrame) SwingUtilities.getWindowAncestor(parent), "Search Results", true);
            setSize(600, 400);
            setLocationRelativeTo(parent);

            setLayout(new BorderLayout(10, 10));

            JLabel headerLabel = new JLabel("Found \"" + searchText + "\" in " + results.size() + " page(s)");
            headerLabel.setFont(headerLabel.getFont().deriveFont(14f));
            headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
            add(headerLabel, BorderLayout.NORTH);

            DefaultListModel<String> listModel = new DefaultListModel<>();
            int totalOccurrences = 0;

            for (SearchResult result : results) {
                totalOccurrences += result.occurrences;
                String displayText = String.format("Page %d (%d occurrence%s): %s",
                        result.pageNumber + 1,
                        result.occurrences,
                        result.occurrences > 1 ? "s" : "",
                        result.context);
                listModel.addElement(displayText);
            }

            JList<String> resultsList = new JList<>(listModel);
            resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            resultsList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int selectedIndex = resultsList.getSelectedIndex();
                        if (selectedIndex >= 0) {
                            SearchResult selected = results.get(selectedIndex);
                            parent.goToPage(selected.pageNumber);
                            dispose();
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(resultsList);
            add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            JButton goToButton = new JButton("Go to Page");
            goToButton.addActionListener(e -> {
                int selectedIndex = resultsList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    SearchResult selected = results.get(selectedIndex);
                    parent.goToPage(selected.pageNumber);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a result first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());

            JLabel footerLabel = new JLabel("Total: " + totalOccurrences + " occurrence(s). Double-click to go to page.");
            footerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            buttonPanel.add(goToButton);
            buttonPanel.add(closeButton);

            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.add(footerLabel, BorderLayout.NORTH);
            southPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(southPanel, BorderLayout.SOUTH);
        }
    }

    private class ImagePanel extends JPanel {
        private BufferedImage image;

        public void setImage(BufferedImage image) {
            this.image = image;
            if (image != null) {
                setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, null);

                // Draw sticky notes on current page
                Graphics2D g2d = (Graphics2D) g;
                for (StickyNote note : stickyNotes) {
                    if (note.pageNumber == currentPage) {
                        // Draw sticky note
                        g2d.setColor(note.color);
                        g2d.fillRect(note.x, note.y, StickyNote.WIDTH, StickyNote.HEIGHT);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(note.x, note.y, StickyNote.WIDTH, StickyNote.HEIGHT);

                        // Draw text
                        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                        String[] lines = wrapText(note.text, g2d, StickyNote.WIDTH - 10);
                        int yOffset = note.y + 15;
                        for (int i = 0; i < Math.min(lines.length, 6); i++) {
                            g2d.drawString(lines[i], note.x + 5, yOffset);
                            yOffset += 12;
                        }
                    }
                }
            }
        }

        private String[] wrapText(String text, Graphics2D g2d, int maxWidth) {
            List<String> lines = new ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                int width = g2d.getFontMetrics().stringWidth(testLine);

                if (width > maxWidth && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }

            return lines.toArray(new String[0]);
        }
    }
}
