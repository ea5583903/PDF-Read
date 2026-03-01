import javax.swing.*;
import java.awt.*;

public class PDFViewerApp extends JFrame {
    private PDFViewerPanel viewerPanel;
    private JMenuBar menuBar;
    private JToolBar toolBar;

    public PDFViewerApp() {
        setTitle("PDF Viewer - VR 1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initializeComponents();
        createMenuBar();
        createToolBar();
        checkForUpdates();

        setVisible(true);
    }

    private void initializeComponents() {
        viewerPanel = new PDFViewerPanel();
        add(viewerPanel, BorderLayout.CENTER);
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open PDF");
        openItem.addActionListener(e -> openPDF());
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> closePDF());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(closeItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem zoomInItem = new JMenuItem("Zoom In");
        zoomInItem.addActionListener(e -> viewerPanel.zoomIn());
        JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
        zoomOutItem.addActionListener(e -> viewerPanel.zoomOut());
        JMenuItem fitWidthItem = new JMenuItem("Fit Width");
        fitWidthItem.addActionListener(e -> viewerPanel.fitWidth());
        JMenuItem fitPageItem = new JMenuItem("Fit Page");
        fitPageItem.addActionListener(e -> viewerPanel.fitPage());

        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.addSeparator();
        viewMenu.add(fitWidthItem);
        viewMenu.add(fitPageItem);

        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem searchItem = new JMenuItem("Search");
        searchItem.addActionListener(e -> viewerPanel.showSearchDialog());
        JMenuItem addNoteItem = new JMenuItem("Add Sticky Note");
        addNoteItem.addActionListener(e -> viewerPanel.enableStickyNoteMode());

        toolsMenu.add(searchItem);
        toolsMenu.addSeparator();
        toolsMenu.add(addNoteItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);

        setJMenuBar(menuBar);
    }

    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton openBtn = new JButton("Open");
        openBtn.addActionListener(e -> openPDF());

        toolBar.add(openBtn);
        toolBar.addSeparator();

        JButton prevBtn = new JButton("◀ Previous");
        prevBtn.addActionListener(e -> viewerPanel.previousPage());

        JButton nextBtn = new JButton("Next ▶");
        nextBtn.addActionListener(e -> viewerPanel.nextPage());

        toolBar.add(prevBtn);
        toolBar.add(nextBtn);
        toolBar.addSeparator();

        JButton zoomInBtn = new JButton("Zoom +");
        zoomInBtn.addActionListener(e -> viewerPanel.zoomIn());

        JButton zoomOutBtn = new JButton("Zoom -");
        zoomOutBtn.addActionListener(e -> viewerPanel.zoomOut());

        toolBar.add(zoomInBtn);
        toolBar.add(zoomOutBtn);
        toolBar.addSeparator();

        JButton addNoteBtn = new JButton("Add Sticky Note");
        addNoteBtn.addActionListener(e -> viewerPanel.enableStickyNoteMode());

        toolBar.add(addNoteBtn);

        add(toolBar, BorderLayout.NORTH);
    }

    private void openPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            viewerPanel.loadPDF(fileChooser.getSelectedFile());
        }
    }

    private void closePDF() {
        viewerPanel.closePDF();
    }

    private void checkForUpdates() {
        SwingUtilities.invokeLater(() -> {
            AutoUpdater.checkAndUpdate();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PDFViewerApp());
    }
}
