package com.task3513;

import java.util.*;

public class Model { //будет содержать игровую логику и хранить игровое поле
    private static final int FIELD_WIDTH = 4;
    protected int maxTile;
    protected int score;
    private Tile[][] gameTiles;

    private boolean isSaveNeeded = true;
    private Stack<Tile[][]> previousStates;
    private Stack<Integer> previousScores;

    public Model() {
        resetGameTiles();
        this.previousStates = new Stack<>();
        this.previousScores = new Stack<>();

    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> tileList = new ArrayList<>();
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                if (gameTiles[i][j].isEmpty()) {
                    tileList.add(gameTiles[i][j]);
                }
            }
        }
        return tileList;
    }

    private void addTile() {
        List<Tile> tileList = getEmptyTiles();
        if (!tileList.isEmpty()) {
            int listSize = tileList.size();
            int random = (int) (listSize * Math.random());
            tileList.get(random).value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    public void resetGameTiles() {
        this.gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
        this.score = 0;
        this.maxTile = 0;
    }

    private boolean compressTiles(Tile[] tiles) { //Сжатие плиток
        int insertPosition = 0;
        boolean isChange = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    isChange = true;
                }
                insertPosition++;
            }
        }
        return isChange;
    }

    private boolean mergeTiles(Tile[] tiles) { //Слияние плиток одного номинала
        boolean isChange = false;
        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            if (tiles[i].isEmpty()) continue;
            if (tiles[i].value == tiles[i + 1].value) {
                tiles[i].value += tiles[i + 1].value;
                tiles[i + 1] = new Tile();
                if (tiles[i].value > maxTile) maxTile = tiles[i].value;
                score += tiles[i].value;
                isChange = true;
            }
        }
        compressTiles(tiles);
        return isChange;
    }

    private Tile[][] rotateClockwise(Tile[][] tiles) { // поворот матриці на 90 градусів за годинниковою стрілкою
        final int N = tiles.length;
        Tile[][] result = new Tile[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                result[c][N - 1 - r] = tiles[r][c];
            }
        }
        return result;
    }

    public void left() {
        if (isSaveNeeded) saveState(gameTiles);
        boolean moveFlag = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                moveFlag = true;
            }
        }
        if (moveFlag) {
            addTile();
        }
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    public void up() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
    }

    public void down() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
    }

    public boolean canMove() {
        if (!(getEmptyTiles().size() == 0)) return true;

        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            for (int j = 0; j < FIELD_WIDTH - 1; j++) {
                if (gameTiles[i][j].value == gameTiles[i + 1][j].value ||
                        gameTiles[i][j].value == gameTiles[i][j + 1].value) {
                    return true;
                }
            }
        }
        return false;
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] tempTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tempTiles[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(tempTiles);
        previousScores.push(this.score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.isEmpty() && !previousScores.empty()) {
            this.gameTiles = previousStates.pop();
            this.score = previousScores.pop();
        }
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    private MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency = new MoveEfficiency(-1, 0, move);
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), this.score, move);
        }
        rollback();
        return moveEfficiency;
    }

    public boolean hasBoardChanged() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value != previousStates.peek()[i][j].value) {
                    return true;
                }
            }
        }
        return false;
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.offer(getMoveEfficiency(this::left));
        priorityQueue.offer(getMoveEfficiency(this::right));
        priorityQueue.offer(getMoveEfficiency(() -> up()));
        priorityQueue.offer(getMoveEfficiency(() -> down()));

        priorityQueue.peek().getMove().move();
    }
}
