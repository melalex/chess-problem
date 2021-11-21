package com.melalex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChessProblemApp {

    public static void main(String[] args) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Board height: ");
        var rowCount = Integer.parseInt(reader.readLine());

        System.out.print("Board width: ");
        var columnCount = Integer.parseInt(reader.readLine());

        System.out.print("Queen count: ");
        var queenCount = Integer.parseInt(reader.readLine());

        System.out.print("Horse count: ");
        var horseCount = Integer.parseInt(reader.readLine());

        var solution = findSolution(Board.of(rowCount, columnCount), queenCount, horseCount);

        System.out.println("Solution: ");
        System.out.println(solution.map(Board::asString).orElse("No solution"));
    }

    private static Optional<Board> findSolution(Board board, int queensCount, int horsesCount) {
        System.out.println("Queens count = " + queensCount + " ; Horses count = " + horsesCount);
        System.out.println(board.asString(true));
        System.out.println();

        if (queensCount == 0 && horsesCount == 0) { // Found solution
            return Optional.of(board);
        } else if (queensCount == 0) { // Place Horse
            return board.lookFor(CellState::canPlaceHorse)
                    .flatMap(position -> findSolution(placeHorse(board, position), queensCount, horsesCount - 1).stream())
                    .findAny();
        } else { // Place Queen
            return board.lookFor(CellState::canPlaceQueen)
                    .flatMap(position -> findSolution(placeQueen(board, position), queensCount - 1, horsesCount).stream())
                    .findAny();
        }
    }

    private static Board placeQueen(Board board, Position position) {
        var newBoard = board.copy();

        banCellThatQueenCouldAttack(newBoard, position, true);
        banCellThatHorseCouldAttack(newBoard, position, false);

        newBoard.set(position, CellState.QUEEN);

        return newBoard;
    }

    private static Board placeHorse(Board board, Position position) {
        var newBoard = board.copy();

        banCellThatHorseCouldAttack(newBoard, position, true);
        banCellThatQueenCouldAttack(newBoard, position, false);

        newBoard.set(position, CellState.HORSE);

        return newBoard;
    }

    private static void banCellThatQueenCouldAttack(Board board, Position position, boolean forceRewrite) {
        // Ban cells in a same column
        for (int row = 0; row < board.getRowCount(); row++) {
            var cell = board.get(row, position.getColumn());

            if (forceRewrite || cell == CellState.CAN_PLACE_QUEEN) {
                board.set(row, position.getColumn(), CellState.NOT_AVAILABLE);
            } else if (cell == CellState.AVAILABLE) {
                board.set(row, position.getColumn(), CellState.CAN_PLACE_HORSE);
            }
        }

        // Ban cells in a same row
        for (int column = 0; column < board.getColumnCount(); column++) {
            var cell = board.get(position.getRow(), column);

            if (forceRewrite || cell == CellState.CAN_PLACE_QUEEN) {
                board.set(position.getRow(), column, CellState.NOT_AVAILABLE);
            } else if (cell == CellState.AVAILABLE) {
                board.set(position.getRow(), column, CellState.CAN_PLACE_HORSE);
            }
        }

        // Ban cells in a upper diagonal
        for (int i = 1; position.getRow() - i >= 0; i++) {
            var leftColumn = position.getColumn() - i;
            var rightColumn = position.getColumn() + i;
            var row = position.getRow() - i;

            if (leftColumn >= 0) {
                var cell = board.get(row, leftColumn);

                if (forceRewrite || cell == CellState.CAN_PLACE_QUEEN) {
                    board.set(row, leftColumn, CellState.NOT_AVAILABLE);
                } else if (cell == CellState.AVAILABLE) {
                    board.set(row, leftColumn, CellState.CAN_PLACE_HORSE);
                }
            }

            if (rightColumn < board.getColumnCount()) {
                var cell = board.get(row, rightColumn);

                if (forceRewrite || cell == CellState.CAN_PLACE_QUEEN) {
                    board.set(row, rightColumn, CellState.NOT_AVAILABLE);
                } else if (cell == CellState.AVAILABLE) {
                    board.set(row, rightColumn, CellState.CAN_PLACE_HORSE);
                }
            }
        }

        // Ban cells in a lower diagonal
        for (int i = 1; position.getRow() + i < board.getRowCount(); i++) {
            var leftColumn = position.getColumn() - i;
            var rightColumn = position.getColumn() + i;
            var row = position.getRow() + i;

            if (leftColumn >= 0) {
                var cell = board.get(row, leftColumn);

                if (forceRewrite || cell == CellState.CAN_PLACE_QUEEN) {
                    board.set(row, leftColumn, CellState.NOT_AVAILABLE);
                } else if (cell == CellState.AVAILABLE) {
                    board.set(row, leftColumn, CellState.CAN_PLACE_HORSE);
                }
            }

            if (rightColumn < board.getColumnCount()) {
                var cell = board.get(row, rightColumn);

                if (forceRewrite || cell == CellState.CAN_PLACE_QUEEN) {
                    board.set(row, rightColumn, CellState.NOT_AVAILABLE);
                } else if (cell == CellState.AVAILABLE) {
                    board.set(row, rightColumn, CellState.CAN_PLACE_HORSE);
                }
            }
        }
    }

    private static void banCellThatHorseCouldAttack(Board board, Position position, boolean forceRewrite) {
        var horseAttackPositions = List.of(
                position.moveOn(2, 1),
                position.moveOn(-2, 1),
                position.moveOn(2, -1),
                position.moveOn(-2, -1),
                position.moveOn(1, 2),
                position.moveOn(-1, 2),
                position.moveOn(1, -2),
                position.moveOn(-1, -2)
        );

        for (var horseAttackPosition : horseAttackPositions) {
            board.get(horseAttackPosition).ifPresent(it -> {
                        if (forceRewrite || it == CellState.CAN_PLACE_HORSE) {
                            board.set(horseAttackPosition, CellState.NOT_AVAILABLE);
                        } else if (it == CellState.AVAILABLE) {
                            board.set(horseAttackPosition, CellState.CAN_PLACE_QUEEN);
                        }
                    }
            );
        }
    }

    private enum CellState {
        AVAILABLE(' '),
        QUEEN('♛'),
        HORSE('♞'),
        NOT_AVAILABLE('*'),
        CAN_PLACE_QUEEN('Q'),
        CAN_PLACE_HORSE('H');

        private final char symbol;

        CellState(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }

        public boolean canPlaceQueen() {
            return this == AVAILABLE || this == CAN_PLACE_QUEEN;
        }

        public boolean canPlaceHorse() {
            return this == AVAILABLE || this == CAN_PLACE_HORSE;
        }
    }

    private static final class Position {

        private final int row;
        private final int column;

        public static Position of(int index, int columnCount) {
            return new Position(index / columnCount, index % columnCount);
        }

        public Position(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public Position moveOn(int rowDiff, int columnDiff) {
            return new Position(row + rowDiff, column + columnDiff);
        }
    }

    private static final class Board {

        private final int rowCount;
        private final int columnCount;
        private final CellState[] cells; // In Java, it is more convenient to use 1d array instead of 2d

        static Board of(int rowCount, int columnCount) {
            var cells = new CellState[rowCount * columnCount];

            Arrays.fill(cells, CellState.AVAILABLE);

            return new Board(rowCount, columnCount, cells);
        }

        private Board(int rowCount, int columnCount, CellState[] cells) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.cells = cells;
        }

        public int getRowCount() {
            return rowCount;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public CellState get(int row, int column) {
            return cells[indexMapping(row, column)];
        }

        public Optional<CellState> get(Position position) {
            if (position.getRow() < 0 || position.getRow() >= rowCount || position.getColumn() < 0 || position.getColumn() >= columnCount) {
                return Optional.empty();
            }

            return Optional.of(getUnsafe(position));
        }

        public CellState getUnsafe(Position position) {
            return get(position.getRow(), position.getColumn());
        }

        public void set(Position position, CellState value) {
            set(position.getRow(), position.getColumn(), value);
        }

        public void set(int row, int column, CellState value) {
            cells[indexMapping(row, column)] = value;
        }

        public Board copy() {
            return new Board(rowCount, columnCount, Arrays.copyOf(cells, cells.length));
        }

        public Stream<Position> lookFor(Predicate<CellState> predicate) {
            return IntStream.range(0, cells.length)
                    .filter(it -> predicate.test(cells[it]))
                    .mapToObj(it -> Position.of(it, columnCount));
        }

        public String asString() {
            return asString(false);
        }

        public String asString(boolean detailed) {
            var stringBuilder = new StringBuilder();
            var divider = "+-".repeat(columnCount) + "+\n";

            stringBuilder.append(divider);

            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {
                    var cell = get(row, column);
                    var cellChar = ' ';

                    if (detailed || cell == CellState.QUEEN || cell == CellState.HORSE) {
                        cellChar = cell.getSymbol();
                    }

                    stringBuilder.append('|').append(cellChar);
                }
                stringBuilder.append("|\n");
                stringBuilder.append(divider);
            }

            return stringBuilder.toString();
        }

        private int indexMapping(int i, int j) {
            return i * columnCount + j;
        }
    }
}
