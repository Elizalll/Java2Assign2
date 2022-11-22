package application.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller1 implements Initializable {
    private static int I_AM;
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;
    public Label label;
    public Label winLabel;
    public Label nameLabel;
    @FXML
    private Pane base_square;
    @FXML
    private Rectangle game_panel;
    private static boolean TURN = true, GAME_START = false, GAME_END = false;
    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];

    Socket socket;
    PrintWriter pw;
    BufferedReader br;
    String name = "A";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new socketThread().start();
        game_panel.setOnMouseClicked(event -> {
            if (TURN && GAME_START && !GAME_END) {
                int x = (int) (event.getX() / BOUND);
                int y = (int) (event.getY() / BOUND);
                if (refreshBoard(x, y)) {
                    TURN = false;
                    showTurnInfo();
                    new socketThread2(x, y).start();
                }
            }
        });
    }

    class socketThread extends Thread {
        @Override
        public void run() {
            try {
                socket = new Socket("localhost", 1234);
                pw = new PrintWriter(socket.getOutputStream());
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                pw.write("Client " + name + " here, I wants to play.\nEND\n");
                pw.flush();
                System.out.println(ReadOneSentence(br));// First sentence from the server
                String info;
                do {
                    info = ReadOneSentence(br);
                    if (info.length() > 0) {
                        System.out.println(name + " " + info);
                    }
                } while (!info.equals("Matched."));

                info = ReadOneSentence(br);
                if (Objects.equals(info, "Player 1")) {
                    I_AM = PLAY_1;
                    System.out.println("I am player 1");
                    showTurnInfo();
                    showPlayerInfo();
                    GAME_START = true;
                } else if (Objects.equals(info, "Player 2")) {
                    I_AM = PLAY_2;
                    TURN = false;
                    System.out.println("I am player 2");
                    showTurnInfo();
                    showPlayerInfo();
                    if (!TURN && !GAME_START) {
                        GAME_START = true;
                        System.out.println("GAME_START = true;");
                        String[] infoij;

                        infoij = ReadOneSentence(br).split(",");
                        System.out.println("info0" + Arrays.toString(infoij));
                        System.out.println("info0" + ReadOneSentence(br));

                        int i = Integer.parseInt(infoij[0]), j = Integer.parseInt(infoij[1]);
                        if (refreshBoard(i, j)) {
                            TURN = true;
                            showTurnInfo();
                        }
                    }
                } else {
                    System.out.println("Wrong in I_AM");
                }
            } catch (SocketException e) {
                System.err.println("Oppo/Server Exit");
//                    System.err.println(e.getMessage());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.err.println("Opposite log out");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class socketThread2 extends Thread {
        int x, y;

        socketThread2(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void run() {
            pw.write(x + "," + y + "\nEND\n");
            pw.flush();
            if (!TURN && GAME_START && !GAME_END) {
                try {
                    String[] infoij;
                    String info;
                    info = ReadOneSentence(br);
                    System.out.println("info1:" + info);

                    switch (info) {
                        case "Continue" -> {
                            info = ReadOneSentence(br);
                            System.out.println("info2:" + info);
                            infoij = info.split(",");
                            int i = Integer.parseInt(infoij[0]), j = Integer.parseInt(infoij[1]);
                            if (refreshBoard(i, j)) {

                                info = ReadOneSentence(br);
                                System.out.println("info3:" + info);
                                switch (info) {
                                    case "Continue" -> {
                                        TURN = true;
                                        showTurnInfo();
                                    }
                                    case "Win" -> {
                                        GAME_END = true;
                                        showWinInfo(1);
                                    }
                                    case "Loss" -> {
                                        GAME_END = true;
                                        showWinInfo(2);
                                    }
                                    case "Draw" -> {
                                        GAME_END = true;
                                        showWinInfo(0);
                                    }
                                    default -> System.out.println("Wrong in inner Win switch");
                                }
                            }
                        }
                        case "Win" -> {
                            GAME_END = true;
                            showWinInfo(1);
                        }
                        case "Loss" -> {
                            GAME_END = true;
                            showWinInfo(2);
                        }
                        case "Draw" -> {
                            GAME_END = true;
                            showWinInfo(0);
                        }
                        default -> System.out.println("Wrong in Win switch");
                    }

                } catch (SocketException e) {
                    System.err.println("Oppo/Server Exit");
//                    System.err.println(e.getMessage());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    System.err.println("Opposite log out");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String ReadOneSentence(BufferedReader br) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null && !"END".equals(line)) {
            sb.append(line);
        }
        line = sb.toString();
        if (line.equals("Oppo Exit") || line.length() == 0) {
            throw new SocketException();
        }
        return line;
    }

    void showTurnInfo() {
        Platform.runLater(() -> {
            label.setText("Turn: " + (TURN ? "your" : "opposite"));
        });
    }

    void showPlayerInfo() {
        Platform.runLater(() -> {
            nameLabel.setText("You are " + (I_AM == 1 ? "Circle" : "Cross"));
        });
    }

    void showWinInfo(int win) {
        Platform.runLater(() -> {
            switch (win) {
                case 0 -> winLabel.setText("---------Draw---------");
                case 1 -> winLabel.setText("---Winner :  you! Congratulation!---");
                case 2 -> winLabel.setText("---Winner :  opposite---");
            }
        });
    }

    private boolean refreshBoard(int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            Platform.runLater(this::drawChess);
            return true;
        }
        return false;
    }

    private void drawChess() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        switch (I_AM) {
                            case 1 -> drawCircle(i, j);
                            case 2 -> drawLine(i, j);
                        }
                        break;
                    case PLAY_2:
                        switch (I_AM) {
                            case 1 -> drawLine(i, j);
                            case 2 -> drawCircle(i, j);
                        }
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine(int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
}
