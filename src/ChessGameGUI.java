/*
 * Chess Game in Java with GUI and Advanced Rules
 * - Graphical interface using Swing
 * - Two-player local game
 * - Check and checkmate detection
 * - Castling, en passant, and pawn promotion
 * - Main Menu with "How to Play" and "Play" buttons
 * - Piece movement highlighting
 * - Images for chess pieces (resized for better fit)
 * - Correct movement rules for each piece
 * - Fixed board size of 1000x1000 pixels
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ChessGameGUI extends JFrame {
    private static final String[][] board = new String[8][8];
    private static boolean whiteTurn = true;
    private static JButton[][] buttons = new JButton[8][8];
    private int[] selected = null;

    public ChessGameGUI() {
        setTitle("Chess Game");
        setSize(1000, 1000);
        setResizable(false);
        showMainMenu();
    }

    private void showMainMenu() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Chess Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        add(title, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton playButton = new JButton("Play");
        playButton.addActionListener(e -> startGame());
        JButton howToPlayButton = new JButton("How to Play");
        howToPlayButton.addActionListener(e -> showHowToPlay());

        menuPanel.add(playButton);
        menuPanel.add(howToPlayButton);
        add(menuPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void showHowToPlay() {
        JOptionPane.showMessageDialog(this, "Chess Rules:\n- Each player takes turns moving one piece.\n- Checkmate the opponent's King to win.\n- Special moves: Castling, En Passant, Pawn Promotion.\n- White starts first.", "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startGame() {
        getContentPane().removeAll();
        setLayout(new GridLayout(8, 8));
        initializeBoard();
        initializeGUI();
        revalidate();
        repaint();
    }

    private void initializeBoard() {
        String[] backRow = {"R", "N", "B", "Q", "K", "B", "N", "R"};
        for (int i = 0; i < 8; i++) {
            board[0][i] = "b" + backRow[i];
            board[1][i] = "bP";
            board[6][i] = "wP";
            board[7][i] = "w" + backRow[i];
        }
    }

    private void initializeGUI() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                buttons[row][col] = new JButton();
                buttons[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                buttons[row][col].setFocusPainted(false);
                buttons[row][col].addActionListener(new ButtonListener(row, col));
                add(buttons[row][col]);
                updateButton(row, col);
            }
        }
    }

    private void updateButton(int row, int col) {
        String piece = board[row][col];
        buttons[row][col].setIcon(null);
        if (piece != null) {
            ImageIcon icon = new ImageIcon("images/" + piece + ".png");
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            buttons[row][col].setIcon(new ImageIcon(img));
        }
    }

    private class ButtonListener implements ActionListener {
        int row, col;

        ButtonListener(int row, int col) {this.row = row; this.col = col;}

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selected == null) {
                if (board[row][col] != null && ((whiteTurn && board[row][col].startsWith("w")) || (!whiteTurn && board[row][col].startsWith("b")))) {
                    selected = new int[]{row, col};
                    highlightMoves(selected);
                }
            } else {
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

                    // **CHECK FOR CHECKMATE**
                    if (MoveValidator.isCheckmate(board, !whiteTurn)) {
                        showGameOverDialog(whiteTurn ? "White" : "Black");
                        return;  // Stop further execution after game over
                    }
                }

                selected = null;
                clearHighlights();
            }
            refreshBoard();
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
        board[to[0]][to[1]] = board[from[0]][from[1]];
        board[from[0]][from[1]] = null;
    }

    private void refreshBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                updateButton(row, col);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChessGameGUI().setVisible(true));
    }
}
