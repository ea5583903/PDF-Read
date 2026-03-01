import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class PDFCreatorDialog extends JDialog {
    private PDDocument createdPDF;
    private JComboBox<String> pageSizeCombo;
    private JSpinner pageCountSpinner;
    private JTextField titleField;
    private JTextArea contentArea;
    private JCheckBox addContentCheck;

    public PDFCreatorDialog(JFrame parent) {
        super(parent, "Create New PDF", true);
        setSize(500, 450);
        setLocationRelativeTo(parent);

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.add(new JLabel("Document Title:"));
        titleField = new JTextField(30);
        titlePanel.add(titleField);
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Page size
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sizePanel.add(new JLabel("Page Size:"));
        pageSizeCombo = new JComboBox<>(new String[]{"Letter (8.5 x 11)", "A4", "Legal (8.5 x 14)", "Tabloid (11 x 17)"});
        sizePanel.add(pageSizeCombo);
        mainPanel.add(sizePanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Page count
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countPanel.add(new JLabel("Number of Pages:"));
        pageCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        countPanel.add(pageCountSpinner);
        mainPanel.add(countPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Add content checkbox
        addContentCheck = new JCheckBox("Add initial content to first page");
        addContentCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        addContentCheck.setSelected(false);
        addContentCheck.addActionListener(e -> contentArea.setEnabled(addContentCheck.isSelected()));
        mainPanel.add(addContentCheck);
        mainPanel.add(Box.createVerticalStrut(10));

        // Content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(new JLabel("Content:"), BorderLayout.NORTH);
        contentArea = new JTextArea(8, 40);
        contentArea.setEnabled(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(contentPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton createButton = new JButton("Create PDF");
        createButton.addActionListener(e -> createPDF());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createPDF() {
        try {
            createdPDF = new PDDocument();

            int pageCount = (Integer) pageCountSpinner.getValue();
            PDRectangle pageSize = getSelectedPageSize();

            for (int i = 0; i < pageCount; i++) {
                PDPage page = new PDPage(pageSize);
                createdPDF.addPage(page);

                // Add content to first page if requested
                if (i == 0 && addContentCheck.isSelected() && !contentArea.getText().trim().isEmpty()) {
                    PDPageContentStream contentStream = new PDPageContentStream(createdPDF, page);

                    // Add title if provided
                    String title = titleField.getText().trim();
                    if (!title.isEmpty()) {
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                        contentStream.newLineAtOffset(50, pageSize.getHeight() - 50);
                        contentStream.showText(title);
                        contentStream.endText();
                    }

                    // Add content
                    String content = contentArea.getText();
                    String[] lines = content.split("\n");

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    float yPosition = pageSize.getHeight() - (title.isEmpty() ? 50 : 80);
                    contentStream.newLineAtOffset(50, yPosition);

                    for (String line : lines) {
                        if (yPosition < 50) break; // Stop if we run out of space

                        // Handle long lines
                        if (line.length() > 80) {
                            String[] words = line.split(" ");
                            StringBuilder currentLine = new StringBuilder();

                            for (String word : words) {
                                if (currentLine.length() + word.length() + 1 > 80) {
                                    contentStream.showText(currentLine.toString());
                                    contentStream.newLineAtOffset(0, -15);
                                    yPosition -= 15;
                                    currentLine = new StringBuilder(word + " ");
                                } else {
                                    currentLine.append(word).append(" ");
                                }
                            }
                            if (currentLine.length() > 0) {
                                contentStream.showText(currentLine.toString());
                                contentStream.newLineAtOffset(0, -15);
                                yPosition -= 15;
                            }
                        } else {
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -15);
                            yPosition -= 15;
                        }
                    }

                    contentStream.endText();
                    contentStream.close();
                }
            }

            JOptionPane.showMessageDialog(this,
                    "PDF created with " + pageCount + " page(s)!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error creating PDF: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            createdPDF = null;
        }
    }

    private PDRectangle getSelectedPageSize() {
        String selected = (String) pageSizeCombo.getSelectedItem();

        if (selected.startsWith("Letter")) {
            return PDRectangle.LETTER;
        } else if (selected.startsWith("A4")) {
            return PDRectangle.A4;
        } else if (selected.startsWith("Legal")) {
            return PDRectangle.LEGAL;
        } else if (selected.startsWith("Tabloid")) {
            return new PDRectangle(792, 1224); // 11x17 inches
        }

        return PDRectangle.LETTER;
    }

    public PDDocument getCreatedPDF() {
        return createdPDF;
    }
}
