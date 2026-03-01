import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.multipdf.Splitter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PDFSplitDialog extends JDialog {
    private PDDocument document;
    private JRadioButton splitByPageRangeRadio;
    private JRadioButton splitEachPageRadio;
    private JRadioButton splitByCountRadio;
    private JTextField rangeTextField;
    private JTextField countTextField;
    private JLabel totalPagesLabel;

    public PDFSplitDialog(JFrame parent, PDDocument document) {
        super(parent, "Split PDF", true);
        this.document = document;
        setSize(500, 350);
        setLocationRelativeTo(parent);

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Total pages info
        totalPagesLabel = new JLabel("Total pages in PDF: " + document.getNumberOfPages());
        totalPagesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(totalPagesLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Split options
        ButtonGroup buttonGroup = new ButtonGroup();

        splitByPageRangeRadio = new JRadioButton("Split by page range");
        splitByPageRangeRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonGroup.add(splitByPageRangeRadio);
        mainPanel.add(splitByPageRangeRadio);

        JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rangePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rangePanel.add(new JLabel("Pages (e.g., 1-3, 5-7):"));
        rangeTextField = new JTextField(20);
        rangePanel.add(rangeTextField);
        mainPanel.add(rangePanel);
        mainPanel.add(Box.createVerticalStrut(10));

        splitEachPageRadio = new JRadioButton("Split into individual pages");
        splitEachPageRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonGroup.add(splitEachPageRadio);
        mainPanel.add(splitEachPageRadio);
        mainPanel.add(Box.createVerticalStrut(10));

        splitByCountRadio = new JRadioButton("Split every N pages");
        splitByCountRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonGroup.add(splitByCountRadio);
        mainPanel.add(splitByCountRadio);

        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countPanel.add(new JLabel("Pages per split:"));
        countTextField = new JTextField(10);
        countPanel.add(countTextField);
        mainPanel.add(countPanel);

        splitByPageRangeRadio.setSelected(true);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton splitButton = new JButton("Split PDF");
        splitButton.addActionListener(e -> splitPDF());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(splitButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void splitPDF() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("Select output folder");

        int result = folderChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFolder = folderChooser.getSelectedFile();

            try {
                if (splitByPageRangeRadio.isSelected()) {
                    splitByPageRange(outputFolder);
                } else if (splitEachPageRadio.isSelected()) {
                    splitEachPage(outputFolder);
                } else if (splitByCountRadio.isSelected()) {
                    splitByCount(outputFolder);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error splitting PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void splitByPageRange(File outputFolder) throws IOException {
        String rangeText = rangeTextField.getText().trim();
        if (rangeText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter page ranges.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] ranges = rangeText.split(",");
        int splitCount = 0;

        for (String range : ranges) {
            range = range.trim();
            String[] parts = range.split("-");

            if (parts.length != 2) {
                JOptionPane.showMessageDialog(this, "Invalid range format: " + range, "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int startPage = Integer.parseInt(parts[0].trim()) - 1;
                int endPage = Integer.parseInt(parts[1].trim()) - 1;

                if (startPage < 0 || endPage >= document.getNumberOfPages() || startPage > endPage) {
                    JOptionPane.showMessageDialog(this, "Invalid page range: " + range, "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                PDDocument splitDoc = new PDDocument();
                for (int i = startPage; i <= endPage; i++) {
                    splitDoc.addPage(document.getPage(i));
                }

                File outputFile = new File(outputFolder, "split_" + (++splitCount) + "_pages_" + (startPage + 1) + "-" + (endPage + 1) + ".pdf");
                splitDoc.save(outputFile);
                splitDoc.close();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid page number in range: " + range, "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "PDF split into " + splitCount + " files!", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void splitEachPage(File outputFolder) throws IOException {
        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(1);

        List<PDDocument> splitDocs = splitter.split(document);
        int pageNum = 1;

        for (PDDocument doc : splitDocs) {
            File outputFile = new File(outputFolder, "page_" + pageNum + ".pdf");
            doc.save(outputFile);
            doc.close();
            pageNum++;
        }

        JOptionPane.showMessageDialog(this, "PDF split into " + splitDocs.size() + " files!", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void splitByCount(File outputFolder) throws IOException {
        String countText = countTextField.getText().trim();
        if (countText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter pages per split.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int pagesPerSplit = Integer.parseInt(countText);

            if (pagesPerSplit <= 0) {
                JOptionPane.showMessageDialog(this, "Pages per split must be greater than 0.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(pagesPerSplit);

            List<PDDocument> splitDocs = splitter.split(document);
            int splitNum = 1;

            for (PDDocument doc : splitDocs) {
                File outputFile = new File(outputFolder, "split_" + splitNum + ".pdf");
                doc.save(outputFile);
                doc.close();
                splitNum++;
            }

            JOptionPane.showMessageDialog(this, "PDF split into " + splitDocs.size() + " files!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}
