import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.awt.datatransfer.*;

// =================== FINESTRA PRINCIPALE =====================
public class VisualJava extends JFrame {
    DefaultListModel<String> projectModel = new DefaultListModel<>();
    JList<String> projectList = new JList<>(projectModel);
    HashMap<String, File> projectFiles = new HashMap<>();

    public VisualJava() {
        setTitle("Visual Java - Project Manager");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // Colore di sfondo moderno
        getContentPane().setBackground(new Color(30, 35, 45));

        loadProjects();

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.setBackground(new Color(30, 35, 45));

        JLabel titleLabel = new JLabel("My Projects", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(220, 230, 240));
        centerPanel.add(titleLabel, BorderLayout.NORTH);

        projectList.setFont(new Font("Arial", Font.PLAIN, 14));
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.setBackground(new Color(45, 52, 65));
        projectList.setForeground(new Color(220, 230, 240));
        JScrollPane scrollPane = new JScrollPane(projectList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 70, 85), 2),
                "Saved Projects",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(180, 190, 200)
        ));
        scrollPane.getViewport().setBackground(new Color(45, 52, 65));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(new Color(30, 35, 45));

        JButton createBtn = createStyledButton("New Project", new Color(50, 120, 200));
        createBtn.addActionListener(e -> createProject());

        JButton editBtn = createStyledButton("Edit Project", new Color(70, 140, 210));
        editBtn.addActionListener(e -> editProject());

        JButton renameBtn = createStyledButton("Rename Project", new Color(100, 150, 210));
        renameBtn.addActionListener(e -> renameProject());

        JButton deleteBtn = createStyledButton("Delete Project", new Color(200, 70, 70));
        deleteBtn.addActionListener(e -> deleteProject());

        JButton exitBtn = createStyledButton("Exit", new Color(100, 110, 130));
        exitBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(createBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(renameBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(exitBtn);

        add(buttonPanel, BorderLayout.EAST);

        projectList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editProject();
            }
        });
    }

    JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    void createProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File dir = chooser.getSelectedFile();
        String name = JOptionPane.showInputDialog(this, "Nome del nuovo progetto:");
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim();
        if (projectModel.contains(name)) {
            JOptionPane.showMessageDialog(this, "Un progetto con questo nome esiste già!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        VisualJavaEditor.VisualJavaProject project = new VisualJavaEditor.VisualJavaProject();
        VisualJavaEditor.VisualJavaSprite defaultSprite = new VisualJavaEditor.VisualJavaSprite("Sprite1", 400, 300);
        defaultSprite.createDefaultSquare();
        project.sprites.add(defaultSprite);
        File file = new File(dir, name + ".ser");
        saveProject(file, project);
        projectModel.addElement(name);
        projectFiles.put(name, file);
        new VisualJavaEditor(file, name);
    }

    void editProject() {
        String name = projectList.getSelectedValue();
        if (name != null) {
            File f = projectFiles.get(name);
            if (f == null) f = new File(name + ".ser");
            new VisualJavaEditor(f, name);
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona un progetto!", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }
    }

    void deleteProject() {
        String name = projectList.getSelectedValue();
        if (name != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Eliminare '" + name + "'?", "Conferma", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                File f = projectFiles.get(name);
                if (f == null) f = new File(name + ".ser");
                if (f.exists()) f.delete();
                projectFiles.remove(name);
                projectModel.removeElement(name);
            }
        }
    }

    void renameProject() {
        String oldName = projectList.getSelectedValue();
        if (oldName == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un progetto!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newName = JOptionPane.showInputDialog(this, "Nuovo nome progetto:", oldName);
        if (newName == null) return;
        newName = newName.trim();
        if (newName.isEmpty()) return;
        if (projectModel.contains(newName)) {
            JOptionPane.showMessageDialog(this, "Esiste già un progetto con questo nome.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File oldFile = projectFiles.get(oldName);
        if (oldFile == null) oldFile = new File(oldName + ".ser");
        File newFile = new File(oldFile.getParentFile(), newName + ".ser");
        boolean ok = oldFile.renameTo(newFile);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Impossibile rinominare il file del progetto.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        projectFiles.remove(oldName);
        projectFiles.put(newName, newFile);
        projectModel.removeElement(oldName);
        projectModel.addElement(newName);
        projectList.setSelectedValue(newName, true);
    }

    void loadProjects() {
        File[] files = new File(".").listFiles((d, f) -> f.endsWith(".ser"));
        if (files != null) {
            for (File f : files) {
                String n = f.getName().replace(".ser", "");
                projectModel.addElement(n);
                projectFiles.put(n, f);
            }
        }
    }

    static void saveProject(File file, VisualJavaEditor.VisualJavaProject project) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(project);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new VisualJava().setVisible(true));
    }

    // =================== ENUM PER FORME BLOCCHI =====================
    enum BlockShape {
        NORMAL, HAT, CAP, BOOLEAN, REPORTER, CONTROL
    }

    // =================== EDITOR SCRATCH =====================
    class VisualJavaEditor extends JFrame {
        String projectName;
        File projectFile;
        VisualJavaProject project;
        VisualJavaSprite selectedSprite;

        DefaultListModel<String> spriteListModel = new DefaultListModel<>();
        JList<String> spriteList = new JList<>(spriteListModel);

        JPanel scriptArea;
        ArrayList<VisualJavaScriptBlock> scriptBlocks = new ArrayList<>();
        VisualJavaScriptBlock draggedBlock = null;
        Point dragOffset = new Point();

        // Sistema copia/incolla
        VisualJavaScriptBlock copiedBlock = null;

        JTabbedPane categoryTabs;

        // Evita che il drag intercetti l'interazione con i controlli (combo, textfield, bottoni)
        boolean suppressDragGesture = false;

        public VisualJavaEditor(File projectFile, String projectName) {
            this.projectName = projectName;
            this.projectFile = projectFile != null ? projectFile : new File(projectName + ".ser");
            setTitle("Scratch Editor - " + projectName);
            setSize(1400, 800);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout());
            setLocationRelativeTo(null);

            getContentPane().setBackground(new Color(25, 30, 40));

            loadProject();

            // TOP TOOLBAR
            JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            topBar.setBackground(new Color(35, 40, 50));
            topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(60, 70, 85)));

            JButton filesBtn = createToolbarButton("Files", new Color(100, 110, 130));
            filesBtn.addActionListener(e -> { VisualJava m = new VisualJava(); m.setVisible(true); dispose(); });

            JButton saveBtn = createToolbarButton("Save", new Color(50, 120, 200));
            saveBtn.addActionListener(e -> saveProject());

            JButton testBtn = createToolbarButton("Run", new Color(70, 180, 100));
            testBtn.addActionListener(e -> executeScript());

            JButton clearBtn = createToolbarButton("Clear Script", new Color(200, 100, 70));
            clearBtn.addActionListener(e -> clearScript());

            JButton clearAllBtn = createToolbarButton("Clear All", new Color(200, 80, 80));
            clearAllBtn.addActionListener(e -> clearAllScripts());

            JButton exitBtn = createToolbarButton("Exit", new Color(100, 110, 130));
            exitBtn.addActionListener(e -> dispose());

            topBar.add(filesBtn);
            topBar.add(saveBtn);
            topBar.add(testBtn);
            topBar.add(clearBtn);
            topBar.add(clearAllBtn);
            topBar.add(exitBtn);

            add(topBar, BorderLayout.NORTH);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(25, 30, 40));

            // LEFT PANEL - Categorie blocchi
            categoryTabs = new JTabbedPane(JTabbedPane.LEFT);
            categoryTabs.setPreferredSize(new Dimension(280, 0));
            categoryTabs.setBackground(new Color(35, 40, 50));
            categoryTabs.setForeground(new Color(51, 28, 10));

            addCategory("Movement", createMovementBlocks(), new Color(45, 100, 180));
            addCategory("Scene", createSceneBlocks(), new Color(220, 120, 120));
            addCategory("Events", createEventBlocks(), new Color(200, 140, 50));
            addCategory("Control", createControlBlocks(), new Color(200, 140, 50));
            addCategory("Condition", createSensorBlocks(), new Color(50, 160, 180));
            addCategory("Math", createOperatorBlocks(), new Color(60, 150, 100));
            addCategory("Variable", createVariableBlocks(), new Color(120, 160, 90));

            // CENTER PANEL - Area script
            scriptArea = new JPanel();
            scriptArea.setLayout(null);
            scriptArea.setBackground(new Color(40, 45, 55));
            scriptArea.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(60, 70, 85), 2),
                    "Script Area",
                    0, 0, new Font("Arial", Font.BOLD, 12), new Color(221, 221, 221)
            ));
            scriptArea.setPreferredSize(new Dimension(800, 2000));

            setupScriptAreaDragDrop();
            setupScriptAreaContextMenu();

            JScrollPane centerScroll = new JScrollPane(scriptArea);
            centerScroll.getViewport().setBackground(new Color(40, 45, 55));

            // RIGHT PANEL - Sprite
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setPreferredSize(new Dimension(250, 0));
            rightPanel.setBackground(new Color(35, 40, 50));
            rightPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(60, 70, 85), 2),
                    "Sprites",
                    0, 0, new Font("Arial", Font.BOLD, 12), new Color(51, 28, 10)
            ));

            updateSpriteList();
            spriteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            spriteList.setBackground(new Color(45, 52, 65));
            spriteList.setForeground(new Color(220, 230, 240));
            spriteList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && spriteList.getSelectedIndex() >= 0) {
                    // Persist current blocks to previously selected sprite before switching
                    // Guard against initial selection wiping saved scripts when list first initializes
                    if (selectedSprite != null && !scriptBlocks.isEmpty()) {
                        selectedSprite.saveScriptBlocks(scriptBlocks, scriptArea);
                    }
                    selectedSprite = project.sprites.get(spriteList.getSelectedIndex());
                    loadScriptBlocksForSprite(selectedSprite);
                }
            });

            JScrollPane spriteScroll = new JScrollPane(spriteList);
            spriteScroll.getViewport().setBackground(new Color(45, 52, 65));
            rightPanel.add(spriteScroll, BorderLayout.CENTER);

            JPanel spriteButtons = new JPanel(new GridLayout(5, 1, 5, 5));
            spriteButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            spriteButtons.setBackground(new Color(35, 40, 50));

            JButton addSpriteBtn = createSideButton("➕ Nuovo", new Color(50, 120, 200));
            addSpriteBtn.addActionListener(e -> addNewSprite());

            JButton editSpriteBtn = createSideButton("✏️ Modifica", new Color(70, 140, 210));
            editSpriteBtn.addActionListener(e -> editSprite());

            JButton duplicateSpriteBtn = createSideButton("📋 Duplica", new Color(100, 150, 210));
            duplicateSpriteBtn.addActionListener(e -> duplicateSprite());

            JButton renameSpriteBtn = createSideButton("📝 Rinomina", new Color(120, 160, 210));
            renameSpriteBtn.addActionListener(e -> renameSprite());

            JButton deleteSpriteBtn = createSideButton("🗑️ Elimina", new Color(200, 70, 70));
            deleteSpriteBtn.addActionListener(e -> deleteSprite());

            spriteButtons.add(addSpriteBtn);
            spriteButtons.add(editSpriteBtn);
            spriteButtons.add(duplicateSpriteBtn);
            spriteButtons.add(renameSpriteBtn);
            spriteButtons.add(deleteSpriteBtn);

            rightPanel.add(spriteButtons, BorderLayout.SOUTH);

            mainPanel.add(categoryTabs, BorderLayout.WEST);
            mainPanel.add(centerScroll, BorderLayout.CENTER);
            mainPanel.add(rightPanel, BorderLayout.EAST);

            add(mainPanel, BorderLayout.CENTER);

            if (!project.sprites.isEmpty()) {
                // Trigger selection to load scripts via the listener without pre-setting selectedSprite
                spriteList.setSelectedIndex(0);
            }

            Timer colorUpdateTimer = new Timer(100, e -> updateBlockColors());
            colorUpdateTimer.start();

            setVisible(true);
        }

        JButton createToolbarButton(String text, Color bgColor) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.setBackground(bgColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        JButton createSideButton(String text, Color bgColor) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 11));
            btn.setBackground(bgColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        void setupScriptAreaDragDrop() {
            scriptArea.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) { // Solo click sinistro
                        Component comp = findDeepestComponent(scriptArea, e.getPoint());
                        // Determine the block under the click
                        VisualJavaScriptBlock targetBlock = findScriptBlockParent(comp);

                        // Suppress drag for interactive components, except when clicking a BOOLEAN block still in the script area
                        if (isInteractiveComponent(comp)) {
                            boolean allowDrag = targetBlock != null && targetBlock.shape == BlockShape.BOOLEAN && targetBlock.getParent() == scriptArea;
                            if (!allowDrag) {
                                suppressDragGesture = true;
                                return;
                            }
                        }

                        if (targetBlock != null) {
                            draggedBlock = targetBlock;
                            Point compLoc = getAbsoluteLocation(draggedBlock, scriptArea);
                            dragOffset.x = e.getX() - compLoc.x;
                            dragOffset.y = e.getY() - compLoc.y;

                            // Porta in primo piano
                            bringToFront(draggedBlock);

                            // Se il blocco è dentro un container, rimuovilo temporaneamente
                            if (draggedBlock.getParent() != scriptArea) {
                                Container parent = draggedBlock.getParent();
                                parent.remove(draggedBlock);
                                scriptArea.add(draggedBlock);
                                draggedBlock.setLocation(compLoc.x, compLoc.y);
                                parent.revalidate();
                                parent.repaint();
                            }
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (suppressDragGesture) {
                        suppressDragGesture = false;
                        return;
                    }
                    if (draggedBlock != null) {
                        // Controlla se rilasciato sulla palette (elimina)
                        if (isOverPalette(e.getLocationOnScreen())) {
                            removeBlock(draggedBlock);
                            draggedBlock = null;
                            return;
                        }

                        // Controlla se rilasciato su uno slot
                        boolean dropped = tryDropOnSlot(draggedBlock, e.getPoint());

                        if (!dropped) {
                            // Altrimenti cerca un blocco normale su cui attaccarsi
                            snapToNearestBlock(draggedBlock);
                        }

                        draggedBlock = null;
                        scriptArea.revalidate();
                        scriptArea.repaint();
                    }
                }
            });

            scriptArea.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (suppressDragGesture) return;
                    if (draggedBlock != null) {
                        int newX = e.getX() - dragOffset.x;
                        int newY = e.getY() - dragOffset.y;

                        draggedBlock.setLocation(newX, newY);

                        // Feedback visivo se sopra la palette
                        if (isOverPalette(e.getLocationOnScreen())) {
                            draggedBlock.setBackground(new Color(200, 70, 70));
                            categoryTabs.setBackground(new Color(200, 100, 100, 100));
                        } else {
                            draggedBlock.setBackground(draggedBlock.originalColor);
                            categoryTabs.setBackground(new Color(35, 40, 50));
                        }

                        scriptArea.revalidate();
                        scriptArea.repaint();
                    }
                }
            });
        }

        boolean isInteractiveComponent(Component comp) {
            if (comp == null) return false;
            return (comp instanceof JComboBox) ||
                    (comp instanceof JTextField) ||
                    (comp instanceof JButton) ||
                    (comp instanceof JList) ||
                    (comp instanceof JSlider) ||
                    (comp instanceof JCheckBox) ||
                    (comp instanceof JRadioButton);
        }

        void setupScriptAreaContextMenu() {
            scriptArea.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showContextMenu(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showContextMenu(e);
                    }
                }

                void showContextMenu(MouseEvent e) {
                    Component comp = findDeepestComponent(scriptArea, e.getPoint());
                    VisualJavaScriptBlock targetBlock = findScriptBlockParent(comp);

                    if (targetBlock != null) {
                        showBlockContextMenu(targetBlock, e.getX(), e.getY());
                    } else {
                        showAreaContextMenu(e.getX(), e.getY());
                    }
                }
            });
        }

        void showBlockContextMenu(VisualJavaScriptBlock block, int x, int y) {
            JPopupMenu menu = new JPopupMenu();
            menu.setBackground(new Color(45, 52, 65));

            JMenuItem deleteItem = createMenuItem("Delete");
            deleteItem.addActionListener(e -> removeBlock(block));

            JMenuItem duplicateItem = createMenuItem("Duplicate");
            duplicateItem.addActionListener(e -> duplicateBlock(block));

            JMenuItem copyItem = createMenuItem("Copy");
            copyItem.addActionListener(e -> copyBlock(block));

            JMenuItem pasteItem = createMenuItem("Paste");
            pasteItem.setEnabled(copiedBlock != null);
            pasteItem.addActionListener(e -> pasteBlock(x, y));

            menu.add(deleteItem);
            menu.add(duplicateItem);
            menu.addSeparator();
            menu.add(copyItem);
            menu.add(pasteItem);

            menu.show(scriptArea, x, y);
        }

        void showAreaContextMenu(int x, int y) {
            JPopupMenu menu = new JPopupMenu();
            menu.setBackground(new Color(45, 52, 65));

            // Clear current script area
            JMenuItem clearItem = createMenuItem("Pulisci area");
            clearItem.addActionListener(e -> clearScript());

            // Clear scripts across all sprites in the project
            JMenuItem clearAllItem = createMenuItem("Pulisci tutto");
            clearAllItem.addActionListener(e -> clearAllScripts());

            JMenuItem pasteItem = createMenuItem("Incolla");
            pasteItem.setEnabled(copiedBlock != null);
            pasteItem.addActionListener(e -> pasteBlock(x, y));

            menu.add(clearItem);
            menu.add(clearAllItem);
            menu.addSeparator();
            menu.add(pasteItem);
            menu.show(scriptArea, x, y);
        }

        JMenuItem createMenuItem(String text) {
            JMenuItem item = new JMenuItem(text);
            item.setFont(new Font("Arial", Font.PLAIN, 12));
            item.setBackground(new Color(45, 52, 65));
            item.setForeground(new Color(220, 230, 240));
            return item;
        }

        boolean isOverPalette(Point screenPoint) {
            Point palettePoint = categoryTabs.getLocationOnScreen();
            Rectangle paletteBounds = new Rectangle(
                    palettePoint.x,
                    palettePoint.y,
                    categoryTabs.getWidth(),
                    categoryTabs.getHeight()
            );
            return paletteBounds.contains(screenPoint);
        }

        void removeBlock(VisualJavaScriptBlock block) {
            scriptBlocks.remove(block);
            Container parent = block.getParent();
            if (parent != null) {
                parent.remove(block);
                parent.revalidate();
                parent.repaint();
            }
        }

        void duplicateBlock(VisualJavaScriptBlock block) {
            if (selectedSprite == null) return;

            VisualJavaScriptBlock newBlock = new VisualJavaScriptBlock(block.type, block.shape, block.originalColor, this);
            scriptBlocks.add(newBlock);

            // Copia parametri
            if (block.param1 != null && newBlock.param1 != null) {
                newBlock.param1.setText(block.param1.getText());
            }
            if (block.param2 != null && newBlock.param2 != null) {
                newBlock.param2.setText(block.param2.getText());
            }

            // Copia selezione sprite
            if (block.targetCombo != null && newBlock.targetCombo != null) {
                newBlock.targetCombo.setSelectedIndex(block.targetCombo.getSelectedIndex());
            }

            int x = block.getX() + 20;
            int y = block.getY() + 20;
            newBlock.setBounds(x, y, newBlock.getPreferredSize().width, newBlock.getPreferredSize().height);

            scriptArea.add(newBlock);
            scriptArea.revalidate();
            scriptArea.repaint();
        }

        void copyBlock(VisualJavaScriptBlock block) {
            copiedBlock = block;
            JOptionPane.showMessageDialog(this, "Block copied!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        void pasteBlock(int x, int y) {
            if (copiedBlock == null || selectedSprite == null) return;

            VisualJavaScriptBlock newBlock = new VisualJavaScriptBlock(copiedBlock.type, copiedBlock.shape, copiedBlock.originalColor, this);
            scriptBlocks.add(newBlock);

            // Copia parametri
            if (copiedBlock.param1 != null && newBlock.param1 != null) {
                newBlock.param1.setText(copiedBlock.param1.getText());
            }
            if (copiedBlock.param2 != null && newBlock.param2 != null) {
                newBlock.param2.setText(copiedBlock.param2.getText());
            }

            // Copia selezione sprite
            if (copiedBlock.targetCombo != null && newBlock.targetCombo != null) {
                newBlock.targetCombo.setSelectedIndex(copiedBlock.targetCombo.getSelectedIndex());
            }

            newBlock.setBounds(x, y, newBlock.getPreferredSize().width, newBlock.getPreferredSize().height);

            scriptArea.add(newBlock);
            scriptArea.revalidate();
            scriptArea.repaint();
        }

        VisualJavaScriptBlock findScriptBlockParent(Component comp) {
            if (comp instanceof VisualJavaScriptBlock) {
                return (VisualJavaScriptBlock) comp;
            }

            // Traverse up the component hierarchy to find a ScriptBlock parent
            Component current = comp;
            while (current != null && current != scriptArea) {
                if (current instanceof VisualJavaScriptBlock) {
                    return (VisualJavaScriptBlock) current;
                }
                current = current.getParent();
            }

            return null;
        }

        Component findDeepestComponent(Container container, Point point) {
            Component[] components = container.getComponents();
            for (int i = components.length - 1; i >= 0; i--) {
                Component comp = components[i];
                Point relPoint = new Point(point.x - comp.getX(), point.y - comp.getY());

                if (comp.contains(relPoint)) {
                    if (comp instanceof Container) {
                        Component deeper = findDeepestComponent((Container) comp, relPoint);
                        if (deeper != null) return deeper;
                    }
                    return comp;
                }
            }
            return null;
        }

        Point getAbsoluteLocation(Component comp, Container relativeTo) {
            Point loc = new Point(0, 0);
            while (comp != null && comp != relativeTo) {
                loc.x += comp.getX();
                loc.y += comp.getY();
                comp = comp.getParent();
            }
            return loc;
        }

        void bringToFront(Component comp) {
            Container parent = comp.getParent();
            if (parent != null) {
                parent.setComponentZOrder(comp, 0);
            }
        }

        boolean tryDropOnSlot(VisualJavaScriptBlock block, Point dropPoint) {
            for (VisualJavaScriptBlock targetBlock : scriptBlocks) {
                if (targetBlock == block) continue;

                if (targetBlock.conditionSlot != null && block.shape == BlockShape.BOOLEAN) {
                    Rectangle slotBounds = getAbsoluteBounds(targetBlock.conditionSlot, scriptArea);
                    if (slotBounds.contains(dropPoint)) {
                        targetBlock.setConditionBlock(block);
                        return true;
                    }
                }

                if (targetBlock.innerBlocksPanel != null &&
                        (block.shape == BlockShape.NORMAL || block.shape == BlockShape.CONTROL)) {
                    Rectangle slotBounds = getAbsoluteBounds(targetBlock.innerBlocksPanel, scriptArea);
                    if (slotBounds.contains(dropPoint)) {
                        targetBlock.addInnerBlock(block);
                        return true;
                    }
                }
            }
            return false;
        }

        Rectangle getAbsoluteBounds(Component comp, Container relativeTo) {
            Point loc = getAbsoluteLocation(comp, relativeTo);
            return new Rectangle(loc.x, loc.y, comp.getWidth(), comp.getHeight());
        }

        void snapToNearestBlock(VisualJavaScriptBlock block) {
            int snapDistance = 20;
            VisualJavaScriptBlock bestMatch = null;
            int minDistance = Integer.MAX_VALUE;

            for (VisualJavaScriptBlock other : scriptBlocks) {
                if (other == block || other.getParent() != scriptArea) continue;

                int distance = Math.abs(block.getY() - (other.getY() + other.getHeight()));
                if (distance < snapDistance && Math.abs(block.getX() - other.getX()) < 50) {
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestMatch = other;
                    }
                }
            }

            if (bestMatch != null) {
                block.setLocation(bestMatch.getX(), bestMatch.getY() + bestMatch.getHeight() + 2);
                block.attachedTo = bestMatch;
                pushBlocksDown(block);
            } else {
                block.attachedTo = null;
            }
        }

        void pushBlocksDown(VisualJavaScriptBlock movedBlock) {
            if (movedBlock.getParent() != scriptArea) return;

            Rectangle movedBounds = movedBlock.getBounds();
            boolean changed = true;
            int iterations = 0;

            while (changed && iterations < 10) {
                changed = false;
                iterations++;

                for (VisualJavaScriptBlock block : scriptBlocks) {
                    if (block == movedBlock || block.getParent() != scriptArea) continue;

                    Rectangle blockBounds = block.getBounds();

                    if (blockBounds.y < movedBounds.y + movedBounds.height &&
                            blockBounds.y + blockBounds.height > movedBounds.y &&
                            Math.abs(blockBounds.x - movedBounds.x) < 100) {

                        int overlap = (movedBounds.y + movedBounds.height) - blockBounds.y + 5;

                        if (overlap > 0) {
                            block.setLocation(blockBounds.x, blockBounds.y + overlap);
                            changed = true;
                            movedBounds = movedBounds.union(block.getBounds());
                        }
                    }
                }
            }

            scriptArea.revalidate();
            scriptArea.repaint();
        }

        void updateBlockColors() {
            for (VisualJavaScriptBlock block : scriptBlocks) {
                if (block.getParent() == scriptArea) {
                    boolean isConnected = isConnectedToEvent(block);
                    block.updateConnectionStatus(isConnected);
                }
            }
        }

        boolean isConnectedToEvent(VisualJavaScriptBlock block) {
            if (block.type.equals("onstart")) {
                return true;
            }

            VisualJavaScriptBlock current = block;
            while (current.attachedTo != null) {
                current = current.attachedTo;
                if (current.type.equals("onstart")) {
                    return true;
                }
            }

            return false;
        }

        void addCategory(String name, JPanel blocksPanel, Color color) {
            JScrollPane scroll = new JScrollPane(blocksPanel);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getViewport().setBackground(new Color(40, 45, 55));
            categoryTabs.addTab(name, scroll);
        }

        JPanel createMovementBlocks() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(40, 45, 55));

            addBlockButton(panel, "move", BlockShape.NORMAL, new Color(45, 100, 180), "move [10] passi");
            addBlockButton(panel, "setx", BlockShape.NORMAL, new Color(45, 100, 180), "set x to [0]");
            addBlockButton(panel, "sety", BlockShape.NORMAL, new Color(45, 100, 180), "set y to [0]");
            addBlockButton(panel, "setxy", BlockShape.NORMAL, new Color(45, 100, 180), "set x [0] y [0]");
            addBlockButton(panel, "changex", BlockShape.NORMAL, new Color(45, 100, 180), "change x by [10]");
            addBlockButton(panel, "changey", BlockShape.NORMAL, new Color(45, 100, 180), "change y by [10]");
            addBlockButton(panel, "rotate", BlockShape.NORMAL, new Color(45, 100, 180), "rotate [15] degrees");
            addBlockButton(panel, "setrotation", BlockShape.NORMAL, new Color(45, 100, 180), "set rotation to [0]");
            addBlockButton(panel, "goto", BlockShape.NORMAL, new Color(45, 100, 180), "go to [sprite]");
            addBlockButton(panel, "pointtowards", BlockShape.NORMAL, new Color(45, 100, 180), "point towards [sprite]");

            return panel;
        }

        JPanel createAppearanceBlocks() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(40, 45, 55));

            addBlockButton(panel, "show", BlockShape.NORMAL, new Color(100, 70, 180), "show sprite");
            addBlockButton(panel, "hide", BlockShape.NORMAL, new Color(100, 70, 180), "hide sprite");
            addBlockButton(panel, "setsize", BlockShape.NORMAL, new Color(100, 70, 180), "set size to [100]%");
            addBlockButton(panel, "changesize", BlockShape.NORMAL, new Color(100, 70, 180), "change size by [10]");

            return panel;
        }

        JPanel createEventBlocks() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(40, 45, 55));

            addBlockButton(panel, "onstart", BlockShape.HAT, new Color(200, 140, 50), "when program starts");

            addBlockButton(panel, "wait", BlockShape.NORMAL, new Color(200, 140, 50), "wait [1] seconds");

            return panel;
        }

        JPanel createControlBlocks() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(40, 45, 55));

            addBlockButton(panel, "forever", BlockShape.CONTROL, new Color(200, 140, 50), "forever");
            addBlockButton(panel, "repeat", BlockShape.CONTROL, new Color(200, 140, 50), "repeat [10]");
            addBlockButton(panel, "repeatuntil", BlockShape.CONTROL, new Color(200, 140, 50), "repeat until <condition>");
            addBlockButton(panel, "if", BlockShape.CONTROL, new Color(200, 140, 50), "if <condition>");


            return panel;
        }

        JPanel createSensorBlocks() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(40, 45, 55));

            addBlockButton(panel, "keypressed", BlockShape.BOOLEAN, new Color(50, 160, 180), "key [space] pressed?");
            addBlockButton(panel, "istouchingsprites", BlockShape.BOOLEAN, new Color(50, 160, 180), "is [sprite] touching [sprite]?");
            addBlockButton(panel, "istouchingcolor", BlockShape.BOOLEAN, new Color(50, 160, 180), "is [sprite] touching [color]?");
            // Legacy sensing blocks removed to avoid confusion
            addBlockButton(panel, "xposition", BlockShape.REPORTER, new Color(50, 160, 180), "x position");
            addBlockButton(panel, "yposition", BlockShape.REPORTER, new Color(50, 160, 180), "y position");

            return panel;
        }

        JPanel createOperatorBlocks() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(40, 45, 55));

            addBlockButton(panel, "add", BlockShape.REPORTER, new Color(60, 150, 100), "[0] + [0]");
            addBlockButton(panel, "subtract", BlockShape.REPORTER, new Color(60, 150, 100), "[0] - [0]");
            addBlockButton(panel, "multiply", BlockShape.REPORTER, new Color(60, 150, 100), "[0] * [0]");
            addBlockButton(panel, "divide", BlockShape.REPORTER, new Color(60, 150, 100), "[0] / [0]");
            addBlockButton(panel, "modulo", BlockShape.REPORTER, new Color(60, 150, 100), "[0] % [0]");
            addBlockButton(panel, "equal", BlockShape.BOOLEAN, new Color(60, 150, 100), "[0] = [0]");
            addBlockButton(panel, "greater", BlockShape.BOOLEAN, new Color(60, 150, 100), "[0] > [0]");
            addBlockButton(panel, "less", BlockShape.BOOLEAN, new Color(60, 150, 100), "[0] < [0]");

            return panel;
        }

        // Scene category with display and order controls
        JPanel createSceneBlocks() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(40, 45, 55));

            addBlockButton(panel, "printmessage", BlockShape.NORMAL, new Color(220, 120, 120), "print message [text] at [x] [y] [color]");
            addBlockButton(panel, "setbackground", BlockShape.NORMAL, new Color(220, 120, 120), "set background color to [color]");
            addBlockButton(panel, "hide", BlockShape.NORMAL, new Color(220, 120, 120), "hide [sprite]");
            addBlockButton(panel, "show", BlockShape.NORMAL, new Color(220, 120, 120), "show [sprite]");
            addBlockButton(panel, "bringtofront", BlockShape.NORMAL, new Color(220, 120, 120), "put [sprite] to the front");
            addBlockButton(panel, "bringtoback", BlockShape.NORMAL, new Color(220, 120, 120), "put [sprite] to the back");
            // Optional follow camera not implemented yet for stability
            // addBlockButton(panel, "screenfollow", BlockShape.NORMAL, new Color(220, 120, 120), "screen follow [sprite]");

            return panel;
        }

        // Variable category: create, get, set, change
        JPanel createVariableBlocks() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(40, 45, 55));

            JButton newVarBtn = new JButton("➕ New Variable");
            newVarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            newVarBtn.addActionListener(e -> {
                String name = JOptionPane.showInputDialog(this, "Variable name:", "Create Variable", JOptionPane.PLAIN_MESSAGE);
                if (name != null && !name.trim().isEmpty()) {
                    if (!project.variables.containsKey(name)) {
                        project.variables.put(name, 0);
                        refreshVariableCombos();
                    } else {
                        JOptionPane.showMessageDialog(this, "Variable already exists.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
            panel.add(newVarBtn);
            panel.add(Box.createRigidArea(new Dimension(0, 8)));

            addBlockButton(panel, "variable", BlockShape.REPORTER, new Color(120, 160, 90), "[variable]");
            addBlockButton(panel, "setvariable", BlockShape.NORMAL, new Color(120, 160, 90), "set [variable] to [int]");
            addBlockButton(panel, "changevariable", BlockShape.NORMAL, new Color(120, 160, 90), "change [variable] by [int]");

            return panel;
        }

        void addBlockButton(JPanel panel, String type, BlockShape shape, Color color, String label) {
            JPanel blockBtn = new JPanel();
            blockBtn.setBackground(color);
            blockBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.darker(), 2),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            blockBtn.setMaximumSize(new Dimension(250, 45));
            blockBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel lbl = new JLabel(label);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            blockBtn.add(lbl);

            blockBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedSprite == null) {
                        JOptionPane.showMessageDialog(VisualJavaEditor.this, "Select a sprite!", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    VisualJavaScriptBlock block = new VisualJavaScriptBlock(type, shape, color, VisualJavaEditor.this);
                    scriptBlocks.add(block);

                    int x = 20 + (scriptBlocks.size() % 3) * 200;
                    int y = 20 + (scriptBlocks.size() / 3) * 80;
                    block.setBounds(x, y, block.getPreferredSize().width, block.getPreferredSize().height);

                    scriptArea.add(block);
                    scriptArea.revalidate();
                    scriptArea.repaint();
                }
            });

            panel.add(blockBtn);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        void clearScript() {
            scriptBlocks.clear();
            scriptArea.removeAll();
            scriptArea.revalidate();
            scriptArea.repaint();
        }

        // Clear all scripts for all sprites in the project and the current UI area
        void clearAllScripts() {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Eliminare tutti i blocchi di tutti gli sprite?",
                    "Conferma",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            // Clear persisted scripts across all sprites
            if (project != null && project.sprites != null) {
                for (VisualJavaSprite sprite : project.sprites) {
                    if (sprite != null && sprite.scripts != null) {
                        sprite.scripts.clear();
                    }
                }
            }

            // Clear the visible script area for the current selection
            clearScript();
        }

        void executeScript() {
            if (selectedSprite == null) {
                JOptionPane.showMessageDialog(this, "Seleziona uno sprite!", "Attenzione", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedSprite.saveScriptBlocks(scriptBlocks, scriptArea);
            VisualJavaProject snapshot = deepCopyProject(project);
            new VisualJavaEditor.VisualJavaGameWindow(snapshot, projectName);
        }

        // Evaluate a math/reporter block using the currently selected sprite context
        public double evaluateMathBlock(VisualJavaScriptBlock mathBlock) {
            if (mathBlock == null) return 0;
            switch (mathBlock.type) {
                case "add":
                    return safeParse(mathBlock.getParam1()) + safeParse(mathBlock.getParam2());
                case "subtract":
                    return safeParse(mathBlock.getParam1()) - safeParse(mathBlock.getParam2());
                case "multiply":
                    return safeParse(mathBlock.getParam1()) * safeParse(mathBlock.getParam2());
                case "divide": {
                    double v2 = safeParse(mathBlock.getParam2());
                    return v2 != 0 ? safeParse(mathBlock.getParam1()) / v2 : 0;
                }
                case "modulo": {
                    double v2 = safeParse(mathBlock.getParam2());
                    return v2 != 0 ? safeParse(mathBlock.getParam1()) % v2 : 0;
                }
                case "xposition": {
                    VisualJavaSprite s = mathBlock.getTargetSprite();
                    if (s == null) s = selectedSprite;
                    return s != null ? s.x : 0;
                }
                case "yposition": {
                    VisualJavaSprite s = mathBlock.getTargetSprite();
                    if (s == null) s = selectedSprite;
                    return s != null ? s.y : 0;
                }
                case "variable": {
                    String varName = mathBlock.getCombo1Value();
                    if (project != null) {
                        if (project.variables == null) project.variables = new HashMap<>();
                        Integer v = project.variables.get(varName);
                        return v != null ? v : 0;
                    }
                    return 0;
                }
                default:
                    return 0;
            }
        }

        private double safeParse(String s) {
            try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
        }

        void updateSpriteList() {
            spriteListModel.clear();
            for (VisualJavaSprite sprite : project.sprites) spriteListModel.addElement(sprite.name);
        }

        void refreshVariableCombos() {
            if (project == null || project.variables == null) return;
            java.util.List<String> names = new java.util.ArrayList<>(project.variables.keySet());
            for (VisualJavaScriptBlock b : scriptBlocks) {
                if (b != null) {
                    if ("variable".equals(b.type) || "setvariable".equals(b.type) || "changevariable".equals(b.type)) {
                        if (b.combo1 != null) {
                            String current = (String) b.combo1.getSelectedItem();
                            b.combo1.removeAllItems();
                            for (String n : names) b.combo1.addItem(n);
                            if (current != null && project.variables.containsKey(current)) {
                                b.combo1.setSelectedItem(current);
                            }
                        }
                    }
                }
            }
            scriptArea.revalidate();
            scriptArea.repaint();
        }

        void addNewSprite() {
            String name = JOptionPane.showInputDialog(this, "Nome del nuovo sprite:");
            if (name != null && !name.trim().isEmpty()) {
                VisualJavaSprite newSprite = new VisualJavaSprite(name.trim(), 200, 200);
                newSprite.createDefaultSquare();
                project.sprites.add(newSprite);
                updateSpriteList();
            }
        }

        void editSprite() {
            if (selectedSprite != null) new VisualJavaPaintEditor(selectedSprite, this);
            else JOptionPane.showMessageDialog(this, "Seleziona uno sprite!", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }

        void duplicateSprite() {
            if (selectedSprite != null) {
                VisualJavaSprite copy = selectedSprite.duplicate(selectedSprite.name + " copia");
                project.sprites.add(copy);
                updateSpriteList();
            }
        }

        void deleteSprite() {
            if (selectedSprite != null) {
                if (project.sprites.size() <= 1) {
                    JOptionPane.showMessageDialog(this, "Deve esserci almeno uno sprite!", "Attenzione", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                project.sprites.remove(selectedSprite);
                selectedSprite = project.sprites.get(0);
                updateSpriteList();
                spriteList.setSelectedIndex(0);
            }
        }

        void renameSprite() {
            if (selectedSprite == null) {
                JOptionPane.showMessageDialog(this, "Seleziona uno sprite!", "Attenzione", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String oldName = selectedSprite.name;
            String newName = JOptionPane.showInputDialog(this, "Nuovo nome per lo sprite:", oldName);
            if (newName == null) return;
            newName = newName.trim();
            if (newName.isEmpty() || newName.equals(oldName)) return;
            for (VisualJavaSprite s : project.sprites) {
                if (s.name.equals(newName)) {
                    JOptionPane.showMessageDialog(this, "Esiste già uno sprite con questo nome!", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            selectedSprite.name = newName;
            updateSpriteList();
            spriteList.setSelectedValue(newName, true);
            refreshSpriteCombos(oldName, newName);
        }

        void refreshSpriteCombos(String oldName, String newName) {
            String[] names = new String[project.sprites.size()];
            for (int i = 0; i < project.sprites.size(); i++) names[i] = project.sprites.get(i).name;

            for (VisualJavaScriptBlock block : scriptBlocks) {
                if (block.targetCombo != null) {
                    block.targetCombo.setModel(new DefaultComboBoxModel<>(names));
                    Object sel = block.targetCombo.getSelectedItem();
                    if (sel != null && sel.equals(oldName)) block.targetCombo.setSelectedItem(newName);
                }

                if (block.combo1 != null && ("istouchingsprites".equals(block.type) || "istouchingcolor".equals(block.type) || "goto".equals(block.type) || "pointtowards".equals(block.type))) {
                    block.combo1.setModel(new DefaultComboBoxModel<>(names));
                    Object sel = block.combo1.getSelectedItem();
                    if (sel != null && sel.equals(oldName)) block.combo1.setSelectedItem(newName);
                }

                if (block.mathInput1 != null && block.mathInput1.hasMathBlock() && block.mathInput1.getMathBlock() != null) {
                    VisualJavaScriptBlock m1 = block.mathInput1.getMathBlock();
                    if (m1.targetCombo != null) {
                        m1.targetCombo.setModel(new DefaultComboBoxModel<>(names));
                        Object sel = m1.targetCombo.getSelectedItem();
                        if (sel != null && sel.equals(oldName)) m1.targetCombo.setSelectedItem(newName);
                    }
                }

                if (block.mathInput2 != null && block.mathInput2.hasMathBlock() && block.mathInput2.getMathBlock() != null) {
                    VisualJavaScriptBlock m2 = block.mathInput2.getMathBlock();
                    if (m2.targetCombo != null) {
                        m2.targetCombo.setModel(new DefaultComboBoxModel<>(names));
                        Object sel = m2.targetCombo.getSelectedItem();
                        if (sel != null && sel.equals(oldName)) m2.targetCombo.setSelectedItem(newName);
                    }
                }
            }
        }

        void saveProject() {
            // Persist current script blocks to the selected sprite
            if (selectedSprite != null) {
                selectedSprite.saveScriptBlocks(scriptBlocks, scriptArea);
            }
            // Save the project using serialization
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(projectFile))) {
                oos.writeObject(project);
                JOptionPane.showMessageDialog(this, "Progetto salvato!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore nel salvataggio: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }

        void loadProject() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(projectFile))) {
                project = (VisualJavaProject) ois.readObject();

                // Load script blocks for the selected sprite
                if (selectedSprite != null) {
                    loadScriptBlocksForSprite(selectedSprite);
                }
            } catch (Exception e) {
                project = new VisualJavaProject();
                VisualJavaSprite defaultSprite = new VisualJavaSprite("Sprite1", 400, 300);
                defaultSprite.createDefaultSquare();
                project.sprites.add(defaultSprite);
            }
        }

        VisualJavaProject deepCopyProject(VisualJavaProject src) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(src);
                oos.flush();
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                return (VisualJavaProject) ois.readObject();
            } catch (Exception ex) {
                return src;
            }
        }

        void loadScriptBlocksForSprite(VisualJavaSprite sprite) {
            // Clear existing blocks
            for (VisualJavaScriptBlock block : scriptBlocks) {
                scriptArea.remove(block);
            }
            scriptBlocks.clear();

            // Load blocks for this sprite
            ArrayList<VisualJavaScriptBlock> loadedBlocks = sprite.loadScriptBlocks(this);
            for (VisualJavaScriptBlock block : loadedBlocks) {
                scriptBlocks.add(block);
                scriptArea.add(block);
            }
            // After loading, rebuild vertical attachments from positions and refresh colors
            rebuildAttachmentsAfterLoad();

            scriptArea.revalidate();
            scriptArea.repaint();
        }

        // Rebuild vertical attachments for top-level blocks based on their positions
        void rebuildAttachmentsAfterLoad() {
            for (VisualJavaScriptBlock b : scriptBlocks) {
                if (b.getParent() == scriptArea && b.shape == BlockShape.NORMAL) {
                    b.attachedTo = null;
                }
            }

            // Run multiple passes to stabilize stacking
            for (int pass = 0; pass < 2; pass++) {
                for (VisualJavaScriptBlock b : scriptBlocks) {
                    if (b.getParent() == scriptArea && b.shape == BlockShape.NORMAL) {
                        snapToNearestBlock(b);
                    }
                }
            }

            updateBlockColors();
        }

        // =================== FINESTRA GAME =====================
        class VisualJavaGameWindow extends JFrame {
            VisualJavaProject project;
            VisualJavaGamePanel gamePanel;
            ThreadLocal<VisualJavaSprite> activeSpriteTL = new ThreadLocal<>();
            // Use a thread-safe Set because key events (EDT) and block execution (worker threads)
            // access this concurrently.
            Set<String> pressedKeys = java.util.Collections.synchronizedSet(new HashSet<>());
            // Overlay state for Scene.printmessage
            volatile String printedMessage = null;
            volatile int printedX = 0;
            volatile int printedY = 0;
            volatile Color printedColor = Color.BLACK;
            volatile boolean running = true;

            VisualJavaGameWindow(VisualJavaProject project, String title) {
                this.project = project;

                setTitle("Run - " + title);
                setSize(800, 600);
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                setLocationRelativeTo(null);

                gamePanel = new VisualJavaGamePanel();
                add(gamePanel);

                setVisible(true);

                // Ensure both window and panel can capture keyboard focus reliably
                setFocusable(true);
                requestFocusInWindow();
                gamePanel.setFocusable(true);
                gamePanel.requestFocusInWindow();

                addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        running = false;
                    }
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        running = false;
                    }
                });

                // Add key listener for key pressed detection on the window
                addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        pressedKeys.add(getKeyString(e.getKeyCode()));
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        pressedKeys.remove(getKeyString(e.getKeyCode()));
                    }
                });

                // Also listen on the game panel to handle cases where focus settles on the panel
                gamePanel.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        pressedKeys.add(getKeyString(e.getKeyCode()));
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        pressedKeys.remove(getKeyString(e.getKeyCode()));
                    }
                });

                new Thread(() -> executeBlocks()).start();
            }

            VisualJavaSprite resolveTarget(VisualJavaScriptBlock block, VisualJavaSprite defaultTarget) {
                try {
                    if (block != null && block.targetCombo != null) {
                        Object sel = block.targetCombo.getSelectedItem();
                        if (sel != null) {
                            VisualJavaSprite s = findSpriteByName(sel.toString());
                            if (s != null) return s;
                        }
                    }
                } catch (Exception ignored) {}
                return defaultTarget;
            }

            void executeBlocks() {
                // Collect hats for each sprite and execute with that sprite as default target
                for (VisualJavaSprite sprite : project.sprites) {
                    ArrayList<VisualJavaScriptBlock> spriteBlocks = sprite.loadScriptBlocks(VisualJavaEditor.this);
                    rebuildVerticalAttachmentsRuntime(spriteBlocks);
                    ArrayList<VisualJavaScriptBlock> hats = new ArrayList<>();
                    for (VisualJavaScriptBlock b : spriteBlocks) {
                        if ("onstart".equals(b.type)) {
                            hats.add(b);
                        }
                    }
                    for (VisualJavaScriptBlock hat : hats) {
                        new Thread(() -> {
                            try {
                                activeSpriteTL.set(sprite);
                                executeChainFromHat(hat, spriteBlocks);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                activeSpriteTL.remove();
                            }
                        }).start();
                    }
                }
            }

            // Execute the vertical chain of blocks attached below a given event hat
            void executeChainFromHat(VisualJavaScriptBlock hat, java.util.List<VisualJavaScriptBlock> blocksList) throws Exception {
                // Hats themselves have no action; walk down via attachedTo links
                VisualJavaScriptBlock current = hat;
                while (true) {
                    if (!running) break;
                    VisualJavaScriptBlock next = findChildBlock(current, blocksList);
                    if (next == null) break;
                    executeBlock(next);
                    // Advance down the chain
                    current = next;
                }
            }

            // Find the direct child block attached to the given parent block
            VisualJavaScriptBlock findChildBlock(VisualJavaScriptBlock parent, java.util.List<VisualJavaScriptBlock> blocksList) {
                VisualJavaScriptBlock candidate = null;
                int minY = Integer.MAX_VALUE;
                for (VisualJavaScriptBlock b : blocksList) {
                    if (b.attachedTo == parent) {
                        int y = b.getY();
                        if (y < minY) {
                            minY = y;
                            candidate = b;
                        }
                    }
                }
                return candidate;
            }

            void rebuildVerticalAttachmentsRuntime(java.util.List<VisualJavaScriptBlock> list) {
                for (VisualJavaScriptBlock b : list) b.attachedTo = null;
                for (VisualJavaScriptBlock b : list) {
                    if ("onstart".equals(b.type)) continue;
                    VisualJavaScriptBlock best = null;
                    int bestGap = Integer.MAX_VALUE;
                    for (VisualJavaScriptBlock other : list) {
                        if (other == b) continue;
                        int gap = b.getY() - (other.getY() + other.getHeight());
                        int dx = Math.abs(b.getX() - other.getX());
                        if (gap >= 0 && gap < 25 && dx < 60) {
                            if (gap < bestGap) {
                                bestGap = gap;
                                best = other;
                            }
                        }
                    }
                    if (best != null) b.attachedTo = best;
                }
            }

            void executeBlock(VisualJavaScriptBlock block) throws Exception {
                VisualJavaSprite target = activeSpriteTL.get();
                if (target == null && !project.sprites.isEmpty()) target = project.sprites.get(0);

                switch (block.type) {
                    case "move":
                        VisualJavaSprite moveTarget = resolveTarget(block, target);
                        int steps = (int)Math.round(evaluateMathParameter(block.getParam1()));
                        double rad = Math.toRadians(moveTarget.rotation);
                        int dx = (int)Math.round(steps * Math.cos(rad));
                        int dy = (int)Math.round(steps * Math.sin(rad));
                        moveTarget.x += dx;
                        moveTarget.y += dy;
                        break;

                    case "setx":
                        VisualJavaSprite setXTarget = resolveTarget(block, target);
                        setXTarget.x = (int)Math.round(evaluateMathParameter(block.getParam1()));
                        break;

                    case "sety":
                        VisualJavaSprite setYTarget = resolveTarget(block, target);
                        setYTarget.y = (int)Math.round(evaluateMathParameter(block.getParam1()));
                        break;

                    case "changex":
                        VisualJavaSprite changeXTarget = resolveTarget(block, target);
                        changeXTarget.x += (int)Math.round(evaluateMathParameter(block.getParam1()));
                        break;

                    case "changey":
                        VisualJavaSprite changeYTarget = resolveTarget(block, target);
                        changeYTarget.y += (int)Math.round(evaluateMathParameter(block.getParam1()));
                        break;

                    case "rotate":
                        VisualJavaSprite rotateTarget = resolveTarget(block, target);
                        rotateTarget.rotation += (int)Math.round(evaluateMathParameter(block.getParam1()));
                        break;

                    case "setrotation":
                        VisualJavaSprite setRotTarget = resolveTarget(block, target);
                        setRotTarget.rotation = (int)Math.round(evaluateMathParameter(block.getParam1()));
                        break;

                    case "pointtowards":
                        VisualJavaSprite subject = resolveTarget(block, target);
                        String towardName = block.getParam1();
                        VisualJavaSprite toward = findSpriteByName(towardName);
                        if (toward != null) {
                            double angle = Math.toDegrees(Math.atan2(toward.y - subject.y, toward.x - subject.x));
                            subject.rotation = (int)Math.round(angle);
                        }
                        break;

                    case "goto":
                        String targetSpriteName = block.getParam1();
                        VisualJavaSprite targetSprite = findSpriteByName(targetSpriteName);
                        if (targetSprite != null) {
                            target.x = targetSprite.x;
                            target.y = targetSprite.y;
                        }
                        break;

                    case "setxy":
                        VisualJavaSprite setXYTarget = resolveTarget(block, target);
                        int newX = (int)Math.round(evaluateMathParameter(block.getParam1()));
                        int newY = (int)Math.round(evaluateMathParameter(block.getParam2()));
                        setXYTarget.x = newX;
                        setXYTarget.y = newY;
                        break;

                    case "show":
                        VisualJavaSprite showTarget = resolveTarget(block, target);
                        showTarget.visible = true;
                        break;

                    case "hide":
                        VisualJavaSprite hideTarget = resolveTarget(block, target);
                        hideTarget.visible = false;
                        break;

                    case "setsize":
                        VisualJavaSprite sizeTarget = resolveTarget(block, target);
                        sizeTarget.size = (int)Math.round(evaluateMathParameter(block.getParam1()));
                        break;

                    case "changesize":
                        VisualJavaSprite changeSizeTarget = resolveTarget(block, target);
                        changeSizeTarget.size += (int)Math.round(evaluateMathParameter(block.getParam1()));
                        break;


                    case "bringtofront":
                        VisualJavaSprite frontTarget = resolveTarget(block, target);
                        int idxFront = project.sprites.indexOf(frontTarget);
                        if (idxFront >= 0) {
                            project.sprites.remove(idxFront);
                            project.sprites.add(frontTarget); // draw last => front
                        }
                        break;

                    case "bringtoback":
                        VisualJavaSprite backTarget = resolveTarget(block, target);
                        int idxBack = project.sprites.indexOf(backTarget);
                        if (idxBack >= 0) {
                            project.sprites.remove(idxBack);
                            project.sprites.add(0, backTarget); // draw first => back
                        }
                        break;



                    case "wait":
                        // Allow fractional seconds (e.g., 0.1), support comma or dot decimals, skip non-positive
                        double waitSeconds = evaluateMathParameter(block.getParam1());
                        if (waitSeconds > 0) {
                            long millis = (long) Math.round(waitSeconds * 1000.0);
                            Thread.sleep(millis);
                        }
                        break;

                    case "add":
                        // Math blocks return values, they don't execute actions
                        // These will be handled when evaluating parameters
                        break;

                    case "subtract":
                        // Math blocks return values, they don't execute actions
                        break;

                    case "multiply":
                        // Math blocks return values, they don't execute actions
                        break;

                    case "divide":
                        // Math blocks return values, they don't execute actions
                        break;

                    case "printmessage": {
                        String msg = block.param1 != null ? block.param1.getText() : "";
                        int x = (int)Math.round(evaluateMathParameter(block.getParam1()));
                        int y = (int)Math.round(evaluateMathParameter(block.getParam2()));
                        Color col = block.getSelectedColor();
                        printedMessage = msg;
                        printedX = x;
                        printedY = y;
                        printedColor = col != null ? col : Color.BLACK;
                        break;
                    }

                    case "setbackground": {
                        Color col = block.getSelectedColor();
                        if (col != null) {
                            project.backgroundColor = col;
                        }
                        break;
                    }

                    case "setvariable": {
                        String name = block.getCombo1Value();
                        double value = evaluateMathParameter(block.getParam1());
                        if (project.variables == null) project.variables = new HashMap<>();
                        project.variables.put(name, (int)Math.round(value));
                        break;
                    }

                    case "changevariable": {
                        String name = block.getCombo1Value();
                        double delta = evaluateMathParameter(block.getParam1());
                        if (project.variables == null) project.variables = new HashMap<>();
                        int current = project.variables.getOrDefault(name, 0);
                        project.variables.put(name, current + (int)Math.round(delta));
                        break;
                    }

                    case "if":
                        if (block.conditionBlock != null) {
                            int guard = 0;
                            while (running && evaluateCondition(block.conditionBlock)) {
                                executeInnerBlocks(block);
                                if (++guard >= 32) break;
                            }
                        }
                        break;

                    case "repeat":
                        int times = (int)Math.max(0, Math.round(evaluateMathParameter(block.getParam1())));
                        for (int i = 0; i < times && running; i++) {
                            executeInnerBlocks(block);
                        }
                        break;

                    case "repeatuntil":
                        // Execute inner blocks repeatedly until the condition becomes true
                        while (running) {
                            if (block.conditionBlock != null && evaluateCondition(block.conditionBlock)) {
                                break;
                            }
                            executeInnerBlocks(block);
                            Thread.sleep(10); // avoid busy spin
                        }
                        break;

                    case "forever":
                        while (running) {
                            executeInnerBlocks(block);
                            Thread.sleep(10); // avoid busy spin
                        }
                }

                gamePanel.repaint();
            }

            void executeInnerBlocks(VisualJavaScriptBlock parentBlock) throws Exception {
                if (!running) return;
                if (parentBlock.innerBlocksPanel != null) {
                    Component[] components = parentBlock.innerBlocksPanel.getComponents();
                    for (Component comp : components) {
                        if (!running) break;
                        if (comp instanceof VisualJavaScriptBlock) {
                            executeBlock((VisualJavaScriptBlock) comp);
                        }
                    }
                }
            }

            double evaluateMathBlock(VisualJavaScriptBlock mathBlock) {
                if (mathBlock == null) return 0;

                switch (mathBlock.type) {
                    case "add":
                        try {
                            double val1 = evaluateMathParameter(mathBlock.getParam1());
                            double val2 = evaluateMathParameter(mathBlock.getParam2());
                            return val1 + val2;
                        } catch (Exception e) {
                            return 0;
                        }

                    case "subtract":
                        try {
                            double val1 = evaluateMathParameter(mathBlock.getParam1());
                            double val2 = evaluateMathParameter(mathBlock.getParam2());
                            return val1 - val2;
                        } catch (Exception e) {
                            return 0;
                        }

                    case "multiply":
                        try {
                            double val1 = evaluateMathParameter(mathBlock.getParam1());
                            double val2 = evaluateMathParameter(mathBlock.getParam2());
                            return val1 * val2;
                        } catch (Exception e) {
                            return 0;
                        }

                    case "divide":
                        try {
                            double val1 = evaluateMathParameter(mathBlock.getParam1());
                            double val2 = evaluateMathParameter(mathBlock.getParam2());
                            return val2 != 0 ? val1 / val2 : 0;
                        } catch (Exception e) {
                            return 0;
                        }

                    case "modulo":
                        try {
                            double val1 = evaluateMathParameter(mathBlock.getParam1());
                            double val2 = evaluateMathParameter(mathBlock.getParam2());
                            return val2 != 0 ? val1 % val2 : 0;
                        } catch (Exception e) {
                            return 0;
                        }

                    case "xposition":
                        try {
                            VisualJavaSprite s = null;
                            if (mathBlock != null && mathBlock.targetCombo != null) {
                                Object sel = mathBlock.targetCombo.getSelectedItem();
                                if (sel != null) s = findSpriteByName(sel.toString());
                            }
                            if (s == null) s = activeSpriteTL.get();
                            if (s == null && !project.sprites.isEmpty()) s = project.sprites.get(0);
                            return s != null ? s.x : 0;
                        } catch (Exception e) {
                            return 0;
                        }

                    case "yposition":
                        try {
                            VisualJavaSprite s = null;
                            if (mathBlock != null && mathBlock.targetCombo != null) {
                                Object sel = mathBlock.targetCombo.getSelectedItem();
                                if (sel != null) s = findSpriteByName(sel.toString());
                            }
                            if (s == null) s = activeSpriteTL.get();
                            if (s == null && !project.sprites.isEmpty()) s = project.sprites.get(0);
                            return s != null ? s.y : 0;
                        } catch (Exception e) {
                            return 0;
                        }
                    case "variable":
                        try {
                            String varName = mathBlock.getCombo1Value();
                            if (project.variables == null) project.variables = new HashMap<>();
                            return project.variables.getOrDefault(varName, 0);
                        } catch (Exception e) {
                            return 0;
                        }

                    default:
                        return 0;
                }
            }

            double evaluateMathParameter(String param) {
                if (param == null || param.trim().isEmpty()) return 0;
                // Normalize decimal separator: accept both comma and dot (e.g., "0,1" -> 0.1)
                String normalized = param.trim().replace(',', '.');
                try {
                    return Double.parseDouble(normalized);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }

            boolean evaluateCondition(VisualJavaScriptBlock conditionBlock) {
                if (conditionBlock == null) return false;

                switch (conditionBlock.type) {
                    case "keypressed":
                        String key = conditionBlock.getCombo1Value();
                        return isKeyPressed(key);



                    case "istouchingsprites":
                        String aName = conditionBlock.getCombo1Value();
                        VisualJavaSprite a = findSpriteByName(aName);
                        VisualJavaSprite b = null;
                        if (conditionBlock.targetCombo != null) {
                            Object sel = conditionBlock.targetCombo.getSelectedItem();
                            if (sel != null) b = findSpriteByName(sel.toString());
                        }
                        return isTouchingSprite(a, b);

                    case "istouchingcolor":
                        String sName = conditionBlock.getCombo1Value();
                        VisualJavaSprite s = findSpriteByName(sName);
                        Color tColor = conditionBlock.getSelectedColor();
                        return containsColor(s, tColor);

                    case "equal":
                        try {
                            double val1 = evaluateMathParameter(conditionBlock.getParam1());
                            double val2 = evaluateMathParameter(conditionBlock.getParam2());
                            return val1 == val2;
                        } catch (NumberFormatException e) {
                            return conditionBlock.getParam1().equals(conditionBlock.getParam2());
                        }

                    case "greater":
                        try {
                            double val1 = evaluateMathParameter(conditionBlock.getParam1());
                            double val2 = evaluateMathParameter(conditionBlock.getParam2());
                            return val1 > val2;
                        } catch (NumberFormatException e) {
                            return false;
                        }

                    case "less":
                        try {
                            double val1 = evaluateMathParameter(conditionBlock.getParam1());
                            double val2 = evaluateMathParameter(conditionBlock.getParam2());
                            return val1 < val2;
                        } catch (NumberFormatException e) {
                            return false;
                        }

                    default:
                        return false;
                }
            }

            boolean isKeyPressed(String key) {
                return pressedKeys.contains(key);
            }

            String getKeyString(int keyCode) {
                switch (keyCode) {
                    case KeyEvent.VK_SPACE: return "space";
                    case KeyEvent.VK_UP: return "up arrow";
                    case KeyEvent.VK_DOWN: return "down arrow";
                    case KeyEvent.VK_LEFT: return "left arrow";
                    case KeyEvent.VK_RIGHT: return "right arrow";
                    default: return KeyEvent.getKeyText(keyCode).toLowerCase();
                }
            }

            VisualJavaSprite findSpriteByName(String name) {
                for (VisualJavaSprite sprite : project.sprites) {
                    if (sprite.name.equals(name)) {
                        return sprite;
                    }
                }
                return null;
            }

            boolean isTouchingSpriteColor(VisualJavaSprite s1, VisualJavaSprite s2, Color targetColor) {
                if (s1 == null || s2 == null || targetColor == null) return false;
                if (!s1.visible || !s2.visible) return false;
                if (s1.image == null || s2.image == null) return false;

                int w1 = s1.image.getWidth() * s1.size / 100;
                int h1 = s1.image.getHeight() * s1.size / 100;
                int w2 = s2.image.getWidth() * s2.size / 100;
                int h2 = s2.image.getHeight() * s2.size / 100;

                Rectangle r1 = new Rectangle(s1.x, s1.y, w1, h1);
                Rectangle r2 = new Rectangle(s2.x, s2.y, w2, h2);
                Rectangle inter = r1.intersection(r2);
                if (inter.isEmpty()) return false;

                // Tolerance for anti-aliasing
                int tol = 12;
                int tr = targetColor.getRed();
                int tg = targetColor.getGreen();
                int tb = targetColor.getBlue();

                for (int wx = inter.x; wx < inter.x + inter.width; wx += 3) {
                    for (int wy = inter.y; wy < inter.y + inter.height; wy += 3) {
                        int ix2 = (wx - s2.x) * s2.image.getWidth() / Math.max(1, w2);
                        int iy2 = (wy - s2.y) * s2.image.getHeight() / Math.max(1, h2);
                        if (ix2 >= 0 && iy2 >= 0 && ix2 < s2.image.getWidth() && iy2 < s2.image.getHeight()) {
                            int argb = s2.image.getRGB(ix2, iy2);
                            Color pc = new Color(argb, true);
                            int a = pc.getAlpha();
                            if (a > 10) {
                                int dr = Math.abs(pc.getRed() - tr);
                                int dg = Math.abs(pc.getGreen() - tg);
                                int db = Math.abs(pc.getBlue() - tb);
                                if (dr <= tol && dg <= tol && db <= tol) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }

            boolean containsColor(VisualJavaSprite sprite, Color targetColor) {
                if (sprite == null || targetColor == null) return false;
                if (!sprite.visible || sprite.image == null) return false;
                int tol = 12;
                int tr = targetColor.getRed();
                int tg = targetColor.getGreen();
                int tb = targetColor.getBlue();
                int w = sprite.image.getWidth();
                int h = sprite.image.getHeight();
                for (int x = 0; x < w; x += 3) {
                    for (int y = 0; y < h; y += 3) {
                        int argb = sprite.image.getRGB(x, y);
                        Color pc = new Color(argb, true);
                        if (pc.getAlpha() > 10) {
                            int dr = Math.abs(pc.getRed() - tr);
                            int dg = Math.abs(pc.getGreen() - tg);
                            int db = Math.abs(pc.getBlue() - tb);
                            if (dr <= tol && dg <= tol && db <= tol) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            boolean isTouchingSprite(VisualJavaSprite sprite1, VisualJavaSprite sprite2) {
                if (sprite1 == null || sprite2 == null ||
                        sprite1.image == null || sprite2.image == null ||
                        !sprite1.visible || !sprite2.visible) return false;

                // Simple bounding box collision detection
                int w1 = sprite1.image.getWidth() * sprite1.size / 100;
                int h1 = sprite1.image.getHeight() * sprite1.size / 100;
                int w2 = sprite2.image.getWidth() * sprite2.size / 100;
                int h2 = sprite2.image.getHeight() * sprite2.size / 100;

                Rectangle rect1 = new Rectangle(sprite1.x, sprite1.y, w1, h1);
                Rectangle rect2 = new Rectangle(sprite2.x, sprite2.y, w2, h2);

                return rect1.intersects(rect2);
            }

            class VisualJavaGamePanel extends JPanel {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g.setColor(project.backgroundColor != null ? project.backgroundColor : Color.WHITE);
                    g.fillRect(0, 0, getWidth(), getHeight());

                    for (VisualJavaSprite sprite : project.sprites) {
                        if (sprite.visible && sprite.image != null) {
                            int w = sprite.image.getWidth() * sprite.size / 100;
                            int h = sprite.image.getHeight() * sprite.size / 100;

                            g2.rotate(Math.toRadians(sprite.rotation), sprite.x + w/2, sprite.y + h/2);
                            g2.drawImage(sprite.image, sprite.x, sprite.y, w, h, null);
                            g2.rotate(-Math.toRadians(sprite.rotation), sprite.x + w/2, sprite.y + h/2);
                        }
                    }
                    // Draw printed message overlay if present
                    if (printedMessage != null && !printedMessage.isEmpty()) {
                        g2.setColor(printedColor != null ? printedColor : Color.BLACK);
                        g2.setFont(new Font("Arial", Font.BOLD, 14));
                        // Adjust Y to account for baseline semantics so y=0 is visible at top
                        int baselineY = printedY + g2.getFontMetrics().getAscent();
                        g2.drawString(printedMessage, printedX, baselineY);
                    }
                }
            }
        }


        // =================== PAINT EDITOR =====================
        class VisualJavaPaintEditor extends JFrame {
            enum Tool {PEN, RECTANGLE, OVAL, TEXT, ERASER}

            VisualJavaSprite sprite;
            BufferedImage canvas;
            Graphics2D g2d;
            Tool currentTool = Tool.PEN;
            Color currentColor = Color.BLACK;
            boolean fill = false;
            int penSize = 3;
            String fontName = "Arial";
            int fontSize = 20;

            int prevX, prevY, startX, startY;
            BufferedImage tempImage;

            // Track the actual drawing bounds
            int minDrawX = Integer.MAX_VALUE;
            int minDrawY = Integer.MAX_VALUE;
            int maxDrawX = Integer.MIN_VALUE;
            int maxDrawY = Integer.MIN_VALUE;

            int canvasWidth = 512;
            int canvasHeight = 512;

            JPanel canvasPanel;

            public VisualJavaPaintEditor(VisualJavaSprite sprite, VisualJavaEditor parent) {
                this.sprite = sprite;

                setTitle("Editor Paint - " + sprite.name);
                setSize(900, 750);
                setLocationRelativeTo(parent);
                setLayout(new BorderLayout());
                getContentPane().setBackground(new Color(30, 35, 45));

                if (sprite.image == null) {
                    canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                    g2d = canvas.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // start fully transparent
                    g2d.setComposite(AlphaComposite.Clear);
                    g2d.fillRect(0, 0, canvasWidth, canvasHeight);
                    g2d.setComposite(AlphaComposite.SrcOver);
                } else {
                    canvas = sprite.image;
                    canvasWidth = canvas.getWidth();
                    canvasHeight = canvas.getHeight();
                    g2d = canvas.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }

                JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                toolbar.setBackground(new Color(35, 40, 50));
                toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(60, 70, 85)));

                JLabel toolLabel = new JLabel("Strumento:");
                toolLabel.setForeground(new Color(220, 230, 240));
                toolbar.add(toolLabel);

                String[] tools = {"Penna", "Rettangolo", "Ovale", "Testo", "Gomma"};
                JComboBox<String> toolBox = new JComboBox<>(tools);
                toolBox.setBackground(new Color(45, 52, 65));
                toolBox.setForeground(new Color(220, 230, 240));
                toolBox.addActionListener(e -> currentTool = Tool.values()[toolBox.getSelectedIndex()]);
                toolbar.add(toolBox);

                JButton colorBtn = new JButton("Colore");
                colorBtn.setBackground(currentColor);
                colorBtn.setForeground(Color.WHITE);
                colorBtn.setFocusPainted(false);
                colorBtn.setBorderPainted(false);
                colorBtn.addActionListener(e -> {
                    Color c = JColorChooser.showDialog(this, "Seleziona Colore", currentColor);
                    if (c != null) {
                        currentColor = c;
                        colorBtn.setBackground(c);
                    }
                });
                toolbar.add(colorBtn);

                JCheckBox fillBox = new JCheckBox("Riempi");
                fillBox.setBackground(new Color(35, 40, 50));
                fillBox.setForeground(new Color(220, 230, 240));
                fillBox.addActionListener(e -> fill = fillBox.isSelected());
                toolbar.add(fillBox);

                JSpinner penSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
                penSpinner.addChangeListener(e -> penSize = (int) penSpinner.getValue());
                JLabel sizeLabel = new JLabel("Spessore:");
                sizeLabel.setForeground(new Color(220, 230, 240));
                toolbar.add(sizeLabel);
                toolbar.add(penSpinner);

                JButton clearBtn = new JButton("🗑️ Pulisci");
                clearBtn.setBackground(new Color(200, 100, 70));
                clearBtn.setForeground(Color.WHITE);
                clearBtn.setFocusPainted(false);
                clearBtn.setBorderPainted(false);
                clearBtn.addActionListener(e -> {
                    g2d.setComposite(AlphaComposite.Clear);
                    g2d.fillRect(0, 0, canvasWidth, canvasHeight);
                    g2d.setComposite(AlphaComposite.SrcOver);
                    canvasPanel.repaint();
                });
                toolbar.add(clearBtn);

                JButton saveBtn = new JButton("💾 Salva");
                saveBtn.setBackground(new Color(50, 120, 200));
                saveBtn.setForeground(Color.WHITE);
                saveBtn.setFocusPainted(false);
                saveBtn.setBorderPainted(false);
                saveBtn.addActionListener(e -> {
                    BufferedImage cropped = cropToContent(canvas);
                    sprite.image = cropped != null ? cropped : canvas;
                    JOptionPane.showMessageDialog(this, "Sprite salvato!");
                    dispose();
                });
                toolbar.add(saveBtn);

                add(toolbar, BorderLayout.NORTH);

                canvasPanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.drawImage(canvas, 0, 0, null);
                        if (tempImage != null) g.drawImage(tempImage, 0, 0, null);
                    }

                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(canvasWidth, canvasHeight);
                    }
                };
                canvasPanel.setBackground(Color.WHITE);

                JScrollPane scroll = new JScrollPane(canvasPanel);
                scroll.getViewport().setBackground(new Color(40, 45, 55));
                add(scroll, BorderLayout.CENTER);

                canvasPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        startX = prevX = e.getX();
                        startY = prevY = e.getY();

                        // Track drawing bounds
                        updateDrawingBounds(startX, startY);

                        // Expand canvas if needed
                        expandCanvasIfNeeded(startX, startY);

                        if (currentTool == Tool.TEXT) {
                            String text = JOptionPane.showInputDialog(VisualJavaPaintEditor.this, "Testo:");
                            if (text != null) {
                                g2d.setColor(currentColor);
                                g2d.setFont(new Font(fontName, Font.PLAIN, fontSize));
                                g2d.drawString(text, startX, startY);
                                canvasPanel.repaint();
                            }
                        } else if (currentTool != Tool.PEN && currentTool != Tool.ERASER) {
                            tempImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (tempImage != null && currentTool != Tool.PEN && currentTool != Tool.ERASER) {
                            g2d.drawImage(tempImage, 0, 0, null);
                            tempImage = null;
                            canvasPanel.repaint();
                        }
                    }
                });

                canvasPanel.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        int x = e.getX();
                        int y = e.getY();

                        // Track drawing bounds
                        updateDrawingBounds(x, y);

                        // Expand canvas if needed
                        expandCanvasIfNeeded(x, y);

                        if (currentTool == Tool.PEN) {
                            g2d.setColor(currentColor);
                            g2d.setStroke(new BasicStroke(penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2d.drawLine(prevX, prevY, x, y);
                            prevX = x;
                            prevY = y;
                            canvasPanel.repaint();
                        } else if (currentTool == Tool.ERASER) {
                            g2d.setComposite(AlphaComposite.Clear);
                            g2d.setStroke(new BasicStroke(penSize * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2d.drawLine(prevX, prevY, x, y);
                            g2d.setComposite(AlphaComposite.SrcOver);
                            prevX = x;
                            prevY = y;
                            canvasPanel.repaint();
                        } else if (tempImage != null) {
                            Graphics2D tempG = tempImage.createGraphics();
                            tempG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            tempG.setColor(currentColor);
                            tempG.setStroke(new BasicStroke(penSize));

                            int width = x - startX;
                            int height = y - startY;

                            switch (currentTool) {
                                case RECTANGLE:
                                    if (fill) tempG.fillRect(Math.min(startX, x), Math.min(startY, y), Math.abs(width), Math.abs(height));
                                    else tempG.drawRect(Math.min(startX, x), Math.min(startY, y), Math.abs(width), Math.abs(height));
                                    break;
                                case OVAL:
                                    if (fill) tempG.fillOval(Math.min(startX, x), Math.min(startY, y), Math.abs(width), Math.abs(height));
                                    else tempG.drawOval(Math.min(startX, x), Math.min(startY, y), Math.abs(width), Math.abs(height));
                                    break;
                            }
                            tempG.dispose();
                            canvasPanel.repaint();
                        }
                    }
                });

                setVisible(true);
            }

            void updateDrawingBounds(int x, int y) {
                minDrawX = Math.min(minDrawX, x);
                minDrawY = Math.min(minDrawY, y);
                maxDrawX = Math.max(maxDrawX, x);
                maxDrawY = Math.max(maxDrawY, y);
            }

            void expandCanvasIfNeeded(int x, int y) {
                // If drawing near the edge, expand the canvas
                int margin = 100;
                boolean needsExpansion = false;

                if (x > canvasWidth - margin || y > canvasHeight - margin) {
                    int newWidth = Math.max(canvasWidth, x + margin);
                    int newHeight = Math.max(canvasHeight, y + margin);

                    if (newWidth > canvasWidth || newHeight > canvasHeight) {
                        expandCanvas(newWidth, newHeight);
                    }
                }
            }

            void expandCanvas(int newWidth, int newHeight) {
                // Create new larger canvas
                BufferedImage newCanvas = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D newG2d = newCanvas.createGraphics();
                newG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Start transparent and copy existing content
                newG2d.setComposite(AlphaComposite.Clear);
                newG2d.fillRect(0, 0, newWidth, newHeight);
                newG2d.setComposite(AlphaComposite.SrcOver);
                newG2d.drawImage(canvas, 0, 0, null);

                // Update references
                canvas = newCanvas;
                g2d.dispose();
                g2d = newG2d;
                canvasWidth = newWidth;
                canvasHeight = newHeight;

                // Update panel preferred size and repaint
                canvasPanel.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
                canvasPanel.revalidate();
                canvasPanel.repaint();
            }

            BufferedImage cropToContent(BufferedImage src) {
                int w = src.getWidth();
                int h = src.getHeight();
                int minX = w, minY = h, maxX = -1, maxY = -1;
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                        if (a != 0) {
                            if (x < minX) minX = x;
                            if (y < minY) minY = y;
                            if (x > maxX) maxX = x;
                            if (y > maxY) maxY = y;
                        }
                    }
                }
                if (maxX < minX || maxY < minY) return null; // empty
                int cw = maxX - minX + 1;
                int ch = maxY - minY + 1;
                BufferedImage out = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = out.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(src.getSubimage(minX, minY, cw, ch), 0, 0, null);
                g.dispose();
                return out;
            }
        }

        // =================== SCRIPT BLOCK TRANSFERABLE ====================
        class VisualJavaScriptBlockTransferable implements Transferable {
            public static final DataFlavor SCRIPT_BLOCK_FLAVOR = new DataFlavor(VisualJavaScriptBlock.class, "VisualJavaScriptBlock");
            private final VisualJavaScriptBlock scriptBlock;

            public VisualJavaScriptBlockTransferable(VisualJavaScriptBlock block) {
                this.scriptBlock = block;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{SCRIPT_BLOCK_FLAVOR};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return SCRIPT_BLOCK_FLAVOR.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return scriptBlock;
            }
        }

        // =================== MATH INPUT FIELD ====================
        class VisualJavaMathInputField extends JPanel {
            private JTextField textField;
            private VisualJavaScriptBlock mathBlock = null;
            private boolean hasMathBlock = false;
            private VisualJavaEditor editor;

            public VisualJavaMathInputField(String defaultText, int columns, Font font, VisualJavaEditor editor) {
                this.editor = editor;
                setLayout(new BorderLayout());
                setBackground(new Color(200, 200, 200));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        BorderFactory.createEmptyBorder(2, 5, 2, 5)
                ));

                textField = new JTextField(defaultText, columns);
                textField.setFont(font);
                textField.setHorizontalAlignment(JTextField.CENTER);
                textField.setBorder(null);
                textField.setBackground(new Color(200, 200, 200));
                add(textField, BorderLayout.CENTER);

                // Make this field droppable for math blocks
                setTransferHandler(new TransferHandler() {
                    @Override
                    public boolean canImport(TransferSupport support) {
                        return support.isDataFlavorSupported(VisualJavaScriptBlockTransferable.SCRIPT_BLOCK_FLAVOR);
                    }

                    @Override
                    public boolean importData(TransferSupport support) {
                        if (!canImport(support)) return false;

                        try {
                            VisualJavaScriptBlock droppedBlock = (VisualJavaScriptBlock) support.getTransferable().getTransferData(VisualJavaScriptBlockTransferable.SCRIPT_BLOCK_FLAVOR);

                            // Only accept math blocks (reporter blocks)
                            if (droppedBlock.shape == BlockShape.REPORTER &&
                                    (droppedBlock.type.equals("add") || droppedBlock.type.equals("subtract") ||
                                            droppedBlock.type.equals("multiply") || droppedBlock.type.equals("divide") ||
                                            droppedBlock.type.equals("modulo") || droppedBlock.type.equals("xposition") ||
                                            droppedBlock.type.equals("yposition") || droppedBlock.type.equals("variable"))) {

                                setMathBlock(droppedBlock);
                                return true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                });

                // Allow clearing math block with right-click
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3 && hasMathBlock) { // Right click
                            clearMathBlock();
                        }
                    }
                });
            }

            public void setMathBlock(VisualJavaScriptBlock block) {
                this.mathBlock = block;
                this.hasMathBlock = true;

                removeAll();
                add(mathBlock, BorderLayout.CENTER);
                revalidate();
                repaint();
            }

            public void clearMathBlock() {
                this.mathBlock = null;
                this.hasMathBlock = false;

                removeAll();
                add(textField, BorderLayout.CENTER);
                revalidate();
                repaint();
            }

            public boolean hasMathBlock() {
                return hasMathBlock;
            }

            public VisualJavaScriptBlock getMathBlock() {
                return mathBlock;
            }

            public String getText() {
                if (hasMathBlock && mathBlock != null) {
                    return "MATH:" + mathBlock.type; // Special marker for math blocks
                }
                return textField.getText();
            }

            public void setText(String text) {
                textField.setText(text);
            }

            public boolean isEmpty() {
                return !hasMathBlock && textField.getText().trim().isEmpty();
            }
        }

        class MathInputField extends VisualJavaMathInputField {
            public MathInputField(String defaultText, int columns, Font font, VisualJavaEditor editor) {
                super(defaultText, columns, font, editor);
            }
        }

        // =================== BLOCCO SCRIPT =====================
        class VisualJavaScriptBlock extends JPanel {
            String type;
            BlockShape shape;
            Color originalColor;
            Color color;
            JTextField param1, param2;
            JComboBox<String> combo1, targetCombo;
            JButton colorButton;
            Color selectedColor;
            VisualJavaMathInputField mathInput1, mathInput2;
            VisualJavaEditor editor;
            VisualJavaScriptBlock attachedTo = null;
            boolean isConnected = false;

            JPanel conditionSlot;
            JPanel innerBlocksPanel;
            VisualJavaScriptBlock conditionBlock;
            ArrayList<VisualJavaScriptBlock> innerBlocks = new ArrayList<>();

            public VisualJavaScriptBlock(String type, BlockShape shape, Color color, VisualJavaEditor editor) {
                this.type = type;
                this.shape = shape;
                this.originalColor = color;
                this.color = color;
                this.editor = editor;

                setBackground(color);
                setLayout(new BorderLayout());

                JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
                mainPanel.setBackground(color);
                mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                Font labelFont = new Font("Arial", Font.BOLD, 12);
                Font inputFont = new Font("Arial", Font.PLAIN, 11);

                switch (type) {
                    case "move":
                        mainPanel.add(createLabel("MOVE", labelFont));
                        mathInput1 = new MathInputField("10", 5, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("steps", labelFont));
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "setx":
                        mainPanel.add(createLabel("set x to", labelFont));
                        mathInput1 = new MathInputField("0", 5, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "sety":
                        mainPanel.add(createLabel("set y to", labelFont));
                        mathInput1 = new MathInputField("0", 5, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "changex":
                        mainPanel.add(createLabel("change x by", labelFont));
                        mathInput1 = new MathInputField("10", 5, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "changey":
                        mainPanel.add(createLabel("change y by", labelFont));
                        mathInput1 = new MathInputField("10", 5, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "rotate":
                        mainPanel.add(createLabel("rotate", labelFont));
                        mathInput1 = new MathInputField("15", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("degrees", labelFont));
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "setrotation":
                        mainPanel.add(createLabel("set rotation to", labelFont));
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("degrees", labelFont));
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "pointtowards":
                        mainPanel.add(createLabel("point towards", labelFont));
                        combo1 = createSpriteCombo(inputFont);
                        mainPanel.add(combo1);
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "goto":
                        mainPanel.add(createLabel("go to", labelFont));
                        combo1 = createSpriteCombo(inputFont);
                        mainPanel.add(combo1);
                        break;

                    case "setxy":
                        mainPanel.add(createLabel("set x", labelFont));
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel(" y", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "show":
                        mainPanel.add(createLabel("show sprite", labelFont));
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "hide":
                        mainPanel.add(createLabel("hide sprite", labelFont));
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "setsize":
                        mainPanel.add(createLabel("set size to", labelFont));
                        mathInput1 = new MathInputField("100", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("%", labelFont));
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "changesize":
                        mainPanel.add(createLabel("change size by", labelFont));
                        mathInput1 = new MathInputField("10", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("on", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "printmessage":
                        mainPanel.add(createLabel("print message", labelFont));
                        param1 = createTextField("...", 12, inputFont);
                        mainPanel.add(param1);
                        mainPanel.add(createLabel("at", labelFont));
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        mainPanel.add(createLabel("color", labelFont));
                        colorButton = new JButton("   ");
                        colorButton.setBackground(new Color(255, 255, 255));
                        selectedColor = colorButton.getBackground();
                        colorButton.setPreferredSize(new Dimension(40, 25));
                        colorButton.addActionListener(e -> {
                            Color newColor = JColorChooser.showDialog(editor, "Choose Color", colorButton.getBackground());
                            if (newColor != null) {
                                colorButton.setBackground(newColor);
                                selectedColor = newColor;
                            }
                        });
                        mainPanel.add(colorButton);
                        break;

                    case "setbackground":
                        mainPanel.add(createLabel("set background color to", labelFont));
                        colorButton = new JButton("   ");
                        colorButton.setBackground(new Color(255, 255, 255));
                        selectedColor = colorButton.getBackground();
                        colorButton.setPreferredSize(new Dimension(40, 25));
                        colorButton.addActionListener(e -> {
                            Color newColor = JColorChooser.showDialog(editor, "Choose Color", colorButton.getBackground());
                            if (newColor != null) {
                                colorButton.setBackground(newColor);
                                selectedColor = newColor;
                            }
                        });
                        mainPanel.add(colorButton);
                        break;

                    case "bringtofront":
                        mainPanel.add(createLabel("put", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        mainPanel.add(createLabel("to the front", labelFont));
                        break;

                    case "bringtoback":
                        mainPanel.add(createLabel("put", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        mainPanel.add(createLabel("to the back", labelFont));
                        break;



                    case "onstart":
                        mainPanel.add(createLabel("🚀 when program starts", labelFont));
                        break;



                    case "wait":
                        mainPanel.add(createLabel("wait", labelFont));
                        mathInput1 = new MathInputField("1", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("seconds", labelFont));
                        break;



                    case "keypressed":
                        mainPanel.add(createLabel("key", labelFont));
                        combo1 = new JComboBox<>(new String[]{"space", "up arrow", "down arrow", "left arrow", "right arrow"});
                        combo1.setFont(inputFont);
                        mainPanel.add(combo1);
                        mainPanel.add(createLabel("pressed?", labelFont));
                        break;





                    case "istouchingsprites":
                        mainPanel.add(createLabel("is", labelFont));
                        combo1 = createSpriteCombo(inputFont); // first sprite
                        mainPanel.add(combo1);
                        mainPanel.add(createLabel("touching", labelFont));
                        targetCombo = createSpriteCombo(inputFont); // second sprite
                        mainPanel.add(targetCombo);
                        mainPanel.add(createLabel("?", labelFont));
                        break;

                    case "istouchingcolor":
                        mainPanel.add(createLabel("is", labelFont));
                        combo1 = createSpriteCombo(inputFont); // sprite to check
                        mainPanel.add(combo1);
                        mainPanel.add(createLabel("touching", labelFont));
                        colorButton = new JButton("   ");
                        colorButton.setBackground(new Color(255, 0, 0));
                        selectedColor = colorButton.getBackground();
                        colorButton.setPreferredSize(new Dimension(40, 25));
                        colorButton.addActionListener(e -> {
                            Color c = JColorChooser.showDialog(this, "Scegli colore", selectedColor);
                            if (c != null) {
                                selectedColor = c;
                                colorButton.setBackground(c);
                            }
                        });
                        mainPanel.add(colorButton);
                        mainPanel.add(createLabel("?", labelFont));
                        break;

                    case "xposition":
                        mainPanel.add(createLabel("x position of", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "yposition":
                        mainPanel.add(createLabel("y position of", labelFont));
                        targetCombo = createSpriteCombo(inputFont);
                        mainPanel.add(targetCombo);
                        break;

                    case "variable":
                        combo1 = createVariableCombo(inputFont);
                        mainPanel.add(combo1);
                        break;

                    case "setvariable":
                        mainPanel.add(createLabel("set", labelFont));
                        combo1 = createVariableCombo(inputFont);
                        mainPanel.add(combo1);
                        mainPanel.add(createLabel("to", labelFont));
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        break;

                    case "changevariable":
                        mainPanel.add(createLabel("change", labelFont));
                        combo1 = createVariableCombo(inputFont);
                        mainPanel.add(combo1);
                        mainPanel.add(createLabel("by", labelFont));
                        mathInput1 = new MathInputField("1", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        break;

                    case "add":
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("+", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        break;

                    case "subtract":
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("-", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        break;

                    case "multiply":
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("×", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        break;

                    case "divide":
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("/", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        break;

                    case "modulo":
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("%", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        break;


                    case "equal":
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("=", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        break;

                    case "greater":
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel(">", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        break;

                    case "less":
                        mathInput1 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        mainPanel.add(createLabel("<", labelFont));
                        mathInput2 = new MathInputField("0", 4, inputFont, editor);
                        mainPanel.add(mathInput2);
                        break;

                    case "if":
                        mainPanel.add(createLabel("if", labelFont));
                        conditionSlot = createConditionSlot();
                        mainPanel.add(conditionSlot);
                        mainPanel.add(createLabel("then", labelFont));
                        break;

                    // 'ifelse' block is not supported; removed from palette to avoid confusion

                    case "repeat":
                        mainPanel.add(createLabel("repeat", labelFont));
                        mathInput1 = new MathInputField("10", 4, inputFont, editor);
                        mainPanel.add(mathInput1);
                        break;

                    case "repeatuntil":
                        mainPanel.add(createLabel("repeat until", labelFont));
                        conditionSlot = createConditionSlot();
                        mainPanel.add(conditionSlot);
                        break;

                    case "forever":
                        mainPanel.add(createLabel("♾️ forever", labelFont));
                        break;
                }

                JButton deleteBtn = new JButton("×");
                deleteBtn.setFont(new Font("Arial", Font.BOLD, 16));
                deleteBtn.setPreferredSize(new Dimension(30, 30));
                deleteBtn.setBackground(new Color(200, 50, 50));
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setFocusPainted(false);
                deleteBtn.setBorderPainted(false);
                deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                deleteBtn.addActionListener(e -> editor.removeBlock(this));
                mainPanel.add(deleteBtn);

                add(mainPanel, BorderLayout.NORTH);

                if (shape == BlockShape.CONTROL) {
                    innerBlocksPanel = new JPanel();
                    innerBlocksPanel.setLayout(new BoxLayout(innerBlocksPanel, BoxLayout.Y_AXIS));
                    innerBlocksPanel.setBackground(color.darker());
                    innerBlocksPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(color.darker().darker(), 2),
                            BorderFactory.createEmptyBorder(10, 20, 10, 10)
                    ));
                    innerBlocksPanel.setPreferredSize(new Dimension(400, 80));
                    innerBlocksPanel.setMinimumSize(new Dimension(400, 80));

                    JLabel hintLabel = new JLabel("← Trascina qui i blocchi");
                    hintLabel.setForeground(Color.WHITE);
                    hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
                    innerBlocksPanel.add(hintLabel);

                    add(innerBlocksPanel, BorderLayout.CENTER);
                }

                setBorder(BorderFactory.createLineBorder(color.darker(), 3));

                if (shape == BlockShape.CONTROL) {
                    setPreferredSize(new Dimension(450, 130));
                } else if (shape == BlockShape.BOOLEAN) {
                    // Widen boolean blocks so both sprite combos are visible (e.g., "is [sprite] touching [sprite]?")
                    setPreferredSize(new Dimension(360, 40));
                } else if (shape == BlockShape.REPORTER) {
                    setPreferredSize(new Dimension(120, 35));
                } else {
                    setPreferredSize(new Dimension(450, 50));
                }
            }

            JPanel createConditionSlot() {
                JPanel slot = new JPanel();
                slot.setBackground(color.darker());
                // Make the condition slot large enough to display wider boolean blocks inside
                slot.setPreferredSize(new Dimension(360, 40));
                slot.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        BorderFactory.createEmptyBorder(3, 10, 3, 10)
                ));

                JLabel hintLabel = new JLabel("< condizione >");
                hintLabel.setForeground(Color.WHITE);
                hintLabel.setFont(new Font("Arial", Font.ITALIC, 10));
                slot.add(hintLabel);

                return slot;
            }

            void setConditionBlock(VisualJavaScriptBlock block) {
                if (conditionSlot != null) {
                    conditionSlot.removeAll();
                    conditionSlot.setLayout(new BorderLayout());
                    conditionSlot.add(block, BorderLayout.CENTER);
                    conditionBlock = block;
                    conditionSlot.revalidate();
                    conditionSlot.repaint();
                }
            }

            void addInnerBlock(VisualJavaScriptBlock block) {
                if (innerBlocksPanel != null) {
                    if (innerBlocksPanel.getComponentCount() > 0 &&
                            innerBlocksPanel.getComponent(0) instanceof JLabel) {
                        innerBlocksPanel.remove(0);
                    }

                    innerBlocks.add(block);
                    block.setBounds(0, 0, 400, block.getPreferredSize().height);
                    innerBlocksPanel.add(block);

                    int totalHeight = 20;
                    for (VisualJavaScriptBlock b : innerBlocks) {
                        totalHeight += b.getPreferredSize().height + 5;
                    }
                    innerBlocksPanel.setPreferredSize(new Dimension(400, Math.max(80, totalHeight)));

                    setPreferredSize(new Dimension(450, 60 + innerBlocksPanel.getPreferredSize().height));
                    setSize(getPreferredSize());

                    editor.pushBlocksDown(this);

                    innerBlocksPanel.revalidate();
                    innerBlocksPanel.repaint();
                    revalidate();
                    repaint();
                }
            }

            void updateConnectionStatus(boolean connected) {
                this.isConnected = connected;
                if (!connected && shape == BlockShape.NORMAL) {
                    color = new Color(150, 50, 50);
                } else {
                    color = originalColor;
                }
                setBackground(color);

                Component[] components = getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        comp.setBackground(color);
                    }
                }

                if (innerBlocksPanel != null) {
                    innerBlocksPanel.setBackground(color.darker());
                }

                repaint();
            }

            JLabel createLabel(String text, Font font) {
                JLabel label = new JLabel(text);
                label.setForeground(Color.WHITE);
                label.setFont(font);
                // Disabilita eventi mouse per permettere drag del blocco
                label.addMouseListener(new MouseAdapter() {});
                label.addMouseMotionListener(new MouseMotionAdapter() {});
                return label;
            }

            JTextField createTextField(String text, int cols, Font font) {
                JTextField field = new JTextField(text, cols);
                field.setFont(font);
                field.setHorizontalAlignment(JTextField.CENTER);
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        BorderFactory.createEmptyBorder(2, 5, 2, 5)
                ));
                // Permetti editing ma non interferire con drag
                field.setFocusable(true);
                return field;
            }

            JComboBox<String> createSpriteCombo(Font font) {
                JComboBox<String> combo = new JComboBox<>();
                combo.setFont(font);
                combo.setPreferredSize(new Dimension(100, 26));
                for (VisualJavaSprite s : editor.project.sprites) {
                    combo.addItem(s.name);
                }
                if (editor.selectedSprite != null) {
                    combo.setSelectedItem(editor.selectedSprite.name);
                }
                return combo;
            }

            JComboBox<String> createVariableCombo(Font font) {
                JComboBox<String> combo = new JComboBox<>();
                combo.setFont(font);
                combo.setPreferredSize(new Dimension(120, 26));
                if (editor.project.variables != null && !editor.project.variables.isEmpty()) {
                    for (String name : editor.project.variables.keySet()) {
                        combo.addItem(name);
                    }
                } else {
                    combo.addItem("variable");
                }
                return combo;
            }

            VisualJavaSprite findSpriteByName(String name) {
                for (VisualJavaSprite sprite : editor.project.sprites) {
                    if (sprite.name.equals(name)) {
                        return sprite;
                    }
                }
                return null;
            }

            public String getParam1() {
                if (mathInput1 != null) {
                    if (mathInput1.hasMathBlock() && mathInput1.getMathBlock() != null) {
                        double v = editor.evaluateMathBlock(mathInput1.getMathBlock());
                        return Double.toString(v);
                    }
                    return mathInput1.getText();
                }
                return param1 != null ? param1.getText() : "";
            }

            public String getParam2() {
                if (mathInput2 != null) {
                    if (mathInput2.hasMathBlock() && mathInput2.getMathBlock() != null) {
                        double v = editor.evaluateMathBlock(mathInput2.getMathBlock());
                        return Double.toString(v);
                    }
                    return mathInput2.getText();
                }
                return param2 != null ? param2.getText() : "";
            }

            public String getCombo1Value() {
                return combo1 != null ? (String) combo1.getSelectedItem() : "";
            }

            public Color getSelectedColor() {
                return selectedColor;
            }

            public VisualJavaSprite getTargetSprite() {
                if (targetCombo != null) {
                    String targetSpriteName = (String) targetCombo.getSelectedItem();
                    if (targetSpriteName != null && !targetSpriteName.isEmpty()) {
                        return findSpriteByName(targetSpriteName);
                    }
                }
                return null;
            }
        }

        // =================== CLASSI DATI =====================
        static class VisualJavaProject implements Serializable {
            private static final long serialVersionUID = 1L;
            ArrayList<VisualJavaSprite> sprites = new ArrayList<>();
            Color backgroundColor = Color.WHITE;
            HashMap<String, Integer> variables = new HashMap<>();
        }

        static class VisualJavaSprite implements Serializable {
            private static final long serialVersionUID = 1L;
            String name;
            int x, y;
            int rotation = 0;
            int size = 100;
            int colorEffect = 0;
            boolean visible = true;
            transient BufferedImage image;
            private byte[] imageData;
            // Persisted, serializable representation of scripts
            ArrayList<VisualJavaScriptDTO> scripts = new ArrayList<>();

            public VisualJavaSprite(String name, int x, int y) {
                this.name = name;
                this.x = x;
                this.y = y;
            }

            void createDefaultSquare() {
                image = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(45, 100, 180));
                g.fillRoundRect(0, 0, 80, 80, 10, 10);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString(name, 10, 45);
                g.dispose();
            }

            VisualJavaSprite duplicate(String newName) {
                VisualJavaSprite copy = new VisualJavaSprite(newName, x + 20, y + 20);
                copy.rotation = rotation;
                copy.size = size;
                copy.visible = visible;
                if (image != null) {
                    copy.image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = copy.image.createGraphics();
                    g.drawImage(image, 0, 0, null);
                    g.dispose();
                }
                return copy;
            }

            private void writeObject(ObjectOutputStream out) throws IOException {
                out.defaultWriteObject();
                if (image != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(image, "png", baos);
                    imageData = baos.toByteArray();
                    out.writeObject(imageData);
                } else {
                    out.writeObject(null);
                }
            }

            private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
                in.defaultReadObject();
                imageData = (byte[]) in.readObject();
                if (imageData != null) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                    image = javax.imageio.ImageIO.read(bais);
                }
            }

            public void saveScriptBlocks(ArrayList<VisualJavaScriptBlock> blocks, Container scriptArea) {
                scripts.clear();
                for (VisualJavaScriptBlock block : blocks) {
                    if (block.getParent() == scriptArea) {
                        scripts.add(VisualJavaScriptDTO.fromBlock(block));
                    }
                }
            }

            public ArrayList<VisualJavaScriptBlock> loadScriptBlocks(VisualJavaEditor editor) {
                ArrayList<VisualJavaScriptBlock> loadedBlocks = new ArrayList<>();
                for (VisualJavaScriptDTO dto : scripts) {
                    VisualJavaScriptBlock newBlock = dto.toBlock(editor);
                    loadedBlocks.add(newBlock);
                }
                return loadedBlocks;
            }
        }

        // Serializable Data Transfer Object for ScriptBlock
        static class VisualJavaScriptDTO implements Serializable {
            private static final long serialVersionUID = 1L;
            String type;
            BlockShape shape;
            int colorRGB;
            String param1Text;
            String param2Text;
            String combo1Value;
            String targetComboValue;
            Integer selectedColorRGB; // nullable
            int x, y, w, h;
            VisualJavaScriptDTO condition;
            ArrayList<VisualJavaScriptDTO> inner = new ArrayList<>();

            static VisualJavaScriptDTO fromBlock(VisualJavaScriptBlock b) {
                VisualJavaScriptDTO dto = new VisualJavaScriptDTO();
                dto.type = b.type;
                dto.shape = b.shape;
                dto.colorRGB = b.originalColor != null ? b.originalColor.getRGB() : new Color(90,90,90).getRGB();
                dto.param1Text = b.getParam1();
                dto.param2Text = b.getParam2();
                dto.combo1Value = b.combo1 != null ? (String) b.combo1.getSelectedItem() : null;
                dto.targetComboValue = b.targetCombo != null ? (String) b.targetCombo.getSelectedItem() : null;
                Color sel = b.getSelectedColor();
                dto.selectedColorRGB = sel != null ? sel.getRGB() : null;
                dto.x = b.getX();
                dto.y = b.getY();
                dto.w = b.getWidth();
                dto.h = b.getHeight();

                if (b.conditionBlock != null) {
                    dto.condition = fromBlock(b.conditionBlock);
                }
                if (b.innerBlocksPanel != null) {
                    for (Component comp : b.innerBlocksPanel.getComponents()) {
                        if (comp instanceof VisualJavaScriptBlock) {
                            dto.inner.add(fromBlock((VisualJavaScriptBlock) comp));
                        }
                    }
                }
                return dto;
            }

            VisualJavaScriptBlock toBlock(VisualJavaEditor editor) {
                VisualJavaScriptBlock nb = editor.new VisualJavaScriptBlock(type, shape, new Color(colorRGB, true), editor);
                // params
                if (nb.param1 != null && param1Text != null) nb.param1.setText(param1Text);
                if (nb.param2 != null && param2Text != null) nb.param2.setText(param2Text);

                if (nb.mathInput1 != null && param1Text != null) nb.mathInput1.setText(param1Text);
                if (nb.mathInput2 != null && param2Text != null) nb.mathInput2.setText(param2Text);

                // combos
                if (nb.combo1 != null && combo1Value != null) nb.combo1.setSelectedItem(combo1Value);
                if (nb.targetCombo != null && targetComboValue != null) nb.targetCombo.setSelectedItem(targetComboValue);

                // color
                if (selectedColorRGB != null) {
                    nb.selectedColor = new Color(selectedColorRGB, true);
                    if (nb.colorButton != null) nb.colorButton.setBackground(nb.selectedColor);
                }

                // condition
                if (condition != null) {
                    VisualJavaScriptBlock condBlock = condition.toBlock(editor);
                    nb.setConditionBlock(condBlock);
                }

                // inner blocks
                if (inner != null && !inner.isEmpty()) {
                    for (VisualJavaScriptDTO child : inner) {
                        VisualJavaScriptBlock childBlock = child.toBlock(editor);
                        nb.addInnerBlock(childBlock);
                    }
                }

                nb.setBounds(x, y, w > 0 ? w : nb.getPreferredSize().width, h > 0 ? h : nb.getPreferredSize().height);
                return nb;
            }
        }
    }
}