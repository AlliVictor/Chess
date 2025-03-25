import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChessGameGUI extends JFrame {
    private static final String[][] board = new String[8][8];
    private static boolean whiteTurn = true;
    private static final JButton[][] buttons = new JButton[8][8];
    private int[] selected = null;
    private int[] lastFrom = null;
    private int[] lastTo = null;
    private final Image backgroundImage;

    private final JTextArea moveHistoryArea = new JTextArea();  // Move history panel
    private boolean vsAI = false;  // Tracks if playing vs AI


    public ChessGameGUI() {
        setTitle("Chess Game");
        setSize(1000, 1000);
        setResizable(false);
        // Load background image
        backgroundImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/background.jpg"))).getImage();
        showMainMenu();
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void showMainMenu() {
        getContentPane().removeAll();

        // Background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // --- Title ---
        JLabel title = new JLabel("Chess Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        // Increased top margin to move title down
        title.setBorder(BorderFactory.createEmptyBorder(60, 0, 30, 0));
        backgroundPanel.add(title, BorderLayout.NORTH);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(new Color(0, 0, 0, 100)); // semi-transparent black
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        buttonPanel.setMaximumSize(new Dimension(400, 1000)); // optional sizing constraint


        Font buttonFont = new Font("Arial", Font.BOLD, 20); // Larger font

        JButton playButton = new JButton("2-Player Game");
        styleMenuButton(playButton, buttonFont); // Larger button size
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> {
            vsAI = false;
            startGame();
        });

        JButton aiButton = new JButton("Play vs AI");
        styleMenuButton(aiButton, buttonFont);
        aiButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        aiButton.addActionListener(e -> {
            vsAI = true;
            startGame();
        });

        JButton howToPlayButton = new JButton("How to Play");
        styleMenuButton(howToPlayButton, buttonFont);
        howToPlayButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        howToPlayButton.addActionListener(e -> showHowToPlay());

        buttonPanel.add(playButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(460, 40))); // More spacing
        buttonPanel.add(aiButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(460, 40)));
        buttonPanel.add(howToPlayButton);

        // --- Wrapper to Center Button Panel ---
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(buttonPanel);

        backgroundPanel.add(centerWrapper, BorderLayout.CENTER);

        revalidate();
        repaint();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void styleMenuButton(JButton button, Font font) {
        button.setFont(font);
        button.setPreferredSize(new Dimension(250, 55));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237)); // Lighter blue
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180)); // Original blue
            }
        });

    }

    private void showHowToPlay() {
        String rules = """
                Chess Rules:
                
                Objective:
                - Checkmate the opponent's King to win.
                
                Piece Movement:
                - Pawns: Move forward 1 square; 2 squares from starting position. Capture diagonally.
                - Rooks: Move any number of squares vertically or horizontally.
                - Knights: Move in 'L' shape; can jump over pieces.
                - Bishops: Move diagonally any number of squares.
                - Queen: Combines Rook and Bishop movements.
                - King: Moves 1 square in any direction.
                
                Special Moves:
                - Castling: King and Rook move simultaneously under specific conditions.
                - En Passant: Special pawn capture immediately after opponent's double move.
                - Promotion: Pawn reaching the opposite end becomes Queen, Rook, Bishop, or Knight.
                
                Game Rules:
                - White moves first; players alternate turns.
                - A player cannot make a move that leaves their King in check.
                - The game ends in checkmate, stalemate, or draw.
                """;

        JOptionPane.showMessageDialog(this, rules, "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startGame() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Menu");

        JMenuItem rulesItem = new JMenuItem("How to Play");
        rulesItem.addActionListener(e -> showHowToPlay());

        JMenuItem restartItem = new JMenuItem("Restart");
        restartItem.addActionListener(e -> startGame());

        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(e -> {
            setJMenuBar(null);
            showMainMenu();
        });

        gameMenu.add(rulesItem);
        gameMenu.add(restartItem);
        gameMenu.add(quitItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        // Game state
        whiteTurn = true;
        selected = null;
        lastFrom = null;
        lastTo = null;
        initializeBoard();
        moveHistoryArea.setText("");  // Clear history on reset

        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        initializeGUI(boardPanel);
        add(boardPanel, BorderLayout.CENTER);

        // Move history panel (Styled)
        // Reset history text
        moveHistoryArea.setText("");
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        moveHistoryArea.setBackground(new Color(245, 245, 245));
        moveHistoryArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Move History "));
        moveHistoryArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        scrollPane.setPreferredSize(new Dimension(150, 1000));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(scrollPane, BorderLayout.EAST);

        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
        refreshBoard();
    }

    private void refreshBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                buttons[row][col].setIcon(null);
                if (board[row][col] != null) {
                    ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/" + board[row][col] + ".png")));
                    Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    buttons[row][col].setIcon(new ImageIcon(img));

                }
                // Reset square color (no highlight)
                buttons[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
            }
        }
    }

    private void initializeBoard() {
        // Clear the board first
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }

        // Set up new game pieces
        String[] backRow = {"R", "N", "B", "Q", "K", "B", "N", "R"};
        for (int i = 0; i < 8; i++) {
            board[0][i] = "b" + backRow[i];
            board[1][i] = "bP";
            board[6][i] = "wP";
            board[7][i] = "w" + backRow[i];
        }
    }

    private void initializeGUI(JPanel boardPanel) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                buttons[row][col] = new JButton();
                buttons[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                buttons[row][col].setFocusPainted(false);
                buttons[row][col].addActionListener(new ButtonListener(row, col));
                boardPanel.add(buttons[row][col]);
                updateButton(row, col);
            }
        }
    }

    private void updateButton(int row, int col) {
        String piece = board[row][col];
        buttons[row][col].setIcon(null);
        if (piece != null) {
            ImageIcon icon = new ImageIcon("Chess/src/images/" + piece + ".png");
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            buttons[row][col].setIcon(new ImageIcon(img));
        }
    }

    private class ButtonListener extends Component implements ActionListener {
        int row, col;

        ButtonListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selected == null) {
                if (board[row][col] != null && ((whiteTurn && board[row][col].startsWith("w")) || (!whiteTurn && board[row][col].startsWith("b")))) {
                    selected = new int[]{row, col};
                    highlightMoves(selected);
                }
            }

            else {
                List<int[]> validMoves = MoveValidator.getValidMoves(board, selected);
                boolean isValid = false;

                for (int[] move : validMoves) {
                    if (move[0] == row && move[1] == col) {
                        isValid = true;
                        break;
                    }
                }

                if (isValid) {
                    makeMove(selected, new int[]{row, col});
                    whiteTurn = !whiteTurn;

                    refreshBoard();

                    if (MoveValidator.isCheckmate(board, !whiteTurn)) {
                        showGameOverDialog(whiteTurn ? "White" : "Black");
                        return;
                    }

                    else if (isStalemate(!whiteTurn)) {
                        showStalemateDialog();
                        return;
                    }

                    // AI Move if enabled
                    if (vsAI && !whiteTurn) {
                        aiMove();
                        whiteTurn = true;
                        refreshBoard();

                        if (MoveValidator.isCheckmate(board, false)) {
                            showGameOverDialog("White");
                        }

                        else if (isStalemate(true)) {
                            showStalemateDialog();
                        }
                    }
                }

                selected = null;
                clearHighlights();
            }
        }

        private void showGameOverDialog(String winner) {
            int option = JOptionPane.showOptionDialog(
                    this,
                    winner + " has won by checkmate!",
                    "Game Over",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Return to Menu"},
                    "Return to Menu"
            );

            if (option == 0) {
                showMainMenu(); // Go back to the main menu
            }
        }

        private void highlightMoves(int[] from) {
            List<int[]> moves = MoveValidator.getValidMoves(board, from);
            for (int[] move : moves) {
                buttons[move[0]][move[1]].setBackground(Color.YELLOW);
            }
        }

        private void clearHighlights() {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    buttons[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                }
            }
        }

        private void makeMove(int[] from, int[] to) {
            String piece = board[from[0]][from[1]];
            board[to[0]][to[1]] = piece;
            board[from[0]][from[1]] = null;

            lastFrom = from;
            lastTo = to;

            // Handle pawn promotion
            if ((piece.equals("wP") && to[0] == 0) || (piece.equals("bP") && to[0] == 7)) {
                String[] options = {"Queen", "Rook", "Bishop", "Knight"};
                String choice = (String) JOptionPane.showInputDialog(this, "Promote to:", "Pawn Promotion",
                        JOptionPane.PLAIN_MESSAGE, null, options, "Queen");
                if (choice != null) {
                    board[to[0]][to[1]] = piece.charAt(0) + choice.substring(0, 1);
                }
            }

            // Add move to history
            String moveNotation = piece + ": " + (char) ('a' + from[1]) + (8 - from[0]) + " → " + (char) ('a' + to[1]) + (8 - to[0]);
            moveHistoryArea.append(moveNotation + "\n");
            moveHistoryArea.setCaretPosition(moveHistoryArea.getDocument().getLength());  // Auto-scroll
        }

        private boolean isStalemate(boolean isWhite) {
            if (MoveValidator.isCheckmate(board, isWhite)) return false;

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] != null && board[row][col].startsWith(isWhite ? "w" : "b")) {
                        List<int[]> moves = MoveValidator.getValidMoves(board, new int[]{row, col});
                        if (!moves.isEmpty()) return false;
                    }
                }
            }
            return true;
        }

        private void showStalemateDialog() {
            int option = JOptionPane.showOptionDialog(
                    this,
                    "Stalemate! It's a draw.",
                    "Game Over",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Return to Menu"},
                    "Return to Menu"
            );

            if (option == 0) {
                showMainMenu();
            }
        }

        private void aiMove() {
            List<int[]> allMoves = new ArrayList<>();
            List<int[]> fromList = new ArrayList<>();

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] != null && board[row][col].startsWith("b")) {
                        List<int[]> moves = MoveValidator.getValidMoves(board, new int[]{row, col});
                        for (int[] move : moves) {
                            allMoves.add(move);
                            fromList.add(new int[]{row, col});
                        }
                    }
                }
            }

            if (!allMoves.isEmpty()) {
                int index = (int) (Math.random() * allMoves.size());
                makeMove(fromList.get(index), allMoves.get(index));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChessGameGUI().setVisible(true));
    }
}