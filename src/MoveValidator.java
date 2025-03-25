import java.util.ArrayList;
import java.util.List;

public class MoveValidator {
    private static final boolean whiteKingMoved = false;
    private static final boolean blackKingMoved = false;
    private static final boolean whiteRookLeftMoved = false;
    private static final boolean whiteRookRightMoved = false;
    private static final boolean blackRookLeftMoved = false;
    private static final boolean blackRookRightMoved = false;
    private static final int[] enPassantTarget = null; // Tracks en passant possibility

    public static List<int[]> getValidMoves(String[][] board, int[] from) {
        List<int[]> moves = new ArrayList<>();
        int row = from[0], col = from[1];
        String piece = board[row][col];

        if (piece == null) return moves;

        char type = piece.charAt(1);
        boolean isWhite = piece.charAt(0) == 'w';

        switch (type) {
            case 'P': // Pawn
                getPawnMoves(board, row, col, isWhite, moves);
                break;
            case 'R': // Rook
                getSlidingMoves(board, row, col, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
                break;
            case 'B': // Bishop
                getSlidingMoves(board, row, col, moves, new int[][]{{1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
                break;
            case 'Q': // Queen
                getSlidingMoves(board, row, col, moves, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}});
                break;
            case 'N': // Knight
                getKnightMoves(board, row, col, moves);
                break;
            case 'K': // King
                getKingMoves(board, row, col, isWhite, moves);
                break;
        }

        return removeMovesThatLeaveKingInCheck(board, from, moves);
    }

    private static void getPawnMoves(String[][] board, int row, int col, boolean isWhite, List<int[]> moves) {
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        if (isValidCell(row + direction, col) && board[row + direction][col] == null)
            moves.add(new int[]{row + direction, col});

        if (row == startRow && board[row + direction][col] == null && board[row + 2 * direction][col] == null)
            moves.add(new int[]{row + 2 * direction, col});

        for (int dc : new int[]{-1, 1}) {
            int newRow = row + direction, newCol = col + dc;
            if (isValidCell(newRow, newCol) && board[newRow][newCol] != null && board[newRow][newCol].charAt(0) != (isWhite ? 'w' : 'b'))
                moves.add(new int[]{newRow, newCol});
        }

        // En passant
        if (enPassantTarget != null && Math.abs(col - enPassantTarget[1]) == 1 && row == (isWhite ? 3 : 4)) {
            moves.add(new int[]{enPassantTarget[0], enPassantTarget[1]});
        }
    }

    private static void getSlidingMoves(String[][] board, int row, int col, List<int[]> moves, int[][] directions) {
        for (int[] dir : directions) {
            int r = row, c = col;
            while (true) {
                r += dir[0];
                c += dir[1];
                if (!isValidCell(r, c)) break;
                if (board[r][c] == null) {
                    moves.add(new int[]{r, c});
                } else {
                    if (board[r][c].charAt(0) != board[row][col].charAt(0)) moves.add(new int[]{r, c});
                    break;
                }
            }
        }
    }

    private static void getKnightMoves(String[][] board, int row, int col, List<int[]> moves) {
        int[][] jumps = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}};
        for (int[] jump : jumps) {
            int r = row + jump[0], c = col + jump[1];
            if (isValidCell(r, c) && (board[r][c] == null || board[r][c].charAt(0) != board[row][col].charAt(0)))
                moves.add(new int[]{r, c});
        }
    }

    private static void getKingMoves(String[][] board, int row, int col, boolean isWhite, List<int[]> moves) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] dir : directions) {
            int r = row + dir[0], c = col + dir[1];
            if (isValidCell(r, c) && (board[r][c] == null || board[r][c].charAt(0) != board[row][col].charAt(0)))
                moves.add(new int[]{r, c});
        }

        // Castling
        if ((isWhite && !whiteKingMoved) || (!isWhite && !blackKingMoved)) {
            if (canCastle(board, isWhite, true)) moves.add(new int[]{row, col + 2});
            if (canCastle(board, isWhite, false)) moves.add(new int[]{row, col - 2});
        }
    }

    private static boolean canCastle(String[][] board, boolean isWhite, boolean kingside) {
        int row = isWhite ? 7 : 0;
        int kingCol = 4, rookCol = kingside ? 7 : 0;
        boolean rookMoved = kingside ? (isWhite ? whiteRookRightMoved : blackRookRightMoved) : (isWhite ? whiteRookLeftMoved : blackRookLeftMoved);

        if (board[row][rookCol] == null || board[row][rookCol].charAt(1) != 'R' || rookMoved)
            return false;

        int step = kingside ? 1 : -1;
        for (int c = kingCol + step; c != rookCol; c += step) {
            if (board[row][c] != null) return false;
        }

        return true;
    }

    private static String[][] copyBoard(String[][] board) {
        String[][] newBoard = new String[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, 8);
        }
        return newBoard;
    }

    private static boolean isKingInCheck(String[][] board, boolean isWhite) {
        int kingRow = -1, kingCol = -1;

        // Find the king's position
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null && board[row][col].equals((isWhite ? "wK" : "bK"))) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }

        // If the king is missing (should not happen), assume checkmate to prevent errors
        if (kingRow == -1 || kingCol == -1) return true;

        // Check if the king's position is attacked
        return isSquareAttacked(board, kingRow, kingCol, !isWhite); // FIX: This should return true if attacked
    }

    private static boolean isSquareAttacked(String[][] board, int row, int col, boolean byWhite) {
        String opponentColor = byWhite ? "w" : "b";

        // Check pawn attacks
        int pawnRowOffset = byWhite ? -1 : 1;
        if (isValidCell(row + pawnRowOffset, col - 1) && board[row + pawnRowOffset][col - 1] != null &&
                board[row + pawnRowOffset][col - 1].equals(opponentColor + "P")) return true;
        if (isValidCell(row + pawnRowOffset, col + 1) && board[row + pawnRowOffset][col + 1] != null &&
                board[row + pawnRowOffset][col + 1].equals(opponentColor + "P")) return true;

        // Check knight attacks
        int[][] knightMoves = {{-2, -1}, {-2, 1}, {2, -1}, {2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}};
        for (int[] move : knightMoves) {
            int r = row + move[0], c = col + move[1];
            if (isValidCell(r, c) && board[r][c] != null && board[r][c].equals(opponentColor + "N")) return true;
        }

        // Check sliding piece attacks (Rook, Bishop, Queen)
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] dir : directions) {
            int r = row, c = col;
            while (true) {
                r += dir[0];
                c += dir[1];
                if (!isValidCell(r, c)) break;
                if (board[r][c] != null) {
                    String piece = board[r][c];
                    if (piece.startsWith(opponentColor)) {
                        char type = piece.charAt(1);
                        if ((dir[0] == 0 || dir[1] == 0) && (type == 'R' || type == 'Q')) return true; // Rook or Queen
                        if ((dir[0] != 0 && dir[1] != 0) && (type == 'B' || type == 'Q')) return true; // Bishop or Queen
                    }
                    break;
                }
            }
        }

        // Check king attacks
        int[][] kingMoves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] move : kingMoves) {
            int r = row + move[0], c = col + move[1];
            if (isValidCell(r, c) && board[r][c] != null && board[r][c].equals(opponentColor + "K")) return true;
        }

        return false;
    }

    private static List<int[]> removeMovesThatLeaveKingInCheck(String[][] board, int[] from, List<int[]> moves) {
        List<int[]> safeMoves = new ArrayList<>();
        for (int[] move : moves) {
            String[][] tempBoard = copyBoard(board);
            tempBoard[move[0]][move[1]] = tempBoard[from[0]][from[1]];
            tempBoard[from[0]][from[1]] = null;
            if (!isKingInCheck(tempBoard, tempBoard[move[0]][move[1]].charAt(0) == 'w')) {
                safeMoves.add(move);
            }
        }
        return safeMoves;
    }

    public static boolean isCheckmate(String[][] board, boolean isWhite) {
        // If the king is not in check, it's not checkmate
        if (!isKingInCheck(board, isWhite)) return false;

        // Check if any move can get the king out of check
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null && board[row][col].startsWith(isWhite ? "w" : "b")) {
                    List<int[]> moves = getValidMoves(board, new int[]{row, col});
                    if (!moves.isEmpty()) return false; // The player still has legal moves
                }
            }
        }

        // No valid moves left and king is in check → Checkmate
        return true;
    }

    private static boolean isValidCell(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}