import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFMergeDialog extends JDialog {
    private DefaultListModel<File> fileListModel;
    private JList<File> fileList;
    private List<File> selectedFiles;

    public PDFMergeDialog(JFrame parent) {
        super(parent, "Merge PDFs", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);

        selectedFiles = new ArrayList<>();
        fileListModel = new DefaultListModel<>();

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        // File list
        fileList = new JList<>(fileListModel);
        fileList.setCellRenderer(new FileListCellRenderer());
        JScrollPane scrollPane = new JScrollPane(fileList);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addButton = new JButton("Add PDF");
        addButton.addActionListener(e -> addPDF());

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelected());

        JButton moveUpButton = new JButton("Move Up");
        moveUpButton.addActionListener(e -> moveUp());

        JButton moveDownButton = new JButton("Move Down");
        moveDownButton.addActionListener(e -> moveDown());

        JButton mergeButton = new JButton("Merge PDFs");
        mergeButton.addActionListener(e -> mergePDFs());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(moveUpButton);
        buttonPanel.add(moveDownButton);
        buttonPanel.add(mergeButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Info label
        JLabel infoLabel = new JLabel("Add PDF files and arrange them in the order you want to merge");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(infoLabel, BorderLayout.NORTH);
    }

    private void addPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        fileChooser.setMultiSelectionEnabled(true);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                if (!selectedFiles.contains(file)) {
                    selectedFiles.add(file);
                    fileListModel.addElement(file);
                }
            }
        }
    }

    private void removeSelected() {
        int[] indices = fileList.getSelectedIndices();
        for (int i = indices.length - 1; i >= 0; i--) {
            selectedFiles.remove(indices[i]);
            fileListModel.remove(indices[i]);
        }
    }

    private void moveUp() {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex > 0) {
            File file = selectedFiles.remove(selectedIndex);
            selectedFiles.add(selectedIndex - 1, file);
            fileListModel.remove(selectedIndex);
            fileListModel.add(selectedIndex - 1, file);
            fileList.setSelectedIndex(selectedIndex - 1);
        }
    }

    private void moveDown() {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex < selectedFiles.size() - 1 && selectedIndex >= 0) {
            File file = selectedFiles.remove(selectedIndex);
            selectedFiles.add(selectedIndex + 1, file);
            fileListModel.remove(selectedIndex);
            fileListModel.add(selectedIndex + 1, file);
            fileList.setSelectedIndex(selectedIndex + 1);
        }
    }

    private void mergePDFs() {
        if (selectedFiles.size() < 2) {
            JOptionPane.showMessageDialog(this, "Please select at least 2 PDF files to merge.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        fileChooser.setSelectedFile(new File("merged.pdf"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();

            try {
                PDFMergerUtility merger = new PDFMergerUtility();

                for (File file : selectedFiles) {
                    merger.addSource(file);
                }

                merger.setDestinationFileName(outputFile.getAbsolutePath());
                merger.mergeDocuments(null);

                JOptionPane.showMessageDialog(this, "PDFs merged successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error merging PDFs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class FileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                File file = (File) value;
                setText((index + 1) + ". " + file.getName());
            }
            return this;
        }
    }
}
